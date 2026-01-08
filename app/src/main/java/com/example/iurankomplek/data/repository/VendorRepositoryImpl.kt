package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult

class VendorRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiService
) : VendorRepository {
    private val circuitBreaker: CircuitBreaker = ApiConfig.circuitBreaker
    private val maxRetries = com.example.iurankomplek.utils.Constants.Network.MAX_RETRIES
    
    override suspend fun getVendors(): Result<VendorResponse> = executeWithCircuitBreaker {
        apiService.getVendors()
    }

    override suspend fun getVendor(id: String): Result<SingleVendorResponse> = executeWithCircuitBreaker {
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
    ): Result<SingleVendorResponse> = executeWithCircuitBreaker {
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
    ): Result<SingleVendorResponse> = executeWithCircuitBreaker {
        apiService.updateVendor(
            id, name, contactPerson, phoneNumber, email, specialty, address,
            licenseNumber, insuranceInfo, contractStart, contractEnd, isActive
        )
    }
    
    override suspend fun getWorkOrders(): Result<WorkOrderResponse> = executeWithCircuitBreaker {
        apiService.getWorkOrders()
    }
    
    override suspend fun getWorkOrder(id: String): Result<SingleWorkOrderResponse> = executeWithCircuitBreaker {
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
    ): Result<SingleWorkOrderResponse> = executeWithCircuitBreaker {
        apiService.createWorkOrder(
            title, description, category, priority, propertyId, reporterId, estimatedCost
        )
    }
    
    override suspend fun assignVendorToWorkOrder(
        workOrderId: String,
        vendorId: String,
        scheduledDate: String?
    ): Result<SingleWorkOrderResponse> = executeWithCircuitBreaker {
        apiService.assignVendorToWorkOrder(workOrderId, vendorId, scheduledDate)
    }
    
    override suspend fun updateWorkOrderStatus(
        workOrderId: String,
        status: String,
        notes: String?
    ): Result<SingleWorkOrderResponse> = executeWithCircuitBreaker {
        apiService.updateWorkOrderStatus(workOrderId, status, notes)
    }

    private suspend fun <T : Any> executeWithCircuitBreaker(
        apiCall: suspend () -> retrofit2.Response<T>
    ): Result<T> {
        val circuitBreakerResult = circuitBreaker.execute {
            com.example.iurankomplek.utils.RetryHelper.executeWithRetry(
                apiCall = apiCall,
                maxRetries = maxRetries
            )
        }

        return when (circuitBreakerResult) {
            is CircuitBreakerResult.Success -> Result.success(circuitBreakerResult.value)
            is CircuitBreakerResult.Failure -> Result.failure(circuitBreakerResult.exception)
            is CircuitBreakerResult.CircuitOpen -> Result.failure(com.example.iurankomplek.network.model.NetworkError.CircuitBreakerError())
        }
    }
}