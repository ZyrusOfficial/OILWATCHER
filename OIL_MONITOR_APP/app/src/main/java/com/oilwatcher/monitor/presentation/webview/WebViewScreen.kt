package com.oilwatcher.monitor.presentation.webview

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "webViewAlpha"
    )

    val webView = remember {
        WebViewPool.obtain(context).apply {
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
                    
                    // Brief delay for DOM paint — reduced from 150ms for faster feel
                    coroutineScope.launch {
                        delay(50)
                        isLoading = false
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
            WebViewPool.recycle(webView)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFFF9F9F7)),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize().alpha(alpha),
        )
    }
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
