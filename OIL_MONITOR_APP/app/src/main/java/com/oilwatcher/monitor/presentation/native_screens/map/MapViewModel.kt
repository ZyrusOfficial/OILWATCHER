package com.oilwatcher.monitor.presentation.native_screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oilwatcher.monitor.data.location.LocationClient
import com.oilwatcher.monitor.domain.model.Station
import com.oilwatcher.monitor.domain.repository.StationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

data class MapUiState(
    val stations: List<Station> = emptyList(),
    val userLocation: GeoPoint? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val locationClient: LocationClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun getUserLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val location = locationClient.getCurrentLocation()
            if (location != null) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                _uiState.update { 
                    it.copy(
                        userLocation = geoPoint,
                        isLoading = false
                    ) 
                }
                // Fetch stations around this location
                fetchStationsInBounds(
                    northEastLat = location.latitude + 0.1,
                    northEastLng = location.longitude + 0.1,
                    southWestLat = location.latitude - 0.1,
                    southWestLng = location.longitude - 0.1
                )
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Could not get location"
                    ) 
                }
            }
        }
    }

    fun fetchStationsInBounds(
        northEastLat: Double,
        northEastLng: Double,
        southWestLat: Double,
        southWestLng: Double
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = stationRepository.getStationsInBounds(
                northEastLat = northEastLat,
                northEastLng = northEastLng,
                southWestLat = southWestLat,
                southWestLng = southWestLng
            )
            result.onSuccess { stations ->
                _uiState.update { it.copy(stations = stations, isLoading = false, error = null) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun searchArea(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = stationRepository.searchStations(query)
            result.onSuccess { stations ->
                _uiState.update { it.copy(stations = stations, isLoading = false, error = null) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
