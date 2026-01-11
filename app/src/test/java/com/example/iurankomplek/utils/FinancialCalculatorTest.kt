package com.example.iurankomplek.utils

import com.example.iurankomplek.model.DataItem
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive test suite for FinancialCalculator
 * 
 * Tests cover:
 * - Happy paths (normal calculations)
 * - Edge cases (empty, single item, large values)
 * - Error handling (negative values, overflow, invalid data)
 * - Boundary conditions (Int.MAX_VALUE, zero, large datasets)
 * - Data class operations (FinancialTotals)
 */
class FinancialCalculatorTest {
    
    private lateinit var validItem: DataItem
    private lateinit var items: List<DataItem>
    
    @Before
    fun setUp() {
        validItem = DataItem(
            id = 1,
            nama_warga = "Test User",
            iuran_perwarga = 100000,
            pengeluaran_iuran_warga = 50000,
            total_iuran_individu = 30000
        )
        
        items = listOf(
            DataItem(1, "User1", 100000, 50000, 30000),
            DataItem(2, "User2", 150000, 75000, 45000),
            DataItem(3, "User3", 200000, 100000, 60000)
        )
    }
    
    // ===== VALIDATION TESTS =====
    
    @Test
    fun `validateDataItem with valid item returns true`() {
        assertTrue(FinancialCalculator.validateDataItem(validItem))
    }
    
    @Test
    fun `validateDataItem with negative iuran_perwarga returns false`() {
        val invalidItem = validItem.copy(iuran_perwarga = -1)
        assertFalse(FinancialCalculator.validateDataItem(invalidItem))
    }
    
    @Test
    fun `validateDataItem with negative pengeluaran_iuran_warga returns false`() {
        val invalidItem = validItem.copy(pengeluaran_iuran_warga = -1)
        assertFalse(FinancialCalculator.validateDataItem(invalidItem))
    }
    
    @Test
    fun `validateDataItem with negative total_iuran_individu returns false`() {
        val invalidItem = validItem.copy(total_iuran_individu = -1)
        assertFalse(FinancialCalculator.validateDataItem(invalidItem))
    }
    
    @Test
    fun `validateDataItem with iuran_perwarga exceeding overflow threshold returns false`() {
        val overflowThreshold = Int.MAX_VALUE / 2
        val invalidItem = validItem.copy(iuran_perwarga = overflowThreshold + 1)
        assertFalse(FinancialCalculator.validateDataItem(invalidItem))
    }
    
    @Test
    fun `validateDataItem with pengeluaran_iuran_warga exceeding overflow threshold returns false`() {
        val overflowThreshold = Int.MAX_VALUE / 2
        val invalidItem = validItem.copy(pengeluaran_iuran_warga = overflowThreshold + 1)
        assertFalse(FinancialCalculator.validateDataItem(invalidItem))
    }
    
    @Test
    fun `validateDataItem with total_iuran_individu exceeding overflow threshold returns false`() {
        val overflowThreshold = Int.MAX_VALUE / Constants.Financial.IURAN_MULTIPLIER
        val invalidItem = validItem.copy(total_iuran_individu = overflowThreshold + 1)
        assertFalse(FinancialCalculator.validateDataItem(invalidItem))
    }
    
    @Test
    fun `validateDataItems with all valid items returns true`() {
        assertTrue(FinancialCalculator.validateDataItems(items))
    }
    
    @Test
    fun `validateDataItems with one invalid item returns false`() {
        val invalidItems = items + DataItem(4, "BadUser", -100, 50000, 30000)
        assertFalse(FinancialCalculator.validateDataItems(invalidItems))
    }
    
    @Test
    fun `validateDataItems with empty list returns true`() {
        assertTrue(FinancialCalculator.validateDataItems(emptyList()))
    }
    
    // ===== CALCULATE TOTAL IURAN BULANAN TESTS =====
    
    @Test
    fun `calculateTotalIuranBulanan with valid items returns correct total`() {
        val expected = 100000 + 150000 + 200000 // 450000
        val result = FinancialCalculator.calculateTotalIuranBulanan(items)
        assertEquals(expected, result)
    }
    
    @Test
    fun `calculateTotalIuranBulanan with empty list returns zero`() {
        val result = FinancialCalculator.calculateTotalIuranBulanan(emptyList())
        assertEquals(0, result)
    }
    
    @Test
    fun `calculateTotalIuranBulanan with single item returns item value`() {
        val singleItem = listOf(validItem)
        val result = FinancialCalculator.calculateTotalIuranBulanan(singleItem)
        assertEquals(100000, result)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `calculateTotalIuranBulanan with invalid items throws IllegalArgumentException`() {
        val invalidItems = listOf(DataItem(1, "Bad", -100, 50000, 30000))
        FinancialCalculator.calculateTotalIuranBulanan(invalidItems)
    }
    
    @Test(expected = ArithmeticException::class)
    fun `calculateTotalIuranBulanan with overflow throws ArithmeticException`() {
        val largeValue = Int.MAX_VALUE / 2
        val overflowItems = listOf(
            DataItem(1, "User1", largeValue, 0, 0),
            DataItem(2, "User2", largeValue, 0, 0)
        )
        FinancialCalculator.calculateTotalIuranBulanan(overflowItems)
    }
    
    // ===== CALCULATE TOTAL PENGELUARAN TESTS =====
    
    @Test
    fun `calculateTotalPengeluaran with valid items returns correct total`() {
        val expected = 50000 + 75000 + 100000 // 225000
        val result = FinancialCalculator.calculateTotalPengeluaran(items)
        assertEquals(expected, result)
    }
    
    @Test
    fun `calculateTotalPengeluaran with empty list returns zero`() {
        val result = FinancialCalculator.calculateTotalPengeluaran(emptyList())
        assertEquals(0, result)
    }
    
    @Test
    fun `calculateTotalPengeluaran with single item returns item value`() {
        val singleItem = listOf(validItem)
        val result = FinancialCalculator.calculateTotalPengeluaran(singleItem)
        assertEquals(50000, result)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `calculateTotalPengeluaran with invalid items throws IllegalArgumentException`() {
        val invalidItems = listOf(DataItem(1, "Bad", 100000, -50000, 30000))
        FinancialCalculator.calculateTotalPengeluaran(invalidItems)
    }
    
    @Test(expected = ArithmeticException::class)
    fun `calculateTotalPengeluaran with overflow throws ArithmeticException`() {
        val largeValue = Int.MAX_VALUE / 2
        val overflowItems = listOf(
            DataItem(1, "User1", 0, largeValue, 0),
            DataItem(2, "User2", 0, largeValue, 0)
        )
        FinancialCalculator.calculateTotalPengeluaran(overflowItems)
    }
    
    // ===== CALCULATE TOTAL IURAN INDIVIDU TESTS =====
    
    @Test
    fun `calculateTotalIuranIndividu with valid items returns correct total`() {
        // total_iuran_individu is multiplied by IURAN_MULTIPLIER (3)
        val expected = (30000 + 45000 + 60000) * Constants.Financial.IURAN_MULTIPLIER
        val result = FinancialCalculator.calculateTotalIuranIndividu(items)
        assertEquals(expected, result)
    }
    
    @Test
    fun `calculateTotalIuranIndividu with empty list returns zero`() {
        val result = FinancialCalculator.calculateTotalIuranIndividu(emptyList())
        assertEquals(0, result)
    }
    
    @Test
    fun `calculateTotalIuranIndividu with single item returns multiplied value`() {
        val singleItem = listOf(validItem)
        val expected = 30000 * Constants.Financial.IURAN_MULTIPLIER
        val result = FinancialCalculator.calculateTotalIuranIndividu(singleItem)
        assertEquals(expected, result)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `calculateTotalIuranIndividu with invalid items throws IllegalArgumentException`() {
        val invalidItems = listOf(DataItem(1, "Bad", 100000, 50000, -30000))
        FinancialCalculator.calculateTotalIuranIndividu(invalidItems)
    }
    
    @Test(expected = ArithmeticException::class)
    fun `calculateTotalIuranIndividu with overflow throws ArithmeticException`() {
        val largeValue = Int.MAX_VALUE / Constants.Financial.IURAN_MULTIPLIER
        val overflowItems = listOf(
            DataItem(1, "User1", 0, 0, largeValue),
            DataItem(2, "User2", 0, 0, largeValue)
        )
        FinancialCalculator.calculateTotalIuranIndividu(overflowItems)
    }
    
    // ===== CALCULATE REKAP IURAN TESTS =====
    
    @Test
    fun `calculateRekapIuran with valid items returns correct value`() {
        // Rekap = totalIuranIndividu - totalPengeluaran
        // totalIuranIndividu = (30000 + 45000 + 60000) * 3 = 405000
        // totalPengeluaran = 50000 + 75000 + 100000 = 225000
        // Rekap = 405000 - 225000 = 180000
        val result = FinancialCalculator.calculateRekapIuran(items)
        assertEquals(180000, result)
    }
    
    @Test
    fun `calculateRekapIuran with empty list returns zero`() {
        val result = FinancialCalculator.calculateRekapIuran(emptyList())
        assertEquals(0, result)
    }
    
    @Test
    fun `calculateRekapIuran when pengeluaran exceeds iuran returns zero`() {
        val itemsWithHighPengeluaran = listOf(
            DataItem(1, "User1", 0, 50000, 1000),
            DataItem(2, "User2", 0, 100000, 1000)
        )
        // totalIuranIndividu = (1000 + 1000) * 3 = 6000
        // totalPengeluaran = 50000 + 100000 = 150000
        // Rekap = 6000 - 150000 = -144000 -> max(0, -144000) = 0
        val result = FinancialCalculator.calculateRekapIuran(itemsWithHighPengeluaran)
        assertEquals(0, result)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `calculateRekapIuran with invalid items throws IllegalArgumentException`() {
        val invalidItems = listOf(DataItem(1, "Bad", -100, 50000, 30000))
        FinancialCalculator.calculateRekapIuran(invalidItems)
    }
    
    @Test(expected = ArithmeticException::class)
    fun `calculateRekapIuran with overflow throws ArithmeticException`() {
        val largeValue = Int.MAX_VALUE / Constants.Financial.IURAN_MULTIPLIER
        val overflowItems = listOf(
            DataItem(1, "User1", 0, 0, largeValue),
            DataItem(2, "User2", 0, 0, largeValue)
        )
        FinancialCalculator.calculateRekapIuran(overflowItems)
    }
    
    @Test
    fun `calculateRekapIuran with underflow protection returns zero`() {
        val itemsWithNegativeRekap = listOf(
            DataItem(1, "User1", 0, 1000000, 1000)
        )
        // totalIuranIndividu = 1000 * 3 = 3000
        // totalPengeluaran = 1000000
        // Rekap = 3000 - 1000000 = -997000 -> max(0, -997000) = 0
        val result = FinancialCalculator.calculateRekapIuran(itemsWithNegativeRekap)
        assertEquals(0, result)
    }
    
    // ===== VALIDATE FINANCIAL CALCULATIONS TESTS =====
    
    @Test
    fun `validateFinancialCalculations with valid items returns true`() {
        assertTrue(FinancialCalculator.validateFinancialCalculations(items))
    }
    
    @Test
    fun `validateFinancialCalculations with invalid items returns false`() {
        val invalidItems = listOf(DataItem(1, "Bad", -100, 50000, 30000))
        assertFalse(FinancialCalculator.validateFinancialCalculations(invalidItems))
    }
    
    @Test
    fun `validateFinancialCalculations with overflow conditions returns false`() {
        val largeValue = Int.MAX_VALUE / 2
        val overflowItems = listOf(
            DataItem(1, "User1", largeValue, 0, 0),
            DataItem(2, "User2", largeValue, 0, 0)
        )
        assertFalse(FinancialCalculator.validateFinancialCalculations(overflowItems))
    }
    
    @Test
    fun `validateFinancialCalculations with empty list returns true`() {
        assertTrue(FinancialCalculator.validateFinancialCalculations(emptyList()))
    }
    
    // ===== CALCULATE ALL TOTALS TESTS =====
    
    @Test
    fun `calculateAllTotals returns correct FinancialTotals`() {
        val result = FinancialCalculator.calculateAllTotals(items)
        
        assertEquals(450000, result.totalIuranBulanan) // 100k + 150k + 200k
        assertEquals(225000, result.totalPengeluaran) // 50k + 75k + 100k
        assertEquals(405000, result.totalIuranIndividu) // (30k + 45k + 60k) * 3
        assertEquals(180000, result.rekapIuran) // 405k - 225k
    }
    
    @Test
    fun `calculateAllTotals with empty list returns zeros`() {
        val result = FinancialCalculator.calculateAllTotals(emptyList())
        
        assertEquals(0, result.totalIuranBulanan)
        assertEquals(0, result.totalPengeluaran)
        assertEquals(0, result.totalIuranIndividu)
        assertEquals(0, result.rekapIuran)
    }
    
    @Test
    fun `calculateAllTotals with single item returns correct totals`() {
        val singleItem = listOf(validItem)
        val result = FinancialCalculator.calculateAllTotals(singleItem)
        
        assertEquals(100000, result.totalIuranBulanan)
        assertEquals(50000, result.totalPengeluaran)
        assertEquals(90000, result.totalIuranIndividu) // 30000 * 3
        assertEquals(40000, result.rekapIuran) // 90000 - 50000
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `calculateAllTotals with invalid items throws IllegalArgumentException`() {
        val invalidItems = listOf(DataItem(1, "Bad", -100, 50000, 30000))
        FinancialCalculator.calculateAllTotals(invalidItems)
    }
    
    @Test(expected = ArithmeticException::class)
    fun `calculateAllTotals with overflow throws ArithmeticException`() {
        val largeValue = Int.MAX_VALUE / 2
        val overflowItems = listOf(
            DataItem(1, "User1", largeValue, 0, 0),
            DataItem(2, "User2", largeValue, 0, 0)
        )
        FinancialCalculator.calculateAllTotals(overflowItems)
    }
    
    // ===== FINANCIAL TOTALS DATA CLASS TESTS =====
    
    @Test
    fun `FinancialTotals data class equality works correctly`() {
        val totals1 = FinancialCalculator.FinancialTotals(100, 200, 300, 400)
        val totals2 = FinancialCalculator.FinancialTotals(100, 200, 300, 400)
        val totals3 = FinancialCalculator.FinancialTotals(100, 200, 300, 401)
        
        assertEquals(totals1, totals2)
        assertNotEquals(totals1, totals3)
    }
    
    @Test
    fun `FinancialTotals data class copy works correctly`() {
        val totals = FinancialCalculator.FinancialTotals(100, 200, 300, 400)
        val copied = totals.copy(rekapIuran = 500)
        
        assertEquals(100, copied.totalIuranBulanan)
        assertEquals(200, copied.totalPengeluaran)
        assertEquals(300, copied.totalIuranIndividu)
        assertEquals(500, copied.rekapIuran)
    }
    
    // ===== BOUNDARY CONDITION TESTS =====
    
    @Test
    fun `calculateTotalIuranBulanan with Int MAX_VALUE threshold does not overflow`() {
        val threshold = Int.MAX_VALUE / 2
        val boundaryItems = listOf(
            DataItem(1, "User1", threshold, 0, 0),
            DataItem(2, "User2", 1, 0, 0)
        )
        // Should NOT overflow: threshold + 1 = Int.MAX_VALUE / 2 + 1
        val result = FinancialCalculator.calculateTotalIuranBulanan(boundaryItems)
        assertTrue(result > 0)
    }
    
    @Test
    fun `calculateTotalIuranIndividu with multiplier boundary does not overflow`() {
        val threshold = Int.MAX_VALUE / Constants.Financial.IURAN_MULTIPLIER
        val boundaryItems = listOf(
            DataItem(1, "User1", 0, 0, threshold),
            DataItem(2, "User2", 0, 0, 1)
        )
        // Should NOT overflow: (threshold + 1) * IURAN_MULTIPLIER should be safe
        val result = FinancialCalculator.calculateTotalIuranIndividu(boundaryItems)
        assertTrue(result > 0)
    }
    
    @Test
    fun `calculateAllTotals with zero values returns zeros`() {
        val zeroItems = listOf(
            DataItem(1, "User1", 0, 0, 0),
            DataItem(2, "User2", 0, 0, 0)
        )
        val result = FinancialCalculator.calculateAllTotals(zeroItems)
        
        assertEquals(0, result.totalIuranBulanan)
        assertEquals(0, result.totalPengeluaran)
        assertEquals(0, result.totalIuranIndividu)
        assertEquals(0, result.rekapIuran)
    }
    
    // ===== LARGE DATASET TESTS =====
    
    @Test
    fun `calculateAllTotals with large dataset handles efficiently`() {
        val largeDataset = (1..1000).map { index ->
            DataItem(index, "User$index", 1000, 500, 300)
        }
        
        val result = FinancialCalculator.calculateAllTotals(largeDataset)
        
        assertEquals(1000000, result.totalIuranBulanan) // 1000 * 1000
        assertEquals(500000, result.totalPengeluaran) // 1000 * 500
        assertEquals(900000, result.totalIuranIndividu) // (1000 * 300) * 3
        assertEquals(400000, result.rekapIuran) // 900000 - 500000
    }
    
    @Test
    fun `validateFinancialCalculations with large dataset handles efficiently`() {
        val largeDataset = (1..10000).map { index ->
            DataItem(index, "User$index", 100, 50, 30)
        }
        
        assertTrue(FinancialCalculator.validateFinancialCalculations(largeDataset))
    }
    
    // ===== CONSISTENCY TESTS =====
    
    @Test
    fun `calculateAllTotals matches individual calculation methods`() {
        val result = FinancialCalculator.calculateAllTotals(items)
        
        assertEquals(
            FinancialCalculator.calculateTotalIuranBulanan(items),
            result.totalIuranBulanan
        )
        assertEquals(
            FinancialCalculator.calculateTotalPengeluaran(items),
            result.totalPengeluaran
        )
        assertEquals(
            FinancialCalculator.calculateTotalIuranIndividu(items),
            result.totalIuranIndividu
        )
        assertEquals(
            FinancialCalculator.calculateRekapIuran(items),
            result.rekapIuran
        )
    }
}
