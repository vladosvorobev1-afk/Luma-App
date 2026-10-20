package com.example.formamind.data.remote

import retrofit2.http.GET

/**
 * Retrofit-интерфейс для получения данных о курсах и упражнениях.
 *
 * Базовый URL задаётся в NetworkModule.
 * Здесь указывается только относительный путь ("courses.json").
 *
 * Пример полного URL:
 * BASE_URL + "courses.json" = https://gist.githubusercontent.com/.../courses.json
 */
interface CourseApi {

    /**
     * Загружает курсы и упражнения с сервера.
     *
     * suspend — потому что это долгая операция, выполняется в фоне.
     * Retrofit сам переключит поток и вернёт результат в корутину.
     */
    @GET("courses.json")
    suspend fun getCourses(): CoursesResponseDto
}