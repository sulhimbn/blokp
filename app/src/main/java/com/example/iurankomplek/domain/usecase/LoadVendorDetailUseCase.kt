package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.utils.OperationResult

class LoadVendorDetailUseCase(
    private val vendorRepository: VendorRepository
) {
    suspend operator fun invoke(id: String): OperationResult<SingleVendorResponse> {
        return try {
            vendorRepository.getVendor(id)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load vendor details")
        }
    }
}
