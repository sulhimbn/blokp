package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
class PemanfaatanAdapterTest {

    private lateinit var pemanfaatan: MutableList<DataItem>
    private lateinit var adapter: PemanfaatanAdapter

    @Before
    fun setup() {
        pemanfaatan = mutableListOf()
        adapter = PemanfaatanAdapter(pemanfaatan, CoroutineScope(Dispatchers.Unconfined))
    }

    @Test
    fun `constructor should start with an empty list`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `constructor with only a scope should start empty`() {
        val emptyAdapter = PemanfaatanAdapter(CoroutineScope(Dispatchers.Unconfined))

        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun `PemanfaatanDiffCallback should identify identical items correctly`() {
        val oldList = listOf(pemanfaatanItem("Maintenance", 50))
        val newList = listOf(pemanfaatanItem("Maintenance", 50))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertTrue(diffCallback.areItemsTheSame(0, 0))
        assertTrue(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different items correctly`() {
        val oldList = listOf(pemanfaatanItem("Maintenance", 50))
        val newList = listOf(pemanfaatanItem("Utilities", 75))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different contents for the same key`() {
        val oldList = listOf(pemanfaatanItem("Maintenance", 50))
        val newList = listOf(pemanfaatanItem("Maintenance", 60))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertFalse(diffCallback.areItemsTheSame(0, 0))
        assertFalse(diffCallback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should return correct list sizes`() {
        val oldList = listOf(
            pemanfaatanItem("Maintenance", 50),
            pemanfaatanItem("Utilities", 75)
        )
        val newList = listOf(pemanfaatanItem("Maintenance", 50))

        val diffCallback = PemanfaatanAdapter.PemanfaatanDiffCallback(oldList, newList)

        assertEquals(2, diffCallback.getOldListSize())
        assertEquals(1, diffCallback.getNewListSize())
    }

    private fun pemanfaatanItem(pemanfaatan: String, pengeluaran: Int): DataItem =
        TestFixtures.dataItem(
            first_name = "Test",
            last_name = "User",
            email = "test@example.com",
            alamat = "Test Address",
            iuran_perwarga = 100,
            total_iuran_rekap = 500,
            jumlah_iuran_bulanan = 200,
            total_iuran_individu = 150,
            pengeluaran_iuran_warga = pengeluaran,
            pemanfaatan_iuran = pemanfaatan,
            avatar = "https://example.com/avatar.jpg"
        )
}