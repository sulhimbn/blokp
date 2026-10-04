package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the financial calculation logic behind LaporanActivity.
 *
 * LaporanActivity does not compute totals itself; it renders the FinancialSummary
 * produced by FinancialCalculator. These tests therefore assert against
 * FinancialCalculator so that they fail if the production arithmetic regresses
 * (previously they re-implemented the same arithmetic inline and could not fail).
 */
class LaporanActivityCalculationTest {

    private fun item(
        perwarga: Int,
        individu: Int,
        pengeluaran: Int,
        rekap: Int = 0,
        bulanan: Int = 0
    ) = DataItem(
        first_name = "Warga",
        last_name = "Test",
        email = "warga@example.com",
        alamat = "Jl. Test 1",
        iuran_perwarga = perwarga,
        total_iuran_rekap = rekap,
        jumlah_iuran_bulanan = bulanan,
        total_iuran_individu = individu,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = "Test",
        avatar = ""
    )

    @Test
    fun totals_accumulateAcrossAllItems() {
        val items = listOf(
            item(perwarga = 100, individu = 50, pengeluaran = 25),
            item(perwarga = 200, individu = 75, pengeluaran = 30),
            item(perwarga = 300, individu = 100, pengeluaran = 45)
        )

        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(100, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(675, FinancialCalculator.calculateTotalIuranIndividu(items))
        assertEquals(575, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun totals_handleSingleItem() {
        val items = listOf(item(perwarga = 100, individu = 50, pengeluaran = 25))

        assertEquals(100, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(25, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(150, FinancialCalculator.calculateTotalIuranIndividu(items))
    }

    @Test
    fun totals_areZeroForEmptyList() {
        val items = emptyList<DataItem>()

        assertEquals(0, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(0, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(0, FinancialCalculator.calculateTotalIuranIndividu(items))
        assertEquals(0, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun totalIuranIndividu_multipliesEveryItemAndAccumulates() {
        // Regression: an earlier bug assigned instead of accumulated, so only the
        // last item's contribution survived (30*3 = 90 instead of 180).
        val items = listOf(
            item(perwarga = 100, individu = 10, pengeluaran = 5),
            item(perwarga = 200, individu = 20, pengeluaran = 10),
            item(perwarga = 300, individu = 30, pengeluaran = 15)
        )

        val total = FinancialCalculator.calculateTotalIuranIndividu(items)

        assertEquals(180, total)
        assertNotEquals(90, total)
        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(30, FinancialCalculator.calculateTotalPengeluaran(items))
    }

    @Test
    fun totals_ignorePaymentAmountsCarriedOnDataItems() {
        // Regression: payment processing used to be folded into the iuran totals.
        // Payment money arrives as its own field and must not change iuran totals.
        val items = listOf(
            item(perwarga = 150, individu = 10, pengeluaran = 5, rekap = 200_000),
            item(perwarga = 200, individu = 20, pengeluaran = 10, bulanan = 150_000)
        )

        val totalIuranBulanan = FinancialCalculator.calculateTotalIuranBulanan(items)
        val totalPengeluaran = FinancialCalculator.calculateTotalPengeluaran(items)
        val rekapIuran = FinancialCalculator.calculateRekapIuran(items)

        assertEquals(350, totalIuranBulanan)
        assertEquals(15, totalPengeluaran)
        assertEquals(75, rekapIuran)
        assertTrue(FinancialCalculator.validateFinancialCalculations(items))
    }
}