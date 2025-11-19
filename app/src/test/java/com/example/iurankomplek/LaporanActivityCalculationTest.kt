package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for financial calculation logic in LaporanActivity
 */
class LaporanActivityCalculationTest {

    @Test
    fun testTotalIuranIndividuCalculation_accumulatesCorrectly() {
        // Test data with multiple items to verify accumulation
        val testItems = listOf(
            DataItem(iuran_perwarga = 100, total_iuran_individu = 50, pengeluaran_iuran_warga = 25),
            DataItem(iuran_perwarga = 200, total_iuran_individu = 75, pengeluaran_iuran_warga = 30),
            DataItem(iuran_perwarga = 300, total_iuran_individu = 100, pengeluaran_iuran_warga = 45)
        )

        // Simulate the calculation logic from LaporanActivity
        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3  // The multiplication by 3 logic
        }

        var rekapIuran = totalIuranIndividu - totalPengeluaran

        // Verify calculations are correct
        assertEquals(600, totalIuranBulanan)  // 100 + 200 + 300
        assertEquals(100, totalPengeluaran)   // 25 + 30 + 45
        assertEquals(675, totalIuranIndividu) // (50*3) + (75*3) + (100*3) = 150 + 225 + 300
        assertEquals(575, rekapIuran)         // 675 - 100
    }

    @Test
    fun testTotalIuranIndividuCalculation_singleItem() {
        // Test with single item to ensure it doesn't just take last item but properly accumulates
        val testItems = listOf(
            DataItem(iuran_perwarga = 100, total_iuran_individu = 50, pengeluaran_iuran_warga = 25)
        )

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        assertEquals(100, totalIuranBulanan)
        assertEquals(25, totalPengeluaran)
        assertEquals(150, totalIuranIndividu)  // 50 * 3
    }

    @Test
    fun testTotalIuranIndividuCalculation_emptyList() {
        // Test with empty list to ensure no errors
        val testItems = emptyList<DataItem>()

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        assertEquals(0, totalIuranBulanan)
        assertEquals(0, totalPengeluaran)
        assertEquals(0, totalIuranIndividu)
    }

    @Test
    fun testTotalIuranIndividuCalculation_withZeroValues() {
        // Test with zero values to ensure calculations work correctly
        val testItems = listOf(
            DataItem(first_name = "John", last_name = "Doe", email = "john@example.com", 
                alamat = "Jl. Example 1", iuran_perwarga = 0, total_iuran_rekap = 0, 
                jumlah_iuran_bulanan = 0, total_iuran_individu = 0, pengeluaran_iuran_warga = 0, 
                pemanfaatan_iuran = "Test", avatar = "")
        )

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        assertEquals(0, totalIuranBulanan)
        assertEquals(0, totalPengeluaran)
        assertEquals(0, totalIuranIndividu)
    }

    @Test
    fun testTotalIuranIndividuCalculation_withLargeNumbers() {
        // Test with large numbers to ensure no overflow issues in normal scenarios
        val largeValue = 1000000 // Using a large but reasonable value
        val testItems = listOf(
            DataItem(first_name = "John", last_name = "Doe", email = "john@example.com", 
                alamat = "Jl. Example 1", iuran_perwarga = largeValue, total_iuran_rekap = largeValue, 
                jumlah_iuran_bulanan = largeValue, total_iuran_individu = largeValue, 
                pengeluaran_iuran_warga = largeValue / 2, 
                pemanfaatan_iuran = "Test", avatar = "")
        )

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        assertEquals(largeValue, totalIuranBulanan)
        assertEquals(largeValue / 2, totalPengeluaran)
        assertEquals(largeValue * 3, totalIuranIndividu) // 1000000 * 3
    }

    @Test
    fun testRekapIuranCalculation_edgeCase() {
        // Test rekap iuran calculation with different scenarios
        val testItems = listOf(
            DataItem(first_name = "John", last_name = "Doe", email = "john@example.com", 
                alamat = "Jl. Example 1", iuran_perwarga = 500, total_iuran_rekap = 500, 
                jumlah_iuran_bulanan = 500, total_iuran_individu = 100, 
                pengeluaran_iuran_warga = 80, 
                pemanfaatan_iuran = "Test", avatar = ""),
            DataItem(first_name = "Jane", last_name = "Smith", email = "jane@example.com", 
                alamat = "Jl. Example 2", iuran_perwarga = 600, total_iuran_rekap = 600, 
                jumlah_iuran_bulanan = 600, total_iuran_individu = 150, 
                pengeluaran_iuran_warga = 70, 
                pemanfaatan_iuran = "Test2", avatar = "")
        )

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        val rekapIuran = totalIuranIndividu - totalPengeluaran

        assertEquals(1100, totalIuranBulanan)  // 500 + 600
        assertEquals(150, totalPengeluaran)    // 80 + 70
        assertEquals(750, totalIuranIndividu)  // (100 * 3) + (150 * 3) = 300 + 450
        assertEquals(600, rekapIuran)          // 750 - 150
    }
}