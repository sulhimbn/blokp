package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the financial aggregation shown on the laporan screen.
 * The assertions run against [FinancialCalculator], which is the production code
 * path behind LaporanActivity's summary row (via FinancialViewModel), rather
 * than against a copy of the formula inside the test.
 */
class LaporanActivityCalculationTest {

    private fun item(
        iuranPerwarga: Int = 0,
        totalIuranIndividu: Int = 0,
        pengeluaran: Int = 0
    ) = DataItem(
        first_name = "Test",
        last_name = "User",
        email = "test@example.com",
        alamat = "Test Address",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = 0,
        total_iuran_individu = totalIuranIndividu,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = "Test",
        avatar = null
    )

    @Test
    fun `totals accumulate across every item`() {
        val items = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaran = 25),
            item(iuranPerwarga = 200, totalIuranIndividu = 75, pengeluaran = 30),
            item(iuranPerwarga = 300, totalIuranIndividu = 100, pengeluaran = 45)
        )

        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(100, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(675, FinancialCalculator.calculateTotalIuranIndividu(items))
        assertEquals(575, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun `single item produces the multiplied individual total`() {
        val items = listOf(item(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaran = 25))

        assertEquals(100, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(25, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(50 * Constants.Financial.IURAN_MULTIPLIER, FinancialCalculator.calculateTotalIuranIndividu(items))
    }

    @Test
    fun `empty list yields zero totals`() {
        val items = emptyList<DataItem>()

        assertEquals(0, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(0, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(0, FinancialCalculator.calculateTotalIuranIndividu(items))
        assertEquals(0, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun `individual total accumulates instead of taking the last item`() {
        val items = listOf(
            item(totalIuranIndividu = 10),
            item(totalIuranIndividu = 20),
            item(totalIuranIndividu = 30)
        )

        val total = FinancialCalculator.calculateTotalIuranIndividu(items)

        assertEquals(180, total)
    }

    @Test
    fun `rekap is floored at zero when expenses exceed collections`() {
        val items = listOf(item(totalIuranIndividu = 10, pengeluaran = 1_000))

        assertEquals(0, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun `negative data is rejected instead of silently reducing totals`() {
        val items = listOf(item(iuranPerwarga = -100))

        assertTrue(!FinancialCalculator.validateDataItems(items))
        assertTrue(!FinancialCalculator.validateFinancialCalculations(items))
        runCatching { FinancialCalculator.calculateTotalIuranBulanan(items) }
            .onSuccess { throw AssertionError("negative iuran must be rejected") }
            .onFailure { assertTrue(it is IllegalArgumentException) }
    }

    @Test
    fun `overflowing individual totals are rejected`() {
        val items = listOf(item(totalIuranIndividu = Int.MAX_VALUE))

        assertTrue(!FinancialCalculator.validateDataItems(items))
        runCatching { FinancialCalculator.calculateTotalIuranIndividu(items) }
            .onSuccess { throw AssertionError("overflowing individual total must be rejected") }
            .onFailure { assertTrue(it is ArithmeticException || it is IllegalArgumentException) }
    }
}