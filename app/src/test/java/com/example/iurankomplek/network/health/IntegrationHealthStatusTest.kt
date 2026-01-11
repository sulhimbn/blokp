package com.example.iurankomplek.network.health

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class IntegrationHealthStatusTest {

    @Test
    fun `Healthy status has correct default values`() {
        val healthy = IntegrationHealthStatus.Healthy()

        assertEquals("HEALTHY", healthy.status)
        assertNotNull(healthy.timestamp)
        assertEquals("All integration systems operational", healthy.message)
        assertEquals("All integration systems operational", healthy.details)
        assertNotNull(healthy.lastSuccessfulRequest)
    }

    @Test
    fun `Healthy status with custom message uses provided values`() {
        val customTimestamp = 1234567890000L
        val healthy = IntegrationHealthStatus.Healthy(
            message = "Custom message",
            lastSuccessfulRequest = customTimestamp
        )

        assertEquals("HEALTHY", healthy.status)
        assertEquals("Custom message", healthy.message)
        assertEquals("Custom message", healthy.details)
        assertEquals(customTimestamp, healthy.lastSuccessfulRequest)
    }

    @Test
    fun `Healthy isHealthy returns true`() {
        val healthy = IntegrationHealthStatus.Healthy()
        assertTrue(healthy.isHealthy())
    }

    @Test
    fun `Healthy isDegraded returns false`() {
        val healthy = IntegrationHealthStatus.Healthy()
        assertFalse(healthy.isDegraded())
    }

    @Test
    fun `Healthy isUnhealthy returns false`() {
        val healthy = IntegrationHealthStatus.Healthy()
        assertFalse(healthy.isUnhealthy())
    }

    @Test
    fun `Degraded status with single affected component`() {
        val lastSuccess = 1234567890000L
        val degraded = IntegrationHealthStatus.Degraded(
            affectedComponents = listOf("PaymentService"),
            message = "Payment service slow",
            lastSuccessfulRequest = lastSuccess
        )

        assertEquals("DEGRADED", degraded.status)
        assertNotNull(degraded.timestamp)
        assertEquals(listOf("PaymentService"), degraded.affectedComponents)
        assertEquals("Payment service slow", degraded.message)
        assertEquals(lastSuccess, degraded.lastSuccessfulRequest)
    }

    @Test
    fun `Degraded status with multiple affected components`() {
        val degraded = IntegrationHealthStatus.Degraded(
            affectedComponents = listOf("PaymentService", "NotificationService", "Database"),
            message = "Multiple services slow",
            lastSuccessfulRequest = null
        )

        assertEquals("DEGRADED", degraded.status)
        assertEquals(3, degraded.affectedComponents.size)
        assertTrue(degraded.details.contains("PaymentService"))
        assertTrue(degraded.details.contains("NotificationService"))
        assertTrue(degraded.details.contains("Database"))
        assertTrue(degraded.details.contains("Multiple services slow"))
        assertNull(degraded.lastSuccessfulRequest)
    }

    @Test
    fun `Degraded with empty affected components list`() {
        val degraded = IntegrationHealthStatus.Degraded(
            affectedComponents = emptyList(),
            message = "No specific components",
            lastSuccessfulRequest = null
        )

        assertEquals("DEGRADED", degraded.status)
        assertTrue(degraded.affectedComponents.isEmpty())
        assertTrue(degraded.details.contains("Degraded components:"))
    }

    @Test
    fun `Degraded isHealthy returns false`() {
        val degraded = IntegrationHealthStatus.Degraded(
            affectedComponents = listOf("ServiceA"),
            message = "Slow",
            lastSuccessfulRequest = null
        )
        assertFalse(degraded.isHealthy())
    }

    @Test
    fun `Degraded isDegraded returns true`() {
        val degraded = IntegrationHealthStatus.Degraded(
            affectedComponents = listOf("ServiceA"),
            message = "Slow",
            lastSuccessfulRequest = null
        )
        assertTrue(degraded.isDegraded())
    }

    @Test
    fun `Degraded isUnhealthy returns false`() {
        val degraded = IntegrationHealthStatus.Degraded(
            affectedComponents = listOf("ServiceA"),
            message = "Slow",
            lastSuccessfulRequest = null
        )
        assertFalse(degraded.isUnhealthy())
    }

    @Test
    fun `Degraded details include last successful request when provided`() {
        val lastSuccess = 1234567890000L
        val degraded = IntegrationHealthStatus.Degraded(
            affectedComponents = listOf("ServiceA"),
            message = "Slow",
            lastSuccessfulRequest = lastSuccess
        )

        assertTrue(degraded.details.contains("Last successful request: $lastSuccess"))
    }

    @Test
    fun `Unhealthy status with single failed component`() {
        val unhealthy = IntegrationHealthStatus.Unhealthy(
            affectedComponents = listOf("Database"),
            message = "Connection failed",
            errorCause = "Connection timeout"
        )

        assertEquals("UNHEALTHY", unhealthy.status)
        assertNotNull(unhealthy.timestamp)
        assertEquals(listOf("Database"), unhealthy.affectedComponents)
        assertEquals("Connection failed", unhealthy.message)
        assertEquals("Connection timeout", unhealthy.errorCause)
    }

    @Test
    fun `Unhealthy status with multiple failed components`() {
        val unhealthy = IntegrationHealthStatus.Unhealthy(
            affectedComponents = listOf("Database", "PaymentService"),
            message = "Multiple failures",
            errorCause = "Network partition"
        )

        assertEquals(2, unhealthy.affectedComponents.size)
        assertTrue(unhealthy.details.contains("Database"))
        assertTrue(unhealthy.details.contains("PaymentService"))
        assertTrue(unhealthy.details.contains("Multiple failures"))
        assertTrue(unhealthy.details.contains("Network partition"))
    }

    @Test
    fun `Unhealthy isHealthy returns false`() {
        val unhealthy = IntegrationHealthStatus.Unhealthy(
            affectedComponents = listOf("ServiceA"),
            message = "Failed",
            errorCause = "Error"
        )
        assertFalse(unhealthy.isHealthy())
    }

    @Test
    fun `Unhealthy isDegraded returns false`() {
        val unhealthy = IntegrationHealthStatus.Unhealthy(
            affectedComponents = listOf("ServiceA"),
            message = "Failed",
            errorCause = "Error"
        )
        assertFalse(unhealthy.isDegraded())
    }

    @Test
    fun `Unhealthy isUnhealthy returns true`() {
        val unhealthy = IntegrationHealthStatus.Unhealthy(
            affectedComponents = listOf("ServiceA"),
            message = "Failed",
            errorCause = "Error"
        )
        assertTrue(unhealthy.isUnhealthy())
    }

    @Test
    fun `CircuitOpen status with default message`() {
        val openSince = 1234567890000L
        val circuitOpen = IntegrationHealthStatus.CircuitOpen(
            service = "PaymentService",
            failureCount =5,
            openSince = openSince
        )

        assertEquals("CIRCUIT_OPEN", circuitOpen.status)
        assertNotNull(circuitOpen.timestamp)
        assertEquals("PaymentService", circuitOpen.service)
        assertEquals(5, circuitOpen.failureCount)
        assertEquals(openSince, circuitOpen.openSince)
        assertEquals("Circuit breaker is open for service: PaymentService", circuitOpen.message)
    }

    @Test
    fun `CircuitOpen status with custom message`() {
        val openSince = 1234567890000L
        val circuitOpen = IntegrationHealthStatus.CircuitOpen(
            service = "PaymentService",
            message = "Service unavailable",
            failureCount = 10,
            openSince = openSince
        )

        assertEquals("Service unavailable", circuitOpen.message)
        assertTrue(circuitOpen.details.contains("Service unavailable"))
    }

    @Test
    fun `CircuitOpen with zero failure count`() {
        val circuitOpen = IntegrationHealthStatus.CircuitOpen(
            service = "TestService",
            failureCount = 0,
            openSince = System.currentTimeMillis()
        )

        assertEquals(0, circuitOpen.failureCount)
        assertTrue(circuitOpen.details.contains("Failures: 0"))
    }

    @Test
    fun `CircuitOpen details contain all required information`() {
        val openSince = 1234567890000L
        val circuitOpen = IntegrationHealthStatus.CircuitOpen(
            service = "PaymentService",
            failureCount = 7,
            openSince = openSince
        )

        assertTrue(circuitOpen.details.contains("Service: PaymentService"))
        assertTrue(circuitOpen.details.contains("Failures: 7"))
        assertTrue(circuitOpen.details.contains("Open since: $openSince"))
    }

    @Test
    fun `CircuitOpen isHealthy returns false`() {
        val circuitOpen = IntegrationHealthStatus.CircuitOpen(
            service = "ServiceA",
            failureCount = 3,
            openSince = System.currentTimeMillis()
        )
        assertFalse(circuitOpen.isHealthy())
    }

    @Test
    fun `CircuitOpen isDegraded returns false`() {
        val circuitOpen = IntegrationHealthStatus.CircuitOpen(
            service = "ServiceA",
            failureCount = 3,
            openSince = System.currentTimeMillis()
        )
        assertFalse(circuitOpen.isDegraded())
    }

    @Test
    fun `CircuitOpen isUnhealthy returns true`() {
        val circuitOpen = IntegrationHealthStatus.CircuitOpen(
            service = "ServiceA",
            failureCount = 3,
            openSince = System.currentTimeMillis()
        )
        assertTrue(circuitOpen.isUnhealthy())
    }

    @Test
    fun `RateLimited status with default message`() {
        val limitExceededAt = 1234567890000L
        val rateLimited = IntegrationHealthStatus.RateLimited(
            endpoint = "/api/v1/payments",
            requestCount = 100,
            limitExceededAt = limitExceededAt
        )

        assertEquals("RATE_LIMITED", rateLimited.status)
        assertNotNull(rateLimited.timestamp)
        assertEquals("/api/v1/payments", rateLimited.endpoint)
        assertEquals(100, rateLimited.requestCount)
        assertEquals(limitExceededAt, rateLimited.limitExceededAt)
        assertEquals("Rate limit exceeded for endpoint: /api/v1/payments", rateLimited.message)
    }

    @Test
    fun `RateLimited status with custom message`() {
        val rateLimited = IntegrationHealthStatus.RateLimited(
            endpoint = "/api/v1/users",
            message = "Too many requests",
            requestCount = 200,
            limitExceededAt = System.currentTimeMillis()
        )

        assertEquals("Too many requests", rateLimited.message)
        assertTrue(rateLimited.details.contains("Too many requests"))
    }

    @Test
    fun `RateLimited with zero request count`() {
        val rateLimited = IntegrationHealthStatus.RateLimited(
            endpoint = "/api/test",
            requestCount = 0,
            limitExceededAt = System.currentTimeMillis()
        )

        assertEquals(0, rateLimited.requestCount)
        assertTrue(rateLimited.details.contains("Request count: 0"))
    }

    @Test
    fun `RateLimited details contain all required information`() {
        val limitExceededAt = 1234567890000L
        val rateLimited = IntegrationHealthStatus.RateLimited(
            endpoint = "/api/v1/payments",
            requestCount = 150,
            limitExceededAt = limitExceededAt
        )

        assertTrue(rateLimited.details.contains("Endpoint: /api/v1/payments"))
        assertTrue(rateLimited.details.contains("Request count: 150"))
        assertTrue(rateLimited.details.contains("Limit exceeded at: $limitExceededAt"))
    }

    @Test
    fun `RateLimited isHealthy returns false`() {
        val rateLimited = IntegrationHealthStatus.RateLimited(
            endpoint = "/api/test",
            requestCount = 100,
            limitExceededAt = System.currentTimeMillis()
        )
        assertFalse(rateLimited.isHealthy())
    }

    @Test
    fun `RateLimited isDegraded returns false`() {
        val rateLimited = IntegrationHealthStatus.RateLimited(
            endpoint = "/api/test",
            requestCount = 100,
            limitExceededAt = System.currentTimeMillis()
        )
        assertFalse(rateLimited.isDegraded())
    }

    @Test
    fun `RateLimited isUnhealthy returns true`() {
        val rateLimited = IntegrationHealthStatus.RateLimited(
            endpoint = "/api/test",
            requestCount = 100,
            limitExceededAt = System.currentTimeMillis()
        )
        assertTrue(rateLimited.isUnhealthy())
    }

    @Test
    fun `All status types have non-empty status strings`() {
        val healthy = IntegrationHealthStatus.Healthy()
        val degraded = IntegrationHealthStatus.Degraded(listOf("A"), "M", null)
        val unhealthy = IntegrationHealthStatus.Unhealthy(listOf("B"), "M", "C")
        val circuitOpen = IntegrationHealthStatus.CircuitOpen("Service", 0, System.currentTimeMillis())
        val rateLimited = IntegrationHealthStatus.RateLimited("/api/test", 0, System.currentTimeMillis())

        assertFalse(healthy.status.isEmpty())
        assertFalse(degraded.status.isEmpty())
        assertFalse(unhealthy.status.isEmpty())
        assertFalse(circuitOpen.status.isEmpty())
        assertFalse(rateLimited.status.isEmpty())
    }

    @Test
    fun `All status types have non-null timestamps`() {
        val healthy = IntegrationHealthStatus.Healthy()
        val degraded = IntegrationHealthStatus.Degraded(listOf("A"), "M", null)
        val unhealthy = IntegrationHealthStatus.Unhealthy(listOf("B"), "M", "C")
        val circuitOpen = IntegrationHealthStatus.CircuitOpen("Service", 0, System.currentTimeMillis())
        val rateLimited = IntegrationHealthStatus.RateLimited("/api/test", 0, System.currentTimeMillis())

        assertNotNull(healthy.timestamp)
        assertNotNull(degraded.timestamp)
        assertNotNull(unhealthy.timestamp)
        assertNotNull(circuitOpen.timestamp)
        assertNotNull(rateLimited.timestamp)
    }

    @Test
    fun `All status types have non-empty details strings`() {
        val healthy = IntegrationHealthStatus.Healthy()
        val degraded = IntegrationHealthStatus.Degraded(listOf("A"), "M", null)
        val unhealthy = IntegrationHealthStatus.Unhealthy(listOf("B"), "M", "C")
        val circuitOpen = IntegrationHealthStatus.CircuitOpen("Service", 0, System.currentTimeMillis())
        val rateLimited = IntegrationHealthStatus.RateLimited("/api/test", 0, System.currentTimeMillis())

        assertFalse(healthy.details.isEmpty())
        assertFalse(degraded.details.isEmpty())
        assertFalse(unhealthy.details.isEmpty())
        assertFalse(circuitOpen.details.isEmpty())
        assertFalse(rateLimited.details.isEmpty())
    }

    @Test
    fun `Only Healthy returns true for isHealthy`() {
        val statuses = listOf(
            IntegrationHealthStatus.Healthy(),
            IntegrationHealthStatus.Degraded(listOf("A"), "M", null),
            IntegrationHealthStatus.Unhealthy(listOf("B"), "M", "C"),
            IntegrationHealthStatus.CircuitOpen("Service", 0, System.currentTimeMillis()),
            IntegrationHealthStatus.RateLimited("/api/test", 0, System.currentTimeMillis())
        )

        assertTrue(statuses[0].isHealthy())
        for (i in 1 until statuses.size) {
            assertFalse("Status at index $i should not be healthy", statuses[i].isHealthy())
        }
    }

    @Test
    fun `Only Degraded returns true for isDegraded`() {
        val statuses = listOf(
            IntegrationHealthStatus.Healthy(),
            IntegrationHealthStatus.Degraded(listOf("A"), "M", null),
            IntegrationHealthStatus.Unhealthy(listOf("B"), "M", "C"),
            IntegrationHealthStatus.CircuitOpen("Service", 0, System.currentTimeMillis()),
            IntegrationHealthStatus.RateLimited("/api/test", 0, System.currentTimeMillis())
        )

        assertTrue(statuses[1].isDegraded())
        for (i in listOf(0, 2, 3, 4)) {
            assertFalse("Status at index $i should not be degraded", statuses[i].isDegraded())
        }
    }

    @Test
    fun `Unhealthy CircuitOpen and RateLimited return true for isUnhealthy`() {
        val statuses = listOf(
            IntegrationHealthStatus.Healthy(),
            IntegrationHealthStatus.Degraded(listOf("A"), "M", null),
            IntegrationHealthStatus.Unhealthy(listOf("B"), "M", "C"),
            IntegrationHealthStatus.CircuitOpen("Service", 0, System.currentTimeMillis()),
            IntegrationHealthStatus.RateLimited("/api/test", 0, System.currentTimeMillis())
        )

        assertFalse(statuses[0].isUnhealthy())
        assertFalse(statuses[1].isUnhealthy())
        assertTrue(statuses[2].isUnhealthy())
        assertTrue(statuses[3].isUnhealthy())
        assertTrue(statuses[4].isUnhealthy())
    }

    @Test
    fun `Degraded with special characters in component names`() {
        val degraded = IntegrationHealthStatus.Degraded(
            affectedComponents = listOf("Service_A", "Service-B", "Service.C"),
            message = "Test",
            lastSuccessfulRequest = null
        )

        assertTrue(degraded.details.contains("Service_A"))
        assertTrue(degraded.details.contains("Service-B"))
        assertTrue(degraded.details.contains("Service.C"))
    }

    @Test
    fun `Degraded with unicode characters in message`() {
        val degraded = IntegrationHealthStatus.Degraded(
            affectedComponents = listOf("ServiceA"),
            message = "Service is slow 🐢",
            lastSuccessfulRequest = null
        )

        assertTrue(degraded.details.contains("Service is slow 🐢"))
    }

    @Test
    fun `Degraded with very long component list`() {
        val longList = (1..50).map { "Service$it" }
        val degraded = IntegrationHealthStatus.Degraded(
            affectedComponents = longList,
            message = "Test",
            lastSuccessfulRequest = null
        )

        assertEquals(50, degraded.affectedComponents.size)
        for (i in 1..50) {
            assertTrue(degraded.details.contains("Service$i"))
        }
    }
}
