package com.elderassist.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elderassist.app.ui.theme.ElderAssistColors

@Composable
fun SetupHelpScreen(
    state: HomeUiState,
    onBack: () -> Unit,
    onRequestMicPermission: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onToggleTts: (Boolean) -> Unit,
    onToggleExperimentalAiNav: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.height(54.dp),
            border = BorderStroke(2.dp, ElderAssistColors.Primary)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = ElderAssistColors.Primary
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = "Back to Home",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ElderAssistColors.Primary
            )
        }

        Text(
            text = "Setup & Permissions Checklist",
            style = MaterialTheme.typography.headlineLarge,
            color = ElderAssistColors.Primary,
            modifier = Modifier.semantics { heading() }
        )

        Text(
            text = "ElderAssist only asks for permissions when needed. You stay in full control.",
            style = MaterialTheme.typography.bodyLarge,
            color = ElderAssistColors.OnSurfaceVariant
        )

        ChecklistCard(
            title = "1. Microphone Access",
            description = "Needed so ElderAssist can listen to your voice commands.",
            isEnabled = state.hasMicPermission,
            enabledLabel = "Allowed",
            disabledLabel = "Not allowed yet",
            actionLabel = "Allow Microphone",
            onAction = onRequestMicPermission
        )

        ChecklistCard(
            title = "2. Accessibility Helper",
            description = "Needed for 'Go home', 'Go back', recent apps, and guided menu help. ElderAssist never reads passwords or banking screens.",
            isEnabled = state.isAccessibilityEnabled,
            enabledLabel = "Enabled",
            disabledLabel = "Needs manual enablement",
            actionLabel = "Open Accessibility Settings",
            onAction = onOpenAccessibilitySettings
        )

        ChecklistCard(
            title = "3. Contacts Access (Optional)",
            description = "Needed only if you ask ElderAssist to find or rename someone in your Contacts.",
            isEnabled = state.hasContactsPermission,
            enabledLabel = "Allowed",
            disabledLabel = "Not allowed yet",
            actionLabel = "Allow Contacts",
            onAction = onRequestContactsPermission
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, Color(0xFFD0D5DD))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Assistant Preferences",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Speak responses aloud",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1C1E)
                        )
                        Text(
                            text = "Read answers aloud using phone Text-to-Speech.",
                            fontSize = 15.sp,
                            color = Color(0xFF43474E)
                        )
                    }
                    Switch(
                        checked = state.preferences.ttsEnabled,
                        onCheckedChange = onToggleTts
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Experimental AI screen navigation",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1C1E)
                        )
                        Text(
                            text = "Experimental: when enabled and Accessibility is on, Gemini can propose up to 5 safe screen taps for unknown requests.",
                            fontSize = 15.sp,
                            color = Color(0xFF43474E)
                        )
                    }
                    Switch(
                        checked = state.preferences.experimentalAiNavigationEnabled,
                        onCheckedChange = onToggleExperimentalAiNav
                    )
                }

                Text(
                    text = "Voice provider: ${state.speechProviderName}" +
                        if (state.isGeminiConfigured) " | Gemini helper: Ready" else " | Gemini helper: Offline / Key missing",
                    fontSize = 14.sp,
                    color = Color(0xFF43474E)
                )
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F6FA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Examples you can say",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                val examples = listOf(
                    "\"Take me to Bluetooth settings\"",
                    "\"Open Wi-Fi settings\"",
                    "\"Where can I turn on my hotspot?\"",
                    "\"Help me make text bigger\"",
                    "\"Go home\" or \"Go back\"",
                    "\"Find Rahul in my contacts\"",
                    "\"Add Rahul 9876543210\""
                )
                examples.forEach { example ->
                    Text(
                        text = "• $example",
                        fontSize = 17.sp,
                        color = Color(0xFF1A1C1E)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    enabledLabel: String,
    disabledLabel: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    val borderColor = if (isEnabled) ElderAssistColors.Success else ElderAssistColors.Warning
    val badgeColor = if (isEnabled) ElderAssistColors.Success else ElderAssistColors.Warning

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElderAssistColors.SurfaceContainerLowest),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = badgeColor
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = if (isEnabled) enabledLabel else disabledLabel,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Text(
                text = description,
                fontSize = 16.sp,
                color = Color(0xFF43474E)
            )

            if (!isEnabled) {
                Button(
                    onClick = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElderAssistColors.Primary,
                        contentColor = ElderAssistColors.OnPrimary
                    )
                ) {
                    Text(
                        text = actionLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
