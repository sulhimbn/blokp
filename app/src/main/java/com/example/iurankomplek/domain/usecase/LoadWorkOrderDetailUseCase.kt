package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.utils.OperationResult

class LoadWorkOrderDetailUseCase(
    private val vendorRepository: VendorRepository
) {
    suspend operator fun invoke(id: String): OperationResult<SingleWorkOrderResponse> {
        return try {
            vendorRepository.getWorkOrder(id)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load work order details")
        }
    }
}
