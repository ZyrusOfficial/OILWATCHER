package com.oilwatcher.monitor.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Oil Watcher typography system.
 *
 * Design Language:
 * - Clash Display / Epilogue for headlines (editorial, authoritative)
 * - Manrope for body (geometric, legible, modern)
 * - Labels: uppercase with wide tracking for "archive utility" feel
 *
 * Note: Fonts load from Google Fonts at runtime via Compose's
 * downloadable fonts or fall back to system sans-serif.
 * For production, bundle the font files in res/font/.
 */

// Font families — using system fallbacks until fonts are bundled
// Replace with actual Font resources when .ttf/.otf files are added to res/font/
val ClashDisplay = FontFamily.Default   // TODO: Replace with bundled Clash Display
val Epilogue = FontFamily.Default       // TODO: Replace with bundled Epilogue
val Manrope = FontFamily.Default        // TODO: Replace with bundled Manrope
val DMSans = FontFamily.Default         // TODO: Replace with bundled DM Sans

/**
 * The app typography hierarchy.
 * Maps to the design system's editorial typography treatment.
 */
val OilWatcherTypography = Typography(
    // ── Display ── (Hero moments, large stat numbers)
    displayLarge = TextStyle(
        fontFamily = Epilogue,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.02).sp  // Tight tracking for headlines
    ),
    displayMedium = TextStyle(
        fontFamily = Epilogue,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.02).sp
    ),
    displaySmall = TextStyle(
        fontFamily = Epilogue,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.01).sp
    ),

    // ── Headlines ── (Section headers, card titles)
    headlineLarge = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.01).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.01).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Epilogue,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),

    // ── Title ── (List item titles, station names)
    titleLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),

    // ── Body ── (Descriptions, form text, readable content)
    bodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),

    // ── Label ── (Category headers, badges — the "archive" style)
    // In the mockups: 10-12px, UPPERCASE, tracking-widest, extra-bold
    labelLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp  // Wide tracking for "utility" feel
    ),
    labelMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 2.sp   // Extra wide — "SESSION ID", "POINTS EARNED"
    ),
)
