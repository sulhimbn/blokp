package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
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

/**
 * Covers [PemanfaatanAdapter] and its DiffUtil callback.
 *
 * The adapter mutates the MutableList handed to its constructor in place, so these tests
 * keep their own reference to that list and assert against it rather than the adapter's
 * private field.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PemanafyaatanAdapterTest {

    private lateinit var testData: MutableList<DataItem>
    private lateinit var adapter: PemanfaatanAdapter
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testData = mutableListOf()
        adapter = PemanfaatanAdapter(testData, TestScope(testDispatcher))
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

    private fun item(pemanfaatan: String, rekap: Int, pengeluaran: Int) = TestFixtures.dataItem(
        pemanfaatanIuran = pemanfaatan,
        totalIuranRekap = rekap,
        pengeluaranIuranWarga = pengeluaran
    )

    @Test
    fun `setPemanfaatan replaces the adapter contents`() {
        val newData = listOf(
            item("Maintenance", 100, 50),
            item("Utilities", 200, 75)
        )

        adapter.setPemanfaatan(newData)
        awaitItemCount(2)

        assertEquals(2, adapter.itemCount)
        assertEquals("Maintenance", testData[0].pemanfaatan_iuran)
        assertEquals("Utilities", testData[1].pemanfaatan_iuran)
    }

    @Test
    fun `itemCount starts at zero and tracks the submitted list`() {
        assertEquals(0, adapter.itemCount)

        adapter.setPemanfaatan(listOf(item("Maintenance", 100, 50)))
        awaitItemCount(1)

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `the convenience constructor starts empty`() {
        val emptyAdapter = PemanfaatanAdapter(TestScope(testDispatcher))

        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `diff callback treats identical entries as unchanged`() {
        val oldList = listOf(item("Maintenance", 100, 50))
        val newList = listOf(item("Maintenance", 100, 50))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertTrue(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff callback treats a changed utilisation type as a different item`() {
        val oldList = listOf(item("Maintenance", 100, 50))
        val newList = listOf(item("Utilities", 200, 75))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff callback treats a changed amount as a different item`() {
        val oldList = listOf(item("Maintenance", 100, 50))
        val newList = listOf(item("Maintenance", 100, 999))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff callback detects changed content under the same item`() {
        // iuran_perwarga is the one field outside the issue #266 identity key.
        val oldList = listOf(item("Maintenance", 100, 50).copy(iuran_perwarga = 100))
        val newList = listOf(item("Maintenance", 100, 50).copy(iuran_perwarga = 999))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertFalse(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff callback reports both list sizes`() {
        val oldList = listOf(item("Maintenance", 100, 50), item("Utilities", 200, 75))
        val newList = listOf(item("Maintenance", 100, 50))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertEquals(2, diffCallback.getOldListSize())
        assertEquals(1, diffCallback.getNewListSize())
    }
}
