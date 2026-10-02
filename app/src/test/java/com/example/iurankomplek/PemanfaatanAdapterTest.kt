package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PemanfaatanAdapterTest {

    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var testData: MutableList<DataItem>
    private lateinit var scope: CoroutineScope

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        scope = CoroutineScope(UnconfinedTestDispatcher())
        testData = mutableListOf()
        adapter = PemanfaatanAdapter(testData, scope)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun awaitItemCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }

    @Test
    fun `setPemanfaatan should update adapter data correctly`() {
        val newData = listOf(
            createTestDataItem("Maintenance", 100, 50),
            createTestDataItem("Utilities", 200, 75)
        )

        adapter.setPemanfaatan(newData)
        awaitItemCount(newData.size)

        assertEquals(newData.size, adapter.itemCount)
        assertEquals("Maintenance", testData[0].pemanfaatan_iuran)
        assertEquals("Utilities", testData[1].pemanfaatan_iuran)
    }

    @Test
    fun `itemCount should return correct count`() {
        assertEquals(0, adapter.itemCount)

        adapter.setPemanfaatan(listOf(createTestDataItem("Maintenance", 100, 50)))
        awaitItemCount(1)

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should initialize with empty list using scope constructor`() {
        val emptyAdapter = PemanfaatanAdapter(scope)

        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `PemanfaatanDiffCallback should identify same items correctly`() {
        val oldList = listOf(createTestDataItem("Maintenance", 100, 50))
        val newList = listOf(createTestDataItem("Maintenance", 100, 50))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertTrue(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different items correctly`() {
        val oldList = listOf(createTestDataItem("Maintenance", 100, 50))
        val newList = listOf(createTestDataItem("Utilities", 200, 75))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different contents correctly`() {
        val oldList = listOf(createTestDataItem("Maintenance", 100, 50))
        val newList = listOf(createTestDataItem("Maintenance", 100, 50).copy(avatar = "changed.jpg"))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertFalse(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should return correct list sizes`() {
        val oldList = listOf(
            createTestDataItem("Maintenance", 100, 50),
            createTestDataItem("Utilities", 200, 75)
        )
        val newList = listOf(createTestDataItem("Maintenance", 100, 50))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertEquals(2, diffCallback.getOldListSize())
        assertEquals(1, diffCallback.getNewListSize())
    }

    private fun createTestDataItem(
        pemanfaatan: String,
        totalIuranRekap: Int,
        pengeluaran: Int
    ): DataItem = DataItem(
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

    @Suppress("unused")
    private fun legacyCreateTestDataItem(
        pemanfaatan: String,
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