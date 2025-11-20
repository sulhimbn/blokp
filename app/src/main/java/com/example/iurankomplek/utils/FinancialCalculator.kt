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
               item.total_iuran_individu <= Int.MAX_VALUE / 6 // Since we multiply by 3 later
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
            throw IllegalArgumentException("Invalid financial data detected")
        }
        
        var total = 0
        for (item in items) {
            val value = item.iuran_perwarga
            if (value > Int.MAX_VALUE - total) {
                throw ArithmeticException("Total iuran bulanan calculation would cause overflow")
            }
            total += value
        }
        return total
    }
    
    /**
     * Calculates total pengeluaran from a list of DataItems
     */
    fun calculateTotalPengeluaran(items: List<DataItem>): Int {
        if (!validateDataItems(items)) {
            throw IllegalArgumentException("Invalid financial data detected")
        }
        
        var total = 0
        for (item in items) {
            val value = item.pengeluaran_iuran_warga
            if (value > Int.MAX_VALUE - total) {
                throw ArithmeticException("Total pengeluaran calculation would cause overflow")
            }
            total += value
        }
        return total
    }
    
    /**
     * Calculates total iuran individu (multiplied by 3) from a list of DataItems
     */
    fun calculateTotalIuranIndividu(items: List<DataItem>): Int {
        if (!validateDataItems(items)) {
            throw IllegalArgumentException("Invalid financial data detected")
        }
        
        var total = 0
        for (item in items) {
            var value = item.total_iuran_individu
            // Check if multiplying by 3 would cause overflow
            if (value > Int.MAX_VALUE / 3) {
                throw ArithmeticException("Individual iuran calculation would cause overflow")
            }
            value *= 3
            
            if (value > Int.MAX_VALUE - total) {
                throw ArithmeticException("Total iuran individu calculation would cause overflow")
            }
            total += value
        }
        return total
    }
    
    /**
     * Calculates rekap iuran (total iuran individu - total pengeluaran)
     */
    fun calculateRekapIuran(items: List<DataItem>): Int {
        val totalIuranIndividu = calculateTotalIuranIndividu(items)
        val totalPengeluaran = calculateTotalPengeluaran(items)
        
        if (totalIuranIndividu < totalPengeluaran) {
            // Check for potential underflow
            if (totalPengeluaran > Int.MAX_VALUE - (totalIuranIndividu.inv() + 1)) {
                throw ArithmeticException("Rekap iuran calculation would cause underflow")
            }
        }
        
        val result = totalIuranIndividu - totalPengeluaran
        return max(0, result) // Ensure non-negative result
    }
    
    /**
     * Validates all financial calculations for a list of DataItems
     */
    fun validateFinancialCalculations(items: List<DataItem>): Boolean {
        return try {
            calculateTotalIuranBulanan(items)
            calculateTotalPengeluaran(items)
            calculateTotalIuranIndividu(items)
            calculateRekapIuran(items)
            true
        } catch (e: Exception) {
            false
        }
    }
}