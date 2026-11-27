package com.elderassist.app.feature.home

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elderassist.app.ElderAssistApplication
import com.elderassist.app.accessibility.AccessibilityServiceState
import com.elderassist.app.core.model.ActionExecutionResult
import com.elderassist.app.core.model.ActionSource
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.AssistantMode
import com.elderassist.app.core.model.ClarificationOption
import com.elderassist.app.core.model.ContactCandidate
import com.elderassist.app.core.model.ExecutionContext
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.core.model.SupportedApp
import com.elderassist.app.core.model.TaskStatus
import com.elderassist.app.feature.voice.SpeechState
import com.elderassist.app.intelligence.IntentInterpretationResult
import com.elderassist.app.intelligence.NavigationAgentOutcome
import com.elderassist.app.intelligence.RoutingResult
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
    val source: ActionSource
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
    val status: TaskStatus = TaskStatus.IDLE,
    val statusHeadline: String = "Tap the microphone and tell me what you want to do.",
    val statusDetail: String? = null,
    val transcript: String = "",
    val manualInput: String = "",
    val speechState: SpeechState = SpeechState.Idle,
    val isListening: Boolean = false,
    val isBusy: Boolean = false,
    val hasMicPermission: Boolean = false,
    val hasContactsPermission: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isSpeechRecognitionAvailable: Boolean = true,
    val isGeminiConfigured: Boolean = false,
    val speechProviderName: String = "Android SpeechRecognizer",
    val preferences: UserPreferences = UserPreferences(),
    val pendingConfirmation: PendingConfirmation? = null,
    val pendingContactChoice: PendingContactChoice? = null,
    val pendingClarification: PendingClarification? = null,
    val showSetupHelp: Boolean = false,
    val showSettingsShortcuts: Boolean = false,
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

    init {
        refreshEnvironmentState()

        viewModelScope.launch {
            container.appSettings.preferences.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }

        viewModelScope.launch {
            container.voiceSessionManager.speechState.collect { speechState ->
                _uiState.update { state ->
                    val listening = speechState is SpeechState.Listening || speechState is SpeechState.Processing
                    val partial = (speechState as? SpeechState.Listening)?.partialTranscript
                    state.copy(
                        speechState = speechState,
                        isListening = listening,
                        transcript = if (!partial.isNullOrBlank()) partial else state.transcript
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
        val ctx = getApplication<Application>()
        val hasMic = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        val hasContacts = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
        val accessibilityEnabled = AccessibilityServiceState.isAvailable(ctx)

        _uiState.update {
            it.copy(
                hasMicPermission = hasMic,
                hasContactsPermission = hasContacts,
                isAccessibilityEnabled = accessibilityEnabled,
                isSpeechRecognitionAvailable = container.voiceSessionManager.isSpeechAvailable(),
                isGeminiConfigured = container.geminiClient.isConfigured(),
                speechProviderName = container.voiceSessionManager.providerName
            )
        }
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

        if (!_uiState.value.hasMicPermission) {
            startListeningAfterMicPermission = true
            _uiState.update {
                it.copy(
                    requestMicPermissionPrompt = true,
                    status = TaskStatus.IDLE,
                    statusHeadline = "Please allow Microphone access so I can hear your request."
                )
            }
            return
        }

        startListeningInternal()
    }

    fun onMicPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(requestMicPermissionPrompt = false) }
        refreshEnvironmentState()
        val shouldStart = startListeningAfterMicPermission
        startListeningAfterMicPermission = false

        if (granted && shouldStart) {
            startListeningInternal()
        } else if (!granted) {
            setFeedback(
                status = TaskStatus.FAILED,
                headline = "Microphone permission was denied. You can still type your request below.",
                speak = true
            )
        }
    }

    fun onContactsPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(requestContactsPermissionPrompt = false) }
        refreshEnvironmentState()
        val pending = pendingCommandAfterPermission
        pendingCommandAfterPermission = null

        if (granted && !pending.isNullOrBlank()) {
            submitCommand(pending)
        } else if (!granted) {
            setFeedback(
                status = TaskStatus.FAILED,
                headline = "Contacts permission is needed to search contacts by name. Opening Contacts instead.",
                speak = true
            )
            executeDirectAction(
                action = AssistantAction.OpenApp(SupportedApp.CONTACTS),
                source = ActionSource.DETERMINISTIC,
                summary = "Opening Contacts."
            )
        }
    }

    fun submitManualCommand() {
        val text = _uiState.value.manualInput.trim()
        if (text.isEmpty()) return
        _uiState.update { it.copy(manualInput = "") }
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
            setFeedback(
                status = TaskStatus.COMPLETED,
                headline = "Selected ${candidate.displayName}$phoneDetail.",
                detail = "You can open Contacts to view or edit their full details.",
                speak = true
            )
        }
    }

    fun dismissContactSelection() {
        _uiState.update { it.copy(pendingContactChoice = null) }
        setFeedback(TaskStatus.CANCELLED, "Cancelled contact selection.", speak = false)
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
        _uiState.update { it.copy(pendingConfirmation = null) }
        setFeedback(
            status = TaskStatus.CANCELLED,
            headline = "Cancelled. No changes were saved.",
            speak = true
        )
    }

    fun cancelActiveTask() {
        activeTaskCancelled = true
        activeTaskJob?.cancel()
        activeTaskJob = null
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
            headline = "Stopped.",
            speak = true
        )
    }

    fun openQuickSetting(destination: SettingDestination) {
        _uiState.update { it.copy(showSettingsShortcuts = false) }
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

    fun setShowSettingsShortcuts(show: Boolean) {
        _uiState.update { it.copy(showSettingsShortcuts = show) }
    }

    fun setShowSetupHelp(show: Boolean) {
        refreshEnvironmentState()
        _uiState.update { it.copy(showSetupHelp = show) }
    }

    fun openAccessibilitySettings() {
        container.intentExecutor.openAccessibilitySettings()
    }

    fun requestMicPermission() {
        startListeningAfterMicPermission = false
        _uiState.update { it.copy(requestMicPermissionPrompt = true) }
    }

    fun requestContactsPermission() {
        _uiState.update { it.copy(requestContactsPermissionPrompt = true) }
    }

    fun toggleTts(enabled: Boolean) {
        viewModelScope.launch {
            container.appSettings.setTtsEnabled(enabled)
            if (!enabled) container.voiceSessionManager.stopSpeaking()
        }
    }

    fun toggleExperimentalAiNavigation(enabled: Boolean) {
        viewModelScope.launch {
            container.appSettings.setExperimentalAiNavigationEnabled(enabled)
        }
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
                status = TaskStatus.INTERPRETING,
                statusHeadline = "Understanding your request...",
                statusDetail = null,
                pendingClarification = null,
                pendingConfirmation = null,
                pendingContactChoice = null
            )
        }

        activeTaskJob = viewModelScope.launch {
            val mode = if (_uiState.value.preferences.experimentalAiNavigationEnabled) {
                AssistantMode.AI_EXPERIMENTAL
            } else {
                AssistantMode.DETERMINISTIC
            }

            when (val routing = container.intentRouter.route(text, mode)) {
                RoutingResult.Empty -> {
                    _uiState.update { it.copy(isBusy = false) }
                    setFeedback(
                        status = TaskStatus.FAILED,
                        headline = "Please tell me what you would like to do.",
                        speak = true
                    )
                }

                RoutingResult.Cancelled -> {
                    _uiState.update { it.copy(isBusy = false) }
                    setFeedback(TaskStatus.CANCELLED, "Cancelled.", speak = true)
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
                        headline = routing.question,
                        speak = true
                    )
                }

                is RoutingResult.Unsupported -> {
                    _uiState.update { it.copy(isBusy = false) }
                    setFeedback(
                        status = TaskStatus.FAILED,
                        headline = routing.message,
                        speak = true
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
                    handleAiFallback(
                        originalTranscript = text,
                        allowExperimentalNavigation = routing.allowExperimentalNavigation
                    )
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
                    val service = com.elderassist.app.accessibility.AccessibilityServiceState.activeService()
                    if (service != null) {
                        _uiState.update {
                            it.copy(
                                status = TaskStatus.NAVIGATING_AI,
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
                            is NavigationAgentOutcome.Finished -> setFeedback(
                                status = TaskStatus.COMPLETED,
                                headline = navOutcome.message,
                                speak = true
                            )
                            is NavigationAgentOutcome.NeedsClarification -> setFeedback(
                                status = TaskStatus.AWAITING_CLARIFICATION,
                                headline = navOutcome.question,
                                speak = true
                            )
                            is NavigationAgentOutcome.StoppedSafely -> setFeedback(
                                status = TaskStatus.FAILED,
                                headline = navOutcome.message,
                                speak = true
                            )
                        }
                        return
                    }
                }

                _uiState.update { it.copy(isBusy = false) }
                setFeedback(
                    status = TaskStatus.FAILED,
                    headline = aiResult.userMessage,
                    detail = "Try commands like 'Open Bluetooth settings', 'Go home', or 'Find Rahul'.",
                    speak = true
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
        if ((action is AssistantAction.SearchContact || action is AssistantAction.RenameContact) &&
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
                headline = "Please allow Contacts access so I can find that contact for you.",
                speak = true
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
                setFeedback(
                    status = TaskStatus.COMPLETED,
                    headline = outcome.verification.userMessage,
                    speak = true
                )
            }

            is ActionExecutionResult.NeedsConfirmation -> {
                _uiState.update {
                    it.copy(
                        pendingConfirmation = PendingConfirmation(
                            title = result.title,
                            previewMessage = result.previewMessage,
                            action = result.action,
                            source = source
                        )
                    )
                }
                setFeedback(
                    status = TaskStatus.AWAITING_CONFIRMATION,
                    headline = result.previewMessage,
                    speak = true
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
                    headline = "I found ${result.candidates.size} contacts matching ${result.query}. Please choose one.",
                    speak = true
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
                    headline = result.question,
                    speak = true
                )
            }

            is ActionExecutionResult.Failed -> {
                setFeedback(
                    status = TaskStatus.FAILED,
                    headline = outcome.verification.userMessage,
                    speak = true
                )
            }
        }
    }

    private fun startListeningInternal() {
        activeTaskJob?.cancel()
        activeTaskCancelled = false

        _uiState.update {
            it.copy(
                status = TaskStatus.LISTENING,
                statusHeadline = "Listening... Speak clearly, then pause.",
                statusDetail = null,
                pendingClarification = null,
                pendingConfirmation = null,
                pendingContactChoice = null
            )
        }

        activeTaskJob = viewModelScope.launch {
            val lang = _uiState.value.preferences.speechLanguageTag
            val result = container.voiceSessionManager.listenOnce(lang)
            result.onSuccess { spoken ->
                submitCommand(spoken)
            }.onFailure { err ->
                if (!activeTaskCancelled) {
                    val msg = err.message ?: "I couldn't hear that. Please tap the microphone and try again."
                    setFeedback(
                        status = TaskStatus.FAILED,
                        headline = msg,
                        speak = false
                    )
                }
            }
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
            mode = if (s.preferences.experimentalAiNavigationEnabled) {
                AssistantMode.AI_EXPERIMENTAL
            } else {
                AssistantMode.DETERMINISTIC
            },
            confirmationGranted = confirmationGranted
        )
    }

    private fun setFeedback(
        status: TaskStatus,
        headline: String,
        detail: String? = null,
        speak: Boolean
    ) {
        _uiState.update {
            it.copy(
                status = status,
                statusHeadline = headline,
                statusDetail = detail
            )
        }
        if (speak) {
            container.voiceSessionManager.speakFeedback(
                message = headline,
                ttsEnabled = _uiState.value.preferences.ttsEnabled
            )
        }
    }

    override fun onCleared() {
        container.voiceSessionManager.release()
        super.onCleared()
    }
}
