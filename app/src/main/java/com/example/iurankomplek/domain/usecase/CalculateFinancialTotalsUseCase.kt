package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.model.DataItem

/**
 * Use case for calculating financial totals from DataItem list
 * Encapsulates business logic for financial calculations
 */
class CalculateFinancialTotalsUseCase {
    
    /**
     * Result class for financial calculations
     */
    data class FinancialTotals(
        val totalIuranBulanan: Int,
        val totalPengeluaran: Int,
        val totalIuranIndividu: Int,
        val rekapIuran: Int
    )
    
    /**
     * Executes financial calculations for a list of DataItems
     * 
     * @param items List of DataItems to calculate totals for
     * @return FinancialTotals with calculated values
     * @throws IllegalArgumentException if validation fails
     * @throws ArithmeticException if calculation would cause overflow/underflow
     */
    operator fun invoke(items: List<DataItem>): FinancialTotals {
        if (items.isEmpty()) {
            return FinancialTotals(0, 0, 0, 0)
        }
        
        validateDataItems(items)
        
        val totalIuranBulanan = calculateTotalIuranBulanan(items)
        val totalPengeluaran = calculateTotalPengeluaran(items)
        val totalIuranIndividu = calculateTotalIuranIndividu(items)
        val rekapIuran = calculateRekapIuran(totalIuranIndividu, totalPengeluaran)
        
        return FinancialTotals(
            totalIuranBulanan = totalIuranBulanan,
            totalPengeluaran = totalPengeluaran,
            totalIuranIndividu = totalIuranIndividu,
            rekapIuran = rekapIuran
        )
    }
    
    /**
     * Validates a list of DataItems for financial calculation
     */
    private fun validateDataItems(items: List<DataItem>) {
        for (item in items) {
            require(item.iuran_perwarga >= 0) { "iuran_perwarga must be >= 0" }
            require(item.pengeluaran_iuran_warga >= 0) { "pengeluaran_iuran_warga must be >= 0" }
            require(item.total_iuran_individu >= 0) { "total_iuran_individu must be >= 0" }
            require(item.iuran_perwarga <= Int.MAX_VALUE / 2) { "iuran_perwarga too large, may cause overflow" }
            require(item.pengeluaran_iuran_warga <= Int.MAX_VALUE / 2) { "pengeluaran_iuran_warga too large, may cause overflow" }
            require(item.total_iuran_individu <= Int.MAX_VALUE / 3) { "total_iuran_individu too large, may cause overflow" }
        }
    }
    
    /**
     * Calculates total iuran bulanan from a list of DataItems
     */
    private fun calculateTotalIuranBulanan(items: List<DataItem>): Int {
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
    private fun calculateTotalPengeluaran(items: List<DataItem>): Int {
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
    private fun calculateTotalIuranIndividu(items: List<DataItem>): Int {
        var total = 0
        for (item in items) {
            var value = item.total_iuran_individu
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
