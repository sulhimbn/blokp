package com.example.iurankomplek.network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import kotlinx.serialization.serializer
import com.example.iurankomplek.data.api.models.ApiResponse

@Serializable
data class HealthCheckRequest(
    val includeDiagnostics: Boolean = false,
    val includeMetrics: Boolean = false
)

@Serializable
data class HealthCheckResponse(
    val status: String,
    val version: String,
    val uptimeMs: Long,
    val components: Map<String, ComponentHealth>,
    val timestamp: Long,
    val diagnostics: HealthDiagnostics? = null,
    val metrics: HealthMetrics? = null
)

@Serializable
data class ComponentHealth(
    val status: String,
    val healthy: Boolean,
    val message: String? = null,
    val details: Map<String, @Contextual Any>? = null
)

@Serializable
data class HealthDiagnostics(
    val circuitBreakerState: String,
    val circuitBreakerFailures: Int,
    val rateLimitStats: Map<String, RateLimitStats>
)

@Serializable
data class RateLimitStats(
    val requestCount: Int,
    val lastRequestTime: Long
)

@Serializable
data class HealthMetrics(
    val healthScore: Double,
    val totalRequests: Int,
    val successRate: Double,
    val averageResponseTimeMs: Double,
    val errorRate: Double,
    val timeoutCount: Int,
    val rateLimitViolations: Int
)
