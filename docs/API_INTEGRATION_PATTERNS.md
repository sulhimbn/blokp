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

**Purpose**: Ensure reliable delivery of webhook events from payment gateways with automatic retries, idempotency guarantees, and queue-based processing.

**Implementation**: `com.example.iurankomplek.payment.*` (WebhookQueue, WebhookReceiver, WebhookEvent, WebhookEventDao)

### Architecture Overview

The webhook reliability system ensures that payment events from external gateways are:
- **Reliably Delivered**: Persistent storage prevents event loss
- **Idempotent**: Duplicate events are safely ignored using idempotency keys
- **Automatically Retried**: Failed events are retried with exponential backoff
- **Monitored**: Real-time observability of webhook processing status
- **Self-Cleaning**: Old events are automatically cleaned up

### Core Components

#### 1. WebhookEvent (Room Entity)

**Location**: `payment/WebhookEvent.kt`

**Schema**:
```kotlin
@Entity(
    tableName = "webhook_events",
    indices = [
        Index(value = ["idempotency_key"], unique = true),
        Index(value = ["status"]),
        Index(value = ["event_type"]),
        Index(value = ["status", "next_retry_at"])
    ]
)
data class WebhookEvent(
    id: Long = 0,
    idempotencyKey: String,
    eventType: String,
    payload: String,
    transactionId: String?,
    status: WebhookDeliveryStatus,
    retryCount: Int = 0,
    maxRetries: Int = 3,
    nextRetryAt: Long? = null,
    deliveredAt: Long? = null,
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis(),
    lastError: String? = null
)
```

**Key Features**:
- Unique idempotency key prevents duplicate events
- Indexes for efficient queries (by status, type, retry time)
- Retry tracking (count, max retries, next retry time)
- Error tracking for debugging
- Timestamps for monitoring and cleanup

**Delivery Status States**:
```kotlin
enum class WebhookDeliveryStatus {
    PENDING,      // Event queued, awaiting processing
    PROCESSING,    // Event is being processed
    DELIVERED,     // Event successfully delivered
    FAILED,        // Event failed (exceeded max retries)
    CANCELLED      // Event cancelled (manual or business rule)
}
```

#### 2. WebhookQueue (Queue-Based Processing)

**Location**: `payment/WebhookQueue.kt`

**Purpose**: Background processing of webhook events with automatic retries

**Configuration**:
```kotlin
// Retry Strategy
INITIAL_RETRY_DELAY_MS = 1000L      // 1 second initial delay
MAX_RETRY_DELAY_MS = 60000L          // 60 seconds max delay
RETRY_BACKOFF_MULTIPLIER = 2.0       // Exponential backoff (2x)
RETRY_JITTER_MS = 500L               // ±500ms jitter
MAX_RETRIES = 5                       // Max retry attempts

// Retention
MAX_EVENT_RETENTION_DAYS = 30         // Clean up events after 30 days
DEFAULT_RETRY_LIMIT = 50              // Max events to retry at once
```

**Behavior**:
1. **Event Enqueue**:
   - Generate unique idempotency key (`whk_{timestamp}_{random}`)
   - Enrich payload with metadata and timestamp
   - Store in database with PENDING status
   - Send event ID to processing channel

2. **Background Processing**:
   - Coroutine-based processing on IO dispatcher
   - Processes events from channel sequentially
   - Marks events as PROCESSING before delivery
   - Attempts payload processing
   - Updates status to DELIVERED on success
   - Calculates retry delay on failure
   - Updates retry count and schedules next retry

3. **Retry Logic**:
   ```
   Retry Delay = min(
       INITIAL_RETRY_DELAY_MS × (BACKOFF_MULTIPLIER ^ retryCount),
       MAX_RETRY_DELAY_MS
   ) ± RETRY_JITTER_MS
   
   Example:
   - Retry 1: 1000ms ± 500ms (0.5-1.5s)
   - Retry 2: 2000ms ± 500ms (1.5-2.5s)
   - Retry 3: 4000ms ± 500ms (3.5-4.5s)
   - Retry 4: 8000ms ± 500ms (7.5-8.5s)
   - Retry 5: 16000ms ± 500ms (15.5-16.5s)
   - Max: 60000ms (60s)
   ```

4. **Failure Handling**:
   - Event marked as FAILED when `retryCount >= maxRetries`
   - Last error message stored for debugging
   - No further automatic retries
   - Manual retry available via WebhookEventCleaner

**Benefits**:
- Non-blocking: Events processed in background
- Durable: Persistent storage survives app crashes
- Idempotent: Duplicate events with same key ignored
- Resilient: Automatic retries with exponential backoff
- Jitter: Prevents retry storms (thundering herd problem)
- Observable: Real-time status tracking via Flow

#### 3. WebhookReceiver (Event Ingress)

**Location**: `payment/WebhookReceiver.kt`

**Purpose**: Receive and validate incoming webhook payloads

**Behavior**:
1. Parse JSON payload to `WebhookPayload` model
2. Validate event type (non-blank)
3. Enqueue to WebhookQueue (if available) or process immediately
4. Log all events with timestamps

**Event Types Supported**:
```kotlin
"payment.success"  -> Update transaction to COMPLETED
"payment.failed"   -> Update transaction to FAILED
"payment.refunded" -> Update transaction to REFUNDED
```

**Payload Model**:
```kotlin
@Serializable
data class WebhookPayload(
    val eventType: String,
    val transactionId: String? = null,
    val metadata: Map<String, String> = emptyMap()
)
```

**Benefits**:
- Validation: Ensures payloads are well-formed
- Flexibility: Optional immediate processing fallback
- Type Safety: Kotlinx serialization for type safety
- Error Handling: Graceful handling of invalid payloads

#### 4. WebhookPayloadProcessor (Business Logic)

**Location**: `payment/WebhookPayloadProcessor.kt`

**Purpose**: Process webhook payloads and update transaction status

**Behavior**:
1. Deserialize JSON payload to `WebhookPayload`
2. Match event type to business logic:
   - `payment.success` → Update transaction to COMPLETED
   - `payment.failed` → Update transaction to FAILED
   - `payment.refunded` → Update transaction to REFUNDED
3. Update transaction in database via TransactionRepository
4. Return `true` on success, `false` on failure

**Error Handling**:
- SerializationException: Invalid JSON → return false
- Transaction not found: Log error → return false
- Invalid transaction ID: Log warning → return false
- Unknown event type: Log info → return true (ignored, not failed)

**Benefits**:
- Decoupled: Business logic separate from queue processing
- Type-safe: Kotlinx serialization prevents runtime errors
- Transaction-safe: Single transaction update per webhook
- Observable: All errors logged with context

#### 5. WebhookEventDao (Data Access)

**Location**: `payment/WebhookEventDao.kt`

**Key Methods**:

**Event Queries**:
```kotlin
suspend fun getEventById(id: Long): WebhookEvent?
suspend fun getEventByIdempotencyKey(idempotencyKey: String): WebhookEvent?
fun getPendingEvents(): Flow<List<WebhookEvent>>
fun getEventsByTransactionId(transactionId: String): Flow<List<WebhookEvent>>
fun getEventsByType(eventType: String): Flow<List<WebhookEvent>>
```

**Processing Queries**:
```kotlin
// Get events ready for retry (status + retry time)
suspend fun getPendingEventsByStatus(
    status: WebhookDeliveryStatus,
    currentTime: Long,
    limit: Int = 10
): List<WebhookEvent>
```

**Update Operations**:
```kotlin
suspend fun updateStatus(id: Long, status: WebhookDeliveryStatus, updatedAt: Long)
suspend fun updateRetryInfo(
    id: Long,
    retryCount: Int,
    nextRetryAt: Long?,
    lastError: String?,
    updatedAt: Long
)
suspend fun markAsDelivered(id: Long, deliveredAt: Long, updatedAt: Long)
suspend fun markAsFailed(id: Long, updatedAt: Long)
```

**Maintenance Operations**:
```kotlin
suspend fun deleteById(id: Long)
suspend fun deleteEventsOlderThan(cutoffTime: Long): Int
suspend fun getFailedEventsOlderThan(cutoffTime: Long): List<WebhookEvent>
suspend fun getDeliveredEventsOlderThan(cutoffTime: Long): List<WebhookEvent>
```

**Transactional Operations**:
```kotlin
// Upsert by idempotency key
suspend fun insertOrUpdate(webhookEvent: WebhookEvent): Long

// Delete old events and return count
suspend fun deleteEventsOlderThanAndCount(cutoffTime: Long): Int
```

**Benefits**:
- Type-safe: Room compile-time query validation
- Efficient: Properly indexed queries
- Observable: Flow-based real-time updates
- Transactional: Complex operations wrapped in transactions

#### 6. WebhookRetryCalculator (Retry Strategy)

**Location**: `payment/WebhookRetryCalculator.kt`

**Purpose**: Calculate retry delay with exponential backoff and jitter

**Algorithm**:
```kotlin
fun calculateRetryDelay(retryCount: Int): Long {
    // 1. Exponential backoff
    val exponentialDelay = INITIAL_RETRY_DELAY_MS *
        Math.pow(RETRY_BACKOFF_MULTIPLIER, retryCount.toDouble()).toLong()
    
    // 2. Cap at maximum delay
    val cappedDelay = min(exponentialDelay, MAX_RETRY_DELAY_MS)
    
    // 3. Add random jitter to prevent thundering herd
    val jitter = secureRandom.nextLong() % (2 * RETRY_JITTER_MS + 1) - RETRY_JITTER_MS
    
    // 4. Ensure non-negative
    return (cappedDelay + jitter).coerceAtLeast(0)
}
```

**Example Calculations**:
```
Retry 0 (first failure): 1000ms ± 500ms → 500-1500ms
Retry 1 (second failure): 2000ms ± 500ms → 1500-2500ms
Retry 2 (third failure): 4000ms ± 500ms → 3500-4500ms
Retry 3 (fourth failure): 8000ms ± 500ms → 7500-8500ms
Retry 4 (fifth failure): 16000ms ± 500ms → 15500-16500ms
Retry 5 (capped at max): 60000ms ± 500ms → 59500-60500ms
```

**Benefits**:
- Exponential Backoff: Increasing delays prevent overwhelming receiver
- Jitter: Random variation prevents synchronized retries
- Capped Delay: Maximum delay prevents excessive waiting
- Thread-Safe: SecureRandom for jitter generation

#### 7. WebhookEventMonitor (Observability)

**Location**: `payment/WebhookEventMonitor.kt`

**Purpose**: Monitor webhook processing status and queue metrics

**Monitoring Methods**:
```kotlin
suspend fun getPendingEventCount(): Int
suspend fun getFailedEventCount(): Int
```

**Real-Time Monitoring**:
```kotlin
// Monitor webhook queue health
val pendingCount = webhookEventMonitor.getPendingEventCount()
val failedCount = webhookEventMonitor.getFailedEventCount()

// Alert if queue is unhealthy
if (pendingCount > 100) {
    Log.w(TAG, "Webhook queue backlog: $pendingCount events pending")
}
if (failedCount > 50) {
    Log.e(TAG, "Webhook delivery issues: $failedCount events failed")
}
```

**Benefits**:
- Real-time observability of webhook queue
- Alert on high pending/failed event counts
- Dashboard metrics for operations
- Proactive issue detection

#### 8. WebhookEventCleaner (Maintenance)

**Location**: `payment/WebhookEventCleaner.kt`

**Purpose**: Retry failed events and clean up old events

**Operations**:

**Retry Failed Events**:
```kotlin
suspend fun retryFailedEvents(limit: Int = DEFAULT_RETRY_LIMIT): Int {
    val cutoffTime = System.currentTimeMillis()
    val failedEvents = webhookEventDao.getPendingEventsByStatus(
        status = WebhookDeliveryStatus.FAILED,
        currentTime = cutoffTime,
        limit = limit
    )
    
    var retriedCount = 0
    for (event in failedEvents) {
        webhookEventDao.updateStatus(event.id, WebhookDeliveryStatus.PENDING)
        eventChannel.send(event.id)
        retriedCount++
    }
    
    return retriedCount
}
```

**Cleanup Old Events**:
```kotlin
suspend fun cleanupOldEvents(): Int {
    val cutoffTime = System.currentTimeMillis() - 
        (MAX_EVENT_RETENTION_DAYS * 24L * 60L * 60L * 1000L)
    
    val deletedCount = webhookEventDao.deleteEventsOlderThan(cutoffTime)
    return deletedCount
}
```

**Benefits**:
- Manual Retry: Admin can retry failed events
- Automatic Cleanup: Prevents database bloat
- Configurable Limits: Control batch sizes

### Database Schema

**Table: webhook_events**

```sql
CREATE TABLE webhook_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    idempotency_key TEXT NOT NULL UNIQUE,
    event_type TEXT NOT NULL,
    payload TEXT NOT NULL,
    transaction_id TEXT,
    status TEXT NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    next_retry_at INTEGER,
    delivered_at INTEGER,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    last_error TEXT
);

CREATE INDEX idx_webhook_idempotency_key ON webhook_events(idempotency_key);
CREATE INDEX idx_webhook_status ON webhook_events(status);
CREATE INDEX idx_webhook_event_type ON webhook_events(event_type);
CREATE INDEX idx_webhook_status_retry ON webhook_events(status, next_retry_at);
```

**Indexes**:
- Unique index on `idempotency_key`: Prevents duplicate events
- Index on `status`: Fast queries for pending/failed events
- Index on `event_type`: Filter events by type
- Composite index on `(status, next_retry_at)`: Efficient retry scheduling

### Idempotency Guarantee

**Purpose**: Ensure duplicate webhook events are processed only once

**Implementation**:
1. Unique idempotency key format: `whk_{timestamp}_{random}`
2. Database unique constraint prevents duplicates
3. Idempotency key embedded in enriched payload
4. `insertIgnoreConflict()` DAO method ignores duplicates

**Example**:
```
First webhook:
  idempotencyKey: "whk_1704710400000_12345"
  status: DELIVERED

Duplicate webhook (same key):
  idempotencyKey: "whk_1704710400000_12345"
  status: IGNORED (not inserted)

```

**Benefits**:
- Duplicate Prevention: Database enforces uniqueness
- Network Retries: Safe to retry on network failures
- Exactly-Once Semantics: Each event processed once
- Audit Trail: First event stored with original key

### Integration with Payment Flow

```
Payment Gateway
    │
    │ (1) User initiates payment
    │
    ▼
Payment Service
    │
    │ (2) Create transaction (PENDING)
    │
    ▼
Database (Transaction: PENDING)
    │
    │ (3) Payment completes
    │
    ▼
Payment Gateway
    │
    │ (4) Send webhook (payment.success)
    │
    ▼
WebhookReceiver
    │
    │ (5) Parse payload
    │     Generate idempotency key
    │
    ▼
WebhookQueue
    │
    │ (6) Enqueue event (PENDING)
    │     Store in database
    │
    ▼
Background Processor
    │
    │ (7) Process event (PROCESSING)
    │     Attempt payload processing
    │     If success: Mark DELIVERED
    │     If failure: Calculate retry delay
    │
    ▼
WebhookPayloadProcessor
    │
    │ (8) Update transaction (COMPLETED)
    │
    ▼
Database (Transaction: COMPLETED)
```

### Configuration

**Constants** (in `Constants.Webhook`):
```kotlin
object Webhook {
    // Retry Configuration
    const val MAX_RETRIES = 5                       // Max retry attempts
    const val INITIAL_RETRY_DELAY_MS = 1000L         // 1 second initial delay
    const val MAX_RETRY_DELAY_MS = 60000L            // 60 seconds max delay
    const val RETRY_BACKOFF_MULTIPLIER = 2.0         // Exponential backoff (2x)
    const val RETRY_JITTER_MS = 500L                 // ±500ms jitter

    // Idempotency
    const val IDEMPOTENCY_KEY_PREFIX = "whk_"        // Key prefix

    // Retention
    const val MAX_EVENT_RETENTION_DAYS = 30          // Clean up after 30 days

    // Cleanup
    const val DEFAULT_RETRY_LIMIT = 50               // Max events to retry
}
```

### Best Practices

**For Webhook Consumers**:

1. **Always Validate Payloads**: Check event type and required fields
2. **Use Idempotency Keys**: Prevent duplicate processing
3. **Handle All Event Types**: Process payment.success, payment.failed, payment.refunded
4. **Log Processing Errors**: Store last error for debugging
5. **Monitor Queue Health**: Track pending and failed event counts
6. **Implement Cleanup**: Delete old events to prevent bloat

**For Webhook Senders**:

1. **Include Idempotency Key**: Enable safe retries
2. **Retry on Network Errors**: Use exponential backoff
3. **Send All Status Changes**: Success, failed, refunded
4. **Use Consistent Format**: JSON with required fields
5. **Document Event Types**: Clearly define all event types

**For Operations**:

1. **Monitor Retry Storms**: Alert on high failed event rates
2. **Check Idempotency Collisions**: Investigate duplicate keys
3. **Review Delivery Times**: Track time-to-delivery metrics
4. **Audit Event History**: Verify all events delivered
5. **Schedule Cleanup**: Regular cleanup of old events

---

## Health Check API

**Purpose**: Provide REST API endpoint for external monitoring tools to query system health status in real-time.

### Health Check Endpoint

#### POST /api/v1/health

Returns system health status, integration metrics, and optional diagnostics.

**Request Body**:
```json
{
  "includeDiagnostics": false,
  "includeMetrics": false
}
```

**Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|-----------|-------------|
| `includeDiagnostics` | Boolean | No | Include detailed circuit breaker and rate limiter diagnostics |
| `includeMetrics` | Boolean | No | Include performance metrics (success rate, response time) |

**Response Body**:
```json
{
  "data": {
    "status": "HEALTHY",
    "version": "1.0.0",
    "uptimeMs": 86400000,
    "components": {
      "circuit_breaker": {
        "status": "HEALTHY",
        "healthy": true,
        "message": "All integration systems operational"
      },
      "rate_limiter": {
        "status": "HEALTHY",
        "healthy": true,
        "message": "Rate limiter within normal limits"
      },
      "api_service": {
        "status": "HEALTHY",
        "healthy": true,
        "message": "API service operational"
      },
      "network": {
        "status": "HEALTHY",
        "healthy": true,
        "message": "Network connectivity normal"
      }
    },
    "timestamp": 1704672000000,
    "diagnostics": {
      "circuitBreakerState": "CLOSED",
      "circuitBreakerFailures": 0,
      "rateLimitStats": {
        "GET:/api/v1/users": {
          "requestCount": 50,
          "lastRequestTime": 1704671995000
        }
      }
    },
    "metrics": {
      "healthScore": 95.5,
      "totalRequests": 100,
      "successRate": 95.0,
      "averageResponseTimeMs": 150.0,
      "errorRate": 5.0,
      "timeoutCount": 1,
      "rateLimitViolations": 0
    }
  },
  "request_id": "req_1234567890_abc42",
  "timestamp": 1704672000000
}
```

### Health Status Values

| Status | Description | Action |
|--------|-------------|---------|
| `HEALTHY` | All systems operational | Continue normal monitoring |
| `DEGRADED` | Some components degraded | Investigate degraded components |
| `UNHEALTHY` | Critical systems failing | Immediate investigation required |
| `CIRCUIT_OPEN` | Circuit breaker is open | Check service availability |
| `RATE_LIMITED` | Rate limit exceeded | Reduce request rate |

### Component Health

| Component | Description | Status Values |
|-----------|-------------|---------------|
| `circuit_breaker` | Circuit breaker state | HEALTHY, CIRCUIT_OPEN |
| `rate_limiter` | Rate limiter status | HEALTHY, RATE_LIMITED |
| `api_service` | API service health | HEALTHY, DEGRADED, UNHEALTHY |
| `network` | Network connectivity | HEALTHY, UNHEALTHY |

### Health Metrics

| Metric | Type | Description | Target |
|--------|-------|-------------|--------|
| `healthScore` | Double | Overall health score (0-100) | >= 90 |
| `totalRequests` | Integer | Total requests tracked | N/A |
| `successRate` | Double | Percentage of successful requests | >= 95% |
| `averageResponseTimeMs` | Double | Average response time in ms | <= 500 |
| `errorRate` | Double | Percentage of failed requests | <= 5% |
| `timeoutCount` | Integer | Number of timeout errors | N/A |
| `rateLimitViolations` | Integer | Number of rate limit violations | 0 |

### Usage Example

**Basic Health Check**:
```kotlin
val result = healthRepository.getHealth()
result.onSuccess { response ->
    when (response.status) {
        "HEALTHY" -> println("System operational")
        "DEGRADED" -> println("System degraded")
        "UNHEALTHY" -> println("System unavailable")
    }
}
```

**Health Check with Diagnostics**:
```kotlin
healthRepository.getHealth(
    includeDiagnostics = true,
    includeMetrics = false
)
```

**Full Health Check**:
```kotlin
healthRepository.getHealth(
    includeDiagnostics = true,
    includeMetrics = true
)
```

### Integration with Monitoring Tools

**Prometheus Health Check**:
```bash
curl -X POST https://api.example.com/api/v1/health \
  -H "Content-Type: application/json" \
  -d '{"includeDiagnostics": true, "includeMetrics": true}'
```

**Datadog Synthetics**:
- Create synthetic monitor for POST /api/v1/health
- Alert on status != "HEALTHY"
- Monitor healthScore metric (alert if < 90)

**Uptime Robot Monitoring**:
- Configure HTTP POST monitor for /api/v1/health
- Parse JSON response.status field
- Set up alerts for status changes

### Automatic Health Tracking

**HealthCheckInterceptor**: Automatically tracks request health for all API calls
- Records request metrics via IntegrationHealthMonitor
- Logs requests in debug mode
- Monitors circuit breaker and rate limit events
- Skips health endpoint to avoid infinite recursion

**Intercepted Requests**: All non-health API endpoints automatically monitored
**Monitoring Data**:
- Request response times
- Success/failure counts
- Timeout errors
- Rate limit violations
- Circuit breaker state changes

### Best Practices

**For Monitoring Tools**:
1. **Check Health Endpoint**: Poll POST /api/v1/health every 30-60 seconds
2. **Parse JSON Response**: Extract `status` field for alerting
3. **Monitor Health Score**: Alert if `healthScore` drops below 90
4. **Check Component Health**: Investigate specific component degradation
5. **Set Up Alerts**: Notify on status changes (HEALTHY → DEGRADED/UNHEALTHY)

**For Health Check Implementation**:
1. **Fast Response**: Health check should complete within 500ms
2. **Timeout Protection**: Set 5s timeout for health check endpoint
3. **Circuit Breaker Bypass**: Health check not subject to circuit breaker
4. **Rate Limit Exemption**: Health check endpoint not rate-limited
5. **Minimal Dependencies**: Avoid complex dependencies in health check path

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

### Webhook Queue Monitoring

```kotlin
// Monitor webhook queue health
val pendingCount = webhookEventMonitor.getPendingEventCount()
val failedCount = webhookEventMonitor.getFailedEventCount()

// Check webhook queue status
if (pendingCount > 100) {
    Log.w("Webhook", "High pending event count: $pendingCount")
}
if (failedCount > 50) {
    Log.e("Webhook", "High failed event count: $failedCount")
}
```

**Webhook Queue Metrics**:
```kotlin
// Get recent webhook events
val recentEvents = webhookEventDao.getAllEvents(limit = 100)

// Filter by status
val pendingEvents = webhookEventDao.getPendingEvents().first()
val failedEvents = webhookEventDao.getFailedEventsOlderThan(
    System.currentTimeMillis() - 24 * 60 * 60 * 1000
)

// Get events by transaction
val transactionEvents = webhookEventDao.getEventsByTransactionId("tx_123").first()

// Get events by type
val paymentEvents = webhookEventDao.getEventsByType("payment.success").first()
```

**Manual Retry of Failed Events**:
```kotlin
// Retry failed events (e.g., after fixing issue)
val retriedCount = webhookEventCleaner.retryFailedEvents(limit = 50)
Log.i("Webhook", "Retried $retriedCount failed events")
```

**Cleanup Old Events**:
```kotlin
// Clean up events older than 30 days
val deletedCount = webhookEventCleaner.cleanupOldEvents()
Log.i("Webhook", "Cleaned up $deletedCount old webhook events")
```

---

## Testing

All integration patterns have comprehensive test coverage:
- `CircuitBreakerTest`: 15 test cases
- `RateLimiterInterceptorTest`: 11 test cases
- `NetworkErrorInterceptorTest`: 17 test cases
- `RequestIdInterceptorTest`: 8 test cases
- `RetryableRequestInterceptorTest`: 14 test cases
- `WebhookQueueTest`: 12 test cases
- `WebhookReceiverTest`: 9 test cases
- `WebhookPayloadProcessorTest`: 8 test cases
- `WebhookEventMonitorTest`: 2 test cases
- `WebhookEventCleanerTest`: 3 test cases
- `WebhookRetryCalculatorTest`: 6 test cases
- `WebhookEventDaoTest`: 13 test cases

Total: **118 test cases** for integration patterns and webhook reliability

**Webhook Test Coverage**:
- **WebhookQueue**: 12 tests - enqueue, process events, retry logic, jitter calculation
- **WebhookReceiver**: 9 tests - payload parsing, event handling, immediate processing
- **WebhookPayloadProcessor**: 8 tests - event processing, transaction updates, error handling
- **WebhookEventMonitor**: 2 tests - pending count, failed count
- **WebhookEventCleaner**: 3 tests - retry failed events, cleanup old events
- **WebhookRetryCalculator**: 6 tests - exponential backoff, jitter, capped delays
- **WebhookEventDao**: 13 tests - CRUD operations, Flow queries, transactional methods

---

## Future Enhancements

### Integration Patterns
1. **Metrics Collection**: Integrate with Firebase Performance Monitoring
2. **Dynamic Thresholds**: Adjust circuit breaker thresholds based on time of day
3. **Distributed Rate Limiting**: Redis-based rate limiting for multi-instance deployments
4. **Circuit Breaker Metrics**: Expose metrics via HTTP endpoint
5. **Smart Retry**: Machine learning-based retry delay optimization

### Webhook Reliability
1. **Webhook Metrics Dashboard**: Real-time visualization of webhook queue health
2. **Dead Letter Queue**: Separate storage for permanently failed events
3. **Event Replay**: Manual replay of historical webhook events
4. **Priority Queue**: Urgent events processed before normal events
5. **Webhook Validation**: Schema validation before processing
6. **Batch Processing**: Process multiple webhook events in single transaction
7. **Event Correlation**: Correlate webhooks with transaction lifecycle
8. **Alerting**: Notifications on webhook delivery failures
9. **Performance Metrics**: Track time-to-delivery metrics
10. **Event Replay API**: Replay events for testing and recovery

---

*Last Updated: 2026-01-08*
*Maintained by: Integration Engineer*
