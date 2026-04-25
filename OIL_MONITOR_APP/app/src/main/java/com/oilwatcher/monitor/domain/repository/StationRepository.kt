package com.oilwatcher.monitor.domain.repository

import com.oilwatcher.monitor.domain.model.Station
import kotlinx.coroutines.flow.Flow

/**
 * Repository for fetching and managing Station data.
 */
interface StationRepository {

    /**
     * Fetch stations within a specific map bounding box.
     */
    suspend fun getStationsInBounds(
        northEastLat: Double,
        northEastLng: Double,
        southWestLat: Double,
        southWestLng: Double
    ): Result<List<Station>>

    /**
     * Get a specific station by its ID.
     */
    suspend fun getStationById(stationId: String): Result<Station>

    /**
     * Search for stations by name or address.
     */
    suspend fun searchStations(query: String): Result<List<Station>>
    
    /**
     * Real-time updates for a specific station (e.g. while viewing details).
     */
    fun getStationUpdates(stationId: String): Flow<Station>
}
