package com.elderassist.app.feature.voice

import kotlinx.coroutines.flow.StateFlow

sealed interface SpeechState {
    data object Idle : SpeechState
    data class Listening(val partialTranscript: String = "") : SpeechState
    data object Processing : SpeechState
    data class Success(val transcript: String) : SpeechState
    data class Error(
        val message: String,
        val isPermissionError: Boolean = false
    ) : SpeechState
}

/**
 * Swappable speech-to-text contract.
 * AndroidSpeechRecognizerProvider is the default implementation;
 * future dictation providers (e.g., Wispr Flow or an external model) can plug in here.
 */
interface SpeechInputProvider {
    val providerName: String
    val state: StateFlow<SpeechState>

    fun isAvailable(): Boolean
    suspend fun startListening(languageTag: String = "en-IN"): Result<String>
    fun stopListening()
    fun cancel()
    fun release()
}
