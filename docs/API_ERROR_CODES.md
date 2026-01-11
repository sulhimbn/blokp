# API Error Code Reference

Complete reference of all API error codes, their meanings, and handling strategies.

## Table of Contents

- [Overview](#overview)
- [HTTP Status Codes](#http-status-codes)
- [API Error Codes](#api-error-codes)
- [Error Response Format](#error-response-format)
- [Error Handling Strategies](#error-handling-strategies)
- [Recovery Actions](#recovery-actions)

---

## Overview

The IuranKomplek API uses a two-layer error reporting system:

1. **HTTP Status Codes**: Standard HTTP status for each response
2. **API Error Codes**: Detailed application-level error codes within error response body

This combination provides both HTTP-level semantics and application-level specificity for robust error handling.

---

## HTTP Status Codes

### 2xx Success

| Code | Name | Description | Retry? |
|------|------|-------------|--------|
| 200 OK | Request succeeded successfully | No |
| 201 Created | Resource created successfully | No |
| 202 Accepted | Request accepted for processing | No |
| 204 No Content | Request succeeded with no response body | No |

### 4xx Client Errors

| Code | Name | Description | Retry? | Recovery |
|------|------|-------------|--------|-----------|
| 400 Bad Request | Invalid request parameters or malformed JSON | No | Fix request and retry |
| 401 Unauthorized | Authentication required or invalid credentials | No | Provide valid auth |
| 403 Forbidden | Insufficient permissions to access resource | No | Check permissions |
| 404 Not Found | Requested resource does not exist | No | Verify resource exists |
| 408 Request Timeout | Server took too long to respond | Yes | Retry with backoff |
| 409 Conflict | Resource already exists or conflicts with existing data | No | Check data and retry |
| 422 Unprocessable Entity | Validation failed for request data | No | Fix validation errors |
| 429 Too Many Requests | Rate limit exceeded | Yes | Wait and retry |
| 431 Request Header Fields Too Large | Request headers too large | No | Reduce header size |

### 5xx Server Errors

| Code | Name | Description | Retry? | Recovery |
|------|------|-------------|--------|-----------|
| 500 Internal Server Error | Unexpected server error occurred | Yes | Retry with backoff |
| 502 Bad Gateway | Invalid response from upstream service | Yes | Retry with backoff |
| 503 Service Unavailable | Service temporarily down for maintenance | Yes | Retry with backoff |
| 504 Gateway Timeout | Upstream service timed out | Yes | Retry with backoff |

---

## API Error Codes

### VALIDATION_ERROR

**HTTP Status**: 400, 422
**Description**: Request validation failed
**User Message**: Invalid input data
**Details**: Specific validation error (e.g., email format, missing field)
**Field**: Field name that caused validation error (if applicable)

**Example**:
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": "Email format is invalid",
    "field": "email"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Common Causes**:
- Invalid email format
- Missing required fields
- Invalid date format
- String exceeds max length
- Number out of allowed range
- Invalid enum value

**Recovery**:
1. Review `details` field for specific validation issue
2. Review `field` field for problematic field name
3. Correct the validation error in request
4. Retry request with corrected data

---

### UNAUTHORIZED

**HTTP Status**: 401
**Description**: Authentication required or credentials invalid
**User Message**: Please log in
**Details**: Invalid/expired token or missing API key

**Example**:
```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Authentication required",
    "details": "Invalid or expired API key"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Common Causes**:
- Missing X-API-Key header
- Invalid API key
- Expired JWT token
- Token missing from Authorization header

**Recovery**:
1. Provide valid API key in X-API-Key header
2. Refresh JWT token if expired
3. Check token is included in Authorization header
4. Verify credentials are correct

---

### FORBIDDEN

**HTTP Status**: 403
**Description**: Insufficient permissions to access resource
**User Message**: Access forbidden
**Details**: User lacks required role or permission

**Example**:
```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "Access forbidden",
    "details": "Insufficient permissions to access this resource"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Common Causes**:
- User doesn't have required role
- Resource belongs to another user
- User account suspended
- IP address blocked

**Recovery**:
1. Check user has required permissions
2. Verify user account is active
3. Contact administrator if access should be granted
4. Login with different account if applicable

---

### NOT_FOUND

**HTTP Status**: 404
**Description**: Requested resource does not exist
**User Message**: Resource not found
**Details**: Resource ID or path invalid

**Example**:
```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Resource not found",
    "details": "Vendor with ID '123' not found"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Common Causes**:
- Invalid resource ID
- Resource deleted
- Incorrect endpoint path
- Resource never existed

**Recovery**:
1. Verify resource ID is correct
2. Check if resource still exists
3. Verify endpoint path is correct
4. Contact support if resource should exist

---

### CONFLICT

**HTTP Status**: 409
**Description**: Resource conflicts with existing data
**User Message**: Resource already exists
**Details**: Duplicate unique field or state conflict

**Example**:
```json
{
  "error": {
    "code": "CONFLICT",
    "message": "Resource conflict",
    "details": "Vendor with email 'acme@plumbing.com' already exists",
    "field": "email"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Common Causes**:
- Duplicate email address
- Duplicate unique identifier
- Resource in different state than expected
- Concurrent modification conflict

**Recovery**:
1. Use different email/identifier for new resource
2. Update existing resource instead of creating duplicate
3. Retry with optimistic locking if concurrent modification
4. Check for existing resources before creation

---

### RATE_LIMIT_EXCEEDED

**HTTP Status**: 429
**Description**: Too many requests sent in short period
**User Message**: Please slow down
**Details**: Rate limit threshold exceeded

**Example**:
```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests",
    "details": "Exceeded 10 requests per second limit"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Headers**:
| Header | Description |
|--------|-------------|
| Retry-After | Seconds to wait before retrying |
| X-RateLimit-Limit | Request limit per window |
| X-RateLimit-Remaining | Remaining requests in window |
| X-RateLimit-Reset | Unix timestamp when window resets |

**Common Causes**:
- Too many rapid requests
- Multiple clients sharing same API key
- Bug causing request loops

**Recovery**:
1. Wait for Retry-After duration
2. Implement client-side rate limiting
3. Use exponential backoff for retries
4. Reduce request frequency
5. Check for bugs causing request loops

---

### INTERNAL_SERVER_ERROR

**HTTP Status**: 500
**Description**: Unexpected server error occurred
**User Message**: Server error occurred
**Details**: Generic error, details may be hidden

**Example**:
```json
{
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "An internal server error occurred",
    "details": "Unexpected error in processing"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Common Causes**:
- Unhandled exception on server
- Database connection failure
- External service failure
- Configuration error

**Recovery**:
1. Retry request with exponential backoff
2. Log request_id for support investigation
3. Check if issue is widespread (status page)
4. Report issue to support team
5. Use fallback/cached data if available

---

### SERVICE_UNAVAILABLE

**HTTP Status**: 503
**Description**: Service temporarily unavailable
**User Message**: Service temporarily unavailable
**Details**: Service down for maintenance or overloaded

**Example**:
```json
{
  "error": {
    "code": "SERVICE_UNAVAILABLE",
    "message": "Service temporarily unavailable",
    "details": "Service is undergoing maintenance"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Headers**:
| Header | Description |
|--------|-------------|
| Retry-After | Seconds to wait before retrying |

**Common Causes**:
- Scheduled maintenance
- Service overload
- Circuit breaker open
- Infrastructure issues

**Recovery**:
1. Wait for Retry-After duration
2. Use cached data if available
3. Display maintenance message to user
4. Check status page for updates
5. Retry after maintenance window

**Circuit Breaker**:
The API implements circuit breaker pattern to prevent cascading failures:
- **Open**: Requests fail fast (503) after 3 consecutive failures
- **Half-Open**: Test requests allowed after timeout
- **Closed**: Normal operation after 2 consecutive successes

---

### TIMEOUT

**HTTP Status**: 504
**Description**: Request timeout occurred
**User Message**: Request timed out
**Details**: Server or upstream service didn't respond in time

**Example**:
```json
{
  "error": {
    "code": "TIMEOUT",
    "message": "Request timeout",
    "details": "Upstream service timeout after 30 seconds"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Common Causes**:
- Slow database query
- External service timeout
- Large payload processing
- Network congestion

**Recovery**:
1. Retry request with exponential backoff
2. Reduce payload size if possible
3. Use pagination for large datasets
4. Check network connectivity
5. Use cached data if available

---

### NETWORK_ERROR

**HTTP Status**: N/A (Client-side)
**Description**: Network connection error occurred
**User Message**: No internet connection
**Details**: Connection failed or DNS resolution failed

**Common Causes**:
- No internet connection
- DNS resolution failure
- SSL certificate error
- Firewall blocking connection
- Timeout waiting for response

**Recovery**:
1. Check internet connectivity
2. Retry when connection restored
3. Use cached data if available
4. Display offline mode UI
5. Implement queue for offline operations

---

### CIRCUIT_BREAKER_ERROR

**HTTP Status**: 503 (Service Unavailable)
**Description**: Circuit breaker is open, preventing requests
**User Message**: Service temporarily unavailable
**Details**: Too many failures, circuit breaker protecting service

**Example**:
```json
{
  "error": {
    "code": "CIRCUIT_BREAKER_ERROR",
    "message": "Service temporarily unavailable",
    "details": "Circuit breaker is open due to repeated failures"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Circuit Breaker States**:
| State | Description | Behavior |
|-------|-------------|----------|
| Closed | Normal operation | Requests allowed |
| Open | Failing service | Requests fail fast (503) |
| Half-Open | Testing recovery | Limited requests allowed |

**Recovery**:
1. Wait for circuit breaker to close (automatic after timeout)
2. Use cached data if available
3. Display service degraded UI
4. Monitor circuit breaker state in logs
5. Implement fallback functionality

---

### UNKNOWN_ERROR

**HTTP Status**: 500 or N/A
**Description**: Unexpected error occurred
**User Message**: An unexpected error occurred
**Details**: Generic error for unhandled scenarios

**Example**:
```json
{
  "error": {
    "code": "UNKNOWN_ERROR",
    "message": "An unexpected error occurred",
    "details": null
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Recovery**:
1. Log request_id for investigation
2. Report issue to support team
3. Use cached data if available
4. Implement generic error handling
5. Consider operation failed and inform user

---

## Error Response Format

All error responses follow this structure:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "User-friendly error message",
    "details": "Detailed error information",
    "field": "field_name"
  },
  "request_id": "uuid-for-tracing",
  "timestamp": 1704067200000
}
```

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| error | object | Yes | Error details object |
| error.code | string | Yes | Error code (see API Error Codes section) |
| error.message | string | Yes | User-friendly error message |
| error.details | string | No | Additional error context or specifics |
| error.field | string | No | Field name that caused validation error |
| request_id | string (UUID) | No | Unique request identifier for tracing |
| timestamp | integer (Unix epoch) | No | Error timestamp |

---

## Error Handling Strategies

### Client-Side Error Handling

#### Kotlin/Android

```kotlin
// Network error handling with typed errors
sealed class NetworkError(override val cause: Throwable? = null) : Exception() {
    abstract val code: ApiErrorCode
    abstract val userMessage: String
    abstract override val message: String
    
    data class HttpError(
        override val code: ApiErrorCode,
        override val userMessage: String,
        val httpCode: Int,
        val details: String? = null
    ) : NetworkError() {
        override val message: String
            get() = "HTTP Error $httpCode: $userMessage"
    }
}

// Usage in ViewModel
viewModelScope.launch {
    _uiState.value = UiState.Loading
    
    repository.getUsers()
        .onSuccess { users ->
            _uiState.value = UiState.Success(users)
        }
        .onFailure { error ->
            val message = when (error) {
                is NetworkError.HttpError -> {
                    when (error.code) {
                        ApiErrorCode.VALIDATION_ERROR -> 
                            "Validation error: ${error.details}"
                        ApiErrorCode.RATE_LIMIT_EXCEEDED -> 
                            "Rate limit exceeded. Please slow down."
                        else -> error.userMessage
                    }
                }
                is NetworkError.TimeoutError -> 
                    "Request timed out. Please try again."
                is NetworkError.ConnectionError -> 
                    "No internet connection. Please check your network."
                is NetworkError.CircuitBreakerError -> 
                    "Service temporarily unavailable. Please try again later."
                else -> 
                    "An unexpected error occurred."
            }
            _uiState.value = UiState.Error(message)
        }
}
```

---

## Recovery Actions

### Retry Strategy

**Retryable Errors**: Retry with exponential backoff
- HTTP 408 (Request Timeout)
- HTTP 429 (Too Many Requests)
- HTTP 500 (Internal Server Error)
- HTTP 502 (Bad Gateway)
- HTTP 503 (Service Unavailable)
- HTTP 504 (Gateway Timeout)
- NETWORK_ERROR
- CIRCUIT_BREAKER_ERROR (after timeout)

**Non-Retryable Errors**: Do not retry
- HTTP 400 (Bad Request) - fix request
- HTTP 401 (Unauthorized) - provide auth
- HTTP 403 (Forbidden) - check permissions
- HTTP 404 (Not Found) - verify resource
- HTTP 409 (Conflict) - use different data
- HTTP 422 (Validation Error) - fix validation

**Exponential Backoff**:
```kotlin
fun retryWithBackoff(attempt: Int): Long {
    val initialDelay = 1000L // 1 second
    val maxDelay = 30000L // 30 seconds
    val multiplier = 2.0
    val jitter = (Math.random() * 500).toLong() // Random jitter
    
    val delay = (initialDelay * Math.pow(multiplier, attempt.toDouble())).toLong()
    return min(delay, maxDelay) + jitter
}
```

### Fallback Strategy

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: NetworkError) : Result<T>()
}

suspend fun <T> withFallback(
    primary: suspend () -> Result<T>,
    fallback: suspend () -> Result<T>
): Result<T> {
    return try {
        primary()
    } catch (e: Exception) {
        when (e) {
            is NetworkError -> {
                // Use cached data for recoverable errors
                if (e.code in listOf(
                    ApiErrorCode.NETWORK_ERROR,
                    ApiErrorCode.TIMEOUT,
                    ApiErrorCode.SERVICE_UNAVAILABLE
                )) {
                    fallback() // Return cached data
                } else {
                    Result.Error(e)
                }
            }
            else -> Result.Error(NetworkError.UnknownNetworkError(e))
        }
    }
}
```

### Circuit Breaker Integration

```kotlin
// Circuit breaker protects against cascading failures
val circuitBreaker = CircuitBreaker(
    failureThreshold = 3,
    successThreshold = 2,
    timeout = 60000L,
    halfOpenMaxCalls = 3
)

// Usage in repository
suspend fun getUsers(): Result<List<User>> {
    val circuitBreakerResult = circuitBreaker.execute {
        apiService.getUsers()
    }
    
    return when (circuitBreakerResult) {
        is CircuitBreakerResult.Success -> {
            val response = circuitBreakerResult.value
            if (response.isSuccessful) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(NetworkError.HttpError(
                    code = ApiErrorCode.fromHttpCode(response.code()),
                    userMessage = "API error occurred",
                    httpCode = response.code()
                ))
            }
        }
        is CircuitBreakerResult.Failure -> {
            Result.failure(NetworkError.HttpError(
                code = ApiErrorCode.INTERNAL_SERVER_ERROR,
                userMessage = "Service error occurred",
                httpCode = 500
            ))
        }
        is CircuitBreakerResult.CircuitOpen -> {
            Result.failure(NetworkError.CircuitBreakerError())
        }
    }
}
```

---

## Best Practices

### For API Consumers

1. **Always Check HTTP Status**: Handle each status code appropriately
2. **Parse Error Body**: Extract error code, message, and details
3. **Log Request ID**: Include in all error logs for tracing
4. **Implement Retry Logic**: Use exponential backoff for retryable errors
5. **Handle Circuit Breaker**: Respect 503 responses and back off
6. **Provide User Feedback**: Display appropriate messages based on error type
7. **Use Fallbacks**: Implement cached data or degraded functionality
8. **Monitor Errors**: Track error rates and types for insights

### For API Providers

1. **Use Specific Error Codes**: Provide detailed, actionable error codes
2. **Include Field Names**: Specify which field caused validation errors
3. **Add Request IDs**: Enable tracing of error scenarios
4. **Set Appropriate HTTP Codes**: Use correct HTTP semantics
5. **Provide Recovery Guidance**: Include helpful details in error messages
6. **Log Errors Thoroughly**: Include context, stack traces, and user info
7. **Monitor Error Rates**: Track error rates for service health
8. **Implement Rate Limiting**: Protect service from overload

---

## Testing Error Handling

### Unit Tests

```kotlin
@Test
fun `handleValidationError displays field name`() {
    // Given
    val errorResponse = ApiErrorResponse(
        error = ApiErrorDetail(
            code = "VALIDATION_ERROR",
            message = "Invalid request parameters",
            details = "Email format is invalid",
            field = "email"
        ),
        requestId = "test-id",
        timestamp = 1704067200000
    )
    
    // When
    val userMessage = errorHandler.getUserMessage(errorResponse)
    
    // Then
    assertEquals("Validation error on email: Email format is invalid", userMessage)
}

@Test
fun `handleRateLimit implements backoff`() {
    // Given
    val error = NetworkError.HttpError(
        code = ApiErrorCode.RATE_LIMIT_EXCEEDED,
        userMessage = "Please slow down",
        httpCode = 429
    )
    
    // When
    val shouldRetry = errorHandler.isRetryable(error)
    
    // Then
    assertTrue(shouldRetry)
}
```

### Integration Tests

```kotlin
@Test
fun `api returns 422 for invalid email`() = runTest {
    // Given
    val invalidRequest = CreateVendorRequest(
        name = "Test Vendor",
        contactPerson = "John",
        phoneNumber = "123",
        email = "invalid-email", // Invalid
        specialty = "Plumbing",
        address = "123 Main St",
        licenseNumber = "LIC-001",
        insuranceInfo = "INS-001",
        contractStart = "2024-01-01",
        contractEnd = "2024-12-31"
    )
    
    // When
    val response = apiService.createVendor(invalidRequest)
    
    // Then
    assertEquals(422, response.code())
    assertEquals("VALIDATION_ERROR", response.body()?.error?.code)
    assertEquals("email", response.body()?.error?.field)
}
```

---

## Support

### Reporting Issues

When reporting API errors to support, include:

1. **Request ID**: For tracing the exact request
2. **Error Code**: From error response body
3. **HTTP Status**: From response headers
4. **Timestamp**: From error response
5. **Endpoint**: The API endpoint being called
6. **Request Payload** (sanitized): Relevant request data
7. **Client Version**: Your application version

**Example Issue Report**:
```
Error Code: VALIDATION_ERROR
HTTP Status: 422
Request ID: 550e8400-e29b-41d4-a716-446655440000
Timestamp: 1704067200000
Endpoint: POST /api/v1/vendors
Client Version: 1.0.0

Description: Creating vendor fails with validation error on email field even though email format is valid.

Expected Behavior: Vendor should be created successfully.
Actual Behavior: Validation error returned.

Steps to Reproduce:
1. Call POST /api/v1/vendors with valid vendor data
2. Include email: "test@example.com"
3. Receive 422 with VALIDATION_ERROR
```

### Contact

- **Email**: dev@iurankomplek.com
- **GitHub Issues**: https://github.com/sulhimbn/blokp/issues
- **Documentation**: [API Documentation Hub](API_DOCS_HUB.md)

---

*Last Updated: 2026-01-08*
*Version: 1.0.0*
*Maintained by: Integration Engineer*
