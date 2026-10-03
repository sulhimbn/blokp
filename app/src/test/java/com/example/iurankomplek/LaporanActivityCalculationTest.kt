package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LaporanActivityCalculationTest {

    private fun item(
        iuranPerwarga: Int = 0,
        totalIuranIndividu: Int = 0,
        pengeluaran: Int = 0
    ) = DataItem(
        first_name = "Warga",
        last_name = "Uji",
        email = "warga@example.com",
        alamat = "Jl. Test No. 1",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = iuranPerwarga,
        total_iuran_individu = totalIuranIndividu,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = "Test",
        avatar = null
    )

    @Test
    fun totalIuranIndividuAccumulatesAcrossEveryItem() {
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
    fun totalIuranIndividuUsesMultiplierOfThree() {
        val items = listOf(item(totalIuranIndividu = 50))

        assertEquals(150, FinancialCalculator.calculateTotalIuranIndividu(items))
    }

    @Test
    fun accumulationIsNotOverwrittenByLaterItems() {
        val items = listOf(
            item(iuranPerwarga = 100, totalIuranIndividu = 10, pengeluaran = 5),
            item(iuranPerwarga = 200, totalIuranIndividu = 20, pengeluaran = 10),
            item(iuranPerwarga = 300, totalIuranIndividu = 30, pengeluaran = 15)
        )

        assertEquals(180, FinancialCalculator.calculateTotalIuranIndividu(items))
        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(30, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(150, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun emptyListYieldsZeroTotals() {
        val items = emptyList<DataItem>()

        assertEquals(0, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(0, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(0, FinancialCalculator.calculateTotalIuranIndividu(items))
        assertEquals(0, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun rekapIuranRejectsUnderflowWhenExpensesExceedCollections() {
        val items = listOf(
            item(totalIuranIndividu = 10, pengeluaran = 500)
        )

        assertThrows(ArithmeticException::class.java) {
            FinancialCalculator.calculateRekapIuran(items)
        }
    }

    @Test
    fun negativeAmountsAreRejectedAsInvalid() {
        val items = listOf(item(iuranPerwarga = -1))

        assertFalse(FinancialCalculator.validateDataItems(items))
        assertFalse(FinancialCalculator.validateFinancialCalculations(items))
    }

    @Test
    fun overflowInTotalIuranIndividuIsRejected() {
        val items = listOf(
            item(totalIuranIndividu = Int.MAX_VALUE),
            item(totalIuranIndividu = Int.MAX_VALUE)
        )

        assertFalse(FinancialCalculator.validateDataItem(items.first()))
        assertFalse(FinancialCalculator.validateFinancialCalculations(items))
    }
}