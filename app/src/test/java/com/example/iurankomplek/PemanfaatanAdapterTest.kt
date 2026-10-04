package com.example.iurankomplek

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PemanfaatanAdapterTest {

    private lateinit var adapter: PemanfaatanAdapter
    private lateinit var backingList: MutableList<DataItem>

    @Before
    fun setup() {
        backingList = mutableListOf()
        adapter = PemanfaatanAdapter(backingList, TestScope())
    }

    private fun item(
        email: String = "test@example.com",
        firstName: String = "Test",
        lastName: String = "User",
        pemanfaatan: String = "Maintenance",
        totalIuranRekap: Int = 100,
        pengeluaran: Int = 50
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = "Test Address",
        iuran_perwarga = 100,
        total_iuran_rekap = totalIuranRekap,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = pemanfaatan,
        avatar = "https://example.com/avatar.jpg"
    )

    private fun callback(old: List<DataItem>, new: List<DataItem>) =
        PemanfaatanAdapter.PemanfaatanDiffCallback(old, new)

    @Test
    fun `adapter starts empty`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `itemCount reflects the backing list`() {
        backingList.add(item())
        backingList.add(item(email = "other@example.com"))

        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `PemanfaatanDiffCallback should identify same items correctly`() {
        val diff = callback(listOf(item()), listOf(item()))

        assertTrue(diff.areItemsTheSame(0, 0))
        assertTrue(diff.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different items correctly`() {
        val diff = callback(listOf(item(pemanfaatan = "Maintenance")), listOf(item(pemanfaatan = "Utilities")))

        assertFalse(diff.areItemsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should identify different contents correctly`() {
        val diff = callback(listOf(item()), listOf(item(totalIuranRekap = 150)))

        assertTrue(diff.areItemsTheSame(0, 0))
        assertFalse(diff.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should treat changed expenditure as a different item`() {
        val diff = callback(listOf(item(pengeluaran = 50)), listOf(item(pengeluaran = 60)))

        assertFalse(diff.areItemsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback should return correct list sizes`() {
        val diff = callback(listOf(item(), item(email = "second@example.com")), listOf(item()))

        assertEquals(2, diff.oldListSize)
        assertEquals(1, diff.newListSize)
    }

    @Test
    fun `PemanfaatanDiffCallback should separate entries sharing only a description`() {
        val diff = callback(
            listOf(item(email = "a@example.com", pemanfaatan = "Maintenance")),
            listOf(item(email = "b@example.com", pemanfaatan = "Maintenance"))
        )

        assertFalse(diff.areItemsTheSame(0, 0))
    }
}
