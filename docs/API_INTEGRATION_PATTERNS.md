# API Integration Patterns

## Overview
This document describes the integration patterns and resilience mechanisms implemented in IuranKomplek's API layer.

## Core Integration Patterns

### 1. Circuit Breaker Pattern

**Purpose**: Prevent cascading failures by stopping calls to failing services.

**Implementation**: `com.example.iurankomplek.network.resilience.CircuitBreaker`

**States**:
- **Closed**: Normal operation, requests pass through
- **Open**: Failing service, requests blocked immediately
- **Half-Open**: Testing if service has recovered

**Configuration**:
```kotlin
val circuitBreaker = CircuitBreaker(
    failureThreshold = 3,           // Open after 3 failures
    successThreshold = 2,             // Close after 2 successes in half-open
    timeout = 60000L,                // Wait 60s before testing recovery
    halfOpenMaxCalls = 3              // Max 3 test calls in half-open
)
```

**Usage in Repositories**:
```kotlin
suspend fun getUsers(): UserResponse {
    return ApiConfig.circuitBreaker.execute {
        apiService.getUsers()
    }.let { result ->
        when (result) {
            is CircuitBreakerResult.Success -> result.value
            is CircuitBreakerResult.Failure -> throw result.exception
            is CircuitBreakerResult.CircuitOpen -> throw CircuitBreakerException("Service unavailable")
        }
    }
}
```

**Benefits**:
- Prevents system overload from failing services
- Automatic recovery detection
- Configurable thresholds and timeouts
- Thread-safe state management

---

### 2. Rate Limiting

**Purpose**: Protect API from overload by limiting request rate.

**Implementation**: `com.example.iurankomplek.network.interceptor.RateLimiterInterceptor`

**Configuration**:
```kotlin
val rateLimiter = RateLimiterInterceptor(
    maxRequestsPerSecond = 10,        // Max 10 requests per second
    maxRequestsPerMinute = 60,         // Max 60 requests per minute
    enableLogging = BuildConfig.DEBUG    // Log in debug mode
)
```

**Behavior**:
- Tracks request timestamps per minute
- Enforces minimum interval between requests
- Blocks requests exceeding limits with HTTP 429
- Per-endpoint statistics tracking

**Rate Limit Response**:
```json
{
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests. Please slow down.",
  "httpCode": 429,
  "details": "Rate limit exceeded for endpoint: GET:/api/users"
}
```

**Benefits**:
- Prevents API abuse
- Protects backend from overload
- Configurable per-second and per-minute limits
- Per-endpoint statistics

---

### 3. Error Handling

**Purpose**: Standardized error handling with user-friendly messages.

**Implementation**: 
- `NetworkErrorInterceptor`: Parses HTTP errors
- `NetworkError`: Sealed class for typed errors
- `ApiErrorCode`: Enum for error codes

**Error Types**:

| Error Type | Code | User Message |
|------------|------|-------------|
| `HttpError` | HTTP status codes | Contextual message |
| `TimeoutError` | TIMEOUT | Request timed out |
| `ConnectionError` | NETWORK_ERROR | No internet connection |
| `CircuitBreakerError` | SERVICE_UNAVAILABLE | Service unavailable |
| `ValidationError` | VALIDATION_ERROR | Validation failed |
| `UnknownNetworkError` | UNKNOWN_ERROR | Unexpected error |

**HTTP Status Code Mapping**:
```kotlin
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
```

**Benefits**:
- Consistent error handling across all API calls
- User-friendly error messages
- Type-safe error handling
- Request tracking via X-Request-ID header

---

### 4. Request Tracking

**Purpose**: Trace requests through the system for debugging.

**Implementation**: `RequestIdInterceptor`

**Behavior**:
- Generates unique request ID
- Adds `X-Request-ID` header to all requests
- Tags request for error logging

**Request ID Format**: `{timestamp}-{random}`

**Benefits**:
- Traceable request lifecycle
- Debug distributed systems
- Correlate logs across services

---

### 5. Retry Logic

**Purpose**: Automatically retry failed recoverable requests.

**Implementation**: `RetryableRequestInterceptor` + Repository-level retry

**Retryable Requests**:
- GET requests (safe to retry)
- HEAD requests
- OPTIONS requests
- Requests with `X-Retryable: true` header

**Non-Retryable Requests**:
- POST (create operations)
- PUT (update operations)
- DELETE (delete operations)
- PATCH (partial updates)

**Retry Strategy** (in BaseActivity):
- Initial delay: 1 second
- Exponential backoff: 2x multiplier
- Maximum delay: 30 seconds
- Jitter: Random variation to prevent thundering herd
- Max retries: 3

**Benefits**:
- Handles transient failures automatically
- Exponential backoff prevents overload
- Jitter prevents thundering herd problem
- Configurable retry limits

---

## Integration Architecture

### Interceptor Chain Order

```
Request → RequestIdInterceptor → RateLimiterInterceptor → RetryableRequestInterceptor → NetworkErrorInterceptor → API
```

**Order Rationale**:
1. **RequestIdInterceptor**: First, to tag request for entire lifecycle
2. **RateLimiterInterceptor**: Early rejection to protect backend
3. **RetryableRequestInterceptor**: Mark retryable requests
4. **NetworkErrorInterceptor**: Last, to parse and handle errors

### Configuration

**Production**:
```kotlin
val rateLimiter = RateLimiterInterceptor(
    maxRequestsPerSecond = 10,
    maxRequestsPerMinute = 60,
    enableLogging = false
)

SecurityConfig.getSecureOkHttpClient()
    .newBuilder()
    .connectionPool(connectionPool)
    .addInterceptor(RequestIdInterceptor())
    .addInterceptor(rateLimiter)
    .addInterceptor(RetryableRequestInterceptor())
    .addInterceptor(NetworkErrorInterceptor(enableLogging = false))
    .build()
```

**Development**:
```kotlin
val rateLimiter = RateLimiterInterceptor(
    maxRequestsPerSecond = 10,
    maxRequestsPerMinute = 60,
    enableLogging = true
)

OkHttpClient.Builder()
    .connectionPool(connectionPool)
    .addInterceptor(RequestIdInterceptor())
    .addInterceptor(rateLimiter)
    .addInterceptor(RetryableRequestInterceptor())
    .addInterceptor(NetworkErrorInterceptor(enableLogging = true))
    .addInterceptor(HttpLoggingInterceptor())
    .build()
```

**Important**: Use the same `rateLimiter` instance for monitoring and reset functions:
```kotlin
val stats = ApiConfig.getRateLimiterStats()
ApiConfig.resetRateLimiter()
```

---

## Resilience Patterns Summary

| Pattern | Implementation | Protection |
|---------|---------------|-------------|
| Circuit Breaker | CircuitBreaker class | Cascading failures |
| Rate Limiting | RateLimiterInterceptor | API overload |
| Timeout | ApiConfig (30s connect/read) | Hanging requests |
| Retry | BaseActivity + Repositories | Transient failures |
| Error Handling | NetworkErrorInterceptor + NetworkError | Unhandled exceptions |
| Request Tracking | RequestIdInterceptor | Debugging issues |
| Connection Pooling | ConnectionPool | Connection overhead |

---

## Webhook Reliability Patterns

See `docs/CACHING_STRATEGY.md` for webhook reliability implementation details:
- Persistent webhook event storage
- Idempotency key system
- Exponential backoff retry logic
- Queue-based processing

---

## Best Practices

### For API Consumers

1. **Handle Rate Limits**: Always catch `RATE_LIMIT_EXCEEDED` errors and implement exponential backoff
2. **Check Circuit Breaker**: Respect `SERVICE_UNAVAILABLE` errors from circuit breaker
3. **Use Request IDs**: Log `X-Request-ID` from responses for debugging
4. **Implement Timeouts**: Set appropriate timeouts for your use case
5. **Retry Safely**: Only retry GET/HEAD/OPTIONS requests automatically

### For API Development

1. **Use Interceptors**: Add interceptors in the correct order
2. **Configure Thresholds**: Adjust circuit breaker and rate limiter thresholds based on load testing
3. **Monitor Metrics**: Track circuit breaker state and rate limiter stats
4. **Log in Debug**: Enable logging in debug mode, disable in production
5. **Handle Errors**: Use typed `NetworkError` for consistent error handling

---

## Monitoring and Observability

### Circuit Breaker Monitoring

```kotlin
val state = ApiConfig.getCircuitBreakerState()
when (state) {
    is CircuitBreakerState.Closed -> println("Normal operation")
    is CircuitBreakerState.Open -> println("Service failing, circuit open")
    is CircuitBreakerState.HalfOpen -> println("Testing recovery")
}

val failureCount = ApiConfig.circuitBreaker.getFailureCount()
val successCount = ApiConfig.circuitBreaker.getSuccessCount()
```

### Rate Limiter Monitoring

```kotlin
val allStats = ApiConfig.getRateLimiterStats()
allStats.forEach { (endpoint, stats) ->
    println("$endpoint: ${stats.getRequestCount()} requests")
}

val endpointStats = ApiConfig.rateLimiter.getEndpointStats("GET:/api/users")
println("Last request: ${endpointStats?.getLastRequestTime()}")
```

### Reset Functions

```kotlin
// Reset circuit breaker (for manual recovery)
ApiConfig.resetCircuitBreaker()

// Reset rate limiter (for testing/development)
ApiConfig.resetRateLimiter()
```

---

## Testing

All integration patterns have comprehensive test coverage:
- `CircuitBreakerTest`: 15 test cases
- `RateLimiterInterceptorTest`: 11 test cases
- `NetworkErrorInterceptorTest`: 17 test cases
- `RequestIdInterceptorTest`: 8 test cases
- `RetryableRequestInterceptorTest`: 14 test cases

Total: **65 test cases** for integration patterns

---

## Future Enhancements

1. **Metrics Collection**: Integrate with Firebase Performance Monitoring
2. **Dynamic Thresholds**: Adjust circuit breaker thresholds based on time of day
3. **Distributed Rate Limiting**: Redis-based rate limiting for multi-instance deployments
4. **Circuit Breaker Metrics**: Expose metrics via HTTP endpoint
5. **Smart Retry**: Machine learning-based retry delay optimization

---

*Last Updated: 2026-01-07*
*Maintained by: Integration Engineer*
