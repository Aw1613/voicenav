package com.elderassist.app.intelligence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiIntentResponseDto(
    val intent: String,
    val destination: String? = null,
    @SerialName("contact_query")
    val contactQuery: String? = null,
    @SerialName("contact_name")
    val contactName: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("new_contact_name")
    val newContactName: String? = null,
    val confidence: Double,
    @SerialName("requires_confirmation")
    val requiresConfirmation: Boolean = false,
    @SerialName("clarification_question")
    val clarificationQuestion: String? = null,
    @SerialName("user_facing_summary")
    val userFacingSummary: String
)

@Serializable
data class GeminiNavigationStepDto(
    val action: String,
    @SerialName("snapshot_id")
    val snapshotId: String,
    @SerialName("node_id")
    val nodeId: String? = null,
    @SerialName("scroll_direction")
    val scrollDirection: String? = null,
    @SerialName("input_text")
    val inputText: String? = null,
    val reason: String,
    val confidence: Double,
    @SerialName("requires_confirmation")
    val requiresConfirmation: Boolean = false
)

object GeminiStrictJson {
    val parser = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = false
        isLenient = false
        explicitNulls = true
    }
}
