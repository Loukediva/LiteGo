
package com.litego.browser
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar
    private var nightMode = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        urlBar = findViewById(R.id.urlBar)
        progress = findViewById(R.id.progress)
        val goBtn = findViewById<Button>(R.id.goBtn)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        val btnTabs = findViewById<ImageButton>(R.id.btnTabs)

        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.javaScriptCanOpenWindowsAutomatically = true
        s.setSupportMultipleWindows(false)
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.useWideViewPort = true
        s.loadWithOverviewMode = true
        s.mediaPlaybackRequiresUserGesture = false
        s.allowFileAccess = true
        s.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        s.userAgentString = "Mozilla/5.0 (Linux; Android 12; SM-M127F Build/SP1A.210812.016) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36 Via/5.8"
        
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (AdBlocker.isAd(url)) return true
                view?.loadUrl(url)
                return true
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progress.visibility = View.VISIBLE
                if (!urlBar.hasFocus()) urlBar.setText(url)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                progress.visibility = View.GONE
                if (!urlBar.hasFocus()) urlBar.setText(url)
            }
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url.toString()
                if (AdBlocker.isAd(url)) return WebResourceResponse("text/plain","utf-8",null)
                return null
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
            }
        }

        webView.setDownloadListener { url, _, contentDisposition, mimetype, _ ->
            if (url.endsWith(".mp4") || url.endsWith(".m4a") || url.contains("video") || mimetype.startsWith("video")) {
                showVideoDialog(url)
            } else {
                try {
                    val request = DownloadManager.Request(Uri.parse(url))
                    request.setMimeType(mimetype)
                    request.addRequestHeader("User-Agent", webView.settings.userAgentString)
                    request.setDescription("Téléchargement LiteGo")
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype))
                    request.allowScanningByMediaScanner()
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype))
                    (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                    Toast.makeText(this, "Téléchargement lancé", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(this, "Erreur: " + url, Toast.LENGTH_SHORT).show() }
            }
        }

        goBtn.setOnClickListener { loadFromBar() }
        urlBar.setOnEditorActionListener { _, _, _ -> loadFromBar(); true }
        btnMenu.setOnClickListener { showMenu() }
        btnTabs.setOnClickListener { if (webView.canGoBack()) webView.goBack() else Toast.makeText(this,"Pas d'historique",Toast.LENGTH_SHORT).show() }

        webView.loadUrl(getString(R.string.home_url))
    }

    private fun loadFromBar() {
        var input = urlBar.text.toString().trim()
        if (input.isEmpty()) return
        if (!input.startsWith("http")) {
            if (input.contains(".")) input = "https://$input" else input = "https://duckduckgo.com/?q=$input"
        }
        webView.loadUrl(input)
        webView.requestFocus()
    }

    private fun showVideoDialog(videoUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("Video detectee")
            .setMessage("Veux-tu telecharger cette video ?

$videoUrl")
            .setPositiveButton("Telecharger") { _, _ ->
                val request = DownloadManager.Request(Uri.parse(videoUrl))
                request.setTitle("video_litego.mp4")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "litego_video_${System.currentTimeMillis()}.mp4")
                (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
                Toast.makeText(this, "Telechargement video lance", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Lire") { _, _ -> webView.loadUrl(videoUrl) }
            .setNeutralButton("Annuler", null)
            .show()
    }

    private fun showMenu() {
        val options = arrayOf(if(nightMode) "Mode jour" else "Mode nuit", "Actualiser", "Accueil", "Effacer historique", "A propos LiteGo")
        AlertDialog.Builder(this).setTitle("LiteGo Menu").setItems(options) { _, which ->
            when(which) {
                0 -> toggleNight()
                1 -> webView.reload()
                2 -> webView.loadUrl(getString(R.string.home_url))
                3 -> { webView.clearHistory(); webView.clearCache(true); Toast.makeText(this,"Nettoye",Toast.LENGTH_SHORT).show() }
                4 -> Toast.makeText(this,"LiteGo v2.0 - Via Engine - Ultra leger",Toast.LENGTH_LONG).show()
            }
        }.show()
    }

    private fun toggleNight() {
        nightMode = !nightMode
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webView.settings, if(nightMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF)
        }
        if (nightMode) webView.evaluateJavascript("document.documentElement.style.filter='invert(1) hue-rotate(180deg)';", null)
        else webView.evaluateJavascript("document.documentElement.style.filter='';", null)
    }

    override fun onBackPressed() { if (webView.canGoBack()) webView.goBack() else super.onBackPressed() }
}
