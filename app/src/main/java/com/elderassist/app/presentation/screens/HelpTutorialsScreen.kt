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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.elderassist.app.core.model.StitchIds

private data class VoiceExampleItem(
    val icon: ImageVector,
    val phrase: String
)

private data class GuideItem(
    val icon: ImageVector,
    val badgeBg: Color,
    val badgeTint: Color,
    val title: String,
    val body: String
)

@Composable
fun HelpTutorialsScreen(
    onListenExample: (String) -> Unit,
    onTryCommand: (String) -> Unit
) {
    var activeGuide by remember { mutableStateOf<GuideItem?>(null) }

    val examples = listOf(
        VoiceExampleItem(Icons.Default.Bluetooth, "Open my Bluetooth settings."),
        VoiceExampleItem(Icons.Default.Wifi, "Take me to Wi-Fi."),
        VoiceExampleItem(Icons.Default.Contacts, "Find Rahul in my contacts."),
        VoiceExampleItem(Icons.Default.FormatSize, "Help me increase text size.")
    )

    val guides = listOf(
        GuideItem(
            icon = Icons.Default.RecordVoiceOver,
            badgeBg = MaterialTheme.colorScheme.tertiaryContainer,
            badgeTint = MaterialTheme.colorScheme.onTertiaryContainer,
            title = "How to talk to ElderAssist",
            body = "1. Tap Ask ElderAssist at the bottom of any screen.\n2. Speak clearly at your normal pace.\n3. You can also tap the keyboard button next to Ask ElderAssist to type a command anytime."
        ),
        GuideItem(
            icon = Icons.Default.SmartToy,
            badgeBg = MaterialTheme.colorScheme.primaryContainer,
            badgeTint = MaterialTheme.colorScheme.onPrimaryContainer,
            title = "Set up Master AI",
            body = "Open the left menu and choose Master AI.\n• Standard commands like Wi-Fi, Bluetooth, and Home work locally without internet.\n• Master AI helps interpret unfamiliar phrases when online.\n• Experimental navigation stays Off unless you explicitly turn it on."
        ),
        GuideItem(
            icon = Icons.Default.AccessibilityNew,
            badgeBg = MaterialTheme.colorScheme.secondaryContainer,
            badgeTint = MaterialTheme.colorScheme.onSecondaryContainer,
            title = "Allow accessibility",
            body = "1. Open Permissions & Privacy from the menu.\n2. Tap Set up Accessibility.\n3. Find 'ElderAssist Navigation Helper' and switch it On.\n4. Note: If you installed ElderAssist directly from an APK file on Android 13+, first open Phone Settings -> Apps -> ElderAssist -> ⋮ -> Allow restricted settings."
        ),
        GuideItem(
            icon = Icons.Default.Build,
            badgeBg = MaterialTheme.colorScheme.surfaceContainerHigh,
            badgeTint = MaterialTheme.colorScheme.onSurfaceVariant,
            title = "Troubleshooting",
            body = "• Voice not heard? Check Microphone permission in Permissions & Privacy or use the keyboard input.\n• Hotspot page looks different? Some phone makers place Hotspot inside Wireless or Network settings.\n• Want to stop? Tap the red Stop button anytime."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(StitchIds.SCR_08),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Help & Tutorials",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Tap Listen to hear an example, or open a step-by-step guide.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Try saying...",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Voice Examples",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        examples.forEach { ex ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onTryCommand(ex.phrase) }
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ex.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = "\"${ex.phrase}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { onListenExample(ex.phrase) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Listen", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Text(
            text = "Guides",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        guides.forEach { guide ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { activeGuide = guide }
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(guide.badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = guide.icon,
                            contentDescription = null,
                            tint = guide.badgeTint
                        )
                    }

                    Text(
                        text = guide.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    activeGuide?.let { guide ->
        AlertDialog(
            onDismissRequest = { activeGuide = null },
            title = { Text(guide.title, style = MaterialTheme.typography.headlineSmall) },
            text = { Text(guide.body, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = { activeGuide = null }) {
                    Text("Got it")
                }
            }
        )
    }
}
