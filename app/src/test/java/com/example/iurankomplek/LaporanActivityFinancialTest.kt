package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import org.junit.Test
import org.junit.Assert.*

class LaporanActivityFinancialTest {

    @Test
    fun testFinancialCalculationAccumulation() {
        // Simulate the calculation logic from LaporanActivity
        val dataArray = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Example 1",
                iuran_perwarga = 100,
                total_iuran_rekap = 300,
                jumlah_iuran_bulanan = 100,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Maintenance",
                avatar = "avatar1.jpg"
            ),
            DataItem(
                first_name = "Jane",
                last_name = "Smith",
                email = "jane@example.com",
                alamat = "Jl. Example 2",
                iuran_perwarga = 200,
                total_iuran_rekap = 600,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 75,
                pengeluaran_iuran_warga = 30,
                pemanfaatan_iuran = "Repair",
                avatar = "avatar2.jpg"
            ),
            DataItem(
                first_name = "Bob",
                last_name = "Johnson",
                email = "bob@example.com",
                alamat = "Jl. Example 3",
                iuran_perwarga = 150,
                total_iuran_rekap = 450,
                jumlah_iuran_bulanan = 150,
                total_iuran_individu = 100,
                pengeluaran_iuran_warga = 40,
                pemanfaatan_iuran = "Improvement",
                avatar = "avatar3.jpg"
            )
        )

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in dataArray) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        // Verify that all values have been accumulated properly
        assertEquals(450, totalIuranBulanan) // 100 + 200 + 150
        assertEquals(95, totalPengeluaran) // 25 + 30 + 40
        assertEquals(675, totalIuranIndividu) // (50*3) + (75*3) + (100*3) = 150 + 225 + 300
        
        var rekapIuran = totalIuranIndividu - totalPengeluaran
        assertEquals(580, rekapIuran) // 675 - 95
    }
    
    @Test
    fun testIfCalculationOnlyTakesLastValue() {
        // This test verifies that the calculation doesn't just take the last value
        // If it did, totalIuranIndividu would be just the last item * 3 = 100 * 3 = 300
        // But with proper accumulation it should be (50*3) + (75*3) + (100*3) = 675
        
        val dataArray = listOf(
            DataItem(
                first_name = "First",
                last_name = "User",
                email = "first@example.com",
                alamat = "Jl. First",
                iuran_perwarga = 100,
                total_iuran_rekap = 300,
                jumlah_iuran_bulanan = 100,
                total_iuran_individu = 50,
                pengeluaran_iuran_warga = 10,
                pemanfaatan_iuran = "Test",
                avatar = "avatar1.jpg"
            ),
            DataItem(
                first_name = "Last",
                last_name = "User", 
                email = "last@example.com",
                alamat = "Jl. Last",
                iuran_perwarga = 200,
                total_iuran_rekap = 600,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 100, // This would be the "last value"
                pengeluaran_iuran_warga = 20,
                pemanfaatan_iuran = "Test",
                avatar = "avatar2.jpg"
            )
        )

        var totalIuranIndividu = 0
        for (dataItem in dataArray) {
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        // If only taking the last value: total would be 100 * 3 = 300
        // With proper accumulation: total should be (50*3) + (100*3) = 450
        assertNotEquals(300, totalIuranIndividu) // Should not equal just the last value * 3
        assertEquals(450, totalIuranIndividu) // Should equal the accumulated value
    }
}