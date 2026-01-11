package com.example.iurankomplek.domain.model

import com.example.iurankomplek.data.dto.LegacyDataItemDto
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FinancialItemTest {

    @Test
    fun financialItem_withDefaultValues_isCreatedSuccessfully() {
        val item = FinancialItem()

        assertEquals(0, item.iuranPerwarga)
        assertEquals(0, item.pengeluaranIuranWarga)
        assertEquals(0, item.totalIuranIndividu)
    }

    @Test
    fun financialItem_withValidData_isCreatedSuccessfully() {
        val item = FinancialItem(
            iuranPerwarga = 100000,
            pengeluaranIuranWarga = 20000,
            totalIuranIndividu = 50000
        )

        assertEquals(100000, item.iuranPerwarga)
        assertEquals(20000, item.pengeluaranIuranWarga)
        assertEquals(50000, item.totalIuranIndividu)
    }

    @Test
    fun financialItem_withZeroValues_isCreatedSuccessfully() {
        val item = FinancialItem(
            iuranPerwarga = 0,
            pengeluaranIuranWarga = 0,
            totalIuranIndividu = 0
        )

        assertEquals(0, item.iuranPerwarga)
        assertEquals(0, item.pengeluaranIuranWarga)
        assertEquals(0, item.totalIuranIndividu)
    }

    @Test
    fun financialItem_withNegativeIuranPerwarga_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialItem(
                iuranPerwarga = -1,
                pengeluaranIuranWarga = 0,
                totalIuranIndividu = 0
            )
        }

        assertEquals("iuranPerwarga cannot be negative", exception.message)
    }

    @Test
    fun financialItem_withNegativePengeluaranIuranWarga_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialItem(
                iuranPerwarga = 0,
                pengeluaranIuranWarga = -100,
                totalIuranIndividu = 0
            )
        }

        assertEquals("pengeluaranIuranWarga cannot be negative", exception.message)
    }

    @Test
    fun financialItem_withNegativeTotalIuranIndividu_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialItem(
                iuranPerwarga = 0,
                pengeluaranIuranWarga = 0,
                totalIuranIndividu = -500
            )
        }

        assertEquals("totalIuranIndividu cannot be negative", exception.message)
    }

    @Test
    fun financialItem_withMaxNumericValue_isCreatedSuccessfully() {
        val item = FinancialItem(
            iuranPerwarga = FinancialItem.MAX_NUMERIC_VALUE,
            pengeluaranIuranWarga = FinancialItem.MAX_NUMERIC_VALUE,
            totalIuranIndividu = FinancialItem.MAX_NUMERIC_VALUE
        )

        assertEquals(FinancialItem.MAX_NUMERIC_VALUE, item.iuranPerwarga)
        assertEquals(FinancialItem.MAX_NUMERIC_VALUE, item.pengeluaranIuranWarga)
        assertEquals(FinancialItem.MAX_NUMERIC_VALUE, item.totalIuranIndividu)
    }

    @Test
    fun financialItem_withMaxPlusOneIuranPerwarga_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialItem(
                iuranPerwarga = FinancialItem.MAX_NUMERIC_VALUE + 1,
                pengeluaranIuranWarga = 0,
                totalIuranIndividu = 0
            )
        }

        assertEquals("iuranPerwarga exceeds max value", exception.message)
    }

    @Test
    fun financialItem_withMaxPlusOnePengeluaranIuranWarga_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialItem(
                iuranPerwarga = 0,
                pengeluaranIuranWarga = FinancialItem.MAX_NUMERIC_VALUE + 1,
                totalIuranIndividu = 0
            )
        }

        assertEquals("pengeluaranIuranWarga exceeds max value", exception.message)
    }

    @Test
    fun financialItem_withMaxPlusOneTotalIuranIndividu_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialItem(
                iuranPerwarga = 0,
                pengeluaranIuranWarga = 0,
                totalIuranIndividu = FinancialItem.MAX_NUMERIC_VALUE + 1
            )
        }

        assertEquals("totalIuranIndividu exceeds max value", exception.message)
    }

    @Test
    fun fromLegacyDataItemDto_convertsSuccessfully() {
        val dto = LegacyDataItemDto(
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

        val item = FinancialItem.fromLegacyDataItemDto(dto)

        assertEquals(100000, item.iuranPerwarga)
        assertEquals(20000, item.pengeluaranIuranWarga)
        assertEquals(50000, item.totalIuranIndividu)
    }

    @Test
    fun fromLegacyDataItemDto_withZeroValues_convertsSuccessfully() {
        val dto = LegacyDataItemDto(
            first_name = "Jane",
            last_name = "Smith",
            email = "jane@example.com",
            alamat = "456 Oak Ave",
            iuran_perwarga = 0,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 0,
            total_iuran_individu = 0,
            pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "No usage",
            avatar = "https://example.com/avatar.jpg"
        )

        val item = FinancialItem.fromLegacyDataItemDto(dto)

        assertEquals(0, item.iuranPerwarga)
        assertEquals(0, item.pengeluaranIuranWarga)
        assertEquals(0, item.totalIuranIndividu)
    }

    @Test
    fun fromLegacyDataItemDto_withMaxValues_convertsSuccessfully() {
        val dto = LegacyDataItemDto(
            first_name = "Test",
            last_name = "User",
            email = "test@example.com",
            alamat = "Test Address",
            iuran_perwarga = FinancialItem.MAX_NUMERIC_VALUE,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = FinancialItem.MAX_NUMERIC_VALUE,
            pengeluaran_iuran_warga = FinancialItem.MAX_NUMERIC_VALUE,
            pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        )

        val item = FinancialItem.fromLegacyDataItemDto(dto)

        assertEquals(FinancialItem.MAX_NUMERIC_VALUE, item.iuranPerwarga)
        assertEquals(FinancialItem.MAX_NUMERIC_VALUE, item.pengeluaranIuranWarga)
        assertEquals(FinancialItem.MAX_NUMERIC_VALUE, item.totalIuranIndividu)
    }

    @Test
    fun fromLegacyDataItemDto_withNegativeIuranPerwarga_throwsIllegalArgumentException() {
        val dto = LegacyDataItemDto(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = -1,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = 1,
            total_iuran_individu = 50000,
            pengeluaran_iuran_warga = 20000,
            pemanfaatan_iuran = "Maintenance",
            avatar = "https://example.com/avatar.jpg"
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialItem.fromLegacyDataItemDto(dto)
        }

        assertEquals("iuranPerwarga cannot be negative", exception.message)
    }

    @Test
    fun fromLegacyDataItemDtoList_withEmptyList_returnsEmptyList() {
        val dtos = emptyList<LegacyDataItemDto>()

        val items = FinancialItem.fromLegacyDataItemDtoList(dtos)

        assertEquals(0, items.size)
    }

    @Test
    fun fromLegacyDataItemDtoList_withSingleItem_convertsSuccessfully() {
        val dtos = listOf(
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
                avatar = "https://example.com/avatar.jpg"
            )
        )

        val items = FinancialItem.fromLegacyDataItemDtoList(dtos)

        assertEquals(1, items.size)
        assertEquals(100000, items[0].iuranPerwarga)
        assertEquals(20000, items[0].pengeluaranIuranWarga)
        assertEquals(50000, items[0].totalIuranIndividu)
    }

    @Test
    fun fromLegacyDataItemDtoList_withMultipleItems_convertsAllSuccessfully() {
        val dtos = listOf(
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

        val items = FinancialItem.fromLegacyDataItemDtoList(dtos)

        assertEquals(2, items.size)
        assertEquals(100000, items[0].iuranPerwarga)
        assertEquals(20000, items[0].pengeluaranIuranWarga)
        assertEquals(50000, items[0].totalIuranIndividu)
        assertEquals(150000, items[1].iuranPerwarga)
        assertEquals(30000, items[1].pengeluaranIuranWarga)
        assertEquals(75000, items[1].totalIuranIndividu)
    }

    @Test
    fun fromLegacyDataItemDtoList_withInvalidData_throwsIllegalArgumentException() {
        val dtos = listOf(
            LegacyDataItemDto(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "123 Main St",
                iuran_perwarga = -100,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000,
                pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialItem.fromLegacyDataItemDtoList(dtos)
        }

        assertEquals("iuranPerwarga cannot be negative", exception.message)
    }

    @Test
    fun financialItem_withBoundaryValueOne_isCreatedSuccessfully() {
        val item = FinancialItem(
            iuranPerwarga = 1,
            pengeluaranIuranWarga = 1,
            totalIuranIndividu = 1
        )

        assertEquals(1, item.iuranPerwarga)
        assertEquals(1, item.pengeluaranIuranWarga)
        assertEquals(1, item.totalIuranIndividu)
    }

    @Test
    fun financialItem_withLargeValues_isCreatedSuccessfully() {
        val item = FinancialItem(
            iuranPerwarga = 100000000,
            pengeluaranIuranWarga = 50000000,
            totalIuranIndividu = 300000000
        )

        assertEquals(100000000, item.iuranPerwarga)
        assertEquals(50000000, item.pengeluaranIuranWarga)
        assertEquals(300000000, item.totalIuranIndividu)
    }

    @Test
    fun financialItem_dataClassEquality_worksCorrectly() {
        val item1 = FinancialItem(
            iuranPerwarga = 100000,
            pengeluaranIuranWarga = 20000,
            totalIuranIndividu = 50000
        )

        val item2 = FinancialItem(
            iuranPerwarga = 100000,
            pengeluaranIuranWarga = 20000,
            totalIuranIndividu = 50000
        )

        assertEquals(item1, item2)
        assertEquals(item1.hashCode(), item2.hashCode())
    }

    @Test
    fun financialItem_dataClassInequality_worksCorrectly() {
        val item1 = FinancialItem(
            iuranPerwarga = 100000,
            pengeluaranIuranWarga = 20000,
            totalIuranIndividu = 50000
        )

        val item2 = FinancialItem(
            iuranPerwarga = 150000,
            pengeluaranIuranWarga = 20000,
            totalIuranIndividu = 50000
        )

        assert(item1 != item2)
    }

    @Test
    fun financialItem_copy_worksCorrectly() {
        val original = FinancialItem(
            iuranPerwarga = 100000,
            pengeluaranIuranWarga = 20000,
            totalIuranIndividu = 50000
        )

        val copied = original.copy(iuranPerwarga = 150000)

        assertEquals(150000, copied.iuranPerwarga)
        assertEquals(20000, copied.pengeluaranIuranWarga)
        assertEquals(50000, copied.totalIuranIndividu)
        assertEquals(100000, original.iuranPerwarga)
    }
}
