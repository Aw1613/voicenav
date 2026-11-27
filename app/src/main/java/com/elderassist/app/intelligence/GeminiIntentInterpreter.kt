package com.elderassist.app.intelligence

import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.core.model.SupportedApp
import com.elderassist.app.core.policy.ActionPolicy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface IntentInterpretationResult {
    data class Interpreted(
        val action: AssistantAction,
        val summaryForUser: String
    ) : IntentInterpretationResult

    data class Rejected(val userMessage: String) : IntentInterpretationResult
}

class GeminiIntentInterpreter(
    private val geminiClient: GeminiClient
) {

    private val intentSchema: JsonObject = buildJsonObject {
        put("type", "OBJECT")
        put("properties", buildJsonObject {
            put("intent", buildJsonObject {
                put("type", "STRING")
                put("enum", buildJsonArray {
                    add(kotlinx.serialization.json.JsonPrimitive("OPEN_SETTING"))
                    add(kotlinx.serialization.json.JsonPrimitive("OPEN_APP"))
                    add(kotlinx.serialization.json.JsonPrimitive("GO_HOME"))
                    add(kotlinx.serialization.json.JsonPrimitive("GO_BACK"))
                    add(kotlinx.serialization.json.JsonPrimitive("OPEN_RECENTS"))
                    add(kotlinx.serialization.json.JsonPrimitive("SEARCH_CONTACT"))
                    add(kotlinx.serialization.json.JsonPrimitive("CREATE_CONTACT"))
                    add(kotlinx.serialization.json.JsonPrimitive("ASK_CLARIFICATION"))
                    add(kotlinx.serialization.json.JsonPrimitive("EXPLAIN"))
                    add(kotlinx.serialization.json.JsonPrimitive("UNSUPPORTED"))
                })
            })
            put("destination", buildJsonObject {
                put("type", "STRING")
                put("nullable", true)
                put("enum", buildJsonArray {
                    SettingDestination.entries.forEach {
                        add(kotlinx.serialization.json.JsonPrimitive(it.name))
                    }
                })
            })
            put("contact_query", buildJsonObject {
                put("type", "STRING")
                put("nullable", true)
            })
            put("contact_name", buildJsonObject {
                put("type", "STRING")
                put("nullable", true)
            })
            put("phone_number", buildJsonObject {
                put("type", "STRING")
                put("nullable", true)
            })
            put("new_contact_name", buildJsonObject {
                put("type", "STRING")
                put("nullable", true)
            })
            put("confidence", buildJsonObject { put("type", "NUMBER") })
            put("requires_confirmation", buildJsonObject { put("type", "BOOLEAN") })
            put("clarification_question", buildJsonObject {
                put("type", "STRING")
                put("nullable", true)
            })
            put("user_facing_summary", buildJsonObject { put("type", "STRING") })
        })
        put("required", buildJsonArray {
            add(kotlinx.serialization.json.JsonPrimitive("intent"))
            add(kotlinx.serialization.json.JsonPrimitive("confidence"))
            add(kotlinx.serialization.json.JsonPrimitive("requires_confirmation"))
            add(kotlinx.serialization.json.JsonPrimitive("user_facing_summary"))
        })
    }

    suspend fun interpret(transcript: String): IntentInterpretationResult {
        if (!geminiClient.isConfigured()) {
            return IntentInterpretationResult.Rejected(
                "I didn't recognize that command, and AI interpretation is currently unavailable."
            )
        }

        val prompt = buildString {
            appendLine("You interpret voice requests from an older adult using an Android accessibility assistant.")
            appendLine("Never invent actions outside the allowed schema.")
            appendLine("Never approve deletion, banking, payments, calling, or sending messages—map those to UNSUPPORTED.")
            appendLine("If the request is ambiguous (e.g. unclear which setting), return ASK_CLARIFICATION with a short simple question.")
            appendLine("Allowed destinations for OPEN_SETTING: WIFI, BLUETOOTH, HOTSPOT, DISPLAY, SOUND, ACCESSIBILITY, BATTERY, GENERAL_SETTINGS.")
            appendLine("User request: \"$transcript\"")
        }

        val rawResult = geminiClient.generateStructuredJson(prompt, intentSchema)
        val jsonString = rawResult.getOrElse {
            return IntentInterpretationResult.Rejected(
                "I couldn't reach the AI helper right now. Please try a direct command like 'Open Wi-Fi settings'."
            )
        }

        return parseAndValidateResponse(jsonString)
    }

    fun parseAndValidateResponse(rawJson: String): IntentInterpretationResult {
        val dto = try {
            GeminiStrictJson.parser.decodeFromString<GeminiIntentResponseDto>(rawJson)
        } catch (_: Exception) {
            return IntentInterpretationResult.Rejected(
                "I received an unexpected response format and stopped safely."
            )
        }

        if (dto.confidence !in 0.0..1.0) {
            return IntentInterpretationResult.Rejected("Confidence out of range.")
        }

        if (dto.confidence < ActionPolicy.MIN_AI_INTENT_CONFIDENCE &&
            dto.intent != "ASK_CLARIFICATION" &&
            dto.intent != "UNSUPPORTED"
        ) {
            val question = dto.clarificationQuestion?.takeIf { it.isNotBlank() }
                ?: "I wasn't sure what you meant. Could you say that another way?"
            return IntentInterpretationResult.Interpreted(
                action = AssistantAction.AskClarification(question),
                summaryForUser = question
            )
        }

        val action: AssistantAction = when (dto.intent) {
            "OPEN_SETTING" -> {
                val dest = dto.destination?.let { name ->
                    SettingDestination.entries.firstOrNull { it.name == name }
                } ?: return IntentInterpretationResult.Rejected(
                    "Unknown setting destination returned by AI."
                )
                AssistantAction.OpenSetting(dest)
            }

            "OPEN_APP" -> AssistantAction.OpenApp(SupportedApp.CONTACTS)
            "GO_HOME" -> AssistantAction.GoHome
            "GO_BACK" -> AssistantAction.GoBack
            "OPEN_RECENTS" -> AssistantAction.OpenRecents

            "SEARCH_CONTACT" -> {
                val query = dto.contactQuery?.trim().orEmpty()
                if (query.isEmpty()) {
                    return IntentInterpretationResult.Rejected("Missing contact name to search.")
                }
                AssistantAction.SearchContact(query)
            }

            "CREATE_CONTACT" -> {
                val name = dto.contactName?.trim().orEmpty()
                val phone = dto.phoneNumber?.trim().orEmpty()
                if (name.isEmpty() || phone.isEmpty()) {
                    AssistantAction.AskClarification(
                        "Please tell me both the name and phone number to save."
                    )
                } else {
                    AssistantAction.CreateContact(name, phone)
                }
            }

            "ASK_CLARIFICATION" -> {
                val q = dto.clarificationQuestion?.takeIf { it.isNotBlank() }
                    ?: dto.userFacingSummary.ifBlank { "Could you clarify what you would like to open?" }
                AssistantAction.AskClarification(q)
            }

            "EXPLAIN" -> AssistantAction.Explain(dto.userFacingSummary)

            "UNSUPPORTED" -> AssistantAction.Unsupported(
                dto.userFacingSummary.ifBlank { "That request is not supported in ElderAssist yet." }
            )

            else -> return IntentInterpretationResult.Rejected("Unsupported AI intent: ${dto.intent}")
        }

        return IntentInterpretationResult.Interpreted(
            action = action,
            summaryForUser = dto.userFacingSummary.ifBlank { "Working on your request." }
        )
    }
}
