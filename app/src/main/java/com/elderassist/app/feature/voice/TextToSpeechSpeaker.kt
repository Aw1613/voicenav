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

    fun speak(message: String) {
        if (message.isBlank()) return
        if (!isReady) {
            pendingUtterance = message
            return
        }
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
