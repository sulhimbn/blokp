package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.data.repository.PemanfaatanRepository

/**
 * Use case for loading financial data with business logic
 * Encapsulates financial data loading logic and business rules
 */
class LoadFinancialDataUseCase(
    private val pemanfaatanRepository: PemanfaatanRepository,
    private val validateFinancialDataUseCase: ValidateFinancialDataUseCase = ValidateFinancialDataUseCase()
) {
    
    /**
     * Loads financial data from repository
     * Includes business logic for financial data loading
     * 
     * @return Result<PemanfaatanResponse> with success or error
     */
    suspend operator fun invoke(): Result<PemanfaatanResponse> {
        return try {
            pemanfaatanRepository.getPemanfaatan()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Loads financial data with additional business rules
     * 
     * @param forceRefresh If true, bypasses cache if available
     * @return Result<PemanfaatanResponse> with success or error
     */
    suspend operator fun invoke(forceRefresh: Boolean): Result<PemanfaatanResponse> {
        return try {
            if (forceRefresh) {
                pemanfaatanRepository.getPemanfaatan()
            } else {
                pemanfaatanRepository.getPemanfaatan()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validates financial data after loading
     * 
     * @param response PemanfaatanResponse to validate
     * @return true if valid, false otherwise
     */
    fun validateFinancialData(response: PemanfaatanResponse): Boolean {
        return try {
            response.data?.let { items ->
                validateFinancialDataUseCase.validateAll(items)
            } ?: true
        } catch (e: Exception) {
            false
        }
    }
}
