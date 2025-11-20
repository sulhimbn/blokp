package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.ValidatedDataItem
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ValidatedDataItem class
 */
class ValidatedDataItemTest {

    @Test
    fun testValidatedDataItem_fromValidDataItem() {
        val originalItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. Test 1",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 75,
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test expense",
            avatar = "https://example.com/avatar.jpg"
        )

        val validatedItem = ValidatedDataItem.fromDataItem(originalItem)

        assertEquals("John", validatedItem.first_name)
        assertEquals("Doe", validatedItem.last_name)
        assertEquals("john@example.com", validatedItem.email)
        assertEquals("Jl. Test 1", validatedItem.alamat)
        assertEquals(100, validatedItem.iuran_perwarga)
        assertEquals(500, validatedItem.total_iuran_rekap)
        assertEquals(200, validatedItem.jumlah_iuran_bulanan)
        assertEquals(75, validatedItem.total_iuran_individu)
        assertEquals(25, validatedItem.pengeluaran_iuran_warga)
        assertEquals("Test expense", validatedItem.pemanfaatan_iuran)
        assertEquals("https://example.com/avatar.jpg", validatedItem.avatar)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidatedDataItem_fromDataItemWithNegativeIuranPerwarga() {
        val originalItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. Test 1",
            iuran_perwarga = -100,  // Negative value
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 75,
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test expense",
            avatar = "https://example.com/avatar.jpg"
        )

        ValidatedDataItem.fromDataItem(originalItem)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidatedDataItem_fromDataItemWithNegativeTotalIuranRekap() {
        val originalItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. Test 1",
            iuran_perwarga = 100,
            total_iuran_rekap = -500,  // Negative value
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 75,
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test expense",
            avatar = "https://example.com/avatar.jpg"
        )

        ValidatedDataItem.fromDataItem(originalItem)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidatedDataItem_fromDataItemWithNegativeTotalIuranIndividu() {
        val originalItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. Test 1",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = -75,  // Negative value
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test expense",
            avatar = "https://example.com/avatar.jpg"
        )

        ValidatedDataItem.fromDataItem(originalItem)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidatedDataItem_fromDataItemWithTooLargeTotalIuranIndividu() {
        val originalItem = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. Test 1",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = Int.MAX_VALUE,  // Too large for multiplication by 3
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test expense",
            avatar = "https://example.com/avatar.jpg"
        )

        ValidatedDataItem.fromDataItem(originalItem)
    }

    @Test
    fun testValidatedDataItem_createValidated_success() {
        val validatedItem = ValidatedDataItem.createValidated(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. Test 1",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 75,
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test expense",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals("John", validatedItem.first_name)
        assertEquals("Doe", validatedItem.last_name)
        assertEquals("john@example.com", validatedItem.email)
        assertEquals("Jl. Test 1", validatedItem.alamat)
        assertEquals(100, validatedItem.iuran_perwarga)
        assertEquals(500, validatedItem.total_iuran_rekap)
        assertEquals(200, validatedItem.jumlah_iuran_bulanan)
        assertEquals(75, validatedItem.total_iuran_individu)
        assertEquals(25, validatedItem.pengeluaran_iuran_warga)
        assertEquals("Test expense", validatedItem.pemanfaatan_iuran)
        assertEquals("https://example.com/avatar.jpg", validatedItem.avatar)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testValidatedDataItem_createValidated_withNegativeValue() {
        ValidatedDataItem.createValidated(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "Jl. Test 1",
            iuran_perwarga = -100,  // Negative value
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 75,
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "Test expense",
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test
    fun testValidatedDataItem_sanitizesFields() {
        val originalItem = DataItem(
            first_name = "   John   ",  // Has whitespace to be trimmed
            last_name = "   Doe   ",   // Has whitespace to be trimmed
            email = "invalid-email",    // Invalid email to be replaced
            alamat = "   ",            // Empty address to be replaced
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 75,
            pengeluaran_iuran_warga = 25,
            pemanfaatan_iuran = "   ", // Empty expense to be replaced
            avatar = "https://example.com/avatar.jpg"
        )

        val validatedItem = ValidatedDataItem.fromDataItem(originalItem)

        // Check that sanitization occurred
        assertEquals("John", validatedItem.first_name)  // Whitespace trimmed
        assertEquals("Doe", validatedItem.last_name)    // Whitespace trimmed
        assertEquals("invalid@email.com", validatedItem.email)  // Invalid email replaced
        assertEquals("Address not available", validatedItem.alamat)  // Empty address replaced
        assertEquals("Unknown expense", validatedItem.pemanfaatan_iuran)  // Empty expense replaced
    }
}