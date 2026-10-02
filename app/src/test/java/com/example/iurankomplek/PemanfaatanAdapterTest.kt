package com.example.iurankomplek
import android.app.Application

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
@Config(sdk = [33], application = Application::class)
class PemanfaatanAdapterTest {

    private lateinit var backing: MutableList<DataItem>
    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var scope: CoroutineScope

    private fun item(pemanfaatan: String? = "Perbaikan jalan", pengeluaran: Int = 50) = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = "john@example.com",
        alamat = "Jl. Test 1",
        iuran_perwarga = 100,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = 0,
        total_iuran_individu = 50,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = pemanfaatan,
        avatar = ""
    )

    @Before
    fun setup() {
        val dispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(dispatcher)
        scope = CoroutineScope(dispatcher)
        backing = mutableListOf()
        adapter = PemanfaatanAdapter(backing, scope)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `item count reflects the backing list`() {
        assertEquals(0, adapter.itemCount)
        backing += item()
        assertEquals(1, adapter.itemCount)
        backing += item(pemanfaatan = "Pembangunan taman", pengeluaran = 75)
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `scope-only constructor starts empty`() {
        val fresh = PemanfaatanAdapter(scope)

        assertEquals(0, fresh.itemCount)
    }

    @Test
    fun `setPemanfaatan replaces the content`() = runTest {
        val rows = listOf(
            item(pemanfaatan = "Perbaikan jalan", pengeluaran = 50),
            item(pemanfaatan = "Pembangunan taman", pengeluaran = 75)
        )

        adapter.setPemanfaatan(rows)

        awaitItemCount(2)
    }

    @Test
    fun `setPemanfaatan with an empty list empties the adapter`() = runTest {
        backing += item()

        adapter.setPemanfaatan(emptyList())

        awaitItemCount(0)
    }

    @Test
    fun `diff callback reports both list sizes`() {
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(item()),
            listOf(item(), item(pemanfaatan = "Lain"))
        )

        assertEquals(1, callback.oldListSize)
        assertEquals(2, callback.newListSize)
    }

    @Test
    fun `diff callback treats a changed description as a different row`() {
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(item(pemanfaatan = "Lama")),
            listOf(item(pemanfaatan = "Baru"))
        )

        assertFalse(
            "a changed description must not match the same row",
            callback.areItemsTheSame(0, 0)
        )
    }

    @Test
    fun `diff callback treats an identical record as same row with same content`() {
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(item()),
            listOf(item())
        )

        assertTrue(callback.areItemsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    private fun awaitItemCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 10_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(5)
        }
        assertEquals(expected, adapter.itemCount)
    }
}
