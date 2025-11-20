package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for FinancialCalculator class
 */
class FinancialCalculatorTest {

    @Test
    fun testCalculateTotalIuranBulanan_accumulatesCorrectly() {
        // Test data with multiple items to verify accumulation
        val testItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Doe",
                email = "jane@example.com",
                alamat = "Jl. Test 2",
                iuran_perwarga = 200,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 75,
                pengeluaran_iuran_warga = 30,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Bob",
                last_name = "Smith",
                email = "bob@example.com",
                alamat = "Jl. Test 3",
                iuran_perwarga = 300,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 100,
                pengeluaran_iuran_warga = 45,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val totalIuranBulanan = FinancialCalculator.calculateTotalIuranBulanan(testItems)

        // Verify calculation is correct
        assertEquals(600, totalIuranBulanan)  // 100 + 200 + 300
    }

    @Test
    fun testCalculateTotalPengeluaran_accumulatesCorrectly() {
        // Test data with multiple items to verify accumulation
        val testItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Doe",
                email = "jane@example.com",
                alamat = "Jl. Test 2",
                iuran_perwarga = 200,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 75,
                pengeluaran_iuran_warga = 30,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Bob",
                last_name = "Smith",
                email = "bob@example.com",
                alamat = "Jl. Test 3",
                iuran_perwarga = 300,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 100,
                pengeluaran_iuran_warga = 45,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val totalPengeluaran = FinancialCalculator.calculateTotalPengeluaran(testItems)

        // Verify calculation is correct
        assertEquals(100, totalPengeluaran)  // 25 + 30 + 45
    }

    @Test
    fun testCalculateTotalIuranIndividu_accumulatesCorrectly() {
        // Test data with multiple items to verify accumulation with multiplication by 3
        val testItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Doe",
                email = "jane@example.com",
                alamat = "Jl. Test 2",
                iuran_perwarga = 200,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 75,
                pengeluaran_iuran_warga = 30,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Bob",
                last_name = "Smith",
                email = "bob@example.com",
                alamat = "Jl. Test 3",
                iuran_perwarga = 300,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 100,
                pengeluaran_iuran_warga = 45,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val totalIuranIndividu = FinancialCalculator.calculateTotalIuranIndividu(testItems)

        // Verify calculation is correct: (50*3) + (75*3) + (100*3) = 150 + 225 + 300 = 675
        assertEquals(675, totalIuranIndividu)
    }

    @Test
    fun testCalculateRekapIuran_correctlyCalculates() {
        // Test data with multiple items
        val testItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Doe",
                email = "jane@example.com",
                alamat = "Jl. Test 2",
                iuran_perwarga = 200,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 75,
                pengeluaran_iuran_warga = 30,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Bob",
                last_name = "Smith",
                email = "bob@example.com",
                alamat = "Jl. Test 3",
                iuran_perwarga = 300,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 100,
                pengeluaran_iuran_warga = 45,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val rekapIuran = FinancialCalculator.calculateRekapIuran(testItems)

        // Total iuran individu: (50*3) + (75*3) + (100*3) = 675
        // Total pengeluaran: 25 + 30 + 45 = 100
        // Rekap iuran: 675 - 100 = 575
        assertEquals(575, rekapIuran)
    }

    @Test
    fun testValidateDataItem_validItems() {
        val validItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. Test 1",
            iuran_perwarga = 100,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 0,
            total_iuran_individu = 50,
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test",
            avatar = ""
        )

        assertTrue(FinancialCalculator.validateDataItem(validItem))
    }

    @Test
    fun testValidateDataItem_negativeValues() {
        val invalidItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. Test 1",
            iuran_perwarga = -100,  // Negative value
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 0,
            total_iuran_individu = 50,
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test",
            avatar = ""
        )

        assertFalse(FinancialCalculator.validateDataItem(invalidItem))
    }

    @Test
    fun testValidateDataItem_tooLargeValues() {
        val invalidItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. Test 1",
            iuran_perwarga = Int.MAX_VALUE,  // Too large value
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 0,
            total_iuran_individu = 50,
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test",
            avatar = ""
        )

        assertFalse(FinancialCalculator.validateDataItem(invalidItem))
    }

    @Test
    fun testValidateDataItems_allValid() {
        val validItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Doe",
                email = "jane@example.com",
                alamat = "Jl. Test 2",
                iuran_perwarga = 200,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 75,
                pengeluaran_iuran_warga = 30,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        assertTrue(FinancialCalculator.validateDataItems(validItems))
    }

    @Test
    fun testValidateDataItems_someInvalid() {
        val mixedItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Doe",
                email = "jane@example.com",
                alamat = "Jl. Test 2",
                iuran_perwarga = -100,  // Invalid
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 75,
                pengeluaran_iuran_warga = 30,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        assertFalse(FinancialCalculator.validateDataItems(mixedItems))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testCalculateTotalIuranBulanan_throwsOnInvalidData() {
        val invalidItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = -100,  // Invalid negative value
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        FinancialCalculator.calculateTotalIuranBulanan(invalidItems)
    }

    @Test(expected = ArithmeticException::class)
    fun testCalculateTotalIuranBulanan_throwsOnOverflow() {
        val itemsWithLargeValues = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = Int.MAX_VALUE - 1,  // Very large value that will cause overflow
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Doe",
                email = "jane@example.com",
                alamat = "Jl. Test 2",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 75,
                pengeluaran_iuran_warga = 30,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        FinancialCalculator.calculateTotalIuranBulanan(itemsWithLargeValues)
    }
}