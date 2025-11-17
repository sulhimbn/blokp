package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.ResponseUser
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for financial calculation logic in LaporanActivity
 */
class LaporanCalculationTest {

    @Test
    fun testTotalIuranIndividuAccumulation() {
        // Create test data with multiple items to verify accumulation
        val testData = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Example 1",
                iuran_perwarga = 100000,
                total_iuran_rekap = 300000,
                jumlah_iuran_bulanan = 100000,
                total_iuran_individu = 50000, // Will be multiplied by 3: 150000
                pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "Maintenance",
                avatar = "avatar1.jpg"
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Smith",
                email = "jane@example.com",
                alamat = "Jl. Example 2",
                iuran_perwarga = 120000,
                total_iuran_rekap = 360000,
                jumlah_iuran_bulanan = 120000,
                total_iuran_individu = 60000, // Will be multiplied by 3: 180000
                pengeluaran_iuran_warga = 25000,
                pemanfaatan_iuran = "Renovation",
                avatar = "avatar2.jpg"
            ),
            DataItem(
                first_name = "Bob",
                last_name = "Johnson",
                email = "bob@example.com",
                alamat = "Jl. Example 3",
                iuran_perwarga = 80000,
                total_iuran_rekap = 240000,
                jumlah_iuran_bulanan = 80000,
                total_iuran_individu = 40000, // Will be multiplied by 3: 120000
                pengeluaran_iuran_warga = 15000,
                pemanfaatan_iuran = "Utilities",
                avatar = "avatar3.jpg"
            )
        )

        val response = ResponseUser(testData)

        // Simulate the calculation logic from LaporanActivity
        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in response.data) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3  // This is the critical calculation
        }

        var rekapIuran = totalIuranIndividu - totalPengeluaran

        // Verify the accumulation is correct
        assertEquals(300000, totalIuranBulanan) // 100000 + 120000 + 80000
        assertEquals(60000, totalPengeluaran)   // 20000 + 25000 + 15000
        assertEquals(450000, totalIuranIndividu) // (50000*3) + (60000*3) + (40000*3) = 150000 + 180000 + 120000
        assertEquals(390000, rekapIuran)        // 450000 - 60000

        // Most importantly, verify that totalIuranIndividu is an accumulation of ALL items,
        // not just the last item (which would be 40000 * 3 = 120000)
        assertNotEquals(120000, totalIuranIndividu) // This would be the bug: only last item value
    }

    @Test
    fun testSingleItemCalculation() {
        // Test with just one item
        val testData = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Example 1",
                iuran_perwarga = 100000,
                total_iuran_rekap = 300000,
                jumlah_iuran_bulanan = 100000,
                total_iuran_individu = 50000,
                pengeluaran_iuran_warga = 20000,
                pemanfaatan_iuran = "Maintenance",
                avatar = "avatar1.jpg"
            )
        )

        val response = ResponseUser(testData)

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in response.data) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        var rekapIuran = totalIuranIndividu - totalPengeluaran

        assertEquals(100000, totalIuranBulanan)
        assertEquals(20000, totalPengeluaran)
        assertEquals(150000, totalIuranIndividu) // 50000 * 3
        assertEquals(130000, rekapIuran)         // 150000 - 20000
    }

    @Test
    fun testEmptyDataCalculation() {
        // Test with empty data
        val response = ResponseUser(emptyList())

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in response.data) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        var rekapIuran = totalIuranIndividu - totalPengeluaran

        assertEquals(0, totalIuranBulanan)
        assertEquals(0, totalPengeluaran)
        assertEquals(0, totalIuranIndividu)
        assertEquals(0, rekapIuran)
    }
}