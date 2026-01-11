package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.network.model.CreateVendorRequest
import com.example.iurankomplek.network.model.UpdateVendorRequest
import com.example.iurankomplek.network.model.CreateWorkOrderRequest
import com.example.iurankomplek.network.model.AssignVendorRequest
import com.example.iurankomplek.network.model.UpdateWorkOrderRequest
import com.example.iurankomplek.utils.OperationResult

class VendorRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : VendorRepository, BaseRepository() {

    override suspend fun getVendors(): OperationResult<VendorResponse> = executeWithCircuitBreakerV1 {
        apiService.getVendors()
    }

    override suspend fun getVendor(id: String): OperationResult<SingleVendorResponse> = executeWithCircuitBreakerV1 {
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
    ): OperationResult<SingleVendorResponse> = executeWithCircuitBreakerV1 {
        apiService.createVendor(
            CreateVendorRequest(name, contactPerson, phoneNumber, email, specialty, address,
                licenseNumber, insuranceInfo, contractStart, contractEnd)
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
    ): OperationResult<SingleVendorResponse> = executeWithCircuitBreakerV1 {
        apiService.updateVendor(
            id, UpdateVendorRequest(name, contactPerson, phoneNumber, email, specialty, address,
                licenseNumber, insuranceInfo, contractStart, contractEnd, isActive)
        )
    }

    override suspend fun getWorkOrders(): OperationResult<WorkOrderResponse> = executeWithCircuitBreakerV1 {
        apiService.getWorkOrders()
    }

    override suspend fun getWorkOrder(id: String): OperationResult<SingleWorkOrderResponse> = executeWithCircuitBreakerV1 {
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
    ): OperationResult<SingleWorkOrderResponse> {
        return executeWithCircuitBreakerV1 {
            apiService.createWorkOrder(
                CreateWorkOrderRequest(title, description, category, priority, propertyId, reporterId, estimatedCost)
            )
        }
    }

    override suspend fun assignVendorToWorkOrder(
        workOrderId: String,
        vendorId: String,
        scheduledDate: String?
    ): OperationResult<SingleWorkOrderResponse> = executeWithCircuitBreakerV1 {
        apiService.assignVendorToWorkOrder(workOrderId, AssignVendorRequest(vendorId, scheduledDate))
    }

    override suspend fun updateWorkOrderStatus(
        workOrderId: String,
        status: String,
        notes: String?
    ): OperationResult<SingleWorkOrderResponse> = executeWithCircuitBreakerV1 {
        apiService.updateWorkOrderStatus(workOrderId, UpdateWorkOrderRequest(status, notes))
    }
}