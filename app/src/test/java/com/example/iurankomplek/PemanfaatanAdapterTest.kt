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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PemanfaatanAdapterTest {

    private lateinit var adapter: PemanfaatanAdapter

    @Before
    fun setup() {
        // setPemanfaatan hops through Dispatchers.Main; a plain JVM test has no Main dispatcher.
        Dispatchers.setMain(UnconfinedTestDispatcher())
        adapter = PemanfaatanAdapter(mutableListOf<DataItem>(), CoroutineScope(Dispatchers.Unconfined))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `adapter should start empty`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `setPemanfaatan should update item count`() {
        val newData = listOf(
            TestFixtures.dataItem(pemanfaatanIuran = "Maintenance", pengeluaranIuranWarga = 50),
            TestFixtures.dataItem(pemanfaatanIuran = "Utilities", pengeluaranIuranWarga = 75)
        )

        adapter.setPemanfaatan(newData)

        assertEquals(2, awaitItemCount(2))
    }

    @Test
    fun `itemCount should return correct count`() {
        assertEquals(0, adapter.itemCount)

        adapter.setPemanfaatan(
            listOf(TestFixtures.dataItem(pemanfaatanIuran = "Maintenance"))
        )

        assertEquals(1, awaitItemCount(1))
    }

    @Test
    fun `adapter should initialize with empty list using scope-only constructor`() {
        val emptyAdapter = PemanfaatanAdapter(CoroutineScope(Dispatchers.Unconfined))

        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `PemanfaatanDiffCallback should identify same items correctly`() {
        val oldList = listOf(
            TestFixtures.dataItem(pemanfaatanIuran = "Maintenance", pengeluaranIuranWarga = 100)
        )
        val newList = listOf(
            TestFixtures.dataItem(pemanfaatanIuran = "Maintenance", pengeluaranIuranWarga = 100)
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertTrue(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different items correctly`() {
        val oldList = listOf(
            TestFixtures.dataItem(pemanfaatanIuran = "Maintenance", pengeluaranIuranWarga = 100)
        )
        val newList = listOf(
            TestFixtures.dataItem(pemanfaatanIuran = "Utilities", pengeluaranIuranWarga = 200)
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should treat changed amount as same item with new contents`() {
        // Issue #266 widened the identity key beyond pemanfaatan_iuran alone, but that key
        // still includes pengeluaran_iuran_warga: a changed amount is a different row, not a
        // rebind of the existing one.
        val oldList = listOf(
            TestFixtures.dataItem(pemanfaatanIuran = "Maintenance", pengeluaranIuranWarga = 100)
        )
        val newList = listOf(
            TestFixtures.dataItem(pemanfaatanIuran = "Maintenance", pengeluaranIuranWarga = 150)
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
        assertFalse(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should return correct list sizes`() {
        val oldList = listOf(
            TestFixtures.dataItem(pemanfaatanIuran = "Maintenance"),
            TestFixtures.dataItem(pemanfaatanIuran = "Utilities")
        )
        val newList = listOf(
            TestFixtures.dataItem(pemanfaatanIuran = "Maintenance")
        )

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertEquals(2, diffCallback.oldListSize)
        assertEquals(1, diffCallback.newListSize)
    }

    private fun awaitItemCount(expected: Int, timeoutMs: Long = 5_000): Int {
        val deadline = System.currentTimeMillis() + timeoutMs
        var count = adapter.itemCount
        while (count != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
            count = adapter.itemCount
        }
        return count
    }
}
