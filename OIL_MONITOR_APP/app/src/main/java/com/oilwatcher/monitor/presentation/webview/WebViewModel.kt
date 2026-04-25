package com.oilwatcher.monitor.presentation.webview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oilwatcher.monitor.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WebUiState(
    val dataPayload: Map<String, Any> = emptyMap(),
    val isLoading: Boolean = false
)

@HiltViewModel
class WebViewModel @Inject constructor(
    // private val userRepository: UserRepository // TODO: Inject repos here
) : ViewModel() {

    private val _uiState = MutableStateFlow(WebUiState())
    val uiState: StateFlow<WebUiState> = _uiState.asStateFlow()

    fun handleAction(screenId: String, action: BridgeAction) {
        when(action) {
            is BridgeAction.Refresh -> loadScreenData(screenId)
            is BridgeAction.LoadMore -> { /* Handle pagination */ }
            is BridgeAction.ToggleSetting -> { /* Save settings to DataStore */ }
            is BridgeAction.FilterPeriod -> { /* Reload leaderboard for period */ }
            is BridgeAction.SignOut -> { /* Trigger Firebase SignOut */ }
        }
    }

    fun loadScreenData(screenId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val payload = when (screenId) {
                "profile" -> fetchProfileData()
                "history" -> fetchHistoryData()
                "leaderboard" -> fetchLeaderboardData()
                "settings" -> fetchSettingsData()
                else -> emptyMap()
            }

            _uiState.value = _uiState.value.copy(dataPayload = payload, isLoading = false)
        }
    }

    private suspend fun fetchProfileData(): Map<String, Any> {
        // Dummy data for now. Connect to Firestore mapped UserProfile
        return mapOf(
            "name" to "Community Member",
            "handle" to "contributor123",
            "contributions" to 42,
            "accuracy" to 98,
            "rank" to 15
        )
    }

    private suspend fun fetchHistoryData(): Map<String, Any> {
        return mapOf(
            "totalContributions" to 42,
            "lifetimePoints" to 1250,
            "savingsText" to "You helped save the community $45.20 this week!",
            "contributions" to listOf(
                mapOf(
                    "stationId" to "st1",
                    "stationName" to "Shell Route 66",
                    "stationAddress" to "123 Main St",
                    "fuelType" to "REGULAR",
                    "price" to 3.45,
                    "points" to 25,
                    "timeAgo" to "2h ago"
                )
            )
        )
    }

    private suspend fun fetchLeaderboardData(): Map<String, Any> {
        return mapOf(
            "user" to mapOf(
                "name" to "You",
                "rank" to 15,
                "points" to 1250,
                "weeklyChange" to 12
            ),
            "top3" to listOf(
                mapOf("name" to "SpeedyGas", "points" to 15000),
                mapOf("name" to "EcoDriver", "points" to 14200),
                mapOf("name" to "FuelSaver", "points" to 13800)
            ),
            "rankings" to listOf(
                mapOf("rank" to 4, "name" to "LocalHero", "points" to 12000, "change" to 2),
                mapOf("rank" to 5, "name" to "RoadWarrior", "points" to 11500, "change" to -1)
            )
        )
    }

    private suspend fun fetchSettingsData(): Map<String, Any> {
        return mapOf(
            "name" to "Community Member",
            "email" to "user@example.com",
            "notifications" to true,
            "location" to true,
            "darkmode" to false
        )
    }
}
