package com.oilwatcher.monitor.presentation.navigation

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oilwatcher.monitor.presentation.auth.AuthBridge
import com.oilwatcher.monitor.presentation.auth.AuthState
import com.oilwatcher.monitor.presentation.auth.AuthViewModel
import com.oilwatcher.monitor.presentation.native_screens.analyze.AnalyzeScreen
import com.oilwatcher.monitor.presentation.native_screens.camera.CameraScreen
import com.oilwatcher.monitor.presentation.native_screens.map.MapScreen
import com.oilwatcher.monitor.presentation.webview.WebViewScreen
import com.oilwatcher.monitor.presentation.webview.WebViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main navigation host for the app.
 *
 * Architecture:
 * - Auth gate: checks FirebaseAuth.currentUser on launch
 *   → logged in → MAP, not logged in → LOGIN
 * - 3 screens are native Compose (Camera, Map, Analyze)
 * - 5 screens are WebView (Station Details, History, Leaderboard, Profile, Settings)
 * - 3 auth screens are WebView (Login, Signup, Forgot Password)
 * - BottomNavBar is always native, wrapping all content
 * - Bottom nav hides on Camera, Analyze, and Auth screens (immersive)
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    webViewModel: WebViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    
    val webUiState by webViewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // Determine start destination based on auth state
    val startDestination = when (authState) {
        is AuthState.Loading -> Routes.LOGIN // Will show loading briefly
        is AuthState.Authenticated -> Routes.MAP
        is AuthState.Unauthenticated -> Routes.LOGIN
    }

    // Hide bottom nav on immersive / auth screens
    val showBottomNav = currentRoute in listOf(
        Routes.MAP,
        Routes.HISTORY,
        Routes.LEADERBOARD,
        Routes.PROFILE,
        Routes.SETTINGS,
    )

    // Observe auth state changes for sign-out navigation
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            // If we're on an app screen (not already on an auth screen), navigate to login
            val authScreens = listOf(Routes.LOGIN, Routes.SIGNUP, Routes.FORGOT_PASSWORD)
            if (currentRoute.isNotEmpty() && currentRoute !in authScreens) {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        // Pop up to map to avoid building up a large back stack
                        popUpTo(Routes.MAP) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                isVisible = showBottomNav,
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.fillMaxSize(),
        ) {
            // ══════════════════════════════════════
            //  AUTH SCREENS (WebView)
            // ══════════════════════════════════════

            composable(Routes.LOGIN) {
                AuthWebViewScreen(
                    assetPath = "webview/auth_login.html",
                    navController = navController,
                    authViewModel = authViewModel,
                )
            }

            composable(Routes.SIGNUP) {
                AuthWebViewScreen(
                    assetPath = "webview/auth_signup.html",
                    navController = navController,
                    authViewModel = authViewModel,
                )
            }

            composable(Routes.FORGOT_PASSWORD) {
                AuthWebViewScreen(
                    assetPath = "webview/auth_forgot_password.html",
                    navController = navController,
                    authViewModel = authViewModel,
                )
            }

            // ══════════════════════════════════════
            //  NATIVE SCREENS (Jetpack Compose)
            // ══════════════════════════════════════

            composable(Routes.MAP) {
                MapScreen(
                    innerPadding = innerPadding,
                    onOpenCamera = { navController.navigate(Routes.CAMERA) },
                    onOpenStation = { stationId ->
                        navController.navigate(Routes.stationDetails(stationId))
                    },
                )
            }

            composable(Routes.CAMERA) {
                CameraScreen(
                    onPhotoCapture = { imageUri ->
                        // Navigate to analyze with the captured image URI safely encoded
                        val encodedUri = android.net.Uri.encode(imageUri)
                        navController.navigate(Routes.analyze(encodedUri))
                    },
                    onClose = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.ANALYZE,
                arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val imageUri = backStackEntry.arguments?.getString("imageUri")
                AnalyzeScreen(
                    imageUri = imageUri,
                    onConfirmUpload = {
                        // After upload, go back to map
                        navController.navigate(Routes.MAP) {
                            popUpTo(Routes.MAP) { inclusive = true }
                        }
                    },
                    onGoBack = { navController.popBackStack() },
                )
            }

            // ══════════════════════════════════════
            //  WEBVIEW SCREENS (HTML from assets)
            // ══════════════════════════════════════

            composable(
                route = Routes.STATION_DETAILS,
                arguments = listOf(navArgument("stationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val stationId = backStackEntry.arguments?.getString("stationId") ?: ""
                WebViewScreen(
                    modifier = Modifier.padding(innerPadding),
                    assetPath = "webview/station_details.html",
                    screenId = "station_details",
                    navController = navController,
                    extraData = mapOf("stationId" to stationId),
                    onAction = { webViewModel.handleAction("station_details", it) }
                )
            }

            composable(Routes.HISTORY) {
                LaunchedEffect(Unit) { webViewModel.loadScreenData("history") }
                WebViewScreen(
                    modifier = Modifier.padding(innerPadding),
                    assetPath = "webview/community_history.html",
                    screenId = "history",
                    navController = navController,
                    extraData = webUiState.dataPayload,
                    onAction = { webViewModel.handleAction("history", it) }
                )
            }

            composable(Routes.LEADERBOARD) {
                LaunchedEffect(Unit) { webViewModel.loadScreenData("leaderboard") }
                WebViewScreen(
                    modifier = Modifier.padding(innerPadding),
                    assetPath = "webview/community_leaderboard.html",
                    screenId = "leaderboard",
                    navController = navController,
                    extraData = webUiState.dataPayload,
                    onAction = { webViewModel.handleAction("leaderboard", it) }
                )
            }

            composable(Routes.PROFILE) {
                LaunchedEffect(Unit) { webViewModel.loadScreenData("profile") }
                WebViewScreen(
                    modifier = Modifier.padding(innerPadding),
                    assetPath = "webview/profile.html",
                    screenId = "profile",
                    navController = navController,
                    extraData = webUiState.dataPayload,
                    onAction = { webViewModel.handleAction("profile", it) }
                )
            }

            composable(Routes.SETTINGS) {
                LaunchedEffect(Unit) { webViewModel.loadScreenData("settings") }
                WebViewScreen(
                    modifier = Modifier.padding(innerPadding),
                    assetPath = "webview/settings.html",
                    screenId = "settings",
                    navController = navController,
                    extraData = webUiState.dataPayload,
                    onAction = { action ->
                        webViewModel.handleAction("settings", action)
                        // If sign-out, also update auth state
                        if (action is com.oilwatcher.monitor.presentation.webview.BridgeAction.SignOut) {
                            authViewModel.signOut()
                        }
                    }
                )
            }
        }
    }
}

/**
 * Reusable WebView screen for authentication flows.
 *
 * Uses [AuthBridge] instead of [AndroidBridge] to handle auth-specific
 * JavaScript calls (login, signUp, resetPassword, etc.).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthWebViewScreen(
    assetPath: String,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "authWebViewAlpha"
    )

    val webView = remember {
        WebView(context).apply {
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

                // Aggressive caching — Material Symbols font cached after first load
                cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK

                // Prioritize rendering speed
                setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
            }

            // Surface-colored background prevents white flash
            setBackgroundColor(android.graphics.Color.parseColor("#F9F9F7"))
            overScrollMode = WebView.OVER_SCROLL_NEVER
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    if (url.startsWith("javascript:")) return false
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    android.util.Log.d("AuthWebView", "onPageFinished: $url")
                    coroutineScope.launch {
                        delay(50)
                        isLoading = false
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: android.webkit.WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    android.util.Log.e("AuthWebView", "WebView error: ${error?.description} (code ${error?.errorCode}) for ${request?.url}")
                }
            }

            // Intercept console.log from JS → logcat for debugging
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        android.util.Log.d("AuthWebView", "JS: ${it.message()} [${it.sourceId()}:${it.lineNumber()}]")
                    }
                    return true
                }
            }
        }
    }

    // Create and attach the AuthBridge
    val authBridge = remember(webView, authViewModel, navController) {
        AuthBridge(
            navController = navController,
            webView = webView,
            authViewModel = authViewModel,
        )
    }

    // Attach bridge FIRST, then load page — avoids race where JS calls
    // AndroidBridge before the interface is registered.
    LaunchedEffect(assetPath, authBridge) {
        webView.removeJavascriptInterface("AndroidBridge")
        webView.addJavascriptInterface(authBridge, "AndroidBridge")
        webView.loadUrl("file:///android_asset/$assetPath")
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            authBridge.cleanup()
            webView.removeJavascriptInterface("AndroidBridge")
            webView.stopLoading()
            webView.clearHistory()
            webView.destroy()
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize().alpha(alpha),
        )
    }
}

