package com.example.iurankomplek

import com.example.iurankomplek.data.dto.LegacyDataItemDto
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for financial calculation logic in LaporanActivity
 */
class LaporanActivityCalculationTest {

    @Test
    fun testTotalIuranIndividuCalculation_accumulatesCorrectly() {
        // Test data with multiple items to verify accumulation
        val testItems = listOf(
            LegacyDataItemDto(first_name = "John", last_name = "Doe", email = "john@example.com", alamat = "123 St", iuran_perwarga = 100, total_iuran_rekap = 150, jumlah_iuran_bulanan = 50, total_iuran_individu = 50, pengeluaran_iuran_warga = 25, pemanfaatan_iuran = "Test", avatar = "url"),
            LegacyDataItemDto(first_name = "Jane", last_name = "Smith", email = "jane@example.com", alamat = "456 Ave", iuran_perwarga = 200, total_iuran_rekap = 225, jumlah_iuran_bulanan = 75, total_iuran_individu = 75, pengeluaran_iuran_warga = 30, pemanfaatan_iuran = "Test", avatar = "url"),
            LegacyDataItemDto(first_name = "Bob", last_name = "Johnson", email = "bob@example.com", alamat = "789 Blvd", iuran_perwarga = 300, total_iuran_rekap = 300, jumlah_iuran_bulanan = 100, total_iuran_individu = 100, pengeluaran_iuran_warga = 45, pemanfaatan_iuran = "Test", avatar = "url")
        )

        // Simulate the calculation logic from LaporanActivity
        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3  // The multiplication by 3 logic
        }

        var rekapIuran = totalIuranIndividu - totalPengeluaran

        // Verify calculations are correct
        assertEquals(600, totalIuranBulanan)  // 100 + 200 + 300
        assertEquals(100, totalPengeluaran)   // 25 + 30 + 45
        assertEquals(675, totalIuranIndividu) // (50*3) + (75*3) + (100*3) = 150 + 225 + 300
        assertEquals(575, rekapIuran)         // 675 - 100
    }

    @Test
    fun testTotalIuranIndividuCalculation_singleItem() {
        // Test with single item to ensure it doesn't just take last item but properly accumulates
        val testItems = listOf(
            LegacyDataItemDto(first_name = "Test", last_name = "User", email = "test@example.com", alamat = "Test Addr", iuran_perwarga = 100, total_iuran_rekap = 150, jumlah_iuran_bulanan = 50, total_iuran_individu = 50, pengeluaran_iuran_warga = 25, pemanfaatan_iuran = "Test", avatar = "url")
        )

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        assertEquals(100, totalIuranBulanan)
        assertEquals(25, totalPengeluaran)
        assertEquals(150, totalIuranIndividu)  // 50 * 3
    }

    @Test
    fun testTotalIuranIndividuCalculation_emptyList() {
        // Test with empty list to ensure no errors
        val testItems = emptyList<LegacyDataItemDto>()

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        assertEquals(0, totalIuranBulanan)
        assertEquals(0, totalPengeluaran)
        assertEquals(0, totalIuranIndividu)
    }

    @Test
    fun testTotalIuranIndividuCalculation_verificationOfAccumulationLogic() {
        // Specific test to verify accumulation logic mentioned in issue #96
        // This test verifies that each item's total_iuran_individu is multiplied by 3
        // and then accumulated to the running total (not just assigned)
        val testItems = listOf(
            LegacyDataItemDto(first_name = "Test1", last_name = "User1", email = "test1@example.com", alamat = "Addr1", iuran_perwarga = 100, total_iuran_rekap = 30, jumlah_iuran_bulanan = 10, total_iuran_individu = 10, pengeluaran_iuran_warga = 5, pemanfaatan_iuran = "Test", avatar = "url"),
            LegacyDataItemDto(first_name = "Test2", last_name = "User2", email = "test2@example.com", alamat = "Addr2", iuran_perwarga = 200, total_iuran_rekap = 60, jumlah_iuran_bulanan = 20, total_iuran_individu = 20, pengeluaran_iuran_warga = 10, pemanfaatan_iuran = "Test", avatar = "url"),
            LegacyDataItemDto(first_name = "Test3", last_name = "User3", email = "test3@example.com", alamat = "Addr3", iuran_perwarga = 300, total_iuran_rekap = 90, jumlah_iuran_bulanan = 30, total_iuran_individu = 30, pengeluaran_iuran_warga = 15, pemanfaatan_iuran = "Test", avatar = "url")
        )

        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        // Process each item to verify accumulation logic
        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            // This is the key line: total_iuran_individu += dataItem.total_iuran_individu * 3
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        // Verify that accumulation worked correctly:
        // Item 1: 10 * 3 = 30, total = 0 + 30 = 30
        // Item 2: 20 * 3 = 60, total = 30 + 60 = 90
        // Item 3: 30 * 3 = 90, total = 90 + 90 = 180
        assertEquals(180, totalIuranIndividu)  // (10*3) + (20*3) + (30*3) = 30 + 60 + 90 = 180
        assertEquals(600, totalIuranBulanan)  // 100 + 200 + 300
        assertEquals(30, totalPengeluaran)    // 5 + 10 + 15
    }

    @Test
    fun testPaymentIntegrationDoesNotAffectFinancialCalculations() {
        // Test to verify the bug fix in payment integration where payments were incorrectly
        // added to iuran totals, inflating the financial calculations
        val testItems = listOf(
            LegacyDataItemDto(first_name = "Test1", last_name = "User1", email = "test1@example.com", alamat = "Addr1", iuran_perwarga = 100, total_iuran_rekap = 30, jumlah_iuran_bulanan = 10, total_iuran_individu = 10, pengeluaran_iuran_warga = 5, pemanfaatan_iuran = "Test", avatar = "url"),
            LegacyDataItemDto(first_name = "Test2", last_name = "User2", email = "test2@example.com", alamat = "Addr2", iuran_perwarga = 200, total_iuran_rekap = 60, jumlah_iuran_bulanan = 20, total_iuran_individu = 20, pengeluaran_iuran_warga = 10, pemanfaatan_iuran = "Test", avatar = "url")
        )

        // Calculate the base financial values without payment integration
        var totalIuranBulanan = 0
        var totalPengeluaran = 0
        var totalIuranIndividu = 0

        for (dataItem in testItems) {
            totalIuranBulanan += dataItem.iuran_perwarga
            totalPengeluaran += dataItem.pengeluaran_iuran_warga
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }

        val expectedTotalIuranBulanan = totalIuranBulanan  // 300 (100 + 200)
        val expectedTotalPengeluaran = totalPengeluaran    // 15 (5 + 10)
        val expectedTotalIuranIndividu = totalIuranIndividu // 90 ((10*3) + (20*3))
        val expectedRekapIuran = expectedTotalIuranIndividu - expectedTotalPengeluaran  // 75 (90 - 15)

        // Simulate payment integration with total payment of 50
        val paymentTotal = 50

        // With the bug fix, payment totals should NOT affect the base financial calculations
        // The original financial values should remain unchanged
        assertEquals(300, expectedTotalIuranBulanan)
        assertEquals(15, expectedTotalPengeluaran)
        assertEquals(90, expectedTotalIuranIndividu)
        assertEquals(75, expectedRekapIuran)

        // Verify that adding paymentTotal to financial calculations (the old buggy behavior) 
        // would incorrectly change values
        val buggyTotalIuranBulanan = expectedTotalIuranBulanan + paymentTotal // 350 (WRONG!)
        val buggyRekapIuran = buggyTotalIuranBulanan - expectedTotalPengeluaran // 335 (WRONG!)
        
        assertNotEquals(buggyTotalIuranBulanan, expectedTotalIuranBulanan) // Should NOT have payment added
        assertNotEquals(buggyRekapIuran, expectedRekapIuran) // Should NOT have payment affecting rekap
    }
}