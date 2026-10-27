package com.ready2play.launcher

import android.annotation.SuppressLint
import android.app.Activity
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
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Fullscreen & No Title
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    // Bridge Penghubung JavaScript <-> Native Android
    inner class AndroidBridge(private val context: Context) {

        @JavascriptInterface
        fun isAndroid(): Boolean = true

        @JavascriptInterface
        fun getInstalledApps(): String {
            return try {
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val appsList = JSONArray()
                val resolveList = pm.queryIntentActivities(intent, 0)

                for (resolveInfo in resolveList) {
                    val pkgName = resolveInfo.activityInfo.packageName
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
                appsList.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                "[]"
            }
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
            return try {
                prefs.getString("custom_ports", "[]") ?: "[]"
            } catch (e: Exception) {
                "[]"
            }
        }

        @JavascriptInterface
        fun saveCustomPorts(json: String) {
            try {
                prefs.edit().putString("custom_ports", json).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JavascriptInterface
        fun saveInstalledApps(json: String) {
            try {
                prefs.edit().putString("installed_apps_pref", json).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JavascriptInterface
        fun getLastPlayed(): String {
            return try {
                prefs.getString("last_played", "{}") ?: "{}"
            } catch (e: Exception) {
                "{}"
            }
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
            (context as? Activity)?.runOnUiThread {
                context.moveTaskToBack(true)
            }
        }

        private fun drawableToBase64(drawable: Drawable): String {
            return try {
                val bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                    drawable.bitmap
                } else {
                    val w = if (drawable.intrinsicWidth in 1..256) drawable.intrinsicWidth else 96
                    val h = if (drawable.intrinsicHeight in 1..256) drawable.intrinsicHeight else 96
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bmp)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bmp
                }
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, stream)
                val byteArray = stream.toByteArray()
                Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } catch (e: Exception) {
                ""
            }
        }
    }
}    
