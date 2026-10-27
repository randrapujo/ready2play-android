package com.ready2play.launcher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("ready2play_prefs", Context.MODE_PRIVATE)

        webView = WebView(this)
        setContentView(webView)

        hideSystemUI()

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.databaseEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.mediaPlaybackRequiresUserGesture = false

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideSystemUI()
            }
        }

        // Daftarkan Javascript Interface Bridge
        webView.addJavascriptInterface(AndroidBridge(this), "Android")

        // Muat aset tampilan web
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }

    override fun onBackPressed() {
        // Alihkan tombol back ke WebView atau keluar ke background
        moveTaskToBack(true)
    }

    // Bridge Penghubung JavaScript <-> Native Android
    inner class AndroidBridge(private val context: Context) {

        @JavascriptInterface
        fun isAndroid(): Boolean = true

        @JavascriptInterface
        fun getInstalledApps(): String {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val appsList = JSONArray()
            val resolveList = pm.queryIntentActivities(intent, 0)

            for (resolveInfo in resolveList) {
                val pkgName = resolveInfo.activityInfo.packageName
                // Jangan masukkan launcher ini sendiri ke dalam list
                if (pkgName == context.packageName) continue

                val appName = resolveInfo.loadLabel(pm).toString()
                val isGame = try {
                    val appInfo = pm.getApplicationInfo(pkgName, 0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        appInfo.category == ApplicationInfo.CATEGORY_GAME
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }

                val iconBase64 = try {
                    val drawable = resolveInfo.loadIcon(pm)
                    drawableToBase64(drawable)
                } catch (e: Exception) {
                    ""
                }

                val appObj = JSONObject().apply {
                    put("id", "app:$pkgName")
                    put("name", appName)
                    put("packageName", pkgName)
                    put("genre", if (isGame) "Game Android" else "Aplikasi")
                    put("source", "android")
                    put("enabled", true)
                    put("cover", if (iconBase64.isNotEmpty()) "data:image/png;base64,$iconBase64" else "")
                }
                appsList.put(appObj)
            }

            return appsList.toString()
        }

        @JavascriptInterface
        fun launchApp(packageName: String): Boolean {
            return try {
                val pm = context.packageManager
                val intent = pm.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        @JavascriptInterface
        fun getCustomPorts(): String {
            return prefs.getString("custom_ports", "[]") ?: "[]"
        }

        @JavascriptInterface
        fun saveCustomPorts(json: String) {
            prefs.edit().putString("custom_ports", json).apply()
        }

        @JavascriptInterface
        fun saveInstalledApps(json: String) {
            prefs.edit().putString("installed_apps_pref", json).apply()
        }

        @JavascriptInterface
        fun getLastPlayed(): String {
            return prefs.getString("last_played", "{}") ?: "{}"
        }

        @JavascriptInterface
        fun setLastPlayed(id: String, timestamp: Long) {
            try {
                val current = JSONObject(getLastPlayed())
                current.put(id, timestamp)
                prefs.edit().putString("last_played", current.toString()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JavascriptInterface
        fun quit() {
            (context as? MainActivity)?.runOnUiThread {
                context.moveTaskToBack(true)
            }
        }

        private fun drawableToBase64(drawable: Drawable): String {
            val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
                val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            val byteArray = stream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.NO_WRAP)
        }
    }
}
