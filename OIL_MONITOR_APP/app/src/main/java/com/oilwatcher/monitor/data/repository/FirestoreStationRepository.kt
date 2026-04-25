package com.oilwatcher.monitor.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.oilwatcher.monitor.domain.model.Station
import com.oilwatcher.monitor.domain.repository.StationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Firestore implementation of StationRepository.
 * Note: Until Firebase is fully configured with google-services.json, these calls may fail.
 */
class FirestoreStationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : StationRepository {

    private val stationsCollection = firestore.collection("stations")

    override suspend fun getStationsInBounds(
        northEastLat: Double,
        northEastLng: Double,
        southWestLat: Double,
        southWestLng: Double
    ): Result<List<Station>> {
        return try {
            // Simplified bounds query. In production, use GeoFire or geohashes.
            val snapshot = stationsCollection
                .whereGreaterThanOrEqualTo("latitude", southWestLat)
                .whereLessThanOrEqualTo("latitude", northEastLat)
                .get()
                .await()
            
            // Post-filter longitude locally since Firestore only allows one inequality filter locally
            val stations = snapshot.toObjects(Station::class.java).filter {
                it.longitude in southWestLng..northEastLng
            }
            
            Result.success(stations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStationById(stationId: String): Result<Station> {
        return try {
            val document = stationsCollection.document(stationId).get().await()
            val station = document.toObject(Station::class.java)
            if (station != null) {
                Result.success(station)
            } else {
                Result.failure(Exception("Station not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchStations(query: String): Result<List<Station>> {
        return try {
            // Basic text search. Production would use Algolia or Typesense.
            val snapshot = stationsCollection
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .limit(10)
                .get()
                .await()
            Result.success(snapshot.toObjects(Station::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getStationUpdates(stationId: String): Flow<Station> = callbackFlow {
        val listener = stationsCollection.document(stationId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val station = snapshot?.toObject(Station::class.java)
            if (station != null) {
                trySend(station)
            }
        }
        
        awaitClose { listener.remove() }
    }
}
