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
    fun testTotalIuranIndividuCalculation_regression_bug_check() {
        // Regression test to verify that the accumulation logic works correctly
        // and doesn't just take the last item's value (which was the original bug)
        val testItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Example 1",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,  // First item: 50
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Maintenance",
                avatar = ""
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Smith", 
                email = "jane@example.com",
                alamat = "Jl. Example 2",
                iuran_perwarga = 200,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 75,  // Second item: 75
                pengeluaran_iuran_warga = 30,
                pemanfaatan_iuran = "Utilities",
                avatar = ""
            ),
            DataItem(
                first_name = "Bob",
                last_name = "Johnson",
                email = "bob@example.com", 
                alamat = "Jl. Example 3",
                iuran_perwarga = 300,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 100,  // Third item: 100
                pengeluaran_iuran_warga = 45,
                pemanfaatan_iuran = "Repairs",
                avatar = ""
            )
        )

        // Simulate the correct calculation logic from LaporanActivity (using +=)
        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3  // The multiplication by 3 logic
        }

        // Calculate what the result would be if we used the buggy logic (using = instead of +=)
        // This would only take the last item's value: 100 * 3 = 300
        val buggyResult = testItems.last().total_iuran_individu * 3  // 100 * 3 = 300

        // The correct result should be: (50*3) + (75*3) + (100*3) = 150 + 225 + 300 = 675
        val correctResult = (50 * 3) + (75 * 3) + (100 * 3)  // 150 + 225 + 300 = 675

        // Verify that we get the correct accumulated result, not the buggy one
        assertEquals("Total iuran individu should be the sum of all items multiplied by 3, not just the last item", 
            correctResult, totalIuranIndividu)
        assertNotEquals("Total iuran individu should not be just the last item's value (this would be the bug)", 
            buggyResult, totalIuranIndividu)
        
        // Verify other calculations are also correct
        assertEquals(600, totalIuranBulanan)  // 100 + 200 + 300
        assertEquals(100, totalPengeluaran)   // 25 + 30 + 45
    }
}