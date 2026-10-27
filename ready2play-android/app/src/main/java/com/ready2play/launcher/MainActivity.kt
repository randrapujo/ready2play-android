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
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : Activity() {

    private var webView: WebView? = null
    private var prefs: SharedPreferences? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tangkap uncaught crash agar layar tidak langsung force close
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            runOnUiThread {
                showErrorScreen("Uncaught Error", throwable)
            }
        }

        try {
            // Fullscreen & Keep Screen On
            try {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            prefs = getSharedPreferences("ready2play_prefs", Context.MODE_PRIVATE)

            // Buat WebView
            val wv = WebView(this)
            webView = wv
            setContentView(wv)

            val s: WebSettings = wv.settings
            s.javaScriptEnabled = true
            s.domStorageEnabled = true
            s.allowFileAccess = true
            s.allowContentAccess = true
            s.databaseEnabled = true
            s.useWideViewPort = true
            s.loadWithOverviewMode = true
            s.mediaPlaybackRequiresUserGesture = false

            wv.webChromeClient = WebChromeClient()
            wv.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    hideSystemUI()
                }
            }

            // Pasang Javascript Bridge
            wv.addJavascriptInterface(AndroidBridge(this), "Android")

            // Muat HTML
            wv.loadUrl("file:///android_asset/www/index.html")

            hideSystemUI()
        } catch (t: Throwable) {
            showErrorScreen("Startup Error", t)
        }
    }

    private fun showErrorScreen(title: String, t: Throwable) {
        val sv = ScrollView(this)
        val tv = TextView(this)
        tv.setBackgroundColor(Color.parseColor("#0b0e17"))
        tv.setTextColor(Color.parseColor("#ff7a6b"))
        tv.setPadding(40, 40, 40, 40)
        tv.textSize = 14f
        tv.text = "=== Ready2Play Launcher Error Log ===\n\n[$title]\n${t.message}\n\nStacktrace:\n${android.util.Log.getStackTraceString(t)}"
        sv.addView(tv)
        setContentView(sv)
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
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

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

                var count = 0
                for (resolveInfo in resolveList) {
                    if (count >= 50) break
                    val pkgName = resolveInfo.activityInfo?.packageName ?: continue
                    if (pkgName == context.packageName) continue

                    val appName = resolveInfo.loadLabel(pm).toString()
                    val isGame = try {
                        val appInfo = pm.getApplicationInfo(pkgName, 0)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            appInfo.category == ApplicationInfo.CATEGORY_GAME
                        } else {
                            false
                        }
                    } catch (e: Throwable) {
                        false
                    }

                    val iconBase64 = try {
                        val drawable = resolveInfo.loadIcon(pm)
                        drawableToBase64(drawable)
                    } catch (e: Throwable) {
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
                    count++
                }
                appsList.toString()
            } catch (e: Throwable) {
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
            } catch (e: Throwable) {
                e.printStackTrace()
                false
            }
        }

        @JavascriptInterface
        fun getCustomPorts(): String {
            return try {
                prefs?.getString("custom_ports", "[]") ?: "[]"
            } catch (e: Throwable) {
                "[]"
            }
        }

        @JavascriptInterface
        fun saveCustomPorts(json: String) {
            try {
                prefs?.edit()?.putString("custom_ports", json)?.apply()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        @JavascriptInterface
        fun saveInstalledApps(json: String) {
            try {
                prefs?.edit()?.putString("installed_apps_pref", json)?.apply()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        @JavascriptInterface
        fun getLastPlayed(): String {
            return try {
                prefs?.getString("last_played", "{}") ?: "{}"
            } catch (e: Throwable) {
                "{}"
            }
        }

        @JavascriptInterface
        fun setLastPlayed(id: String, timestamp: Long) {
            try {
                val current = JSONObject(getLastPlayed())
                current.put(id, timestamp)
                prefs?.edit()?.putString("last_played", current.toString())?.apply()
            } catch (e: Throwable) {
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
                val size = 72
                val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, size, size)
                drawable.draw(canvas)

                val stream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 80, stream)
                val byteArray = stream.toByteArray()
                Base64.encodeToString(byteArray, Base64.NO_WRAP)
            } catch (e: Throwable) {
                ""
            }
        }
    }
}
