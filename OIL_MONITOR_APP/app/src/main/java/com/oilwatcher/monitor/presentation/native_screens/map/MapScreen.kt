package com.oilwatcher.monitor.presentation.native_screens.map

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.oilwatcher.monitor.domain.model.FuelType
import com.oilwatcher.monitor.presentation.theme.OilWatcherColors
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Map Screen — Native Compose (osmdroid variant).
 *
 * Implements OpenStreetMap to avoid Google API Key & credit card requirements.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onOpenCamera: () -> Unit,
    onOpenStation: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    // Default location (e.g., center of US)
    val defaultLocation = GeoPoint(39.8283, -98.5795)

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            viewModel.getUserLocation()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        
        // ── osmdroid MapView ──
        val lifecycleOwner = LocalLifecycleOwner.current
        var mapView by remember { mutableStateOf<MapView?>(null) }
        // Keep a stable reference to the location overlay to prevent leaks
        var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
        
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                locationOverlay?.disableMyLocation()
                mapView?.onDetach()
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(4.0)
                    controller.setCenter(defaultLocation)
                    minZoomLevel = 3.0
                    maxZoomLevel = 20.0
                    
                    // Create the location overlay ONCE in the factory
                    if (locationPermissionState.status.isGranted) {
                        val locOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        locOverlay.enableMyLocation()
                        overlays.add(locOverlay)
                        locationOverlay = locOverlay
                    }
                    
                    mapView = this
                }
            },
            update = { view ->
                // Apply User Location to Map Camera if changed
                uiState.userLocation?.let { geoPoint ->
                    view.controller.animateTo(geoPoint, 14.0, 1000L)
                }

                // Apply Station Markers
                // Remove existing markers first to avoid duplication
                view.overlays.removeAll { it is Marker }
                
                uiState.stations.forEach { station ->
                    val marker = Marker(view)
                    marker.position = GeoPoint(station.latitude, station.longitude)
                    marker.title = station.name
                    marker.snippet = station.address
                    
                    // Use the actual Station model fields
                    val displayPrice = station.latestPrices.regular?.toString()
                        ?: station.latestPrices.midgrade?.toString()
                        ?: station.latestPrices.premium?.toString()
                        ?: station.latestPrices.diesel?.toString()
                        ?: "N/A"
                    
                    val fuelType = when {
                        station.latestPrices.regular != null -> FuelType.REGULAR
                        station.latestPrices.midgrade != null -> FuelType.MIDGRADE
                        station.latestPrices.premium != null -> FuelType.PREMIUM
                        station.latestPrices.diesel != null -> FuelType.DIESEL
                        else -> FuelType.REGULAR
                    }
                    
                    marker.icon = MarkerUtil.createPriceMarkerBitmap(
                        context = context,
                        price = displayPrice,
                        fuelType = fuelType
                    )
                    
                    // Center the anchor since the tail is at the bottom
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    
                    marker.setOnMarkerClickListener { _, _ ->
                        onOpenStation(station.id)
                        true
                    }
                    view.overlays.add(marker)
                }
                
                view.invalidate() // Force map redraw
            }
        )

        // ── Floating Search Bar ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 48.dp)
                .align(Alignment.TopCenter),
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it 
                    isDropdownExpanded = it.isNotEmpty()
                },
                placeholder = {
                    Text(
                        "Search area",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OilWatcherColors.TextMuted,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = OilWatcherColors.TextMuted,
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { 
                        viewModel.searchArea(searchQuery)
                        isDropdownExpanded = false
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.85f),
                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
            )

            // Simple autocomplete dropdown skeleton for Nominatim
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Search location: '$searchQuery'") },
                    onClick = {
                        isDropdownExpanded = false
                        viewModel.searchArea(searchQuery)
                    }
                )
            }
        }

        // ── My Location FAB ──
        FloatingActionButton(
            onClick = {
                if (locationPermissionState.status.isGranted) {
                    viewModel.getUserLocation()
                    uiState.userLocation?.let { geoPoint ->
                        mapView?.controller?.animateTo(geoPoint, 16.0, 1000L)
                    }
                } else {
                    locationPermissionState.launchPermissionRequest()
                }
            },
            containerColor = Color.White,
            contentColor = OilWatcherColors.PrimaryContainer,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 104.dp) // Above camera FAB
                .size(56.dp),
        ) {
            Icon(
                Icons.Filled.MyLocation,
                contentDescription = "My Location",
            )
        }

        // ── Camera FAB ──
        FloatingActionButton(
            onClick = onOpenCamera,
            containerColor = OilWatcherColors.PrimaryContainer,
            contentColor = OilWatcherColors.OnPrimary,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .size(64.dp),
        ) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = "Open camera to contribute a price",
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
