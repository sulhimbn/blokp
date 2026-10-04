package com.example.iurankomplek

import android.app.Application
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter.PemanfaatanDiffCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class PemanfaatanAdapterTest {

    private fun adapterWith(items: List<com.example.iurankomplek.model.DataItem>) =
        PemanfaatanAdapter(items.toMutableList(), CoroutineScope(Dispatchers.Unconfined))

    private fun parent(): FrameLayout =
        FrameLayout(RuntimeEnvironment.getApplication())

    @Test
    fun `starts empty when constructed without data`() {
        assertEquals(0, PemanfaatanAdapter(CoroutineScope(Dispatchers.Unconfined)).itemCount)
    }

    @Test
    fun `reports the number of rows it was seeded with`() {
        val adapter = adapterWith(
            listOf(
                TestFixtures.dataItem(pemanfaatanIuran = "Jalan"),
                TestFixtures.dataItem(pemanfaatanIuran = "Atap")
            )
        )

        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `renders the utilisation label and the formatted amount`() {
        val adapter = adapterWith(
            listOf(TestFixtures.dataItem(pemanfaatanIuran = "Perbaikan jalan", pengeluaranIuranWarga = 125000))
        )
        val holder = adapter.onCreateViewHolder(parent(), 0)

        adapter.onBindViewHolder(holder, 0)

        assertEquals("-Perbaikan jalan:", holder.binding.itemPemanfaatan.text.toString())
        assertEquals("Rp.125,000", holder.binding.itemDanaPemanfaatan.text.toString())
    }

    @Test
    fun `substitutes a placeholder when the utilisation text is missing`() {
        val adapter = adapterWith(
            listOf(TestFixtures.dataItem(pemanfaatanIuran = null, pengeluaranIuranWarga = 1000))
        )
        val holder = adapter.onCreateViewHolder(parent(), 0)

        adapter.onBindViewHolder(holder, 0)

        assertEquals("-Unknown expense:", holder.binding.itemPemanfaatan.text.toString())
    }

    @Test
    fun `clamps a negative amount to zero instead of rendering a negative rupiah value`() {
        val adapter = adapterWith(
            listOf(TestFixtures.dataItem(pemanfaatanIuran = "Dana", pengeluaranIuranWarga = -5000))
        )
        val holder = adapter.onCreateViewHolder(parent(), 0)

        adapter.onBindViewHolder(holder, 0)

        assertEquals("Rp.0", holder.binding.itemDanaPemanfaatan.text.toString())
    }

    @Test
    fun `diff treats the same resident and line item as the same row`() {
        val old = TestFixtures.dataItem(pemanfaatanIuran = "Jalan", pengeluaranIuranWarga = 100)
        val new = old.copy()
        val diff = PemnatalanCallback(listOf(old), listOf(new))

        assertTrue(diff.areItemsTheSame(0, 0))
        assertTrue(diff.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff treats a changed line item as a different row`() {
        val diff = PemnatalanCallback(
            listOf(TestFixtures.dataItem(pemanfaatanIuran = "Jalan")),
            listOf(TestFixtures.dataItem(pemanfaatanIuran = "Atap"))
        )

        assertFalse(diff.areItemsTheSame(0, 0))
    }

    @Test
    fun `diff treats a changed amount as the same row with new contents`() {
        val old = TestFixtures.dataItem(pemanfaatanIuran = "Jalan", pengeluaranIuranWarga = 100)
        val diff = PemnatalanCallback(listOf(old), listOf(old.copy(pengeluaran_iuran_warga = 200)))

        assertTrue(diff.areItemsTheSame(0, 0))
        assertFalse(diff.areContentsTheSame(0, 0))
    }

    @Test
    fun `diff reports sizes on both sides`() {
        val diff = PemnatalanCallback(
            listOf(TestFixtures.dataItem(), TestFixtures.dataItem()),
            listOf(TestFixtures.dataItem())
        )

        assertEquals(2, diff.oldListSize)
        assertEquals(1, diff.newListSize)
    }

    @Test
    fun `adapts to a recycle pool without crashing`() {
        val adapter: RecyclerView.Adapter<PemanfaatanAdapter.ListViewHolder> =
            adapterWith(listOf(TestFixtures.dataItem()))

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.bindViewHolder(holder, 0)
        adapter.onViewRecycled(holder)

        assertEquals(1, adapter.itemCount)
    }

    private fun PemnatalanCallback(
        old: List<com.example.iurankomplek.model.DataItem>,
        new: List<com.example.iurankomplek.model.DataItem>
    ) = PemanfaatanDiffCallback(old, new)
}
