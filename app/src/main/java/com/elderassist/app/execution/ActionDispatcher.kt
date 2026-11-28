package com.elderassist.app.execution

import android.accessibilityservice.AccessibilityService
import com.elderassist.app.accessibility.AccessibilityExecutor
import com.elderassist.app.core.model.ActionExecutionResult
import com.elderassist.app.core.model.ActionPlan
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.ExecutionContext
import com.elderassist.app.core.model.SupportedApp
import com.elderassist.app.feature.contacts.ContactsActionHandler
import com.elderassist.app.feature.settings.SettingsActionHandler

class ActionDispatcher(
    private val validator: ActionValidator,
    private val settingsHandler: SettingsActionHandler,
    private val contactsHandler: ContactsActionHandler,
    private val accessibilityExecutor: AccessibilityExecutor,
    private val intentExecutor: IntentExecutor? = null
) {

    fun dispatch(plan: ActionPlan, context: ExecutionContext): ActionExecutionResult {
        return when (val validation = validator.validate(plan, context)) {
            is ValidationResult.Rejected -> ActionExecutionResult.Failed(
                userMessage = validation.userMessage,
                reason = validation.reason
            )

            is ValidationResult.RequiresConfirmation -> {
                // For RenameContact, resolve unique candidate first if needed so preview shows exact person.
                val action = plan.action
                if (action is AssistantAction.RenameContact) {
                    contactsHandler.prepareOrExecuteRename(action, confirmationGranted = false)
                } else {
                    ActionExecutionResult.NeedsConfirmation(
                        title = validation.title,
                        previewMessage = validation.previewMessage,
                        action = action
                    )
                }
            }

            is ValidationResult.Valid -> executeValidated(plan.action, context)
        }
    }

    private fun executeValidated(
        action: AssistantAction,
        context: ExecutionContext
    ): ActionExecutionResult = when (action) {
        is AssistantAction.OpenSetting -> settingsHandler.open(action.destination)

        is AssistantAction.OpenApp -> when (action.app) {
            SupportedApp.CONTACTS -> contactsHandler.openContacts()
            SupportedApp.SETTINGS -> settingsHandler.open(com.elderassist.app.core.model.SettingDestination.GENERAL_SETTINGS)
        }

        is AssistantAction.OpenNamedApp -> {
            val outcome = intentExecutor?.openAppByName(action.appName)
            if (outcome?.launched == true) {
                ActionExecutionResult.Executed(
                    message = "I've opened ${action.appName}.",
                    openedPackage = outcome.resolvedPackage
                )
            } else {
                ActionExecutionResult.Failed(
                    userMessage = outcome?.errorMessage ?: "Could not open ${action.appName}."
                )
            }
        }

        is AssistantAction.PlaceCall -> contactsHandler.callContact(action.recipient)

        AssistantAction.GoHome -> accessibilityExecutor.performGlobalNavigation(
            globalAction = AccessibilityService.GLOBAL_ACTION_HOME,
            successMessage = "Returned to your home screen."
        )

        AssistantAction.GoBack -> accessibilityExecutor.performGlobalNavigation(
            globalAction = AccessibilityService.GLOBAL_ACTION_BACK,
            successMessage = "Went back to the previous screen."
        )

        AssistantAction.OpenRecents -> accessibilityExecutor.performGlobalNavigation(
            globalAction = AccessibilityService.GLOBAL_ACTION_RECENTS,
            successMessage = "Opened your recent apps."
        )

        AssistantAction.Cancel -> ActionExecutionResult.Executed("Cancelled.")

        is AssistantAction.SearchContact -> contactsHandler.searchContact(action.query)

        is AssistantAction.CreateContact -> contactsHandler.createContact(action)

        is AssistantAction.RenameContact ->
            contactsHandler.prepareOrExecuteRename(action, confirmationGranted = context.confirmationGranted)

        is AssistantAction.ClickNode -> accessibilityExecutor.clickNode(
            snapshotId = action.snapshotId,
            nodeId = action.nodeId,
            hint = action.descriptionHint
        )

        is AssistantAction.Scroll -> accessibilityExecutor.scroll(
            snapshotId = action.snapshotId,
            nodeId = action.nodeId,
            direction = action.direction
        )

        is AssistantAction.SetNodeText -> accessibilityExecutor.setNodeText(
            snapshotId = action.snapshotId,
            nodeId = action.nodeId,
            text = action.text
        )

        is AssistantAction.AskClarification -> ActionExecutionResult.ClarificationNeeded(
            question = action.question,
            options = action.options
        )

        is AssistantAction.Explain -> ActionExecutionResult.Executed(action.message)

        is AssistantAction.Unsupported -> ActionExecutionResult.Failed(action.message)
    }
}
