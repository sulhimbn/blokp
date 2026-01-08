package com.example.iurankomplek.utils

import com.example.iurankomplek.model.DataItem
import kotlin.math.max

/**
 * Utility class for financial calculations with proper validation and sanitization
 */
object FinancialCalculator {
    
    /**
     * Validates a single DataItem for financial calculation
     */
    fun validateDataItem(item: DataItem): Boolean {
        return item.iuran_perwarga >= 0 && 
               item.pengeluaran_iuran_warga >= 0 && 
               item.total_iuran_individu >= 0 &&
               item.iuran_perwarga <= Int.MAX_VALUE / 2 && // Prevent overflow during calculations
               item.pengeluaran_iuran_warga <= Int.MAX_VALUE / 2 &&
               item.total_iuran_individu <= Int.MAX_VALUE / Constants.Financial.IURAN_MULTIPLIER // Since we multiply by multiplier later
    }
    
    /**
     * Validates a list of DataItems for financial calculation
     */
    fun validateDataItems(items: List<DataItem>): Boolean {
        return items.all { validateDataItem(it) }
    }
    
    /**
     * Calculates total iuran bulanan from a list of DataItems
     */
    fun calculateTotalIuranBulanan(items: List<DataItem>): Int {
        if (!validateDataItems(items)) {
            throw IllegalArgumentException(Constants.ErrorMessages.FINANCIAL_DATA_INVALID)
        }
        return calculateTotalIuranBulananInternal(items)
    }
    
    /**
     * Calculates total pengeluaran from a list of DataItems
     */
    fun calculateTotalPengeluaran(items: List<DataItem>): Int {
        if (!validateDataItems(items)) {
            throw IllegalArgumentException(Constants.ErrorMessages.FINANCIAL_DATA_INVALID)
        }
        return calculateTotalPengeluaranInternal(items)
    }
    
    /**
     * Calculates total iuran individu (multiplied by IURAN_MULTIPLIER) from a list of DataItems
     */
    fun calculateTotalIuranIndividu(items: List<DataItem>): Int {
        if (!validateDataItems(items)) {
            throw IllegalArgumentException(Constants.ErrorMessages.FINANCIAL_DATA_INVALID)
        }
        return calculateTotalIuranIndividuInternal(items)
    }
    
    /**
     * Calculates rekap iuran (total iuran individu - total pengeluaran)
     */
    fun calculateRekapIuran(items: List<DataItem>): Int {
        if (!validateDataItems(items)) {
            throw IllegalArgumentException(Constants.ErrorMessages.FINANCIAL_DATA_INVALID)
        }
        return calculateRekapIuranInternal(items)
    }
    
    /**
     * Calculates total iuran bulanan from a list of DataItems (internal, no validation)
     */
    private fun calculateTotalIuranBulananInternal(items: List<DataItem>): Int {
        var total = 0
        for (item in items) {
            val value = item.iuran_perwarga
            if (value > Int.MAX_VALUE - total) {
                throw ArithmeticException(Constants.ErrorMessages.CALCULATION_OVERFLOW_IURAN_BULANAN)
            }
            total += value
        }
        return total
    }
    
    /**
     * Calculates total pengeluaran from a list of DataItems (internal, no validation)
     */
    private fun calculateTotalPengeluaranInternal(items: List<DataItem>): Int {
        var total = 0
        for (item in items) {
            val value = item.pengeluaran_iuran_warga
            if (value > Int.MAX_VALUE - total) {
                throw ArithmeticException(Constants.ErrorMessages.CALCULATION_OVERFLOW_PENGELUARAN)
            }
            total += value
        }
        return total
    }
    
    /**
     * Calculates total iuran individu (multiplied by IURAN_MULTIPLIER) from a list of DataItems (internal, no validation)
     */
    private fun calculateTotalIuranIndividuInternal(items: List<DataItem>): Int {
        var total = 0
        for (item in items) {
            var value = item.total_iuran_individu
            if (value > Int.MAX_VALUE / Constants.Financial.IURAN_MULTIPLIER) {
                throw ArithmeticException(Constants.ErrorMessages.CALCULATION_OVERFLOW_INDIVIDU)
            }
            value *= Constants.Financial.IURAN_MULTIPLIER
            
            if (value > Int.MAX_VALUE - total) {
                throw ArithmeticException(Constants.ErrorMessages.CALCULATION_OVERFLOW_TOTAL_INDIVIDU)
            }
            total += value
        }
        return total
    }
    
    /**
     * Calculates rekap iuran (total iuran individu - total pengeluaran) (internal, no validation)
     */
    private fun calculateRekapIuranInternal(items: List<DataItem>): Int {
        val totalIuranIndividu = calculateTotalIuranIndividuInternal(items)
        val totalPengeluaran = calculateTotalPengeluaranInternal(items)
        
        if (totalIuranIndividu < totalPengeluaran) {
            if (totalPengeluaran > Int.MAX_VALUE - (totalIuranIndividu.inv() + 1)) {
                throw ArithmeticException(Constants.ErrorMessages.CALCULATION_UNDERFLOW_REKAP)
            }
        }
        
        val result = totalIuranIndividu - totalPengeluaran
        return max(0, result)
    }
    
    /**
     * Validates all financial calculations for a list of DataItems
     * Optimized: Validates once instead of 6 times (83.33% reduction in validation overhead)
     * Before: 1 validation (validateDataItems) + 3 validations (calculate methods) + 2 validations (calculateRekapIuran) = 6 total
     * After: 1 validation (validateDataItems) + 0 validations (all internal methods) = 1 total
     */
    fun validateFinancialCalculations(items: List<DataItem>): Boolean {
        return try {
            if (!validateDataItems(items)) {
                return false
            }
            calculateTotalIuranBulananInternal(items)
            calculateTotalPengeluaranInternal(items)
            calculateTotalIuranIndividuInternal(items)
            calculateRekapIuranInternal(items)
            true
        } catch (e: Exception) {
            android.util.Log.e("FinancialCalculator", "Financial validation failed: ${e.message}", e)
            false
        }
    }
}