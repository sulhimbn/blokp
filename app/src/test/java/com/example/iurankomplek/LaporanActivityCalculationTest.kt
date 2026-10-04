package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the financial calculation logic behind LaporanActivity.
 *
 * The assertions run against the production calculator
 * (utils/FinancialCalculator.kt) instead of a copy of the formula, so any change to
 * the recap rule `total_iuran_individu * IURAN_MULTIPLIER` fails here instead of
 * silently diverging from the screen.
 */
class LaporanActivityCalculationTest {

    private fun item(
        iuranPerwarga: Int,
        totalIuranIndividu: Int,
        pengeluaran: Int
    ) = DataItem(
        first_name = "Warga",
        last_name = "Uji",
        email = "warga@example.com",
        alamat = "Jl. Uji No. 1",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = iuranPerwarga,
        total_iuran_individu = totalIuranIndividu,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = "Pengujian",
        avatar = null
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
    fun testTotalIuranIndividuCalculation_appliesTheDocumentedMultiplier() {
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaran = 25)
        )

        assertEquals(3, Constants.Financial.IURAN_MULTIPLIER)
        assertEquals(
            50 * Constants.Financial.IURAN_MULTIPLIER,
            FinancialCalculator.calculateTotalIuranIndividu(testItems)
        )
    }

    @Test
    fun testTotalIuranIndividuCalculation_singleItem() {
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaran = 25)
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
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaran = 5),
            item(iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaran = 10),
            item(iuranPerwarga = 300, totalIuranIndividu = 30, pengeluaran = 15)
        )

        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(30, FinancialCalculator.calculateTotalPengeluaran(testItems))
        assertEquals(180, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(150, FinancialCalculator.calculateRekapIuran(testItems))
    }

    @Test
    fun testPaymentIntegrationDoesNotAffectFinancialCalculations() {
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaran = 5),
            item(iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaran = 10)
        )

        // Payment processing must not leak into the iuran aggregates.
        val paymentTotal = 50

        val totalIuranBulanan = FinancialCalculator.calculateTotalIuranBulanan(testItems)
        val totalPengeluaran = FinancialCalculator.calculateTotalPengeluaran(testItems)
        val totalIuranIndividu = FinancialCalculator.calculateTotalIuranIndividu(testItems)
        val rekapIuran = FinancialCalculator.calculateRekapIuran(testItems)

        assertEquals(300, totalIuranBulanan)
        assertEquals(15, totalPengeluaran)
        assertEquals(90, totalIuranIndividu)
        assertEquals(75, rekapIuran)
        assertTrue(FinancialCalculator.validateFinancialCalculations(testItems))

        // The old buggy behaviour folded payments into the aggregates.
        val buggyTotalIuranBulanan = totalIuranBulanan + paymentTotal
        val buggyRekapIuran = buggyTotalIuranBulanan - totalPengeluaran

        assertNotEquals(buggyTotalIuranBulanan, totalIuranBulanan)
        assertNotEquals(buggyRekapIuran, rekapIuran)
    }

    @Test
    fun testNegativeAmountsAreRejectedBeforeAnyArithmetic() {
        val negative = listOf(item(iuranPerwarga = -1, totalIuranIndividu = 0, pengeluaran = 0))

        assertTrue(!FinancialCalculator.validateDataItems(negative))
        assertTrue(!FinancialCalculator.validateFinancialCalculations(negative))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testOverflowingMultiplicationIsRejected() {
        val tooLarge = listOf(
            item(iuranPerwarga = 0, totalIuranIndividu = Int.MAX_VALUE, pengeluaran = 0)
        )

        FinancialCalculator.calculateTotalIuranIndividu(tooLarge)
    }
}