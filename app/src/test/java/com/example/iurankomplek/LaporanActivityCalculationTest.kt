package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Regression tests for the financial aggregation used by LaporanActivity.
 *
 * The Activity itself no longer computes these figures inline: it delegates to
 * FinancialViewModel, which in turn uses FinancialCalculator. These tests therefore
 * assert against FinancialCalculator so they can actually fail when the aggregation
 * breaks (the previous version of this file re-implemented the arithmetic inside the
 * test body, so it passed no matter what production code did).
 */
class LaporanActivityCalculationTest {

    private fun item(
        firstName: String = "John",
        iuranPerwarga: Int = 0,
        totalIuranIndividu: Int = 0,
        pengeluaranIuranWarga: Int = 0
    ) = DataItem(
        first_name = firstName,
        last_name = "Doe",
        email = "$firstName@example.com",
        alamat = "Jl. Test 1",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = 0,
        total_iuran_individu = totalIuranIndividu,
        pengeluaran_iuran_warga = pengeluaranIuranWarga,
        pemanfaatan_iuran = "Test",
        avatar = ""
    )

    @Test
    fun testTotalIuranIndividuCalculation_accumulatesCorrectly() {
        val testItems = listOf(
            item(firstName = "a", iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaranIuranWarga = 25),
            item(firstName = "b", iuranPerwarga = 200, totalIuranIndividu = 75, pengeluaranIuranWarga = 30),
            item(firstName = "c", iuranPerwarga = 300, totalIuranIndividu = 100, pengeluaranIuranWarga = 45)
        )

        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(100, FinancialCalculator.calculateTotalPengeluaran(testItems))
        assertEquals(675, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(575, FinancialCalculator.calculateRekapIuran(testItems))
    }

    @Test
    fun testTotalIuranIndividuCalculation_singleItem() {
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaranIuranWarga = 25)
        )

        assertEquals(100, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(25, FinancialCalculator.calculateTotalPengeluaran(testItems))
        assertEquals(150, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(125, FinancialCalculator.calculateRekapIuran(testItems))
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
        // Verifies each item's total_iuran_individu is multiplied by 3 and ACCUMULATED,
        // not overwritten by the last item (issue #96).
        val testItems = listOf(
            item(firstName = "a", iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaranIuranWarga = 5),
            item(firstName = "b", iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaranIuranWarga = 10),
            item(firstName = "c", iuranPerwarga = 300, totalIuranIndividu = 30, pengeluaranIuranWarga = 15)
        )

        val totalIuranIndividu = FinancialCalculator.calculateTotalIuranIndividu(testItems)

        // If the bug existed (only the last item used) this would be 30 * 3 = 90.
        assertEquals(180, totalIuranIndividu)
        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(30, FinancialCalculator.calculateTotalPengeluaran(testItems))
    }

    @Test
    fun testPaymentIntegrationDoesNotAffectFinancialCalculations() {
        // Regression: payment totals must not be folded into the iuran aggregates,
        // which used to inflate every reported figure.
        val testItems = listOf(
            item(firstName = "a", iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaranIuranWarga = 5),
            item(firstName = "b", iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaranIuranWarga = 10)
        )

        val totalIuranBulanan = FinancialCalculator.calculateTotalIuranBulanan(testItems)
        val totalPengeluaran = FinancialCalculator.calculateTotalPengeluaran(testItems)
        val totalIuranIndividu = FinancialCalculator.calculateTotalIuranIndividu(testItems)
        val rekapIuran = FinancialCalculator.calculateRekapIuran(testItems)

        assertEquals(300, totalIuranBulanan)
        assertEquals(15, totalPengeluaran)
        assertEquals(90, totalIuranIndividu)
        assertEquals(75, rekapIuran)

        val paymentTotal = 50
        val buggyTotalIuranBulanan = totalIuranBulanan + paymentTotal
        val buggyRekapIuran = buggyTotalIuranBulanan - totalPengeluaran

        assertNotEquals(buggyTotalIuranBulanan, totalIuranBulanan)
        assertNotEquals(buggyRekapIuran, rekapIuran)
    }

    @Test
    fun testRekapIuran_isClampedAtZeroWhenSpendingExceedsCollections() {
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 0, pengeluaranIuranWarga = 500)
        )

        assertEquals(0, FinancialCalculator.calculateRekapIuran(testItems))
    }
}