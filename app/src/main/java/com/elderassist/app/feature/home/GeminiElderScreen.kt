package com.elderassist.app.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
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

/**
 * Gemini-styled Elder Navigation UI.
 *
 * Designed with:
 * 1. Gemini's clean floating sheet / overlay aesthetics (sparkle icon, clean suggestion pills, floating card).
 * 2. Pop-up assistant overlay simulation ("Hello Google" pop-up over the phone screen).
 * 3. Single-tone color harmony (soft calm Google Indigo, warm glare-free off-white, no jarring dual-color contrasts).
 * 4. Maximum simplicity for elderly users with zero tech familiarity:
 *    - Big 64dp+ tap targets
 *    - Conversational plain-language phrasing
 *    - Clear audio wave visualizer
 *    - Immediate 1-tap suggestions
 */
@Composable
fun GeminiElderRootScreen(
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
    // Mode toggle: allows previewing full-screen vs. the pop-up assistant sheet over any app
    var showAsPopupOverlay by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9F9F7) // Soft, warm off-white (glare-free)
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
            Box(modifier = Modifier.fillMaxSize()) {
                if (showAsPopupOverlay) {
                    // Simulated background screen (representing whatever app or home screen user was on)
                    SimulatedPhoneBackgroundScreen(
                        onTriggerAssistant = { /* Already open */ }
                    )

                    // Dimmed backdrop
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x73000000))
                            .clickable { showAsPopupOverlay = false }
                    )

                    // Gemini-style Pop-up Bottom Sheet
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        GeminiAssistantSheet(
                            state = state,
                            isOverlayMode = true,
                            onCloseOverlay = { showAsPopupOverlay = false },
                            onMicTap = onMicTap,
                            onStopCancel = onStopCancel,
                            onManualInputChange = onManualInputChange,
                            onSubmitManual = onSubmitManual,
                            onRetryTranscript = onRetryTranscript,
                            onSelectClarification = onSelectClarification,
                            onSelectContact = onSelectContact,
                            onDismissContactSelection = onDismissContactSelection,
                            onQuickSettingTap = onQuickSettingTap,
                            onQuickContactsTap = onQuickContactsTap,
                            onOpenSetupHelp = { onOpenSetupHelp(true) }
                        )
                    }
                } else {
                    // Regular full-screen Gemini Elder experience
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top Mode Pill: Switch to simulated "Hello Google" pop-up mode
                        ModeSelectorBanner(
                            onActivatePopup = { showAsPopupOverlay = true }
                        )

                        GeminiAssistantSheet(
                            state = state,
                            isOverlayMode = false,
                            onCloseOverlay = {},
                            onMicTap = onMicTap,
                            onStopCancel = onStopCancel,
                            onManualInputChange = onManualInputChange,
                            onSubmitManual = onSubmitManual,
                            onRetryTranscript = onRetryTranscript,
                            onSelectClarification = onSelectClarification,
                            onSelectContact = onSelectContact,
                            onDismissContactSelection = onDismissContactSelection,
                            onQuickSettingTap = onQuickSettingTap,
                            onQuickContactsTap = onQuickContactsTap,
                            onOpenSetupHelp = { onOpenSetupHelp(true) }
                        )
                    }
                }
            }
        }

        // Confirmation Dialog (when needed)
        state.pendingConfirmation?.let { pending ->
            ConfirmationDialog(
                pending = pending,
                onConfirm = onConfirmPending,
                onCancel = onCancelPending
            )
        }
    }
}

/**
 * The core Gemini Assistant Sheet: clean, rounded, elder-simplified card with single-tone calm blue.
 */
@Composable
private fun GeminiAssistantSheet(
    state: HomeUiState,
    isOverlayMode: Boolean,
    onCloseOverlay: () -> Unit,
    onMicTap: () -> Unit,
    onStopCancel: () -> Unit,
    onManualInputChange: (String) -> Unit,
    onSubmitManual: () -> Unit,
    onRetryTranscript: () -> Unit,
    onSelectClarification: (ClarificationOption) -> Unit,
    onSelectContact: (ContactCandidate) -> Unit,
    onDismissContactSelection: () -> Unit,
    onQuickSettingTap: (SettingDestination) -> Unit,
    onQuickContactsTap: () -> Unit,
    onOpenSetupHelp: () -> Unit
) {
    Card(
        shape = if (isOverlayMode) RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp) else RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFFE4E1DA)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOverlayMode) 8.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Drag handle pill for Gemini bottom sheet
            if (isOverlayMode) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD3D0C8))
                )
            }

            // Header: Gemini Star Icon + Friendly Simple Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GeminiSparkleIcon(modifier = Modifier.size(34.dp))
                    Column {
                        Text(
                            text = "Assistant",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E2022)
                        )
                        Text(
                            text = "Here to help you",
                            fontSize = 15.sp,
                            color = Color(0xFF5F6368)
                        )
                    }
                }

                if (isOverlayMode) {
                    OutlinedButton(
                        onClick = onCloseOverlay,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFE0DDD5)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF5F6368),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onOpenSetupHelp,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0DDD5))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = null,
                            tint = Color(0xFF1A73E8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Help",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A73E8)
                        )
                    }
                }
            }

            // Dynamic Listening / Speaking Area
            if (state.isListening) {
                GeminiListeningWaveCard(
                    onStopTap = onMicTap
                )
            } else {
                // Large, Senior-Friendly Speaking Button (Single-tone calm blue, high legibility)
                Button(
                    onClick = onMicTap,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A73E8),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .semantics {
                            contentDescription = "Tap to speak your command"
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Tap to Speak",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Always-visible Stop/Cancel Button when busy or listening
            if (state.isBusy) {
                Button(
                    onClick = onStopCancel,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5F6368),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stop Action",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Status Headline & Feedback Card (Calm single-tone theme, avoids jarring contrast)
            GeminiFeedbackCard(state = state)

            // Transcript bubble (What user just said)
            if (state.transcript.isNotBlank()) {
                GeminiTranscriptCard(
                    transcript = state.transcript,
                    onRetry = onRetryTranscript
                )
            }

            // Clarification question (if command was ambiguous)
            state.pendingClarification?.let { clarification ->
                GeminiClarificationCard(
                    clarification = clarification,
                    onSelect = onSelectClarification
                )
            }

            // Contact Disambiguation Choice
            state.pendingContactChoice?.let { choice ->
                GeminiContactChoiceCard(
                    choice = choice,
                    onSelect = onSelectContact,
                    onCancel = onDismissContactSelection
                )
            }

            // Simple 1-Tap Quick Action Pills (Gemini style)
            Text(
                text = "Or tap a quick command:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E2022),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GeminiSuggestionChip(
                        icon = Icons.Default.Wifi,
                        label = "Wi-Fi Settings",
                        onClick = { onQuickSettingTap(SettingDestination.WIFI) },
                        modifier = Modifier.weight(1f)
                    )
                    GeminiSuggestionChip(
                        icon = Icons.Default.VolumeUp,
                        label = "Sound & Volume",
                        onClick = { onQuickSettingTap(SettingDestination.SOUND) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GeminiSuggestionChip(
                        icon = Icons.Default.Call,
                        label = "Open Contacts",
                        onClick = onQuickContactsTap,
                        modifier = Modifier.weight(1f)
                    )
                    GeminiSuggestionChip(
                        icon = Icons.Default.Settings,
                        label = "All Settings",
                        onClick = { onQuickSettingTap(SettingDestination.SETTINGS_ROOT) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Optional Manual Input Fallback (Minimal & unobtrusive)
            GeminiManualInputRow(
                value = state.manualInput,
                onValueChange = onManualInputChange,
                onSubmit = onSubmitManual
            )
        }
    }
}

/**
 * Animated Gemini 4-Point Star Sparkle Icon.
 */
@Composable
fun GeminiSparkleIcon(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparklePulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleScale"
    )

    Canvas(modifier = modifier.scale(scale)) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f

        val path = Path().apply {
            moveTo(cx, 0f)
            cubicTo(cx * 1.05f, cy * 0.75f, cx * 1.25f, cy * 0.95f, width, cy)
            cubicTo(cx * 1.25f, cy * 1.05f, cx * 1.05f, cy * 1.25f, cx, height)
            cubicTo(cx * 0.95f, cy * 1.25f, cx * 0.75f, cy * 1.05f, 0f, cy)
            cubicTo(cx * 0.75f, cy * 0.95f, cx * 0.95f, cy * 0.75f, cx, 0f)
            close()
        }

        drawPath(path = path, color = Color(0xFF1A73E8))
    }
}

/**
 * Gemini Live Soundwave Indicator when listening to elderly user.
 */
@Composable
private fun GeminiListeningWaveCard(
    onStopTap: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F9)),
        border = BorderStroke(1.5.dp, Color(0xFFD2E3FC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Listening to you...",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A73E8)
            )

            // Animated pulsing soundwave bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(44.dp)
            ) {
                val transition = rememberInfiniteTransition(label = "waves")
                val heights = listOf(14.dp, 28.dp, 40.dp, 32.dp, 20.dp, 36.dp, 22.dp)

                heights.forEachIndexed { i, targetH ->
                    val hFraction by transition.animateFloat(
                        initialValue = 0.35f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500 + (i * 120), easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bar_$i"
                    )

                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(targetH * hFraction)
                            .clip(CircleShape)
                            .background(Color(0xFF1A73E8))
                    )
                }
            }

            Button(
                onClick = onStopTap,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Tap when done speaking",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Status and feedback card: clear, soothing, high readability.
 */
@Composable
private fun GeminiFeedbackCard(state: HomeUiState) {
    val isSuccess = state.status == TaskStatus.COMPLETED
    val isPending = state.status == TaskStatus.AWAITING_CLARIFICATION || state.status == TaskStatus.AWAITING_CONFIRMATION

    val bgColor = if (isSuccess) Color(0xFFF1F8F4) else Color(0xFFF8F9FA)
    val borderColor = if (isSuccess) Color(0xFFCEEAD6) else Color(0xFFE8EAED)
    val titleColor = if (isSuccess) Color(0xFF137333) else Color(0xFF1E2022)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Color(0xFF1A73E8)
                    )
                } else if (isSuccess) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF137333),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = state.statusHeadline,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
            }

            state.statusDetail?.let { detail ->
                Text(
                    text = detail,
                    fontSize = 16.sp,
                    color = Color(0xFF5F6368),
                    lineHeight = 22.sp
                )
            }
        }
    }
}

/**
 * Large speech bubble showing what was heard.
 */
@Composable
private fun GeminiTranscriptCard(
    transcript: String,
    onRetry: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F9)),
        border = BorderStroke(1.dp, Color(0xFFD2E3FC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "You said:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5F6368)
            )
            Text(
                text = "\"$transcript\"",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2022)
            )
        }
    }
}

/**
 * Clarification card when command is ambiguous.
 */
@Composable
private fun GeminiClarificationCard(
    clarification: PendingClarification,
    onSelect: (ClarificationOption) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFF1A73E8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = clarification.question,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2022)
            )

            clarification.options.forEach { option ->
                Button(
                    onClick = { onSelect(option) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0F4F9),
                        contentColor = Color(0xFF1A73E8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
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

/**
 * Contact picker card when multiple contact matches are found.
 */
@Composable
private fun GeminiContactChoiceCard(
    choice: PendingContactChoice,
    onSelect: (ContactCandidate) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFF1A73E8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Which contact did you mean?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2022)
            )

            choice.candidates.forEach { contact ->
                Button(
                    onClick = { onSelect(contact) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0F4F9),
                        contentColor = Color(0xFF1E2022)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = contact.displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A73E8)
                        )
                        contact.phoneHint?.let { num ->
                            Text(
                                text = num,
                                fontSize = 14.sp,
                                color = Color(0xFF5F6368)
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "None of these",
                    fontSize = 16.sp,
                    color = Color(0xFF5F6368)
                )
            }
        }
    }
}

/**
 * 1-Tap Suggestion Chip (Gemini style).
 */
@Composable
private fun GeminiSuggestionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Color(0xFFE0DDD5)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFFFAF9F6)
        ),
        modifier = modifier.height(58.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF1A73E8),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E2022)
            )
        }
    }
}

/**
 * Manual input box (for testing or when voice is not desired).
 */
@Composable
private fun GeminiManualInputRow(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text("Or type here (e.g. Turn on Wi-Fi)", fontSize = 15.sp, color = Color(0xFF70757A))
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSubmit() }),
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onSubmit,
            enabled = value.isNotBlank(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A73E8),
                contentColor = Color.White
            ),
            modifier = Modifier.size(48.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Banner at top letting user preview the "Hello Google" pop-up overlay behavior.
 */
@Composable
private fun ModeSelectorBanner(
    onActivatePopup: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF3FC)),
        border = BorderStroke(1.dp, Color(0xFFD2E3FC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "\"Hello Google\" Pop-up Mode",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A73E8)
                )
                Text(
                    text = "Pops up over any active screen",
                    fontSize = 13.sp,
                    color = Color(0xFF5F6368)
                )
            }
            Button(
                onClick = onActivatePopup,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A73E8),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Try Pop-up", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Simulated home screen of a phone to demonstrate how the assistant overlays
 * on top of whichever screen the elderly person is currently viewing.
 */
@Composable
private fun SimulatedPhoneBackgroundScreen(
    onTriggerAssistant: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE9EDF0))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = "10:30 AM",
                fontSize = 54.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF3C4043)
            )
            Text(
                text = "Thursday, November 27",
                fontSize = 18.sp,
                color = Color(0xFF5F6368)
            )
        }

        Text(
            text = "(Saying \"Hello Google\" or tapping assistant triggers overlay over any app)",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF70757A),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}
