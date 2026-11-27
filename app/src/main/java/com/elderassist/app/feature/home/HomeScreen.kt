package com.elderassist.app.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elderassist.app.core.model.ClarificationOption
import com.elderassist.app.core.model.ContactCandidate
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.core.model.TaskStatus
import com.elderassist.app.feature.confirmation.ConfirmationDialog
import com.elderassist.app.ui.theme.ElderAssistColors
import java.util.Calendar

private enum class HomeTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    VOICE("Voice", Icons.Default.Mic),
    SETTINGS("Settings", Icons.Default.Settings),
    FAMILY("Family", Icons.Default.Group)
}

/**
 * Root composable switching between the main Voice Guide experience and Setup & Help.
 */
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
        color = ElderAssistColors.Surface
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
            VoiceGuideHomeScaffold(
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
private fun VoiceGuideHomeScaffold(
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
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.HOME) }
    var showManualInput by rememberSaveable { mutableStateOf(false) }

    val effectiveShowManual = showManualInput ||
        state.manualInput.isNotBlank() ||
        !state.isSpeechRecognitionAvailable ||
        selectedTab == HomeTab.VOICE

    Scaffold(
        containerColor = ElderAssistColors.Surface,
        topBar = {
            VoiceGuideTopBar(
                onMenuTap = onOpenSetupHelp,
                onHelpTap = onOpenSetupHelp
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ElderAssistColors.Surface.copy(alpha = 0.96f))
                    .navigationBarsPadding()
            ) {
                FloatingVoiceListeningUnit(
                    isListening = state.isListening,
                    isBusy = state.isBusy,
                    isManualExpanded = effectiveShowManual,
                    onToggleManual = { showManualInput = !showManualInput },
                    onMicTap = onMicTap,
                    onStopCancel = onStopCancel,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
                HorizontalDivider(color = ElderAssistColors.SurfaceContainerHigh, thickness = 1.dp)
                VoiceGuideBottomBar(
                    selectedTab = selectedTab,
                    onSelectTab = { tab ->
                        selectedTab = tab
                        onOpenSettingsShortcuts(tab == HomeTab.SETTINGS)
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            when (selectedTab) {
                HomeTab.HOME -> HomeTabContent(
                    state = state,
                    showManualInput = effectiveShowManual,
                    onMicTap = onMicTap,
                    onStopCancel = onStopCancel,
                    onManualInputChange = onManualInputChange,
                    onSubmitManual = onSubmitManual,
                    onRetryTranscript = onRetryTranscript,
                    onSelectClarification = onSelectClarification,
                    onSelectContact = onSelectContact,
                    onDismissContactSelection = onDismissContactSelection,
                    onQuickSettingTap = onQuickSettingTap,
                    onQuickContactsTap = onQuickContactsTap
                )

                HomeTab.VOICE -> VoiceTabContent(
                    state = state,
                    onMicTap = onMicTap,
                    onStopCancel = onStopCancel,
                    onManualInputChange = onManualInputChange,
                    onSubmitManual = onSubmitManual,
                    onRetryTranscript = onRetryTranscript,
                    onSelectClarification = onSelectClarification,
                    onSelectContact = onSelectContact,
                    onDismissContactSelection = onDismissContactSelection
                )

                HomeTab.SETTINGS -> SettingsTabContent(
                    onQuickSettingTap = onQuickSettingTap,
                    onOpenSetupHelp = onOpenSetupHelp
                )

                HomeTab.FAMILY -> FamilyTabContent(
                    state = state,
                    onQuickContactsTap = onQuickContactsTap,
                    onSelectContact = onSelectContact,
                    onDismissContactSelection = onDismissContactSelection,
                    onSuggestSearch = {
                        showManualInput = true
                        onManualInputChange("Find ")
                    }
                )
            }
        }
    }
}

@Composable
private fun VoiceGuideTopBar(
    onMenuTap: () -> Unit,
    onHelpTap: () -> Unit
) {
    Surface(
        color = ElderAssistColors.Surface.copy(alpha = 0.94f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(76.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onMenuTap,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Setup and menu",
                        tint = ElderAssistColors.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = "ELDERASSIST",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElderAssistColors.Secondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Voice Guide Home",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ElderAssistColors.Primary,
                        modifier = Modifier.semantics { heading() }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onHelpTap,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Voice Guide Help",
                        tint = ElderAssistColors.OnSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ElderAssistColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User profile",
                        tint = ElderAssistColors.OnPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTabContent(
    state: HomeUiState,
    showManualInput: Boolean,
    onMicTap: () -> Unit,
    onStopCancel: () -> Unit,
    onManualInputChange: (String) -> Unit,
    onSubmitManual: () -> Unit,
    onRetryTranscript: () -> Unit,
    onSelectClarification: (ClarificationOption) -> Unit,
    onSelectContact: (ContactCandidate) -> Unit,
    onDismissContactSelection: () -> Unit,
    onQuickSettingTap: (SettingDestination) -> Unit,
    onQuickContactsTap: () -> Unit
) {
    GreetingHeaderSection(state = state)

    if (state.isListening || state.isBusy) {
        StopCancelBanner(onStopCancel = onStopCancel)
    }

    if (state.status != TaskStatus.IDLE || state.statusDetail != null) {
        StatusFeedbackCard(state = state)
    }

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

    AnimatedVisibility(visible = showManualInput) {
        ManualCommandCard(
            manualInput = state.manualInput,
            onManualInputChange = onManualInputChange,
            onSubmitManual = onSubmitManual
        )
    }

    VoiceGuideHighlightCard(
        state = state,
        onActionTap = onMicTap
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "QUICK SHORTCUTS",
            style = MaterialTheme.typography.labelMedium,
            color = ElderAssistColors.OnSurfaceVariant,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        QuickShortcutCard(
            title = "Wi-Fi & Internet",
            subtitle = "Open Wi-Fi & network settings",
            icon = Icons.Default.Wifi,
            iconContainerColor = ElderAssistColors.PrimaryFixed,
            iconTint = ElderAssistColors.OnPrimaryFixed,
            onClick = { onQuickSettingTap(SettingDestination.WIFI) }
        )

        QuickShortcutCard(
            title = "Bluetooth Audio",
            subtitle = "Connect speakers & headphones",
            icon = Icons.Default.Headphones,
            iconContainerColor = ElderAssistColors.SurfaceContainerHigh,
            iconTint = ElderAssistColors.Primary,
            onClick = { onQuickSettingTap(SettingDestination.BLUETOOTH) }
        )

        QuickShortcutCard(
            title = "Call Family & Friends",
            subtitle = "Open or search your contacts",
            icon = Icons.Default.PhoneInTalk,
            iconContainerColor = ElderAssistColors.SecondaryFixed,
            iconTint = ElderAssistColors.OnSecondaryFixedVariant,
            onClick = onQuickContactsTap
        )

        QuickShortcutCard(
            title = "Magnifier & Large Text",
            subtitle = "Screen zoom, brightness & text size",
            icon = Icons.Default.FormatSize,
            iconContainerColor = ElderAssistColors.TertiaryFixed,
            iconTint = ElderAssistColors.OnTertiaryFixedVariant,
            onClick = { onQuickSettingTap(SettingDestination.DISPLAY) }
        )
    }

    SuggestionPromptPill(
        prefix = "Try saying: ",
        exampleOne = "\"Open Bluetooth settings\"",
        exampleTwo = "\"Find Rahul\""
    )
}

@Composable
private fun VoiceTabContent(
    state: HomeUiState,
    onMicTap: () -> Unit,
    onStopCancel: () -> Unit,
    onManualInputChange: (String) -> Unit,
    onSubmitManual: () -> Unit,
    onRetryTranscript: () -> Unit,
    onSelectClarification: (ClarificationOption) -> Unit,
    onSelectContact: (ContactCandidate) -> Unit,
    onDismissContactSelection: () -> Unit
) {
    Text(
        text = "Voice Assistant",
        style = MaterialTheme.typography.headlineLarge,
        color = ElderAssistColors.Primary
    )
    Text(
        text = "Tap the microphone below to speak, or type a command.",
        style = MaterialTheme.typography.bodyLarge,
        color = ElderAssistColors.OnSurfaceVariant
    )

    if (state.isListening || state.isBusy) {
        StopCancelBanner(onStopCancel = onStopCancel)
    }

    StatusFeedbackCard(state = state)

    if (state.transcript.isNotBlank()) {
        TranscriptCard(
            transcript = state.transcript,
            onRetry = onRetryTranscript
        )
    }

    state.pendingClarification?.let { clarification ->
        ClarificationCard(clarification = clarification, onSelect = onSelectClarification)
    }

    state.pendingContactChoice?.let { choice ->
        ContactSelectionCard(
            choice = choice,
            onSelect = onSelectContact,
            onCancel = onDismissContactSelection
        )
    }

    ManualCommandCard(
        manualInput = state.manualInput,
        onManualInputChange = onManualInputChange,
        onSubmitManual = onSubmitManual
    )

    Button(
        onClick = onMicTap,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (state.isListening) ElderAssistColors.Error else ElderAssistColors.Secondary,
            contentColor = ElderAssistColors.OnSecondary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Icon(
            imageVector = if (state.isListening) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (state.isListening) "Stop Listening" else "Speak a Command",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsTabContent(
    onQuickSettingTap: (SettingDestination) -> Unit,
    onOpenSetupHelp: () -> Unit
) {
    Text(
        text = "Quick Settings",
        style = MaterialTheme.typography.headlineLarge,
        color = ElderAssistColors.Primary
    )
    Text(
        text = "Jump directly to phone settings or check permissions.",
        style = MaterialTheme.typography.bodyLarge,
        color = ElderAssistColors.OnSurfaceVariant
    )

    SettingDestination.entries.forEach { dest ->
        QuickShortcutCard(
            title = dest.displayName,
            subtitle = "Open ${dest.displayName.lowercase()}",
            icon = Icons.Default.Settings,
            iconContainerColor = ElderAssistColors.SurfaceContainerHigh,
            iconTint = ElderAssistColors.Primary,
            onClick = { onQuickSettingTap(dest) }
        )
    }

    OutlinedButton(
        onClick = onOpenSetupHelp,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, ElderAssistColors.Primary),
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            tint = ElderAssistColors.Primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Permissions & Setup Checklist",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = ElderAssistColors.Primary
        )
    }
}

@Composable
private fun FamilyTabContent(
    state: HomeUiState,
    onQuickContactsTap: () -> Unit,
    onSelectContact: (ContactCandidate) -> Unit,
    onDismissContactSelection: () -> Unit,
    onSuggestSearch: () -> Unit
) {
    Text(
        text = "Family & Contacts",
        style = MaterialTheme.typography.headlineLarge,
        color = ElderAssistColors.Primary
    )
    Text(
        text = "Quickly open Contacts or ask ElderAssist to find someone.",
        style = MaterialTheme.typography.bodyLarge,
        color = ElderAssistColors.OnSurfaceVariant
    )

    state.pendingContactChoice?.let { choice ->
        ContactSelectionCard(
            choice = choice,
            onSelect = onSelectContact,
            onCancel = onDismissContactSelection
        )
    }

    QuickShortcutCard(
        title = "Open Contacts App",
        subtitle = "View all saved family & friends",
        icon = Icons.Default.Contacts,
        iconContainerColor = ElderAssistColors.SecondaryFixed,
        iconTint = ElderAssistColors.OnSecondaryFixedVariant,
        onClick = onQuickContactsTap
    )

    QuickShortcutCard(
        title = "Find a Contact by Name",
        subtitle = "Type or say \"Find <name>\"",
        icon = Icons.Default.PhoneInTalk,
        iconContainerColor = ElderAssistColors.PrimaryFixed,
        iconTint = ElderAssistColors.OnPrimaryFixed,
        onClick = onSuggestSearch
    )
}

@Composable
private fun GreetingHeaderSection(state: HomeUiState) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val (pillBg, pillFg, dotColor, statusText) = when {
        state.isListening -> Quad(
            ElderAssistColors.ErrorContainer,
            ElderAssistColors.OnErrorContainer,
            ElderAssistColors.Error,
            "Listening now"
        )
        state.isBusy -> Quad(
            ElderAssistColors.WarningContainer,
            ElderAssistColors.Warning,
            ElderAssistColors.Warning,
            "Working..."
        )
        state.status == TaskStatus.FAILED -> Quad(
            ElderAssistColors.ErrorContainer,
            ElderAssistColors.OnErrorContainer,
            ElderAssistColors.Error,
            "Needs attention"
        )
        else -> Quad(
            ElderAssistColors.SecondaryFixed,
            ElderAssistColors.OnSecondaryFixed,
            ElderAssistColors.Secondary,
            "Ready to assist"
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(pillBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = pillFg
                )
            }

            val badgeText = if (state.preferences.experimentalAiNavigationEnabled) {
                "AI Nav: ON"
            } else if (state.isAccessibilityEnabled) {
                "Helper: Active"
            } else {
                "Voice & Text"
            }
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = ElderAssistColors.OnSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineLarge,
                color = ElderAssistColors.Primary
            )
            Text(
                text = "What would you like assistance with today?",
                style = MaterialTheme.typography.bodyLarge,
                color = ElderAssistColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun VoiceGuideHighlightCard(
    state: HomeUiState,
    onActionTap: () -> Unit
) {
    val headline = if (state.transcript.isNotBlank()) {
        "\"${state.transcript}\""
    } else {
        "Hands-Free Voice Guide"
    }
    val subtext = if (state.transcript.isNotBlank()) {
        state.statusHeadline
    } else {
        "Tap the button to speak a command naturally"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElderAssistColors.SurfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "VOICE GUIDE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElderAssistColors.Secondary,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineMedium,
                    color = ElderAssistColors.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElderAssistColors.OnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onActionTap,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElderAssistColors.Primary,
                    contentColor = ElderAssistColors.OnPrimary
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(56.dp)
                    .semantics {
                        contentDescription = if (state.isListening) "Stop listening" else "Start voice guide"
                    }
            ) {
                Icon(
                    imageVector = if (state.isListening) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElderAssistColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 76.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    color = ElderAssistColors.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElderAssistColors.OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ElderAssistColors.OnSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun SuggestionPromptPill(
    prefix: String,
    exampleOne: String,
    exampleTwo: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ElderAssistColors.SurfaceContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(ElderAssistColors.SecondaryFixed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = ElderAssistColors.Secondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = ElderAssistColors.OnSurfaceVariant)) {
                    append(prefix)
                }
                withStyle(SpanStyle(color = ElderAssistColors.Primary, fontWeight = FontWeight.Bold)) {
                    append(exampleOne)
                }
                withStyle(SpanStyle(color = ElderAssistColors.OnSurfaceVariant)) {
                    append(" or ")
                }
                withStyle(SpanStyle(color = ElderAssistColors.Primary, fontWeight = FontWeight.Bold)) {
                    append(exampleTwo)
                }
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun FloatingVoiceListeningUnit(
    isListening: Boolean,
    isBusy: Boolean,
    isManualExpanded: Boolean,
    onToggleManual: () -> Unit,
    onMicTap: () -> Unit,
    onStopCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(ElderAssistColors.SurfaceContainerHigh)
                .padding(horizontal = 14.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.RecordVoiceOver,
                contentDescription = null,
                tint = ElderAssistColors.Secondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isListening) "Listening... Tap Stop when done" else "Tap or speak naturally",
                style = MaterialTheme.typography.labelSmall,
                color = ElderAssistColors.OnSurfaceVariant
            )
        }

        Surface(
            shape = CircleShape,
            color = ElderAssistColors.SurfaceContainerLowest,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onToggleManual,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ElderAssistColors.SurfaceContainer)
                ) {
                    Icon(
                        imageVector = if (isManualExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (isManualExpanded) "Hide text input" else "Type a command",
                        tint = ElderAssistColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                AudioWaveVisualizer(
                    isActive = isListening || isBusy,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .padding(horizontal = 12.dp)
                )

                Button(
                    onClick = if (isBusy && !isListening) onStopCancel else onMicTap,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening || isBusy) ElderAssistColors.Error else ElderAssistColors.Secondary,
                        contentColor = ElderAssistColors.OnSecondary
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .semantics {
                            contentDescription = if (isListening) "Stop listening" else "Tap and speak a command"
                        }
                ) {
                    Icon(
                        imageVector = if (isListening || isBusy) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioWaveVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val barHeights = listOf(14f, 24f, 32f, 20f, 28f, 16f, 12f)
    val transition = rememberInfiniteTransition(label = "voice_wave")
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_pulse"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        barHeights.forEachIndexed { index, baseHeight ->
            val factor = if (isActive) {
                if (index % 2 == 0) pulse else (1.55f - pulse)
            } else {
                1f
            }
            val heightDp = (baseHeight * factor).coerceIn(8f, 34f).dp
            val barColor = if (index % 2 == 0) {
                ElderAssistColors.SecondaryContainer
            } else {
                ElderAssistColors.Secondary
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(heightDp)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun VoiceGuideBottomBar(
    selectedTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit
) {
    NavigationBar(
        containerColor = ElderAssistColors.Surface,
        tonalElevation = 0.dp
    ) {
        HomeTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElderAssistColors.Secondary,
                    selectedTextColor = ElderAssistColors.Secondary,
                    unselectedIconColor = ElderAssistColors.OnSurfaceVariant,
                    unselectedTextColor = ElderAssistColors.OnSurfaceVariant,
                    indicatorColor = ElderAssistColors.SecondaryFixed
                )
            )
        }
    }
}

@Composable
private fun StopCancelBanner(onStopCancel: () -> Unit) {
    Button(
        onClick = onStopCancel,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ElderAssistColors.Error,
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Stop / Cancel",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ManualCommandCard(
    manualInput: String,
    onManualInputChange: (String) -> Unit,
    onSubmitManual: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElderAssistColors.SurfaceContainerLowest),
        border = BorderStroke(1.dp, ElderAssistColors.OutlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Or type a command",
                style = MaterialTheme.typography.titleMedium,
                color = ElderAssistColors.Primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = manualInput,
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
                    enabled = manualInput.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElderAssistColors.Primary,
                        contentColor = ElderAssistColors.OnPrimary
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
}

@Composable
private fun StatusFeedbackCard(state: HomeUiState) {
    val (bgColor, borderColor, icon, iconTint) = when (state.status) {
        TaskStatus.COMPLETED -> Quad(
            ElderAssistColors.SuccessContainer,
            ElderAssistColors.Success,
            Icons.Default.CheckCircle,
            ElderAssistColors.Success
        )
        TaskStatus.FAILED -> Quad(
            ElderAssistColors.ErrorContainer,
            ElderAssistColors.Error,
            Icons.Default.ErrorOutline,
            ElderAssistColors.Error
        )
        TaskStatus.AWAITING_CLARIFICATION,
        TaskStatus.AWAITING_CONFIRMATION -> Quad(
            ElderAssistColors.WarningContainer,
            ElderAssistColors.Warning,
            Icons.Default.Info,
            ElderAssistColors.Warning
        )
        else -> Quad(
            ElderAssistColors.SurfaceContainerLow,
            ElderAssistColors.Primary,
            Icons.Default.Info,
            ElderAssistColors.Primary
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.5.dp, borderColor),
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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 25.sp,
                    color = ElderAssistColors.OnSurface
                )
            }
            state.statusDetail?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElderAssistColors.OnSurfaceVariant
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
        colors = CardDefaults.cardColors(containerColor = ElderAssistColors.SurfaceContainerLowest),
        border = BorderStroke(1.dp, ElderAssistColors.OutlineVariant),
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
                    style = MaterialTheme.typography.labelSmall,
                    color = ElderAssistColors.OnSurfaceVariant
                )
                Text(
                    text = "\"$transcript\"",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = ElderAssistColors.Primary
                )
            }
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Retry", fontSize = 15.sp)
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
        colors = CardDefaults.cardColors(containerColor = ElderAssistColors.WarningContainer),
        border = BorderStroke(1.5.dp, ElderAssistColors.Warning),
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
                color = ElderAssistColors.OnSurface
            )
            clarification.options.forEach { option ->
                Button(
                    onClick = { onSelect(option) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElderAssistColors.Primary,
                        contentColor = ElderAssistColors.OnPrimary
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
        colors = CardDefaults.cardColors(containerColor = ElderAssistColors.SurfaceContainerLowest),
        border = BorderStroke(1.5.dp, ElderAssistColors.Primary),
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
                color = ElderAssistColors.Primary
            )
            choice.candidates.forEach { candidate ->
                val label = buildString {
                    append(candidate.displayName)
                    candidate.phoneHint?.let { append(" ($it)") }
                }
                Button(
                    onClick = { onSelect(candidate) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElderAssistColors.Primary,
                        contentColor = ElderAssistColors.OnPrimary
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

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
