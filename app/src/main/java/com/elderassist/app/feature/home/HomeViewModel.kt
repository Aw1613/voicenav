package com.elderassist.app.feature.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elderassist.app.ElderAssistApplication
import com.elderassist.app.accessibility.AccessibilityServiceState
import com.elderassist.app.core.model.ActionExecutionResult
import com.elderassist.app.core.model.ActionId
import com.elderassist.app.core.model.ActionSource
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.AssistantMode
import com.elderassist.app.core.model.AssistantUiState
import com.elderassist.app.core.model.ClarificationOption
import com.elderassist.app.core.model.ContactCandidate
import com.elderassist.app.core.model.ContactProposal
import com.elderassist.app.core.model.ExecutionContext
import com.elderassist.app.core.model.Recovery
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.core.model.SupportedApp
import com.elderassist.app.core.model.TaskStatus
import com.elderassist.app.data.history.ActivityEvent
import com.elderassist.app.data.history.ActivityOutcome
import com.elderassist.app.feature.voice.SpeechState
import com.elderassist.app.intelligence.IntentInterpretationResult
import com.elderassist.app.intelligence.NavigationAgentOutcome
import com.elderassist.app.intelligence.RoutingResult
import com.elderassist.app.permissions.CapabilitySnapshot
import com.elderassist.app.settings.AppTextScale
import com.elderassist.app.settings.AppThemeMode
import com.elderassist.app.settings.FeedbackLevel
import com.elderassist.app.settings.SpeechSpeed
import com.elderassist.app.settings.UserPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class PendingConfirmation(
    val title: String,
    val previewMessage: String,
    val action: AssistantAction,
    val source: ActionSource,
    val proposal: ContactProposal? = null
)

data class PendingContactChoice(
    val query: String,
    val pendingNewName: String?,
    val candidates: List<ContactCandidate>
)

data class PendingClarification(
    val question: String,
    val options: List<ClarificationOption>
)

data class HomeUiState(
    val assistantState: AssistantUiState = AssistantUiState.Idle,
    val status: TaskStatus = TaskStatus.IDLE,
    val statusHeadline: String = "Tap the microphone and tell me what you want to do.",
    val statusDetail: String? = null,
    val transcript: String = "",
    val manualInput: String = "",
    val isTextSheetOpen: Boolean = false,
    val speechState: SpeechState = SpeechState.Idle,
    val isListening: Boolean = false,
    val isBusy: Boolean = false,
    val capabilities: CapabilitySnapshot = CapabilitySnapshot(),
    val hasMicPermission: Boolean = false,
    val hasContactsPermission: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isSpeechRecognitionAvailable: Boolean = true,
    val isGeminiConfigured: Boolean = false,
    val speechProviderName: String = "Android SpeechRecognizer",
    val preferences: UserPreferences = UserPreferences(),
    val activityHistory: List<ActivityEvent> = emptyList(),
    val contactSearchQuery: String = "",
    val contactSearchResults: List<ContactCandidate> = emptyList(),
    val micTestResult: String? = null,
    val pendingConfirmation: PendingConfirmation? = null,
    val pendingContactChoice: PendingContactChoice? = null,
    val pendingClarification: PendingClarification? = null,
    val requestMicPermissionPrompt: Boolean = false,
    val requestContactsPermissionPrompt: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as ElderAssistApplication).container

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var activeTaskJob: Job? = null
    private var activeTaskCancelled: Boolean = false
    private var pendingCommandAfterPermission: String? = null
    private var startListeningAfterMicPermission: Boolean = false
    private var isMicTestMode: Boolean = false

    init {
        refreshEnvironmentState()

        viewModelScope.launch {
            container.appSettings.preferences.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }

        viewModelScope.launch {
            container.historyRepository.events.collect { events ->
                _uiState.update { it.copy(activityHistory = events) }
            }
        }

        viewModelScope.launch {
            container.voiceSessionManager.speechState.collect { speechState ->
                _uiState.update { state ->
                    val listening =
                        speechState is SpeechState.Listening || speechState is SpeechState.Processing
                    val partial = (speechState as? SpeechState.Listening)?.partialTranscript
                    val nextAssistantState = when (speechState) {
                        is SpeechState.Listening -> AssistantUiState.Listening(partial.orEmpty())
                        is SpeechState.Processing -> AssistantUiState.Processing(state.transcript)
                        else -> state.assistantState
                    }
                    state.copy(
                        speechState = speechState,
                        isListening = listening,
                        transcript = if (!partial.isNullOrBlank()) partial else state.transcript,
                        assistantState = nextAssistantState
                    )
                }
            }
        }

        viewModelScope.launch {
            AccessibilityServiceState.isConnected.collect {
                refreshEnvironmentState()
            }
        }
    }

    fun refreshEnvironmentState() {
        val caps = container.permissionRepository.refresh()
        _uiState.update {
            it.copy(
                capabilities = caps,
                hasMicPermission = caps.microphoneGranted,
                hasContactsPermission = caps.contactsReadGranted,
                isAccessibilityEnabled = caps.accessibilityEnabled,
                isSpeechRecognitionAvailable = container.voiceSessionManager.isSpeechAvailable(),
                isGeminiConfigured = caps.geminiKeyConfigured,
                speechProviderName = container.voiceSessionManager.providerName
            )
        }
    }

    fun setTextSheetOpen(open: Boolean) {
        _uiState.update { it.copy(isTextSheetOpen = open) }
    }

    fun onManualInputChanged(value: String) {
        _uiState.update { it.copy(manualInput = value) }
    }

    fun onMicButtonTapped() {
        refreshEnvironmentState()
        if (_uiState.value.isListening) {
            container.voiceSessionManager.stopListening()
            return
        }

        isMicTestMode = false
        if (!_uiState.value.hasMicPermission) {
            startListeningAfterMicPermission = true
            _uiState.update {
                it.copy(
                    requestMicPermissionPrompt = true,
                    assistantState = AssistantUiState.NeedsSetup(it.capabilities.missingCapabilities()),
                    status = TaskStatus.IDLE,
                    statusHeadline = "Please allow Microphone access so I can hear your request."
                )
            }
            return
        }

        startListeningInternal()
    }

    fun testMicrophone() {
        refreshEnvironmentState()
        if (_uiState.value.isListening) {
            container.voiceSessionManager.stopListening()
            return
        }
        isMicTestMode = true
        if (!_uiState.value.hasMicPermission) {
            startListeningAfterMicPermission = true
            _uiState.update { it.copy(requestMicPermissionPrompt = true) }
            return
        }
        startListeningInternal(testOnly = true)
    }

    fun onMicPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(requestMicPermissionPrompt = false) }
        refreshEnvironmentState()
        val shouldStart = startListeningAfterMicPermission
        startListeningAfterMicPermission = false

        if (granted && shouldStart) {
            startListeningInternal(testOnly = isMicTestMode)
        } else if (!granted) {
            isMicTestMode = false
            setFeedback(
                status = TaskStatus.FAILED,
                assistantState = AssistantUiState.Failure(
                    userMessage = "Microphone permission was denied. You can still type your request.",
                    recovery = Recovery.OPEN_PERMISSIONS
                ),
                headline = "Microphone permission was denied. You can still type your request.",
                important = true
            )
        }
    }

    fun onContactsPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(requestContactsPermissionPrompt = false) }
        refreshEnvironmentState()
        val pending = pendingCommandAfterPermission
        pendingCommandAfterPermission = null

        if (granted) {
            val inlineQuery = _uiState.value.contactSearchQuery
            if (inlineQuery.isNotBlank()) {
                searchContactsInline(inlineQuery)
            }
            if (!pending.isNullOrBlank()) {
                submitCommand(pending)
            }
        } else {
            setFeedback(
                status = TaskStatus.FAILED,
                assistantState = AssistantUiState.Failure(
                    userMessage = "Contacts permission is needed to search contacts by name.",
                    recovery = Recovery.OPEN_PERMISSIONS
                ),
                headline = "Contacts permission is needed to search contacts by name.",
                important = true
            )
        }
    }

    fun submitManualCommand() {
        val text = _uiState.value.manualInput.trim()
        if (text.isEmpty()) return
        _uiState.update { it.copy(manualInput = "", isTextSheetOpen = false) }
        submitCommand(text)
    }

    fun retryLastTranscript() {
        val current = _uiState.value.transcript.trim()
        if (current.isNotEmpty()) {
            submitCommand(current)
        } else {
            onMicButtonTapped()
        }
    }

    fun selectClarificationOption(option: ClarificationOption) {
        _uiState.update { it.copy(pendingClarification = null) }
        submitCommand(option.canonicalUtterance)
    }

    fun onContactSearchQueryChanged(query: String) {
        _uiState.update { it.copy(contactSearchQuery = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(contactSearchResults = emptyList()) }
        } else if (_uiState.value.hasContactsPermission) {
            searchContactsInline(query)
        }
    }

    fun searchContactsInline(query: String) {
        refreshEnvironmentState()
        if (!_uiState.value.hasContactsPermission) {
            _uiState.update { it.copy(requestContactsPermissionPrompt = true) }
            return
        }
        val matches = container.contactsRepository.searchContacts(query)
        _uiState.update { it.copy(contactSearchResults = matches) }
    }

    fun proposeCreateContact(displayName: String, phone: String) {
        val cleanName = displayName.trim()
        val cleanPhone = phone.trim()
        if (cleanName.isEmpty() || cleanPhone.filter { it.isDigit() }.length < 3) {
            setFeedback(
                status = TaskStatus.FAILED,
                assistantState = AssistantUiState.Failure("Please enter both a name and a valid phone number."),
                headline = "Please enter both a name and a valid phone number.",
                important = true
            )
            return
        }
        executeDirectAction(
            action = AssistantAction.CreateContact(cleanName, cleanPhone),
            source = ActionSource.DETERMINISTIC,
            summary = "Preparing to save $cleanName."
        )
    }

    fun proposeRenameContact(oldName: String, newName: String) {
        val cleanOld = oldName.trim()
        val cleanNew = newName.trim()
        if (cleanOld.isEmpty() || cleanNew.isEmpty()) {
            setFeedback(
                status = TaskStatus.FAILED,
                assistantState = AssistantUiState.Failure("Please enter both the current name and the new name."),
                headline = "Please enter both the current name and the new name.",
                important = true
            )
            return
        }
        if (!_uiState.value.hasContactsPermission) {
            pendingCommandAfterPermission = "rename $cleanOld to $cleanNew"
            _uiState.update { it.copy(requestContactsPermissionPrompt = true) }
            return
        }
        executeDirectAction(
            action = AssistantAction.RenameContact(
                currentDisplayName = cleanOld,
                newDisplayName = cleanNew
            ),
            source = ActionSource.DETERMINISTIC,
            summary = "Preparing to rename $cleanOld."
        )
    }

    fun selectContactCandidate(candidate: ContactCandidate) {
        val choice = _uiState.value.pendingContactChoice ?: return
        _uiState.update { it.copy(pendingContactChoice = null) }

        if (!choice.pendingNewName.isNullOrBlank()) {
            val renameAction = AssistantAction.RenameContact(
                contactId = candidate.contactId,
                lookupKey = candidate.lookupKey,
                currentDisplayName = candidate.displayName,
                newDisplayName = choice.pendingNewName
            )
            executeDirectAction(
                action = renameAction,
                source = ActionSource.DETERMINISTIC,
                summary = "Preparing to rename ${candidate.displayName}."
            )
        } else {
            val phoneDetail = candidate.phoneHint?.let { " (phone $it)" }.orEmpty()
            val message = "Found ${candidate.displayName}$phoneDetail."
            setFeedback(
                status = TaskStatus.COMPLETED,
                assistantState = AssistantUiState.Success(message),
                headline = message,
                detail = "You can open Contacts to view full details.",
                important = false
            )
            recordHistory(AssistantAction.SearchContact(choice.query), ActivityOutcome.SUCCESS)
        }
    }

    fun dismissContactSelection() {
        _uiState.update { it.copy(pendingContactChoice = null) }
        setFeedback(
            status = TaskStatus.CANCELLED,
            assistantState = AssistantUiState.Idle,
            headline = "Cancelled contact selection.",
            important = false
        )
    }

    fun confirmPendingAction() {
        val pending = _uiState.value.pendingConfirmation ?: return
        _uiState.update { it.copy(pendingConfirmation = null) }
        executeDirectAction(
            action = pending.action,
            source = pending.source,
            summary = pending.previewMessage,
            confirmationGranted = true
        )
    }

    fun cancelPendingAction() {
        val pending = _uiState.value.pendingConfirmation
        _uiState.update { it.copy(pendingConfirmation = null) }
        if (pending != null) {
            recordHistory(pending.action, ActivityOutcome.CANCELLED)
        }
        setFeedback(
            status = TaskStatus.CANCELLED,
            assistantState = AssistantUiState.Idle,
            headline = "Cancelled. No changes were saved.",
            important = true
        )
    }

    fun cancelActiveTask() {
        activeTaskCancelled = true
        activeTaskJob?.cancel()
        activeTaskJob = null
        isMicTestMode = false
        container.voiceSessionManager.cancelListening()
        _uiState.update {
            it.copy(
                isListening = false,
                isBusy = false,
                pendingClarification = null,
                pendingConfirmation = null,
                pendingContactChoice = null
            )
        }
        setFeedback(
            status = TaskStatus.CANCELLED,
            assistantState = AssistantUiState.Idle,
            headline = "Stopped.",
            important = true
        )
    }

    fun openQuickSetting(destination: SettingDestination) {
        executeDirectAction(
            action = AssistantAction.OpenSetting(destination),
            source = ActionSource.DETERMINISTIC,
            summary = "Opening ${destination.displayName}."
        )
    }

    fun openQuickContacts() {
        executeDirectAction(
            action = AssistantAction.OpenApp(SupportedApp.CONTACTS),
            source = ActionSource.DETERMINISTIC,
            summary = "Opening Contacts."
        )
    }

    fun triggerGlobalAction(action: AssistantAction) {
        executeDirectAction(
            action = action,
            source = ActionSource.DETERMINISTIC,
            summary = "Navigating..."
        )
    }

    fun openAccessibilitySettings() {
        container.intentExecutor.openAccessibilitySettings()
    }

    fun openAppDetailsSettings() {
        container.intentExecutor.openAppDetailsSettings()
    }

    fun requestMicPermission() {
        startListeningAfterMicPermission = false
        _uiState.update { it.copy(requestMicPermissionPrompt = true) }
    }

    fun requestContactsPermission() {
        _uiState.update { it.copy(requestContactsPermissionPrompt = true) }
    }

    fun setMasterAiEnabled(enabled: Boolean) {
        viewModelScope.launch { container.appSettings.setMasterAiEnabled(enabled) }
    }

    fun toggleExperimentalAiNavigation(enabled: Boolean) {
        viewModelScope.launch { container.appSettings.setExperimentalAiNavigationEnabled(enabled) }
    }

    fun toggleTts(enabled: Boolean) {
        viewModelScope.launch {
            container.appSettings.setTtsEnabled(enabled)
            if (!enabled) container.voiceSessionManager.stopSpeaking()
        }
    }

    fun setSpeechLanguageTag(tag: String) {
        viewModelScope.launch { container.appSettings.setSpeechLanguageTag(tag) }
    }

    fun setSpeechSpeed(speed: SpeechSpeed) {
        viewModelScope.launch { container.appSettings.setSpeechSpeed(speed) }
    }

    fun setFeedbackLevel(level: FeedbackLevel) {
        viewModelScope.launch { container.appSettings.setFeedbackLevel(level) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { container.appSettings.setThemeMode(mode) }
    }

    fun setTextScale(scale: AppTextScale) {
        viewModelScope.launch { container.appSettings.setTextScale(scale) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { container.appSettings.setHapticsEnabled(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { container.appSettings.setNotificationsEnabled(enabled) }
    }

    fun resetPreferences() {
        viewModelScope.launch {
            container.appSettings.resetToDefaults()
            setFeedback(
                status = TaskStatus.COMPLETED,
                assistantState = AssistantUiState.Success("App preferences reset to defaults."),
                headline = "App preferences reset to defaults.",
                important = false
            )
        }
    }

    fun clearActivityHistory() {
        viewModelScope.launch {
            container.historyRepository.clearHistory()
        }
    }

    fun speakExamplePhrase(phrase: String) {
        val prefs = _uiState.value.preferences
        container.voiceSessionManager.speakExample(
            message = phrase,
            speechRate = prefs.speechSpeed.rate,
            languageTag = prefs.speechLanguageTag
        )
    }

    fun submitCommand(rawText: String) {
        val text = rawText.trim()
        if (text.isEmpty()) return

        activeTaskJob?.cancel()
        activeTaskCancelled = false

        _uiState.update {
            it.copy(
                transcript = text,
                isBusy = true,
                assistantState = AssistantUiState.Processing(text),
                status = TaskStatus.INTERPRETING,
                statusHeadline = "Understanding your request...",
                statusDetail = null,
                pendingClarification = null,
                pendingConfirmation = null,
                pendingContactChoice = null
            )
        }

        activeTaskJob = viewModelScope.launch {
            val prefs = _uiState.value.preferences
            val mode = if (prefs.masterAiEnabled && prefs.experimentalAiNavigationEnabled) {
                AssistantMode.AI_EXPERIMENTAL
            } else {
                AssistantMode.DETERMINISTIC
            }

            when (val routing = container.intentRouter.route(text, mode)) {
                RoutingResult.Empty -> {
                    _uiState.update { it.copy(isBusy = false) }
                    setFeedback(
                        status = TaskStatus.FAILED,
                        assistantState = AssistantUiState.Failure("Please tell me what you would like to do."),
                        headline = "Please tell me what you would like to do.",
                        important = true
                    )
                }

                RoutingResult.Cancelled -> {
                    _uiState.update { it.copy(isBusy = false) }
                    recordHistory(AssistantAction.Cancel, ActivityOutcome.CANCELLED)
                    setFeedback(
                        status = TaskStatus.CANCELLED,
                        assistantState = AssistantUiState.Idle,
                        headline = "Cancelled.",
                        important = true
                    )
                }

                is RoutingResult.ClarificationRequired -> {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            pendingClarification = PendingClarification(
                                question = routing.question,
                                options = routing.options
                            )
                        )
                    }
                    setFeedback(
                        status = TaskStatus.AWAITING_CLARIFICATION,
                        assistantState = AssistantUiState.Idle,
                        headline = routing.question,
                        important = true
                    )
                }

                is RoutingResult.Unsupported -> {
                    _uiState.update { it.copy(isBusy = false) }
                    recordHistory(
                        AssistantAction.Unsupported(routing.message, routing.fallbackDestination),
                        ActivityOutcome.BLOCKED
                    )
                    setFeedback(
                        status = TaskStatus.FAILED,
                        assistantState = AssistantUiState.Failure(routing.message),
                        headline = routing.message,
                        important = true
                    )
                }

                is RoutingResult.Deterministic -> {
                    handleResolvedAction(
                        originalTranscript = text,
                        action = routing.action,
                        source = routing.source,
                        summary = routing.summaryForUser
                    )
                }

                is RoutingResult.NeedsAiInterpretation -> {
                    if (!prefs.masterAiEnabled) {
                        _uiState.update { it.copy(isBusy = false) }
                        setFeedback(
                            status = TaskStatus.FAILED,
                            assistantState = AssistantUiState.Failure(
                                "Master AI is currently paused. Turn it on in Master AI or use a direct command like 'Open Bluetooth'."
                            ),
                            headline = "Master AI is paused. Turn it on in Master AI or try a direct command like 'Open Bluetooth'.",
                            important = true
                        )
                    } else {
                        handleAiFallback(
                            originalTranscript = text,
                            allowExperimentalNavigation = routing.allowExperimentalNavigation
                        )
                    }
                }
            }
        }
    }

    private suspend fun handleAiFallback(
        originalTranscript: String,
        allowExperimentalNavigation: Boolean
    ) {
        val aiResult = container.geminiIntentInterpreter.interpret(originalTranscript)
        if (activeTaskCancelled) return

        when (aiResult) {
            is IntentInterpretationResult.Interpreted -> {
                handleResolvedAction(
                    originalTranscript = originalTranscript,
                    action = aiResult.action,
                    source = ActionSource.GEMINI_INTENT,
                    summary = aiResult.summaryForUser
                )
            }

            is IntentInterpretationResult.Rejected -> {
                if (allowExperimentalNavigation && _uiState.value.isAccessibilityEnabled) {
                    val service = AccessibilityServiceState.activeService()
                    if (service != null) {
                        _uiState.update {
                            it.copy(
                                status = TaskStatus.NAVIGATING_AI,
                                assistantState = AssistantUiState.Executing("Trying guided navigation..."),
                                statusHeadline = "Trying experimental guided navigation...",
                                statusDetail = "Maximum 5 safe steps. Tap Stop anytime."
                            )
                        }
                        val baseContext = buildExecutionContext(confirmationGranted = false)
                        val navOutcome = container.generalNavigationAgent.runBoundedNavigation(
                            userGoal = originalTranscript,
                            service = service,
                            baseContext = baseContext,
                            isCancelled = { activeTaskCancelled },
                            onStepProgress = { stepMsg ->
                                _uiState.update { it.copy(statusDetail = stepMsg) }
                            }
                        )
                        _uiState.update { it.copy(isBusy = false) }
                        when (navOutcome) {
                            is NavigationAgentOutcome.Finished -> {
                                recordHistory(
                                    AssistantAction.Explain(navOutcome.message),
                                    ActivityOutcome.SUCCESS
                                )
                                setFeedback(
                                    status = TaskStatus.COMPLETED,
                                    assistantState = AssistantUiState.Success(navOutcome.message),
                                    headline = navOutcome.message,
                                    important = false
                                )
                            }

                            is NavigationAgentOutcome.NeedsClarification -> setFeedback(
                                status = TaskStatus.AWAITING_CLARIFICATION,
                                assistantState = AssistantUiState.Idle,
                                headline = navOutcome.question,
                                important = true
                            )

                            is NavigationAgentOutcome.StoppedSafely -> {
                                recordHistory(
                                    AssistantAction.Unsupported(navOutcome.message),
                                    ActivityOutcome.BLOCKED
                                )
                                setFeedback(
                                    status = TaskStatus.FAILED,
                                    assistantState = AssistantUiState.Failure(navOutcome.message),
                                    headline = navOutcome.message,
                                    important = true
                                )
                            }
                        }
                        return
                    }
                }

                _uiState.update { it.copy(isBusy = false) }
                setFeedback(
                    status = TaskStatus.FAILED,
                    assistantState = AssistantUiState.Failure(aiResult.userMessage),
                    headline = aiResult.userMessage,
                    detail = "Try commands like 'Open Bluetooth settings', 'Go home', or 'Find Rahul'.",
                    important = true
                )
            }
        }
    }

    private fun handleResolvedAction(
        originalTranscript: String,
        action: AssistantAction,
        source: ActionSource,
        summary: String
    ) {
        if ((action is AssistantAction.SearchContact || action is AssistantAction.RenameContact || (action is AssistantAction.PlaceCall && action.recipient.any { it.isLetter() })) &&
            !_uiState.value.hasContactsPermission
        ) {
            pendingCommandAfterPermission = originalTranscript
            _uiState.update {
                it.copy(
                    isBusy = false,
                    requestContactsPermissionPrompt = true
                )
            }
            setFeedback(
                status = TaskStatus.IDLE,
                assistantState = AssistantUiState.NeedsSetup(setOf(com.elderassist.app.core.model.Capability.CONTACTS_READ)),
                headline = "Please allow Contacts access so I can find that contact for you.",
                important = true
            )
            return
        }

        executeDirectAction(
            action = action,
            source = source,
            summary = summary,
            confirmationGranted = false
        )
    }

    private fun executeDirectAction(
        action: AssistantAction,
        source: ActionSource,
        summary: String,
        confirmationGranted: Boolean = false
    ) {
        refreshEnvironmentState()
        _uiState.update {
            it.copy(
                assistantState = AssistantUiState.Executing(summary)
            )
        }
        val context = buildExecutionContext(confirmationGranted)
        val plan = container.actionPlanner.createPlan(
            action = action,
            source = source,
            summaryForUser = summary,
            context = context
        )

        val outcome = container.taskRunner.runPlan(plan, context)
        _uiState.update { it.copy(isBusy = false) }

        when (val result = outcome.result) {
            is ActionExecutionResult.Executed -> {
                recordHistory(action, ActivityOutcome.SUCCESS)
                setFeedback(
                    status = TaskStatus.COMPLETED,
                    assistantState = AssistantUiState.Success(outcome.verification.userMessage),
                    headline = outcome.verification.userMessage,
                    important = false
                )
            }

            is ActionExecutionResult.NeedsConfirmation -> {
                val proposal = toContactProposal(result.action)
                _uiState.update {
                    it.copy(
                        pendingConfirmation = PendingConfirmation(
                            title = result.title,
                            previewMessage = result.previewMessage,
                            action = result.action,
                            source = source,
                            proposal = proposal
                        )
                    )
                }
                setFeedback(
                    status = TaskStatus.AWAITING_CONFIRMATION,
                    assistantState = proposal?.let { AssistantUiState.Confirmation(it) }
                        ?: AssistantUiState.Idle,
                    headline = result.previewMessage,
                    important = true
                )
            }

            is ActionExecutionResult.NeedsContactSelection -> {
                _uiState.update {
                    it.copy(
                        pendingContactChoice = PendingContactChoice(
                            query = result.query,
                            pendingNewName = result.pendingNewName,
                            candidates = result.candidates
                        )
                    )
                }
                setFeedback(
                    status = TaskStatus.AWAITING_CLARIFICATION,
                    assistantState = AssistantUiState.Idle,
                    headline = "I found ${result.candidates.size} contacts matching ${result.query}. Please choose one.",
                    important = true
                )
            }

            is ActionExecutionResult.ClarificationNeeded -> {
                _uiState.update {
                    it.copy(
                        pendingClarification = PendingClarification(
                            question = result.question,
                            options = result.options
                        )
                    )
                }
                setFeedback(
                    status = TaskStatus.AWAITING_CLARIFICATION,
                    assistantState = AssistantUiState.Idle,
                    headline = result.question,
                    important = true
                )
            }

            is ActionExecutionResult.Failed -> {
                val recovery = when (action) {
                    AssistantAction.GoHome,
                    AssistantAction.GoBack,
                    AssistantAction.OpenRecents ->
                        if (!_uiState.value.isAccessibilityEnabled) {
                            Recovery.OPEN_ACCESSIBILITY_SETTINGS
                        } else {
                            Recovery.NONE
                        }
                    else -> Recovery.NONE
                }
                recordHistory(action, ActivityOutcome.BLOCKED)
                setFeedback(
                    status = TaskStatus.FAILED,
                    assistantState = AssistantUiState.Failure(outcome.verification.userMessage, recovery),
                    headline = outcome.verification.userMessage,
                    important = true
                )
            }
        }
    }

    private fun startListeningInternal(testOnly: Boolean = false) {
        activeTaskJob?.cancel()
        activeTaskCancelled = false

        _uiState.update {
            it.copy(
                status = TaskStatus.LISTENING,
                assistantState = AssistantUiState.Listening(""),
                statusHeadline = if (testOnly) {
                    "Testing microphone... Say a short phrase."
                } else {
                    "I am listening. Go ahead and speak..."
                },
                statusDetail = null,
                micTestResult = null,
                pendingClarification = null,
                pendingConfirmation = null,
                pendingContactChoice = null
            )
        }

        activeTaskJob = viewModelScope.launch {
            val lang = _uiState.value.preferences.speechLanguageTag
            val result = container.voiceSessionManager.listenOnce(lang)
            result.onSuccess { spoken ->
                if (testOnly) {
                    isMicTestMode = false
                    viewModelScope.launch {
                        container.historyRepository.record(
                            actionCode = ActionId.TEST_MIC.code,
                            outcome = ActivityOutcome.SUCCESS,
                            userSummary = "Tested microphone successfully"
                        )
                    }
                    _uiState.update {
                        it.copy(
                            micTestResult = "Heard clearly: \"$spoken\"",
                            transcript = spoken
                        )
                    }
                    setFeedback(
                        status = TaskStatus.COMPLETED,
                        assistantState = AssistantUiState.Success("Microphone working! Heard: \"$spoken\""),
                        headline = "Microphone working! Heard: \"$spoken\"",
                        important = false
                    )
                } else {
                    submitCommand(spoken)
                }
            }.onFailure { err ->
                isMicTestMode = false
                if (!activeTaskCancelled) {
                    val msg = err.message ?: "I couldn't hear that. Please tap the microphone and try again."
                    _uiState.update { it.copy(micTestResult = msg) }
                    setFeedback(
                        status = TaskStatus.FAILED,
                        assistantState = AssistantUiState.Failure(msg, Recovery.RETRY_VOICE),
                        headline = msg,
                        important = true
                    )
                }
            }
        }
    }

    private fun toContactProposal(action: AssistantAction): ContactProposal? = when (action) {
        is AssistantAction.CreateContact -> ContactProposal.Create(
            displayName = action.displayName,
            phone = action.phoneNumber
        )
        is AssistantAction.RenameContact -> ContactProposal.Rename(
            contactId = action.contactId,
            lookupKey = action.lookupKey,
            oldName = action.currentDisplayName,
            newName = action.newDisplayName
        )
        else -> null
    }

    private fun recordHistory(action: AssistantAction, outcome: ActivityOutcome) {
        viewModelScope.launch {
            container.historyRepository.recordActionOutcome(action, outcome)
        }
    }

    private fun buildExecutionContext(confirmationGranted: Boolean): ExecutionContext {
        val s = _uiState.value
        return ExecutionContext(
            taskId = UUID.randomUUID().toString(),
            isTaskActive = !activeTaskCancelled,
            isAccessibilityEnabled = s.isAccessibilityEnabled,
            hasReadContactsPermission = s.hasContactsPermission,
            hasRecordAudioPermission = s.hasMicPermission,
            mode = if (s.preferences.masterAiEnabled && s.preferences.experimentalAiNavigationEnabled) {
                AssistantMode.AI_EXPERIMENTAL
            } else {
                AssistantMode.DETERMINISTIC
            },
            confirmationGranted = confirmationGranted
        )
    }

    private fun setFeedback(
        status: TaskStatus,
        assistantState: AssistantUiState,
        headline: String,
        detail: String? = null,
        important: Boolean
    ) {
        _uiState.update {
            it.copy(
                status = status,
                assistantState = assistantState,
                statusHeadline = headline,
                statusDetail = detail
            )
        }
        val prefs = _uiState.value.preferences
        val shouldSpeak = prefs.ttsEnabled &&
            (prefs.feedbackLevel == FeedbackLevel.EVERY_ACTION || important)
        if (shouldSpeak) {
            container.voiceSessionManager.speakFeedback(
                message = headline,
                ttsEnabled = true,
                speechRate = prefs.speechSpeed.rate,
                languageTag = prefs.speechLanguageTag
            )
        }
    }

    override fun onCleared() {
        container.voiceSessionManager.release()
        super.onCleared()
    }
}
