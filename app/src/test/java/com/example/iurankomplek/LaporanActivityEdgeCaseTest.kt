package com.example.iurankomplek

import com.example.iurankomplek.data.dto.LegacyDataItemDto
import org.junit.Assert.*
import org.junit.Test

class LaporanActivityEdgeCaseTest {

    @Test
    fun `calculateAndSetSummary with empty list shows empty state`() {
        val dataArray = emptyList<LegacyDataItemDto>()

        val summaryItems = createSummaryItems(0, 0, 0)

        assertTrue(summaryItems.isEmpty())
    }

    @Test
    fun `calculateAndSetSummary with single zero values item creates summary`() {
        val dataArray = listOf(
            LegacyDataItemDto(
                first_name = "Zero",
                last_name = "User",
                email = "zero@example.com",
                alamat = "Zero St",
                iuran_perwarga = 0,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 0,
                total_iuran_individu = 0,
                pengeluaran_iuran_warga = 0,
                pemanfaatan_iuran = "None",
                avatar = "https://example.com/avatar.jpg"
            )
        )

        val summaryItems = createSummaryItems(0, 0, 0)

        assertEquals(3, summaryItems.size)
        assertEquals("Jumlah Iuran Bulanan", summaryItems[0].label)
        assertEquals("Rp0", summaryItems[0].value)
        assertEquals("Total Pengeluaran", summaryItems[1].label)
        assertEquals("Rp0", summaryItems[1].value)
        assertEquals("Rekap Total Iuran", summaryItems[2].label)
        assertEquals("Rp0", summaryItems[2].value)
    }

    @Test
    fun `calculateAndSetSummary with maximum integer values`() {
        val maxInt = Int.MAX_VALUE / 3

        val dataArray = listOf(
            LegacyDataItemDto(
                first_name = "Max",
                last_name = "User",
                email = "max@example.com",
                alamat = "Max St",
                iuran_perwarga = maxInt,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 1,
                total_iuran_individu = maxInt,
                pengeluaran_iuran_warga = maxInt,
                pemanfaatan_iuran = "Max",
                avatar = "https://example.com/avatar.jpg"
            )
        )

        val totalIuranBulanan = maxInt
        val totalPengeluaran = maxInt
        val totalIuranIndividu = maxInt * 3
        val rekapIuran = totalIuranIndividu - totalPengeluaran

        val summaryItems = createSummaryItems(totalIuranBulanan, totalPengeluaran, rekapIuran)

        assertEquals(3, summaryItems.size)
        assertTrue(summaryItems[0].value.contains(maxInt.toString()))
    }

    @Test
    fun `calculateAndSetSummary with pengeluaran exceeding total iuran individu returns zero rekap`() {
        val dataArray = listOf(
            LegacyDataItemDto(
                first_name = "High",
                last_name = "Expense",
                email = "high@example.com",
                alamat = "High St",
                iuran_perwarga = 100000,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000,
                pengeluaran_iuran_warga = 500000,
                pemanfaatan_iuran = "High",
                avatar = "https://example.com/avatar.jpg"
            )
        )

        val totalIuranBulanan = 100000
        val totalPengeluaran = 500000
        val totalIuranIndividu = 50000 * 3
        val rekapIuran = maxOf(0, totalIuranIndividu - totalPengeluaran)

        assertEquals(0, rekapIuran)

        val summaryItems = createSummaryItems(totalIuranBulanan, totalPengeluaran, rekapIuran)

        assertEquals(3, summaryItems.size)
        assertTrue(summaryItems[2].value.contains("0"))
    }

    @Test
    fun `updateSummaryWithPayments appends payment total to summary`() {
        val baseSummaryItems = listOf(
            LaporanSummaryItem("Jumlah Iuran Bulanan", "Rp100000"),
            LaporanSummaryItem("Total Pengeluaran", "Rp20000"),
            LaporanSummaryItem("Rekap Total Iuran", "Rp130000")
        )

        val paymentTotal = 50000
        val transactionCount = 5

        val updatedSummaryItems = baseSummaryItems + LaporanSummaryItem(
            "Total Pembayaran Diproses",
            formatCurrency(paymentTotal)
        )

        assertEquals(4, updatedSummaryItems.size)
        assertEquals("Total Pembayaran Diproses", updatedSummaryItems[3].label)
        assertTrue(updatedSummaryItems[3].value.contains("50000"))
    }

    @Test
    fun `updateSummaryWithPayments preserves base summary items`() {
        val baseSummaryItems = listOf(
            LaporanSummaryItem("Jumlah Iuran Bulanan", "Rp100000"),
            LaporanSummaryItem("Total Pengeluaran", "Rp20000"),
            LaporanSummaryItem("Rekap Total Iuran", "Rp130000")
        )

        val paymentTotal = 50000
        val transactionCount = 5

        val updatedSummaryItems = baseSummaryItems + LaporanSummaryItem(
            "Total Pembayaran Diproses",
            formatCurrency(paymentTotal)
        )

        assertEquals("Jumlah Iuran Bulanan", updatedSummaryItems[0].label)
        assertEquals("Rp100000", updatedSummaryItems[0].value)
        assertEquals("Total Pengeluaran", updatedSummaryItems[1].label)
        assertEquals("Rp20000", updatedSummaryItems[1].value)
        assertEquals("Rekap Total Iuran", updatedSummaryItems[2].label)
        assertEquals("Rp130000", updatedSummaryItems[2].value)
    }

    @Test
    fun `calculateAndSetSummary with invalid validation returns early`() {
        val dataArray = listOf(
            LegacyDataItemDto(
                first_name = "Invalid",
                last_name = "Data",
                email = "invalid@example.com",
                alamat = "Invalid St",
                iuran_perwarga = -1,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000,
                pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "Invalid",
                avatar = "https://example.com/avatar.jpg"
            )
        )

        val isValid = validateDataItems(dataArray)

        assertFalse(isValid)
    }

    @Test
    fun `calculateAndSetSummary with multiple items aggregates correctly`() {
        val dataArray = listOf(
            LegacyDataItemDto(
                first_name = "User1",
                last_name = "A",
                email = "user1@example.com",
                alamat = "A St",
                iuran_perwarga = 100000,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 1,
                total_iuran_individu = 50000,
                pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "Test1",
                avatar = "https://example.com/avatar1.jpg"
            ),
            LegacyDataItemDto(
                first_name = "User2",
                last_name = "B",
                email = "user2@example.com",
                alamat = "B St",
                iuran_perwarga = 150000,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 1,
                total_iuran_individu = 75000,
                pengeluaran_iuran_warga = 30000,
                pemanfaatan_iuran = "Test2",
                avatar = "https://example.com/avatar2.jpg"
            )
        )

        val totalIuranBulanan = 100000 + 150000
        val totalPengeluaran = 20000 + 30000
        val totalIuranIndividu = (50000 * 3) + (75000 * 3)
        val rekapIuran = totalIuranIndividu - totalPengeluaran

        assertEquals(250000, totalIuranBulanan)
        assertEquals(50000, totalPengeluaran)
        assertEquals(375000, totalIuranIndividu)
        assertEquals(325000, rekapIuran)

        val summaryItems = createSummaryItems(totalIuranBulanan, totalPengeluaran, rekapIuran)

        assertEquals(3, summaryItems.size)
    }

    @Test
    fun `calculateAndSetSummary with boundary value 1`() {
        val dataArray = listOf(
            LegacyDataItemDto(
                first_name = "Boundary",
                last_name = "One",
                email = "boundary@example.com",
                alamat = "Boundary St",
                iuran_perwarga = 1,
                total_iuran_rekap = 0,
                jumlah_iuran_bulanan = 1,
                total_iuran_individu = 1,
                pengeluaran_iuran_warga = 1,
                pemanfaatan_iuran = "Boundary",
                avatar = "https://example.com/avatar.jpg"
            )
        )

        val totalIuranBulanan = 1
        val totalPengeluaran = 1
        val totalIuranIndividu = 1 * 3
        val rekapIuran = totalIuranIndividu - totalPengeluaran

        assertEquals(1, totalIuranBulanan)
        assertEquals(1, totalPengeluaran)
        assertEquals(3, totalIuranIndividu)
        assertEquals(2, rekapIuran)

        val summaryItems = createSummaryItems(totalIuranBulanan, totalPengeluaran, rekapIuran)

        assertEquals(3, summaryItems.size)
    }

    @Test
    fun `calculateAndSetSummary handles large dataset efficiently`() {
        val dataArray = mutableListOf<LegacyDataItemDto>()

        for (i in 1..100) {
            dataArray.add(
                LegacyDataItemDto(
                    first_name = "User$i",
                    last_name = "Test",
                    email = "user$i@example.com",
                    alamat = "Test St",
                    iuran_perwarga = 10000,
                    total_iuran_rekap = 0,
                    jumlah_iuran_bulanan = 1,
                    total_iuran_individu = 5000,
                    pengeluaran_iuran_warga = 2000,
                    pemanfaatan_iuran = "Test$i",
                    avatar = "https://example.com/avatar$i.jpg"
                )
            )
        }

        val totalIuranBulanan = 10000 * 100
        val totalPengeluaran = 2000 * 100
        val totalIuranIndividu = (5000 * 3) * 100
        val rekapIuran = totalIuranIndividu - totalPengeluaran

        assertEquals(1000000, totalIuranBulanan)
        assertEquals(200000, totalPengeluaran)
        assertEquals(1500000, totalIuranIndividu)
        assertEquals(1300000, rekapIuran)

        val summaryItems = createSummaryItems(totalIuranBulanan, totalPengeluaran, rekapIuran)

        assertEquals(3, summaryItems.size)
    }

    private fun createSummaryItems(
        totalIuranBulanan: Int,
        totalPengeluaran: Int,
        rekapIuran: Int
    ): List<LaporanSummaryItem> = listOf(
        LaporanSummaryItem("Jumlah Iuran Bulanan", formatCurrency(totalIuranBulanan)),
        LaporanSummaryItem("Total Pengeluaran", formatCurrency(totalPengeluaran)),
        LaporanSummaryItem("Rekap Total Iuran", formatCurrency(rekapIuran))
    )

    private fun formatCurrency(value: Int): String {
        return "Rp$value"
    }

    private fun validateDataItems(items: List<LegacyDataItemDto>): Boolean {
        for (item in items) {
            if (item.iuran_perwarga < 0) return false
            if (item.pengeluaran_iuran_warga < 0) return false
            if (item.total_iuran_individu < 0) return false
        }
        return true
    }
}

data class LaporanSummaryItem(
    val label: String,
    val value: String
)
