package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.network.ApiService
import retrofit2.HttpException
import java.io.IOException

class VendorRepositoryImpl(
    private val apiService: ApiService
) : VendorRepository {
    
    override suspend fun getVendors(): Result<VendorResponse> {
        return try {
            val response = apiService.getVendors()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getVendor(id: String): Result<SingleVendorResponse> {
        return try {
            val response = apiService.getVendor(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
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
        return try {
            val response = apiService.createVendor(
                name, contactPerson, phoneNumber, email, specialty, address,
                licenseNumber, insuranceInfo, contractStart, contractEnd
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
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
        return try {
            val response = apiService.updateVendor(
                id, name, contactPerson, phoneNumber, email, specialty, address,
                licenseNumber, insuranceInfo, contractStart, contractEnd, isActive
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getWorkOrders(): Result<WorkOrderResponse> {
        return try {
            val response = apiService.getWorkOrders()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getWorkOrder(id: String): Result<SingleWorkOrderResponse> {
        return try {
            val response = apiService.getWorkOrder(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
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
        return try {
            val response = apiService.createWorkOrder(
                title, description, category, priority, propertyId, reporterId, estimatedCost
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
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
        return try {
            val response = apiService.assignVendorToWorkOrder(workOrderId, vendorId, scheduledDate)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
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
        return try {
            val response = apiService.updateWorkOrderStatus(workOrderId, status, notes)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}