package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.utils.OperationResult

class LoadWorkOrdersUseCase(
    private val vendorRepository: VendorRepository
) {
    suspend operator fun invoke(): OperationResult<WorkOrderResponse> {
        return try {
            vendorRepository.getWorkOrders()
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load work orders")
        }
    }
}
