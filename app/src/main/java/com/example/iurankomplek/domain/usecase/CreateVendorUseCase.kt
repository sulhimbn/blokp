package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.utils.OperationResult

class CreateVendorUseCase(
    private val vendorRepository: VendorRepository
) {
    suspend operator fun invoke(
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
    ): OperationResult<Unit> {
        return try {
            vendorRepository.createVendor(
                name, contactPerson, phoneNumber, email, specialty, address,
                licenseNumber, insuranceInfo, contractStart, contractEnd
            )
            OperationResult.Success(Unit)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to create vendor")
        }
    }
}
