package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.domain.model.FinancialItem

/**
 * Use case for calculating financial summary with totals
 * Encapsulates business logic for financial report summaries
 *
 * Optimized: Single-pass validation + calculation (eliminates 4 redundant iterations)
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
     * Calculates financial summary from a list of FinancialItem
     * Optimized to single-pass validation + calculation (80% faster for large datasets)
     *
     * @param items List of FinancialItem to calculate summary for
     * @return FinancialSummary with calculated values and validation status
     */
    operator fun invoke(items: List<FinancialItem>): FinancialSummary {
        if (items.isEmpty()) {
            return FinancialSummary(
                totalIuranBulanan = 0,
                totalPengeluaran = 0,
                rekapIuran = 0,
                isValid = true
            )
        }
        
        return try {
            validateAndCalculateInSinglePass(items)
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
    
    /**
     * Validates and calculates financial summary in a single pass
     * Optimized from 5 iterations to 1 iteration (~80% faster)
     *
     * Before (5 iterations):
     * 1. validateAll(items) - iterate all items
     * 2. validateCalculations(items) -> validateAll(items) - iterate all items again
     * 3. validateCalculations(items) -> calculateFinancialTotalsUseCase(items) - iterate all items again
     * 4. calculateFinancialTotalsUseCase(items) -> validateDataItems(items) - iterate all items again
     * 5. calculateFinancialTotalsUseCase(items) -> calculateAllTotalsInSinglePass(items) - iterate all items again
     *
     * After (1 iteration):
     * 1. validateAndCalculateInSinglePass(items) - single iteration for validation + calculation
     *
     * @param items List of FinancialItem to validate and calculate
     * @return FinancialSummary with validated calculated values
     */
    private fun validateAndCalculateInSinglePass(items: List<FinancialItem>): FinancialSummary {
        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (item in items) {
            val iuranPerwarga = item.iuranPerwarga
            val pengeluaranIuranWarga = item.pengeluaranIuranWarga
            val totalIuranIndividuValue = item.totalIuranIndividu
            
            if (iuranPerwarga < 0) {
                throw IllegalArgumentException("Invalid financial data detected")
            }
            if (pengeluaranIuranWarga < 0) {
                throw IllegalArgumentException("Invalid financial data detected")
            }
            if (totalIuranIndividuValue < 0) {
                throw IllegalArgumentException("Invalid financial data detected")
            }
            if (iuranPerwarga > Int.MAX_VALUE / 2) {
                throw IllegalArgumentException("Invalid financial data detected")
            }
            if (pengeluaranIuranWarga > Int.MAX_VALUE / 2) {
                throw IllegalArgumentException("Invalid financial data detected")
            }
            if (totalIuranIndividuValue > Int.MAX_VALUE / 3) {
                throw IllegalArgumentException("Invalid financial data detected")
            }
            
            if (iuranPerwarga > Int.MAX_VALUE - totalIuranBulanan) {
                throw ArithmeticException("Financial calculation would cause overflow")
            }
            totalIuranBulanan += iuranPerwarga
            
            if (pengeluaranIuranWarga > Int.MAX_VALUE - totalPengeluaran) {
                throw ArithmeticException("Financial calculation would cause overflow")
            }
            totalPengeluaran += pengeluaranIuranWarga
            
            var calculatedIuranIndividu = totalIuranIndividuValue
            if (calculatedIuranIndividu > Int.MAX_VALUE / 3) {
                throw ArithmeticException("Financial calculation would cause overflow")
            }
            calculatedIuranIndividu *= 3
            
            if (calculatedIuranIndividu > Int.MAX_VALUE - totalIuranIndividu) {
                throw ArithmeticException("Financial calculation would cause overflow")
            }
            totalIuranIndividu += calculatedIuranIndividu
        }
        
        val rekapIuran = calculateRekapIuran(totalIuranIndividu, totalPengeluaran)
        
        return FinancialSummary(
            totalIuranBulanan = totalIuranBulanan,
            totalPengeluaran = totalPengeluaran,
            rekapIuran = rekapIuran,
            isValid = true
        )
    }
    
    /**
     * Calculates rekap iuran (total iuran individu - total pengeluaran)
     */
    private fun calculateRekapIuran(totalIuranIndividu: Int, totalPengeluaran: Int): Int {
        if (totalIuranIndividu < totalPengeluaran) {
            val minValue = totalIuranIndividu.inv() + 1
            if (totalPengeluaran > Int.MAX_VALUE - minValue) {
                throw ArithmeticException("Rekap iuran calculation would cause underflow")
            }
        }
        
        val result = totalIuranIndividu - totalPengeluaran
        return maxOf(0, result)
    }
}
