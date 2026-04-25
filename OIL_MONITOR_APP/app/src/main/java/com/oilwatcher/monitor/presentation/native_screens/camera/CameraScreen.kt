package com.oilwatcher.monitor.presentation.native_screens.camera

import android.Manifest
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.oilwatcher.monitor.presentation.theme.OilWatcherColors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Camera Capture Screen — Native Compose.
 *
 * Full-screen camera viewfinder with CameraX.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onPhotoCapture: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Camera Permissions
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // CameraX configuration states
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    
    val imageCapture = remember {
        ImageCapture.Builder()
            .setFlashMode(flashMode)
            .build()
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    // Update flash mode when toggled
    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }

    // Scan line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanLine",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1A1C1B)),
    ) {
        
        if (cameraPermissionState.status.isGranted) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                            
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(previewView.context))
                }
            )
        } else {
            // Permission Denied UI
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Camera permission is required to contribute prices.",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        // ── Header Overlay ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f)),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close camera",
                    tint = Color.White,
                )
            }

            // Brand indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = OilWatcherColors.PrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "FuelTrack Capture",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }

            // Switch camera
            IconButton(
                onClick = { 
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
            ) {
                Icon(
                    Icons.Default.FlipCameraAndroid,
                    contentDescription = "Switch camera",
                    tint = Color.White,
                )
            }
        }

        // ── Center Alignment Guide ──
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 288.dp, height = 192.dp),
        ) {
            // Corner brackets drawn on Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 4.dp.toPx()
                val cornerLength = 32.dp.toPx()
                val orange = OilWatcherColors.PrimaryContainer

                // Top-left corner
                drawLine(orange, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth, StrokeCap.Round)
                drawLine(orange, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth, StrokeCap.Round)

                // Top-right corner
                drawLine(orange, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), strokeWidth, StrokeCap.Round)
                drawLine(orange, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth, StrokeCap.Round)

                // Bottom-left corner
                drawLine(orange, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth, StrokeCap.Round)
                drawLine(orange, Offset(0f, size.height), Offset(0f, size.height - cornerLength), strokeWidth, StrokeCap.Round)

                // Bottom-right corner
                drawLine(orange, Offset(size.width, size.height), Offset(size.width - cornerLength, size.height), strokeWidth, StrokeCap.Round)
                drawLine(orange, Offset(size.width, size.height), Offset(size.width, size.height - cornerLength), strokeWidth, StrokeCap.Round)

                // Horizontal level line
                drawLine(
                    Color.White.copy(alpha = 0.3f),
                    Offset(-20.dp.toPx(), size.height / 2),
                    Offset(size.width + 20.dp.toPx(), size.height / 2),
                    1.dp.toPx(),
                )
            }

            // Animated scan line
            val scanY = ((scanOffset + 1f) / 2f) // Normalize to 0..1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .offset(y = (192.dp * scanY))
                    .size(width = 288.dp, height = 2.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                OilWatcherColors.PrimaryContainer,
                                Color.Transparent,
                            )
                        )
                    ),
            )
        }

        // ── Helper Text ──
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 130.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Align prices in the frame",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        // ── Bottom Controls ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 40.dp, end = 40.dp, bottom = 64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Gallery preview placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
            )

            // Shutter button
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Outer ring
                Canvas(modifier = Modifier.size(96.dp)) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 4.dp.toPx()),
                    )
                }
                // Inner button wrapper (with loader)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(enabled = !uiState.isCapturing) {
                            if (cameraPermissionState.status.isGranted) {
                                viewModel.takePhoto(imageCapture, context) { uri ->
                                    onPhotoCapture(uri.toString())
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.isCapturing) {
                        CircularProgressIndicator(
                            color = OilWatcherColors.PrimaryContainer,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            // Flash toggle
            IconButton(
                onClick = {
                    flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) {
                        ImageCapture.FLASH_MODE_ON
                    } else {
                        ImageCapture.FLASH_MODE_OFF
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
            ) {
                Icon(
                    if (flashMode == ImageCapture.FLASH_MODE_ON) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Toggle flash",
                    tint = Color.White,
                )
            }
        }
    }
}
