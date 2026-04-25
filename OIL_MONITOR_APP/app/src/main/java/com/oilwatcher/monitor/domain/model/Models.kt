package com.oilwatcher.monitor.domain.model



/**
 * Core domain models for Oil Watcher.
 */

/** A gas station with its latest price data. */
data class Station(
    val id: String = "",
    val name: String = "",
    val brand: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val latestPrices: FuelPrices = FuelPrices(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val lastUpdatedBy: String = "",
    val lastUpdatedByName: String = "",
)

/** Fuel prices for a station. Null means not reported. */
data class FuelPrices(
    val regular: Double? = null,
    val midgrade: Double? = null,
    val premium: Double? = null,
    val diesel: Double? = null,
)

/** A user's price contribution. */
data class Contribution(
    val id: String = "",
    val userId: String = "",
    val stationId: String = "",
    val stationName: String = "",
    val stationAddress: String = "",
    val prices: FuelPrices = FuelPrices(),
    val fuelType: FuelType = FuelType.REGULAR,
    val photoUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val pointsEarned: Int = 0,
    val verified: Boolean = false,
)

/** Fuel types the app supports. */
enum class FuelType(val label: String, val pointsValue: Int) {
    REGULAR("Regular", 25),
    MIDGRADE("Midgrade", 25),
    PREMIUM("Premium", 30),
    DIESEL("Diesel", 25),
    ELECTRIC("Electric", 15);

    companion object {
        fun fromLabel(label: String): FuelType? =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
    }
}

/** User profile with community stats. */
data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val totalContributions: Int = 0,
    val accuracyRate: Float = 0f,
    val lifetimePoints: Int = 0,
    val currentRank: Int = 0,
    val weeklyRankChange: Int = 0,
    val proStatus: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Price tier for map marker color coding. */
enum class PriceTier {
    CHEAP,      // Green — below average
    AVERAGE,    // Dark — average
    EXPENSIVE;  // White — above average

    companion object {
        fun fromPrice(price: Double, averagePrice: Double): PriceTier = when {
            price < averagePrice * 0.95 -> CHEAP
            price > averagePrice * 1.05 -> EXPENSIVE
            else -> AVERAGE
        }
    }
}

/**
 * Internal model: a price candidate extracted by ML Kit OCR.
 * Used between the camera and analyze screens.
 */
data class PriceCandidate(
    val value: Double,
    val topPosition: Int = 0,      // Y-position on image (for grade mapping)
    val confidence: Float = 0f,
    val rawText: String = "",
    val fuelGrade: FuelType? = null,
) {
    val isDetected: Boolean get() = confidence >= 0.8f
    val isLowConfidence: Boolean get() = confidence in 0.5f..0.8f
}

/** Leaderboard entry for community rankings. */
data class LeaderboardEntry(
    val userId: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val rank: Int = 0,
    val totalPoints: Int = 0,
    val weeklyChange: Int = 0,
    val tier: String = "",
)
