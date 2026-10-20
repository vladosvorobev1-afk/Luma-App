package com.example.formamind

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.formamind.data.settings.SettingsRepository
import com.example.formamind.data.settings.ThemeMode
import com.example.formamind.ui.navigation.AppNavigation
import com.example.formamind.ui.theme.FormaMindTheme
import kotlinx.coroutines.flow.stateIn

class MainActivity : ComponentActivity() {

    /**
     * Launcher для запроса POST_NOTIFICATIONS.
     * На уровне класса — обязательно до onCreate().
     */
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* результат не важен */ }

    override fun onCreate(savedInstanceState: Bundle?) {

        // ⚠️ ВАЖНО: installSplashScreen() ДО super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermission()

        // Читаем тему из DataStore один раз при старте.
        val settingsRepo = SettingsRepository(applicationContext)
        val themeModeFlow = settingsRepo.themeMode.stateIn(
            scope = lifecycleScope,
            started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

        setContent {
            val themeMode by themeModeFlow.collectAsState()
            val lifecycleOwner = LocalLifecycleOwner.current

            // Стейт для хранения ID упражнения из Deep Link
            var deepLinkExerciseId by remember { mutableStateOf<String?>(null) }

            // Этот эффект слушает жизненный цикл. Каждый раз, когда приложение
            // выходит на передний план (RESUMED), мы проверяем Intent на наличие ID.
            LaunchedEffect(lifecycleOwner) {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    deepLinkExerciseId = getExerciseIdFromIntent(intent)
                }
            }

            FormaMindTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(deepLinkExerciseId = deepLinkExerciseId)
                }
            }
        }
    }

    // Переопределяем метод, чтобы Activity обновляла Intent при клике на уведомление
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Важно: обновляем intent, чтобы getIntent() возвращал новый
    }

    // Вспомогательная функция для извлечения ID из Intent
    private fun getExerciseIdFromIntent(intent: Intent?): String? {
        return intent?.getStringExtra("EXTRA_EXERCISE_ID")
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }
}