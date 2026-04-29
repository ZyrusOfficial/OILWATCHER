package com.oilwatcher.monitor.presentation.native_screens.analyze

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oilwatcher.monitor.data.ml.PriceExtractor
import com.oilwatcher.monitor.domain.model.FuelType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.oilwatcher.monitor.domain.repository.ContributionRepository
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AnalyzeUiState(
    val imageUri: Uri? = null,
    val isAnalyzing: Boolean = false,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null,
    
    // Extracted Prices
    val regularPrice: String = "",
    val midgradePrice: String = "",
    val premiumPrice: String = "",
    val dieselPrice: String = "",
    
    // Confidence Badges
    val isRegularDetected: Boolean = false,
    val isMidgradeDetected: Boolean = false,
    val isPremiumDetected: Boolean = false,
    val isDieselDetected: Boolean = false
)

@HiltViewModel
class AnalyzeViewModel @Inject constructor(
    private val priceExtractor: PriceExtractor,
    private val contributionRepository: ContributionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyzeUiState())
    val uiState: StateFlow<AnalyzeUiState> = _uiState.asStateFlow()

    fun analyzeImage(context: Context, uriString: String) {
        val uri = Uri.parse(uriString)
        _uiState.update { it.copy(imageUri = uri, isAnalyzing = true, error = null) }
        
        viewModelScope.launch {
            try {
                val candidates = withContext(Dispatchers.IO) {
                    priceExtractor.extractPrices(context, uri)
                }
                
                var reg = ""
                var mid = ""
                var prem = ""
                var diesel = ""
                
                var regDet = false
                var midDet = false
                var premDet = false
                var dieselDet = false
                
                for (candidate in candidates) {
                    val priceStr = candidate.value.toString()
                    when (candidate.fuelGrade) {
                        FuelType.REGULAR -> { reg = priceStr; regDet = candidate.isDetected }
                        FuelType.MIDGRADE -> { mid = priceStr; midDet = candidate.isDetected }
                        FuelType.PREMIUM -> { prem = priceStr; premDet = candidate.isDetected }
                        FuelType.DIESEL -> { diesel = priceStr; dieselDet = candidate.isDetected }
                        else -> {}
                    }
                }
                
                _uiState.update { 
                    it.copy(
                        isAnalyzing = false,
                        regularPrice = reg,
                        midgradePrice = mid,
                        premiumPrice = prem,
                        dieselPrice = diesel,
                        isRegularDetected = regDet,
                        isMidgradeDetected = midDet,
                        isPremiumDetected = premDet,
                        isDieselDetected = dieselDet
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isAnalyzing = false,
                        error = "Failed to extract prices: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun updatePrice(fuelType: FuelType, value: String) {
        _uiState.update { 
            when(fuelType) {
                FuelType.REGULAR -> it.copy(regularPrice = value, isRegularDetected = false)
                FuelType.MIDGRADE -> it.copy(midgradePrice = value, isMidgradeDetected = false)
                FuelType.PREMIUM -> it.copy(premiumPrice = value, isPremiumDetected = false)
                FuelType.DIESEL -> it.copy(dieselPrice = value, isDieselDetected = false)
                else -> it
            }
        }
    }

    fun confirmAndUpload(stationId: String = "dummy_station") {
        val currentState = _uiState.value
        val uri = currentState.imageUri
        if (uri == null) {
            _uiState.update { it.copy(error = "No image found to upload.") }
            return
        }
        
        _uiState.update { it.copy(isUploading = true) }
        
        viewModelScope.launch {
            val pricesMap = mutableMapOf<String, Double>()
            currentState.regularPrice.toDoubleOrNull()?.let { pricesMap[FuelType.REGULAR.name] = it }
            currentState.midgradePrice.toDoubleOrNull()?.let { pricesMap[FuelType.MIDGRADE.name] = it }
            currentState.premiumPrice.toDoubleOrNull()?.let { pricesMap[FuelType.PREMIUM.name] = it }
            currentState.dieselPrice.toDoubleOrNull()?.let { pricesMap[FuelType.DIESEL.name] = it }
            
            val result = contributionRepository.uploadContribution(
                stationId = stationId,
                imageUri = uri,
                prices = pricesMap
            )
            
            if (result.isSuccess) {
                Log.d("AnalyzeViewModel", "Successfully uploaded contribution: ${result.getOrNull()}")
                _uiState.update { it.copy(isUploading = false, uploadSuccess = true) }
            } else {
                _uiState.update { it.copy(error = "Upload failed. Try again.", isUploading = false) }
            }
        }
    }
}
