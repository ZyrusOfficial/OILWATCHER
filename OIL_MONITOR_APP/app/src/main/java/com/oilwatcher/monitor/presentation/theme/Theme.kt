package com.oilwatcher.monitor.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Oil Watcher Material 3 Theme.
 *
 * Implements the "Tactile Architect" design language:
 * - Warm paper surfaces with vivid kinetic orange accents
 * - 20dp universal corner radius ("tactile softness")
 * - No-line rule (tonal layering defines boundaries)
 */

private val LightColorScheme = lightColorScheme(
    primary = OilWatcherColors.Primary,
    onPrimary = OilWatcherColors.OnPrimary,
    primaryContainer = OilWatcherColors.PrimaryContainer,
    onPrimaryContainer = OilWatcherColors.OnPrimaryContainer,
    secondary = OilWatcherColors.Secondary,
    onSecondary = OilWatcherColors.OnSecondary,
    secondaryContainer = OilWatcherColors.SecondaryContainer,
    onSecondaryContainer = OilWatcherColors.OnSecondaryContainer,
    tertiary = OilWatcherColors.Tertiary,
    onTertiary = OilWatcherColors.OnTertiary,
    tertiaryContainer = OilWatcherColors.TertiaryContainer,
    onTertiaryContainer = OilWatcherColors.OnTertiaryContainer,
    error = OilWatcherColors.Error,
    onError = OilWatcherColors.OnError,
    errorContainer = OilWatcherColors.ErrorContainer,
    onErrorContainer = OilWatcherColors.OnErrorContainer,
    background = OilWatcherColors.Background,
    onBackground = OilWatcherColors.OnBackground,
    surface = OilWatcherColors.Surface,
    onSurface = OilWatcherColors.OnSurface,
    surfaceVariant = OilWatcherColors.SurfaceVariant,
    onSurfaceVariant = OilWatcherColors.OnSurfaceVariant,
    surfaceTint = OilWatcherColors.SurfaceTint,
    inverseSurface = OilWatcherColors.InverseSurface,
    inverseOnSurface = OilWatcherColors.InverseOnSurface,
    inversePrimary = OilWatcherColors.InversePrimary,
    outline = OilWatcherColors.Outline,
    outlineVariant = OilWatcherColors.OutlineVariant,
    surfaceBright = OilWatcherColors.SurfaceBright,
    surfaceDim = OilWatcherColors.SurfaceDim,
    surfaceContainer = OilWatcherColors.SurfaceContainer,
    surfaceContainerHigh = OilWatcherColors.SurfaceContainerHigh,
    surfaceContainerHighest = OilWatcherColors.SurfaceContainerHighest,
    surfaceContainerLow = OilWatcherColors.SurfaceContainerLow,
    surfaceContainerLowest = OilWatcherColors.SurfaceContainerLowest,
)

// Dark color scheme — initial version, will be refined post-MVP
private val DarkColorScheme = darkColorScheme(
    primary = OilWatcherColors.PrimaryFixedDim,
    onPrimary = OilWatcherColors.OnPrimaryFixed,
    primaryContainer = OilWatcherColors.Primary,
    onPrimaryContainer = OilWatcherColors.PrimaryFixed,
    secondary = OilWatcherColors.SecondaryFixedDim,
    onSecondary = OilWatcherColors.OnSurface,
    background = OilWatcherColors.InverseSurface,
    onBackground = OilWatcherColors.InverseOnSurface,
    surface = OilWatcherColors.InverseSurface,
    onSurface = OilWatcherColors.InverseOnSurface,
    error = OilWatcherColors.Error,
    onError = OilWatcherColors.OnError,
)

/**
 * Shape system — 20dp universal radius from the design system.
 * "Everything follows the 20px or full radius rule to maintain tactile softness."
 */
val OilWatcherShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),      // The 20dp universal radius
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun OilWatcherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Set status bar and navigation bar appearance
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OilWatcherTypography,
        shapes = OilWatcherShapes,
        content = content
    )
}
