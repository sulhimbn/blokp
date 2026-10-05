package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import androidx.recyclerview.widget.RecyclerView
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * The adapter keeps its backing list private, so these tests assert item count plus
 * the PemanfaatanDiffCallback identity/content rules it relies on.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
@LooperMode(LooperMode.Mode.LEGACY)
class PemanfaatanAdapterTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var testData: MutableList<DataItem>

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        testData = mutableListOf()
        adapter = PemanfaatanAdapter(testData, CoroutineScope(UnconfinedTestDispatcher()))
    }

    private fun item(
        pemanfaatan: String = "Maintenance",
        totalIuranRekap: Int = 100,
        pengeluaran: Int = 50
    ) = DataItem(
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

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setPemanfaatan should update adapter data correctly`() {
        val newData = listOf(
            item(pemanfaatan = "Maintenance"),
            item(pemanfaatan = "Utilities")
        )

        adapter.setPemanfaatan(newData)

        awaitItemCount(adapter, newData.size)

        assertEquals(newData.size, adapter.itemCount)
    }

    @Test
    fun `itemCount should return correct count`() {
        assertEquals(0, adapter.itemCount)

        adapter.setPemanfaatan(listOf(item()))

        awaitItemCount(adapter, 1)

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should initialize with empty list`() {
        val emptyAdapter = PemanfaatanAdapter(CoroutineScope(UnconfinedTestDispatcher()))

        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `PemanfaatanDiffCallback should identify same items correctly`() {
        val oldList = listOf(item(pemanfaatan = "Maintenance"))
        val newList = listOf(item(pemanfaatan = "Maintenance"))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertTrue(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different items correctly`() {
        val oldList = listOf(item(pemanfaatan = "Maintenance"))
        val newList = listOf(item(pemanfaatan = "Utilities"))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different contents correctly`() {
        val oldList = listOf(item(pemanfaatan = "Maintenance", totalIuranRekap = 100))
        val newList = listOf(item(pemanfaatan = "Maintenance", totalIuranRekap = 150))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertFalse(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should return correct list sizes`() {
        val oldList = listOf(item(pemanfaatan = "Maintenance"), item(pemanfaatan = "Utilities"))
        val newList = listOf(item(pemanfaatan = "Maintenance"))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertEquals(2, diffCallback.getOldListSize())
        assertEquals(1, diffCallback.getNewListSize())
    }
}

/** setUsers computes the diff on Dispatchers.Default and applies it on Main. */
private fun awaitItemCount(adapter: RecyclerView.Adapter<*>, expected: Int) {
    val deadline = System.currentTimeMillis() + 5_000
    while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
        Thread.sleep(10)
    }
}
