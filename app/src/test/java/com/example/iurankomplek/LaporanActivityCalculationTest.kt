package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class LaporanActivityCalculationTest {

    private fun item(
        iuranPerwarga: Int,
        totalIndividu: Int,
        pengeluaran: Int
    ) = DataItem(
        iuran_perwarga = iuranPerwarga,
        total_iuran_individu = totalIndividu,
        pengeluaran_iuran_warga = pengeluaran
    )

    @Test
    fun `total iuran bulanan accumulates every resident's dues`() {
        val items = listOf(
            item(iuranPerwarga = 100, totalIndividu = 50, pengeluaran = 25),
            item(iuranPerwarga = 200, totalIndividu = 75, pengeluaran = 30),
            item(iuranPerwarga = 300, totalIndividu = 100, pengeluaran = 45)
        )

        assertEquals(600, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(100, FinancialCalculator.calculateTotalPengeluaran(items))
    }

    @Test
    fun `total iuran individu multiplies each resident's total by three`() {
        val items = listOf(
            item(iuranPerwarga = 100, totalIndividu = 50, pengeluaran = 25),
            item(iuranPerwarga = 200, totalIndividu = 75, pengeluaran = 30),
            item(iuranPerwarga = 300, totalIndividu = 100, pengeluaran = 45)
        )

        assertEquals(675, FinancialCalculator.calculateTotalIuranIndividu(items))
        assertEquals(575, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun `single resident produces the same result as a one element list`() {
        val items = listOf(item(iuranPerwarga = 100, totalIndividu = 50, pengeluaran = 25))

        assertEquals(100, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(25, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(50 * Constants.Financial.IURAN_MULTIPLIER, FinancialCalculator.calculateTotalIuranIndividu(items))
    }

    @Test
    fun `empty resident list yields zero totals`() {
        val items = emptyList<DataItem>()

        assertEquals(0, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(0, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(0, FinancialCalculator.calculateTotalIuranIndividu(items))
        assertEquals(0, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun `rekap never goes negative when spending exceeds collected dues`() {
        val items = listOf(item(iuranPerwarga = 100, totalIndividu = 10, pengeluaran = 500))

        assertEquals(0, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun `payment totals stay out of the iuran totals`() {
        val items = listOf(
            item(iuranPerwarga = 100, totalIndividu = 10, pengeluaran = 5),
            item(iuranPerwarga = 200, totalIndividu = 20, pengeluaran = 10)
        )

        assertEquals(300, FinancialCalculator.calculateTotalIuranBulanan(items))
        assertEquals(15, FinancialCalculator.calculateTotalPengeluaran(items))
        assertEquals(90, FinancialCalculator.calculateTotalIuranIndividu(items))
        assertEquals(75, FinancialCalculator.calculateRekapIuran(items))
    }

    @Test
    fun `negative dues are rejected as invalid financial data`() {
        val items = listOf(item(iuranPerwarga = -100, totalIndividu = 10, pengeluaran = 5))

        assertEquals(false, FinancialCalculator.validateDataItem(items.first()))
        assertEquals(false, FinancialCalculator.validateFinancialCalculations(items))
    }
}