package com.example.formamind.data.remote

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Unit-тесты для NetworkErrorMapper.
 */
class NetworkErrorMapperTest {

    @Test
    fun `UnknownHostException maps to no internet message`() {
        val error = UnknownHostException("gist.githubusercontent.com")
        assertEquals(
            "Нет подключения к интернету",
            NetworkErrorMapper.toUserMessage(error)
        )
    }

    @Test
    fun `ConnectException maps to no internet message`() {
        val error = java.net.ConnectException("Connection refused")
        assertEquals(
            "Нет подключения к интернету",
            NetworkErrorMapper.toUserMessage(error)
        )
    }

    @Test
    fun `SocketTimeoutException maps to timeout message`() {
        val error = SocketTimeoutException("timeout")
        assertEquals(
            "Превышено время ожидания. Попробуйте позже",
            NetworkErrorMapper.toUserMessage(error)
        )
    }

    @Test
    fun `HttpException 500 maps to server error message`() {
        val error = httpException(500)
        assertEquals(
            "Ошибка сервера. Попробуйте позже",
            NetworkErrorMapper.toUserMessage(error)
        )
    }

    @Test
    fun `HttpException 503 maps to server error message`() {
        val error = httpException(503)
        assertEquals(
            "Ошибка сервера. Попробуйте позже",
            NetworkErrorMapper.toUserMessage(error)
        )
    }

    @Test
    fun `HttpException 404 maps to client error with code`() {
        val error = httpException(404)
        assertEquals(
            "Ошибка запроса (404)",
            NetworkErrorMapper.toUserMessage(error)
        )
    }

    @Test
    fun `HttpException 403 maps to client error with code`() {
        val error = httpException(403)
        assertEquals(
            "Ошибка запроса (403)",
            NetworkErrorMapper.toUserMessage(error)
        )
    }

    @Test
    fun `IOException maps to network problem message`() {
        val error = IOException("Socket closed")
        assertEquals(
            "Проблема с сетью. Проверьте подключение",
            NetworkErrorMapper.toUserMessage(error)
        )
    }

    @Test
    fun `RuntimeException maps to generic message`() {
        val error = RuntimeException("Something bad")
        assertEquals(
            "Что-то пошло не так. Попробуйте ещё раз",
            NetworkErrorMapper.toUserMessage(error)
        )
    }

    @Test
    fun `IllegalStateException maps to generic message`() {
        val error = IllegalStateException("Invalid state")
        assertEquals(
            "Что-то пошло не так. Попробуйте ещё раз",
            NetworkErrorMapper.toUserMessage(error)
        )
    }

    private fun httpException(code: Int): HttpException {
        val response = Response.error<Any>(
            code,
            "error body".toResponseBody(null)
        )
        return HttpException(response)
    }
}