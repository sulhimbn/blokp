package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for financial calculation logic behind LaporanActivity.
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
        pemanfaatan_iuran = "Maintenance",
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

        assertEquals(300, totalIuranBulanan)
        assertEquals(15, totalPengeluaran)
        assertEquals(90, totalIuranIndividu)
        assertEquals(75, rekapIuran)
    }

    @Test
    fun testRekapIuranIsClampedAtZero() {
        val testItems = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaran = 500)
        )

        assertEquals(30, FinancialCalculator.calculateTotalIuranIndividu(testItems))
        assertEquals(500, FinancialCalculator.calculateTotalPengeluaran(testItems))
        assertEquals(0, FinancialCalculator.calculateRekapIuran(testItems))
    }
}