package com.oilwatcher.monitor.presentation.native_screens.analyze

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.oilwatcher.monitor.domain.model.FuelType
import com.oilwatcher.monitor.presentation.theme.OilWatcherColors

/**
 * Analyze & Preview Screen — Native Compose.
 *
 * Post-capture verification:
 * - Shows captured image with scan overlay animation
 * - Displays OCR-extracted prices in editable fields
 * - "Detected" (green) badges for high-confidence results
 * - "Optional" (grey) for low/no confidence
 * - User can manually correct any price
 * - "Confirm & Upload" submits to Firebase
 */
@Composable
fun AnalyzeScreen(
    imageUri: String?,
    onConfirmUpload: () -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalyzeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            viewModel.analyzeImage(context, imageUri)
        }
    }

    // Navigate after successful upload
    LaunchedEffect(uiState.uploadSuccess) {
        if (uiState.uploadSuccess) {
            onConfirmUpload()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Image Preview Area (Top 40%) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .background(Color(0xFF23160F)),
                contentAlignment = Alignment.Center,
            ) {
                // Back button
                IconButton(
                    onClick = onGoBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Go back",
                        tint = Color.White,
                    )
                }

                // Image placeholder logic based on uri analysis
                if (uiState.imageUri != null) {
                    AsyncImage(
                        model = uiState.imageUri,
                        contentDescription = "Captured receipt",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "📷 Captured Image Preview",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                
                if (uiState.isAnalyzing) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OilWatcherColors.PrimaryContainer)
                    }
                }
            }

            // ── Price Form Area (Bottom 60%) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                Text(
                    text = "Verify Prices",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tap any price to correct scanning errors.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OilWatcherColors.TextMuted,
                )
                Spacer(Modifier.height(24.dp))

                // Price input rows
                PriceInputRow(
                    label = "Regular",
                    value = uiState.regularPrice,
                    onValueChange = { viewModel.updatePrice(FuelType.REGULAR, it) },
                    isDetected = uiState.isRegularDetected,
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))

                PriceInputRow(
                    label = "Midgrade",
                    value = uiState.midgradePrice,
                    onValueChange = { viewModel.updatePrice(FuelType.MIDGRADE, it) },
                    isDetected = uiState.isMidgradeDetected,
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))

                PriceInputRow(
                    label = "Premium",
                    value = uiState.premiumPrice,
                    onValueChange = { viewModel.updatePrice(FuelType.PREMIUM, it) },
                    isDetected = uiState.isPremiumDetected,
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))

                PriceInputRow(
                    label = "Diesel",
                    value = uiState.dieselPrice,
                    onValueChange = { viewModel.updatePrice(FuelType.DIESEL, it) },
                    isDetected = uiState.isDieselDetected,
                )

                Spacer(Modifier.height(80.dp)) // Space for button
            }
        }

        // ── Confirm & Upload Button ──
        Button(
            onClick = {
                viewModel.confirmAndUpload()
            },
            enabled = !uiState.isUploading && !uiState.isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 0.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OilWatcherColors.PrimaryContainer,
                contentColor = Color.White,
            ),
        ) {
            if (uiState.isUploading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Confirm & Upload",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                Icon(Icons.Default.CloudUpload, contentDescription = null)
            }
        }
    }
}

/**
 * A single price input row matching the mockup design.
 * Left: fuel grade label + detected/optional badge
 * Right: dollar sign + large number input
 */
@Composable
private fun PriceInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isDetected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left: Label + status badge
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OilWatcherColors.OnSurface,
            )
            Spacer(Modifier.height(4.dp))
            if (isDetected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Detected",
                        tint = OilWatcherColors.Accent,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "Detected",
                        color = OilWatcherColors.Accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            } else {
                Text(
                    text = "Optional",
                    color = OilWatcherColors.TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // Right: Dollar sign + price input
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OilWatcherColors.TextMuted,
            )
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        "0.00",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD0D0D0),
                    )
                },
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    textAlign = TextAlign.End,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
