package com.elderassist.app.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.ClarificationOption
import com.elderassist.app.core.model.ContactCandidate
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.core.model.StitchIds
import com.elderassist.app.core.model.TaskStatus
import com.elderassist.app.feature.home.HomeUiState
import com.elderassist.app.feature.home.PendingClarification
import com.elderassist.app.feature.home.PendingContactChoice
import com.elderassist.app.presentation.theme.SunlitColors

@Composable
fun HomeScreen(
    state: HomeUiState,
    onQuickSetting: (SettingDestination) -> Unit,
    onGoHome: () -> Unit,
    onProposeCreateContact: (String, String) -> Unit,
    onRetryTranscript: () -> Unit,
    onStopCancel: () -> Unit,
    onSelectClarification: (ClarificationOption) -> Unit,
    onSelectContact: (ContactCandidate) -> Unit,
    onDismissContactChoice: () -> Unit,
    onOpenPermissions: () -> Unit
) {
    var showCreateContactDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(StitchIds.SCR_01),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (state.isListening) "Listening now" else "Ready to help",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = "Hello! How can I help you today?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )

            Text(
                text = "Tap a helpful shortcut below, or press the microphone to ask me anything.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 2x2 Quick Action Grid with matched row heights
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QuickShortcutTile(
                    title = "Turn on Hotspot",
                    icon = Icons.Default.WifiTethering,
                    badgeBg = MaterialTheme.colorScheme.primaryContainer,
                    badgeTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onQuickSetting(SettingDestination.HOTSPOT) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                QuickShortcutTile(
                    title = "Bluetooth Settings",
                    icon = Icons.Default.Bluetooth,
                    badgeBg = MaterialTheme.colorScheme.primaryContainer,
                    badgeTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { onQuickSetting(SettingDestination.BLUETOOTH) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QuickShortcutTile(
                    title = "Take me Home",
                    icon = Icons.Default.Home,
                    badgeBg = MaterialTheme.colorScheme.secondaryContainer,
                    badgeTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onGoHome,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                QuickShortcutTile(
                    title = "Add a Contact",
                    icon = Icons.Default.PersonAdd,
                    badgeBg = MaterialTheme.colorScheme.tertiaryContainer,
                    badgeTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = { showCreateContactDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        if (state.isListening || state.isBusy) {
            Button(
                onClick = onStopCancel,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Stop / Cancel", style = MaterialTheme.typography.labelLarge)
            }
        }

        // Live Status Pill / Card
        StatusCard(
            state = state,
            onOpenPermissions = onOpenPermissions
        )

        if (state.transcript.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "You asked:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "\"${state.transcript}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    OutlinedButton(
                        onClick = onRetryTranscript,
                        shape = CircleShape,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Retry", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        state.pendingClarification?.let { clarification ->
            HomeClarificationCard(
                clarification = clarification,
                onSelect = onSelectClarification
            )
        }

        state.pendingContactChoice?.let { choice ->
            HomeContactChoiceCard(
                choice = choice,
                onSelect = onSelectContact,
                onDismiss = onDismissContactChoice
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    if (showCreateContactDialog) {
        AddContactDialog(
            onDismiss = { showCreateContactDialog = false },
            onSubmit = { name, phone ->
                showCreateContactDialog = false
                onProposeCreateContact(name, phone)
            }
        )
    }
}

@Composable
private fun QuickShortcutTile(
    title: String,
    icon: ImageVector,
    badgeBg: Color,
    badgeTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatusCard(
    state: HomeUiState,
    onOpenPermissions: () -> Unit
) {
    val bgColor = when (state.status) {
        TaskStatus.COMPLETED -> SunlitColors.SuccessBg
        TaskStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val textColor = when (state.status) {
        TaskStatus.COMPLETED -> SunlitColors.SuccessGreen
        TaskStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val icon = when {
        state.isListening -> Icons.Default.GraphicEq
        state.status == TaskStatus.COMPLETED -> Icons.Default.CheckCircle
        state.status == TaskStatus.FAILED -> Icons.Default.ErrorOutline
        else -> Icons.Default.Info
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isBusy && !state.isListening) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (state.isListening) MaterialTheme.colorScheme.secondary else textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = state.statusHeadline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
            state.statusDetail?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.status == TaskStatus.FAILED &&
                (!state.isAccessibilityEnabled || !state.hasMicPermission)
            ) {
                OutlinedButton(
                    onClick = onOpenPermissions,
                    shape = CircleShape,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(text = "Check Permissions & Setup", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun HomeClarificationCard(
    clarification: PendingClarification,
    onSelect: (ClarificationOption) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = clarification.question,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            clarification.options.forEach { option ->
                Button(
                    onClick = { onSelect(option) },
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(text = option.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun HomeContactChoiceCard(
    choice: PendingContactChoice,
    onSelect: (ContactCandidate) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "We found ${choice.candidates.size} people matching \"${choice.query}\". Which one?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            choice.candidates.forEach { candidate ->
                val initials = candidate.displayName
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                    .ifEmpty { "?" }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(candidate) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
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
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = candidate.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            candidate.phoneHint?.let { hint ->
                                Text(
                                    text = "Phone $hint",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = onDismiss,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
