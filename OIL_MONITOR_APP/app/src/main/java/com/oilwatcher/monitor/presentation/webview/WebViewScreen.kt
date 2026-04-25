package com.oilwatcher.monitor.presentation.webview

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.gson.Gson

/**
 * Reusable WebView screen composable.
 *
 * Wraps an Android WebView inside Compose, loads HTML from the assets folder,
 * injects the JavaScript bridge, and handles data injection.
 *
 * Optimizations applied:
 * - Hardware acceleration enabled
 * - Overscroll disabled (no bounce glow)
 * - Transparent background (blends with native Compose)
 * - DOM storage enabled for local data
 * - JavaScript enabled for bridge communication
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    assetPath: String,
    screenId: String,
    navController: NavController,
    extraData: Map<String, Any> = emptyMap(),
    onAction: (BridgeAction) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val gson = remember { Gson() }

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            // ── WebView Configuration ──
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false

                // Performance: cache mode for local assets
                cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
            }

            // Transparent background to blend with native
            setBackgroundColor(Color.TRANSPARENT)

            // Disable overscroll glow
            overScrollMode = WebView.OVER_SCROLL_NEVER

            // Enable hardware acceleration
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

            // Prevent WebView from opening links in external browser
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    // Handle javascript: URLs from the bridge
                    if (url.startsWith("javascript:")) return false
                    // Block external navigation — all routing goes through the bridge
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Inject data after the page has fully loaded
                    if (extraData.isNotEmpty()) {
                        val json = gson.toJson(extraData)
                        view?.evaluateJavascript(
                            "if(typeof receiveData === 'function') { receiveData($json); }",
                            null
                        )
                    }
                }
            }

            webChromeClient = WebChromeClient()

            // ── Add JavaScript Bridge ──
            addJavascriptInterface(
                AndroidBridge(
                    navController = navController,
                    webView = this,
                    onAction = onAction,
                ),
                "AndroidBridge"
            )
        }
    }

    // Load the HTML asset
    LaunchedEffect(assetPath) {
        webView.loadUrl("file:///android_asset/$assetPath")
    }

    // Clean up WebView when leaving the composition
    DisposableEffect(Unit) {
        onDispose {
            webView.removeJavascriptInterface("AndroidBridge")
            webView.stopLoading()
            webView.clearHistory()
            webView.destroy() // Fix memory leak since we are not pooling
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize(),
    )
}

/**
 * Inject dynamic data into a loaded WebView page.
 * Call this after `onPageFinished` to update the HTML with real data.
 */
fun WebView.injectData(data: Any) {
    val gson = Gson()
    val json = gson.toJson(data)
    this.evaluateJavascript(
        "if(typeof receiveData === 'function') { receiveData($json); }",
        null
    )
}
