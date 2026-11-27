package com.elderassist.app.feature.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechSpeaker(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    private var pendingUtterance: String? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val langResult = tts?.setLanguage(Locale.getDefault())
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            tts?.setSpeechRate(0.92f)
            isReady = true
            pendingUtterance?.let { queued ->
                pendingUtterance = null
                speak(queued)
            }
        }
    }

    fun speak(
        message: String,
        speechRate: Float = 0.95f,
        languageTag: String = "en-IN"
    ) {
        if (message.isBlank()) return
        if (!isReady) {
            pendingUtterance = message
            return
        }
        val locale = Locale.forLanguageTag(languageTag)
        val langRes = tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        if (langRes >= TextToSpeech.LANG_AVAILABLE) {
            tts?.language = locale
        }
        tts?.setSpeechRate(speechRate.coerceIn(0.65f, 1.3f))
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "elder_assist_utterance")
    }

    fun stop() {
        pendingUtterance = null
        tts?.stop()
    }

    fun shutdown() {
        pendingUtterance = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
