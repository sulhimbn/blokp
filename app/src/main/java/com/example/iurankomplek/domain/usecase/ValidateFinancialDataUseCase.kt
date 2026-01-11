package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.domain.model.FinancialItem

/**
 * Use case for validating financial data
 * Encapsulates business logic for data validation
 */
class ValidateFinancialDataUseCase(
    private val calculateFinancialTotalsUseCase: CalculateFinancialTotalsUseCase = CalculateFinancialTotalsUseCase()
) {

    /**
     * Validates a single FinancialItem for financial calculation
     *
     * @param item FinancialItem to validate
     * @return true if valid, false otherwise
     */
    operator fun invoke(item: FinancialItem): Boolean {
        return try {
            validateDataItem(item)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validates a list of FinancialItem for financial calculation
     *
     * @param items List of FinancialItem to validate
     * @return true if all items are valid, false otherwise
     */
    fun validateAll(items: List<FinancialItem>): Boolean {
        return items.all { invoke(it) }
    }

    /**
     * Validates a single FinancialItem for financial calculation
     *
     * @param item FinancialItem to validate
     * @throws IllegalArgumentException if validation fails
     */
    private fun validateDataItem(item: FinancialItem) {
        require(item.iuranPerwarga >= 0) {
            "iuranPerwarga must be >= 0, got ${item.iuranPerwarga}"
        }
        require(item.pengeluaranIuranWarga >= 0) {
            "pengeluaranIuranWarga must be >= 0, got ${item.pengeluaranIuranWarga}"
        }
        require(item.totalIuranIndividu >= 0) {
            "totalIuranIndividu must be >= 0, got ${item.totalIuranIndividu}"
        }
        require(item.iuranPerwarga <= Int.MAX_VALUE / 2) {
            "iuranPerwarga too large, may cause overflow: ${item.iuranPerwarga}"
        }
        require(item.pengeluaranIuranWarga <= Int.MAX_VALUE / 2) {
            "pengeluaranIuranWarga too large, may cause overflow: ${item.pengeluaranIuranWarga}"
        }
        require(item.totalIuranIndividu <= Int.MAX_VALUE / 3) {
            "totalIuranIndividu too large, may cause overflow: ${item.totalIuranIndividu}"
        }
    }

    /**
     * Validates all financial calculations for a list of FinancialItem
     * Performs test calculations to ensure no overflow/underflow
     *
     * @param items List of FinancialItem to validate
     * @return true if all calculations succeed, false otherwise
     */
    fun validateCalculations(items: List<FinancialItem>): Boolean {
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
