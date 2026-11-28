package com.elderassist.app.core.model

data class ActionPlan(
    val taskId: String,
    val createdAtEpochMs: Long,
    val action: AssistantAction,
    val requiresConfirmation: Boolean,
    val riskLevel: RiskLevel,
    val source: ActionSource,
    val expectedPackage: String? = null,
    val maxSteps: Int = 5,
    val summaryForUser: String
)

enum class TaskStatus {
    IDLE,
    LISTENING,
    INTERPRETING,
    AWAITING_CLARIFICATION,
    AWAITING_CONFIRMATION,
    EXECUTING,
    NAVIGATING_AI,
    COMPLETED,
    CANCELLED,
    FAILED
}

data class ExecutionContext(
    val taskId: String,
    val isTaskActive: Boolean,
    val isAccessibilityEnabled: Boolean,
    val hasReadContactsPermission: Boolean,
    val hasRecordAudioPermission: Boolean,
    val mode: AssistantMode,
    val currentSnapshot: ScreenSnapshot? = null,
    val confirmationGranted: Boolean = false,
    val currentStep: Int = 0
)

sealed interface ActionExecutionResult {
    data class Executed(
        val message: String,
        val openedPackage: String? = null,
        val usedFallback: Boolean = false
    ) : ActionExecutionResult

    data class NeedsContactSelection(
        val query: String,
        val pendingNewName: String? = null,
        val candidates: List<ContactCandidate>,
        val pendingAction: AssistantAction? = null
    ) : ActionExecutionResult

    data class NeedsConfirmation(
        val title: String,
        val previewMessage: String,
        val action: AssistantAction
    ) : ActionExecutionResult

    data class ClarificationNeeded(
        val question: String,
        val options: List<ClarificationOption>
    ) : ActionExecutionResult

    data class Failed(
        val userMessage: String,
        val reason: String? = null
    ) : ActionExecutionResult
}

data class ContactCandidate(
    val contactId: Long,
    val lookupKey: String,
    val displayName: String,
    val phoneHint: String? = null,
    val rawPhoneNumber: String? = null
)

data class VerificationResult(
    val verified: Boolean,
    val userMessage: String
)
