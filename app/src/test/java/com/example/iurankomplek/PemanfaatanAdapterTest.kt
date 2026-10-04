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
import org.robolectric.RobolectricTestRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class PemanfaatanAdapterTest {

    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var testData: MutableList<DataItem>

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        testData = mutableListOf()
        adapter = PemanfaatanAdapter(testData, CoroutineScope(Dispatchers.Unconfined))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun awaitData(expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (testData.size != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }

    @Test
    fun `setPemanfaatan replaces the backing list`() {
        val newData = listOf(
            item("Maintenance", 100, 50),
            item("Utilities", 200, 75)
        )

        adapter.setPemanfaatan(newData)
        awaitData(newData.size)

        assertEquals(newData.size, adapter.itemCount)
        assertEquals("Maintenance", testData[0].pemanfaatan_iuran)
        assertEquals("Utilities", testData[1].pemanfaatan_iuran)
    }

    @Test
    fun `itemCount tracks the backing list`() {
        assertEquals(0, adapter.itemCount)

        adapter.setPemanfaatan(listOf(item("Maintenance", 100, 50)))
        awaitData(1)

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `secondary constructor starts empty`() {
        val emptyAdapter = PemanfaatanAdapter(CoroutineScope(Dispatchers.Unconfined))

        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `PemanfaatanDiffCallback treats identical rows as unchanged`() {
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(item("Maintenance", 100, 50)),
            listOf(item("Maintenance", 100, 50))
        )

        assertEquals(1, callback.oldListSize)
        assertEquals(1, callback.newListSize)
        assertTrue(callback.areItemsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback separates rows with different identifiers`() {
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(item("Maintenance", 100, 50)),
            listOf(item("Maintenance", 100, 50, firstName = "Other"))
        )

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback reports changed contents for edited amounts`() {
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(item("Maintenance", 100, 50)),
            listOf(item("Maintenance", 100, 99))
        )

        assertFalse(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback reports list sizes`() {
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(item("Maintenance", 100, 50), item("Utilities", 200, 75)),
            listOf(item("Maintenance", 100, 50))
        )

        assertEquals(2, callback.oldListSize)
        assertEquals(1, callback.newListSize)
    }

    private fun item(
        pemanfaatan: String,
        totalIuranRekap: Int,
        pengeluaran: Int,
        firstName: String = "Test"
    ) = DataItem(
        first_name = firstName,
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