package com.oilwatcher.monitor.presentation.auth

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.navigation.NavController
import com.oilwatcher.monitor.presentation.navigation.Routes

/**
 * JavaScript bridge for authentication WebView screens.
 *
 * Exposed to JavaScript as `AndroidBridge` (same name as the main bridge
 * for consistency — auth screens and main screens are never loaded simultaneously).
 *
 * Handles:
 * - Email/password login and sign-up
 * - Password reset
 * - Navigation between auth screens
 * - Google/Facebook/AuthKey placeholders
 *
 * Results are delivered back to the WebView via `receiveAuthResult(status, message)`.
 */
class AuthBridge(
    private val navController: NavController,
    private val webView: WebView,
    private val authViewModel: AuthViewModel,
) {
    companion object {
        private const val TAG = "AuthBridge"
    }

    /**
     * Token returned by [AuthViewModel.setAuthResultCallback]. We pass this
     * back to [clearAuthResultCallback] in [cleanup] so that only the *current*
     * bridge can clear its own callback — preventing a stale dispose from an
     * old composable from nuking the new screen's callback.
     */
    private val callbackToken: Int

    init {
        // Set up the callback so auth results are relayed to the WebView
        callbackToken = authViewModel.setAuthResultCallback { success, message ->
            val status = if (success) "success" else "error"
            val escapedMessage = message.replace("'", "\\'").replace("\"", "\\\"")
            Log.d(TAG, "receiveAuthResult: status=$status, message=$message")
            webView.post {
                webView.evaluateJavascript(
                    "if(typeof receiveAuthResult === 'function') { receiveAuthResult('$status', '$escapedMessage'); }",
                    null
                )
                // If login/signup succeeded, navigate to map after a brief delay
                if (success) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            navController.navigate(Routes.MAP) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Navigation to MAP failed", e)
                        }
                    }, 600) // Brief delay to let the user see the success message
                }
            }
        }
    }

    // ══════════════════════════════════════
    //  Auth Operations
    // ══════════════════════════════════════

    @JavascriptInterface
    fun login(email: String, password: String) {
        Log.d(TAG, "login called from JS: email=$email")
        authViewModel.login(email, password)
    }

    @JavascriptInterface
    fun signUp(name: String, email: String, password: String) {
        Log.d(TAG, "signUp called from JS: name=$name, email=$email")
        authViewModel.signUp(name, email, password)
    }

    @JavascriptInterface
    fun resetPassword(email: String) {
        Log.d(TAG, "resetPassword called from JS: email=$email")
        authViewModel.resetPassword(email)
    }

    // ══════════════════════════════════════
    //  Social / Alt Auth (Placeholders)
    // ══════════════════════════════════════

    @JavascriptInterface
    fun loginWithGoogle() {
        Log.d(TAG, "loginWithGoogle called from JS")
        authViewModel.loginWithGoogle(webView.context)
    }

    @JavascriptInterface
    fun loginWithFacebook() {
        webView.post {
            Toast.makeText(
                webView.context,
                "Facebook Login coming soon! Please use email/password.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @JavascriptInterface
    fun loginWithAuthKey() {
        webView.post {
            Toast.makeText(
                webView.context,
                "Auth Key login coming soon!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ══════════════════════════════════════
    //  Navigation
    // ══════════════════════════════════════

    @JavascriptInterface
    fun navigate(screen: String) {
        Log.d(TAG, "navigate called from JS: screen=$screen")
        webView.post {
            try {
                when (screen) {
                    "login" -> navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                    "signup" -> navController.navigate(Routes.SIGNUP) {
                        launchSingleTop = true
                    }
                    "forgot_password" -> navController.navigate(Routes.FORGOT_PASSWORD) {
                        launchSingleTop = true
                    }
                    "back" -> navController.popBackStack()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Navigation failed for screen=$screen", e)
            }
        }
    }

    /**
     * Call this when the WebView is being disposed to avoid leaking the callback.
     * Uses the token so only the owning bridge clears its callback.
     */
    fun cleanup() {
        Log.d(TAG, "cleanup: token=$callbackToken")
        authViewModel.clearAuthResultCallback(callbackToken)
    }
}
