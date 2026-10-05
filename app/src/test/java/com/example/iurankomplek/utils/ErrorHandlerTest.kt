package com.example.iurankomplek.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorHandlerTest {

    private val errorHandler = ErrorHandler()

    private fun httpException(code: Int): HttpException =
        HttpException(retrofit2.Response.error<Any>(code, "".toResponseBody("text/plain".toMediaType())))

    @Test
    fun `handleError should return correct message for UnknownHostException`() {
        assertEquals("No internet connection", errorHandler.handleError(UnknownHostException()))
    }

    @Test
    fun `handleError should return correct message for SocketTimeoutException`() {
        assertEquals("Connection timeout", errorHandler.handleError(SocketTimeoutException()))
    }

    @Test
    fun `handleError should return correct message for 401 HttpException`() {
        assertEquals("Unauthorized access", errorHandler.handleError(httpException(401)))
    }

    @Test
    fun `handleError should return correct message for 403 HttpException`() {
        assertEquals("Forbidden", errorHandler.handleError(httpException(403)))
    }

    @Test
    fun `handleError should return correct message for 404 HttpException`() {
        assertEquals("Resource not found", errorHandler.handleError(httpException(404)))
    }

    @Test
    fun `handleError should return correct message for 500 HttpException`() {
        assertEquals("Server error", errorHandler.handleError(httpException(500)))
    }

    @Test
    fun `handleError should return generic message for unknown HTTP error code`() {
        assertEquals("HTTP Error: 418", errorHandler.handleError(httpException(418)))
    }

    @Test
    fun `handleError should return correct message for generic IOException`() {
        assertEquals("Network error occurred", errorHandler.handleError(IOException("File not found")))
    }

    @Test
    fun `handleError should return correct message for IOException without message`() {
        assertEquals("Network error occurred", errorHandler.handleError(IOException()))
    }

    @Test
    fun `handleError should return generic error message for unknown exception`() {
        assertEquals(
            "An error occurred: Something went wrong",
            errorHandler.handleError(RuntimeException("Something went wrong"))
        )
    }

    @Test
    fun `handleError should return generic error message for exception with null message`() {
        assertEquals("An error occurred: null", errorHandler.handleError(NullPointerException()))
    }

    @Test
    fun `handleError should return generic error message for IllegalArgumentException`() {
        assertEquals(
            "An error occurred: Invalid argument",
            errorHandler.handleError(IllegalArgumentException("Invalid argument"))
        )
    }

    @Test
    fun `handleError should return generic error message for IllegalStateException`() {
        assertEquals(
            "An error occurred: Invalid state",
            errorHandler.handleError(IllegalStateException("Invalid state"))
        )
    }
}