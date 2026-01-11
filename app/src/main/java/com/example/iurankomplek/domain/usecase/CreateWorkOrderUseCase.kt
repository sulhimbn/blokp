package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.utils.OperationResult

class CreateWorkOrderUseCase(
    private val vendorRepository: VendorRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        category: String,
        priority: String,
        propertyId: String,
        reporterId: String,
        estimatedCost: Double
    ): OperationResult<Unit> {
        return try {
            vendorRepository.createWorkOrder(
                title, description, category, priority, propertyId, reporterId, estimatedCost
            )
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to create work order")
        }
    }
}
