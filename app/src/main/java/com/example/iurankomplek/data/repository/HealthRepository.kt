package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.network.ApiServiceV1
import com.example.iurankomplek.network.model.HealthCheckRequest
import com.example.iurankomplek.network.model.HealthCheckResponse
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.model.ApiErrorCode
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import com.example.iurankomplek.utils.RetryHelper
import retrofit2.Response

interface HealthRepository {
    suspend fun getHealth(includeDiagnostics: Boolean = false, includeMetrics: Boolean = false): Result<HealthCheckResponse>
}

class HealthRepositoryImpl(
    private val apiService: ApiServiceV1
) : HealthRepository, BaseRepository {
    
    override suspend fun getHealth(includeDiagnostics: Boolean, includeMetrics: Boolean): Result<HealthCheckResponse> {
        return executeWithCircuitBreakerV1 {
            val request = HealthCheckRequest(
                includeDiagnostics = includeDiagnostics,
                includeMetrics = includeMetrics
            )
            apiService.getHealth(request)
        }.onFailure { error ->
            throw NetworkError.HttpError(
                code = ApiErrorCode.SERVICE_UNAVAILABLE,
                userMessage = "Health check failed",
                httpCode = 500,
                details = error.message
            )
        }
    }
}
