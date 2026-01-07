package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class PemanfaatanAdapterTest {

    private lateinit var adapter: PemanfaatanAdapter

    @Before
    fun setup() {
        adapter = PemanfaatanAdapter()
    }

    @Test
    fun `submitList should update adapter data correctly`() {
        val newData = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50),
            createTestDataItem("Utilities", "Electricity bill", 200, 75)
        )

        adapter.submitList(newData)

        assertEquals(newData.size, adapter.itemCount)
    }

    @Test
    fun `itemCount should return correct count`() {
        assertEquals(0, adapter.itemCount)

        val newData = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50)
        )
        adapter.submitList(newData)

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should initialize with empty list`() {
        val emptyAdapter = PemanfaatanAdapter()

        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `PemanfaatanDiffCallback should identify same items correctly`() {
        val oldItem = createTestDataItem("Maintenance", "Repair work", 100, 50)
        val newItem = createTestDataItem("Maintenance", "Repair work", 100, 50) // Same pemanfaatan_iuran

        assertTrue(PemanfaatanAdapter.PemanfaatanDiffCallback.areItemsTheSame(oldItem, newItem))
        assertTrue(PemanfaatanAdapter.PemanfaatanDiffCallback.areContentsTheSame(oldItem, newItem))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different items correctly`() {
        val oldItem = createTestDataItem("Maintenance", "Repair work", 100, 50)
        val newItem = createTestDataItem("Utilities", "Electricity bill", 200, 75) // Different pemanfaatan_iuran

        assertFalse(PemanfaatanAdapter.PemanfaatanDiffCallback.areItemsTheSame(oldItem, newItem))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different contents correctly`() {
        val oldItem = createTestDataItem("Maintenance", "Repair work", 100, 50)
        val newItem = createTestDataItem("Maintenance", "Repair work updated", 150, 60) // Same pemanfaatan_iuran but different other fields

        assertTrue(PemanfaatanAdapter.PemanfaatanDiffCallback.areItemsTheSame(oldItem, newItem)) // Same pemanfaatan_iuran
        assertFalse(PemanfaatanAdapter.PemanfaatanDiffCallback.areContentsTheSame(oldItem, newItem)) // Different overall content
    }

    @Test
    fun `submitList should handle empty list`() {
        val initialData = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50),
            createTestDataItem("Utilities", "Electricity bill", 200, 75)
        )
        adapter.submitList(initialData)

        assertEquals(2, adapter.itemCount)

        adapter.submitList(emptyList())

        assertEquals(0, adapter.itemCount)
    }

    private fun createTestDataItem(
        pemanfaatan: String,
        description: String,
        totalIuranRekap: Int,
        pengeluaran: Int
    ): DataItem {
        return DataItem(
            first_name = "Test",
            last_name = "User",
            email = "test@example.com",
            alamat = "Test Address",
            iuran_perwarga = 100,
            total_iuran_rekap = totalIuranRekap,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 150,
            pengeluaran_iuran_warga = pengeluaran,
            pemanfaatan_iuran = pemanfaatan,
            avatar = "https://example.com/avatar.jpg"
        )
    }
}