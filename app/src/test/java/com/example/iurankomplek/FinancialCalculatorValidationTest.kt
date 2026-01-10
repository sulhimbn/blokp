package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Assert.*
import org.junit.Test

class FinancialCalculatorValidationTest {

    @Test
    fun validateFinancialCalculations_validDataReturnsTrue() {
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
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(testItems)

        assertTrue("Valid data should pass validation", result)
    }

    @Test
    fun validateFinancialCalculations_emptyListReturnsTrue() {
        val emptyItems = emptyList<DataItem>()

        val result = FinancialCalculator.validateFinancialCalculations(emptyItems)

        assertTrue("Empty list should pass validation", result)
    }

    @Test
    fun validateFinancialCalculations_negativeIuranPerwargaReturnsFalse() {
        val invalidItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = -100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(invalidItems)

        assertFalse("Negative iuran_perwarga should fail validation", result)
    }

    @Test
    fun validateFinancialCalculations_negativePengeluaranReturnsFalse() {
        val invalidItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = -25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(invalidItems)

        assertFalse("Negative pengeluaran_iuran_warga should fail validation", result)
    }

    @Test
    fun validateFinancialCalculations_negativeTotalIuranIndividuReturnsFalse() {
        val invalidItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = -50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(invalidItems)

        assertFalse("Negative total_iuran_individu should fail validation", result)
    }

    @Test
    fun validateFinancialCalculations_overflowIuranPerwargaReturnsFalse() {
        val invalidItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = Int.MAX_VALUE,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(invalidItems)

        assertFalse("Overflow value should fail validation", result)
    }

    @Test
    fun validateFinancialCalculations_totalOverflowReturnsFalse() {
        val itemsWithOverflow = listOf(
            DataItem(
                first_name = "User1",
                last_name = "Test",
                email = "user1@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = Int.MAX_VALUE - 10,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "Test",
                avatar = ""
            ),
            DataItem(
                first_name = "User2",
                last_name = "Test",
                email = "user2@example.com",
                alamat = "Jl. Test 2",
                iuran_perwarga = 20,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(itemsWithOverflow)

        assertFalse("Total overflow should fail validation", result)
    }

    @Test
    fun validateFinancialCalculations_multiplicationOverflowReturnsFalse() {
        val invalidItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 0,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = Int.MAX_VALUE / 2,
                pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(invalidItems)

        assertFalse("Multiplication overflow should fail validation", result)
    }

    @Test
    fun validateFinancialCalculations_rekapUnderflowReturnsFalse() {
        val itemsWithUnderflow = listOf(
            DataItem(
                first_name = "User1",
                last_name = "Test",
                email = "user1@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 0,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 0,
                pengeluaran_iuran_warga = Int.MAX_VALUE,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(itemsWithUnderflow)

        assertFalse("Rekap underflow should fail validation", result)
    }

    @Test
    fun validateFinancialCalculations_multipleValidItemsReturnsTrue() {
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
                last_name = "Smith",
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
                last_name = "Johnson",
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

        val result = FinancialCalculator.validateFinancialCalculations(validItems)

        assertTrue("Multiple valid items should pass validation", result)
    }

    @Test
    fun validateFinancialCalculations_singleInvalidItemReturnsFalse() {
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
                first_name = "Invalid",
                last_name = "User",
                email = "invalid@example.com",
                alamat = "Jl. Test 2",
                iuran_perwarga = -100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 75,
                pengeluaran_iuran_warga = 30,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(mixedItems)

        assertFalse("Single invalid item should fail validation", result)
    }

    @Test
    fun validateFinancialCalculations_zeroValuesReturnTrue() {
        val zeroItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 0,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 0,
                pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(zeroItems)

        assertTrue("Zero values should pass validation", result)
    }

    @Test
    fun validateFinancialCalculations_boundaryValuesReturnTrue() {
        val boundaryItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = Int.MAX_VALUE / 2,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = Int.MAX_VALUE / 3,
                pengeluaran_iuran_warga = Int.MAX_VALUE / 2,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(boundaryItems)

        assertTrue("Boundary values should pass validation", result)
    }

    @Test
    fun validateFinancialCalculations_boundaryPlusOneReturnsFalse() {
        val invalidBoundaryItems = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = (Int.MAX_VALUE / 2) + 1,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        )

        val result = FinancialCalculator.validateFinancialCalculations(invalidBoundaryItems)

        assertFalse("Boundary + 1 should fail validation", result)
    }

    @Test
    fun validateFinancialCalculations_largeListReturnsTrue() {
        val largeItems = (1..100).map { index ->
            DataItem(
                first_name = "User$index",
                last_name = "Test",
                email = "user$index@example.com",
                alamat = "Jl. Test $index",
                iuran_perwarga = 1000,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 500,
                pengeluaran_iuran_warga = 200,
                pemanfaatan_iuran = "Test",
                avatar = ""
            )
        }

        val result = FinancialCalculator.validateFinancialCalculations(largeItems)

        assertTrue("Large list of valid items should pass validation", result)
    }
}
