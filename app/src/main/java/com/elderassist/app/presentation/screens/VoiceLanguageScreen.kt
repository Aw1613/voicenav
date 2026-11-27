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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.elderassist.app.core.model.ActionId
import com.elderassist.app.core.model.StitchIds
import com.elderassist.app.feature.home.HomeUiState
import com.elderassist.app.settings.FeedbackLevel
import com.elderassist.app.settings.SpeechSpeed

@Composable
fun VoiceLanguageScreen(
    state: HomeUiState,
    onTestMicrophone: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onToggleTts: (Boolean) -> Unit,
    onSelectSpeed: (SpeechSpeed) -> Unit,
    onSelectFeedbackLevel: (FeedbackLevel) -> Unit
) {
    val isHindi = state.preferences.speechLanguageTag.startsWith("hi")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(StitchIds.SCR_05),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Voice & Language",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = "Adjust how ElderAssist listens and speaks with you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Speech provider status card (fixed title: no "Android speech r..." clipping)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.speechProviderName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (state.isSpeechRecognitionAvailable) {
                            "Ready for voice commands"
                        } else {
                            "Speech service unavailable — type fallback active"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (state.isSpeechRecognitionAvailable) "Active" else "Fallback",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Microphone Check (ACT-12)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Microphone Check",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Make sure your voice comes through clearly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onTestMicrophone,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag(ActionId.TEST_MIC.code)
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isListening) "Listening... Tap to stop" else "Test microphone",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.micTestResult ?: "Tap to test your microphone...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Language selector
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Text(
                        text = "Language",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChoicePillButton(
                        label = "English",
                        selected = !isHindi,
                        selectedBg = MaterialTheme.colorScheme.secondary,
                        selectedFg = MaterialTheme.colorScheme.onSecondary,
                        onClick = { onSelectLanguage("en-IN") },
                        modifier = Modifier.weight(1f)
                    )
                    ChoicePillButton(
                        label = "Hindi",
                        selected = isHindi,
                        selectedBg = MaterialTheme.colorScheme.secondary,
                        selectedFg = MaterialTheme.colorScheme.onSecondary,
                        onClick = { onSelectLanguage("hi-IN") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Voice recognition and spoken voices depend on your device's installed language support.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Spoken responses, speaking speed, feedback level (fixed pill widths: no "Important actio..." clip)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Spoken responses",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (state.preferences.ttsEnabled) {
                                "On — ElderAssist reads replies aloud"
                            } else {
                                "Off — visual messages only"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.preferences.ttsEnabled,
                        onCheckedChange = onToggleTts
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Speaking speed",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ChoicePillButton(
                            label = "Slow",
                            selected = state.preferences.speechSpeed == SpeechSpeed.SLOW,
                            selectedBg = MaterialTheme.colorScheme.primary,
                            selectedFg = MaterialTheme.colorScheme.onPrimary,
                            leadingIcon = Icons.Default.Speed,
                            onClick = { onSelectSpeed(SpeechSpeed.SLOW) },
                            modifier = Modifier.weight(1f)
                        )
                        ChoicePillButton(
                            label = "Normal",
                            selected = state.preferences.speechSpeed == SpeechSpeed.NORMAL,
                            selectedBg = MaterialTheme.colorScheme.primary,
                            selectedFg = MaterialTheme.colorScheme.onPrimary,
                            leadingIcon = Icons.Default.Speed,
                            onClick = { onSelectSpeed(SpeechSpeed.NORMAL) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Feedback level",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ChoicePillButton(
                            label = "Every action",
                            selected = state.preferences.feedbackLevel == FeedbackLevel.EVERY_ACTION,
                            selectedBg = MaterialTheme.colorScheme.primary,
                            selectedFg = MaterialTheme.colorScheme.onPrimary,
                            onClick = { onSelectFeedbackLevel(FeedbackLevel.EVERY_ACTION) },
                            modifier = Modifier.weight(1f)
                        )
                        ChoicePillButton(
                            label = "Important only",
                            selected = state.preferences.feedbackLevel == FeedbackLevel.IMPORTANT_ONLY,
                            selectedBg = MaterialTheme.colorScheme.primary,
                            selectedFg = MaterialTheme.colorScheme.onPrimary,
                            onClick = { onSelectFeedbackLevel(FeedbackLevel.IMPORTANT_ONLY) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun ChoicePillButton(
    label: String,
    selected: Boolean,
    selectedBg: androidx.compose.ui.graphics.Color,
    selectedFg: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) selectedBg else MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = if (selected) selectedFg else MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.height(50.dp)
    ) {
        if (selected && leadingIcon == null) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        } else if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}
