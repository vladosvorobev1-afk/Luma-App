package com.example.formamind.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Расширение Context — создаёт DataStore с именем "settings".
 *
 * preferencesDataStore — extension property. Создаётся один раз
 * на весь Context (внутри Android использует singleton).
 *
 * Имя "settings" — это имя файла в /data/data/com.example.formamind/files/datastore/.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)

/**
 * Репозиторий настроек приложения.
 *
 * Хранит:
 *  - режим темы (SYSTEM / LIGHT / DARK)
 *  - флаг «напоминания включены»
 *  - время напоминания (час, 0..23)
 *  - флаг «озвучка (TTS) включена»
 *
 * Все значения — через Flow. UI подписывается и автообновляется.
 *
 * Почему DataStore, а не SharedPreferences:
 *  - асинхронный (не блокирует UI-поток)
 *  - реактивный (Flow)
 *  - типобезопасный (Preferences keys)
 *  - рекомендован Google с 2020
 */
class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    // ==================== КЛЮЧИ ====================

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
    }

    // ==================== ДЕФОЛТЫ ====================

    companion object {
        const val DEFAULT_REMINDER_HOUR = 20
        const val DEFAULT_REMINDER_MINUTE = 0
    }

    // ==================== FLOW-ГЕТТЕРЫ ====================

    /** Режим темы. По умолчанию — SYSTEM. */
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromString(prefs[Keys.THEME_MODE])
    }

    /** Включены ли напоминания. По умолчанию — true. */
    val remindersEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.REMINDERS_ENABLED] ?: true
    }

    /** Час напоминания (0..23). По умолчанию — 20. */
    val reminderHour: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.REMINDER_HOUR] ?: DEFAULT_REMINDER_HOUR
    }

    /** Минута напоминания (0..59). По умолчанию — 0. */
    val reminderMinute: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.REMINDER_MINUTE] ?: DEFAULT_REMINDER_MINUTE
    }

    /** Включена ли озвучка (TTS). По умолчанию — false. */
    val ttsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.TTS_ENABLED] ?: false
    }

    // ==================== СЕТТЕРЫ ====================

    /**
     * Сохранить режим темы.
     * edit {} — это suspend-транзакция (атомарная запись).
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    /** Сохранить флаг «напоминания включены». */
    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.REMINDERS_ENABLED] = enabled
        }
    }

    /** Сохранить время напоминания. */
    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
            prefs[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    /** Сохранить флаг «озвучка включена». */
    suspend fun setTtsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.TTS_ENABLED] = enabled
        }
    }
}