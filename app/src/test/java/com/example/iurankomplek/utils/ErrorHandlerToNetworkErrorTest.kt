package com.example.iurankomplek.utils

import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.model.ApiErrorCode
import com.example.iurankomplek.network.resilience.CircuitBreakerException
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
class ErrorHandlerToNetworkErrorTest {

    private val errorHandler = ErrorHandler(RuntimeEnvironment.getApplication())

    @Test
    fun `toNetworkError should return ConnectionError for UnknownHostException`() {
        val exception = UnknownHostException()
        val result = errorHandler.toNetworkError(exception)

        assertTrue("Should be ConnectionError", result is NetworkError.ConnectionError)
        assertEquals("No internet connection", (result as NetworkError.ConnectionError).userMessage)
    }

    @Test
    fun `toNetworkError should return TimeoutError for SocketTimeoutException`() {
        val exception = SocketTimeoutException()
        val result = errorHandler.toNetworkError(exception)

        assertTrue("Should be TimeoutError", result is NetworkError.TimeoutError)
        assertEquals("Connection timeout", (result as NetworkError.TimeoutError).userMessage)
    }

    @Test
    fun `toNetworkError should return CircuitBreakerError for CircuitBreakerException`() {
        val exception = CircuitBreakerException("Service unavailable")
        val result = errorHandler.toNetworkError(exception)

        assertTrue("Should be CircuitBreakerError", result is NetworkError.CircuitBreakerError)
        val error = result as NetworkError.CircuitBreakerError
        assertEquals(ApiErrorCode.SERVICE_UNAVAILABLE, error.code)
        assertEquals("Service unavailable", error.userMessage)
    }

    @Test
    fun `toNetworkError should return CircuitBreakerError with custom message`() {
        val exception = CircuitBreakerException("Circuit breaker open")
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.CircuitBreakerError
        assertEquals("Circuit breaker open", error.userMessage)
    }

    @Test
    fun `toNetworkError should return CircuitBreakerError with null message`() {
        val exception = CircuitBreakerException(null)
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.CircuitBreakerError
        assertEquals("Service unavailable", error.userMessage)
    }

    @Test
    fun `toNetworkError should return HttpError with 400 code`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(400)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Bad Request")
            .build())

        val result = errorHandler.toNetworkError(exception)

        assertTrue("Should be HttpError", result is NetworkError.HttpError)
        val error = result as NetworkError.HttpError
        assertEquals(ApiErrorCode.BAD_REQUEST, error.code)
        assertEquals(400, error.httpCode)
        assertEquals("HTTP error", error.userMessage)
    }

    @Test
    fun `toNetworkError should return HttpError with 401 code`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(401)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Unauthorized")
            .build())

        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.HttpError
        assertEquals(ApiErrorCode.UNAUTHORIZED, error.code)
        assertEquals(401, error.httpCode)
    }

    @Test
    fun `toNetworkError should return HttpError with 403 code`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(403)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Forbidden")
            .build())

        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.HttpError
        assertEquals(ApiErrorCode.FORBIDDEN, error.code)
        assertEquals(403, error.httpCode)
    }

    @Test
    fun `toNetworkError should return HttpError with 404 code`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(404)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Not Found")
            .build())

        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.HttpError
        assertEquals(ApiErrorCode.NOT_FOUND, error.code)
        assertEquals(404, error.httpCode)
    }

    @Test
    fun `toNetworkError should return HttpError with 500 code`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(500)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Internal Server Error")
            .build())

        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.HttpError
        assertEquals(ApiErrorCode.INTERNAL_SERVER_ERROR, error.code)
        assertEquals(500, error.httpCode)
    }

    @Test
    fun `toNetworkError should return HttpError with 503 code`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(503)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Service Unavailable")
            .build())

        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.HttpError
        assertEquals(ApiErrorCode.SERVICE_UNAVAILABLE, error.code)
        assertEquals(503, error.httpCode)
    }

    @Test
    fun `toNetworkError should return HttpError with 429 code`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(429)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Too Many Requests")
            .build())

        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.HttpError
        assertEquals(ApiErrorCode.TOO_MANY_REQUESTS, error.code)
        assertEquals(429, error.httpCode)
    }

    @Test
    fun `toNetworkError should return HttpError with unknown 4xx code`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(418)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("I'm a teapot")
            .build())

        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.HttpError
        assertEquals(ApiErrorCode.UNKNOWN_ERROR, error.code)
        assertEquals(418, error.httpCode)
    }

    @Test
    fun `toNetworkError should return HttpError with unknown 5xx code`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(599)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Unknown Error")
            .build())

        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.HttpError
        assertEquals(ApiErrorCode.UNKNOWN_ERROR, error.code)
        assertEquals(599, error.httpCode)
    }

    @Test
    fun `toNetworkError should return HttpError with custom message`() {
        val exception = HttpException(okhttp3.Response.Builder()
            .code(500)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Custom error message")
            .build())

        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.HttpError
        assertEquals("Custom error message", error.userMessage)
    }

    @Test
    fun `toNetworkError should return ConnectionError for IOException`() {
        val exception = IOException("Network error")
        val result = errorHandler.toNetworkError(exception)

        assertTrue("Should be ConnectionError", result is NetworkError.ConnectionError)
        assertEquals("Network error occurred", (result as NetworkError.ConnectionError).userMessage)
    }

    @Test
    fun `toNetworkError should return ConnectionError for IOException without message`() {
        val exception = IOException()
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.ConnectionError
        assertEquals("Network error occurred", error.userMessage)
    }

    @Test
    fun `toNetworkError should return UnknownNetworkError for RuntimeException`() {
        val exception = RuntimeException("Something went wrong")
        val result = errorHandler.toNetworkError(exception)

        assertTrue("Should be UnknownNetworkError", result is NetworkError.UnknownNetworkError)
        val error = result as NetworkError.UnknownNetworkError
        assertEquals(ApiErrorCode.UNKNOWN_ERROR, error.code)
        assertEquals("Something went wrong", error.userMessage)
    }

    @Test
    fun `toNetworkError should return UnknownNetworkError for NullPointerException`() {
        val exception = NullPointerException()
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.UnknownNetworkError
        assertEquals(ApiErrorCode.UNKNOWN_ERROR, error.code)
        assertEquals("null", error.userMessage)
    }

    @Test
    fun `toNetworkError should return UnknownNetworkError for IllegalArgumentException`() {
        val exception = IllegalArgumentException("Invalid argument")
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.UnknownNetworkError
        assertEquals("Invalid argument", error.userMessage)
    }

    @Test
    fun `toNetworkError should return UnknownNetworkError for IllegalStateException`() {
        val exception = IllegalStateException("Invalid state")
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.UnknownNetworkError
        assertEquals("Invalid state", error.userMessage)
    }

    @Test
    fun `toNetworkError should return UnknownNetworkError for custom exception`() {
        class CustomException(message: String) : Exception(message)

        val exception = CustomException("Custom error")
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.UnknownNetworkError
        assertEquals("Custom error", error.userMessage)
    }

    @Test
    fun `toNetworkError should return UnknownNetworkError with null message`() {
        val exception = RuntimeException(null)
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.UnknownNetworkError
        assertEquals("null", error.userMessage)
    }

    @Test
    fun `toNetworkError should return UnknownNetworkError with empty message`() {
        val exception = RuntimeException("")
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.UnknownNetworkError
        assertEquals("", error.userMessage)
    }

    @Test
    fun `toNetworkError should map all 4xx error codes correctly`() {
        val errorCodes = mapOf(
            400 to ApiErrorCode.BAD_REQUEST,
            401 to ApiErrorCode.UNAUTHORIZED,
            403 to ApiErrorCode.FORBIDDEN,
            404 to ApiErrorCode.NOT_FOUND,
            405 to ApiErrorCode.UNKNOWN_ERROR,
            409 to ApiErrorCode.UNKNOWN_ERROR,
            429 to ApiErrorCode.TOO_MANY_REQUESTS,
            422 to ApiErrorCode.UNKNOWN_ERROR
        )

        errorCodes.forEach { (httpCode, expectedApiCode) ->
            val exception = HttpException(okhttp3.Response.Builder()
                .code(httpCode)
                .request(okhttp3.Request.Builder().url("https://test.com").build())
                .message("Error $httpCode")
                .build())

            val result = errorHandler.toNetworkError(exception)
            val error = result as NetworkError.HttpError

            assertEquals("HTTP $httpCode should map to $expectedApiCode", expectedApiCode, error.code)
            assertEquals("HTTP code should be $httpCode", httpCode, error.httpCode)
        }
    }

    @Test
    fun `toNetworkError should map all 5xx error codes correctly`() {
        val errorCodes = mapOf(
            500 to ApiErrorCode.INTERNAL_SERVER_ERROR,
            502 to ApiErrorCode.BAD_GATEWAY,
            503 to ApiErrorCode.SERVICE_UNAVAILABLE,
            504 to ApiErrorCode.GATEWAY_TIMEOUT,
            501 to ApiErrorCode.UNKNOWN_ERROR,
            599 to ApiErrorCode.UNKNOWN_ERROR
        )

        errorCodes.forEach { (httpCode, expectedApiCode) ->
            val exception = HttpException(okhttp3.Response.Builder()
                .code(httpCode)
                .request(okhttp3.Request.Builder().url("https://test.com").build())
                .message("Error $httpCode")
                .build())

            val result = errorHandler.toNetworkError(exception)
            val error = result as NetworkError.HttpError

            assertEquals("HTTP $httpCode should map to $expectedApiCode", expectedApiCode, error.code)
            assertEquals("HTTP code should be $httpCode", httpCode, error.httpCode)
        }
    }

    @Test
    fun `toNetworkError should handle HttpException with custom error body`() {
        val errorBody = ResponseBody.create(null, "{\"error\":\"Custom error\"}")
        val exception = HttpException(okhttp3.Response.Builder()
            .code(400)
            .request(okhttp3.Request.Builder().url("https://test.com").build())
            .message("Bad Request")
            .build())

        val result = errorHandler.toNetworkError(exception)
        val error = result as NetworkError.HttpError

        assertEquals("HTTP error", error.userMessage)
        assertEquals(400, error.httpCode)
    }

    @Test
    fun `toNetworkError should handle SocketTimeoutException with custom message`() {
        val exception = SocketTimeoutException("Custom timeout message")
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.TimeoutError
        assertEquals("Connection timeout", error.userMessage)
    }

    @Test
    fun `toNetworkError should handle UnknownHostException with custom message`() {
        val exception = UnknownHostException("Custom host error")
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.ConnectionError
        assertEquals("No internet connection", error.userMessage)
    }

    @Test
    fun `toNetworkError should handle IOException with custom message`() {
        val exception = IOException("Custom IO error")
        val result = errorHandler.toNetworkError(exception)

        val error = result as NetworkError.ConnectionError
        assertEquals("Network error occurred", error.userMessage)
    }

    @Test
    fun `toNetworkError should consistently return same type for same exception`() {
        val exception1 = RuntimeException("Error 1")
        val exception2 = RuntimeException("Error 2")
        val exception3 = RuntimeException("Error 3")

        val result1 = errorHandler.toNetworkError(exception1)
        val result2 = errorHandler.toNetworkError(exception2)
        val result3 = errorHandler.toNetworkError(exception3)

        assertTrue(result1 is NetworkError.UnknownNetworkError)
        assertTrue(result2 is NetworkError.UnknownNetworkError)
        assertTrue(result3 is NetworkError.UnknownNetworkError)
    }

    @Test
    fun `toNetworkError should return all NetworkError subtypes`() {
        val exceptionsAndTypes = listOf(
            UnknownHostException() to NetworkError.ConnectionError::class.java,
            SocketTimeoutException() to NetworkError.TimeoutError::class.java,
            CircuitBreakerException("Test") to NetworkError.CircuitBreakerError::class.java,
            IOException("Test") to NetworkError.ConnectionError::class.java,
            RuntimeException("Test") to NetworkError.UnknownNetworkError::class.java
        )

        exceptionsAndTypes.forEach { (exception, expectedType) ->
            val result = errorHandler.toNetworkError(exception)
            assertEquals("Exception ${exception.javaClass.simpleName} should map to $expectedType", expectedType, result.javaClass)
        }
    }
}
