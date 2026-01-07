package com.example.iurankomplek.network.model

import com.google.gson.annotations.SerializedName

data class ApiError(
    @SerializedName("code")
    val code: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("details")
    val details: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long? = null,
    
    @SerializedName("requestId")
    val requestId: String? = null
)

data class WrappedErrorResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("error")
    val error: ApiError? = null
)

enum class ApiErrorCode(val code: String, val defaultMessage: String) {
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "An internal server error occurred"),
    BAD_REQUEST("BAD_REQUEST", "Invalid request parameters"),
    UNAUTHORIZED("UNAUTHORIZED", "Authentication required"),
    FORBIDDEN("FORBIDDEN", "Access forbidden"),
    NOT_FOUND("NOT_FOUND", "Resource not found"),
    CONFLICT("CONFLICT", "Resource conflict"),
    VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed"),
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Too many requests"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service temporarily unavailable"),
    TIMEOUT("TIMEOUT", "Request timeout"),
    NETWORK_ERROR("NETWORK_ERROR", "Network connection error"),
    UNKNOWN_ERROR("UNKNOWN_ERROR", "An unknown error occurred");

    companion object {
        fun fromHttpCode(code: Int): ApiErrorCode {
            return when (code) {
                400 -> BAD_REQUEST
                401 -> UNAUTHORIZED
                403 -> FORBIDDEN
                404 -> NOT_FOUND
                409 -> CONFLICT
                422 -> VALIDATION_ERROR
                429 -> RATE_LIMIT_EXCEEDED
                500 -> INTERNAL_SERVER_ERROR
                503 -> SERVICE_UNAVAILABLE
                504 -> TIMEOUT
                else -> UNKNOWN_ERROR
            }
        }
    }
}

sealed class NetworkError(override val cause: Throwable? = null) : Exception() {
    abstract val code: ApiErrorCode
    abstract val userMessage: String
    abstract override val message: String
    
    data class HttpError(
        override val code: ApiErrorCode,
        override val userMessage: String,
        val httpCode: Int,
        val details: String? = null,
        override val cause: Throwable? = null
    ) : NetworkError() {
        override val message: String
            get() = "HTTP Error $httpCode: $userMessage"
    }
    
    data class TimeoutError(
        override val code: ApiErrorCode = ApiErrorCode.TIMEOUT,
        override val userMessage: String = "Request timed out. Please try again.",
        val timeoutDuration: Long? = null,
        override val cause: Throwable? = null
    ) : NetworkError() {
        override val message: String
            get() = "Timeout error: $userMessage"
    }
    
    data class ConnectionError(
        override val code: ApiErrorCode = ApiErrorCode.NETWORK_ERROR,
        override val userMessage: String = "No internet connection. Please check your network.",
        override val cause: Throwable? = null
    ) : NetworkError() {
        override val message: String
            get() = "Connection error: $userMessage"
    }
    
    data class CircuitBreakerError(
        override val code: ApiErrorCode = ApiErrorCode.SERVICE_UNAVAILABLE,
        override val userMessage: String = "Service is temporarily unavailable. Please try again later.",
        override val cause: Throwable? = null
    ) : NetworkError() {
        override val message: String
            get() = "Circuit breaker open: $userMessage"
    }
    
    data class ValidationError(
        override val code: ApiErrorCode = ApiErrorCode.VALIDATION_ERROR,
        override val userMessage: String,
        val field: String? = null
    ) : NetworkError() {
        override val message: String = "Validation error${field?.let { " on $it" } ?: ""}: $userMessage"
    }
    
    data class UnknownNetworkError(
        override val code: ApiErrorCode = ApiErrorCode.UNKNOWN_ERROR,
        override val userMessage: String = "An unexpected error occurred.",
        val originalException: Throwable? = null
    ) : NetworkError() {
        override val message: String
            get() = originalException?.message ?: "Unknown network error"
    }
}

data class NetworkState<T>(
    val status: Status,
    val data: T? = null,
    val error: NetworkError? = null
) {
    enum class Status {
        LOADING,
        SUCCESS,
        ERROR,
        RETRYING
    }
    
    companion object {
        fun <T> loading(): NetworkState<T> = NetworkState(Status.LOADING)
        
        fun <T> success(data: T): NetworkState<T> = NetworkState(Status.SUCCESS, data)
        
        fun <T> error(error: NetworkError): NetworkState<T> = NetworkState(Status.ERROR, error = error)
        
        fun <T> retrying(): NetworkState<T> = NetworkState(Status.RETRYING)
    }
}
