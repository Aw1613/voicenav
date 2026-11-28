package com.elderassist.app.execution

import com.elderassist.app.core.model.ActionPlan
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.ExecutionContext
import com.elderassist.app.core.model.RiskLevel
import com.elderassist.app.core.policy.ActionPolicy

sealed interface ValidationResult {
    data class Valid(val plan: ActionPlan) : ValidationResult
    data class RequiresConfirmation(
        val plan: ActionPlan,
        val title: String,
        val previewMessage: String
    ) : ValidationResult

    data class Rejected(
        val userMessage: String,
        val reason: String
    ) : ValidationResult
}

class ActionValidator(
    private val nowMsProvider: () -> Long = { System.currentTimeMillis() }
) {

    fun validate(plan: ActionPlan, context: ExecutionContext): ValidationResult {
        if (!context.isTaskActive) {
            return ValidationResult.Rejected(
                userMessage = "Task was cancelled.",
                reason = "Task inactive"
            )
        }

        if (plan.riskLevel == RiskLevel.PROHIBITED) {
            return ValidationResult.Rejected(
                userMessage = "That action is not allowed in ElderAssist for your safety.",
                reason = "Prohibited risk level"
            )
        }

        return when (val action = plan.action) {
            is AssistantAction.OpenSetting -> {
                if (action.destination !in ActionPolicy.allowedSettings) {
                    ValidationResult.Rejected(
                        userMessage = "That settings screen is not supported.",
                        reason = "Destination not in allowlist"
                    )
                } else {
                    ValidationResult.Valid(plan)
                }
            }

            is AssistantAction.OpenApp,
            is AssistantAction.OpenNamedApp,
            is AssistantAction.PlaceCall,
            is AssistantAction.PlaceWhatsAppCall -> ValidationResult.Valid(plan)

            AssistantAction.GoHome,
            AssistantAction.GoBack,
            AssistantAction.OpenRecents -> {
                if (!context.isAccessibilityEnabled) {
                    ValidationResult.Rejected(
                        userMessage = "Please enable ElderAssist accessibility access in Android Settings to use Home, Back, or Recents.",
                        reason = "AccessibilityService disabled"
                    )
                } else {
                    ValidationResult.Valid(plan)
                }
            }

            AssistantAction.Cancel,
            is AssistantAction.AskClarification,
            is AssistantAction.Explain,
            is AssistantAction.Unsupported -> ValidationResult.Valid(plan)

            is AssistantAction.SearchContact -> {
                if (action.query.isBlank()) {
                    ValidationResult.Rejected(
                        userMessage = "Please tell me which contact name to search for.",
                        reason = "Blank contact query"
                    )
                } else {
                    ValidationResult.Valid(plan)
                }
            }

            is AssistantAction.CreateContact -> {
                if (action.displayName.isBlank() || action.phoneNumber.filter { it.isDigit() }.length < 3) {
                    ValidationResult.Rejected(
                        userMessage = "Please provide both a contact name and a valid phone number.",
                        reason = "Missing contact fields"
                    )
                } else if (!context.confirmationGranted) {
                    val masked = ActionPolicy.maskPhoneLast4(action.phoneNumber)
                    ValidationResult.RequiresConfirmation(
                        plan = plan,
                        title = "Confirm new contact",
                        previewMessage = "Save ${action.displayName} with phone number $masked?"
                    )
                } else {
                    ValidationResult.Valid(plan)
                }
            }

            is AssistantAction.RenameContact -> {
                if (action.currentDisplayName.isBlank() || action.newDisplayName.isBlank()) {
                    ValidationResult.Rejected(
                        userMessage = "Please provide both the current name and the new contact name.",
                        reason = "Missing rename fields"
                    )
                } else if (!context.confirmationGranted) {
                    ValidationResult.RequiresConfirmation(
                        plan = plan,
                        title = "Confirm contact rename",
                        previewMessage = "Rename ${action.currentDisplayName} to ${action.newDisplayName}?"
                    )
                } else {
                    ValidationResult.Valid(plan)
                }
            }

            is AssistantAction.ClickNode -> validateNodeAction(
                plan = plan,
                context = context,
                snapshotId = action.snapshotId,
                nodeId = action.nodeId,
                requireClickable = true,
                requireScrollable = false,
                requireEditable = false
            )

            is AssistantAction.Scroll -> validateNodeAction(
                plan = plan,
                context = context,
                snapshotId = action.snapshotId,
                nodeId = action.nodeId,
                requireClickable = false,
                requireScrollable = true,
                requireEditable = false
            )

            is AssistantAction.SetNodeText -> {
                if (action.text.isBlank()) {
                    ValidationResult.Rejected(
                        userMessage = "Cannot enter empty text.",
                        reason = "Empty text"
                    )
                } else {
                    validateNodeAction(
                        plan = plan,
                        context = context,
                        snapshotId = action.snapshotId,
                        nodeId = action.nodeId,
                        requireClickable = false,
                        requireScrollable = false,
                        requireEditable = true
                    )
                }
            }
        }
    }

    private fun validateNodeAction(
        plan: ActionPlan,
        context: ExecutionContext,
        snapshotId: String,
        nodeId: String?,
        requireClickable: Boolean,
        requireScrollable: Boolean,
        requireEditable: Boolean
    ): ValidationResult {
        if (!context.isAccessibilityEnabled) {
            return ValidationResult.Rejected(
                userMessage = "Please enable ElderAssist accessibility access in Android Settings.",
                reason = "AccessibilityService disabled"
            )
        }
        if (context.currentStep >= plan.maxSteps) {
            return ValidationResult.Rejected(
                userMessage = "I reached the maximum number of automatic steps and stopped safely.",
                reason = "Max steps reached"
            )
        }
        val snapshot = context.currentSnapshot
            ?: return ValidationResult.Rejected(
                userMessage = "No active screen snapshot is available, so I stopped.",
                reason = "Missing snapshot"
            )

        if (snapshot.snapshotId != snapshotId) {
            return ValidationResult.Rejected(
                userMessage = "The screen changed since the last step, so I stopped safely.",
                reason = "Stale snapshot ID"
            )
        }

        if (snapshot.timestampMs > 0L &&
            (nowMsProvider() - snapshot.timestampMs) > ActionPolicy.MAX_SNAPSHOT_AGE_MS
        ) {
            return ValidationResult.Rejected(
                userMessage = "The screen view expired, so I stopped safely.",
                reason = "Snapshot expired"
            )
        }

        if (ActionPolicy.isPackageExcluded(snapshot.packageName)) {
            return ValidationResult.Rejected(
                userMessage = "Stopped because a protected or sensitive screen is active.",
                reason = "Excluded package: ${snapshot.packageName}"
            )
        }

        if (nodeId == null) {
            // Only Scroll is allowed with null nodeId (scrolls first visible scrollable container).
            return if (requireScrollable) {
                ValidationResult.Valid(plan)
            } else {
                ValidationResult.Rejected(
                    userMessage = "No target element was selected.",
                    reason = "Null nodeId"
                )
            }
        }

        val node = snapshot.nodes.firstOrNull { it.nodeId == nodeId }
            ?: return ValidationResult.Rejected(
                userMessage = "That option is no longer on the screen, so I stopped.",
                reason = "Node missing from snapshot"
            )

        if (node.isSensitive) {
            return ValidationResult.Rejected(
                userMessage = "I cannot interact with sensitive or password fields.",
                reason = "Sensitive node"
            )
        }

        if (!node.visible || !node.enabled) {
            return ValidationResult.Rejected(
                userMessage = "That option is disabled or not visible right now.",
                reason = "Node not visible/enabled"
            )
        }

        if (requireClickable && !node.clickable) {
            // Note: Android text rows inside clickable parents may sometimes report clickable=false,
            // so we allow click only if the node itself is clickable OR has a text label in a Settings row;
            // stricter check: require clickable or non-blank text label in a non-sensitive package.
            if (node.text.isNullOrBlank() && node.contentDescription.isNullOrBlank()) {
                return ValidationResult.Rejected(
                    userMessage = "That element cannot be tapped safely.",
                    reason = "Node not clickable and unlabeled"
                )
            }
        }

        if (requireScrollable && !node.scrollable) {
            return ValidationResult.Rejected(
                userMessage = "That area cannot be scrolled.",
                reason = "Node not scrollable"
            )
        }

        if (requireEditable && !node.editable) {
            return ValidationResult.Rejected(
                userMessage = "That element is not an editable text box.",
                reason = "Node not editable"
            )
        }

        return ValidationResult.Valid(plan)
    }
}
