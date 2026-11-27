package com.elderassist.app.feature.contacts

import com.elderassist.app.core.model.ActionExecutionResult
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.policy.ActionPolicy
import com.elderassist.app.execution.IntentExecutor

class ContactsActionHandler(
    private val contactsRepository: ContactsRepository,
    private val intentExecutor: IntentExecutor
) {

    fun openContacts(): ActionExecutionResult {
        val outcome = intentExecutor.openContactsApp()
        return if (outcome.launched) {
            ActionExecutionResult.Executed(
                message = "I've opened your Contacts app.",
                openedPackage = outcome.resolvedPackage
            )
        } else {
            ActionExecutionResult.Failed(
                userMessage = outcome.errorMessage ?: "I couldn't open Contacts on this phone."
            )
        }
    }

    fun searchContact(query: String): ActionExecutionResult {
        if (!contactsRepository.hasReadPermission()) {
            return ActionExecutionResult.Failed(
                userMessage = "Contacts permission is needed to search for $query. You can grant it in Setup & Help, or open Contacts directly."
            )
        }

        val matches = contactsRepository.searchContacts(query)
        return when {
            matches.isEmpty() -> ActionExecutionResult.Failed(
                userMessage = "I couldn't find any contact matching $query."
            )
            matches.size == 1 -> {
                val match = matches.first()
                val phoneDetail = match.phoneHint?.let { " (phone $it)" }.orEmpty()
                ActionExecutionResult.Executed(
                    message = "Found ${match.displayName}$phoneDetail."
                )
            }
            else -> ActionExecutionResult.NeedsContactSelection(
                query = query,
                candidates = matches
            )
        }
    }

    fun createContact(action: AssistantAction.CreateContact): ActionExecutionResult {
        val outcome = intentExecutor.launchInsertContact(
            displayName = action.displayName,
            phoneNumber = action.phoneNumber
        )
        return if (outcome.launched) {
            val masked = ActionPolicy.maskPhoneLast4(action.phoneNumber)
            ActionExecutionResult.Executed(
                message = "I've opened the new contact screen for ${action.displayName} ($masked). Tap Save in Contacts to finish.",
                openedPackage = outcome.resolvedPackage
            )
        } else {
            ActionExecutionResult.Failed(
                userMessage = outcome.errorMessage ?: "Could not open the new contact screen."
            )
        }
    }

    fun prepareOrExecuteRename(
        action: AssistantAction.RenameContact,
        confirmationGranted: Boolean
    ): ActionExecutionResult {
        // Resolve the contact first if contactId wasn't already selected.
        if (action.contactId <= 0L || action.lookupKey.isBlank()) {
            if (!contactsRepository.hasReadPermission()) {
                return ActionExecutionResult.Failed(
                    userMessage = "Contacts permission is needed to find ${action.currentDisplayName} before renaming."
                )
            }
            val matches = contactsRepository.searchContacts(action.currentDisplayName)
            return when {
                matches.isEmpty() -> ActionExecutionResult.Failed(
                    userMessage = "I couldn't find a contact named ${action.currentDisplayName}."
                )
                matches.size > 1 -> ActionExecutionResult.NeedsContactSelection(
                    query = action.currentDisplayName,
                    pendingNewName = action.newDisplayName,
                    candidates = matches
                )
                else -> {
                    val exact = matches.first()
                    val resolvedAction = action.copy(
                        contactId = exact.contactId,
                        lookupKey = exact.lookupKey,
                        currentDisplayName = exact.displayName
                    )
                    if (!confirmationGranted) {
                        ActionExecutionResult.NeedsConfirmation(
                            title = "Confirm contact rename",
                            previewMessage = "Rename ${exact.displayName} to ${action.newDisplayName}?",
                            action = resolvedAction
                        )
                    } else {
                        launchRenameEditor(resolvedAction)
                    }
                }
            }
        }

        if (!confirmationGranted) {
            return ActionExecutionResult.NeedsConfirmation(
                title = "Confirm contact rename",
                previewMessage = "Rename ${action.currentDisplayName} to ${action.newDisplayName}?",
                action = action
            )
        }

        return launchRenameEditor(action)
    }

    private fun launchRenameEditor(action: AssistantAction.RenameContact): ActionExecutionResult {
        val outcome = intentExecutor.launchEditContact(
            contactId = action.contactId,
            lookupKey = action.lookupKey,
            newDisplayName = action.newDisplayName
        )
        return if (outcome.launched) {
            ActionExecutionResult.Executed(
                message = "I've opened the contact editor to rename ${action.currentDisplayName} to ${action.newDisplayName}. Tap Save to finish.",
                openedPackage = outcome.resolvedPackage
            )
        } else {
            ActionExecutionResult.Failed(
                userMessage = outcome.errorMessage ?: "Could not open the contact editor."
            )
        }
    }
}
