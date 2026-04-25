package com.oilwatcher.monitor.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oilwatcher.monitor.presentation.theme.OilWatcherColors

/**
 * Bottom Navigation Bar — always native Compose.
 *
 * Design specs from mockups:
 * - Glassmorphism: white/80% opacity + backdrop blur
 * - Active: filled icon + primary orange + dot indicator below
 * - Inactive: outlined icon + muted color
 * - Labels: 10px, bold, uppercase-looking tracking
 * - 4 items: Map | Contribute | Community | Settings
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    ambientColor = Color(0x0F1A1C1B),
                    spotColor = Color(0x0F1A1C1B),
                )
                .graphicsLayer {
                    // Glassmorphism — semi-transparent white
                    alpha = 1f
                }
                .background(
                    color = Color.White.copy(alpha = 0.92f)
                )
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavItem(
                    icon = if (currentRoute == Routes.MAP) Icons.Filled.Map else Icons.Outlined.Map,
                    label = "Map",
                    isSelected = currentRoute == Routes.MAP,
                    onClick = { onNavigate(Routes.MAP) },
                )
                NavItem(
                    icon = if (currentRoute == Routes.CAMERA) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
                    label = "Contribute",
                    isSelected = currentRoute == Routes.CAMERA,
                    onClick = { onNavigate(Routes.CAMERA) },
                )
                NavItem(
                    icon = if (currentRoute == Routes.HISTORY) Icons.Filled.Group else Icons.Outlined.Group,
                    label = "Community",
                    isSelected = currentRoute == Routes.HISTORY,
                    onClick = { onNavigate(Routes.HISTORY) },
                )
                NavItem(
                    icon = if (currentRoute == Routes.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    label = "Settings",
                    isSelected = currentRoute == Routes.SETTINGS,
                    onClick = { onNavigate(Routes.SETTINGS) },
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) OilWatcherColors.PrimaryContainer else OilWatcherColors.TextMuted,
        )
        Text(
            text = label,
            color = if (isSelected) OilWatcherColors.PrimaryContainer else OilWatcherColors.TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp),
        )
        // Active indicator dot
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(OilWatcherColors.PrimaryContainer)
            )
        }
    }
}
