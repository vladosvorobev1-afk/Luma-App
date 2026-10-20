package com.example.formamind.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.formamind.ui.screens.CourseListScreen
import com.example.formamind.ui.screens.ExerciseListScreen
import com.example.formamind.ui.screens.ExerciseScreen
import com.example.formamind.ui.screens.HistoryScreen
import com.example.formamind.ui.screens.SettingsScreen
import com.example.formamind.ui.screens.StatsScreen

@Composable
fun AppNavigation(deepLinkExerciseId: String? = null) {
    val navController = rememberNavController()

    // Эффект для обработки Deep Link при запуске
    LaunchedEffect(deepLinkExerciseId) {
        if (deepLinkExerciseId != null) {
            // Добавляем в стек главный экран, чтобы кнопка "Назад" работала корректно
            navController.navigate("course_list") {
                popUpTo("course_list") { inclusive = true }
            }
            // Открываем упражнение
            navController.navigate("exercise/$deepLinkExerciseId")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "course_list"
    ) {
        composable("course_list") {
            CourseListScreen(
                onCourseClick = { courseId -> navController.navigate("exercise_list/$courseId") },
                onHistoryClick = { navController.navigate("history") },
                onStatsClick = { navController.navigate("stats") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        composable(
            route = "exercise_list/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId").orEmpty()
            ExerciseListScreen(
                courseId = courseId,
                onExerciseClick = { exerciseId -> navController.navigate("exercise/$exerciseId") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "exercise/{exerciseId}",
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId").orEmpty()
            ExerciseScreen(
                exerciseId = exerciseId,
                onFinish = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("history") {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable("stats") {
            StatsScreen(onBack = { navController.popBackStack() })
        }
    }
}