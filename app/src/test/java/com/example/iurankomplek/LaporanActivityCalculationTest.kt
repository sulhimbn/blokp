package com.example.iurankomplek

import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers the financial aggregation that feeds the LaporanActivity summary tiles.
 *
 * The previous version of this file re-implemented the accumulation loop inside the test
 * body, so it asserted against its own copy of the logic and could never catch a
 * regression in [FinancialCalculator]. These tests call the production calculator.
 */
class LaporanActivityCalculationTest {

    @Test
    fun totalsAccumulateAcrossEveryItem() {
        val items = listOf(
            TestFixtures.dataItem(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaranIuranWarga = 25),
            TestFixtures.dataItem(iuranPerwarga = 200, totalIuranIndividu = 75, pengeluaranIuranWarga = 30),
            TestFixtures.dataItem(iuranPerwarga = 300, totalIuranIndividu = 100, pengeluaranIuranWarga = 45)
        )

        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(items)) // 100 + 200 + 300
        assertEquals(100, FinancialCalculator.calculateTotalPengeluaran(items))  // 25 + 30 + 45
        // Each item is scaled by Constants.Financial.IURAN_MULTIPLIER before summing.
        assertEquals(675, FinancialCalculator.calculateTotalIuranIndividu(items)) // 150 + 225 + 300
        assertEquals(575, FinancialCalculator.calculateRekapIuran(items))         // 675 - 100
    }

    @Test
    fun singleItemProducesSameResultAsFirstElementOfALargerList() {
        val items = listOf(
            TestFixtures.dataItem(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaranIuranWarga = 25)
        )

        assertEquals(100, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(25, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(150, FinancialCalculator.calculateTotalIuranIndividu(items)) // 50 * 3
    }

    @Test
    fun emptyListProducesZeroTotals() {
        val items = emptyList<com.example.iurankomplek.model.DataItem>()

        assertEquals(0, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(0, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(0, FinancialCalculator.calculateTotalIuranIndividu(items))
        assertEquals(0, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun accumulationIsNotOverwrittenByTheLastItem() {
        // Regression guard for issue #96: taking only the final item would yield 90, not 180.
        val items = listOf(
            TestFixtures.dataItem(iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaranIuranWarga = 5),
            TestFixtures.dataItem(iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaranIuranWarga = 10),
            TestFixtures.dataItem(iuranPerwarga = 300, totalIuranIndividu = 30, pengeluaranIuranWarga = 15)
        )

        assertEquals(180, FinancialCalculator.calculateTotalIuranIndividu(items)) // 30 + 60 + 90
        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(30, FinancialCalculator.calculateTotalPengeluaran(items))
    }

    @Test
    fun rekapIsClampedToZeroWhenSpendingExceedsCollections() {
        val items = listOf(
            TestFixtures.dataItem(totalIuranIndividu = 10, pengeluaranIuranWarga = 100)
        )

        // 30 collected - 100 spent = -70; the calculator must not surface a negative balance.
        assertEquals(0, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun invalidInputIsRejectedRatherThanSilentlyMisreported() {
        val negative = listOf(TestFixtures.dataItem(iuranPerwarga = -1))

        assertEquals(false, FinancialCalculator.validateFinancialCalculations(negative))
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            FinancialCalculator.calculateTotalIuranBulanan(negative)
        }
    }
}