package com.example.iurankomplek.network.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkErrorTest {
    
    @Test
    fun `ApiErrorCode fromHttpCode returns correct code for 400`() {
        val errorCode = ApiErrorCode.fromHttpCode(400)
        assertEquals(ApiErrorCode.BAD_REQUEST, errorCode)
    }
    
    @Test
    fun `ApiErrorCode fromHttpCode returns correct code for 401`() {
        val errorCode = ApiErrorCode.fromHttpCode(401)
        assertEquals(ApiErrorCode.UNAUTHORIZED, errorCode)
    }
    
    @Test
    fun `ApiErrorCode fromHttpCode returns correct code for 403`() {
        val errorCode = ApiErrorCode.fromHttpCode(403)
        assertEquals(ApiErrorCode.FORBIDDEN, errorCode)
    }
    
    @Test
    fun `ApiErrorCode fromHttpCode returns correct code for 404`() {
        val errorCode = ApiErrorCode.fromHttpCode(404)
        assertEquals(ApiErrorCode.NOT_FOUND, errorCode)
    }
    
    @Test
    fun `ApiErrorCode fromHttpCode returns correct code for 409`() {
        val errorCode = ApiErrorCode.fromHttpCode(409)
        assertEquals(ApiErrorCode.CONFLICT, errorCode)
    }
    
    @Test
    fun `ApiErrorCode fromHttpCode returns correct code for 422`() {
        val errorCode = ApiErrorCode.fromHttpCode(422)
        assertEquals(ApiErrorCode.VALIDATION_ERROR, errorCode)
    }
    
    @Test
    fun `ApiErrorCode fromHttpCode returns correct code for 429`() {
        val errorCode = ApiErrorCode.fromHttpCode(429)
        assertEquals(ApiErrorCode.RATE_LIMIT_EXCEEDED, errorCode)
    }
    
    @Test
    fun `ApiErrorCode fromHttpCode returns correct code for 500`() {
        val errorCode = ApiErrorCode.fromHttpCode(500)
        assertEquals(ApiErrorCode.INTERNAL_SERVER_ERROR, errorCode)
    }
    
    @Test
    fun `ApiErrorCode fromHttpCode returns correct code for 503`() {
        val errorCode = ApiErrorCode.fromHttpCode(503)
        assertEquals(ApiErrorCode.SERVICE_UNAVAILABLE, errorCode)
    }
    
    @Test
    fun `ApiErrorCode fromHttpCode returns correct code for 504`() {
        val errorCode = ApiErrorCode.fromHttpCode(504)
        assertEquals(ApiErrorCode.TIMEOUT, errorCode)
    }
    
    @Test
    fun `ApiErrorCode fromHttpCode returns UNKNOWN_ERROR for unknown codes`() {
        val errorCode = ApiErrorCode.fromHttpCode(418)
        assertEquals(ApiErrorCode.UNKNOWN_ERROR, errorCode)
    }
    
    @Test
    fun `NetworkError HttpError has correct properties`() {
        val error = NetworkError.HttpError(
            code = ApiErrorCode.BAD_REQUEST,
            userMessage = "Invalid request",
            httpCode = 400,
            details = "Missing parameter"
        )
        
        assertEquals(ApiErrorCode.BAD_REQUEST, error.code)
        assertEquals("Invalid request", error.userMessage)
        assertEquals(400, error.httpCode)
        assertEquals("Missing parameter", error.details)
        assertTrue(error.message.contains("400"))
    }
    
    @Test
    fun `NetworkError TimeoutError has correct properties`() {
        val error = NetworkError.TimeoutError(
            userMessage = "Request timed out",
            timeoutDuration = 30000L
        )
        
        assertEquals(ApiErrorCode.TIMEOUT, error.code)
        assertEquals("Request timed out", error.userMessage)
        assertEquals(30000L, error.timeoutDuration)
        assertTrue(error.message.contains("Request timed out"))
    }
    
    @Test
    fun `NetworkError ConnectionError has correct properties`() {
        val cause = RuntimeException("No internet")
        val error = NetworkError.ConnectionError(
            userMessage = "No internet connection",
            cause = cause
        )
        
        assertEquals(ApiErrorCode.NETWORK_ERROR, error.code)
        assertEquals("No internet connection", error.userMessage)
        assertEquals(cause, error.cause)
        assertTrue(error.message.contains("Connection error"))
    }
    
    @Test
    fun `NetworkError CircuitBreakerError has correct properties`() {
        val error = NetworkError.CircuitBreakerError()
        
        assertEquals(ApiErrorCode.SERVICE_UNAVAILABLE, error.code)
        assertTrue(error.userMessage.contains("temporarily unavailable"))
        assertTrue(error.message.contains("Circuit breaker open"))
    }
    
    @Test
    fun `NetworkError ValidationError has correct properties`() {
        val error = NetworkError.ValidationError(
            userMessage = "Invalid email format",
            field = "email"
        )
        
        assertEquals(ApiErrorCode.VALIDATION_ERROR, error.code)
        assertEquals("Invalid email format", error.userMessage)
        assertEquals("email", error.field)
        assertTrue(error.message.contains("email"))
    }
    
    @Test
    fun `NetworkError UnknownNetworkError has correct properties`() {
        val originalException = RuntimeException("Unknown error")
        val error = NetworkError.UnknownNetworkError(
            originalException = originalException
        )
        
        assertEquals(ApiErrorCode.UNKNOWN_ERROR, error.code)
        assertEquals("An unexpected error occurred.", error.userMessage)
        assertEquals(originalException, error.originalException)
        assertEquals("Unknown error", error.message)
    }
    
    @Test
    fun `NetworkState loading returns correct state`() {
        val state = NetworkState.loading<String>()
        
        assertEquals(NetworkState.Status.LOADING, state.status)
        assertTrue(state.data == null)
        assertTrue(state.error == null)
    }
    
    @Test
    fun `NetworkState success returns correct state`() {
        val data = "Test Data"
        val state = NetworkState.success(data)
        
        assertEquals(NetworkState.Status.SUCCESS, state.status)
        assertEquals(data, state.data)
        assertTrue(state.error == null)
    }
    
    @Test
    fun `NetworkState error returns correct state`() {
        val error = NetworkError.HttpError(
            code = ApiErrorCode.NOT_FOUND,
            userMessage = "Not found",
            httpCode = 404
        )
        val state = NetworkState.error<String>(error)
        
        assertEquals(NetworkState.Status.ERROR, state.status)
        assertTrue(state.data == null)
        assertEquals(error, state.error)
    }
    
    @Test
    fun `NetworkState retrying returns correct state`() {
        val state = NetworkState.retrying<String>()
        
        assertEquals(NetworkState.Status.RETRYING, state.status)
        assertTrue(state.data == null)
        assertTrue(state.error == null)
    }
}
