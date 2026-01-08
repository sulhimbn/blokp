package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.model.DataItem
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculateFinancialSummaryUseCaseTest {

    private lateinit var useCase: CalculateFinancialSummaryUseCase

    @Before
    fun setup() {
        useCase = CalculateFinancialSummaryUseCase()
    }

    @Test
    fun `invoke returns summary with zeros for empty list`() {
        val result = useCase(emptyList())
        
        assertEquals(0, result.totalIuranBulanan)
        assertEquals(0, result.totalPengeluaran)
        assertEquals(0, result.rekapIuran)
        assertTrue(result.isValid)
        assertNull(result.validationError)
    }

    @Test
    fun `invoke returns correct summary for single valid item`() {
        val item = DataItem(
            first_name = "John", last_name = "Doe", email = "john@example.com", alamat = "123 Main St",
            iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Maintenance", avatar = "https://example.com/avatar.jpg"
        )
        
        val result = useCase(listOf(item))
        
        assertEquals(100000, result.totalIuranBulanan)
        assertEquals(20000, result.totalPengeluaran)
        assertEquals(130000, result.rekapIuran) // 50000 * 3 - 20000 = 130000
        assertTrue(result.isValid)
        assertNull(result.validationError)
    }

    @Test
    fun `invoke returns correct summary for multiple items`() {
        val items = listOf(
            DataItem(
                first_name = "John", last_name = "Doe", email = "john@example.com", alamat = "123 Main St",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "Maintenance", avatar = "https://example.com/avatar1.jpg"
            ),
            DataItem(
                first_name = "Jane", last_name = "Smith", email = "jane@example.com", alamat = "456 Oak Ave",
                iuran_perwarga = 150000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 75000, pengeluaran_iuran_warga = 30000,
                pemanfaatan_iuran = "Cleaning", avatar = "https://example.com/avatar2.jpg"
            )
        )
        
        val result = useCase(items)
        
        assertEquals(250000, result.totalIuranBulanan) // 100000 + 150000
        assertEquals(50000, result.totalPengeluaran) // 20000 + 30000
        assertEquals(295000, result.rekapIuran) // (50000*3-20000) + (75000*3-30000) = 130000 + 165000 = 295000
        assertTrue(result.isValid)
        assertNull(result.validationError)
    }

    @Test
    fun `invoke returns invalid summary when one item has negative iuran_perwarga`() {
        val items = listOf(
            DataItem(
                first_name = "Valid", last_name = "User", email = "valid@example.com", alamat = "x",
                iuran_perwarga = 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            DataItem(
                first_name = "Invalid", last_name = "User", email = "invalid@example.com", alamat = "y",
                iuran_perwarga = -1, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000, pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )
        
        val result = useCase(items)
        
        assertEquals(0, result.totalIuranBulanan)
        assertEquals(0, result.totalPengeluaran)
        assertEquals(0, result.rekapIuran)
        assertFalse(result.isValid)
        assertNotNull(result.validationError)
        assertEquals("Invalid financial data detected", result.validationError)
    }

    @Test
    fun `invoke returns invalid summary when calculations would overflow`() {
        val maxInt = Int.MAX_VALUE
        val items = listOf(
            DataItem(
                first_name = "Overflow", last_name = "User", email = "overflow@example.com", alamat = "x",
                iuran_perwarga = maxInt - 100000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "x", avatar = "x"
            ),
            DataItem(
                first_name = "Overflow2", last_name = "User", email = "overflow2@example.com", alamat = "y",
                iuran_perwarga = 200000, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
                total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "y", avatar = "y"
            )
        )
        
        val result = useCase(items)
        
        assertEquals(0, result.totalIuranBulanan)
        assertEquals(0, result.totalPengeluaran)
        assertEquals(0, result.rekapIuran)
        assertFalse(result.isValid)
        assertNotNull(result.validationError)
        assertEquals("Financial calculation would cause overflow", result.validationError)
    }

    @Test
    fun `invoke handles arithmetic exception gracefully`() {
        val item = DataItem(
            first_name = "Test", last_name = "User", email = "test@example.com", alamat = "Test St",
            iuran_perwarga = Int.MAX_VALUE / 2, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = Int.MAX_VALUE / 3, pengeluaran_iuran_warga = Int.MAX_VALUE / 2,
            pemanfaatan_iuran = "Test", avatar = "https://example.com/avatar.jpg"
        )
        
        val result = useCase(listOf(item))
        
        assertTrue(result.isValid)
        assertNull(result.validationError)
    }

    @Test
    fun `invoke returns valid summary for boundary values`() {
        val item = DataItem(
            first_name = "Boundary", last_name = "Test", email = "boundary@example.com", alamat = "Boundary St",
            iuran_perwarga = Int.MAX_VALUE / 2, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = Int.MAX_VALUE / 3, pengeluaran_iuran_warga = Int.MAX_VALUE / 2,
            pemanfaatan_iuran = "Test", avatar = "https://example.com/avatar.jpg"
        )
        
        val result = useCase(listOf(item))
        
        assertTrue(result.isValid)
        assertNull(result.validationError)
        assertEquals(Int.MAX_VALUE / 2, result.totalIuranBulanan)
        assertEquals(Int.MAX_VALUE / 2, result.totalPengeluaran)
    }

    @Test
    fun `invoke accepts all zero values`() {
        val item = DataItem(
            first_name = "Zero", last_name = "User", email = "zero@example.com", alamat = "Zero St",
            iuran_perwarga = 0, total_iuran_rekap = 0, jumlah_iuran_bulanan = 1,
            total_iuran_individu = 0, pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Zero", avatar = "https://example.com/avatar.jpg"
        )
        
        val result = useCase(listOf(item))
        
        assertEquals(0, result.totalIuranBulanan)
        assertEquals(0, result.totalPengeluaran)
        assertEquals(0, result.rekapIuran)
        assertTrue(result.isValid)
        assertNull(result.validationError)
    }
}
