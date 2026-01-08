package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.dto.LegacyDataItemDto

/**
 * Use case for calculating financial summary with totals
 * Encapsulates business logic for financial report summaries
 */
class CalculateFinancialSummaryUseCase(
    private val validateFinancialDataUseCase: ValidateFinancialDataUseCase = ValidateFinancialDataUseCase(),
    private val calculateFinancialTotalsUseCase: CalculateFinancialTotalsUseCase = CalculateFinancialTotalsUseCase()
) {
    
    /**
     * Result class for financial summary
     */
    data class FinancialSummary(
        val totalIuranBulanan: Int,
        val totalPengeluaran: Int,
        val rekapIuran: Int,
        val isValid: Boolean,
        val validationError: String? = null
    )
    
    /**
     * Calculates financial summary from a list of LegacyDataItemDto
     *
     * @param items List of LegacyDataItemDto to calculate summary for
     * @return FinancialSummary with calculated values and validation status
     */
    operator fun invoke(items: List<LegacyDataItemDto>): FinancialSummary {
        if (items.isEmpty()) {
            return FinancialSummary(
                totalIuranBulanan = 0,
                totalPengeluaran = 0,
                rekapIuran = 0,
                isValid = true
            )
        }
        
        return try {
            if (!validateFinancialDataUseCase.validateAll(items)) {
                return FinancialSummary(
                    totalIuranBulanan = 0,
                    totalPengeluaran = 0,
                    rekapIuran = 0,
                    isValid = false,
                    validationError = "Invalid financial data detected"
                )
            }
            
            if (!validateFinancialDataUseCase.validateCalculations(items)) {
                return FinancialSummary(
                    totalIuranBulanan = 0,
                    totalPengeluaran = 0,
                    rekapIuran = 0,
                    isValid = false,
                    validationError = "Financial calculation would cause overflow"
                )
            }
            
            val totals = calculateFinancialTotalsUseCase(items)
            
            FinancialSummary(
                totalIuranBulanan = totals.totalIuranBulanan,
                totalPengeluaran = totals.totalPengeluaran,
                rekapIuran = totals.rekapIuran,
                isValid = true
            )
        } catch (e: ArithmeticException) {
            FinancialSummary(
                totalIuranBulanan = 0,
                totalPengeluaran = 0,
                rekapIuran = 0,
                isValid = false,
                validationError = "Arithmetic error: ${e.message}"
            )
        } catch (e: IllegalArgumentException) {
            FinancialSummary(
                totalIuranBulanan = 0,
                totalPengeluaran = 0,
                rekapIuran = 0,
                isValid = false,
                validationError = "Validation error: ${e.message}"
            )
        }
    }
}
