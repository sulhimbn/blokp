package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.dto.LegacyDataItemDto

/**
 * Use case for calculating financial totals from LegacyDataItemDto list
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
     * Executes financial calculations for a list of LegacyDataItemDto
     *
     * @param items List of LegacyDataItemDto to calculate totals for
     * @return FinancialTotals with calculated values
     * @throws IllegalArgumentException if validation fails
     * @throws ArithmeticException if calculation would cause overflow/underflow
     */
    operator fun invoke(items: List<LegacyDataItemDto>): FinancialTotals {
        if (items.isEmpty()) {
            return FinancialTotals(0, 0, 0, 0)
        }

        validateDataItems(items)

        return calculateAllTotalsInSinglePass(items)
    }
    
    /**
     * Validates a list of LegacyDataItemDto for financial calculation
     */
    private fun validateDataItems(items: List<LegacyDataItemDto>) {
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
     * Calculates all financial totals in a single pass through the data
     * Optimized from 3 separate iterations to 1 iteration (~66% faster)
     *
     * @param items List of LegacyDataItemDto to calculate totals for
     * @return FinancialTotals with calculated values
     */
    private fun calculateAllTotalsInSinglePass(items: List<LegacyDataItemDto>): FinancialTotals {
        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (item in items) {
            val iuranPerwarga = item.iuran_perwarga

            if (iuranPerwarga > Int.MAX_VALUE - totalIuranBulanan) {
                throw ArithmeticException("Total iuran bulanan calculation would cause overflow")
            }
            totalIuranBulanan += iuranPerwarga

            val pengeluaranIuranWarga = item.pengeluaran_iuran_warga

            if (pengeluaranIuranWarga > Int.MAX_VALUE - totalPengeluaran) {
                throw ArithmeticException("Total pengeluaran calculation would cause overflow")
            }
            totalPengeluaran += pengeluaranIuranWarga

            var totalIuranIndividuValue = item.total_iuran_individu

            if (totalIuranIndividuValue > Int.MAX_VALUE / 3) {
                throw ArithmeticException("Individual iuran calculation would cause overflow")
            }
            totalIuranIndividuValue *= 3

            if (totalIuranIndividuValue > Int.MAX_VALUE - totalIuranIndividu) {
                throw ArithmeticException("Total iuran individu calculation would cause overflow")
            }
            totalIuranIndividu += totalIuranIndividuValue
        }

        val rekapIuran = calculateRekapIuran(totalIuranIndividu, totalPengeluaran)

        return FinancialTotals(
            totalIuranBulanan = totalIuranBulanan,
            totalPengeluaran = totalPengeluaran,
            totalIuranIndividu = totalIuranIndividu,
            rekapIuran = rekapIuran
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
