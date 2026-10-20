package com.example.formamind.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.formamind.FormaMindApp
import com.example.formamind.data.Course
import com.example.formamind.data.CourseRepository
import com.example.formamind.data.Exercise
import com.example.formamind.data.UserStats // <-- НОВЫЙ ИМПОРТ
import com.example.formamind.data.local.SessionEntity
import com.example.formamind.data.remote.NetworkErrorMapper
import com.example.formamind.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel для экранов курсов, упражнений и истории.
 */
class CourseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CourseRepository(application.applicationContext)
    private val settingsRepository = SettingsRepository(application.applicationContext)

    // ==================== КУРСЫ ====================

    val courses: StateFlow<List<Course>> = repository.getCoursesFlow()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse.asStateFlow()

    // ==================== ТЕКУЩЕЕ УПРАЖНЕНИЕ ====================

    private val _currentExercise = MutableStateFlow<Exercise?>(null)
    val currentExercise: StateFlow<Exercise?> = _currentExercise.asStateFlow()

    // ==================== ИСТОРИЯ ====================

    val sessions: StateFlow<List<SessionEntity>> = repository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    val sessionCount: StateFlow<Int> = repository.getSessionCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = 0
        )

    // ==================== СТАТИСТИКА (НОВОЕ) ====================

    /**
     * Поток со статистикой (всего сессий, время, стрик).
     * UI подписывается на него, и как только мы сохраняем новую сессию,
     * Room автоматически присылает свежие цифры.
     */
    val userStats: StateFlow<UserStats> = repository.observeUserStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = UserStats()
        )

    // ==================== ИНИЦИАЛИЗАЦИЯ ====================

    init {
        refreshFromNetwork()
    }

    // ==================== ОБНОВЛЕНИЕ ИЗ СЕТИ ====================

    private fun refreshFromNetwork() {
        viewModelScope.launch {
            repository.refreshFromNetwork()
                .onSuccess {
                    _errorMessage.value = null
                }
                .onFailure { error ->
                    _errorMessage.value = NetworkErrorMapper.toUserMessage(error)
                }
        }
    }

    fun retryLoad() {
        _errorMessage.value = null
        refreshFromNetwork()
    }

    // ==================== УПРАЖНЕНИЯ ====================

    fun loadExerciseById(id: String) {
        viewModelScope.launch {
            _currentExercise.value = repository.getExerciseById(id)
        }
    }

    fun clearCurrentExercise() {
        _currentExercise.value = null
    }

    fun selectCourse(courseId: String) {
        viewModelScope.launch {
            _selectedCourse.value = repository.getCourseById(courseId)
        }
    }

    // ==================== TTS (ОЗВУЧКА) ====================

    fun speakInstruction(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val enabled = settingsRepository.ttsEnabled.first()
            if (enabled) {
                FormaMindApp.ttsManager?.speak(text)
            }
        }
    }

    fun stopSpeaking() {
        FormaMindApp.ttsManager?.stop()
    }

    // ==================== СЕССИИ ====================

    fun saveSession(
        exerciseId: String,
        exerciseTitle: String,
        courseId: String,
        durationSec: Int,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.saveSession(
                exerciseId = exerciseId,
                exerciseTitle = exerciseTitle,
                courseId = courseId,
                durationSec = durationSec,
                note = note
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}