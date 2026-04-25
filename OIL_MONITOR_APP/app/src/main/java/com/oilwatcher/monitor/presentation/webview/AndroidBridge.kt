package com.oilwatcher.monitor.presentation.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.navigation.NavController
import com.google.gson.Gson
import com.oilwatcher.monitor.presentation.navigation.Routes

/**
 * JavaScript bridge between WebView HTML and native Android.
 *
 * Exposed to JavaScript as `AndroidBridge`.
 * WebView HTML calls: AndroidBridge.navigate('map'), AndroidBridge.toggleSetting('notifications', true), etc.
 *
 * All methods annotated with @JavascriptInterface run on a background thread,
 * so we post navigation actions to the main thread via the NavController.
 */
class AndroidBridge(
    private val navController: NavController,
    private val webView: WebView,
    private val onAction: (BridgeAction) -> Unit = {},
) {
    private val gson = Gson()

    // ── Navigation ──

    @JavascriptInterface
    fun navigate(screen: String) {
        webView.post {
            when (screen) {
                "map" -> navController.navigate(Routes.MAP) {
                    popUpTo(Routes.MAP) { inclusive = true }
                }
                "camera" -> navController.navigate(Routes.CAMERA)
                // "analyze" requires an imageUri argument — cannot navigate from bridge without one
                "history" -> navController.navigate(Routes.HISTORY)
                "leaderboard" -> navController.navigate(Routes.LEADERBOARD)
                "profile" -> navController.navigate(Routes.PROFILE)
                "settings" -> navController.navigate(Routes.SETTINGS)
                "back" -> navController.popBackStack()
            }
        }
    }

    @JavascriptInterface
    fun navigateToStation(stationId: String) {
        webView.post {
            navController.navigate(Routes.stationDetails(stationId))
        }
    }

    @JavascriptInterface
    fun navigateExternal(lat: Double, lng: Double, stationName: String) {
        webView.post {
            val uri = Uri.parse("google.navigation:q=$lat,$lng&label=${Uri.encode(stationName)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            try {
                webView.context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to browser Google Maps
                val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                webView.context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        }
    }

    // ── Settings ──

    @JavascriptInterface
    fun toggleSetting(key: String, value: Boolean) {
        runOnMain { onAction(BridgeAction.ToggleSetting(key, value)) }
    }

    @JavascriptInterface
    fun signOut() {
        runOnMain { onAction(BridgeAction.SignOut) }
    }

    // ── Data Loading ──

    @JavascriptInterface
    fun loadMore(type: String, offset: Int) {
        runOnMain { onAction(BridgeAction.LoadMore(type, offset)) }
    }

    @JavascriptInterface
    fun refresh() {
        runOnMain { onAction(BridgeAction.Refresh) }
    }

    @JavascriptInterface
    fun filterPeriod(period: String) {
        runOnMain { onAction(BridgeAction.FilterPeriod(period)) }
    }

    // ── Tab Switching ──

    @JavascriptInterface
    fun switchTab(tab: String) {
        webView.post {
            when (tab) {
                "history" -> navController.navigate(Routes.HISTORY) {
                    popUpTo(Routes.HISTORY) { inclusive = true }
                    launchSingleTop = true
                }
                "leaderboard" -> navController.navigate(Routes.LEADERBOARD) {
                    popUpTo(Routes.HISTORY) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    private fun runOnMain(action: () -> Unit) {
        Handler(Looper.getMainLooper()).post { action() }
    }
}

/**
 * Actions that the WebView can trigger via the bridge.
 * These are processed by the ViewModel layer.
 */
sealed class BridgeAction {
    data class ToggleSetting(val key: String, val value: Boolean) : BridgeAction()
    data object SignOut : BridgeAction()
    data class LoadMore(val type: String, val offset: Int) : BridgeAction()
    data object Refresh : BridgeAction()
    data class FilterPeriod(val period: String) : BridgeAction()
}
