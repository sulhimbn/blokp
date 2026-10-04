package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PemanafatanAdapterTest {

    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var backingList: MutableList<DataItem>

    @Before
    fun setup() {
        // setPemanfaatan() applies the diff on Dispatchers.Main after computing it
        // on Dispatchers.Default, so Main runs eagerly and the diff is awaited.
        Dispatchers.setMain(Dispatchers.Unconfined)
        backingList = mutableListOf()
        adapter = PemanfaatanAdapter(backingList, CoroutineScope(Dispatchers.Default))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun awaitItemCount(expected: Int) = runBlocking {
        withTimeout(10_000) {
            while (adapter.itemCount != expected) {
                delay(5)
            }
        }
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

    @Test
    fun `setPemanfaatan should update adapter data correctly`() {
        val newData = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50),
            createTestDataItem("Utilities", "Electricity bill", 200, 75)
        )

        adapter.setPemanfaatan(newData)
        awaitItemCount(newData.size)

        assertEquals(newData.size, adapter.itemCount)
        assertEquals("Maintenance", backingList[0].pemanfaatan_iuran)
        assertEquals("Utilities", backingList[1].pemanfaatan_iuran)
    }

    @Test
    fun `itemCount should return correct count`() {
        assertEquals(0, adapter.itemCount)

        adapter.setPemanfaatan(listOf(createTestDataItem("Maintenance", "Repair work", 100, 50)))
        awaitItemCount(1)

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `scope only constructor should start empty`() {
        val emptyAdapter = PemanfaatanAdapter(CoroutineScope(Dispatchers.Default))

        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `diff callback should identify same items correctly`() {
        val oldList = listOf(createTestDataItem("Maintenance", "Repair work", 100, 50))
        val newList = listOf(createTestDataItem("Maintenance", "Repair work", 100, 50))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertTrue(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff callback should identify different items correctly`() {
        val oldList = listOf(createTestDataItem("Maintenance", "Repair work", 100, 50))
        val newList = listOf(createTestDataItem("Utilities", "Electricity bill", 200, 75))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff callback should separate items that share a description`() {
        val oldList = listOf(createTestDataItem("Maintenance", "Repair work", 100, 50))
        val newList = listOf(
            createTestDataItem("Maintenance", "Repair work", 150, 60)
                .copy(email = "other@example.com")
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff callback should return correct list sizes`() {
        val oldList = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50),
            createTestDataItem("Utilities", "Electricity bill", 200, 75)
        )
        val newList = listOf(createTestDataItem("Maintenance", "Repair work", 100, 50))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertEquals(2, diffCallback.getOldListSize())
        assertEquals(1, diffCallback.getNewListSize())
    }
}
