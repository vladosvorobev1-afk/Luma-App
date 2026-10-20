package com.example.formamind.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.formamind.data.ExerciseType
import com.example.formamind.ui.viewmodel.CourseViewModel
import kotlinx.coroutines.delay

/**
 * Экран выполнения упражнения.
 *
 * Таймер (BREATHING / TIMER):
 *  - НЕ запускается автоматически
 *  - Кнопка ▶ Старт — запускает отсчёт
 *  - Кнопка ⏸ Пауза — приостанавливает
 *  - Кнопка ▶ Продолжить — возобновляет
 *  - Кнопка 🔄 Сброс — обнуляет
 *  - При 00:00 — «Готово!»
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    exerciseId: String,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    viewModel: CourseViewModel = viewModel()
) {
    LaunchedEffect(exerciseId) {
        viewModel.loadExerciseById(exerciseId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSpeaking()
            viewModel.clearCurrentExercise()
        }
    }

    val exercise by viewModel.currentExercise.collectAsState()

    if (exercise == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Упражнение") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Загрузка…")
            }
        }
        return
    }

    val currentExercise = exercise ?: return

    LaunchedEffect(currentExercise.id) {
        viewModel.speakInstruction(currentExercise.instruction)
    }

    var noteText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentExercise.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.speakInstruction(currentExercise.instruction)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = "Озвучить"
                        )
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
            Text(
                text = currentExercise.description,
                style = MaterialTheme.typography.titleMedium
            )

            // ==================== ИНСТРУКЦИЯ ====================

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Инструкция",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            viewModel.speakInstruction(currentExercise.instruction)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = "Озвучить инструкцию",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentExercise.instruction,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // ==================== ТАЙМЕР ====================

            when (currentExercise.type) {
                ExerciseType.BREATHING, ExerciseType.TIMER -> {
                    TimerBlock(totalSeconds = currentExercise.durationSec)
                }
                ExerciseType.JOURNAL -> {
                    JournalBlock(
                        text = noteText,
                        onTextChange = { noteText = it }
                    )
                }
                ExerciseType.TEXT -> {
                    // Для TEXT дополнительный блок не нужен
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.stopSpeaking()
                    viewModel.saveSession(
                        exerciseId = currentExercise.id,
                        exerciseTitle = currentExercise.title,
                        courseId = currentExercise.courseId,
                        durationSec = currentExercise.durationSec,
                        note = noteText
                    )
                    onFinish()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Готово")
            }
        }
    }
}

// ==================== ТАЙМЕР С КОНТРОЛЕМ ====================

/**
 * Управляемый таймер.
 *
 * Состояния:
 *  - Idle (не запущен) — кнопка ▶ Старт
 *  - Running (идёт)     — кнопка ⏸ Пауза
 *  - Paused (пауза)     — кнопка ▶ Продолжить
 *  - Finished (00:00)   — «Готово!»
 *
 * Кнопка 🔄 Сброс доступна всегда (кроме Finished).
 */
@Composable
private fun TimerBlock(totalSeconds: Int) {
    var secondsLeft by remember { mutableIntStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    val isFinished = secondsLeft == 0
    val isIdle = secondsLeft == totalSeconds && !isRunning

    // Тикаем только когда isRunning = true.
    // Ключ LaunchedEffect — isRunning, чтобы перезапускать при паузе.
    LaunchedEffect(isRunning) {
        while (isRunning && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
        // Если досчитали до нуля — сбрасываем isRunning.
        if (secondsLeft == 0) {
            isRunning = false
        }
    }

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Таймер",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val minutes = secondsLeft / 60
            val seconds = secondsLeft % 60
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )

            if (isFinished) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Готово!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== КНОПКИ ====================

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Основная кнопка: Старт / Пауза / Продолжить
                if (!isFinished) {
                    Button(
                        onClick = { isRunning = !isRunning }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                isRunning -> "Пауза"
                                isIdle -> "Старт"
                                else -> "Продолжить"
                            }
                        )
                    }
                }

                // Кнопка сброса — доступна, если таймер не в исходном состоянии.
                if (!isIdle && !isFinished) {
                    OutlinedButton(
                        onClick = {
                            secondsLeft = totalSeconds
                            isRunning = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Если finished — можно только сбросить.
                if (isFinished) {
                    OutlinedButton(
                        onClick = {
                            secondsLeft = totalSeconds
                            isRunning = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Заново")
                    }
                }
            }
        }
    }
}

// ==================== ЖУРНАЛ ====================

@Composable
private fun JournalBlock(
    text: String,
    onTextChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Твоя запись",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            placeholder = { Text("Напиши здесь…") }
        )
    }
}