package com.example.iurankomplek.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorHandlerTest {

    private val handler = ErrorHandler()

    private fun httpException(code: Int): HttpException = HttpException(
        retrofit2.Response.error<Any>(code, "{}".toResponseBody("application/json".toMediaType()))
    )

    @Test
    fun `unknown host maps to no internet connection`() {
        assertEquals("No internet connection", handler.handleError(UnknownHostException()))
    }

    @Test
    fun `socket timeout maps to connection timeout`() {
        assertEquals("Connection timeout", handler.handleError(SocketTimeoutException()))
    }

    @Test
    fun `401 maps to unauthorized access`() {
        assertEquals("Unauthorized access", handler.handleError(httpException(401)))
    }

    @Test
    fun `403 maps to forbidden`() {
        assertEquals("Forbidden", handler.handleError(httpException(403)))
    }

    @Test
    fun `404 maps to resource not found`() {
        assertEquals("Resource not found", handler.handleError(httpException(404)))
    }

    @Test
    fun `500 maps to server error`() {
        assertEquals("Server error", handler.handleError(httpException(500)))
    }

    @Test
    fun `unmapped http codes keep their status`() {
        assertEquals("HTTP Error: 418", handler.handleError(httpException(418)))
    }

    @Test
    fun `generic io exception maps to a network error`() {
        assertEquals(
            "Network error occurred",
            handler.handleError(IOException("socket closed"))
        )
    }

    @Test
    fun `unknown throwables are surfaced with their message`() {
        assertEquals(
            "An error occurred: something broke",
            handler.handleError(IllegalStateException("something broke"))
        )
    }

    @Test
    fun `handled message is never blank`() {
        val throwables = listOf(
            UnknownHostException(),
            SocketTimeoutException(),
            httpException(401),
            httpException(503),
            IOException("io"),
            RuntimeException("runtime")
        )

        throwables.forEach { throwable ->
            val message = handler.handleError(throwable)
            assertTrue("message for $throwable must not be blank", message.isNotBlank())
        }
    }

    @Test
    fun `http exception is checked before the io exception branch`() {
        assertEquals("Server error", handler.handleError(httpException(500)))
    }
}