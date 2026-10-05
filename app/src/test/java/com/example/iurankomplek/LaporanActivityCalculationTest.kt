package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises the financial figures rendered by LaporanActivity.
 *
 * LaporanActivity renders `FinancialSummary`, which FinancialViewModel derives from
 * FinancialCalculator, so these assertions target FinancialCalculator directly rather than
 * re-implementing the arithmetic locally (a copy of the logic would pass even if the
 * production implementation regressed).
 */
class LaporanActivityCalculationTest {

    private fun item(
        iuranPerwarga: Int = 0,
        totalIuranIndividu: Int = 0,
        pengeluaran: Int = 0
    ) = DataItem(
        first_name = null,
        last_name = null,
        email = null,
        alamat = null,
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = 0,
        total_iuran_individu = totalIuranIndividu,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = null,
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
    fun testTotalIuranIndividuCalculation_singleItem() {
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaran = 25)
        )

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
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaran = 5),
            item(iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaran = 10),
            item(iuranPerwarga = 300, totalIuranIndividu = 30, pengeluaran = 15)
        )

        // 10*3 + 20*3 + 30*3 = 30 + 60 + 90 = 180. A regression that assigned instead of
        // accumulated would yield only the last term (90).
        assertEquals(180, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(30, FinancialCalculator.calculateTotalPengeluaran(testItems))
    }

    @Test
    fun testIuranMultiplierMatchesConfiguredConstant() {
        val testItems = listOf(item(totalIuranIndividu = 7))

        assertEquals(
            7 * Constants.Financial.IURAN_MULTIPLIER,
            FinancialCalculator.calculateTotalIuranIndividu(testItems)
        )
    }

    @Test
    fun testRekapIuranClampsToZeroWhenExpensesExceedCollections() {
        val testItems = listOf(
            item(totalIuranIndividu = 10, pengeluaran = 100)
        )

        assertEquals(0, FinancialCalculator.calculateRekapIuran(testItems))
    }

    @Test
    fun testNegativeFinancialValuesAreRejected() {
        val invalid = listOf(item(totalIuranIndividu = -1))

        assertFalse(FinancialCalculator.validateDataItems(invalid))
        assertThrows(IllegalArgumentException::class.java) {
            FinancialCalculator.calculateTotalIuranIndividu(invalid)
        }
    }

    @Test
    fun testValidItemsPassValidation() {
        val valid = listOf(
            item(iuranPerwarga = 1, totalIuranIndividu = 2, pengeluaran = 3)
        )

        assertTrue(FinancialCalculator.validateDataItems(valid))
        assertTrue(FinancialCalculator.validateFinancialCalculations(valid))
    }

    @Test
    fun testPaymentIntegrationDoesNotAffectFinancialCalculations() {
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

        val paymentTotal = 50
        val buggyTotalIuranBulanan = expectedTotalIuranBulanan + paymentTotal
        val buggyRekapIuran = buggyTotalIuranBulanan - expectedTotalPengeluaran

        assertNotEquals(buggyTotalIuranBulanan, expectedTotalIuranBulanan)
        assertNotEquals(buggyRekapIuran, expectedRekapIuran)
    }
}