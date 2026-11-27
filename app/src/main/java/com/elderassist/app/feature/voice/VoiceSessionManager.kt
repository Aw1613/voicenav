package com.elderassist.app.feature.voice

import kotlinx.coroutines.flow.StateFlow

class VoiceSessionManager(
    private val speechProvider: SpeechInputProvider,
    private val speaker: TextToSpeechSpeaker
) {
    val speechState: StateFlow<SpeechState> = speechProvider.state
    val providerName: String get() = speechProvider.providerName

    fun isSpeechAvailable(): Boolean = speechProvider.isAvailable()

    suspend fun listenOnce(languageTag: String = "en-IN"): Result<String> {
        speaker.stop()
        return speechProvider.startListening(languageTag)
    }

    fun stopListening() {
        speechProvider.stopListening()
    }

    fun cancelListening() {
        speechProvider.cancel()
        speaker.stop()
    }

    fun speakFeedback(message: String, ttsEnabled: Boolean) {
        if (ttsEnabled && message.isNotBlank()) {
            speaker.speak(message)
        }
    }

    fun stopSpeaking() {
        speaker.stop()
    }

    fun release() {
        speechProvider.release()
        speaker.shutdown()
    }
}
