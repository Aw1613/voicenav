package com.elderassist.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elderassist.app.core.model.ClarificationOption
import com.elderassist.app.core.model.ContactCandidate
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.core.model.TaskStatus
import com.elderassist.app.feature.confirmation.ConfirmationDialog

@Composable
fun ElderAssistRootScreen(
    state: HomeUiState,
    onMicTap: () -> Unit,
    onStopCancel: () -> Unit,
    onManualInputChange: (String) -> Unit,
    onSubmitManual: () -> Unit,
    onRetryTranscript: () -> Unit,
    onSelectClarification: (ClarificationOption) -> Unit,
    onSelectContact: (ContactCandidate) -> Unit,
    onDismissContactSelection: () -> Unit,
    onConfirmPending: () -> Unit,
    onCancelPending: () -> Unit,
    onOpenSettingsShortcuts: (Boolean) -> Unit,
    onQuickSettingTap: (SettingDestination) -> Unit,
    onQuickContactsTap: () -> Unit,
    onOpenSetupHelp: (Boolean) -> Unit,
    onRequestMicPermission: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onToggleTts: (Boolean) -> Unit,
    onToggleExperimentalAiNav: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAF9F6)
    ) {
        if (state.showSetupHelp) {
            SetupHelpScreen(
                state = state,
                onBack = { onOpenSetupHelp(false) },
                onRequestMicPermission = onRequestMicPermission,
                onRequestContactsPermission = onRequestContactsPermission,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                onToggleTts = onToggleTts,
                onToggleExperimentalAiNav = onToggleExperimentalAiNav
            )
        } else {
            HomeMainScreen(
                state = state,
                onMicTap = onMicTap,
                onStopCancel = onStopCancel,
                onManualInputChange = onManualInputChange,
                onSubmitManual = onSubmitManual,
                onRetryTranscript = onRetryTranscript,
                onSelectClarification = onSelectClarification,
                onSelectContact = onSelectContact,
                onDismissContactSelection = onDismissContactSelection,
                onOpenSettingsShortcuts = onOpenSettingsShortcuts,
                onQuickSettingTap = onQuickSettingTap,
                onQuickContactsTap = onQuickContactsTap,
                onOpenSetupHelp = { onOpenSetupHelp(true) }
            )
        }

        state.pendingConfirmation?.let { pending ->
            ConfirmationDialog(
                pending = pending,
                onConfirm = onConfirmPending,
                onCancel = onCancelPending
            )
        }
    }
}

@Composable
private fun HomeMainScreen(
    state: HomeUiState,
    onMicTap: () -> Unit,
    onStopCancel: () -> Unit,
    onManualInputChange: (String) -> Unit,
    onSubmitManual: () -> Unit,
    onRetryTranscript: () -> Unit,
    onSelectClarification: (ClarificationOption) -> Unit,
    onSelectContact: (ContactCandidate) -> Unit,
    onDismissContactSelection: () -> Unit,
    onOpenSettingsShortcuts: (Boolean) -> Unit,
    onQuickSettingTap: (SettingDestination) -> Unit,
    onQuickContactsTap: () -> Unit,
    onOpenSetupHelp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ELDERASSIST",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = Color(0xFF124E96),
            modifier = Modifier.semantics { heading() }
        )

        if (state.preferences.experimentalAiNavigationEnabled) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFF0D4), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Experimental AI Screen Navigation: ON",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7A4400)
                )
            }
        }

        Text(
            text = "What would you like to do today?",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = Color(0xFF1A1C1E)
        )

        // Prominent Microphone Button (minimum 148dp touch target)
        val micContainerColor = if (state.isListening) Color(0xFFB3261E) else Color(0xFF124E96)
        val micLabel = if (state.isListening) "Listening... Tap to finish" else "Tap and speak"

        Button(
            onClick = onMicTap,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = micContainerColor,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .size(148.dp)
                .semantics {
                    contentDescription = if (state.isListening) {
                        "Stop listening"
                    } else {
                        "Tap and speak a command"
                    }
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (state.isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (state.isListening) "STOP" else "SPEAK",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = micLabel,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1C1E)
        )

        if (state.isListening || state.isBusy) {
            Button(
                onClick = onStopCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB3261E),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "Stop / Cancel",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        StatusFeedbackCard(state = state)

        if (state.transcript.isNotBlank()) {
            TranscriptCard(
                transcript = state.transcript,
                onRetry = onRetryTranscript
            )
        }

        state.pendingClarification?.let { clarification ->
            ClarificationCard(
                clarification = clarification,
                onSelect = onSelectClarification
            )
        }

        state.pendingContactChoice?.let { choice ->
            ContactSelectionCard(
                choice = choice,
                onSelect = onSelectContact,
                onCancel = onDismissContactSelection
            )
        }

        // Manual text command input (for debugging and non-voice fallback)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, Color(0xFFD0D5DD))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Or type a command",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.manualInput,
                        onValueChange = onManualInputChange,
                        placeholder = {
                            Text("e.g. Open Bluetooth settings", fontSize = 16.sp)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSubmitManual() }),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onSubmitManual,
                        enabled = state.manualInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF124E96),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send command"
                        )
                    }
                }
            }
        }

        // Primary quick action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onOpenSettingsShortcuts(!state.showSettingsShortcuts) },
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                border = BorderStroke(2.dp, Color(0xFF124E96))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF124E96)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF124E96)
                )
            }

            OutlinedButton(
                onClick = onQuickContactsTap,
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                border = BorderStroke(2.dp, Color(0xFF124E96))
            ) {
                Icon(
                    imageVector = Icons.Default.Contacts,
                    contentDescription = null,
                    tint = Color(0xFF124E96)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "Contacts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF124E96)
                )
            }
        }

        if (state.showSettingsShortcuts) {
            SettingsShortcutsCard(onTap = onQuickSettingTap)
        }

        OutlinedButton(
            onClick = onOpenSetupHelp,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            border = BorderStroke(2.dp, Color(0xFF43474E))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = null,
                tint = Color(0xFF1A1C1E)
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = "Setup & Help",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
        }
    }
}

@Composable
private fun StatusFeedbackCard(state: HomeUiState) {
    val (bgColor, borderColor, icon, iconTint) = when (state.status) {
        TaskStatus.COMPLETED -> Quad(
            Color(0xFFECF8F2),
            Color(0xFF136F43),
            Icons.Default.CheckCircle,
            Color(0xFF136F43)
        )
        TaskStatus.FAILED -> Quad(
            Color(0xFFFDF2F2),
            Color(0xFFB3261E),
            Icons.Default.ErrorOutline,
            Color(0xFFB3261E)
        )
        TaskStatus.AWAITING_CLARIFICATION,
        TaskStatus.AWAITING_CONFIRMATION -> Quad(
            Color(0xFFFFF8EC),
            Color(0xFF9A5800),
            Icons.Default.Info,
            Color(0xFF9A5800)
        )
        else -> Quad(
            Color(0xFFF1F5FA),
            Color(0xFF124E96),
            Icons.Default.Info,
            Color(0xFF124E96)
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = iconTint,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = state.statusHeadline,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp,
                    color = Color(0xFF1A1C1E)
                )
            }
            state.statusDetail?.let { detail ->
                Text(
                    text = detail,
                    fontSize = 16.sp,
                    color = Color(0xFF43474E)
                )
            }
        }
    }
}

@Composable
private fun TranscriptCard(
    transcript: String,
    onRetry: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFD0D5DD)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "You said:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF43474E)
                )
                Text(
                    text = "\"$transcript\"",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1C1E)
                )
            }
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.padding(2.dp))
                Text(text = "Retry", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ClarificationCard(
    clarification: PendingClarification,
    onSelect: (ClarificationOption) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF2)),
        border = BorderStroke(2.dp, Color(0xFF9A5800)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = clarification.question,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            clarification.options.forEach { option ->
                Button(
                    onClick = { onSelect(option) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF124E96),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = option.label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactSelectionCard(
    choice: PendingContactChoice,
    onSelect: (ContactCandidate) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFF124E96)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Which ${choice.query} do you mean?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            choice.candidates.forEach { candidate ->
                val label = buildString {
                    append(candidate.displayName)
                    candidate.phoneHint?.let { append(" ($it)") }
                }
                Button(
                    onClick = { onSelect(candidate) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF124E96),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Cancel", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsShortcutsCard(onTap: (SettingDestination) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFF124E96)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Quick Settings Destinations",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            SettingDestination.entries.forEach { dest ->
                OutlinedButton(
                    onClick = { onTap(dest) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = dest.displayName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF124E96)
                    )
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
