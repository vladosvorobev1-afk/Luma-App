package com.example.formamind.data.remote

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Превращает технические исключения сети в понятный для пользователя текст.
 *
 * Зачем это нужно:
 *  - Пользователь не должен видеть "UnknownHostException" — это stack trace
 *  - Правильный UX: короткое сообщение + предложение действия
 *
 * Куда идут разные типы ошибок:
 *  - UnknownHostException / ConnectException → нет интернета
 *  - SocketTimeoutException → сервер не отвечает
 *  - HttpException (4xx/5xx) → ошибка на стороне сервера
 *  - Всё остальное → "что-то пошло не так"
 */
object NetworkErrorMapper {

    /**
     * Превращает Throwable в человеческий текст для UI.
     *
     * @param throwable исключение, которое упало в runCatching
     * @return короткая фраза для показа в баннере
     */
    fun toUserMessage(throwable: Throwable): String = when (throwable) {

        is UnknownHostException -> "Нет подключения к интернету"
        is java.net.ConnectException -> "Нет подключения к интернету"

        is SocketTimeoutException -> "Превышено время ожидания. Попробуйте позже"

        is HttpException -> when (throwable.code()) {
            in 400..499 -> "Ошибка запроса (${throwable.code()})"
            in 500..599 -> "Ошибка сервера. Попробуйте позже"
            else -> "Ошибка сети (${throwable.code()})"
        }

        is IOException -> "Проблема с сетью. Проверьте подключение"

        else -> "Что-то пошло не так. Попробуйте ещё раз"
    }
}