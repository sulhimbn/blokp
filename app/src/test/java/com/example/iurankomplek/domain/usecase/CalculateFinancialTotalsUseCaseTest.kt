package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.dto.LegacyLegacyDataItemDtoDto
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculateFinancialTotalsUseCaseTest {

    private lateinit var useCase: CalculateFinancialTotalsUseCase

    @Before
    fun setup() {
        useCase = CalculateFinancialTotalsUseCase()
    }

    @Test
    fun `invoke with empty list returns zero totals`() {
        val result = useCase(emptyList())

        assertEquals(0, result.totalIuranBulanan)
        assertEquals(0, result.totalPengeluaran)
        assertEquals(0, result.totalIuranIndividu)
        assertEquals(0, result.rekapIuran)
    }

    @Test
    fun `invoke with single item calculates totals correctly`() {
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

        val result = useCase(listOf(item))

        assertEquals(100000, result.totalIuranBulanan)
        assertEquals(20000, result.totalPengeluaran)
        assertEquals(150000, result.totalIuranIndividu)
        assertEquals(130000, result.rekapIuran)
    }

    @Test
    fun `invoke with multiple items aggregates totals correctly`() {
        val items = listOf(
            LegacyDataItemDto(
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
                avatar = "https://example.com/avatar1.jpg"
            ),
            LegacyDataItemDto(
                first_name = "Jane",
                last_name = "Smith",
                email = "jane@example.com",
                alamat = "456 Oak Ave",
                iuran_perwarga = 150000,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 1,
                total_iuran_individu = 75000,
                pengeluaran_iuran_warga = 30000,
                pemanfaatan_iuran = "Cleaning",
                avatar = "https://example.com/avatar2.jpg"
            )
        )

        val result = useCase(items)

        assertEquals(250000, result.totalIuranBulanan)
        assertEquals(50000, result.totalPengeluaran)
        assertEquals(375000, result.totalIuranIndividu)
        assertEquals(325000, result.rekapIuran)
    }

    @Test
    fun `invoke calculates total iuran bulanan correctly`() {
        val items = listOf(
            LegacyDataItemDto(
                first_name = "A", last_name = "B", email = "a@b.com", alamat = "x",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 10000,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            LegacyDataItemDto(
                first_name = "C", last_name = "D", email = "c@d.com", alamat = "y",
                iuran_perwarga = 200000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 100000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        val result = useCase(items)
        assertEquals(300000, result.totalIuranBulanan)
    }

    @Test
    fun `invoke calculates total pengeluaran correctly`() {
        val items = listOf(
            LegacyDataItemDto(
                first_name = "A", last_name = "B", email = "a@b.com", alamat = "x",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 10000,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            LegacyDataItemDto(
                first_name = "C", last_name = "D", email = "c@d.com", alamat = "y",
                iuran_perwarga = 200000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 100000, pengeluaran_iuran_warga = 30000,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        val result = useCase(items)
        assertEquals(40000, result.totalPengeluaran)
    }

    @Test
    fun `invoke multiplies total iuran individu by 3`() {
        val items = listOf(
            LegacyDataItemDto(
                first_name = "A", last_name = "B", email = "a@b.com", alamat = "x",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 10000,
                pemanfaatan_iuran = "x", avatar = "x"
            )
        )

        val result = useCase(items)
        assertEquals(150000, result.totalIuranIndividu)
    }

    @Test
    fun `invoke calculates rekap iuran as total iuran individu minus total pengeluaran`() {
        val items = listOf(
            LegacyDataItemDto(
                first_name = "A", last_name = "B", email = "a@b.com", alamat = "x",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 100000, pengeluaran_iuran_warga = 30000,
                pemanfaatan_iuran = "x", avatar = "x"
            )
        )

        val result = useCase(items)
        assertEquals(270000, result.rekapIuran)
    }

    @Test
    fun `invoke handles zero values correctly`() {
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

        val result = useCase(listOf(item))

        assertEquals(0, result.totalIuranBulanan)
        assertEquals(0, result.totalPengeluaran)
        assertEquals(0, result.totalIuranIndividu)
        assertEquals(0, result.rekapIuran)
    }

    @Test
    fun `invoke returns zero rekap iuran when pengeluaran exceeds total iuran individu`() {
        val item = LegacyDataItemDto(
            first_name = "Test",
            last_name = "User",
            email = "test@example.com",
            alamat = "Test St",
            iuran_perwarga = 100000,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000,
            pengeluaran_iuran_warga = 200000,
            pemanfaatan_iuran = "Expensive",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))
        assertEquals(0, result.rekapIuran)
    }

    @Test
    fun `invoke throws IllegalArgumentException when iuran_perwarga is negative`() {
        val item = LegacyDataItemDto(
            first_name = "Bad",
            last_name = "Data",
            email = "bad@example.com",
            alamat = "Bad St",
            iuran_perwarga = -1,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000,
            pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase(listOf(item))
        }
        assertTrue(exception.message!!.contains("iuran_perwarga must be >= 0"))
    }

    @Test
    fun `invoke throws IllegalArgumentException when pengeluaran_iuran_warga is negative`() {
        val item = LegacyDataItemDto(
            first_name = "Bad",
            last_name = "Data",
            email = "bad@example.com",
            alamat = "Bad St",
            iuran_perwarga = 100000,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000,
            pengeluaran_iuran_warga = -1,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase(listOf(item))
        }
        assertTrue(exception.message!!.contains("pengeluaran_iuran_warga must be >= 0"))
    }

    @Test
    fun `invoke throws IllegalArgumentException when total_iuran_individu is negative`() {
        val item = LegacyDataItemDto(
            first_name = "Bad",
            last_name = "Data",
            email = "bad@example.com",
            alamat = "Bad St",
            iuran_perwarga = 100000,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = -1,
            pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase(listOf(item))
        }
        assertTrue(exception.message!!.contains("total_iuran_individu must be >= 0"))
    }

    @Test
    fun `invoke throws ArithmeticException on total iuran bulanan overflow`() {
        val maxInt = Int.MAX_VALUE
        val items = listOf(
            LegacyDataItemDto(
                first_name = "A", last_name = "B", email = "a@b.com", alamat = "x",
                iuran_perwarga = maxInt - 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            LegacyDataItemDto(
                first_name = "C", last_name = "D", email = "c@d.com", alamat = "y",
                iuran_perwarga = 200000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        assertThrows(ArithmeticException::class.java) {
            useCase(items)
        }
    }

    @Test
    fun `invoke throws ArithmeticException on total pengeluaran overflow`() {
        val maxInt = Int.MAX_VALUE
        val items = listOf(
            LegacyDataItemDto(
                first_name = "A", last_name = "B", email = "a@b.com", alamat = "x",
                iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = maxInt - 100000,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            LegacyDataItemDto(
                first_name = "C", last_name = "D", email = "c@d.com", alamat = "y",
                iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 200000,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        assertThrows(ArithmeticException::class.java) {
            useCase(items)
        }
    }

    @Test
    fun `invoke throws ArithmeticException on total iuran individu overflow before multiplication`() {
        val maxInt = Int.MAX_VALUE / 3 + 1
        val item = LegacyDataItemDto(
            first_name = "Overflow",
            last_name = "Test",
            email = "overflow@example.com",
            alamat = "Overflow St",
            iuran_perwarga = 0,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = maxInt,
            pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        assertThrows(ArithmeticException::class.java) {
            useCase(listOf(item))
        }
    }

    @Test
    fun `invoke throws ArithmeticException on total iuran individu overflow after multiplication`() {
        val maxInt = Int.MAX_VALUE
        val items = listOf(
            LegacyDataItemDto(
                first_name = "A", last_name = "B", email = "a@b.com", alamat = "x",
                iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = maxInt / 3, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            LegacyDataItemDto(
                first_name = "C", last_name = "D", email = "c@d.com", alamat = "y",
                iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = maxInt / 3, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        assertThrows(ArithmeticException::class.java) {
            useCase(items)
        }
    }

    @Test
    fun `invoke throws ArithmeticException on rekap iuran underflow`() {
        val maxInt = Int.MAX_VALUE
        val item = LegacyDataItemDto(
            first_name = "Underflow",
            last_name = "Test",
            email = "underflow@example.com",
            alamat = "Underflow St",
            iuran_perwarga = 0,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0,
            pengeluaran_iuran_warga = maxInt,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        assertThrows(ArithmeticException::class.java) {
            useCase(listOf(item))
        }
    }

    @Test
    fun `invoke validates all items before calculation`() {
        val items = listOf(
            LegacyDataItemDto(
                first_name = "Valid", last_name = "Item", email = "valid@example.com", alamat = "x",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            LegacyDataItemDto(
                first_name = "Invalid", last_name = "Item", email = "invalid@example.com", alamat = "y",
                iuran_perwarga = -1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            useCase(items)
        }
    }

    @Test
    fun `invoke handles boundary values correctly`() {
        val item = LegacyDataItemDto(
            first_name = "Boundary",
            last_name = "Test",
            email = "boundary@example.com",
            alamat = "Boundary St",
            iuran_perwarga = Int.MAX_VALUE / 2,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = Int.MAX_VALUE / 3,
            pengeluaran_iuran_warga = Int.MAX_VALUE / 2,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))
        
        assertEquals(Int.MAX_VALUE / 2, result.totalIuranBulanan)
        assertEquals(Int.MAX_VALUE / 2, result.totalPengeluaran)
        assertEquals(Int.MAX_VALUE / 3 * 3, result.totalIuranIndividu)
    }

    @Test
    fun `invoke returns immutable FinancialTotals result`() {
        val item = LegacyDataItemDto(
            first_name = "Immutability",
            last_name = "Test",
            email = "immutability@example.com",
            alamat = "Test St",
            iuran_perwarga = 100000,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000,
            pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val result = useCase(listOf(item))

        assertTrue(result is CalculateFinancialTotalsUseCase.FinancialTotals)
        assertEquals(4, result.javaClass.declaredFields.size)
    }
}
