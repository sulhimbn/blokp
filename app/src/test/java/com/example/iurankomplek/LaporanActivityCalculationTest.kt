package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the financial aggregation rules rendered by LaporanActivity
 * (including the documented `total_iuran_individu * IURAN_MULTIPLIER` rekap formula)
 * against the production [FinancialCalculator] implementation.
 */
class LaporanActivityCalculationTest {

    private fun item(
        iuranPerwarga: Int = 0,
        totalIndividu: Int = 0,
        pengeluaran: Int = 0,
        jumlahBulanan: Int = 0,
        totalRekap: Int = 0
    ) = DataItem(
        first_name = "Warga",
        last_name = "Uji",
        email = "warga@example.com",
        alamat = "Jl. Test No. 1",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = totalRekap,
        jumlah_iuran_bulanan = jumlahBulanan,
        total_iuran_individu = totalIndividu,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )

    @Test
    fun aggregatesTotalsAcrossMultipleItems() {
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIndividu = 50, pengeluaran = 25),
            item(iuranPerwarga = 200, totalIndividu = 75, pengeluaran = 30),
            item(iuranPerwarga = 300, totalIndividu = 100, pengeluaran = 45)
        )

        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(100, FinancialCalculator.calculateTotalPengeluaran(testItems))
        assertEquals(675, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(575, FinancialCalculator.calculateRekapIuran(testItems))
    }

    @Test
    fun rekapAppliesTheIndividualIuranMultiplier() {
        val testItems = listOf(item(totalIndividu = 150, pengeluaran = 0))

        assertEquals(150 * 3, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(150 * 3, FinancialCalculator.calculateRekapIuran(testItems))
    }

    @Test
    fun singleItemIsAggregatedRatherThanOverwritten() {
        val testItems = listOf(item(iuranPerwarga = 100, totalIndividu = 50, pengeluaran = 25))

        assertEquals(100, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(25, FinancialCalculator.calculateTotalPengeluaran(testItems))
        assertEquals(150, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(125, FinancialCalculator.calculateRekapIuran(testItems))
    }

    @Test
    fun emptyListYieldsZeroTotals() {
        val empty = emptyList<DataItem>()

        assertEquals(0, FinancialCalculator.calculateTotalIuranBulanan(empty))
        assertEquals(0, FinancialCalculator.calculateTotalPengeluaran(empty))
        assertEquals(0, FinancialCalculator.calculateTotalIuranIndividu(empty))
        assertEquals(0, FinancialCalculator.calculateRekapIuran(empty))
    }

    @Test
    fun rekapNeverReturnsANegativeBalance() {
        val testItems = listOf(item(totalIndividu = 10, pengeluaran = 500))

        assertEquals(0, FinancialCalculator.calculateRekapIuran(testItems))
    }

    @Test
    fun allZeroItemsProduceAValidZeroedSummary() {
        val testItems = listOf(item(), item())

        assertTrue(FinancialCalculator.validateDataItems(testItems))
        assertTrue(FinancialCalculator.validateFinancialCalculations(testItems))
        assertEquals(0, FinancialCalculator.calculateRekapIuran(testItems))
    }

    @Test
    fun negativeAmountsAreRejectedAsInvalid() {
        val testItems = listOf(item(iuranPerwarga = -1))

        assertFalse(FinancialCalculator.validateDataItems(testItems))
        assertFalse(FinancialCalculator.validateFinancialCalculations(testItems))
    }
}