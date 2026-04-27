package com.oilwatcher.monitor.presentation.navigation

/**
 * All navigation routes in the app.
 * Native screens use Compose destinations.
 * WebView screens load HTML from assets.
 */
object Routes {
    // ── Auth Screens (WebView) ──
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"

    // ── Native Screens (Jetpack Compose) ──
    const val MAP = "map"
    const val CAMERA = "camera"
    const val ANALYZE = "analyze/{imageUri}"

    // Helper to create analyze route with encoded URI
    fun analyze(encodedUri: String) = "analyze/$encodedUri"

    // ── WebView Screens (HTML from assets) ──
    const val STATION_DETAILS = "station/{stationId}"
    const val HISTORY = "history"
    const val LEADERBOARD = "leaderboard"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    // Helper to create station details route with ID
    fun stationDetails(stationId: String) = "station/$stationId"
}

/**
 * Bottom navigation destinations.
 */
enum class BottomNavItem(
    val route: String,
    val label: String,
    val iconOutlined: String,
    val iconFilled: String
) {
    MAP(
        route = Routes.MAP,
        label = "Map",
        iconOutlined = "map",
        iconFilled = "map"
    ),
    CONTRIBUTE(
        route = Routes.CAMERA,
        label = "Contribute",
        iconOutlined = "photo_camera",
        iconFilled = "photo_camera"
    ),
    COMMUNITY(
        route = Routes.HISTORY,
        label = "Community",
        iconOutlined = "group",
        iconFilled = "group"
    ),
    SETTINGS(
        route = Routes.SETTINGS,
        label = "Settings",
        iconOutlined = "settings",
        iconFilled = "settings"
    );
}
