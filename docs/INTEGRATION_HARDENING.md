# Integration Hardening Guide

## Overview

This document describes the current integration resilience patterns implemented in the IuranKomplek Android application and provides guidance for further hardening.

## Current Resilience Patterns ✅

### 1. Timeout Configuration ✅ (INT-003 - 2026-01-11)

**Implementation**: `TimeoutInterceptor.kt`

**Configuration**:
- **FAST**: 5 seconds (health checks, status checks)
- **NORMAL**: 30 seconds (default for most operations)
- **SLOW**: 60 seconds (payment initiation)

**Behavior**:
- Per-request timeout based on endpoint path
- Automatic profile mapping via `TimeoutProfileConfig`
- Chain methods override client timeouts

**Usage**: Applied via `ApiConfig` interceptor chain (first interceptor)

**Benefits**:
- Fast operations timeout quickly
- Complex operations get adequate time
- Improved monitor responsiveness
- Zero breaking changes

---

### 2. Circuit Breaker Pattern ✅

**Implementation**: `CircuitBreaker.kt`

**Configuration**:
- **Failure Threshold**: 5 consecutive failures
- **Success Threshold**: 2 successful requests to reset to CLOSED
- **Timeout**: 60 seconds before attempting reset from OPEN
- **Half-Open Max Calls**: 3 calls in HALF_OPEN state

**States**:
- **CLOSED**: Normal operation, all requests pass through
- **OPEN**: Circuit is open, no requests pass through (fail-fast)
- **HALF_OPEN**: Testing if service has recovered, limited requests allowed

**Usage**: All repositories use `BaseRepository.executeWithCircuitBreaker()` methods

**Benefits**:
- Prevents cascading failures
- Automatic recovery detection
- Fail-fast behavior during outages
- Configurable thresholds

---

### 3. Retry Pattern ✅

**Implementation**: `RetryHelper.kt`

**Configuration**:
- **Max Retries**: 3 attempts
- **Initial Delay**: 1000ms (1 second)
- **Max Delay**: 30000ms (30 seconds)
- **Backoff Multiplier**: Exponential (2.0x)
- **Jitter**: 500ms random jitter to prevent thundering herd

**Retryable Exceptions**:
- `IOException` - Network connectivity issues
- `SocketTimeoutException` - Request timeouts
- `UnknownHostException` - DNS resolution failures

**Usage**: Automatic retry via `RetryHelper.executeWithRetry()`

**Benefits**:
- Handles transient failures automatically
- Exponential backoff prevents server overload
- Jitter prevents synchronized retry storms
- Configurable retry policy

---

### 4. Rate Limiting ✅

**Implementation**: `RateLimiterInterceptor.kt`, `RateLimiter.kt`

**Configuration**:
- **Max Requests Per Second**: 10 requests/second
- **Max Requests Per Minute**: 60 requests/minute
- **Sliding Window**: Timestamp-based tracking

**Behavior**:
- Per-client rate limiting
- Sliding window algorithm
- 429 status code when limits exceeded
- Automatic reset per second/minute

**Usage**: Applied via `ApiConfig.rateLimiter` interceptor

**Benefits**:
- Prevents API abuse
- Protects server from overload
- Prevents throttling by API provider
- Configurable limits

---

### 5. Connection Pooling ✅

**Implementation**: `ApiConfig.connectionPool`

**Configuration**:
- **Max Idle Connections**: 5 connections
- **Keep-Alive Duration**: 5 minutes

**Benefits**:
- Reuses HTTP connections
- Reduces connection overhead
- Improves performance
- Resource-efficient

---

### 7. Cache-First Strategy ✅

**Implementation**: `SecurityConfig.kt`, `ApiConfig.kt`

**Configuration**:
- **Connect Timeout**: 30 seconds
- **Read Timeout**: 30 seconds
- **Write Timeout**: 30 seconds

**Benefits**:
- Prevents hanging requests
- Fails fast on unresponsive servers
- Configurable per operation type

---

### 6. Timeout Configuration ✅

**Implementation**: `TimeoutInterceptor.kt`, `Constants.kt`, `ApiConfig.kt` (INT-003 - 2026-01-11)

**Configuration**:
- **Global Defaults**: 30 seconds for connect/read/write
- **FAST_TIMEOUT**: 5 seconds (health checks, status checks)
- **NORMAL_TIMEOUT**: 30 seconds (users, vendors, announcements, messages, posts)
- **SLOW_TIMEOUT**: 60 seconds (payment initiation)

**Behavior**:
- `TimeoutProfile` enum: FAST, NORMAL, SLOW
- `TimeoutProfileConfig.getTimeoutForPath()`: Maps endpoint path to profile
- `TimeoutInterceptor`: Applies per-request timeouts based on path
- Chain methods: `withReadTimeout()`, `withWriteTimeout()`

**Usage**: Applied via `ApiConfig` interceptor chain (first interceptor)

**Benefits**:
- Fast operations timeout quickly (5s)
- Complex operations get adequate time (60s)
- Improved monitor responsiveness (health checks return fast)
- Better resource utilization for different operation types
- Zero breaking changes (backward compatible)

---

### 7. Cache-First Strategy ✅

**Implementation**: `DatabaseCacheStrategy.kt`, `CacheFirstStrategy.kt`

**Configuration**:
- **Cache Freshness**: Configurable TTL (default: 30 minutes)
- **Force Refresh**: Optional bypass of cache
- **Database Caching**: Room-based persistent cache

**Behavior**:
1. Check cache first (if not forcing refresh)
2. Return cached data if fresh
3. Call API if cache missing or stale
4. Update cache on successful API response
5. Serve cache on API failure (fallback)

**Usage**: Repository-level caching strategy pattern

**Benefits**:
- Faster response times (data already available)
- Offline capability (serve cached data)
- Reduced API calls
- Better user experience

---

### 8. Request Tracing ✅

**Implementation**: `RequestIdInterceptor.kt`

**Behavior**:
- Generates unique request ID for each request
- Includes in response headers (`X-Request-Id`)
- Logs for debugging and tracing

**Benefits**:
- Request correlation across logs
- Easier debugging of distributed issues
- Error tracking and monitoring

---

### 9. Health Monitoring ✅

**Implementation**: `HealthCheckInterceptor.kt`, `IntegrationHealthMonitor.kt`

**Monitored Metrics**:
- Request success/failure rates
- Response times (P50, P95, P99)
- Error types (timeout, network, server)
- Circuit breaker state
- Rate limit violations

**Health Check Endpoint**: `POST /api/v1/health`

**Benefits**:
- Real-time integration health visibility
- Proactive issue detection
- Performance monitoring
- Alerting capability

---

## New Fallback Strategy Pattern 🆕

### Purpose

Provide graceful degradation when external services fail, ensuring application remains functional with reduced capabilities instead of complete failure.

### Implementation

**New Files**:
- `FallbackManager.kt` - Manages fallback execution
- `FallbackManagerTest.kt` - Comprehensive test coverage (15 test cases)

**Fallback Types**:
1. **Cached Data Fallback** - Serve previously cached data
2. **Static Data Fallback** - Serve predefined static data
3. **Empty Data Fallback** - Return empty collections
4. **Composite Fallback** - Chain multiple fallback strategies with priority

**Configuration**:
- **Enable/Disable Fallback**: Per-operation configuration
- **Fallback Timeout**: 5 seconds default (prevent long fallback operations)
- **Fallback Logging**: Configurable logging of fallback usage

**Fallback Reasons**:
- `API_FAILURE` - Primary API call failed
- `CIRCUIT_BREAKER_OPEN` - Circuit breaker is open
- `TIMEOUT` - Request timed out
- `NETWORK_ERROR` - Network connectivity issue
- `SERVICE_UNAVAILABLE` - Service returned 503
- `RATE_LIMIT_EXCEEDED` - Rate limit exceeded
- `UNKNOWN_ERROR` - Unknown error type

---

## Integration Hardening Recommendations

### High Priority 🔴

#### 1. Implement Idempotency for POST Operations

**Problem**: Retrying failed POST operations can cause duplicate data creation.

**Solution**:
- Add idempotency key header to all POST requests
- Server must track idempotency keys and return cached result on retry
- Generate unique idempotency key per operation

**Status**: ⚠️ PARTIALLY IMPLEMENTED
- Payment operations use idempotency keys (WebhookQueue)
- Other POST operations (messages, posts) lack idempotency

**Implementation Steps**:
1. Add `IdempotencyInterceptor.kt` to generate keys
2. Include `X-Idempotency-Key` header in all POST requests
3. Add idempotency key to request models
4. Update API documentation with idempotency requirements

**Impact**: HIGH - Prevents duplicate data on retry

---

#### 2. Per-Operation Timeout Configuration

**Problem**: All operations use the same 30-second timeout. Some operations (e.g., image upload) need longer timeouts, others (e.g., health check) should timeout faster.

**Solution**:
- Define timeout profiles for different operation types
- `FAST_TIMEOUT`: 5 seconds (health checks, status checks)
- `NORMAL_TIMEOUT`: 30 seconds (default for most operations)
- `SLOW_TIMEOUT`: 60 seconds (file uploads, complex queries)

**Implementation**:
```kotlin
object TimeoutProfile {
    const val FAST_MS = 5000L
    const val NORMAL_MS = 30000L
    const val SLOW_MS = 60000L
}

class TimeoutInterceptor(
    private val getTimeout: (String) -> Long
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val timeout = getTimeout(request.url.encodedPath)
        
        return request.newBuilder()
            .build()
            .let { chain.withConnectTimeout(timeout, TimeUnit.MILLISECONDS) }
            .let { chain.withReadTimeout(timeout, TimeUnit.MILLISECONDS) }
            .let { chain.withWriteTimeout(timeout, TimeUnit.MILLISECONDS) }
            .proceed(it)
    }
}
```

**Status**: ✅ IMPLEMENTED (INT-003 - 2026-01-11)

**Implementation**:
- `TimeoutProfile` enum with FAST, NORMAL, SLOW profiles
- `TimeoutProfileConfig` object with `getTimeoutMs()` and `getTimeoutForPath()` methods
- `TimeoutInterceptor` interceptor applies per-operation timeouts
- Updated `ApiConfig.kt` to include TimeoutInterceptor in interceptor chain
- Added timeout profile constants to `Constants.Network`:
  - `FAST_TIMEOUT_MS = 5000L` (5 seconds)
  - `NORMAL_TIMEOUT_MS = 30000L` (30 seconds)
  - `SLOW_TIMEOUT_MS = 60000L` (60 seconds)

**Timeout Mappings**:
- **FAST (5s)**: Health checks, status checks
- **NORMAL (30s)**: Users, pemanfaatan, vendors, work orders, announcements, messages, community posts, payment status/confirm
- **SLOW (60s)**: Payment initiation

**Impact**: HIGH - Improved responsiveness for fast operations, appropriate timeouts for complex operations

---

#### 3. Request Priority Queue

**Problem**: All requests have equal priority. Critical requests (e.g., payment confirmation) should have higher priority than non-critical requests (e.g., feed refresh).

**Solution**:
- Implement request priority queue
- Define priority levels: CRITICAL, HIGH, NORMAL, LOW
- Prioritize critical requests during high load or circuit breaker open

**Implementation**:
```kotlin
enum class RequestPriority {
    CRITICAL,   // Payment confirmations, authentication
    HIGH,       // User-initiated actions
    NORMAL,      // Data refresh
    LOW          // Background sync, analytics
}

@Target(AnnotationTarget.FUNCTION)
annotation class RequestPriority(val priority: RequestPriority)
```

**Status**: ❌ NOT IMPLEMENTED

**Impact**: MEDIUM - Better user experience during high load

---

### Medium Priority 🟡

#### 4. Server-Sent Events (SSE) for Real-Time Updates

**Problem**: Polling for updates is inefficient. Real-time updates (e.g., messages, payment status) require frequent polling.

**Solution**:
- Implement SSE for real-time push updates
- Reduce polling overhead
- Provide instant updates

**Use Cases**:
- Payment status updates
- New message notifications
- Community post updates

**Status**: ❌ NOT IMPLEMENTED

**Impact**: MEDIUM - Better real-time experience, reduced API calls

---

#### 5. Bulk Operations API

**Problem**: Fetching large datasets requires many API calls (N+1 query problem).

**Solution**:
- Add bulk API endpoints for batch operations
- Reduce number of API calls
- Improve performance for large datasets

**Example Endpoints**:
- `POST /api/v1/users/bulk` - Create multiple users
- `GET /api/v1/users?ids=id1,id2,id3` - Fetch multiple users
- `POST /api/v1/payments/bulk` - Create multiple payments

**Status**: ❌ NOT IMPLEMENTED

**Impact**: MEDIUM - Reduced API calls, better performance

---

### Low Priority 🟢

#### 6. API Version Migration Guide

**Problem**: Migration from legacy API to v1 API needs documentation for breaking changes.

**Solution**:
- Document all breaking changes
- Provide migration guide for API consumers
- Maintain backward compatibility during transition period

**Status**: ⚠️ PARTIALLY DOCUMENTED (openapi.yaml exists but no migration guide)

**Impact**: LOW - Smoother API evolution

---

#### 7. Request Response Compression

**Problem**: Large payloads increase bandwidth and latency.

**Solution**:
- Enable gzip compression for request/response bodies
- Reduce bandwidth usage
- Improve response times

**Implementation**:
```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(GzipRequestInterceptor())
    .build()
```

**Status**: ❌ NOT IMPLEMENTED

**Impact**: LOW - Reduced bandwidth usage

---

## Integration Resilience Checklist

### Timeout
- [x] Global timeout configuration
- [x] Per-operation timeout profiles (INT-003 - 2026-01-11)
- [ ] Timeout escalation (retry with longer timeout)
- [ ] Timeout monitoring and alerting

### Circuit Breaker
- [x] Circuit breaker implemented
- [x] Configurable thresholds
- [x] Automatic state transitions
- [x] Half-open testing state
- [ ] Per-endpoint circuit breakers
- [ ] Circuit breaker metrics API

### Retry
- [x] Exponential backoff
- [x] Jitter for retry storms
- [x] Configurable retry count
- [x] Retryable exception detection
- [ ] Retry statistics and monitoring
- [ ] Per-endpoint retry policies

### Fallback
- [x] Fallback manager implementation
- [x] Fallback test coverage
- [ ] Fallback strategy registration
- [ ] Fallback metrics and logging
- [ ] Fallback A/B testing capability

### Rate Limiting
- [x] Client-side rate limiting
- [ ] Server-side rate limiting documentation
- [ ] Rate limit headers (X-RateLimit-Remaining, etc.)
- [ ] Rate limit recovery strategy

### Timeout
- [x] Global timeout configuration
- [x] Per-operation timeout profiles (INT-003 - 2026-01-11)
- [ ] Timeout escalation (retry with longer timeout)
- [ ] Timeout monitoring and alerting

### Idempotency
- [x] Payment idempotency
- [ ] POST operation idempotency
- [ ] Idempotency key generation
- [ ] Idempotency conflict handling

---

## Monitoring and Observability

### Key Metrics to Track

**Performance Metrics**:
- P50, P95, P99 response times
- Request success rate
- Circuit breaker state changes
- Fallback usage rate
- Cache hit/miss ratio

**Error Metrics**:
- Error rate by error type
- Timeout rate
- Rate limit violations
- Network error rate

**Business Metrics**:
- Payment success rate
- Data freshness (cache age)
- User-impacting incidents
- Degraded functionality incidents

### Alerting Thresholds

**Critical Alerts**:
- Circuit breaker open for > 5 minutes
- Error rate > 10%
- Payment failure rate > 5%
- Fallback usage rate > 50%

**Warning Alerts**:
- P95 response time > 2 seconds
- Cache hit ratio < 50%
- Rate limit violations > 10/hour

---

## Success Criteria

Integration hardening is complete when:

- [ ] All POST operations have idempotency support
- [ ] Per-operation timeout profiles implemented
- [ ] Request priority queue for critical operations
- [ ] Fallback strategies registered for all repositories
- [ ] Fallback usage metrics available
- [ ] Server-Sent Events for real-time updates
- [ ] Bulk operation API endpoints available
- [ ] Request/response compression enabled
- [ ] All resilience patterns have tests with >80% coverage
- [ ] Monitoring and alerting for all resilience metrics
- [ ] Documentation updated with resilience patterns

---

## References

- **Circuit Breaker Pattern**: [Martin Fowler - Circuit Breaker](https://martinfowler.com/bliki/CircuitBreaker.html)
- **Retry Pattern**: [AWS Architecture - Retry Pattern](https://docs.aws.amazon.com/prescriptive-guidance/latest/patterns/retry.html)
- **Fallback Pattern**: [Microsoft Design Patterns - Fallback](https://docs.microsoft.com/en-us/azure/architecture/patterns/fallback)
- **Rate Limiting**: [Stripe API Rate Limits](https://stripe.com/docs/rate-limits)
- **Idempotency**: [Stripe Idempotency Keys](https://stripe.com/docs/api/idempotency)
