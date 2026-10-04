package com.example.iurankomplek.presentation.adapter

import com.example.iurankomplek.model.DataItem
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
@Config(sdk = [28], application = android.app.Application::class)
class PemanfaatanAdapterTest {

    private lateinit var rows: MutableList<DataItem>
    private lateinit var adapter: PemanfaatanAdapter

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        rows = mutableListOf()
        adapter = PemanfaatanAdapter(rows, CoroutineScope(UnconfinedTestDispatcher()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun item(
        email: String = "warga@example.com",
        firstName: String = "Warga",
        lastName: String = "Uji",
        usage: String = "Maintenance",
        spend: Int = 50
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = "Jl. Test No. 1",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = spend,
        pemanfaatan_iuran = usage,
        avatar = "https://example.com/avatar.jpg"
    )

    private fun awaitCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 5000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }

    @Test
    fun startsWithEmptyList() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun scopeOnlyConstructorStartsEmpty() {
        val emptyAdapter = PemanfaatanAdapter(CoroutineScope(UnconfinedTestDispatcher()))
        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun setPemanfaatanReplacesBackingData() {
        adapter.setPemanfaatan(
            listOf(
                item(usage = "Maintenance"),
                item(usage = "Utilities", spend = 75)
            )
        )
        awaitCount(2)

        assertEquals(2, adapter.itemCount)
        assertEquals("Maintenance", rows[0].pemanfaatan_iuran)
        assertEquals("Utilities", rows[1].pemanfaatan_iuran)
    }

    @Test
    fun setPemanfaatanWithEmptyPayloadClearsAdapter() {
        adapter.setPemanfaatan(listOf(item()))
        awaitCount(1)

        adapter.setPemanfaatan(emptyList())
        awaitCount(0)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun diffCallbackTreatsIdenticalEntryAsSameItemAndContent() {
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(listOf(item()), listOf(item()))

        assertTrue(callback.areItemsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun diffCallbackSeparatesEntriesDifferingOnIdentityFields() {
        val byUsage = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(item()),
            listOf(item(usage = "Utilities"))
        )
        val byEmail = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(item()),
            listOf(item(email = "other@example.com"))
        )
        val bySpend = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(item()),
            listOf(item(spend = 999))
        )

        assertFalse(byUsage.areItemsTheSame(0, 0))
        assertFalse(byEmail.areItemsTheSame(0, 0))
        assertFalse(bySpend.areItemsTheSame(0, 0))
    }

    @Test
    fun diffCallbackReportsDifferentContentsForSameIdentity() {
        val old = listOf(item())
        val new = listOf(item(firstName = "Warga", lastName = "Uji", email = "warga@example.com").copy(
            iuran_perwarga = 999
        ))
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(old, new)

        assertTrue(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun diffCallbackReportsListSizes() {
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(listOf(item(), item()), listOf(item()))

        assertEquals(2, callback.oldListSize)
        assertEquals(1, callback.newListSize)
    }
}
