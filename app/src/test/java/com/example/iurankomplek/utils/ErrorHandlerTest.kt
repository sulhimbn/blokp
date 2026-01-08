package com.example.iurankomplek.utils

import android.content.Context
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import retrofit2.HttpException
import okhttp3.ResponseBody
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class ErrorHandlerTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val errorHandler = ErrorHandler(context)

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
        val exception = HttpException(response = okhttp3.Response.Builder()
            .code(401)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Unauthorized")
            .build())
        val result = errorHandler.handleError(exception)
        assertEquals("Unauthorized access", result)
    }

    @Test
    fun `handleError should return correct message for 403 HttpException`() {
        val errorResponse = ResponseBody.create(null, "Forbidden")
        val exception = HttpException(response = okhttp3.Response.Builder()
            .code(403)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Forbidden")
            .build())
        val result = errorHandler.handleError(exception)
        assertEquals("Forbidden", result)
    }

    @Test
    fun `handleError should return correct message for 404 HttpException`() {
        val errorResponse = ResponseBody.create(null, "Not Found")
        val exception = HttpException(response = okhttp3.Response.Builder()
            .code(404)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Not Found")
            .build())
        val result = errorHandler.handleError(exception)
        assertEquals("Resource not found", result)
    }

    @Test
    fun `handleError should return correct message for 500 HttpException`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(500)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Internal Server Error")
            .build())
        val result = errorHandler.handleError(exception)
        assertEquals("Server error", result)
    }

    @Test
    fun `handleError should return generic message for unknown HTTP error code`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(418)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("I'm a teapot")
            .build())
        val result = errorHandler.handleError(exception)
        assertEquals("Request failed with status code: 418", result)
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
