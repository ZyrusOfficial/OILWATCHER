package com.oilwatcher.monitor.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.oilwatcher.monitor.presentation.navigation.AppNavHost
import com.oilwatcher.monitor.presentation.theme.OilWatcherTheme
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.Color
import androidx.activity.SystemBarStyle

/**
 * Main Activity — single-activity architecture.
 *
 * Uses:
 * - Edge-to-edge display (status bar and nav bar transparent)
 * - Splash screen API (Android 12+)
 * - Hilt DI injection
 * - OilWatcherTheme (M3 design tokens)
 * - AppNavHost manages all screen routing
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        setContent {
            OilWatcherTheme {
                AppNavHost()
            }
        }
    }
}
