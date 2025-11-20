package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.FinancialValidator
import com.example.iurankomplek.utils.ValidationResult
import org.junit.Test
import org.junit.Assert.*

class FinancialValidatorTest {

    @Test
    fun `validateFinancialValue should return Valid for acceptable values`() {
        val result = FinancialValidator.validateFinancialValue(1000, "test_field")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateFinancialValue should return Invalid for negative values`() {
        val result = FinancialValidator.validateFinancialValue(-100, "test_field")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue(result.message.contains("Negative value not allowed"))
    }

    @Test
    fun `validateFinancialValue should return Invalid for values exceeding maximum`() {
        val result = FinancialValidator.validateFinancialValue(200_000_000, "test_field")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue(result.message.contains("Value too large"))
    }

    @Test
    fun `validateDataItem should return Valid for valid DataItem`() {
        val validItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 1000,
            total_iuran_rekap = 2000,
            jumlah_iuran_bulanan = 3000,
            total_iuran_individu = 4000,
            pengeluaran_iuran_warga = 5000,
            pemanfaatan_iuran = "Expense",
            avatar = "avatar_url"
        )
        
        val result = FinancialValidator.validateDataItem(validItem)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateDataItem should return Invalid for DataItem with negative values`() {
        val invalidItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = -1000, // Negative value
            total_iuran_rekap = 2000,
            jumlah_iuran_bulanan = 3000,
            total_iuran_individu = 4000,
            pengeluaran_iuran_warga = 5000,
            pemanfaatan_iuran = "Expense",
            avatar = "avatar_url"
        )
        
        val result = FinancialValidator.validateDataItem(invalidItem)
        assertTrue(result is ValidationResult.Invalid)
        assertTrue(result.message.contains("Negative value not allowed"))
    }

    @Test
    fun `validateDataItems should return Valid for valid list`() {
        val validItems = listOf(
            createValidDataItem(1000),
            createValidDataItem(2000)
        )
        
        val result = FinancialValidator.validateDataItems(validItems)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateDataItems should return Invalid for empty list`() {
        val result = FinancialValidator.validateDataItems(emptyList())
        assertTrue(result is ValidationResult.Invalid)
        assertTrue(result.message.contains("Financial data list is empty"))
    }

    @Test
    fun `validateDataItems should return Invalid for list containing invalid items`() {
        val mixedItems = listOf(
            createValidDataItem(1000),
            createInvalidDataItem() // contains negative values
        )
        
        val result = FinancialValidator.validateDataItems(mixedItems)
        assertTrue(result is ValidationResult.Invalid)
        assertTrue(result.message.contains("Item at index 1"))
    }

    @Test
    fun `validateCalculationOverflow should return Valid for safe additions`() {
        val result = FinancialValidator.validateCalculationOverflow(1000, 2000, "test_calculation")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateCalculationOverflow should return Invalid for potential overflow`() {
        val result = FinancialValidator.validateCalculationOverflow(Int.MAX_VALUE - 1, 10, "test_calculation")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue(result.message.contains("Calculation overflow detected"))
    }

    @Test
    fun `validateMultiplicationOverflow should return Valid for safe multiplications`() {
        val result = FinancialValidator.validateMultiplicationOverflow(1000, 3, "test_multiplication")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateMultiplicationOverflow should return Invalid for potential overflow`() {
        val result = FinancialValidator.validateMultiplicationOverflow(Int.MAX_VALUE / 2, 3, "test_multiplication")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue(result.message.contains("Multiplication overflow detected"))
    }

    @Test
    fun `validateFinalResult should return Valid for acceptable results`() {
        val result = FinancialValidator.validateFinalResult(1000, "test_result")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateFinalResult should return Invalid for negative results`() {
        val result = FinancialValidator.validateFinalResult(-100, "test_result")
        assertTrue(result is ValidationResult.Invalid)
        assertTrue(result.message.contains("Negative result not allowed"))
    }

    private fun createValidDataItem(value: Int = 1000) = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = "john@example.com",
        alamat = "123 Main St",
        iuran_perwarga = value,
        total_iuran_rekap = value + 100,
        jumlah_iuran_bulanan = value + 200,
        total_iuran_individu = value + 300,
        pengeluaran_iuran_warga = value + 400,
        pemanfaatan_iuran = "Expense",
        avatar = "avatar_url"
    )

    private fun createInvalidDataItem() = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = "john@example.com",
        alamat = "123 Main St",
        iuran_perwarga = -1000, // Negative
        total_iuran_rekap = 2000,
        jumlah_iuran_bulanan = 3000,
        total_iuran_individu = 4000,
        pengeluaran_iuran_warga = 5000,
        pemanfaatan_iuran = "Expense",
        avatar = "avatar_url"
    )
}