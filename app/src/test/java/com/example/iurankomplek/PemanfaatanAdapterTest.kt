package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PemanfaatanAdapterTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var testData: MutableList<DataItem>

    @Before
    fun setup() {
        testData = mutableListOf()
        adapter = PemanfaatanAdapter(testData, CoroutineScope(mainDispatcherRule.dispatcher))
    }

    @Test
    fun `setPemanfaatan should update adapter data correctly`() {
        val newData = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50),
            createTestDataItem("Utilities", "Electricity bill", 200, 75)
        )

        adapter.setPemanfaatan(newData)

        awaitCondition("adapter to receive ${newData.size} items") {
            adapter.itemCount == newData.size
        }
        assertEquals(newData.size, adapter.itemCount)
        assertEquals("Maintenance", testData[0].pemanfaatan_iuran)
        assertEquals("Utilities", testData[1].pemanfaatan_iuran)
    }

    @Test
    fun `itemCount should return correct count`() {
        assertEquals(0, adapter.itemCount)

        val newData = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50)
        )
        adapter.setPemanfaatan(newData)

        awaitCondition("adapter to receive 1 item") { adapter.itemCount == 1 }
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should initialize with empty list using single-arg constructor`() {
        val emptyAdapter = PemanfaatanAdapter(CoroutineScope(mainDispatcherRule.dispatcher))

        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `PemanfaatanDiffCallback should identify same items correctly`() {
        val oldList = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50)
        )
        val newList = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50)
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertTrue(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different items correctly`() {
        val oldList = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50)
        )
        val newList = listOf(
            createTestDataItem("Utilities", "Electricity bill", 200, 75)
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

@Test
    fun `PemanfaatanDiffCallback should identify different contents correctly`() {
        // Identity is email + first/last name + utilisation + amount (issue #266), so a change
        // confined to a non-key field keeps the same identity while changing the contents.
        val oldList = listOf(createTestDataItem("Maintenance", "Repair work", 100, 50))
        val newList = listOf(
            createTestDataItem("Maintenance", "Repair work", 999, 50).copy(
                avatar = "https://example.com/changed-avatar.jpg"
            )
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertFalse(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should treat a changed amount as a different item`() {
        val oldList = listOf(createTestDataItem("Maintenance", "Repair work", 100, 50))
        val newList = listOf(createTestDataItem("Maintenance", "Repair work", 150, 60))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should return correct list sizes`() {
        val oldList = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50),
            createTestDataItem("Utilities", "Electricity bill", 200, 75)
        )
        val newList = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50)
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertEquals(2, diffCallback.getOldListSize())
        assertEquals(1, diffCallback.getNewListSize())
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