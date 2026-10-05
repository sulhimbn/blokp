package com.example.iurankomplek.presentation.adapter

import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.model.DataItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PemanfaatanAdapterTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private fun diff(old: List<DataItem>, new: List<DataItem>) =
        PemanfaatanAdapter.PemanfaatanDiffCallback(old, new)

    @Test
    fun `the scope-only constructor starts empty`() {
        assertEquals(0, PemanfaatanAdapter(scope).itemCount)
    }

    @Test
    fun `itemCount reflects the initial list`() {
        val adapter = PemanfaatanAdapter(mutableListOf(TestFixtures.dataItem()), scope)

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `empty utilisation data yields an empty adapter`() {
        assertEquals(0, PemanfaatanAdapter(mutableListOf(), scope).itemCount)
    }

    @Test
    fun `diff callback reports list sizes`() {
        val callback = diff(
            listOf(TestFixtures.dataItem()),
            listOf(TestFixtures.dataItem(), TestFixtures.dataItem(firstName = "Jane"))
        )

        assertEquals(1, callback.oldListSize)
        assertEquals(2, callback.newListSize)
    }

    @Test
    fun `diff callback keys identity on the composite fields`() {
        val callback = diff(listOf(TestFixtures.dataItem()), listOf(TestFixtures.dataItem()))

        assertTrue(callback.areItemsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff callback separates rows that share an expense description`() {
        val callback = diff(
            listOf(TestFixtures.dataItem(pemanfaatanIuran = "Maintenance")),
            listOf(
                TestFixtures.dataItem(firstName = "Jane", pemanfaatanIuran = "Maintenance"),
                TestFixtures.dataItem(firstName = "Ann", pemanfaatanIuran = "Maintenance")
            )
        )

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff callback treats a changed expense amount as a different row`() {
        val callback = diff(
            listOf(TestFixtures.dataItem(pengeluaranIuranWarga = 10)),
            listOf(TestFixtures.dataItem(pengeluaranIuranWarga = 99))
        )

        assertFalse(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff callback keeps identity when only unrelated fields change`() {
        val callback = diff(
            listOf(TestFixtures.dataItem(avatar = "https://example.com/a.png")),
            listOf(TestFixtures.dataItem(avatar = "https://example.com/b.png"))
        )

        assertTrue(callback.areItemsTheSame(0, 0))
        assertFalse(callback.areContentsTheSame(0, 0))
    }
}