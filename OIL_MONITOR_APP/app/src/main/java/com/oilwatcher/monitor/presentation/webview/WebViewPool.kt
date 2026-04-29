package com.oilwatcher.monitor.presentation.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView

/**
 * A simple pool for WebViews to improve navigation speed.
 * Creating a WebView is expensive, so we keep a couple idle instances.
 */
object WebViewPool {
    private val pool = mutableListOf<WebView>()
    private const val MAX_POOL_SIZE = 2

    @SuppressLint("SetJavaScriptEnabled")
    fun obtain(context: Context): WebView {
        if (pool.isNotEmpty()) {
            return pool.removeAt(pool.size - 1)
        }
        
        return WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                
                cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK

                // Prioritize rendering speed
                try {
                    @Suppress("DEPRECATION")
                    setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
                } catch (e: Exception) {
                    // Ignore, method is deprecated but might be needed
                }
            }

            setBackgroundColor(Color.parseColor("#F9F9F7"))
            overScrollMode = WebView.OVER_SCROLL_NEVER
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
        }
    }

    fun recycle(webView: WebView) {
        // Clean up before putting back in pool
        webView.stopLoading()
        webView.clearHistory()
        webView.loadUrl("about:blank")
        webView.webChromeClient = null
        webView.webViewClient = android.webkit.WebViewClient()
        webView.removeJavascriptInterface("AndroidBridge")
        
        // Remove from parent if still attached
        (webView.parent as? ViewGroup)?.removeView(webView)

        if (pool.size < MAX_POOL_SIZE) {
            pool.add(webView)
        } else {
            webView.destroy()
        }
    }
}
