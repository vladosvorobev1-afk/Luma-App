package com.example.formamind.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Insights // <-- НОВАЯ ИКОНКА
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.formamind.data.Course
import com.example.formamind.data.DefaultCourses
import com.example.formamind.ui.theme.FormaMindTheme
import com.example.formamind.ui.viewmodel.CourseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    onCourseClick: (String) -> Unit,
    onHistoryClick: () -> Unit,
    onStatsClick: () -> Unit, // <-- НОВЫЙ ПАРАМЕТР
    onSettingsClick: () -> Unit,
    viewModel: CourseViewModel = viewModel()
) {
    val courses by viewModel.courses.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Форма") },
                actions = {
                    // НОВАЯ КНОПКА СТАТИСТИКИ
                    IconButton(onClick = onStatsClick) {
                        Icon(
                            imageVector = Icons.Filled.Insights,
                            contentDescription = "Статистика"
                        )
                    }
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "История"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Настройки"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (courses.isEmpty() && errorMessage == null) {
            LoadingContent(contentPadding = innerPadding)
        } else {
            CourseListContent(
                courses = courses,
                onCourseClick = onCourseClick,
                contentPadding = innerPadding,
                errorMessage = errorMessage,
                onRetry = { viewModel.retryLoad() }
            )
        }
    }
}

// ==================== LOADING ====================

@Composable
private fun LoadingContent(
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// ==================== СПИСОК ====================

@Composable
private fun CourseListContent(
    courses: List<Course>,
    onCourseClick: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Выбери курс",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (errorMessage != null && onRetry != null) {
            item {
                ErrorBanner(
                    message = errorMessage,
                    onRetry = onRetry
                )
            }
        }

        items(courses, key = { it.id }) { course ->
            CourseCard(
                course = course,
                onClick = { onCourseClick(course.id) }
            )
        }
    }
}

// ==================== БАННЕР ОШИБКИ ====================

@Composable
private fun ErrorBanner(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}

// ==================== КАРТОЧКА КУРСА ====================

@Composable
private fun CourseCard(
    course: Course,
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
                text = "${course.emoji}  ${course.title}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${course.exercisesCount} упражнений",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ==================== PREVIEW ====================

@Preview(showBackground = true)
@Composable
private fun CourseListPreview() {
    FormaMindTheme {
        CourseListContent(
            courses = DefaultCourses.all,
            onCourseClick = {}
        )
    }
}