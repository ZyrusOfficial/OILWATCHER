package com.oilwatcher.monitor.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Oil Watcher color system derived from the design system (DESIGN.md).
 *
 * Uses Material Design 3 token naming.
 * Primary orange is the "Vivid Kinetic" brand accent.
 * Surfaces use a warm "Paper" palette for the editorial aesthetic.
 */
object OilWatcherColors {

    // ── Primary Brand ──
    val Primary = Color(0xFFA63B00)              // Deep warm orange (primary actions, text)
    val PrimaryContainer = Color(0xFFFF5E00)     // Bright vivid orange (CTAs, active states)
    val OnPrimary = Color(0xFFFFFFFF)            // White text on primary
    val OnPrimaryContainer = Color(0xFF531900)   // Dark brown text on container
    val OnPrimaryFixed = Color(0xFF370E00)       // Very dark on light orange surfaces
    val PrimaryFixed = Color(0xFFFFDBCE)         // Light peach
    val PrimaryFixedDim = Color(0xFFFFB599)      // Muted peach
    val InversePrimary = Color(0xFFFFB599)

    // ── Secondary ──
    val Secondary = Color(0xFF5F5E5E)            // Neutral grey
    val SecondaryContainer = Color(0xFFE2DFDE)   // Light warm grey
    val OnSecondary = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFF636262)
    val SecondaryFixed = Color(0xFFE5E2E1)
    val SecondaryFixedDim = Color(0xFFC8C6C5)

    // ── Tertiary (Blue — used for EV, electric accents) ──
    val Tertiary = Color(0xFF0061A4)
    val TertiaryContainer = Color(0xFF0097FB)
    val OnTertiary = Color(0xFFFFFFFF)
    val OnTertiaryContainer = Color(0xFF002D51)
    val TertiaryFixed = Color(0xFFD1E4FF)
    val TertiaryFixedDim = Color(0xFF9FCAFF)

    // ── Error ──
    val Error = Color(0xFFBA1A1A)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnError = Color(0xFFFFFFFF)
    val OnErrorContainer = Color(0xFF93000A)

    // ── Surface System (Paper Palette) ──
    val Surface = Color(0xFFF9F9F7)              // Base paper white
    val SurfaceBright = Color(0xFFF9F9F7)
    val SurfaceDim = Color(0xFFDADAD8)
    val SurfaceContainerLowest = Color(0xFFFFFFFF) // Pure white (elevated cards)
    val SurfaceContainerLow = Color(0xFFF4F4F2)   // Soft paper (subtle indents)
    val SurfaceContainer = Color(0xFFEEEEEC)       // Grouped sections
    val SurfaceContainerHigh = Color(0xFFE8E8E6)   // Heavier grouping
    val SurfaceContainerHighest = Color(0xFFE2E3E1) // Deepest surface
    val SurfaceVariant = Color(0xFFE2E3E1)
    val SurfaceTint = Color(0xFFA63B00)

    // ── On-Surface ──
    val OnSurface = Color(0xFF1A1C1B)            // Charcoal (primary text)
    val OnSurfaceVariant = Color(0xFF5B4137)     // Warm brown-grey (secondary text)
    val OnBackground = Color(0xFF1A1C1B)

    // ── Background ──
    val Background = Color(0xFFF9F9F7)

    // ── Outline ──
    val Outline = Color(0xFF8F7065)              // Warm grey-brown
    val OutlineVariant = Color(0xFFE4BFB1)       // Light peach border

    // ── Inverse (Dark mode seeds) ──
    val InverseSurface = Color(0xFF2F3130)
    val InverseOnSurface = Color(0xFFF1F1EF)

    // ── Semantic Colors (App-specific) ──
    val PriceCheap = Color(0xFF00C853)           // Green — below average
    val PriceAverage = Color(0xFF1A1A1A)         // Dark — average
    val PriceExpensive = Color(0xFFFFFFFF)        // White — above average
    val Accent = Color(0xFF00C853)               // Green — verified, positive
    val TextMain = Color(0xFF1A1A1A)
    val TextMuted = Color(0xFF8E8E8B)
}
