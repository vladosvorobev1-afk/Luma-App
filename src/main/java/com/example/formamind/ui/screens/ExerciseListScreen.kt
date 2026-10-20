package com.example.formamind.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.formamind.data.DefaultExercises
import com.example.formamind.data.Exercise
import com.example.formamind.ui.theme.FormaMindTheme

/**
 * Экран упражнений одного курса.
 * Клик по упражнению → запуск экрана выполнения.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    courseId: String,
    onExerciseClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val exercises = DefaultExercises.byCourse(courseId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Упражнения") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        ExerciseListContent(
            exercises = exercises,
            onExerciseClick = onExerciseClick,
            contentPadding = innerPadding
        )
    }
}

@Composable
private fun ExerciseListContent(
    exercises: List<Exercise>,
    onExerciseClick: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    if (exercises.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Упражнений пока нет.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(exercises, key = { it.id }) { exercise ->
            ExerciseCard(
                exercise = exercise,
                onClick = { onExerciseClick(exercise.id) }
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercise.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (exercise.durationSec > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${exercise.durationSec / 60} мин",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseListPreview() {
    FormaMindTheme {
        ExerciseListContent(
            exercises = DefaultExercises.byCourse("anxiety"),
            onExerciseClick = {}
        )
    }
}