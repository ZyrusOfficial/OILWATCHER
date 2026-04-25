package com.oilwatcher.monitor.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface LocationClient {
    suspend fun getCurrentLocation(): Location?
}

@Singleton
class LocationClientImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationClient {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        return try {
            // Note: Permissions should be checked by the caller (ViewModel/UI)
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
