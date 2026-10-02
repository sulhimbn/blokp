package com.example.iurankomplek.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorHandlerTest {

    private val errorHandler = ErrorHandler()

    private fun httpException(code: Int, body: String): HttpException =
        HttpException(Response.error<Any>(code, body.toResponseBody("text/plain".toMediaType())))

    @Test
    fun `handleError should return correct message for UnknownHostException`() {
        val exception = UnknownHostException()
        val result = errorHandler.handleError(exception)
        assertEquals("No internet connection", result)
    }

    @Test
    fun `handleError should return correct message for SocketTimeoutException`() {
        val exception = SocketTimeoutException()
        val result = errorHandler.handleError(exception)
        assertEquals("Connection timeout", result)
    }

    @Test
    fun `handleError should return correct message for 401 HttpException`() {
        val result = errorHandler.handleError(httpException(401, "Unauthorized"))
        assertEquals("Unauthorized access", result)
    }

    @Test
    fun `handleError should return correct message for 403 HttpException`() {
        val result = errorHandler.handleError(httpException(403, "Forbidden"))
        assertEquals("Forbidden", result)
    }

    @Test
    fun `handleError should return correct message for 404 HttpException`() {
        val result = errorHandler.handleError(httpException(404, "Not Found"))
        assertEquals("Resource not found", result)
    }

    @Test
    fun `handleError should return correct message for 500 HttpException`() {
        val result = errorHandler.handleError(httpException(500, "Internal Server Error"))
        assertEquals("Server error", result)
    }

    @Test
    fun `handleError should return generic message for unknown HTTP error code`() {
        val result = errorHandler.handleError(httpException(418, "I'm a teapot"))
        assertEquals("HTTP Error: 418", result)
    }

    @Test
    fun `handleError should return correct message for generic IOException`() {
        val exception = IOException("File not found")
        val result = errorHandler.handleError(exception)
        assertEquals("Network error occurred", result)
    }

    @Test
    fun `handleError should return correct message for IOException without message`() {
        val exception = IOException()
        val result = errorHandler.handleError(exception)
        assertEquals("Network error occurred", result)
    }

    @Test
    fun `handleError should return generic error message for unknown exception`() {
        val exception = RuntimeException("Something went wrong")
        val result = errorHandler.handleError(exception)
        assertEquals("An error occurred: Something went wrong", result)
    }

    @Test
    fun `handleError should return generic error message for exception with null message`() {
        val exception = NullPointerException()
        val result = errorHandler.handleError(exception)
        assertEquals("An error occurred: null", result)
    }

    @Test
    fun `handleError should return generic error message for IllegalArgumentException`() {
        val exception = IllegalArgumentException("Invalid argument")
        val result = errorHandler.handleError(exception)
        assertEquals("An error occurred: Invalid argument", result)
    }

    @Test
    fun `handleError should return generic error message for IllegalStateException`() {
        val exception = IllegalStateException("Invalid state")
        val result = errorHandler.handleError(exception)
        assertEquals("An error occurred: Invalid state", result)
    }
}