package com.example.formamind.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Обёртка над системным TextToSpeech.
 *
 * Задачи:
 *  - Инициализация TTS с русским языком (fallback на дефолтный)
 *  - Настройка «спокойного» голоса: медленная скорость, чуть ниже тон
 *  - speak(text) — прочитать текст вслух
 *  - stop()      — остановить текущее воспроизведение
 *  - shutdown()  — освободить ресурсы
 *
 * Настройки голоса подобраны под ментальные тренировки:
 *  - SPEECH_RATE = 0.85f — медленнее обычного, но не «тормоз»
 *  - SPEECH_PITCH = 0.95f — чуть ниже тон, звучит спокойнее
 */
class TtsManager(context: Context) {

    private var tts: TextToSpeech? = null

    @Volatile
    private var isReady: Boolean = false

    @Volatile
    private var isRussianAvailable: Boolean = false

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            Log.d(TAG, "TTS: начал говорить ($utteranceId)")
        }

        override fun onDone(utteranceId: String?) {
            Log.d(TAG, "TTS: закончил ($utteranceId)")
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            Log.e(TAG, "TTS: ошибка ($utteranceId)")
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            Log.e(TAG, "TTS: ошибка ($utteranceId), code=$errorCode")
        }
    }

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                onTtsInitialized()
            } else {
                Log.e(TAG, "Ошибка инициализации TTS: status=$status")
                isReady = false
            }
        }
    }

    private fun onTtsInitialized() {
        // Устанавливаем русский язык (fallback — дефолтный).
        val result = tts?.setLanguage(Locale("ru", "RU"))

        when (result) {
            TextToSpeech.LANG_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_AVAILABLE,
            TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                isRussianAvailable = true
                Log.d(TAG, "Русский язык доступен")
            }
            TextToSpeech.LANG_MISSING_DATA -> {
                Log.w(TAG, "Русский язык не установлен — используем дефолтный")
                isRussianAvailable = false
            }
            TextToSpeech.LANG_NOT_SUPPORTED -> {
                Log.w(TAG, "Русский не поддерживается движком — используем дефолтный")
                isRussianAvailable = false
            }
            else -> {
                Log.w(TAG, "Неизвестный результат setLanguage: $result")
                isRussianAvailable = false
            }
        }

        // ⚙️ Настройки «спокойного» голоса для ментальных тренировок.
        tts?.setSpeechRate(SPEECH_RATE)
        tts?.setPitch(SPEECH_PITCH)

        tts?.setOnUtteranceProgressListener(progressListener)

        isReady = true
    }

    fun speak(text: String) {
        if (!isReady) {
            Log.w(TAG, "TTS ещё не готов, пропускаем speak()")
            return
        }
        if (text.isBlank()) return

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            UTTERANCE_ID
        )
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }

    companion object {
        private const val TAG = "TtsManager"
        private const val UTTERANCE_ID = "formamind_utterance"

        /**
         * Скорость речи.
         * 1.0 — обычная, 0.85 — спокойнее (для психологического приложения).
         * Диапазон: 0.5 (очень медленно) — 2.0 (очень быстро).
         */
        private const val SPEECH_RATE = 0.85f

        /**
         * Тон голоса.
         * 1.0 — обычный, 0.95 — чуть ниже (звучит спокойнее).
         * Диапазон: 0.5 (низкий) — 2.0 (высокий).
         */
        private const val SPEECH_PITCH = 0.95f
    }
}