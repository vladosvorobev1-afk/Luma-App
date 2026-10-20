package com.example.formamind.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.formamind.data.settings.ThemeMode

/**
 * Тёмная цветовая схема (фиксированная).
 * Используется когда пользователь выбрал "Тёмная" вручную.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/**
 * Светлая цветовая схема (фиксированная).
 * Используется когда пользователь выбрал "Светлая" вручную.
 */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Тема приложения FormaMind.
 *
 * Поддерживает 3 режима через параметр themeMode:
 *  - ThemeMode.SYSTEM — как в системе. Работает dynamic colors на Android 12+
 *  - ThemeMode.LIGHT  — принудительно светлая. Фиксированные цвета
 *  - ThemeMode.DARK   — принудительно тёмная. Фиксированные цвета
 *
 * Почему dynamic colors только в SYSTEM:
 *  - Dynamic colors берут палитру из системных обоев
 *  - Их нельзя заставить быть светлыми, если система в тёмной
 *  - Поэтому в ручных режимах используем фиксированные цвета из Color.kt
 *
 * Параметр darkTheme оставлен для Preview и совместимости.
 */
@Composable
fun FormaMindTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Определяем финальный флаг "тёмная ли тема"
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> darkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Dynamic colors — только в SYSTEM режиме и только на Android 12+
    val useDynamic = themeMode == ThemeMode.SYSTEM &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        useDynamic -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}