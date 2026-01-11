package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.utils.OperationResult

class LoadVendorsUseCase(
    private val vendorRepository: VendorRepository
) {
    suspend operator fun invoke(): OperationResult<VendorResponse> {
        return try {
            vendorRepository.getVendors()
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load vendors")
        }
    }
}
