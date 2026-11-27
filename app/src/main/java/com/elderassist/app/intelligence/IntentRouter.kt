package com.elderassist.app.intelligence

import com.elderassist.app.core.model.ActionSource
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.AssistantMode
import com.elderassist.app.core.model.ClarificationOption
import com.elderassist.app.core.model.SettingDestination

sealed interface RoutingResult {
    data object Empty : RoutingResult
    data object Cancelled : RoutingResult

    data class Deterministic(
        val action: AssistantAction,
        val summaryForUser: String,
        val source: ActionSource = ActionSource.DETERMINISTIC
    ) : RoutingResult

    data class ClarificationRequired(
        val question: String,
        val options: List<ClarificationOption>
    ) : RoutingResult

    data class Unsupported(
        val message: String,
        val fallbackDestination: SettingDestination? = null
    ) : RoutingResult

    data class NeedsAiInterpretation(
        val normalizedTranscript: String,
        val allowExperimentalNavigation: Boolean
    ) : RoutingResult
}

interface IntentRouter {
    suspend fun route(transcript: String, mode: AssistantMode): RoutingResult
}

class DefaultIntentRouter(
    private val commandParser: CommandParser = CommandParser()
) : IntentRouter {

    override suspend fun route(transcript: String, mode: AssistantMode): RoutingResult {
        return when (val outcome = commandParser.parse(transcript)) {
            ParseOutcome.Empty -> RoutingResult.Empty

            is ParseOutcome.Matched -> {
                if (outcome.action is AssistantAction.Cancel) {
                    RoutingResult.Cancelled
                } else {
                    RoutingResult.Deterministic(
                        action = outcome.action,
                        summaryForUser = outcome.summaryForUser
                    )
                }
            }

            is ParseOutcome.Ambiguous -> RoutingResult.ClarificationRequired(
                question = outcome.question,
                options = outcome.options
            )

            is ParseOutcome.ProhibitedOrUnsupported -> RoutingResult.Unsupported(
                message = outcome.message,
                fallbackDestination = outcome.fallbackDestination
            )

            is ParseOutcome.Unmatched -> RoutingResult.NeedsAiInterpretation(
                normalizedTranscript = outcome.normalizedTranscript,
                allowExperimentalNavigation = mode == AssistantMode.AI_EXPERIMENTAL
            )
        }
    }
}
