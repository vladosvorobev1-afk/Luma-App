package com.example.formamind.data.settings

/**
 * Режим темы приложения.
 *
 * Три варианта:
 *  - SYSTEM — как в системе (default). Работает dynamic colors на Android 12+
 *  - LIGHT  — принудительно светлая. Fixed color scheme из Color.kt
 *  - DARK   — принудительно тёмная. Fixed color scheme из Color.kt
 *
 * Хранится в DataStore как String (name enum'а).
 * При чтении — безопасный парсинг через fromString().
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        /**
         * Безопасный парсинг из строки.
         * Если значение неизвестно (например, из старой версии) — возвращаем SYSTEM.
         */
        fun fromString(value: String?): ThemeMode =
            runCatching { valueOf(value?.uppercase() ?: "SYSTEM") }
                .getOrDefault(SYSTEM)
    }
}