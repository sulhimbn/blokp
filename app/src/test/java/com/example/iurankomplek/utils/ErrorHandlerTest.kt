package com.example.iurankomplek.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class ErrorHandlerTest {

    private val handler = ErrorHandler()

    private fun httpException(code: Int) =
        HttpException(retrofit2.Response.error<Any>(code, ResponseBody.create(null, "")))

    @Test
    fun `UnknownHostException becomes No internet connection`() {
        assertEquals("No internet connection", handler.handleError(UnknownHostException("api.example")))
    }

    @Test
    fun `SocketTimeoutException becomes Connection timeout`() {
        assertEquals("Connection timeout", handler.handleError(SocketTimeoutException("read timed out")))
    }

    @Test
    fun `other IOExceptions become Network error occurred`() {
        assertEquals(
            "Network error occurred",
            handler.handleError(IOException("connection reset"))
        )
    }

    @Test
    fun `401 becomes Unauthorized access`() {
        assertEquals("Unauthorized access", handler.handleError(httpException(401)))
    }

    @Test
    fun `403 becomes Forbidden`() {
        assertEquals("Forbidden", handler.handleError(httpException(403)))
    }

    @Test
    fun `404 becomes Resource not found`() {
        assertEquals("Resource not found", handler.handleError(httpException(404)))
    }

    @Test
    fun `500 becomes Server error`() {
        assertEquals("Server error", handler.handleError(httpException(500)))
    }

    @Test
    fun `any other HTTP status keeps the numeric code`() {
        assertEquals("HTTP Error: 418", handler.handleError(httpException(418)))
        assertEquals("HTTP Error: 503", handler.handleError(httpException(503)))
    }

    @Test
    fun `an SSL failure is reported as a network error`() {
        assertEquals("Network error occurred", handler.handleError(SSLException("handshake failed")))
    }

    @Test
    fun `an unknown throwable is reported with its message`() {
        assertEquals("An error occurred: boom", handler.handleError(IllegalStateException("boom")))
    }

    @Test
    fun `an unknown throwable with no message still produces a readable string`() {
        assertEquals("An error occurred: null", handler.handleError(IllegalStateException()))
    }
}
