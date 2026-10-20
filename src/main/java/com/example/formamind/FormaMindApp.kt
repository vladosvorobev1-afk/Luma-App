package com.example.formamind

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.formamind.data.settings.SettingsRepository
import com.example.formamind.notifications.NotificationHelper
import com.example.formamind.notifications.ReminderScheduler
import com.example.formamind.tts.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Application-класс приложения.
 *
 * Создаётся ОДИН раз при старте процесса.
 * Идеальное место для:
 *  - глобальной инициализации (NotificationChannel, TTS)
 *  - подписки на настройки (DataStore → WorkManager)
 *  - отслеживания lifecycle процесса (ProcessLifecycleOwner)
 *
 * ⚠️ В AndroidManifest.xml указан android:name=".FormaMindApp".
 */
class FormaMindApp : Application() {

    /**
     * Корневой scope для глобальных подписок.
     *
     * SupervisorJob — чтобы падение одной корутины не убило остальные.
     */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Репозиторий настроек — общий для всего приложения. */
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()

        settingsRepository = SettingsRepository(this)

        // 1. NotificationChannel
        NotificationHelper.createChannel(this)

        // 2. TTS
        initTts()

        // 3. Подписки на настройки
        observeReminderSettings()
        observeTtsSetting()

        // 4. Lifecycle процесса — для остановки TTS при уходе в фон.
        setupProcessLifecycle()
    }

    /**
     * Инициализирует TtsManager и сохраняет в companion object.
     */
    private fun initTts() {
        ttsManager = TtsManager(this)
    }

    /**
     * Слушаем настройки напоминаний → планируем/отменяем WorkManager.
     */
    private fun observeReminderSettings() {
        appScope.launch {
            combine(
                settingsRepository.remindersEnabled,
                settingsRepository.reminderHour,
                settingsRepository.reminderMinute
            ) { enabled, hour, minute ->
                Triple(enabled, hour, minute)
            }
                .distinctUntilChanged()
                .collect { (enabled, hour, minute) ->
                    if (enabled) {
                        ReminderScheduler.schedule(this@FormaMindApp, hour, minute)
                    } else {
                        ReminderScheduler.cancel(this@FormaMindApp)
                    }
                }
        }
    }

    /**
     * Слушаем флаг ttsEnabled.
     *
     * Если выключили — сразу останавливаем текущее воспроизведение.
     */
    private fun observeTtsSetting() {
        appScope.launch {
            settingsRepository.ttsEnabled
                .distinctUntilChanged()
                .collect { enabled ->
                    if (!enabled) {
                        ttsManager?.stop()
                    }
                }
        }
    }

    /**
     * ProcessLifecycleOwner — единый lifecycle для всего приложения.
     *
     * Отличие от Activity lifecycle:
     *  - onStart → приложение видимо (любая Activity)
     *  - onStop  → приложение ушло в фон (все Activity уничтожены/свёрнуты)
     *
     * Используем для остановки TTS, когда пользователь ушёл из приложения —
     * чтобы озвучка не продолжала играть в фоне.
     */
    private fun setupProcessLifecycle() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    // Приложение ушло в фон — останавливаем TTS.
                    ttsManager?.stop()
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    // Вызывается при завершении процесса (если получится).
                    // На практике Android может убить процесс без этого вызова,
                    // но если вызовется — освободим ресурсы.
                    ttsManager?.shutdown()
                }
            }
        )
    }

    companion object {
        /**
         * Глобальный доступ к TtsManager.
         *
         * lateinit — инициализируется в onCreate().
         * Использование: FormaMindApp.ttsManager?.speak("текст")
         */
        var ttsManager: TtsManager? = null
            private set
    }
}