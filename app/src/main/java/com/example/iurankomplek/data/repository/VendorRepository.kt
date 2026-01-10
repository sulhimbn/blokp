package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.utils.Result

interface VendorRepository {
    suspend fun getVendors(): Result<VendorResponse>
    suspend fun getVendor(id: String): Result<SingleVendorResponse>
    suspend fun createVendor(
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
    ): Result<SingleVendorResponse>

    suspend fun updateVendor(
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
    ): Result<SingleVendorResponse>

    suspend fun getWorkOrders(): Result<WorkOrderResponse>
    suspend fun getWorkOrder(id: String): Result<SingleWorkOrderResponse>
    suspend fun createWorkOrder(
        title: String,
        description: String,
        category: String,
        priority: String,
        propertyId: String,
        reporterId: String,
        estimatedCost: Double
    ): Result<SingleWorkOrderResponse>

    suspend fun assignVendorToWorkOrder(
        workOrderId: String,
        vendorId: String,
        scheduledDate: String?
    ): Result<SingleWorkOrderResponse>

    suspend fun updateWorkOrderStatus(
        workOrderId: String,
        status: String,
        notes: String?
    ): Result<SingleWorkOrderResponse>
}