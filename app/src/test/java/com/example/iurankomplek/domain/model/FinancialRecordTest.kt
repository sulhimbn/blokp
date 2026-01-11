package com.example.iurankomplek.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FinancialRecordTest {

    @Test
    fun financialRecord_withValidData_isCreatedSuccessfully() {
        val record = FinancialRecord(
            id = 1L,
            userId = 100L,
            iuranPerwarga = 50000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 150000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Pembayaran listrik dan air bersih"
        )

        assertEquals(1L, record.id)
        assertEquals(100L, record.userId)
        assertEquals(50000, record.iuranPerwarga)
        assertEquals(100000, record.jumlahIuranBulanan)
        assertEquals(300000, record.totalIuranIndividu)
        assertEquals(150000, record.pengeluaranIuranWarga)
        assertEquals(450000, record.totalIuranRekap)
        assertEquals("Pembayaran listrik dan air bersih", record.pemanfaatanIuran)
    }

    @Test
    fun financialRecord_withZeroValues_isCreatedSuccessfully() {
        val record = FinancialRecord(
            id = 1L,
            userId = 100L,
            iuranPerwarga = 0,
            jumlahIuranBulanan = 0,
            totalIuranIndividu = 0,
            pengeluaranIuranWarga = 0,
            totalIuranRekap = 0,
            pemanfaatanIuran = "No usage this month"
        )

        assertEquals(0, record.iuranPerwarga)
        assertEquals(0, record.jumlahIuranBulanan)
        assertEquals(0, record.totalIuranIndividu)
        assertEquals(0, record.pengeluaranIuranWarga)
        assertEquals(0, record.totalIuranRekap)
    }

    @Test
    fun financialRecord_withNegativeUserId_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = -1L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("User ID must be positive"))
    }

    @Test
    fun financialRecord_withZeroUserId_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 0L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("User ID must be positive"))
    }

    @Test
    fun financialRecord_withNegativeIuranPerwarga_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = -50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("Iuran perwarga cannot be negative"))
    }

    @Test
    fun financialRecord_withNegativeJumlahIuranBulanan_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = -100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("Jumlah iuran bulanan cannot be negative"))
    }

    @Test
    fun financialRecord_withNegativeTotalIuranIndividu_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = -300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("Total iuran individu cannot be negative"))
    }

    @Test
    fun financialRecord_withNegativePengeluaranIuranWarga_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = -150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("Pengeluaran iuran warga cannot be negative"))
    }

    @Test
    fun financialRecord_withNegativeTotalIuranRekap_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = -450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("Total iuran rekap cannot be negative"))
    }

    @Test
    fun financialRecord_withIuranPerwargaExceedingMaxValue_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 1000000000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("Iuran perwarga exceeds max value"))
    }

    @Test
    fun financialRecord_withJumlahIuranBulananExceedingMaxValue_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 1000000000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("Jumlah iuran bulanan exceeds max value"))
    }

    @Test
    fun financialRecord_withTotalIuranIndividuExceedingMaxValue_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 1000000000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("Total iuran individu exceeds max value"))
    }

    @Test
    fun financialRecord_withPengeluaranIuranWargaExceedingMaxValue_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 1000000000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("Pengeluaran iuran warga exceeds max value"))
    }

    @Test
    fun financialRecord_withTotalIuranRekapExceedingMaxValue_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 1000000000,
                pemanfaatanIuran = "Pembayaran listrik"
            )
        }

        assertTrue(exception.message!!.contains("Total iuran rekap exceeds max value"))
    }

    @Test
    fun financialRecord_withIuranPerwargaAtMaxValue_isCreatedSuccessfully() {
        val record = FinancialRecord(
            id = 1L,
            userId = 100L,
            iuranPerwarga = 999999999,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 150000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Pembayaran listrik"
        )

        assertEquals(999999999, record.iuranPerwarga)
    }

    @Test
    fun financialRecord_withBlankPemanfaatanIuran_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = "   "
            )
        }

        assertTrue(exception.message!!.contains("Pemanfaatan iuran cannot be blank"))
    }

    @Test
    fun financialRecord_withEmptyPemanfaatanIuran_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = ""
            )
        }

        assertTrue(exception.message!!.contains("Pemanfaatan iuran cannot be blank"))
    }

    @Test
    fun financialRecord_withPemanfaatanIuranExceedingMaxLength_throwsIllegalArgumentException() {
        val longDescription = "a".repeat(501)

        val exception = assertFailsWith<IllegalArgumentException> {
            FinancialRecord(
                id = 1L,
                userId = 100L,
                iuranPerwarga = 50000,
                jumlahIuranBulanan = 100000,
                totalIuranIndividu = 300000,
                pengeluaranIuranWarga = 150000,
                totalIuranRekap = 450000,
                pemanfaatanIuran = longDescription
            )
        }

        assertTrue(exception.message!!.contains("Pemanfaatan iuran too long"))
    }

    @Test
    fun financialRecord_withPemanfaatanIuranAtMaxLength_isCreatedSuccessfully() {
        val description = "a".repeat(500)

        val record = FinancialRecord(
            id = 1L,
            userId = 100L,
            iuranPerwarga = 50000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 150000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = description
        )

        assertEquals(description, record.pemanfaatanIuran)
    }

    @Test
    fun fromEntity_createsFinancialRecordFromParameters() {
        val record = FinancialRecord.fromEntity(
            id = 1L,
            userId = 100L,
            iuranPerwarga = 50000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 150000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Pembayaran listrik dan air bersih"
        )

        assertEquals(1L, record.id)
        assertEquals(100L, record.userId)
        assertEquals(50000, record.iuranPerwarga)
        assertEquals(100000, record.jumlahIuranBulanan)
        assertEquals(300000, record.totalIuranIndividu)
        assertEquals(150000, record.pengeluaranIuranWarga)
        assertEquals(450000, record.totalIuranRekap)
        assertEquals("Pembayaran listrik dan air bersih", record.pemanfaatanIuran)
    }

    @Test
    fun financialRecord_withZeroId_isCreatedSuccessfully() {
        val record = FinancialRecord(
            id = 0L,
            userId = 100L,
            iuranPerwarga = 50000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 150000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Pembayaran listrik"
        )

        assertEquals(0L, record.id)
    }

    @Test
    fun financialRecord_withSpecialCharactersInPemanfaatanIuran_isCreatedSuccessfully() {
        val record = FinancialRecord(
            id = 1L,
            userId = 100L,
            iuranPerwarga = 50000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 150000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Pembayaran listrik Rp. 500.000, air bersih Rp. 300.000"
        )

        assertEquals("Pembayaran listrik Rp. 500.000, air bersih Rp. 300.000", record.pemanfaatanIuran)
    }

    @Test
    fun financialRecord_withLargeValidNumericValue_isCreatedSuccessfully() {
        val record = FinancialRecord(
            id = 1L,
            userId = 100L,
            iuranPerwarga = 999999998,
            jumlahIuranBulanan = 999999998,
            totalIuranIndividu = 999999998,
            pengeluaranIuranWarga = 999999998,
            totalIuranRekap = 999999998,
            pemanfaatanIuran = "Large value test"
        )

        assertEquals(999999998, record.iuranPerwarga)
        assertEquals(999999998, record.jumlahIuranBulanan)
        assertEquals(999999998, record.totalIuranIndividu)
        assertEquals(999999998, record.pengeluaranIuranWarga)
        assertEquals(999999998, record.totalIuranRekap)
    }

    @Test
    fun financialRecord_withMinimumPositiveUserId_isCreatedSuccessfully() {
        val record = FinancialRecord(
            id = 1L,
            userId = 1L,
            iuranPerwarga = 50000,
            jumlahIuranBulanan = 100000,
            totalIuranIndividu = 300000,
            pengeluaranIuranWarga = 150000,
            totalIuranRekap = 450000,
            pemanfaatanIuran = "Pembayaran listrik"
        )

        assertEquals(1L, record.userId)
    }

    @Test
    fun financialRecord_withDefaultValues_isCreatedSuccessfully() {
        val record = FinancialRecord(
            id = 1L,
            userId = 100L,
            pemanfaatanIuran = "Test"
        )

        assertEquals(0, record.iuranPerwarga)
        assertEquals(0, record.jumlahIuranBulanan)
        assertEquals(0, record.totalIuranIndividu)
        assertEquals(0, record.pengeluaranIuranWarga)
        assertEquals(0, record.totalIuranRekap)
    }
}
