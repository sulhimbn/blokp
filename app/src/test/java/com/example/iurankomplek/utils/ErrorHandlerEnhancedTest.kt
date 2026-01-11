package com.example.iurankomplek.utils

import android.content.Context
import com.example.iurankomplek.network.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreakerException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class ErrorHandlerEnhancedTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val errorHandler = ErrorHandler(context)
    
    @Test
    fun `handleError with UnknownHostException returns connection message`() {
        val exception = UnknownHostException("Unable to resolve host")
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("No internet connection", message)
    }
    
    @Test
    fun `handleError with SocketTimeoutException returns timeout message`() {
        val exception = SocketTimeoutException("Connection timed out")
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Connection timeout", message)
    }
    
    @Test
    fun `handleError with CircuitBreakerException returns service unavailable message`() {
        val exception = CircuitBreakerException("Circuit is open")
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Service temporarily unavailable", message)
    }
    
    @Test
    fun `handleError with HttpException 400 returns bad request message`() {
        val exception = HttpException(
            Response.error<Any>(400, "Bad Request".toResponseBody())
        )
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Invalid request", message)
    }
    
    @Test
    fun `handleError with HttpException 401 returns unauthorized message`() {
        val exception = HttpException(
            Response.error<Any>(401, "Unauthorized".toResponseBody())
        )
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Unauthorized access", message)
    }
    
    @Test
    fun `handleError with HttpException 403 returns forbidden message`() {
        val exception = HttpException(
            Response.error<Any>(403, "Forbidden".toResponseBody())
        )
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Forbidden", message)
    }
    
    @Test
    fun `handleError with HttpException 404 returns not found message`() {
        val exception = HttpException(
            Response.error<Any>(404, "Not Found".toResponseBody())
        )
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Resource not found", message)
    }
    
    @Test
    fun `handleError with HttpException 408 returns timeout message`() {
        val exception = HttpException(
            Response.error<Any>(408, "Request Timeout".toResponseBody())
        )
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Request timeout", message)
    }
    
    @Test
    fun `handleError with HttpException 429 returns rate limit message`() {
        val exception = HttpException(
            Response.error<Any>(429, "Too Many Requests".toResponseBody())
        )
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Too many requests. Please slow down.", message)
    }
    
    @Test
    fun `handleError with HttpException 500 returns server error message`() {
        val exception = HttpException(
            Response.error<Any>(500, "Internal Server Error".toResponseBody())
        )
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Server error", message)
    }
    
    @Test
    fun `handleError with HttpException 503 returns service unavailable message`() {
        val exception = HttpException(
            Response.error<Any>(503, "Service Unavailable".toResponseBody())
        )
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Service unavailable", message)
    }
    
    @Test
    fun `handleError with IOException returns network error message`() {
        val exception = IOException("Connection lost")
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("Network error occurred", message)
    }
    
    @Test
    fun `handleError with generic exception returns message`() {
        val exception = RuntimeException("Something went wrong")
        
        val message = errorHandler.handleError(exception)
        
        assertEquals("An error occurred: Something went wrong", message)
    }
    
    @Test
    fun `handleError with context includes request ID in log`() {
        val exception = UnknownHostException("Unable to resolve host")
        val context = ErrorContext(
            requestId = "test-request-id",
            endpoint = "GET /api/v1/users"
        )
        
        val message = errorHandler.handleError(exception, context)
        
        assertEquals("No internet connection", message)
    }
    
    @Test
    fun `toNetworkError converts UnknownHostException to ConnectionError`() {
        val exception = UnknownHostException("Unable to resolve host")
        
        val networkError = errorHandler.toNetworkError(exception)
        
        assertTrue(networkError is NetworkError.ConnectionError)
        assertEquals("No internet connection", networkError.userMessage)
    }
    
    @Test
    fun `toNetworkError converts SocketTimeoutException to TimeoutError`() {
        val exception = SocketTimeoutException("Connection timed out")
        
        val networkError = errorHandler.toNetworkError(exception)
        
        assertTrue(networkError is NetworkError.TimeoutError)
        assertEquals("Connection timeout", networkError.userMessage)
    }
    
    @Test
    fun `toNetworkError converts CircuitBreakerException to CircuitBreakerError`() {
        val exception = CircuitBreakerException("Circuit is open")
        
        val networkError = errorHandler.toNetworkError(exception)
        
        assertTrue(networkError is NetworkError.CircuitBreakerError)
    }
    
    @Test
    fun `toNetworkError converts HttpException to HttpError`() {
        val exception = HttpException(
            Response.error<Any>(404, "Not Found".toResponseBody())
        )
        
        val networkError = errorHandler.toNetworkError(exception)
        
        assertTrue(networkError is NetworkError.HttpError)
        val httpError = networkError as NetworkError.HttpError
        assertEquals(404, httpError.httpCode)
    }
    
    @Test
    fun `toNetworkError converts IOException to ConnectionError`() {
        val exception = IOException("Connection lost")
        
        val networkError = errorHandler.toNetworkError(exception)
        
        assertTrue(networkError is NetworkError.ConnectionError)
    }
    
    @Test
    fun `toNetworkError converts generic exception to UnknownNetworkError`() {
        val exception = RuntimeException("Something went wrong")
        
        val networkError = errorHandler.toNetworkError(exception)
        
        assertTrue(networkError is NetworkError.UnknownNetworkError)
    }
}
