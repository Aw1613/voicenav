package com.elderassist.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.elderassist.app.core.model.ActionId
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.core.model.StitchIds

private data class SettingShortcutRow(
    val actionId: ActionId,
    val destination: SettingDestination,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val useWarmBadge: Boolean
)

@Composable
fun SettingsNavigationScreen(
    onOpenSetting: (SettingDestination) -> Unit,
    onGlobalNav: (AssistantAction) -> Unit
) {
    val rows = listOf(
        SettingShortcutRow(ActionId.OPEN_WIFI, SettingDestination.WIFI, "Wi-Fi", "Connect to a network", Icons.Default.Wifi, false),
        SettingShortcutRow(ActionId.OPEN_BLUETOOTH, SettingDestination.BLUETOOTH, "Bluetooth", "Connect devices", Icons.Default.Bluetooth, false),
        SettingShortcutRow(ActionId.OPEN_HOTSPOT, SettingDestination.HOTSPOT, "Mobile hotspot", "Share your internet", Icons.Default.WifiTethering, true),
        SettingShortcutRow(ActionId.OPEN_DISPLAY, SettingDestination.DISPLAY, "Display", "Brightness and text size", Icons.Default.Brightness6, true),
        SettingShortcutRow(ActionId.OPEN_SOUND, SettingDestination.SOUND, "Sound", "Volume and audio", Icons.AutoMirrored.Filled.VolumeUp, false),
        SettingShortcutRow(ActionId.OPEN_ACCESSIBILITY, SettingDestination.ACCESSIBILITY, "Accessibility", "Display and controls", Icons.Default.AccessibilityNew, true),
        SettingShortcutRow(ActionId.OPEN_BATTERY, SettingDestination.BATTERY, "Battery", "Power and battery use", Icons.Default.BatteryChargingFull, true)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(StitchIds.SCR_03),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Settings & Navigation",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = "Choose a setting, or ask ElderAssist to find it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        rows.forEach { item ->
            val badgeBg = if (item.useWarmBadge) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
            val badgeTint = if (item.useWarmBadge) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onOpenSetting(item.destination) }
                    .testTag(item.actionId.code)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = badgeTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // System Navigation (ACT-13, ACT-14, ACT-15)
        Text(
            text = "Phone Navigation",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { onGlobalNav(AssistantAction.GoHome) },
                shape = CircleShape,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag(ActionId.GO_HOME.code)
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = null)
                Text(" Home", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = { onGlobalNav(AssistantAction.GoBack) },
                shape = CircleShape,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag(ActionId.GO_BACK.code)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Text(" Back", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = { onGlobalNav(AssistantAction.OpenRecents) },
                shape = CircleShape,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag(ActionId.OPEN_RECENTS.code)
            ) {
                Icon(imageVector = Icons.Default.ViewCarousel, contentDescription = null)
                Text(" Recent", style = MaterialTheme.typography.labelMedium)
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Tip: Tap Ask ElderAssist below and say \"Make my text bigger\" or \"Open Wi-Fi\".",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}
