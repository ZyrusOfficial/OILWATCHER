package com.oilwatcher.monitor.presentation.native_screens.camera

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject

data class CameraUiState(
    val isCapturing: Boolean = false,
    val captureError: String? = null,
    val capturedImageUri: Uri? = null
)

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun takePhoto(
        imageCapture: ImageCapture,
        context: Context,
        onCaptureSuccess: (Uri) -> Unit
    ) {
        _uiState.update { it.copy(isCapturing = true, captureError = null) }

        // Create time-stamped output file
        val photoFile = File(
            context.cacheDir,
            "fuel_price_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    _uiState.update { 
                        it.copy(
                            isCapturing = false,
                            captureError = "Photo capture failed: ${exc.message}"
                        )
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    _uiState.update { 
                        it.copy(
                            isCapturing = false,
                            capturedImageUri = savedUri
                        )
                    }
                    onCaptureSuccess(savedUri)
                }
            }
        )
    }
}
