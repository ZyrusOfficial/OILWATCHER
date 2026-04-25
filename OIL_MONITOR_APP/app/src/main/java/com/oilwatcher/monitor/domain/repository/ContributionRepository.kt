package com.oilwatcher.monitor.domain.repository

import android.net.Uri

/**
 * Repository interface for managing user contributions (receipt uploads and price reporting).
 */
interface ContributionRepository {
    
    /**
     * Compresses the local image URI, uploads it to Firebase Storage,
     * writes a Contribution document to Firestore attached to the Station,
     * and updates the Station's top-level prices.
     *
     * @return Result object containing the new Contribution ID on success.
     */
    suspend fun uploadContribution(
        stationId: String,
        imageUri: Uri,
        prices: Map<String, Double>
    ): Result<String>
}
