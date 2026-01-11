# Integration Health Monitoring

## Overview

The Integration Health Monitoring system provides real-time observability into API integration health, enabling proactive issue detection, rapid troubleshooting, and performance optimization.

## Table of Contents

1. [Architecture](#architecture)
2. [Health Status Types](#health-status-types)
3. [Health Metrics](#health-metrics)
4. [Usage Examples](#usage-examples)
5. [Health Scoring](#health-scoring)
6. [Alerting Recommendations](#alerting-recommendations)
7. [Testing](#testing)

---

## Architecture

### Components

1. **IntegrationHealthStatus** - Sealed class for typed health states
2. **IntegrationHealthMetrics** - Comprehensive metrics data class
3. **IntegrationHealthMonitor** - Singleton health monitoring service
4. **IntegrationHealthTracker** - Request/response metrics collection

### Integration Points

- **NetworkErrorInterceptor** - Records request/response metrics
- **CircuitBreaker** - Monitors circuit breaker state changes
- **RateLimiterInterceptor** - Tracks rate limit violations
- **ApiConfig** - Provides access to circuit breaker and rate limiter stats

### Data Flow

```
Request → NetworkErrorInterceptor → Record Metrics → IntegrationHealthTracker → IntegrationHealthMonitor → Health Status
Response (Success/Failure)                                  ↓
Error Detection → Update Component Health → Health Report Generation
```

---

## Health Status Types

### Healthy

All integration systems are operational.

```kotlin
val status = IntegrationHealthStatus.Healthy(
    message = "All integration systems operational",
    lastSuccessfulRequest = Date()
)
```

**Indicators**:
- Circuit breaker state: CLOSED
- No rate limit violations
- Low error rate (< 5%)
- Normal response times (< 200ms P95)

---

### Degraded

Integration systems are operational but with reduced performance.

```kotlin
val status = IntegrationHealthStatus.Degraded(
    affectedComponents = listOf("api_service"),
    message = "Retry detected for endpoint: /api/v1/users",
    lastSuccessfulRequest = Date()
)
```

**Indicators**:
- Increased retry rate (> 10%)
- Slower response times (> 200ms P95)
- Occasional timeout errors
- Circuit breaker in HALF_OPEN state

---

### Unhealthy

One or more integration components have failed.

```kotlin
val status = IntegrationHealthStatus.Unhealthy(
    affectedComponents = listOf("api_service"),
    message = "API request failed for endpoint: /api/v1/users",
    errorCause = "HTTP 500"
)
```

**Indicators**:
- High error rate (> 20%)
- Multiple consecutive failures
- Connection errors
- Server-side errors (5xx)

---

### Circuit Open

Circuit breaker has tripped for a specific service.

```kotlin
val status = IntegrationHealthStatus.CircuitOpen(
    service = "api_service",
    message = "Circuit breaker is open for service: api_service",
    failureCount = 5,
    openSince = Date()
)
```

**Indicators**:
- Circuit breaker state: OPEN
- Threshold failures reached (default: 5)
- Service unavailable for recovery period (default: 60s)

---

### Rate Limited

Rate limit threshold has been exceeded.

```kotlin
val status = IntegrationHealthStatus.RateLimited(
    endpoint = "/api/v1/users",
    message = "Rate limit exceeded for endpoint: /api/v1/users",
    requestCount = 65,
    limitExceededAt = Date()
)
```

**Indicators**:
- Request count exceeds limit (default: 60/min)
- HTTP 429 responses received
- Rate limiter blocking requests

---

## Health Metrics

### CircuitBreakerMetrics

Tracks circuit breaker state and history.

```kotlin
data class CircuitBreakerMetrics(
    val state: CircuitBreakerState,        // CLOSED, OPEN, HALF_OPEN
    val failureCount: Int,                // Total failures since last reset
    val successCount: Int,                // Total successes since last reset
    val lastFailureTime: Date?,           // Last failure timestamp
    val lastSuccessTime: Date?,           // Last success timestamp
    val lastStateChange: Date?             // Last state change timestamp
)
```

**Monitoring**: State transitions, failure/success trends, time in OPEN state

---

### RateLimiterMetrics

Tracks request rate and limit violations.

```kotlin
data class RateLimiterMetrics(
    val totalRequests: Int,                        // Total requests tracked
    val requestsInLastMinute: Int,                 // Requests in last 60s
    val requestsInLastSecond: Int,                 // Requests in last 1s
    val rateLimitExceededCount: Int,                // Total limit violations
    val perEndpointStats: Map<String, EndpointStats> // Per-endpoint statistics
)
```

**Monitoring**: Request rates, endpoint-specific usage, violation trends

---

### RequestMetrics

Tracks request performance and success/failure rates.

```kotlin
data class RequestMetrics(
    val totalRequests: Int,           // Total requests
    val successfulRequests: Int,       // Successful requests
    val failedRequests: Int,           // Failed requests
    val retriedRequests: Int,         // Retried requests
    val averageResponseTimeMs: Double, // Average response time
    val minResponseTimeMs: Long,       // Minimum response time
    val maxResponseTimeMs: Long,       // Maximum response time
    val p95ResponseTimeMs: Long,       // 95th percentile
    val p99ResponseTimeMs: Long        // 99th percentile
)
```

**Monitoring**: Response times, success rates, retry rates, latency distribution

---

### ErrorMetrics

Tracks error types and frequencies.

```kotlin
data class ErrorMetrics(
    val totalErrors: Int,                // Total errors
    val timeoutErrors: Int,              // Timeout errors
    val connectionErrors: Int,            // Connection errors
    val httpErrors: Map<Int, Int>,      // HTTP error codes to counts
    val circuitBreakerErrors: Int,       // Circuit breaker errors
    val rateLimitErrors: Int,           // Rate limit errors
    val unknownErrors: Int                // Unknown/unclassified errors
)
```

**Monitoring**: Error distribution, trending issues, error rate by type

---

## Usage Examples

### Basic Health Check

```kotlin
val healthMonitor = IntegrationHealthMonitor.getInstance()

suspend fun checkIntegrationHealth() {
    val healthStatus = healthMonitor.getCurrentHealth()

    when (healthStatus) {
        is IntegrationHealthStatus.Healthy -> {
            Log.i("Health", "System healthy: ${healthStatus.message}")
        }
        is IntegrationHealthStatus.Degraded -> {
            Log.w("Health", "System degraded: ${healthStatus.affectedComponents}")
        }
        is IntegrationHealthStatus.Unhealthy -> {
            Log.e("Health", "System unhealthy: ${healthStatus.errorCause}")
        }
        is IntegrationHealthStatus.CircuitOpen -> {
            Log.e("Health", "Circuit open: ${healthStatus.service}")
        }
        is IntegrationHealthStatus.RateLimited -> {
            Log.w("Health", "Rate limited: ${healthStatus.endpoint}")
        }
    }
}
```

---

### Detailed Health Report

```kotlin
val healthMonitor = IntegrationHealthMonitor.getInstance()

suspend fun generateHealthReport() {
    val report = healthMonitor.getDetailedHealthReport()

    Log.i("Health", "=== Integration Health Report ===")
    Log.i("Health", "Timestamp: ${report.timestamp}")
    Log.i("Health", "Status: ${report.overallStatus.status}")
    Log.i("Health", "Health Score: ${report.healthScore}%")

    Log.i("Health", "=== Circuit Breaker ===")
    report.circuitBreakerStats.forEach { (key, value) ->
        Log.i("Health", "$key: $value")
    }

    Log.i("Health", "=== Request Metrics ===")
    with(report.metrics.requestMetrics) {
        Log.i("Health", "Total: $totalRequests")
        Log.i("Health", "Success: $successfulRequests")
        Log.i("Health", "Failed: $failedRequests")
        Log.i("Health", "Avg Response: ${"%.2f".format(averageResponseTimeMs)}ms")
        Log.i("Health", "P95 Response: ${p95ResponseTimeMs}ms")
    }

    Log.i("Health", "=== Recommendations ===")
    report.recommendations.forEach { recommendation ->
        Log.i("Health", "- $recommendation")
    }
}
```

---

### Recording Manual Health Events

```kotlin
val healthMonitor = IntegrationHealthMonitor.getInstance()

suspend fun recordManualHealthEvent() {
    healthMonitor.recordCircuitBreakerOpen("external_service")
    healthMonitor.recordRateLimitExceeded("/api/v1/users", 65)
}
```

---

### Checking Circuit Breaker Health

```kotlin
val healthMonitor = IntegrationHealthMonitor.getInstance()

suspend fun checkCircuitBreakerHealth() {
    healthMonitor.checkCircuitBreakerHealth()
    val status = healthMonitor.getCurrentHealth()

    if (status is IntegrationHealthStatus.CircuitOpen) {
        Log.e("Health", "Circuit breaker is open for ${status.service}")
        Log.e("Health", "Failures: ${status.failureCount}")
        Log.e("Health", "Open since: ${status.openSince}")
    }
}
```

---

## Health Scoring

The health monitoring system calculates a health score (0-100%) based on multiple factors:

### Scoring Algorithm

```kotlin
fun getHealthScore(): Double {
    var score = 100.0

    // Circuit breaker state penalty
    score -= when (circuitBreakerState) {
        CircuitBreakerState.OPEN -> 50.0
        CircuitBreakerState.HALF_OPEN -> 25.0
        CircuitBreakerState.CLOSED -> 0.0
    }

    // Rate limit violation penalty
    score -= (rateLimitViolations * 10.0).coerceAtMost(30.0)

    // Circuit breaker error penalty
    score -= (circuitBreakerErrors * 15.0).coerceAtMost(45.0)

    // Failure rate penalty
    val failureRate = failedRequests.toDouble() / totalRequests.toDouble().coerceAtLeast(1.0)
    score -= (failureRate * 50.0).coerceAtMost(40.0)

    return score.coerceAtLeast(0.0)
}
```

### Health Score Ranges

| Score Range | Status | Action |
|-------------|---------|---------|
| 90-100% | Healthy | Normal operation, continue monitoring |
| 70-89% | Good | Minor issues, investigate recommendations |
| 50-69% | Degraded | Performance degraded, address recommendations |
| 25-49% | Poor | Significant issues, immediate action required |
| 0-24% | Critical | System unstable, emergency response |

---

## Alerting Recommendations

### Critical Alerts (Score < 25%)

Trigger immediate incident response:

- Circuit breaker OPEN for any service
- Multiple components unhealthy (> 3)
- Error rate > 50%
- No successful requests > 5 minutes

**Response Actions**:
1. Check external service availability
2. Review recent code deployments
3. Monitor database connectivity
4. Check network infrastructure
5. Escalate to on-call engineer

---

### Warning Alerts (Score 25-69%)

Trigger investigation within 15 minutes:

- Circuit breaker HALF_OPEN
- Rate limit violations
- Error rate > 20%
- Response times > 500ms P95

**Response Actions**:
1. Review error logs
2. Check rate limiter configuration
3. Monitor API response times
4. Review retry patterns
5. Consider scaling resources

---

### Informational Alerts (Score 70-89%)

Monitor for trends:

- Increased retry rate (> 10%)
- Slower response times (> 200ms P95)
- Occasional timeout errors
- Degraded component health

**Response Actions**:
1. Document trends for capacity planning
2. Review performance optimization opportunities
3. Monitor health score trends
4. Prepare proactive fixes

---

## Testing

### Unit Tests

```kotlin
@RunWith(MockitoJUnitRunner::class)
class IntegrationHealthMonitorTest {

    private lateinit var healthMonitor: IntegrationHealthMonitor

    @Before
    fun setup() {
        healthMonitor = IntegrationHealthMonitor.getInstance()
        healthMonitor.reset()
    }

    @Test
    fun `health status is healthy initially`() = runTest {
        val status = healthMonitor.getCurrentHealth()

        assertTrue(status is IntegrationHealthStatus.Healthy)
        assertTrue(status.isHealthy())
    }

    @Test
    fun `health status transitions to degraded on retry`() = runTest {
        healthMonitor.recordRetry("/api/v1/users")
        val status = healthMonitor.getCurrentHealth()

        assertTrue(status is IntegrationHealthStatus.Degraded)
        assertTrue(status.isDegraded())
    }

    @Test
    fun `health status transitions to unhealthy on circuit open`() = runTest {
        healthMonitor.recordCircuitBreakerOpen("api_service")
        val status = healthMonitor.getCurrentHealth()

        assertTrue(status is IntegrationHealthStatus.CircuitOpen)
        assertTrue(status.isUnhealthy())
    }

    @Test
    fun `health report includes all metrics`() = runTest {
        healthMonitor.recordRequest("/api/v1/users", 150, true, 200)
        healthMonitor.recordRequest("/api/v1/users", 200, false, 500)

        val report = healthMonitor.getDetailedHealthReport()

        assertNotNull(report.metrics)
        assertNotNull(report.circuitBreakerStats)
        assertNotNull(report.rateLimiterStats)
        assertEquals(2, report.metrics.requestMetrics.totalRequests)
    }

    @Test
    fun `health score calculation is accurate`() = runTest {
        healthMonitor.recordRequest("/api/v1/users", 150, true, 200)
        healthMonitor.recordRequest("/api/v1/users", 200, false, 500)

        val report = healthMonitor.getDetailedHealthReport()

        assertTrue(report.healthScore >= 50.0)
        assertTrue(report.healthScore <= 100.0)
    }
}
```

### Integration Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class IntegrationHealthMonitoringTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var healthMonitor: IntegrationHealthMonitor

    @Before
    fun setup() {
        healthMonitor = IntegrationHealthMonitor.getInstance()
    }

    @Test
    fun `health monitoring works with real API calls`() = runTest {
        val apiService = ApiConfig.getApiService()

        try {
            val response = apiService.getUsers()

            healthMonitor.recordRequest(
                endpoint = "/api/v1/users",
                responseTimeMs = 150,
                success = response.isSuccessful,
                httpCode = response.code()
            )

            val status = healthMonitor.getCurrentHealth()
            assertTrue(status.isHealthy())
        } catch (e: Exception) {
            healthMonitor.recordRequest(
                endpoint = "/api/v1/users",
                responseTimeMs = 0,
                success = false,
                httpCode = null
            )

            val status = healthMonitor.getCurrentHealth()
            assertTrue(status.isDegraded() || status.isUnhealthy())
        }
    }
}
```

---

## Best Practices

### 1. Regular Health Checks

Schedule periodic health checks in production:

```kotlin
class HealthCheckWorker : CoroutineWorker() {
    override suspend fun doWork(): Result {
        val healthMonitor = IntegrationHealthMonitor.getInstance()
        val status = healthMonitor.getCurrentHealth()

        if (status.isUnhealthy()) {
            sendAlert(status)
        }

        return Result.success()
    }
}
```

### 2. Log Health Reports

Generate periodic health reports for debugging:

```kotlin
suspend fun logHealthReport() {
    val report = IntegrationHealthMonitor.getInstance()
        .getDetailedHealthReport()

    Log.i("HealthReport", buildString {
        append("Status: ${report.overallStatus.status}\n")
        append("Score: ${report.healthScore}%\n")
        append("Recommendations:\n")
        report.recommendations.forEach { append("- $it\n") }
    })
}
```

### 3. Monitor Health Score Trends

Track health score over time to identify degradation patterns:

```kotlin
val healthScores = mutableListOf<Double>()

suspend fun trackHealthScore() {
    while (isActive) {
        val report = IntegrationHealthMonitor.getInstance()
            .getDetailedHealthReport()

        healthScores.add(report.healthScore)

        if (healthScores.size > 100) {
            healthScores.removeAt(0)
        }

        checkForDegradationTrend()
        delay(60000) // Check every minute
    }
}

fun checkForDegradationTrend() {
    val recent = healthScores.takeLast(10).average()
    val historical = healthScores.average()

    if (recent < historical - 10) {
        Log.w("Health", "Health score trending down: ${"%.2f".format(recent)}")
    }
}
```

### 4. Set Up Alerting Thresholds

Define thresholds for proactive alerting:

```kotlin
data class AlertThresholds(
    val criticalScore: Double = 25.0,
    val warningScore: Double = 70.0,
    val errorRateThreshold: Double = 0.20,
    val responseTimeThreshold: Long = 500
)

fun checkThresholds(report: HealthReport): List<Alert> {
    val alerts = mutableListOf<Alert>()

    if (report.healthScore < criticalScore) {
        alerts.add(Alert("Critical health score: ${report.healthScore}%"))
    }

    val errorRate = report.metrics.errorMetrics.totalErrors.toDouble() /
                  report.metrics.requestMetrics.totalRequests
    if (errorRate > errorRateThreshold) {
        alerts.add(Alert("High error rate: ${String.format("%.2f", errorRate * 100)}%"))
    }

    return alerts
}
```

---

## Troubleshooting

### Issue: Health Score Suddenly Drops

**Symptoms**: Health score drops from 90% to < 50% unexpectedly

**Investigation**:
1. Check circuit breaker state transitions
2. Review rate limiter violations
3. Analyze error metrics for new error types
4. Check recent code deployments

**Resolution**:
- Identify root cause of errors
- Fix underlying issue (e.g., external service outage)
- Monitor health score recovery

---

### Issue: Persistent Circuit Open State

**Symptoms**: Circuit breaker stays OPEN for extended periods

**Investigation**:
1. Check external service availability
2. Review failure logs for common error types
3. Verify circuit breaker timeout configuration
4. Test service recovery manually

**Resolution**:
- Fix external service issues
- Increase circuit breaker timeout if needed
- Consider implementing fallback services
- Manually reset circuit breaker after fix

---

### Issue: Rate Limit Violations

**Symptoms**: Frequent rate limit warnings (HTTP 429)

**Investigation**:
1. Review request patterns and frequency
2. Check rate limiter configuration
3. Identify endpoints with high request rates
4. Analyze caching opportunities

**Resolution**:
- Implement request batching
- Add caching for frequently accessed data
- Increase rate limit thresholds if appropriate
- Optimize API usage patterns

---

## Conclusion

The Integration Health Monitoring system provides comprehensive observability into API integration health, enabling proactive issue detection, rapid troubleshooting, and performance optimization. By monitoring circuit breaker state, rate limiting, request performance, and error metrics, teams can maintain high availability and user satisfaction.

---

*Last Updated: 2026-01-08*
*Version: 1.0.0*
*Maintained by: Integration Engineer*
