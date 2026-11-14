package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.data.model.AppLanguage
import java.util.Locale

class VoiceCoach(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    private var lastSpokenTime = 0L

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.setSpeechRate(1.08f)
            tts?.setPitch(0.95f)
            setLanguage(AppLanguage.HINGLISH)
        }
    }

    fun setLanguage(language: AppLanguage) {
        if (!isReady) return
        val locale = if (language == AppLanguage.HINGLISH) Locale("en", "IN") else Locale.US
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale.US)
        }
    }

    fun speakFeedback(text: String, force: Boolean = false) {
        if (!isReady || text.isBlank()) return
        val now = System.currentTimeMillis()
        if (!force && now - lastSpokenTime < 1400L) return
        lastSpokenTime = now
        val mode = if (force) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, mode, null, "coach_${now}")
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
