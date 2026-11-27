package com.elderassist.app.intelligence

import com.elderassist.app.accessibility.ElderAssistAccessibilityService
import com.elderassist.app.core.model.ActionExecutionResult
import com.elderassist.app.core.model.ActionSource
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.ExecutionContext
import com.elderassist.app.core.model.ScreenSnapshot
import com.elderassist.app.core.model.ScrollDirection
import com.elderassist.app.core.policy.ActionPolicy
import com.elderassist.app.execution.TaskRunner
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface NavigationAgentOutcome {
    data class Finished(val message: String) : NavigationAgentOutcome
    data class NeedsClarification(val question: String) : NavigationAgentOutcome
    data class StoppedSafely(val message: String) : NavigationAgentOutcome
}

class GeneralNavigationAgent(
    private val geminiClient: GeminiClient,
    private val actionPlanner: ActionPlanner,
    private val taskRunner: TaskRunner
) {

    private val stepSchema: JsonObject = buildJsonObject {
        put("type", "OBJECT")
        put("properties", buildJsonObject {
            put("action", buildJsonObject {
                put("type", "STRING")
                put("enum", buildJsonArray {
                    add(JsonPrimitive("CLICK_NODE"))
                    add(JsonPrimitive("SCROLL"))
                    add(JsonPrimitive("SET_TEXT"))
                    add(JsonPrimitive("ASK_CLARIFICATION"))
                    add(JsonPrimitive("DONE"))
                    add(JsonPrimitive("STOP_UNCERTAIN"))
                })
            })
            put("snapshot_id", buildJsonObject { put("type", "STRING") })
            put("node_id", buildJsonObject {
                put("type", "STRING")
                put("nullable", true)
            })
            put("scroll_direction", buildJsonObject {
                put("type", "STRING")
                put("nullable", true)
                put("enum", buildJsonArray {
                    add(JsonPrimitive("UP"))
                    add(JsonPrimitive("DOWN"))
                    add(JsonPrimitive("LEFT"))
                    add(JsonPrimitive("RIGHT"))
                })
            })
            put("input_text", buildJsonObject {
                put("type", "STRING")
                put("nullable", true)
            })
            put("reason", buildJsonObject { put("type", "STRING") })
            put("confidence", buildJsonObject { put("type", "NUMBER") })
            put("requires_confirmation", buildJsonObject { put("type", "BOOLEAN") })
        })
        put("required", buildJsonArray {
            add(JsonPrimitive("action"))
            add(JsonPrimitive("snapshot_id"))
            add(JsonPrimitive("reason"))
            add(JsonPrimitive("confidence"))
            add(JsonPrimitive("requires_confirmation"))
        })
    }

    suspend fun runBoundedNavigation(
        userGoal: String,
        service: ElderAssistAccessibilityService,
        baseContext: ExecutionContext,
        isCancelled: () -> Boolean,
        onStepProgress: (String) -> Unit
    ): NavigationAgentOutcome {
        if (!geminiClient.isConfigured()) {
            return NavigationAgentOutcome.StoppedSafely(
                "AI navigation is unavailable because Gemini is not configured."
            )
        }

        service.isTaskActive = true
        try {
            for (stepIndex in 0 until ActionPolicy.MAX_AI_NAVIGATION_STEPS) {
                if (isCancelled()) {
                    return NavigationAgentOutcome.StoppedSafely("Navigation cancelled.")
                }

                val snapshot = service.captureFreshSnapshot()
                    ?: return NavigationAgentOutcome.StoppedSafely(
                        "I couldn't read this screen safely or it is a protected screen, so I stopped."
                    )

                if (ActionPolicy.isPackageExcluded(snapshot.packageName)) {
                    return NavigationAgentOutcome.StoppedSafely(
                        "Stopped because a protected or sensitive screen is open."
                    )
                }

                val prompt = buildNavigationPrompt(userGoal, snapshot, stepIndex + 1)
                val rawJson = geminiClient.generateStructuredJson(prompt, stepSchema)
                    .getOrElse {
                        return NavigationAgentOutcome.StoppedSafely(
                            "Could not reach the AI navigation helper, so I stopped."
                        )
                    }

                val dto = parseStepDto(rawJson)
                    ?: return NavigationAgentOutcome.StoppedSafely(
                        "The AI proposed an invalid action format, so I stopped safely."
                    )

                if (dto.snapshotId != snapshot.snapshotId) {
                    return NavigationAgentOutcome.StoppedSafely(
                        "Screen snapshot mismatch detected, so I stopped safely."
                    )
                }

                if (dto.confidence < ActionPolicy.MIN_AI_NAVIGATION_CONFIDENCE &&
                    dto.action !in setOf("ASK_CLARIFICATION", "STOP_UNCERTAIN", "DONE")
                ) {
                    return NavigationAgentOutcome.StoppedSafely(
                        "I'm not sure which button to tap next, so I stopped here."
                    )
                }

                when (dto.action) {
                    "DONE" -> return NavigationAgentOutcome.Finished(dto.reason)
                    "STOP_UNCERTAIN" -> return NavigationAgentOutcome.StoppedSafely(dto.reason)
                    "ASK_CLARIFICATION" -> return NavigationAgentOutcome.NeedsClarification(dto.reason)
                }

                val assistantAction = mapStepToAction(dto)
                    ?: return NavigationAgentOutcome.StoppedSafely(
                        "Unsupported navigation step '${dto.action}', so I stopped."
                    )

                val stepContext = baseContext.copy(
                    isTaskActive = !isCancelled(),
                    currentSnapshot = snapshot,
                    currentStep = stepIndex
                )
                val plan = actionPlanner.createPlan(
                    action = assistantAction,
                    source = ActionSource.GEMINI_NAVIGATION,
                    summaryForUser = dto.reason,
                    context = stepContext
                )

                onStepProgress("Step ${stepIndex + 1}/${ActionPolicy.MAX_AI_NAVIGATION_STEPS}: ${dto.reason}")
                val outcome = taskRunner.runPlan(plan, stepContext)
                when (val res = outcome.result) {
                    is ActionExecutionResult.Executed -> {
                        delay(750L)
                    }
                    is ActionExecutionResult.Failed -> {
                        return NavigationAgentOutcome.StoppedSafely(res.userMessage)
                    }
                    is ActionExecutionResult.ClarificationNeeded -> {
                        return NavigationAgentOutcome.NeedsClarification(res.question)
                    }
                    else -> {
                        return NavigationAgentOutcome.StoppedSafely("Stopped to keep you in control.")
                    }
                }
            }

            return NavigationAgentOutcome.StoppedSafely(
                "I took the maximum ${ActionPolicy.MAX_AI_NAVIGATION_STEPS} steps and stopped here so you can review the screen."
            )
        } finally {
            service.isTaskActive = false
        }
    }

    fun parseStepDto(rawJson: String): GeminiNavigationStepDto? {
        return try {
            GeminiStrictJson.parser.decodeFromString<GeminiNavigationStepDto>(rawJson)
        } catch (_: Exception) {
            null
        }
    }

    fun mapStepToAction(dto: GeminiNavigationStepDto): AssistantAction? = when (dto.action) {
        "CLICK_NODE" -> {
            val nodeId = dto.nodeId?.takeIf { it.isNotBlank() } ?: return null
            AssistantAction.ClickNode(
                snapshotId = dto.snapshotId,
                nodeId = nodeId,
                descriptionHint = dto.reason
            )
        }

        "SCROLL" -> {
            val direction = dto.scrollDirection?.let { dir ->
                ScrollDirection.entries.firstOrNull { it.name == dir }
            } ?: ScrollDirection.DOWN
            AssistantAction.Scroll(
                snapshotId = dto.snapshotId,
                nodeId = dto.nodeId,
                direction = direction
            )
        }

        "SET_TEXT" -> {
            val nodeId = dto.nodeId?.takeIf { it.isNotBlank() } ?: return null
            val text = dto.inputText?.takeIf { it.isNotBlank() } ?: return null
            AssistantAction.SetNodeText(
                snapshotId = dto.snapshotId,
                nodeId = nodeId,
                text = text
            )
        }

        else -> null
    }

    private fun buildNavigationPrompt(
        userGoal: String,
        snapshot: ScreenSnapshot,
        stepNumber: Int
    ): String {
        val snapshotJson = Json.encodeToString(snapshot)
        return buildString {
            appendLine("You are a strictly bounded Android accessibility navigation assistant.")
            appendLine("Goal: \"$userGoal\"")
            appendLine("Step $stepNumber of ${ActionPolicy.MAX_AI_NAVIGATION_STEPS}.")
            appendLine("Propose exactly ONE safe next action using ONLY node_ids present in the snapshot below.")
            appendLine("Use snapshot_id = \"${snapshot.snapshotId}\".")
            appendLine("If the user has already reached the desired screen, return action = \"DONE\" with a simple summary in reason.")
            appendLine("If uncertain, multiple items match, or screen looks wrong, return \"STOP_UNCERTAIN\" or \"ASK_CLARIFICATION\".")
            appendLine("Never interact with payment, banking, password, or permission screens.")
            appendLine("Current minimized screen snapshot:")
            appendLine(snapshotJson)
        }
    }
}
