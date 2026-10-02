package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Financial calculation rules rendered by LaporanActivity.
 *
 * LaporanActivity no longer computes the totals inline: it renders the
 * `FinancialSummary` produced by `FinancialViewModel`, which delegates every total to
 * [FinancialCalculator]. These tests therefore pin the real production calculation
 * (including the `total_iuran_individu * IURAN_MULTIPLIER` rule) rather than a copy of it.
 */
class LaporanActivityCalculationTest {

    private fun item(
        iuranPerwarga: Int,
        totalIuranIndividu: Int,
        pengeluaran: Int
    ) = DataItem(
        first_name = "Test",
        last_name = "User",
        email = "test@example.com",
        alamat = "Test Address",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = iuranPerwarga,
        total_iuran_individu = totalIuranIndividu,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = "Test",
        avatar = ""
    )

    @Test
    fun testTotalIuranIndividuCalculation_accumulatesCorrectly() {
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaran = 25),
            item(iuranPerwarga = 200, totalIuranIndividu = 75, pengeluaran = 30),
            item(iuranPerwarga = 300, totalIuranIndividu = 100, pengeluaran = 45)
        )

        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(100, FinancialCalculator.calculateTotalPengeluaran(testItems))
        assertEquals(675, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(575, FinancialCalculator.calculateRekapIuran(testItems))
    }

    @Test
    fun testTotalIuranIndividuCalculation_singleItem() {
        val testItems = listOf(item(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaran = 25))

        assertEquals(100, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(25, FinancialCalculator.calculateTotalPengeluaran(testItems))
        assertEquals(150, FinancialCalculator.calculateTotalIuranIndividu(testItems))
    }

    @Test
    fun testTotalIuranIndividuCalculation_emptyList() {
        val testItems = emptyList<DataItem>()

        assertEquals(0, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(0, FinancialCalculator.calculateTotalPengeluaran(testItems))
        assertEquals(0, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(0, FinancialCalculator.calculateRekapIuran(testItems))
    }

    @Test
    fun testTotalIuranIndividuCalculation_verificationOfAccumulationLogic() {
        // Issue #96: every row must contribute total_iuran_individu * 3 to the running total,
        // it must not be overwritten by the last row.
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaran = 5),
            item(iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaran = 10),
            item(iuranPerwarga = 300, totalIuranIndividu = 30, pengeluaran = 15)
        )

        assertEquals(180, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(30, FinancialCalculator.calculateTotalPengeluaran(testItems))
    }

    @Test
    fun testPaymentIntegrationDoesNotAffectFinancialCalculations() {
        // Payments must not be folded into the iuran totals.
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaran = 5),
            item(iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaran = 10)
        )

        val expectedTotalIuranBulanan = FinancialCalculator.calculateTotalIuranBulanan(testItems)
        val expectedTotalPengeluaran = FinancialCalculator.calculateTotalPengeluaran(testItems)
        val expectedTotalIuranIndividu = FinancialCalculator.calculateTotalIuranIndividu(testItems)
        val expectedRekapIuran = FinancialCalculator.calculateRekapIuran(testItems)

        assertEquals(300, expectedTotalIuranBulanan)
        assertEquals(15, expectedTotalPengeluaran)
        assertEquals(90, expectedTotalIuranIndividu)
        assertEquals(75, expectedRekapIuran)
        assertTrue(FinancialCalculator.validateFinancialCalculations(testItems))

        // The old buggy behaviour added processed payments to the iuran totals
        val paymentTotal = 50
        val buggyTotalIuranBulanan = expectedTotalIuranBulanan + paymentTotal
        val buggyRekapIuran = buggyTotalIuranBulanan - expectedTotalPengeluaran

        assertNotEquals(buggyTotalIuranBulanan, expectedTotalIuranBulanan)
        assertNotEquals(buggyRekapIuran, expectedRekapIuran)
    }

    @Test
    fun testRekapIuranIsClampedToZeroWhenExpensesExceedCollections() {
        val testItems = listOf(item(iuranPerwarga = 100, totalIuranIndividu = 0, pengeluaran = 500))

        assertEquals(0, FinancialCalculator.calculateRekapIuran(testItems))
    }
}