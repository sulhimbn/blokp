package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import retrofit2.HttpException
import java.io.IOException

class VendorRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiService
) : VendorRepository {
    private val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker
    private val maxRetries = 3
    
    override suspend fun getVendors(): Result<VendorResponse> = withCircuitBreaker {
        apiService.getVendors()
    }
    
    override suspend fun getVendor(id: String): Result<SingleVendorResponse> = withCircuitBreaker {
        apiService.getVendor(id)
    }
    
    override suspend fun createVendor(
        name: String,
        contactPerson: String,
        phoneNumber: String,
        email: String,
        specialty: String,
        address: String,
        licenseNumber: String,
        insuranceInfo: String,
        contractStart: String,
        contractEnd: String
    ): Result<SingleVendorResponse> = withCircuitBreaker {
        apiService.createVendor(
            name, contactPerson, phoneNumber, email, specialty, address,
            licenseNumber, insuranceInfo, contractStart, contractEnd
        )
    }
    
    override suspend fun updateVendor(
        id: String,
        name: String,
        contactPerson: String,
        phoneNumber: String,
        email: String,
        specialty: String,
        address: String,
        licenseNumber: String,
        insuranceInfo: String,
        contractStart: String,
        contractEnd: String,
        isActive: Boolean
    ): Result<SingleVendorResponse> = withCircuitBreaker {
        apiService.updateVendor(
            id, name, contactPerson, phoneNumber, email, specialty, address,
            licenseNumber, insuranceInfo, contractStart, contractEnd, isActive
        )
    }
    
    override suspend fun getWorkOrders(): Result<WorkOrderResponse> = withCircuitBreaker {
        apiService.getWorkOrders()
    }
    
    override suspend fun getWorkOrder(id: String): Result<SingleWorkOrderResponse> = withCircuitBreaker {
        apiService.getWorkOrder(id)
    }
    
    override suspend fun createWorkOrder(
        title: String,
        description: String,
        category: String,
        priority: String,
        propertyId: String,
        reporterId: String,
        estimatedCost: Double
    ): Result<SingleWorkOrderResponse> = withCircuitBreaker {
        apiService.createWorkOrder(
            title, description, category, priority, propertyId, reporterId, estimatedCost
        )
    }
    
    override suspend fun assignVendorToWorkOrder(
        workOrderId: String,
        vendorId: String,
        scheduledDate: String?
    ): Result<SingleWorkOrderResponse> = withCircuitBreaker {
        apiService.assignVendorToWorkOrder(workOrderId, vendorId, scheduledDate)
    }
    
    override suspend fun updateWorkOrderStatus(
        workOrderId: String,
        status: String,
        notes: String?
    ): Result<SingleWorkOrderResponse> = withCircuitBreaker {
        apiService.updateWorkOrderStatus(workOrderId, status, notes)
    }
    
    private suspend fun <T : Any> withCircuitBreaker(
        apiCall: suspend () -> retrofit2.Response<T>
    ): Result<T> {
        val circuitBreakerResult = circuitBreaker.execute {
            var currentRetry = 0
            var lastException: Exception? = null
            
            while (currentRetry <= maxRetries) {
                try {
                    val response = apiCall()
                    if (response.isSuccessful) {
                        response.body()?.let { return@execute it }
                            ?: throw Exception("Response body is null")
                    } else {
                        val isRetryable = isRetryableError(response.code())
                        if (currentRetry < maxRetries && isRetryable) {
                            val delayMillis = calculateDelay(currentRetry + 1)
                            kotlinx.coroutines.delay(delayMillis)
                            currentRetry++
                        } else {
                            throw HttpException(response)
                        }
                    }
                } catch (e: NetworkError) {
                    lastException = e
                    if (shouldRetryOnNetworkError(e, currentRetry, maxRetries)) {
                        val delayMillis = calculateDelay(currentRetry + 1)
                        kotlinx.coroutines.delay(delayMillis)
                        currentRetry++
                    } else {
                        break
                    }
                } catch (e: Exception) {
                    lastException = e
                    if (shouldRetryOnException(e, currentRetry, maxRetries)) {
                        val delayMillis = calculateDelay(currentRetry + 1)
                        kotlinx.coroutines.delay(delayMillis)
                        currentRetry++
                    } else {
                        break
                    }
                }
            }
            
            throw lastException ?: Exception("Unknown error occurred")
        }
        
        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> Result.success(circuitBreakerResult.value)
            is CircuitBreakerResult.Failure -> Result.failure(circuitBreakerResult.exception)
            is CircuitBreakerResult.CircuitOpen -> Result.failure(NetworkError.CircuitBreakerError())
        }
    }
    
    private fun isRetryableError(httpCode: Int): Boolean {
        return httpCode in 408..429 || httpCode / 100 == 5
    }
    
    private fun shouldRetryOnNetworkError(error: NetworkError, currentRetry: Int, maxRetries: Int): Boolean {
        if (currentRetry >= maxRetries) return false
        
        return when (error) {
            is NetworkError.TimeoutError,
            is NetworkError.ConnectionError -> true
            is NetworkError.HttpError -> {
                error.httpCode in listOf(408, 429) || error.httpCode / 100 == 5
            }
            else -> false
        }
    }
    
    private fun shouldRetryOnException(e: Exception, currentRetry: Int, maxRetries: Int): Boolean {
        if (currentRetry >= maxRetries) return false
        
        return when (e) {
            is java.net.SocketTimeoutException,
            is java.net.UnknownHostException,
            is javax.net.ssl.SSLException -> true
            else -> false
        }
    }
    
    private fun calculateDelay(currentRetry: Int): Long {
        val exponentialDelay = (com.example.iurankomplek.utils.Constants.Network.INITIAL_RETRY_DELAY_MS * Math.pow(2.0, (currentRetry - 1).toDouble())).toLong()
        val jitter = (Math.random() * com.example.iurankomplek.utils.Constants.Network.INITIAL_RETRY_DELAY_MS).toLong()
        return minOf(exponentialDelay + jitter, com.example.iurankomplek.utils.Constants.Network.MAX_RETRY_DELAY_MS)
    }
}