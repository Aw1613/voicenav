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
                    add(kotlinx.serialization.json.JsonPrimitive("CALL_CONTACT"))
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
            put("app_name", buildJsonObject {
                put("type", "STRING")
                put("nullable", true)
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
            appendLine("Categorize the user's request accurately into one of the allowed intents:")
            appendLine("1. OPEN_SETTING: Use when user wants to change settings, including indirect/conversational descriptions:")
            appendLine("   - Screen brightness, glare, 'can't see the text/screen', 'too dark', 'too bright', font size, display -> destination: DISPLAY")
            appendLine("   - Sound, volume, 'too quiet', 'can't hear', ringer, loudness -> destination: SOUND")
            appendLine("   - Internet, Wi-Fi, 'connect to network', hotel wifi, mobile data -> destination: WIFI")
            appendLine("   - Bluetooth, headphones, hearing aid, earbuds -> destination: BLUETOOTH")
            appendLine("   - Hotspot, tethering -> destination: HOTSPOT")
            appendLine("   - Battery, power saving, phone dying -> destination: BATTERY")
            appendLine("   - Accessibility, talkback, screen reader, magnification -> destination: ACCESSIBILITY")
            appendLine("   - General phone settings -> destination: GENERAL_SETTINGS")
            appendLine("2. OPEN_APP: Use when user asks to open ANY app (e.g. 'open WhatsApp', 'open messages', 'open call log', 'open camera', 'open youtube', 'open contacts', etc.). Set app_name to the name of the app.")
            appendLine("3. CALL_CONTACT: Use when user asks to call someone (e.g. 'call Rahul', 'call doctor', 'call 9876543210'). Set contact_query to the name, or phone_number to digits.")
            appendLine("4. SEARCH_CONTACT: Use when user wants to look up or find someone's phone number or contact info.")
            appendLine("5. CREATE_CONTACT: Use when user wants to add/save a new contact name and number.")
            appendLine("6. GO_HOME, GO_BACK, OPEN_RECENTS: Basic navigation.")
            appendLine("7. If request is ambiguous, return ASK_CLARIFICATION with a short simple question.")
            appendLine("8. Never approve deletion, banking, money transfer, or financial payments—map those to UNSUPPORTED.")
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

    private fun resolveDestination(raw: String?): SettingDestination? {
        if (raw.isNullOrBlank()) return null
        val upper = raw.trim().uppercase()
        return SettingDestination.entries.firstOrNull { it.name == upper }
            ?: when {
                upper.contains("DISPLAY") || upper.contains("BRIGHTNESS") || upper.contains("SCREEN") || upper.contains("FONT") -> SettingDestination.DISPLAY
                upper.contains("SOUND") || upper.contains("VOLUME") || upper.contains("AUDIO") || upper.contains("RING") -> SettingDestination.SOUND
                upper.contains("WIFI") || upper.contains("WI-FI") || upper.contains("INTERNET") || upper.contains("NETWORK") -> SettingDestination.WIFI
                upper.contains("BLUETOOTH") || upper.contains("HEADPHONE") || upper.contains("EARBUD") -> SettingDestination.BLUETOOTH
                upper.contains("HOTSPOT") || upper.contains("TETHERING") -> SettingDestination.HOTSPOT
                upper.contains("BATTERY") || upper.contains("POWER") -> SettingDestination.BATTERY
                upper.contains("ACCESSIBILITY") || upper.contains("TALKBACK") -> SettingDestination.ACCESSIBILITY
                upper.contains("SETTING") -> SettingDestination.GENERAL_SETTINGS
                else -> null
            }
    }

    fun parseAndValidateResponse(rawJson: String): IntentInterpretationResult {
        val dto = try {
            GeminiStrictJson.parser.decodeFromString<GeminiIntentResponseDto>(rawJson)
        } catch (_: Exception) {
            return IntentInterpretationResult.Rejected(
                "I received an unexpected response format and stopped safely."
            )
        }

        val rawIntentUpper = dto.intent.trim().uppercase()

        // Normalize fuzzy intents
        val normalizedIntent = when {
            rawIntentUpper.contains("CALL") -> "CALL_CONTACT"
            rawIntentUpper.contains("APP") || !dto.appName.isNullOrBlank() -> "OPEN_APP"
            rawIntentUpper.contains("SETTING") || rawIntentUpper.contains("BRIGHTNESS") || rawIntentUpper.contains("DISPLAY") || rawIntentUpper.contains("VOLUME") || rawIntentUpper.contains("SOUND") || rawIntentUpper.contains("WIFI") || rawIntentUpper.contains("BATTERY") -> "OPEN_SETTING"
            else -> rawIntentUpper
        }

        if (dto.confidence !in 0.0..1.0 && dto.confidence != 0.0) {
            return IntentInterpretationResult.Rejected("Confidence out of range.")
        }

        if (dto.confidence < ActionPolicy.MIN_AI_INTENT_CONFIDENCE &&
            normalizedIntent != "ASK_CLARIFICATION" &&
            normalizedIntent != "UNSUPPORTED"
        ) {
            val question = dto.clarificationQuestion?.takeIf { it.isNotBlank() }
                ?: "I wasn't sure what you meant. Could you say that another way?"
            return IntentInterpretationResult.Interpreted(
                action = AssistantAction.AskClarification(question),
                summaryForUser = question
            )
        }

        val action: AssistantAction = when (normalizedIntent) {
            "OPEN_SETTING" -> {
                val dest = resolveDestination(dto.destination)
                    ?: resolveDestination(rawIntentUpper)
                    ?: return IntentInterpretationResult.Rejected(
                        "Unknown setting destination returned by AI."
                    )
                AssistantAction.OpenSetting(dest)
            }

            "OPEN_APP" -> {
                val rawTarget = dto.appName?.trim()?.takeIf { it.isNotBlank() }
                    ?: dto.contactQuery?.trim()?.takeIf { it.isNotBlank() }
                if (rawTarget != null) {
                    val targetApp = rawTarget.lowercase()
                        .replace("what's app", "whatsapp")
                        .replace("whats app", "whatsapp")
                        .replace("you tube", "youtube")
                        .replace("call logs", "call log")
                        .trim()
                    if (targetApp == "contacts" || targetApp == "phonebook") {
                        AssistantAction.OpenApp(SupportedApp.CONTACTS)
                    } else if (targetApp == "settings") {
                        AssistantAction.OpenSetting(SettingDestination.GENERAL_SETTINGS)
                    } else {
                        AssistantAction.OpenNamedApp(targetApp)
                    }
                } else {
                    AssistantAction.OpenApp(SupportedApp.CONTACTS)
                }
            }

            "CALL_CONTACT" -> {
                val target = dto.phoneNumber?.trim()?.takeIf { it.isNotBlank() }
                    ?: dto.contactQuery?.trim()?.takeIf { it.isNotBlank() }
                    ?: dto.contactName?.trim()?.takeIf { it.isNotBlank() }
                    ?: return IntentInterpretationResult.Rejected("Please specify who you would like to call.")
                AssistantAction.PlaceCall(target)
            }

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
