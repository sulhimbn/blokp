package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.data.api.models.SingleVendorResponse
import com.example.iurankomplek.data.api.models.WorkOrderResponse
import com.example.iurankomplek.data.api.models.SingleWorkOrderResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.CacheManager
import retrofit2.HttpException
import java.io.IOException

class VendorRepositoryImpl(
    private val apiService: ApiService
) : VendorRepository {
    
    private val cacheManager = CacheManager.getInstance()
    
    companion object {
        private const val CACHE_KEY_VENDORS = "vendor_list"
        private const val CACHE_KEY_WORK_ORDERS = "work_order_list"
        private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes
    }

    override suspend fun getVendors(): Result<VendorResponse> {
        return getCachedOrNetwork(
            cacheKey = CACHE_KEY_VENDORS,
            ttlMs = CACHE_TTL_MS
        ) {
            executeNetworkVendors()
        }
    }
    
    private suspend fun executeNetworkVendors(): Result<VendorResponse> {
        return try {
            val response = apiService.getVendors()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Response body is null"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getVendor(id: String): Result<SingleVendorResponse> {
        val cacheKey = "vendor_$id"
        return getCachedOrNetwork(
            cacheKey = cacheKey,
            ttlMs = CACHE_TTL_MS
        ) {
            executeNetworkVendor(id)
        }
    }
    
    private suspend fun executeNetworkVendor(id: String): Result<SingleVendorResponse> {
        return try {
            val response = apiService.getVendor(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Response body is null"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
    ): Result<SingleVendorResponse> {
        val result = executeNetworkCreateVendor(
            name, contactPerson, phoneNumber, email, specialty, address,
            licenseNumber, insuranceInfo, contractStart, contractEnd
        )
        // Invalidate vendors cache on create
        invalidateVendorCache()
        return result
    }
    
    private suspend fun executeNetworkCreateVendor(
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
    ): Result<SingleVendorResponse> {
        return try {
            val response = apiService.createVendor(
                name, contactPerson, phoneNumber, email, specialty, address,
                licenseNumber, insuranceInfo, contractStart, contractEnd
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Response body is null"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
    ): Result<SingleVendorResponse> {
        val result = executeNetworkUpdateVendor(
            id, name, contactPerson, phoneNumber, email, specialty, address,
            licenseNumber, insuranceInfo, contractStart, contractEnd, isActive
        )
        // Invalidate vendor caches on update
        invalidateVendorCache()
        cacheManager.remove("vendor_$id")
        return result
    }
    
    private suspend fun executeNetworkUpdateVendor(
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
    ): Result<SingleVendorResponse> {
        return try {
            val response = apiService.updateVendor(
                id, name, contactPerson, phoneNumber, email, specialty, address,
                licenseNumber, insuranceInfo, contractStart, contractEnd, isActive
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Response body is null"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getWorkOrders(): Result<WorkOrderResponse> {
        return getCachedOrNetwork(
            cacheKey = CACHE_KEY_WORK_ORDERS,
            ttlMs = CACHE_TTL_MS
        ) {
            executeNetworkWorkOrders()
        }
    }
    
    private suspend fun executeNetworkWorkOrders(): Result<WorkOrderResponse> {
        return try {
            val response = apiService.getWorkOrders()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Response body is null"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getWorkOrder(id: String): Result<SingleWorkOrderResponse> {
        val cacheKey = "work_order_$id"
        return getCachedOrNetwork(
            cacheKey = cacheKey,
            ttlMs = CACHE_TTL_MS
        ) {
            executeNetworkWorkOrder(id)
        }
    }
    
    private suspend fun executeNetworkWorkOrder(id: String): Result<SingleWorkOrderResponse> {
        return try {
            val response = apiService.getWorkOrder(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Response body is null"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createWorkOrder(
        title: String,
        description: String,
        category: String,
        priority: String,
        propertyId: String,
        reporterId: String,
        estimatedCost: Double
    ): Result<SingleWorkOrderResponse> {
        val result = executeNetworkCreateWorkOrder(
            title, description, category, priority, propertyId, reporterId, estimatedCost
        )
        // Invalidate work orders cache on create
        invalidateWorkOrderCache()
        return result
    }
    
    private suspend fun executeNetworkCreateWorkOrder(
        title: String,
        description: String,
        category: String,
        priority: String,
        propertyId: String,
        reporterId: String,
        estimatedCost: Double
    ): Result<SingleWorkOrderResponse> {
        return try {
            val response = apiService.createWorkOrder(
                title, description, category, priority, propertyId, reporterId, estimatedCost
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Response body is null"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun assignVendorToWorkOrder(
        workOrderId: String,
        vendorId: String,
        scheduledDate: String?
    ): Result<SingleWorkOrderResponse> {
        val result = executeNetworkAssignVendor(workOrderId, vendorId, scheduledDate)
        // Invalidate work order caches on assignment
        cacheManager.remove("work_order_$workOrderId")
        invalidateWorkOrderCache()
        return result
    }
    
    private suspend fun executeNetworkAssignVendor(
        workOrderId: String,
        vendorId: String,
        scheduledDate: String?
    ): Result<SingleWorkOrderResponse> {
        return try {
            val response = apiService.assignVendorToWorkOrder(workOrderId, vendorId, scheduledDate)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Response body is null"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateWorkOrderStatus(
        workOrderId: String,
        status: String,
        notes: String?
    ): Result<SingleWorkOrderResponse> {
        val result = executeNetworkUpdateWorkOrderStatus(workOrderId, status, notes)
        // Invalidate work order caches on status update
        cacheManager.remove("work_order_$workOrderId")
        invalidateWorkOrderCache()
        return result
    }
    
    private suspend fun executeNetworkUpdateWorkOrderStatus(
        workOrderId: String,
        status: String,
        notes: String?
    ): Result<SingleWorkOrderResponse> {
        return try {
            val response = apiService.updateWorkOrderStatus(workOrderId, status, notes)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IOException("Response body is null"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Cache helper methods
    /**
     * Gets data from cache or fetches from network.
     *
     * IMPORTANT: This suppression is REQUIRED due to Kotlin type erasure.
     * - The cacheManager.get<Any> call returns Any? but we need to cast to the appropriate type
     * - This is a known Kotlin language limitation with generic type erasure
     * - The Result.success(cachedData) requires proper typing but due to the generic fetchFromNetwork,
     *   we cannot avoid this cast without significant refactoring (e.g., type tokens, reflection)
     * - This pattern is safe because we immediately wrap in Result which preserves type information
     *
     * @suppress UNCHECKED_CAST is the recommended approach for generic caching in Kotlin
     */
    private suspend fun getCachedOrNetwork(
        cacheKey: String,
        ttlMs: Long,
        fetchFromNetwork: suspend () -> Result<*>
    ): Result<*> {
        @Suppress("UNCHECKED_CAST")
        val cachedData = cacheManager.get<Any>(cacheKey)
        if (cachedData != null) {
            return Result.success(cachedData)
        }

        val result = fetchFromNetwork()
        result.onSuccess { data ->
            cacheManager.put(cacheKey, data, ttlMs)
        }
        return result
    private suspend fun getCachedOrNetwork(
        cacheKey: String,
        ttlMs: Long,
        fetchFromNetwork: suspend () -> Result<*>
    ): Result<*> {
        @Suppress("UNCHECKED_CAST")
        val cachedData = cacheManager.get<Any>(cacheKey)
        if (cachedData != null) {
            return Result.success(cachedData)
        }
        
        val result = fetchFromNetwork()
        result.onSuccess { data ->
            cacheManager.put(cacheKey, data, ttlMs)
        }
        return result
    }
    
    private suspend fun invalidateVendorCache() {
        cacheManager.remove(CACHE_KEY_VENDORS)
    }
    
    private suspend fun invalidateWorkOrderCache() {
        cacheManager.remove(CACHE_KEY_WORK_ORDERS)
    }
}
