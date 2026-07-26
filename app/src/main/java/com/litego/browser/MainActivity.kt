package com.litego.browser
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webview)
        urlBar = findViewById(R.id.urlBar)
        progress = findViewById(R.id.progress)
        val goBtn = findViewById<Button>(R.id.goBtn)
        val s = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.useWideViewPort = true
        s.loadWithOverviewMode = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (AdBlocker.isAd(url)) return true
                return false
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) { progress.visibility = View.VISIBLE; urlBar.setText(url) }
            override fun onPageFinished(view: WebView?, url: String?) { progress.visibility = View.GONE }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) { progress.progress = newProgress }
        }
        goBtn.setOnClickListener { loadFromBar() }
        urlBar.setOnEditorActionListener { _, _, _ -> loadFromBar(); true }
        webView.loadUrl(getString(R.string.home_url))
    }
    private fun loadFromBar() {
        var input = urlBar.text.toString().trim()
        if (input.isEmpty()) return
        if (!input.startsWith("http")) { if (input.contains(".")) input = "https://$input" else input = "https://duckduckgo.com/?q=$input" }
        webView.loadUrl(input)
    }
    override fun onBackPressed() { if (webView.canGoBack()) webView.goBack() else super.onBackPressed() }
}
