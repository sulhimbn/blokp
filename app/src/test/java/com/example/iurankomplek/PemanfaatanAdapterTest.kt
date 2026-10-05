package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.app.Application
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class PemanfaatanAdapterTest {

    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var items: MutableList<DataItem>

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        items = mutableListOf()
        adapter = PemanfaatanAdapter(items, CoroutineScope(Dispatchers.Unconfined))
    }

    private fun awaitItemCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun item(
        email: String? = "user@example.com",
        pemanfaatan: String? = "Maintenance",
        pengeluaran: Int = 50
    ) = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = email,
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = pemanfaatan,
        avatar = null
    )

    @Test
    fun `setPemanfaatan should update adapter data correctly`() {
        val newItems = listOf(
            item(email = "a@example.com", pemanfaatan = "Maintenance", pengeluaran = 50),
            item(email = "b@example.com", pemanfaatan = "Repairs", pengeluaran = 75)
        )

        adapter.setPemanfaatan(newItems)

        awaitItemCount(2)

        assertEquals(2, adapter.itemCount)
        assertEquals(2, items.size)
        assertEquals("Maintenance", items[0].pemanfaatan_iuran)
        assertEquals("Repairs", items[1].pemanfaatan_iuran)
    }

    @Test
    fun `setPemanfaatan should replace previously set items`() {
        adapter.setPemanfaatan(listOf(item(email = "a@example.com")))
        awaitItemCount(1)
        assertEquals(1, adapter.itemCount)

        adapter.setPemanfaatan(listOf(item(email = "b@example.com"), item(email = "c@example.com")))

        awaitItemCount(2)

        assertEquals(2, adapter.itemCount)
        assertEquals(2, items.size)
    }

    @Test
    fun `setPemanfaatan with empty list should clear the adapter`() {
        adapter.setPemanfaatan(listOf(item()))
        awaitItemCount(1)
        assertEquals(1, adapter.itemCount)

        adapter.setPemanfaatan(emptyList())

        awaitItemCount(0)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `secondary constructor should start with an empty list`() {
        val fresh = PemanfaatanAdapter(CoroutineScope(Dispatchers.Unconfined))

        assertEquals(0, fresh.itemCount)
    }

    @Test
    fun `itemCount should reflect the number of bound rows`() {
        awaitItemCount(0)
        assertEquals(0, adapter.itemCount)

        adapter.setPemanfaatan(List(5) { item(email = "user$it@example.com") })

        awaitItemCount(5)

        assertEquals(5, adapter.itemCount)
    }
}