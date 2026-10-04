package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PemanfaatanAdapterTest {

    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var testData: MutableList<DataItem>
    private lateinit var scope: CoroutineScope

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        scope = CoroutineScope(UnconfinedTestDispatcher())
        testData = mutableListOf()
        adapter = PemanfaatanAdapter(testData, scope)
    }

    @After
    fun tearDown() {
        scope.cancel()
        Dispatchers.resetMain()
    }

    private fun awaitItemCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
        assertEquals("adapter itemCount did not reach $expected", expected, adapter.itemCount)
    }

    @Test
    fun `setPemanfaatan should update adapter data correctly`() {
        val newData = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50),
            createTestDataItem("Utilities", "Electricity bill", 200, 75)
        )

        adapter.setPemanfaatan(newData)
        awaitItemCount(newData.size)

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
        awaitItemCount(1)
    }

    @Test
    fun `adapter should initialize with empty list using default constructor`() {
        val emptyAdapter = PemanfaatanAdapter(scope)
        
        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `PemanfaatanDiffCallback should identify same items correctly`() {
        val oldList = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50)
        )
        val newList = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50) // Same pemanfaatan_iuran
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
            createTestDataItem("Utilities", "Electricity bill", 200, 75) // Different pemanfaatan_iuran
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different contents correctly`() {
        val oldList = listOf(
            createTestDataItem("Maintenance", "Repair work", 100, 50)
        )
        val newList = listOf(
            createTestDataItem("Maintenance", "Repair work updated", 150, 60) // Same pemanfaatan_iuran but different other fields
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0)) // Same pemanfaatan_iuran
        assertFalse(diffCallback.areContentsTheSame(0, 0)) // Different overall content
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