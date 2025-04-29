package com.aemiio.braillelens.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TTSManager private constructor(private val context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private val _isTTSReady = MutableStateFlow(false)
    val isTTSReady: StateFlow<Boolean> = _isTTSReady

    private val _pitch = MutableStateFlow(1.0f)
    val pitch = _pitch.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate = _speechRate.asStateFlow()

    init {
        initTTS()
    }

    private fun initTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = Locale("fil", "PH")
                val languageResult = textToSpeech?.setLanguage(locale)

                if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                    languageResult == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    Toast.makeText(context, "Filipino language not available", Toast.LENGTH_SHORT)
                        .show()
                    textToSpeech?.setLanguage(Locale.getDefault())
                }

                textToSpeech?.setPitch(_pitch.value)
                textToSpeech?.setSpeechRate(_speechRate.value)
                _isTTSReady.value = true
            } else {
                Toast.makeText(context, "TTS Initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun speak(text: String) {
        if (_isTTSReady.value) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts-id-$text")
        } else {
            Toast.makeText(context, "TTS not ready", Toast.LENGTH_SHORT).show()
        }
    }

    fun setPitch(newPitch: Float) {
        _pitch.value = newPitch
        textToSpeech?.setPitch(newPitch)
    }

    fun setSpeechRate(newRate: Float) {
        _speechRate.value = newRate
        textToSpeech?.setSpeechRate(newRate)
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isTTSReady.value = false
    }

    companion object {
        @Volatile
        private var INSTANCE: TTSManager? = null

        fun getInstance(context: Context): TTSManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TTSManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}