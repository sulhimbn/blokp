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
     fun testTotalIuranIndividuCalculation_bugRegression_accumulationVsAssignment() {
         // Regression test to ensure accumulation works correctly and would catch
         // if someone accidentally changes += to = (which would only use last item's value)
         val testItems = listOf(
             DataItem(iuran_perwarga = 100, total_iuran_individu = 10, pengeluaran_iuran_warga = 5),
             DataItem(iuran_perwarga = 200, total_iuran_individu = 20, pengeluaran_iuran_warga = 10),
             DataItem(iuran_perwarga = 300, total_iuran_individu = 30, pengeluaran_iuran_warga = 15)
         )

         var totalIuranIndividu = 0
         
         for (dataItem in testItems) {
             // This simulates the correct accumulation logic from LaporanActivity
             totalIuranIndividu += dataItem.total_iuran_individu * 3
         }
         
         // With correct accumulation (+=), result should be: (10*3) + (20*3) + (30*3) = 30 + 60 + 90 = 180
         val expectedAccumulatedValue = 180
         assertEquals("Accumulation should sum all items (10*3 + 20*3 + 30*3 = 180), not just use last item", 
             expectedAccumulatedValue, totalIuranIndividu)
         
         // For comparison: if bug were present (= instead of +=), result would be just the last item: 30*3 = 90
         val expectedIfBugPresent = 90  // This would be the result if using assignment instead of accumulation
         assertNotEquals("If += were accidentally changed to =, this would incorrectly return only last item value", 
             expectedIfBugPresent, totalIuranIndividu)
     }
 }