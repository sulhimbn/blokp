package com.example.iurankomplek.data.entity

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class FinancialRecordEntityTest {

    @Test
    fun `create financial record with valid data should succeed`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )

        assertEquals(1L, record.id)
        assertEquals(1L, record.userId)
        assertEquals(100, record.iuranPerwarga)
        assertEquals(200, record.jumlahIuranBulanan)
        assertEquals(300, record.totalIuranIndividu)
        assertEquals(400, record.pengeluaranIuranWarga)
        assertEquals(500, record.totalIuranRekap)
        assertEquals("Maintenance", record.pemanfaatanIuran)
    }

    @Test
    fun `create financial record with default dates should use current time`() {
        val beforeCreate = Date()
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
        val afterCreate = Date()

        assertNotNull(record.createdAt)
        assertNotNull(record.updatedAt)
        assertTrue(record.createdAt.time >= beforeCreate.time)
        assertTrue(record.createdAt.time <= afterCreate.time)
        assertTrue(record.updatedAt.time >= beforeCreate.time)
        assertTrue(record.updatedAt.time <= afterCreate.time)
    }

    @Test
    fun `create financial record with custom dates should preserve them`() {
        val customDate = Date(1000000)
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance",
            createdAt = customDate,
            updatedAt = customDate
        )

        assertEquals(customDate, record.createdAt)
        assertEquals(customDate, record.updatedAt)
    }

    @Test
    fun `create financial record with zero values should succeed`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 0,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 0,
            pengeluaranIuranWarga = 0,
            totalIuranRekap = 0,
            pemanfaatanIuran = "Maintenance"
        )

        assertEquals(0, record.iuranPerwarga)
        assertEquals(0, record.jumlahIuranBulanan)
        assertEquals(0, record.totalIuranIndividu)
        assertEquals(0, record.pengeluaranIuranWarga)
        assertEquals(0, record.totalIuranRekap)
    }

    @Test
    fun `create financial record with max numeric values should succeed`() {
        val maxValue = 999999999
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = maxValue,
            jumlahIuranBulanan = maxValue,
            totalIuranIndividu = maxValue,
            pengeluaranIuranWarga = maxValue,
            totalIuranRekap = maxValue,
            pemanfaatanIuran = "Maintenance"
        )

        assertEquals(maxValue, record.iuranPerwarga)
        assertEquals(maxValue, record.jumlahIuranBulanan)
        assertEquals(maxValue, record.totalIuranIndividu)
        assertEquals(maxValue, record.pengeluaranIuranWarga)
        assertEquals(maxValue, record.totalIuranRekap)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with negative iuranPerwarga should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = -1,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with negative jumlahIuranBulanan should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = -1,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with negative totalIuranIndividu should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = -1,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with negative pengeluaranIuranWarga should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = -1,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with negative totalIuranRekap should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = -1,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with user id zero should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 0,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with negative user id should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = -1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with blank pemanfaatanIuran should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = ""
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with whitespace-only pemanfaatanIuran should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "   "
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with iuranPerwarga exceeding max value should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 1000000000,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with jumlahIuranBulanan exceeding max value should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 1000000000,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with totalIuranIndividu exceeding max value should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 1000000000,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with pengeluaranIuranWarga exceeding max value should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 1000000000,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with totalIuranRekap exceeding max value should throw exception`() {
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 1000000000,
            pemanfaatanIuran = "Maintenance"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create financial record with pemanfaatanIuran exceeding max length should throw exception`() {
        val longPemanfaatan = "a".repeat(501)
        FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = longPemanfaatan
        )
    }

    @Test
    fun `create financial record with pemanfaatanIuran at max length should succeed`() {
        val maxPemanfaatan = "a".repeat(500)
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = maxPemanfaatan
        )

        assertEquals(maxPemanfaatan, record.pemanfaatanIuran)
    }

    @Test
    fun `create financial record with default id should use 0`() {
        val record = FinancialRecordEntity(
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )

        assertEquals(0L, record.id)
    }

    @Test
    fun `create financial record with special characters in pemanfaatanIuran should succeed`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Pembayaran listrik & air bulan Januari - Maret 2024"
        )

        assertEquals("Pembayaran listrik & air bulan Januari - Maret 2024", record.pemanfaatanIuran)
    }

    @Test
    fun `create financial record with Unicode in pemanfaatanIuran should succeed`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Pemeliharaan taman dan fasilitas umum: pagar, lampu jalan, dll."
        )

        assertEquals("Pemeliharaan taman dan fasilitas umum: pagar, lampu jalan, dll.", record.pemanfaatanIuran)
    }

    @Test
    fun `create financial record with realistic values should succeed`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 50000,
            jumlahIuranBulanan = 150000,
            totalIuranIndividu = 150000,
            pengeluaranIuranWarga = 145000,
            totalIuranRekap = 150000,
            pemanfaatanIuran = "Pembayaran iuran bulanan"
        )

        assertEquals(50000, record.iuranPerwarga)
        assertEquals(150000, record.jumlahIuranBulanan)
        assertEquals(150000, record.totalIuranIndividu)
        assertEquals(145000, record.pengeluaranIuranWarga)
        assertEquals(150000, record.totalIuranRekap)
    }

    @Test
    fun `data class equality should work correctly`() {
        val record1 = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )

        val record2 = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )

        assertEquals(record1, record2)
        assertEquals(record1.hashCode(), record2.hashCode())
    }

    @Test
    fun `data class copy should work correctly`() {
        val record1 = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )

        val record2 = record1.copy(pemanfaatanIuran = "Repair")

        assertEquals("Maintenance", record1.pemanfaatanIuran)
        assertEquals("Repair", record2.pemanfaatanIuran)
        assertEquals(record1.userId, record2.userId)
    }

    @Test
    fun `create financial record with large numeric values should succeed`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 999999998,
            jumlahIuranBulanan = 999999998,
            totalIuranIndividu = 999999998,
            pengeluaranIuranWarga = 999999998,
            totalIuranRekap = 999999998,
            pemanfaatanIuran = "Maintenance"
        )

        assertEquals(999999998, record.iuranPerwarga)
        assertEquals(999999998, record.jumlahIuranBulanan)
        assertEquals(999999998, record.totalIuranIndividu)
        assertEquals(999999998, record.pengeluaranIuranWarga)
        assertEquals(999999998, record.totalIuranRekap)
    }

    @Test
    fun `create financial record with single character pemanfaatanIuran should succeed`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = 1,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "A"
        )

        assertEquals("A", record.pemanfaatanIuran)
    }

    @Test
    fun `create financial record with large user id should succeed`() {
        val record = FinancialRecordEntity(
            id = 1,
            userId = Long.MAX_VALUE,
            iuranPerwarga = 100,
            jumlahIuranBulanan = 200,
            totalIuranIndividu = 300,
            pengeluaranIuranWarga = 400,
            totalIuranRekap = 500,
            pemanfaatanIuran = "Maintenance"
        )

        assertEquals(Long.MAX_VALUE, record.userId)
    }
}
