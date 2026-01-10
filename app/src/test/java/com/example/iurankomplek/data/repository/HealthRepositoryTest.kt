package com.example.iurankomplek.data.repository

import com.example.iurankomplek.network.ApiServiceV1
import com.example.iurankomplek.network.model.ApiResponse
import com.example.iurankomplek.network.model.HealthCheckRequest
import com.example.iurankomplek.network.model.HealthCheckResponse
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`
import retrofit2.Response

class HealthRepositoryTest {
    
    @Mock
    private lateinit var mockApiService: ApiServiceV1
    
    @Mock
    private lateinit var mockCircuitBreaker: com.example.iurankomplek.network.resilience.CircuitBreaker
    
    private lateinit var healthRepository: HealthRepositoryImpl
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        healthRepository = HealthRepositoryImpl(mockApiService, mockCircuitBreaker)
    }
    
    @Test
    fun `getHealth with success returns health response`() = runTest {
        val healthResponse = HealthCheckResponse(
            status = "HEALTHY",
            version = "1.0.0",
            uptimeMs = 3600000L,
            components = mapOf(
                "api_service" to com.example.iurankomplek.network.model.ComponentHealth(
                    status = "HEALTHY",
                    healthy = true,
                    message = "All systems operational"
                )
            ),
            timestamp = System.currentTimeMillis(),
            diagnostics = null,
            metrics = null
        )
        
        val apiResponse = Response.success(ApiResponse(healthResponse))
        `when`(mockApiService.getHealth(HealthCheckRequest(includeDiagnostics = false, includeMetrics = false)))
            .thenReturn(apiResponse)
        
        val result = healthRepository.getHealth()
        
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("HEALTHY", response.status)
        assertEquals("1.0.0", response.version)
        assertTrue(response.uptimeMs > 0)
    }
    
    @Test
    fun `getHealth with includeDiagnostics returns diagnostics`() = runTest {
        val healthResponse = HealthCheckResponse(
            status = "HEALTHY",
            version = "1.0.0",
            uptimeMs = 3600000L,
            components = mapOf(),
            timestamp = System.currentTimeMillis(),
            diagnostics = com.example.iurankomplek.network.model.HealthDiagnostics(
                circuitBreakerState = "CLOSED",
                circuitBreakerFailures = 0,
                rateLimitStats = mapOf()
            ),
            metrics = null
        )
        
        val apiResponse = Response.success(ApiResponse(healthResponse))
        `when`(mockApiService.getHealth(
            HealthCheckRequest(includeDiagnostics = true, includeMetrics = false)
        )).thenReturn(apiResponse)
        
        val result = healthRepository.getHealth(includeDiagnostics = true, includeMetrics = false)
        
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertNotNull(response.diagnostics)
        assertEquals("CLOSED", response.diagnostics?.circuitBreakerState)
    }
    
    @Test
    fun `getHealth with includeMetrics returns metrics`() = runTest {
        val healthResponse = HealthCheckResponse(
            status = "HEALTHY",
            version = "1.0.0",
            uptimeMs = 3600000L,
            components = mapOf(),
            timestamp = System.currentTimeMillis(),
            diagnostics = null,
            metrics = com.example.iurankomplek.network.model.HealthMetrics(
                healthScore = 95.5,
                totalRequests = 100,
                successRate = 95.0,
                averageResponseTimeMs = 150.0,
                errorRate = 5.0,
                timeoutCount = 1,
                rateLimitViolations = 0
            )
        )
        
        val apiResponse = Response.success(ApiResponse(healthResponse))
        `when`(mockApiService.getHealth(
            HealthCheckRequest(includeDiagnostics = false, includeMetrics = true)
        )).thenReturn(apiResponse)
        
        val result = healthRepository.getHealth(includeDiagnostics = false, includeMetrics = true)
        
        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertNotNull(response.metrics)
        assertEquals(95.5, response.metrics?.healthScore)
        assertEquals(100, response.metrics?.totalRequests)
    }
    
    @Test
    fun `getHealth with HTTP error returns failure`() = runTest {
        val errorResponse = Response.error<ApiResponse<HealthCheckResponse>>(
            503,
            "Service Unavailable".toResponseBody("application/json".toMediaType())
        )
        `when`(mockApiService.getHealth(HealthCheckRequest())).thenReturn(errorResponse)
        
        val result = healthRepository.getHealth()
        
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error is com.example.iurankomplek.network.model.NetworkError.HttpError)
    }
    
    @Test
    fun `getHealth with network error returns failure`() = runTest {
        `when`(mockApiService.getHealth(HealthCheckRequest()))
            .thenThrow(java.net.SocketTimeoutException("Connection timeout"))
        
        val result = healthRepository.getHealth()
        
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `getHealth passes parameters correctly`() = runTest {
        val healthResponse = HealthCheckResponse(
            status = "HEALTHY",
            version = "1.0.0",
            uptimeMs = 3600000L,
            components = mapOf(),
            timestamp = System.currentTimeMillis(),
            diagnostics = null,
            metrics = null
        )
        
        val apiResponse = Response.success(ApiResponse(healthResponse))
        `when`(mockApiService.getHealth(
            HealthCheckRequest(includeDiagnostics = true, includeMetrics = true)
        )).thenReturn(apiResponse)
        
        val result = healthRepository.getHealth(includeDiagnostics = true, includeMetrics = true)
        
        assertTrue(result.isSuccess)
    }
}
