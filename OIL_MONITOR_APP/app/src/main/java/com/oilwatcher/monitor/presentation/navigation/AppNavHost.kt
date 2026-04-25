package com.oilwatcher.monitor.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oilwatcher.monitor.presentation.native_screens.analyze.AnalyzeScreen
import com.oilwatcher.monitor.presentation.native_screens.camera.CameraScreen
import com.oilwatcher.monitor.presentation.native_screens.map.MapScreen
import com.oilwatcher.monitor.presentation.webview.WebViewScreen
import com.oilwatcher.monitor.presentation.webview.WebViewModel

/**
 * Main navigation host for the app.
 *
 * Architecture:
 * - 3 screens are native Compose (Camera, Map, Analyze)
 * - 5 screens are WebView (Station Details, History, Leaderboard, Profile, Settings)
 * - BottomNavBar is always native, wrapping all content
 * - Bottom nav hides on Camera and Analyze screens (immersive)
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    webViewModel: WebViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.MAP
    
    val webUiState by webViewModel.uiState.collectAsStateWithLifecycle()

    // Hide bottom nav on immersive screens (camera, analyze, station details)
    val showBottomNav = currentRoute in listOf(
        Routes.MAP,
        Routes.HISTORY,
        Routes.LEADERBOARD,
        Routes.PROFILE,
        Routes.SETTINGS,
    )

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
            startDestination = Routes.MAP,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // ══════════════════════════════════════
            //  NATIVE SCREENS (Jetpack Compose)
            // ══════════════════════════════════════

            composable(Routes.MAP) {
                MapScreen(
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
                    assetPath = "webview/settings.html",
                    screenId = "settings",
                    navController = navController,
                    extraData = webUiState.dataPayload,
                    onAction = { webViewModel.handleAction("settings", it) }
                )
            }
        }
    }
}
