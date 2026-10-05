package com.example.iurankomplek.utils

import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import okhttp3.ResponseBody
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.io.IOException

class ErrorHandlerTest {

    private val errorHandler = ErrorHandler()

    private fun httpException(code: Int, message: String): HttpException {
        val body = okhttp3.ResponseBody.create(null, message)
        return HttpException(retrofit2.Response.error<Any>(code, body))
    }

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
        val errorResponse = ResponseBody.create(null, "Unauthorized")
        val exception = httpException(401, "Unauthorized")
        val result = errorHandler.handleError(exception)
        assertEquals("Unauthorized access", result)
    }

    @Test
    fun `handleError should return correct message for 403 HttpException`() {
        val errorResponse = ResponseBody.create(null, "Forbidden")
        val exception = httpException(403, "Forbidden")
        val result = errorHandler.handleError(exception)
        assertEquals("Forbidden", result)
    }

    @Test
    fun `handleError should return correct message for 404 HttpException`() {
        val errorResponse = ResponseBody.create(null, "Not Found")
        val exception = httpException(404, "Not Found")
        val result = errorHandler.handleError(exception)
        assertEquals("Resource not found", result)
    }

    @Test
    fun `handleError should return correct message for 500 HttpException`() {
        val errorResponse = ResponseBody.create(null, "Internal Server Error")
        val exception = httpException(500, "Internal Server Error")
        val result = errorHandler.handleError(exception)
        assertEquals("Server error", result)
    }

    @Test
    fun `handleError should return generic message for unknown HTTP error code`() {
        val errorResponse = ResponseBody.create(null, "I'm a teapot")
        val exception = httpException(418, "I'm a teapot")
        val result = errorHandler.handleError(exception)
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
