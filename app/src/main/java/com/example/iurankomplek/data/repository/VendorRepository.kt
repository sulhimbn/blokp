package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.model.SingleWorkOrderResponse

interface VendorRepository {
    suspend fun getVendors(): OperationResult<VendorResponse>
    suspend fun getVendor(id: String): OperationResult<SingleVendorResponse>
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
    ): OperationResult<SingleVendorResponse>

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
    ): OperationResult<SingleVendorResponse>

    suspend fun getWorkOrders(): OperationResult<WorkOrderResponse>
    suspend fun getWorkOrder(id: String): OperationResult<SingleWorkOrderResponse>
    suspend fun createWorkOrder(
        title: String,
        description: String,
        category: String,
        priority: String,
        propertyId: String,
        reporterId: String,
        estimatedCost: Double
    ): OperationResult<SingleWorkOrderResponse>

    suspend fun assignVendorToWorkOrder(
        workOrderId: String,
        vendorId: String,
        scheduledDate: String?
    ): OperationResult<SingleWorkOrderResponse>

    suspend fun updateWorkOrderStatus(
        workOrderId: String,
        status: String,
        notes: String?
    ): OperationResult<SingleWorkOrderResponse>
}