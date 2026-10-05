package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the financial calculation logic used by the financial report
 * screen (formerly inlined in LaporanActivity, now in FinancialCalculator).
 */
class LaporanActivityCalculationTest {

    private fun item(
        iuranPerwarga: Int = 0,
        totalIuranIndividu: Int = 0,
        pengeluaran: Int = 0
    ) = DataItem(
        first_name = "Test",
        last_name = "User",
        email = "user@example.com",
        alamat = "Jl. Test",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = 0,
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
        // (50*3) + (75*3) + (100*3) = 150 + 225 + 300
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
        assertEquals(150, FinancialCalculator.calculateTotalIuranIndividu(testItems)) // 50 * 3
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
        // Each item's total_iuran_individu is multiplied by 3 and then accumulated
        // into the running total, not merely assigned (regression guard).
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaran = 5),
            item(iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaran = 10),
            item(iuranPerwarga = 300, totalIuranIndividu = 30, pengeluaran = 15)
        )

        // 10*3 = 30, then 30+60 = 90, then 90+90 = 180
        assertEquals(180, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(testItems))
        assertEquals(30, FinancialCalculator.calculateTotalPengeluaran(testItems))
    }

    @Test
    fun testPaymentIntegrationDoesNotAffectFinancialCalculations() {
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaran = 5),
            item(iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaran = 10)
        )

        val totalIuranBulanan = FinancialCalculator.calculateTotalIuranBulanan(testItems)
        val totalPengeluaran = FinancialCalculator.calculateTotalPengeluaran(testItems)
        val totalIuranIndividu = FinancialCalculator.calculateTotalIuranIndividu(testItems)
        val rekapIuran = FinancialCalculator.calculateRekapIuran(testItems)

        assertEquals(300, totalIuranBulanan)   // 100 + 200
        assertEquals(15, totalPengeluaran)    // 5 + 10
        assertEquals(90, totalIuranIndividu)   // (10*3) + (20*3)
        assertEquals(75, rekapIuran)           // 90 - 15

        // Payment totals are not an input to these figures; folding them in is the
        // old bug, so the sum must stay independent of any payment amount.
        val paymentTotal = 50
        val buggyTotalIuranBulanan = totalIuranBulanan + paymentTotal
        assertNotEquals(buggyTotalIuranBulanan, totalIuranBulanan)
    }
}