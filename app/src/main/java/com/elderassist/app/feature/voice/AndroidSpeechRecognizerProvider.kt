package com.elderassist.app.feature.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidSpeechRecognizerProvider(
    private val context: Context
) : SpeechInputProvider {

    override val providerName: String = "Android SpeechRecognizer"

    private val mainHandler = Handler(Looper.getMainLooper())
    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    override val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null
    private var activeContinuation: CancellableContinuation<Result<String>>? = null

    override fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    override suspend fun startListening(languageTag: String): Result<String> {
        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasMicPermission) {
            val message = "Microphone permission is needed so I can hear your request."
            _state.value = SpeechState.Error(message, isPermissionError = true)
            return Result.failure(SecurityException(message))
        }

        if (!isAvailable()) {
            val message = "Voice recognition is not available on this device. You can type your request below."
            _state.value = SpeechState.Error(message)
            return Result.failure(IllegalStateException(message))
        }

        return suspendCancellableCoroutine { continuation ->
            mainHandler.post {
                cancelActiveSessionLocked()
                activeContinuation = continuation
                _state.value = SpeechState.Listening()

                val instance = SpeechRecognizer.createSpeechRecognizer(context).also { sr ->
                    recognizer = sr
                    sr.setRecognitionListener(createListener())
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }

                try {
                    instance.startListening(intent)
                } catch (e: Exception) {
                    val msg = "Could not start listening. Please try again."
                    _state.value = SpeechState.Error(msg)
                    finishWith(Result.failure(e))
                }
            }

            continuation.invokeOnCancellation {
                mainHandler.post { cancelActiveSessionLocked() }
            }
        }
    }

    override fun stopListening() {
        mainHandler.post {
            _state.value = SpeechState.Processing
            recognizer?.stopListening()
        }
    }

    override fun cancel() {
        mainHandler.post {
            cancelActiveSessionLocked()
            _state.value = SpeechState.Idle
        }
    }

    override fun release() {
        mainHandler.post {
            cancelActiveSessionLocked()
            recognizer?.destroy()
            recognizer = null
        }
    }

    private fun cancelActiveSessionLocked() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        val pending = activeContinuation
        activeContinuation = null
        if (pending != null && pending.isActive) {
            pending.resume(Result.failure(InterruptedException("Listening cancelled")))
        }
    }

    private fun finishWith(result: Result<String>) {
        val pending = activeContinuation
        activeContinuation = null
        recognizer?.destroy()
        recognizer = null
        if (pending != null && pending.isActive) {
            pending.resume(result)
        }
    }

    private fun createListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = SpeechState.Listening()
        }

        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            if (_state.value is SpeechState.Listening) {
                _state.value = SpeechState.Processing
            }
        }

        override fun onError(error: Int) {
            val friendlyMessage = mapErrorCode(error)
            _state.value = SpeechState.Error(
                message = friendlyMessage,
                isPermissionError = error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS
            )
            finishWith(Result.failure(IllegalStateException(friendlyMessage)))
        }

        override fun onResults(results: Bundle?) {
            val transcript = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()

            if (transcript.isEmpty()) {
                val msg = "I didn't catch that. Please tap the microphone and speak clearly."
                _state.value = SpeechState.Error(msg)
                finishWith(Result.failure(IllegalStateException(msg)))
            } else {
                _state.value = SpeechState.Success(transcript)
                finishWith(Result.success(transcript))
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (partial.isNotEmpty()) {
                _state.value = SpeechState.Listening(partial)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun mapErrorCode(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please try again."
        SpeechRecognizer.ERROR_CLIENT -> "Voice session was stopped."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            "Microphone permission is needed so I can hear you."
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            "Speech recognition needs a working connection or offline speech pack. You can also type below."
        SpeechRecognizer.ERROR_NO_MATCH ->
            "I couldn't hear a clear command. Please try speaking again."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
            "Voice recognition is busy right now. Please wait a second and try again."
        SpeechRecognizer.ERROR_SERVER ->
            "Voice service had a temporary problem. Please try again."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
            "No speech detected. Tap the microphone when you're ready to speak."
        else -> "Could not hear your command. Please try again."
    }
}
