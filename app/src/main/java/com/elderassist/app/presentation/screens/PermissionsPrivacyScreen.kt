package com.elderassist.app.presentation.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.elderassist.app.core.model.StitchIds
import com.elderassist.app.feature.home.HomeUiState
import com.elderassist.app.presentation.components.PermissionStatusCard

@Composable
fun PermissionsPrivacyScreen(
    state: HomeUiState,
    onRequestMicPermission: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    val caps = state.capabilities

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(StitchIds.SCR_06),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Your Privacy First",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() }
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Protected",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                Text(
                    text = "ElderAssist uses your request and only the screen details needed to help. It does not keep unnecessary screen contents or contact phone numbers in history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "App Permissions",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${caps.readyCount} of 4 ready",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PermissionStatusCard(
            icon = Icons.Default.Mic,
            iconBg = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
            title = "Microphone",
            isReady = caps.microphoneGranted,
            whyWeAsk = "So you can speak naturally to ask for help instead of typing.",
            learnMoreText = "Audio is only captured after you tap Speak, and stops immediately if you tap Stop.",
            howToTurnOffText = "Open Phone Settings -> Apps -> ElderAssist -> Permissions -> Microphone -> Don't allow.",
            setupButtonLabel = "Allow Microphone",
            onSetupClick = onRequestMicPermission
        )

        PermissionStatusCard(
            icon = Icons.Default.Contacts,
            iconBg = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
            title = "Contacts",
            isReady = caps.contactsReadGranted,
            whyWeAsk = "So you can find, add, or rename people in your Contacts safely with confirmation.",
            learnMoreText = "Contacts are matched locally on your phone. Phone numbers are masked in previews and never logged to history.",
            howToTurnOffText = "Open Phone Settings -> Apps -> ElderAssist -> Permissions -> Contacts -> Don't allow.",
            setupButtonLabel = "Allow Contacts",
            onSetupClick = onRequestContactsPermission
        )

        PermissionStatusCard(
            icon = Icons.Default.AccessibilityNew,
            iconBg = MaterialTheme.colorScheme.tertiaryContainer,
            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
            title = "Accessibility",
            isReady = caps.accessibilityEnabled,
            whyWeAsk = "Allows ElderAssist to perform Go Home, Go Back, Recent apps, and optional step-by-step guidance.",
            goodToKnow = "Good to know: Enabling Master AI does not automatically enable Accessibility. You turn it on manually in Android Accessibility settings.",
            learnMoreText = "ElderAssist ignores password, OTP, banking, and payment screens, and never inspects apps when idle.",
            howToTurnOffText = "Open Phone Settings -> Accessibility -> ElderAssist Navigation Helper -> Turn Off.",
            setupButtonLabel = "Set up Accessibility",
            onSetupClick = onOpenAccessibilitySettings
        )

        // Fixed UI misalignment: Full "Internet & AI" title without "Internet / AI co..." truncation
        PermissionStatusCard(
            icon = Icons.Default.Wifi,
            iconBg = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
            title = "Internet & AI",
            isReady = caps.networkAvailable && caps.geminiKeyConfigured,
            statusMissingText = if (!caps.geminiKeyConfigured) "Key missing" else "Offline",
            whyWeAsk = "Used only when a phrase isn't recognized locally, so Gemini can interpret your intent safely.",
            learnMoreText = "Standard commands like Bluetooth, Wi-Fi, and Display settings work offline without calling Gemini.",
            howToTurnOffText = "Pause Master AI anytime on the Master AI screen, or open App Info to restrict background data.",
            setupButtonLabel = "Open App Info",
            onSetupClick = onOpenAppSettings
        )

        Spacer(modifier = Modifier.height(96.dp))
    }
}
