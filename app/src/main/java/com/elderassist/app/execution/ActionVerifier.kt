package com.elderassist.app.execution

import com.elderassist.app.core.model.ActionExecutionResult
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.VerificationResult

class ActionVerifier {

    fun verify(
        action: AssistantAction,
        result: ActionExecutionResult
    ): VerificationResult {
        return when (result) {
            is ActionExecutionResult.Executed -> VerificationResult(
                verified = true,
                userMessage = result.message
            )

            is ActionExecutionResult.NeedsConfirmation -> VerificationResult(
                verified = false,
                userMessage = result.previewMessage
            )

            is ActionExecutionResult.NeedsContactSelection -> VerificationResult(
                verified = false,
                userMessage = "I found multiple contacts matching ${result.query}. Please choose one."
            )

            is ActionExecutionResult.ClarificationNeeded -> VerificationResult(
                verified = false,
                userMessage = result.question
            )

            is ActionExecutionResult.Failed -> VerificationResult(
                verified = false,
                userMessage = result.userMessage
            )
        }
    }
}
