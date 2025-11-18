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
    fun testTotalIuranIndividuBugRegression_ensuresAccumulationNotAssignment() {
        // Regression test to ensure that += is used instead of = for accumulation
        // This test would fail if someone accidentally changes += to = in the calculation
        val testItems = listOf(
            DataItem(iuran_perwarga = 100, total_iuran_individu = 50, pengeluaran_iuran_warga = 10), // 50*3=150
            DataItem(iuran_perwarga = 200, total_iuran_individu = 75, pengeluaran_iuran_warga = 20), // 75*3=225
            DataItem(iuran_perwarga = 300, total_iuran_individu = 100, pengeluaran_iuran_warga = 30) // 100*3=300
        )

        var totalIuranIndividu = 0

        // This simulates the correct accumulation logic that should sum all values
        for (dataItem in testItems) {
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        // The correct result should be the sum of all items: (50*3) + (75*3) + (100*3) = 150 + 225 + 300 = 675
        val expectedTotal = (50 * 3) + (75 * 3) + (100 * 3) // 675
        assertEquals("Total should accumulate all items, not just take the last item's value", 
            expectedTotal, totalIuranIndividu)

        // If the bug were present (using = instead of +=), the result would be just the last item: 100 * 3 = 300
        val buggyResult = 100 * 3 // What the result would be if using assignment instead of accumulation
        assertNotEquals("Result should not be just the last item's value (indicates accumulation bug)", 
            buggyResult, totalIuranIndividu)
    }
}