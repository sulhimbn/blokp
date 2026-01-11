package com.example.iurankomplek.network.health

import java.util.Date

sealed class IntegrationHealthStatus {
    abstract val status: String
    abstract val timestamp: Date
    abstract val details: String

    data class Healthy(
        val message: String = "All integration systems operational",
        val lastSuccessfulRequest: Date = Date()
    ) : IntegrationHealthStatus() {
        override val status: String = "HEALTHY"
        override val timestamp: Date = Date()
        override val details: String = message
    }

    data class Degraded(
        val affectedComponents: List<String>,
        val message: String,
        val lastSuccessfulRequest: Date?
    ) : IntegrationHealthStatus() {
        override val status: String = "DEGRADED"
        override val timestamp: Date = Date()
        override val details: String = buildString {
            append("Degraded components: ${affectedComponents.joinToString(", ")}\n")
            append("Message: $message")
            lastSuccessfulRequest?.let {
                append("\nLast successful request: $it")
            }
        }
    }

    data class Unhealthy(
        val affectedComponents: List<String>,
        val message: String,
        val errorCause: String
    ) : IntegrationHealthStatus() {
        override val status: String = "UNHEALTHY"
        override val timestamp: Date = Date()
        override val details: String = buildString {
            append("Failed components: ${affectedComponents.joinToString(", ")}\n")
            append("Message: $message\n")
            append("Cause: $errorCause")
        }
    }

    data class CircuitOpen(
        val service: String,
        val message: String = "Circuit breaker is open for service: $service",
        val failureCount: Int,
        val openSince: Date
    ) : IntegrationHealthStatus() {
        override val status: String = "CIRCUIT_OPEN"
        override val timestamp: Date = Date()
        override val details: String = buildString {
            append("Service: $service\n")
            append("Failures: $failureCount\n")
            append("Open since: $openSince\n")
            append("Message: $message")
        }
    }

    data class RateLimited(
        val endpoint: String,
        val message: String = "Rate limit exceeded for endpoint: $endpoint",
        val requestCount: Int,
        val limitExceededAt: Date
    ) : IntegrationHealthStatus() {
        override val status: String = "RATE_LIMITED"
        override val timestamp: Date = Date()
        override val details: String = buildString {
            append("Endpoint: $endpoint\n")
            append("Request count: $requestCount\n")
            append("Limit exceeded at: $limitExceededAt\n")
            append("Message: $message")
        }
    }

    fun isHealthy(): Boolean = this is Healthy

    fun isDegraded(): Boolean = this is Degraded

    fun isUnhealthy(): Boolean = this is Unhealthy || this is CircuitOpen || this is RateLimited
}
