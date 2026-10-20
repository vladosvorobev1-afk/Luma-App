package com.example.formamind.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Сетевой модуль приложения.
 *
 * Здесь собирается всё, что нужно для работы с сетью:
 *   1. Json-парсер (kotlinx.serialization)
 *   2. OkHttp-клиент с таймаутами и логгером
 *   3. Retrofit
 *   4. Готовый CourseApi
 *
 * Это singleton — создаётся один раз за жизнь приложения.
 * Классический паттерн "ручной DI" (без Hilt) для небольших проектов.
 */
object NetworkModule {

    /**
     * Базовый URL. ВАЖНО: должен заканчиваться на "/".
     *
     * Retrofit склеивает BASE_URL + относительный путь из @GET("...").
     * Например: BASE_URL + "courses.json".
     */
    private const val BASE_URL =
        "https://gist.githubusercontent.com/vladosvorobev1-afk/" +
                "8608caadd5bc64c27947d71445e54bea/raw/"

    /**
     * Json-парсер.
     *
     * ignoreUnknownKeys = true — если с сервера придёт поле, которого
     * нет в DTO, парсер не упадёт, а просто пропустит его.
     * Это правильная настройка для продакшена.
     */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * OkHttp-клиент с таймаутами и логированием.
     *
     * Таймауты — 30 секунд. Этого хватит с запасом для Gist.
     * Логгер покажет в Logcat все запросы и ответы — удобно для отладки.
     * Уровень BODY — показывать тело запроса/ответа.
     */
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Retrofit-инстанс.
     *
     * Здесь подключаем kotlinx.serialization как конвертер:
     * он автоматически превращает JSON в наши DTO-классы.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    /**
     * Готовый API-клиент.
     * Используется в CourseRepository — там мы будем звать getCourses().
     */
    val courseApi: CourseApi by lazy {
        retrofit.create(CourseApi::class.java)
    }
}