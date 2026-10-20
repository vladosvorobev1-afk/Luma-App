package com.example.formamind.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.formamind.data.CourseRepository
import com.example.formamind.data.settings.SettingsRepository
import com.example.formamind.data.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана настроек.
 *
 * Отвечает за:
 *  - чтение настроек из DataStore (Flow → StateFlow)
 *  - сохранение изменений (setXxx)
 *  - сброс истории (через CourseRepository)
 *
 * ⚠️ Планирование напоминаний вынесено в FormaMindApp —
 * там подписка на DataStore и вызов ReminderScheduler.
 * Здесь только UI-состояние и сеттеры.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val courseRepository = CourseRepository(application.applicationContext)

    // ==================== FLOW → STATE FLOW ====================

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ThemeMode.SYSTEM
        )

    val remindersEnabled: StateFlow<Boolean> = settingsRepository.remindersEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = true
        )

    val reminderHour: StateFlow<Int> = settingsRepository.reminderHour
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SettingsRepository.DEFAULT_REMINDER_HOUR
        )

    val reminderMinute: StateFlow<Int> = settingsRepository.reminderMinute
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SettingsRepository.DEFAULT_REMINDER_MINUTE
        )

    val ttsEnabled: StateFlow<Boolean> = settingsRepository.ttsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = false
        )

    // ==================== СЕТТЕРЫ ====================

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setRemindersEnabled(enabled)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepository.setReminderTime(hour, minute)
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTtsEnabled(enabled)
        }
    }

    // ==================== ДЕЙСТВИЯ ====================

    fun clearHistory() {
        viewModelScope.launch {
            courseRepository.clearHistory()
        }
    }
}