package com.elderassist.app

import com.elderassist.app.core.model.ActionSource
import com.elderassist.app.core.model.AssistantAction
import com.elderassist.app.core.model.AssistantMode
import com.elderassist.app.core.model.ExecutionContext
import com.elderassist.app.core.model.SettingDestination
import com.elderassist.app.execution.ActionDispatcher
import com.elderassist.app.execution.ActionValidator
import com.elderassist.app.execution.TaskRunner
import com.elderassist.app.intelligence.DefaultActionPlanner
import com.elderassist.app.intelligence.GeminiClient
import com.elderassist.app.intelligence.GeminiIntentInterpreter
import com.elderassist.app.intelligence.GeneralNavigationAgent
import com.elderassist.app.intelligence.IntentInterpretationResult
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiSchemaValidationTest {

    private val dummyClient = object : GeminiClient {
        override fun isConfigured(): Boolean = true
        override suspend fun generateStructuredJson(prompt: String, responseSchema: JsonObject): Result<String> =
            Result.failure(UnsupportedOperationException("Not called in unit test"))
    }

    private val interpreter = GeminiIntentInterpreter(dummyClient)

    @Test
    fun `valid structured intent JSON parses to typed action`() {
        val json = """
            {
              "intent": "OPEN_SETTING",
              "destination": "BLUETOOTH",
              "contact_query": null,
              "contact_name": null,
              "phone_number": null,
              "new_contact_name": null,
              "confidence": 0.96,
              "requires_confirmation": false,
              "clarification_question": null,
              "user_facing_summary": "Opening Bluetooth settings."
            }
        """.trimIndent()

        val result = interpreter.parseAndValidateResponse(json)
        assertTrue(result is IntentInterpretationResult.Interpreted)
        assertEquals(
            AssistantAction.OpenSetting(SettingDestination.BLUETOOTH),
            (result as IntentInterpretationResult.Interpreted).action
        )
    }

    @Test
    fun `malformed JSON or unknown fields or unknown destination are rejected`() {
        val unknownField = """
            {
              "intent": "OPEN_SETTING",
              "destination": "BLUETOOTH",
              "unexpected_field": "evil",
              "confidence": 0.95,
              "requires_confirmation": false,
              "user_facing_summary": "Opening"
            }
        """.trimIndent()
        assertTrue(interpreter.parseAndValidateResponse(unknownField) is IntentInterpretationResult.Rejected)

        val unknownDestination = """
            {
              "intent": "OPEN_SETTING",
              "destination": "SECRET_ROOT_MENU",
              "confidence": 0.99,
              "requires_confirmation": false,
              "user_facing_summary": "Opening"
            }
        """.trimIndent()
        assertTrue(interpreter.parseAndValidateResponse(unknownDestination) is IntentInterpretationResult.Rejected)

        assertTrue(interpreter.parseAndValidateResponse("not json at all") is IntentInterpretationResult.Rejected)
    }

    @Test
    fun `low confidence intent turns into clarification rather than executing`() {
        val lowConfidence = """
            {
              "intent": "OPEN_SETTING",
              "destination": "WIFI",
              "confidence": 0.45,
              "requires_confirmation": false,
              "clarification_question": "Did you want Wi-Fi settings?",
              "user_facing_summary": "Checking"
            }
        """.trimIndent()

        val result = interpreter.parseAndValidateResponse(lowConfidence)
        assertTrue(result is IntentInterpretationResult.Interpreted)
        assertTrue((result as IntentInterpretationResult.Interpreted).action is AssistantAction.AskClarification)
    }

    @Test
    fun `navigation step DTO rejects unknown fields and maps valid click`() {
        val validJson = """
            {
              "action": "CLICK_NODE",
              "snapshot_id": "snapshot_42",
              "node_id": "node_7",
              "scroll_direction": null,
              "input_text": null,
              "reason": "The visible row is labelled Display.",
              "confidence": 0.94,
              "requires_confirmation": false
            }
        """.trimIndent()

        val validParsed = com.elderassist.app.intelligence.GeminiStrictJson.parser
            .decodeFromString<com.elderassist.app.intelligence.GeminiNavigationStepDto>(validJson)
        assertEquals("CLICK_NODE", validParsed.action)
        assertEquals("snapshot_42", validParsed.snapshotId)
        assertEquals("node_7", validParsed.nodeId)

        val malformed = """{"action":"CLICK_NODE","snapshot_id":"s1","extra":1,"reason":"r","confidence":0.9,"requires_confirmation":false}"""
        val rejected = try {
            com.elderassist.app.intelligence.GeminiStrictJson.parser
                .decodeFromString<com.elderassist.app.intelligence.GeminiNavigationStepDto>(malformed)
        } catch (_: Exception) {
            null
        }
        assertNull(rejected)
    }
}
