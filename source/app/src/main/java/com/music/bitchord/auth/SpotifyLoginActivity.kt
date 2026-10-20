package com.music.bitchord.auth

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.spotify.SpotifyLoginPolicy

/** A single native WebView per Activity, outside animated/recomposing music UI.
 * Page progress, cookie polling and keyboard changes never create/reload its document.
 * Only initial creation or an explicit Retry tap may initiate a fresh login request.
 */
class SpotifyLoginActivity : ComponentActivity() {
    private lateinit var web: WebView
    private lateinit var progress: ProgressBar
    private lateinit var status: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var delivered = false
    private var resumed = false
    private val poll = object : Runnable {
        override fun run() {
            if (!resumed || delivered) return
            captureSession()
            if (!delivered) handler.postDelayed(this, 750L)
        }
    }
    private val slowPage = Runnable {
        if (!delivered && !isFinishing) showError("Spotify is taking longer than expected. You can keep waiting or tap Retry.")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(18,18,18)) }
        val toolbar = LinearLayout(this)
        toolbar.addView(Button(this).apply { text = "Back"; setOnClickListener { onBackPressedDispatcher.onBackPressed() } })
        toolbar.addView(TextView(this).apply { text = "Spotify sign-in"; textSize = 18f; setTextColor(Color.WHITE); gravity = android.view.Gravity.CENTER_VERTICAL }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        toolbar.addView(Button(this).apply { text = "Retry"; setOnClickListener { retry() } })
        root.addView(toolbar)
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100 }
        root.addView(progress, LinearLayout.LayoutParams(-1, (3 * resources.displayMetrics.density).toInt()))
        status = TextView(this).apply { setTextColor(Color.WHITE); setPadding(16,12,16,12); visibility = View.GONE }
        root.addView(status)
        web = WebView(this).apply {
            setBackgroundColor(Color.rgb(18,18,18))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, value: Int) {
                    this@SpotifyLoginActivity.progress.progress = value
                    // Reserve the thin bar's space, avoiding repeated document reflow.
                    this@SpotifyLoginActivity.progress.visibility = if (value < 100) View.VISIBLE else View.INVISIBLE
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (!request.isForMainFrame) return false
                    if (SpotifyLoginPolicy.allowed(request.url.toString())) return false
                    showError("This link cannot be opened in Spotify sign-in. Use Back to return.")
                    return true
                }
                override fun onPageStarted(view: WebView, url: String?, icon: android.graphics.Bitmap?) {
                    status.visibility = View.GONE
                    handler.removeCallbacks(slowPage)
                    handler.postDelayed(slowPage, 30_000L)
                }
                override fun onPageFinished(view: WebView, url: String?) {
                    handler.removeCallbacks(slowPage)
                    captureSession()
                }
                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    if (request.isForMainFrame) { handler.removeCallbacks(slowPage); showError("Spotify could not load. Check your connection, then tap Retry.") }
                }
                override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                    if (request.isForMainFrame && response.statusCode >= 400) {
                        handler.removeCallbacks(slowPage)
                        showError("Spotify returned HTTP ${response.statusCode}. Please try again later.")
                    }
                }
                // Default SSL handling is retained: never proceed past a TLS error.
            }
        }
        root.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(root)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if (web.canGoBack()) web.goBack() else finish() }
        })
        // Restore without reloading, including Activity/process recreation.
        val restored = savedInstanceState?.getBundle("spotify_web")?.let(web::restoreState)
        if (restored == null) web.loadUrl(LOGIN_URL)
    }

    private fun showError(message: String) { status.text = message; status.visibility = View.VISIBLE }
    private fun retry() {
        if (delivered) return
        status.visibility = View.GONE
        web.stopLoading()
        web.clearCache(true) // HTTP assets only; retain cookies and DOM storage.
        val current = web.url
        if (SpotifyLoginPolicy.allowed(current)) web.reload() else web.loadUrl(LOGIN_URL)
    }
    private fun captureSession() {
        if (delivered || !SpotifyLoginPolicy.completedLanding(web.url)) return
        val cookie = SpotifyLoginPolicy.sessionCookie(CookieManager.getInstance().getCookie("https://open.spotify.com")) ?: return
        delivered = true
        handler.removeCallbacksAndMessages(null)
        CookieManager.getInstance().flush()
        AppSettings.setSpotifySpdcToken(cookie)
        setResult(RESULT_OK) // Never pass the credential in an Intent or log.
        finish()
    }
    override fun onResume() { super.onResume(); web.onResume(); resumed = true; handler.removeCallbacks(poll); handler.post(poll) }
    override fun onPause() { resumed = false; handler.removeCallbacks(poll); web.onPause(); super.onPause() }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle("spotify_web", Bundle().also { web.saveState(it) })
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        (web.parent as? ViewGroup)?.removeView(web)
        web.stopLoading(); web.destroy()
        super.onDestroy()
    }
    companion object {
        private const val LOGIN_URL = "https://accounts.spotify.com/login?continue=https%3A%2F%2Fopen.spotify.com%2F"
    }
}
