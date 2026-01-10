# API Response Headers and Error Response Standardization

## Overview

This document defines the standard HTTP headers and error response formats used across all IuranKomplek API endpoints for resilience, debugging, and client integration.

## Resilience Headers

### Request Headers

#### X-Request-ID
- **Purpose**: Unique request identifier for distributed tracing and debugging
- **Format**: `{timestamp}-{random}`
- **Example**: `req_1234567890_abc42`
- **Added by**: `RequestIdInterceptor` (first in interceptor chain)
- **Usage**: Correlate logs across services, trace request lifecycle

#### X-Retryable
- **Purpose**: Marks requests safe for automatic retry
- **Values**: `true` | `false`
- **Added by**: `RetryableRequestInterceptor` (for GET, HEAD, OPTIONS)
- **Logic**: Non-idempotent methods (POST, PUT, DELETE) are not retryable by default

#### Authorization
- **Purpose**: API key or token authentication (future implementation)
- **Format**: `Bearer {token}` or `X-API-Key {key}`
- **Status**: Placeholder for future authentication system

### Response Headers

#### X-Request-ID
- **Purpose**: Echoes request ID from client request for correlation
- **Example**: `req_1234567890_abc42`
- **Returned by**: All API responses (added by RequestIdInterceptor)
- **Usage**: Match with client request ID for end-to-end tracing

#### X-Retry-After
- **Purpose**: Indicates when client should retry after rate limit exceeded
- **Format**: Integer milliseconds
- **Example**: `5000` (retry after 5 seconds)
- **Added by**: `RateLimiterInterceptor`
- **When Present**: Only on 429 (Rate Limit Exceeded) responses
- **Usage**: `Retry-After: 5000` → Client should wait 5s before retry

#### X-RateLimit-Limit
- **Purpose**: Maximum requests allowed per time window
- **Format**: Integer
- **Example**: `10` (10 requests per second)
- **Added by**: `RateLimiterInterceptor`
- **When Present**: On rate-limited responses (429)

#### X-RateLimit-Remaining
- **Purpose**: Number of requests remaining in current time window
- **Format**: Integer
- **Example**: `0` (no tokens available)
- **Added by**: `RateLimiterInterceptor`
- **When Present**: On rate-limited responses (429)

#### X-RateLimit-Reset
- **Purpose**: Unix timestamp when rate limit window resets
- **Format**: Integer (Unix timestamp)
- **Example**: `1704672050`
- **Added by**: `RateLimiterInterceptor`
- **When Present**: On rate-limited responses (429)

#### X-Retry-Count
- **Purpose**: Indicates number of retries attempted for current request
- **Format**: Integer
- **Example**: `2` (this is the 3rd attempt: original + 2 retries)
- **Added by**: `RetryHelper` (repository-level retry logic)
- **When Present**: On retried requests (network errors, timeouts)

#### X-CircuitBreaker-State
- **Purpose**: Indicates current circuit breaker state
- **Values**: `CLOSED` | `OPEN` | `HALF_OPEN`
- **Added by**: `CircuitBreaker` (when returning CircuitOpen error)
- **When Present**: On 503 (Service Unavailable) responses due to circuit breaker

#### X-Response-Time
- **Purpose**: Server processing time in milliseconds
- **Format**: Integer milliseconds
- **Example**: `127`
- **Added by**: `NetworkErrorInterceptor` (calculated)
- **Usage**: Performance monitoring, SLA compliance

## Standard Error Response Format

### API v1 Error Response Structure

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": "Email field is required",
    "field": "email"
  },
  "request_id": "req_1234567890_abc42",
  "timestamp": 1704672000000
}
```

### Error Response Fields

| Field | Type | Required | Description | Example |
|-------|------|-----------|-------------|---------|
| `error` | `ApiErrorDetail` | Yes | Error detail object | See below |
| `request_id` | `string` | No | Unique request identifier | `"req_1234567890_abc42"` |
| `timestamp` | `integer` | No | Response timestamp (ms) | `1704672000000` |

### ApiErrorDetail Structure

| Field | Type | Required | Description | Example |
|-------|------|-----------|-------------|---------|
| `code` | `string` | Yes | Standard error code | `"VALIDATION_ERROR"` |
| `message` | `string` | Yes | Human-readable error message | `"Invalid request parameters"` |
| `details` | `string` | No | Additional error details | `"Email field is required"` |
| `field` | `string` | No | Field with validation error | `"email"` |

### Helper Method for User-Friendly Messages

```kotlin
ApiErrorDetail.toDisplayMessage()
// "Invalid request parameters: email - Email field is required"
// "Invalid request parameters: Email field is required"
// "Invalid request parameters"
```

## Standard Error Codes

### HTTP Status Code Mapping

| HTTP Status | Error Code | User Message | Retry Strategy |
|-------------|-------------|---------------|----------------|
| 400 | `BAD_REQUEST` | Invalid request parameters | Do not retry |
| 401 | `UNAUTHORIZED` | Authentication required | Do not retry |
| 403 | `FORBIDDEN` | Access forbidden | Do not retry |
| 404 | `NOT_FOUND` | Resource not found | Do not retry |
| 408 | `TIMEOUT` | Request timeout | Retry with backoff |
| 409 | `CONFLICT` | Resource conflict | Do not retry |
| 422 | `VALIDATION_ERROR` | Validation failed | Do not retry |
| 429 | `RATE_LIMIT_EXCEEDED` | Too many requests | Wait and retry (use X-Retry-After) |
| 500 | `INTERNAL_SERVER_ERROR` | Internal server error | Retry with backoff |
| 503 | `SERVICE_UNAVAILABLE` | Service temporarily unavailable | Retry with backoff or check circuit breaker |
| 504 | `GATEWAY_TIMEOUT` | Gateway timeout | Retry with backoff |

### Error Code Definitions

```kotlin
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
    UNKNOWN_ERROR("UNKNOWN_ERROR", "An unknown error occurred")
}
```

## Resilience Pattern Responses

### Rate Limit Exceeded (429)

**Response Headers:**
```
HTTP/1.1 429 Too Many Requests
X-Request-ID: req_1234567890_abc42
X-Retry-After: 5000
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1704672050
Content-Type: application/json
```

**Response Body:**
```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests. Please try again in 5s.",
    "details": "Rate limit exceeded for endpoint: GET:/api/v1/users. Retry after: 5000ms"
  },
  "request_id": "req_1234567890_abc42",
  "timestamp": 1704672000000
}
```

**Client Action:**
```kotlin
if (response.code == 429) {
    val retryAfter = response.headers["X-Retry-After"]?.toLong() ?: 5000L
    delay(retryAfter)
    retryRequest()
}
```

### Circuit Breaker Open (503)

**Response Headers:**
```
HTTP/1.1 503 Service Unavailable
X-Request-ID: req_1234567890_abc42
X-CircuitBreaker-State: OPEN
Content-Type: application/json
```

**Response Body:**
```json
{
  "error": {
    "code": "SERVICE_UNAVAILABLE",
    "message": "Service is temporarily unavailable. Please try again later.",
    "details": "Circuit breaker is open. System experienced 3 consecutive failures."
  },
  "request_id": "req_1234567890_abc42",
  "timestamp": 1704672000000
}
```

**Client Action:**
```kotlin
if (response.code == 503) {
    val circuitState = response.headers["X-CircuitBreaker-State"]
    if (circuitState == "OPEN") {
        showMessage("Service temporarily unavailable. Please try again in 1 minute.")
        scheduleRetryAfter(60000) // Wait 60 seconds
    }
}
```

### Network Error (NetworkError.HttpError)

**Response Headers:**
```
HTTP/1.1 500 Internal Server Error
X-Request-ID: req_1234567890_abc42
X-Response-Time: 245
X-Retry-Count: 1
Content-Type: application/json
```

**Response Body:**
```json
{
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "An internal server error occurred",
    "details": null
  },
  "request_id": "req_1234567890_abc42",
  "timestamp": 1704672000000
}
```

**Client Action:**
```kotlin
when (networkError) {
    is NetworkError.TimeoutError -> {
        showTimeoutMessage()
        retryWithExponentialBackoff(currentRetry + 1)
    }
    is NetworkError.ConnectionError -> {
        showNoConnectionMessage()
        retryWithExponentialBackoff(currentRetry + 1)
    }
    is NetworkError.HttpError -> {
        if (networkError.code == ApiErrorCode.RATE_LIMIT_EXCEEDED) {
            handleRateLimit(networkError.httpCode)
        } else if (networkError.httpCode / 100 == 5) {
            retryWithExponentialBackoff(currentRetry + 1)
        } else {
            showErrorMessage(networkError.userMessage)
        }
    }
}
```

## Retry Strategy Documentation

### Exponential Backoff Algorithm

```kotlin
fun calculateDelay(currentRetry: Int): Long {
    val exponentialDelay = INITIAL_RETRY_DELAY_MS * 2.0.pow((currentRetry - 1).toDouble()).toLong()
    val jitter = (Random.nextDouble() * INITIAL_RETRY_DELAY_MS).toLong()
    return minOf(exponentialDelay + jitter, MAX_RETRY_DELAY_MS)
}
```

**Retry Schedule Example:**
| Retry Attempt | Delay (ms) | Delay (seconds) | Jitter Range |
|--------------|--------------|-------------------|---------------|
| 0 (initial) | 0 | 0 | N/A |
| 1 | 1000 ± 500 | 0.5 - 1.5 | 500 - 1500 |
| 2 | 2000 ± 500 | 1.5 - 2.5 | 1500 - 2500 |
| 3 | 4000 ± 500 | 3.5 - 4.5 | 3500 - 4500 |
| 4 | 8000 ± 500 | 7.5 - 8.5 | 7500 - 8500 |
| Max (capped) | 30000 | 30 | N/A |

### Retry Logic Rules

**Retryable Errors:**
- SocketTimeoutException
- UnknownHostException
- SSLException
- HTTP 408 (Request Timeout)
- HTTP 429 (Rate Limit Exceeded)
- HTTP 5xx (Server errors)

**Non-Retryable Errors:**
- HTTP 400 (Bad Request)
- HTTP 401 (Unauthorized)
- HTTP 403 (Forbidden)
- HTTP 404 (Not Found)
- HTTP 409 (Conflict)
- HTTP 422 (Validation Error)
- Non-idempotent methods (POST, PUT, DELETE) without explicit retry header

### Max Retry Configuration

```kotlin
object Network {
    const val MAX_RETRIES = 3
    const val INITIAL_RETRY_DELAY_MS = 1000L
    const val MAX_RETRY_DELAY_MS = 30000L
}
```

## Circuit Breaker Documentation

### Circuit Breaker States

| State | Description | Behavior |
|--------|-------------|-----------|
| CLOSED | Normal operation | All requests pass through, failure count tracked |
| OPEN | Service failing | Requests fail immediately without hitting service |
| HALF_OPEN | Testing recovery | Limited requests allowed to test service health |

### Circuit Breaker Configuration

```kotlin
CircuitBreaker(
    failureThreshold = 3,       // Open after 3 consecutive failures
    successThreshold = 2,       // Close after 2 successes in half-open
    timeout = 60000L,          // Wait 60s before testing recovery
    halfOpenMaxCalls = 3        // Max 3 test calls in half-open
)
```

### State Transitions

```
CLOSED
  ↓ (3 consecutive failures)
OPEN
  ↓ (60 seconds elapsed)
HALF_OPEN
  ↓ (2 consecutive successes) OR (1 failure)
CLOSED (success) OR OPEN (failure)
```

### Circuit Breaker Error Response

```kotlin
sealed class NetworkError : Exception() {
    data class CircuitBreakerError(
        override val code: ApiErrorCode = ApiErrorCode.SERVICE_UNAVAILABLE,
        override val userMessage: String = "Service is temporarily unavailable. Please try again later.",
        val cause: Throwable? = null
    ) : NetworkError()
}
```

## Request Tracking

### Request ID Lifecycle

1. **Generation**: RequestIdInterceptor generates unique ID when request is created
2. **Propagation**: X-Request-ID header added to all outbound requests
3. **Echo**: Server echoes X-Request-ID in response
4. **Logging**: All logs include request_id for correlation
5. **Debugging**: Use request_id to trace across services

### Request ID Format

```
{timestamp}_{random}
```

**Components:**
- `timestamp`: Unix timestamp in milliseconds
- `random`: Random integer for uniqueness
- **Example**: `1704672000000_12345`

## Webhook Response Headers

### Webhook Delivery Responses

Webhook processing uses the following delivery status tracking:

| Status | Description | Headers |
|--------|-------------|----------|
| DELIVERED | Webhook successfully processed | X-Webhook-Delivered-At: `{timestamp}` |
| FAILED | Max retries exceeded | X-Webhook-Last-Error: `{error message}` |
| RETRYING | Scheduled for retry | X-Webhook-Next-Retry-At: `{timestamp}` |

### Idempotency Headers

```
X-Webhook-Idempotency-Key: whk_1704672000000_12345
```

**Purpose**: Prevent duplicate webhook processing
**Format**: `whk_{timestamp}_{random}`
**Uniqueness**: Enforced by database unique constraint

## Best Practices

### For API Clients

1. **Always Check X-Request-ID**: Include in error reports and logs
2. **Respect X-Retry-After**: Wait specified duration before retry on 429
3. **Handle Circuit Breaker**: Respect X-CircuitBreaker-State header
4. **Monitor X-Response-Time**: Track API performance metrics
5. **Exponential Backoff**: Use retry delay calculation on server errors
6. **Request IDs**: Always include in client-side logging for debugging

### For API Implementation

1. **Always Add X-Request-ID**: Echo client's request ID in response
2. **Return X-Retry-After**: On rate limit exceeded (429)
3. **Return X-RateLimit-* Headers**: Provide rate limit context on 429
4. **Add X-Response-Time**: Calculate and return processing time
5. **Standardized Error Format**: Always return ApiErrorResponse structure
6. **Circuit Breaker State**: Add X-CircuitBreaker-State on 503 responses
7. **Retry Count**: Add X-Retry-Count header for retried requests

## Response Examples

### Success Response (API v1)

**GET /api/v1/users**

**Request Headers:**
```
GET /api/v1/users HTTP/1.1
Host: api.apispreadsheets.com
X-Request-ID: req_1704672000000_abc42
Accept: application/json
```

**Response Headers:**
```
HTTP/1.1 200 OK
X-Request-ID: req_1704672000000_abc42
X-Response-Time: 127
Content-Type: application/json
```

**Response Body:**
```json
{
  "data": [
    {
      "first_name": "John",
      "last_name": "Doe",
      "email": "john.doe@example.com",
      "alamat": "Jl. Contoh No. 123",
      "iuran_perwarga": 150000,
      "total_iuran_rekap": 1800000,
      "jumlah_iuran_bulanan": 150000,
      "total_iuran_individu": 150000,
      "pengeluaran_iuran_warga": 50000,
      "pemanfaatan_iuran": "Perbaikan jalan",
      "avatar": "https://example.com/avatar.jpg"
    }
  ],
  "request_id": "req_1704672000000_abc42",
  "timestamp": 1704672000000
}
```

### Error Response (API v1)

**POST /api/v1/messages** (Validation Error)

**Response Headers:**
```
HTTP/1.1 422 Unprocessable Entity
X-Request-ID: req_1704672001000_def45
X-Response-Time: 23
Content-Type: application/json
```

**Response Body:**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": "Content field is required and cannot be blank",
    "field": "content"
  },
  "request_id": "req_1704672001000_def45",
  "timestamp": 1704672001000
}
```

### Rate Limit Error Response

**GET /api/v1/users** (Rate Limit Exceeded)

**Response Headers:**
```
HTTP/1.1 429 Too Many Requests
X-Request-ID: req_1704672002000_ghi67
X-Retry-After: 5000
X-RateLimit-Limit: 10
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1704672050
X-Response-Time: 8
Content-Type: application/json
```

**Response Body:**
```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests. Please try again in 5s.",
    "details": "Rate limit exceeded for endpoint: GET:/api/v1/users. Retry after: 5000ms"
  },
  "request_id": "req_1704672002000_ghi67",
  "timestamp": 1704672002000
}
```

### Circuit Breaker Error Response

**GET /api/v1/users** (Circuit Open)

**Response Headers:**
```
HTTP/1.1 503 Service Unavailable
X-Request-ID: req_1704672003000_jkl89
X-CircuitBreaker-State: OPEN
X-Response-Time: 5
Content-Type: application/json
```

**Response Body:**
```json
{
  "error": {
    "code": "SERVICE_UNAVAILABLE",
    "message": "Service is temporarily unavailable. Please try again later.",
    "details": "Circuit breaker is open. System experienced 3 consecutive failures."
  },
  "request_id": "req_1704672003000_jkl89",
  "timestamp": 1704672003000
}
```

## Related Documentation

- **[API Documentation](API.md)** - Full API endpoint reference
- **[API Integration Patterns](API_INTEGRATION_PATTERNS.md)** - Resilience pattern implementation details
- **[OpenAPI Specification](openapi.yaml)** - Machine-readable API contract

## Glossary

| Term | Definition |
|-------|------------|
| **Exponential Backoff** | Retry strategy with increasing delay between attempts |
| **Jitter** | Random variation in retry delay to prevent synchronized retries |
| **Circuit Breaker** | Pattern to prevent cascading failures by stopping calls to failing services |
| **Idempotency** | Property of operations that can be applied multiple times with same result |
| **Request Tracing** | Distributed tracing technique to follow requests across services |
| **Token Bucket** | Rate limiting algorithm allowing burst requests up to a limit |

---

*Last Updated: 2026-01-10*
*Maintained by: Integration Engineer*
