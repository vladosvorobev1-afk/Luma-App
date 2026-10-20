package com.example.formamind.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.formamind.data.settings.ThemeMode
import com.example.formamind.ui.theme.FormaMindTheme
import com.example.formamind.ui.viewmodel.SettingsViewModel

/**
 * Экран настроек.
 *
 * Блоки:
 *  1. Напоминания (Switch + выбор времени)
 *  2. Озвучка TTS (Switch)
 *  3. Тема (3 RadioButton: Системная / Светлая / Тёмная)
 *  4. История (кнопка «Очистить» с подтверждением)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val remindersEnabled by viewModel.remindersEnabled.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()
    val ttsEnabled by viewModel.ttsEnabled.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==================== НАПОМИНАНИЯ ====================

            SettingsSection(title = "🔔  Напоминания") {
                SwitchRow(
                    title = "Включить напоминания",
                    checked = remindersEnabled,
                    onCheckedChange = { viewModel.setRemindersEnabled(it) }
                )

                if (remindersEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeRow(
                        hour = reminderHour,
                        minute = reminderMinute,
                        onChangeTime = { h, m -> viewModel.setReminderTime(h, m) }
                    )
                }
            }

            // ==================== TTS ====================

            SettingsSection(title = "🔊  Озвучка инструкций") {
                SwitchRow(
                    title = "Проговаривать инструкции",
                    checked = ttsEnabled,
                    onCheckedChange = { viewModel.setTtsEnabled(it) }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Удобно для дыхательных практик — глаза закрыты, а инструкция читается.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ==================== ТЕМА ====================

            SettingsSection(title = "🎨  Тема") {
                ThemeOption(
                    label = "Системная",
                    selected = themeMode == ThemeMode.SYSTEM,
                    onSelect = { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                )
                ThemeOption(
                    label = "Светлая",
                    selected = themeMode == ThemeMode.LIGHT,
                    onSelect = { viewModel.setThemeMode(ThemeMode.LIGHT) }
                )
                ThemeOption(
                    label = "Тёмная",
                    selected = themeMode == ThemeMode.DARK,
                    onSelect = { viewModel.setThemeMode(ThemeMode.DARK) }
                )
            }

            // ==================== ИСТОРИЯ ====================

            SettingsSection(title = "📊  История") {
                Text(
                    text = "Очистить все записи о выполненных упражнениях.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Очистить историю",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ==================== ДИАЛОГ ПОДТВЕРЖДЕНИЯ ====================

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Очистить историю?") },
            text = { Text("Все записи будут удалены. Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// ==================== ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ ====================

/**
 * Секция с заголовком — обёртка над Card.
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Строка со Switch (заголовок + переключатель).
 */
@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Строка выбора времени напоминания.
 * Простой вариант — 3 кнопки: −1 час, текущее, +1 час.
 * (Полноценный TimePicker отложим — для MVP достаточно.)
 */
@Composable
private fun TimeRow(
    hour: Int,
    minute: Int,
    onChangeTime: (Int, Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Время",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = {
            val newHour = if (hour == 0) 23 else hour - 1
            onChangeTime(newHour, minute)
        }) {
            Text("−1")
        }
        Text(
            text = "%02d:%02d".format(hour, minute),
            style = MaterialTheme.typography.titleLarge
        )
        TextButton(onClick = {
            val newHour = if (hour == 23) 0 else hour + 1
            onChangeTime(newHour, minute)
        }) {
            Text("+1")
        }
    }
}

/**
 * Строка выбора темы — RadioButton + label.
 */
@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// ==================== PREVIEW ====================

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    FormaMindTheme {
        SettingsScreen(onBack = {})
    }
}