package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.dto.LegacyDataItemDto
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ValidateFinancialDataUseCaseTest {

    private lateinit var useCase: ValidateFinancialDataUseCase

    @Before
    fun setup() {
        useCase = ValidateFinancialDataUseCase()
    }

    @Test
    fun `invoke returns true for valid LegacyDataItemDto`() {
        val item = LegacyDataItemDto(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 100000,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000,
            pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Maintenance",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertTrue(result)
    }

    @Test
    fun `invoke returns false when iuran_perwarga is negative`() {
        val item = LegacyDataItemDto(
            first_name = "Invalid",
            last_name = "User",
            email = "invalid@example.com",
            alamat = "Invalid St",
            iuran_perwarga = -1,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000,
            pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertFalse(result)
    }

    @Test
    fun `invoke returns false when pengeluaran_iuran_warga is negative`() {
        val item = LegacyDataItemDto(
            first_name = "Invalid",
            last_name = "User",
            email = "invalid@example.com",
            alamat = "Invalid St",
            iuran_perwarga = 100000,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000,
            pengeluaran_iuran_warga = -1,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertFalse(result)
    }

    @Test
    fun `invoke returns false when total_iuran_individu is negative`() {
        val item = LegacyDataItemDto(
            first_name = "Invalid",
            last_name = "User",
            email = "invalid@example.com",
            alamat = "Invalid St",
            iuran_perwarga = 100000,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = -1,
            pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertFalse(result)
    }

    @Test
    fun `invoke returns false when iuran_perwarga exceeds MAX_VALUE half`() {
        val maxInt = Int.MAX_VALUE / 2 + 1
        val item = LegacyDataItemDto(
            first_name = "Overflow",
            last_name = "User",
            email = "overflow@example.com",
            alamat = "Overflow St",
            iuran_perwarga = maxInt,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000,
            pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertFalse(result)
    }

    @Test
    fun `invoke returns false when pengeluaran_iuran_warga exceeds MAX_VALUE half`() {
        val maxInt = Int.MAX_VALUE / 2 + 1
        val item = LegacyDataItemDto(
            first_name = "Overflow",
            last_name = "User",
            email = "overflow@example.com",
            alamat = "Overflow St",
            iuran_perwarga = 100000,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000,
            pengeluaran_iuran_warga = maxInt,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertFalse(result)
    }

    @Test
    fun `invoke returns false when total_iuran_individu exceeds MAX_VALUE third`() {
        val maxInt = Int.MAX_VALUE / 3 + 1
        val item = LegacyDataItemDto(
            first_name = "Overflow",
            last_name = "User",
            email = "overflow@example.com",
            alamat = "Overflow St",
            iuran_perwarga = 100000,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = maxInt,
            pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertFalse(result)
    }

    @Test
    fun `invoke accepts zero values`() {
        val item = LegacyDataItemDto(
            first_name = "Zero",
            last_name = "User",
            email = "zero@example.com",
            alamat = "Zero St",
            iuran_perwarga = 0,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0,
            pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "None",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertTrue(result)
    }

    @Test
    fun `validateAll returns true when all items are valid`() {
        val items = listOf(
            LegacyDataItemDto(
                first_name = "John", last_name = "Doe", email = "john@example.com", alamat = "123 Main St",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "Maintenance", avatar = "https://example.com/avatar1.jpg"
            ),
            LegacyDataItemDto(
                first_name = "Jane", last_name = "Smith", email = "jane@example.com", alamat = "456 Oak Ave",
                iuran_perwarga = 150000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 75000, pengeluaran_iuran_warga = 30000,
                pemanfaatan_iuran = "Cleaning", avatar = "https://example.com/avatar2.jpg"
            )
        )

        val result = useCase.validateAll(items)
        assertTrue(result)
    }

    @Test
    fun `validateAll returns false when one item is invalid`() {
        val items = listOf(
            LegacyDataItemDto(
                first_name = "Valid", last_name = "User", email = "valid@example.com", alamat = "x",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            LegacyDataItemDto(
                first_name = "Invalid", last_name = "User", email = "invalid@example.com", alamat = "y",
                iuran_perwarga = -1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        val result = useCase.validateAll(items)
        assertFalse(result)
    }

    @Test
    fun `validateAll returns true for empty list`() {
        val result = useCase.validateAll(emptyList())
        assertTrue(result)
    }

    @Test
    fun `validateAll returns true for single valid item`() {
        val item = LegacyDataItemDto(
            first_name = "Single", last_name = "User", email = "single@example.com", alamat = "Single St",
            iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Test", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase.validateAll(listOf(item))
        assertTrue(result)
    }

    @Test
    fun `validateAll returns false for single invalid item`() {
        val item = LegacyDataItemDto(
            first_name = "Invalid", last_name = "User", email = "invalid@example.com", alamat = "Invalid St",
            iuran_perwarga = -1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Test", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase.validateAll(listOf(item))
        assertFalse(result)
    }

    @Test
    fun `validateAll checks all items even if first is invalid`() {
        val items = listOf(
            LegacyDataItemDto(
                first_name = "Invalid1", last_name = "User", email = "invalid1@example.com", alamat = "x",
                iuran_perwarga = -1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            LegacyDataItemDto(
                first_name = "Invalid2", last_name = "User", email = "invalid2@example.com", alamat = "y",
                iuran_perwarga = -2, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        val result = useCase.validateAll(items)
        assertFalse(result)
    }

    @Test
    fun `validateCalculations returns true when all items are valid and calculations succeed`() {
        val items = listOf(
            LegacyDataItemDto(
                first_name = "Valid", last_name = "User", email = "valid@example.com", alamat = "x",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "x", avatar = "x"
            )
        )

        val result = useCase.validateCalculations(items)
        assertTrue(result)
    }

    @Test
    fun `validateCalculations returns false when one item is invalid`() {
        val items = listOf(
            LegacyDataItemDto(
                first_name = "Valid", last_name = "User", email = "valid@example.com", alamat = "x",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            LegacyDataItemDto(
                first_name = "Invalid", last_name = "User", email = "invalid@example.com", alamat = "y",
                iuran_perwarga = -1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        val result = useCase.validateCalculations(items)
        assertFalse(result)
    }

    @Test
    fun `validateCalculations returns false when calculations would overflow`() {
        val maxInt = Int.MAX_VALUE
        val items = listOf(
            LegacyDataItemDto(
                first_name = "Overflow", last_name = "User", email = "overflow@example.com", alamat = "x",
                iuran_perwarga = maxInt - 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            LegacyDataItemDto(
                first_name = "Overflow2", last_name = "User", email = "overflow2@example.com", alamat = "y",
                iuran_perwarga = 200000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        val result = useCase.validateCalculations(items)
        assertFalse(result)
    }

    @Test
    fun `validateCalculations returns true for empty list`() {
        val result = useCase.validateCalculations(emptyList())
        assertTrue(result)
    }

    @Test
    fun `validateCalculations returns true for boundary values`() {
        val item = LegacyDataItemDto(
            first_name = "Boundary", last_name = "Test", email = "boundary@example.com", alamat = "Boundary St",
            iuran_perwarga = Int.MAX_VALUE / 2, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = Int.MAX_VALUE / 3, pengeluaran_iuran_warga = Int.MAX_VALUE / 2,
            pemanfaatan_iuran = "Test", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase.validateCalculations(listOf(item))
        assertTrue(result)
    }

    @Test
    fun `invoke handles all zero boundary values`() {
        val item = LegacyDataItemDto(
            first_name = "Zero", last_name = "Boundary", email = "zero@example.com", alamat = "Zero St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Zero", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertTrue(result)
    }

    @Test
    fun `invoke validates iuran_perwarga upper boundary`() {
        val item = LegacyDataItemDto(
            first_name = "Boundary", last_name = "Test", email = "boundary@example.com", alamat = "Boundary St",
            iuran_perwarga = Int.MAX_VALUE / 2, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Test", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertTrue(result)
    }

    @Test
    fun `invoke rejects iuran_perwarga above upper boundary`() {
        val item = LegacyDataItemDto(
            first_name = "Boundary", last_name = "Test", email = "boundary@example.com", alamat = "Boundary St",
            iuran_perwarga = Int.MAX_VALUE / 2 + 1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Test", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertFalse(result)
    }

    @Test
    fun `invoke validates total_iuran_individu upper boundary`() {
        val item = LegacyDataItemDto(
            first_name = "Boundary", last_name = "Test", email = "boundary@example.com", alamat = "Boundary St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = Int.MAX_VALUE / 3, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Test", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertTrue(result)
    }

    @Test
    fun `invoke rejects total_iuran_individu above upper boundary`() {
        val item = LegacyDataItemDto(
            first_name = "Boundary", last_name = "Test", email = "boundary@example.com", alamat = "Boundary St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = Int.MAX_VALUE / 3 + 1, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Test", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(item)
        assertFalse(result)
    }
}
