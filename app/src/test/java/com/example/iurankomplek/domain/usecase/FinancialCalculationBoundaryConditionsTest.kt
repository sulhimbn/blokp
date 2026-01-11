package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.dto.LegacyDataItemDto
import org.junit.Assert.*
import org.junit.Test

class FinancialCalculationBoundaryConditionsTest {

    @Test
    fun `financial calculations handle boundary value zero correctly`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val item = LegacyDataItemDto(
            first_name = "Zero", last_name = "Boundary", email = "zero@boundary.com", alamat = "Zero St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Zero", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))

        assertEquals(0, result.totalIuranBulanan)
        assertEquals(0, result.totalPengeluaran)
        assertEquals(0, result.totalIuranIndividu)
        assertEquals(0, result.rekapIuran)
    }

    @Test
    fun `financial calculations handle boundary value one correctly`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val item = LegacyDataItemDto(
            first_name = "One", last_name = "Boundary", email = "one@boundary.com", alamat = "One St",
            iuran_perwarga = 1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 1, pengeluaran_iuran_warga = 1,
            pemanfaatan_iuran = "One", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))

        assertEquals(1, result.totalIuranBulanan)
        assertEquals(1, result.totalPengeluaran)
        assertEquals(3, result.totalIuranIndividu)
        assertEquals(2, result.rekapIuran)
    }

    @Test
    fun `financial calculations handle maximum safe value for iuran_perwarga`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val maxSafeValue = Int.MAX_VALUE / 2

        val item = LegacyDataItemDto(
            first_name = "Max", last_name = "Safe", email = "max@safe.com", alamat = "Max St",
            iuran_perwarga = maxSafeValue, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Max", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))

        assertEquals(maxSafeValue, result.totalIuranBulanan)
    }

    @Test
    fun `financial calculations handle maximum safe value for pengeluaran_iuran_warga`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val maxSafeValue = Int.MAX_VALUE / 2

        val item = LegacyDataItemDto(
            first_name = "Max", last_name = "Expense", email = "max@expense.com", alamat = "Max St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = maxSafeValue,
            pemanfaatan_iuran = "Max", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))

        assertEquals(maxSafeValue, result.totalPengeluaran)
    }

    @Test
    fun `financial calculations handle maximum safe value for total_iuran_individu`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val maxSafeValue = Int.MAX_VALUE / 3

        val item = LegacyDataItemDto(
            first_name = "Max", last_name = "Individu", email = "max@individu.com", alamat = "Max St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = maxSafeValue, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Max", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))

        assertEquals(maxSafeValue * 3, result.totalIuranIndividu)
    }

    @Test
    fun `financial calculations throw exception on overflow boundary iuran_perwarga`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val overflowValue = Int.MAX_VALUE / 2 + 1

        val item = LegacyDataItemDto(
            first_name = "Overflow", last_name = "Test", email = "overflow@test.com", alamat = "Overflow St",
            iuran_perwarga = overflowValue, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Overflow", avatar = "https://example.com/avatar.jpg"
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase(listOf(item))
        }

        assertTrue(exception.message!!.contains("too large"))
    }

    @Test
    fun `financial calculations throw exception on overflow boundary pengeluaran_iuran_warga`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val overflowValue = Int.MAX_VALUE / 2 + 1

        val item = LegacyDataItemDto(
            first_name = "Overflow", last_name = "Expense", email = "overflow@expense.com", alamat = "Overflow St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = overflowValue,
            pemanfaatan_iuran = "Overflow", avatar = "https://example.com/avatar.jpg"
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase(listOf(item))
        }

        assertTrue(exception.message!!.contains("too large"))
    }

    @Test
    fun `financial calculations throw exception on overflow boundary total_iuran_individu`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val overflowValue = Int.MAX_VALUE / 3 + 1

        val item = LegacyDataItemDto(
            first_name = "Overflow", last_name = "Individu", email = "overflow@individu.com", alamat = "Overflow St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = overflowValue, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Overflow", avatar = "https://example.com/avatar.jpg"
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase(listOf(item))
        }

        assertTrue(exception.message!!.contains("too large"))
    }

    @Test
    fun `financial calculations handle accumulated overflow correctly`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val halfMax = Int.MAX_VALUE / 2

        val items = listOf(
            LegacyDataItemDto(
                first_name = "Item1", last_name = "A", email = "item1@test.com", alamat = "A St",
                iuran_perwarga = halfMax - 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "Item1", avatar = "https://example.com/avatar1.jpg"
            ),
            LegacyDataItemDto(
                first_name = "Item2", last_name = "B", email = "item2@test.com", alamat = "B St",
                iuran_perwarga = 200000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "Item2", avatar = "https://example.com/avatar2.jpg"
            )
        )

        val exception = assertThrows(ArithmeticException::class.java) {
            useCase(items)
        }

        assertTrue(exception.message!!.contains("overflow"))
    }

    @Test
    fun `financial calculations handle rekap iuran underflow correctly`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val item = LegacyDataItemDto(
            first_name = "Underflow", last_name = "Test", email = "underflow@test.com", alamat = "Underflow St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = Int.MAX_VALUE,
            pemanfaatan_iuran = "Underflow", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))

        assertEquals(0, result.rekapIuran)
    }

    @Test
    fun `financial calculations handle negative boundary values correctly`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val item = LegacyDataItemDto(
            first_name = "Negative", last_name = "Boundary", email = "negative@boundary.com", alamat = "Negative St",
            iuran_perwarga = -1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Negative", avatar = "https://example.com/avatar.jpg"
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase(listOf(item))
        }

        assertTrue(exception.message!!.contains("must be >= 0"))
    }

    @Test
    fun `financial calculations handle mixed boundary values correctly`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val items = listOf(
            LegacyDataItemDto(
                first_name = "Zero", last_name = "Item", email = "zero@test.com", alamat = "Zero St",
                iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "Zero", avatar = "https://example.com/avatar1.jpg"
            ),
            LegacyDataItemDto(
                first_name = "One", last_name = "Item", email = "one@test.com", alamat = "One St",
                iuran_perwarga = 1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 1, pengeluaran_iuran_warga = 1,
                pemanfaatan_iuran = "One", avatar = "https://example.com/avatar2.jpg"
            ),
            LegacyDataItemDto(
                first_name = "Large", last_name = "Item", email = "large@test.com", alamat = "Large St",
                iuran_perwarga = 1000000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 500000, pengeluaran_iuran_warga = 200000,
                pemanfaatan_iuran = "Large", avatar = "https://example.com/avatar3.jpg"
            )
        )

        val result = useCase(items)

        assertEquals(1000001, result.totalIuranBulanan)
        assertEquals(200001, result.totalPengeluaran)
        assertEquals(1500003, result.totalIuranIndividu)
        assertEquals(1300002, result.rekapIuran)
    }

    @Test
    fun `financial calculations handle large dataset without overflow`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val items = mutableListOf<LegacyDataItemDto>()

        for (i in 1..1000) {
            items.add(
                LegacyDataItemDto(
                    first_name = "User$i", last_name = "Test", email = "user$i@test.com", alamat = "Test St",
                    iuran_perwarga = 10000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                    total_iuran_individu = 5000, pengeluaran_iuran_warga = 2000,
                    pemanfaatan_iuran = "Test$i", avatar = "https://example.com/avatar$i.jpg"
                )
            )
        }

        val result = useCase(items)

        assertEquals(10000000, result.totalIuranBulanan)
        assertEquals(2000000, result.totalPengeluaran)
        assertEquals(15000000, result.totalIuranIndividu)
        assertEquals(13000000, result.rekapIuran)
    }

    @Test
    fun `financial calculations verify rekap iuran formula`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val item = LegacyDataItemDto(
            first_name = "Formula", last_name = "Test", email = "formula@test.com", alamat = "Formula St",
            iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Formula", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))

        val expectedTotalIuranIndividu = 50000 * 3
        val expectedRekapIuran = expectedTotalIuranIndividu - 20000

        assertEquals(expectedTotalIuranIndividu, result.totalIuranIndividu)
        assertEquals(expectedRekapIuran, result.rekapIuran)
        assertEquals(130000, result.rekapIuran)
    }

    @Test
    fun `financial calculations handle pengeluaran equal to total_iuran_individu times three`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val item = LegacyDataItemDto(
            first_name = "Equal", last_name = "Test", email = "equal@test.com", alamat = "Equal St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 10000, pengeluaran_iuran_warga = 30000,
            pemanfaatan_iuran = "Equal", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))

        assertEquals(30000, result.totalIuranIndividu)
        assertEquals(30000, result.totalPengeluaran)
        assertEquals(0, result.rekapIuran)
    }

    @Test
    fun `financial calculations handle pengeluaran slightly less than total_iuran_individu times three`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val item = LegacyDataItemDto(
            first_name = "Less", last_name = "Test", email = "less@test.com", alamat = "Less St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 10000, pengeluaran_iuran_warga = 29999,
            pemanfaatan_iuran = "Less", avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))

        assertEquals(30000, result.totalIuranIndividu)
        assertEquals(29999, result.totalPengeluaran)
        assertEquals(1, result.rekapIuran)
    }

    @Test
    fun `financial calculations validate all items before calculation`() {
        val useCase = CalculateFinancialTotalsUseCase()

        val items = listOf(
            LegacyDataItemDto(
                first_name = "Valid", last_name = "Item", email = "valid@test.com", alamat = "Valid St",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "Valid", avatar = "https://example.com/avatar1.jpg"
            ),
            LegacyDataItemDto(
                first_name = "Invalid", last_name = "Item", email = "invalid@test.com", alamat = "Invalid St",
                iuran_perwarga = -1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "Invalid", avatar = "https://example.com/avatar2.jpg"
            )
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase(items)
        }

        assertTrue(exception.message!!.contains("must be >= 0"))
    }
}
