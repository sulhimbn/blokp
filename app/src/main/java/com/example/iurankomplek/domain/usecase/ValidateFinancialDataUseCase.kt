package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.dto.LegacyDataItemDto

/**
 * Use case for validating financial data
 * Encapsulates business logic for data validation
 */
class ValidateFinancialDataUseCase(
    private val calculateFinancialTotalsUseCase: CalculateFinancialTotalsUseCase = CalculateFinancialTotalsUseCase()
) {
    
    /**
     * Validates a single LegacyDataItemDto for financial calculation
     *
     * @param item LegacyDataItemDto to validate
     * @return true if valid, false otherwise
     */
    operator fun invoke(item: LegacyDataItemDto): Boolean {
        return try {
            validateDataItem(item)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Validates a list of LegacyDataItemDto for financial calculation
     *
     * @param items List of LegacyDataItemDto to validate
     * @return true if all items are valid, false otherwise
     */
    fun validateAll(items: List<LegacyDataItemDto>): Boolean {
        return items.all { invoke(it) }
    }
    
    /**
     * Validates a single LegacyDataItemDto for financial calculation
     *
     * @param item LegacyDataItemDto to validate
     * @throws IllegalArgumentException if validation fails
     */
    private fun validateDataItem(item: LegacyDataItemDto) {
        require(item.iuran_perwarga >= 0) { 
            "iuran_perwarga must be >= 0, got ${item.iuran_perwarga}" 
        }
        require(item.pengeluaran_iuran_warga >= 0) { 
            "pengeluaran_iuran_warga must be >= 0, got ${item.pengeluaran_iuran_warga}" 
        }
        require(item.total_iuran_individu >= 0) { 
            "total_iuran_individu must be >= 0, got ${item.total_iuran_individu}" 
        }
        require(item.iuran_perwarga <= Int.MAX_VALUE / 2) { 
            "iuran_perwarga too large, may cause overflow: ${item.iuran_perwarga}" 
        }
        require(item.pengeluaran_iuran_warga <= Int.MAX_VALUE / 2) { 
            "pengeluaran_iuran_warga too large, may cause overflow: ${item.pengeluaran_iuran_warga}" 
        }
        require(item.total_iuran_individu <= Int.MAX_VALUE / 3) { 
            "total_iuran_individu too large, may cause overflow: ${item.total_iuran_individu}" 
        }
    }
    
    /**
     * Validates all financial calculations for a list of LegacyDataItemDto
     * Performs test calculations to ensure no overflow/underflow
     *
     * @param items List of LegacyDataItemDto to validate
     * @return true if all calculations succeed, false otherwise
     */
    fun validateCalculations(items: List<LegacyDataItemDto>): Boolean {
        return try {
            if (!validateAll(items)) {
                return false
            }
            
            calculateFinancialTotalsUseCase(items)
            true
        } catch (e: Exception) {
            false
        }
    }
}
