package com.example.iurankomplek

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.presentation.adapter.PemanfaatanAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PemanfaatanAdapterTest {

    private lateinit var scope: CoroutineScope

    @Before
    fun setup() {
        scope = CoroutineScope(Dispatchers.Main.immediate)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun adapterWith(items: MutableList<DataItem> = mutableListOf()) =
        PemanfaatanAdapter(items, scope)

    private fun parent(): ViewGroup = FrameLayout(RuntimeEnvironment.getApplication())

    private fun textOf(holder: RecyclerView.ViewHolder, viewId: Int): String =
        holder.itemView.findViewById<TextView>(viewId).text.toString()

    private fun entry(description: String?, spent: Int) = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = "john@example.com",
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = spent,
        pemanfaatan_iuran = description,
        avatar = "https://example.com/avatar.jpg"
    )


    private fun awaitItems(adapter: RecyclerView.Adapter<*>, expected: Int, timeoutMs: Long = 10_000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            ShadowLooper.idleMainLooper()
            Thread.sleep(10)
        }
    }

    @Test
    fun `empty adapter reports zero items`() {
        assertEquals(0, adapterWith().itemCount)
    }

    @Test
    fun `bind renders the utilisation label and the spent amount`() {
        val adapter = adapterWith(mutableListOf(entry("Perbaikan jalan", 50_000)))

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder as PemanfaatanAdapter.ListViewHolder, 0)

        assertEquals("-Perbaikan jalan:", textOf(holder, R.id.itemPemanfaatan))
        assertEquals("Rp.50,000", textOf(holder, R.id.itemDanaPemanfaatan))
    }

    @Test
    fun `bind clamps a negative amount to zero instead of showing a negative currency`() {
        val adapter = adapterWith(mutableListOf(entry("Refund", -25_000)))

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder as PemanfaatanAdapter.ListViewHolder, 0)

        assertEquals("Rp.0", textOf(holder, R.id.itemDanaPemanfaatan))
    }

    @Test
    fun `bind replaces a missing description with a placeholder`() {
        val adapter = adapterWith(mutableListOf(entry(null, 10_000)))

        val holder = adapter.onCreateViewHolder(parent(), 0)
        adapter.onBindViewHolder(holder as PemanfaatanAdapter.ListViewHolder, 0)

        assertEquals("-Unknown expense:", textOf(holder, R.id.itemPemanfaatan))
    }

    @Test
    fun `setPemanfaatan replaces the backing list with the new entries`() {
        val adapter = adapterWith()
        val replacement = listOf(
            entry("Perbaikan jalan", 50_000),
            entry("Pembangunan taman", 75_000)
        )

        adapter.setPemanfaatan(replacement)
        awaitItems(adapter, replacement.size)

        assertEquals(2, adapter.itemCount)
        assertEquals("Perbaikan jalan", adapter.currentPemanfaatan.first().pemanfaatan_iuran)
    }

    @Test
    fun `PemanfaatanDiffCallback treats identical entries as unchanged`() {
        val callback = PemanfaatanAdapter.PemanfaatanDiffCallback(
            listOf(entry("Perbaikan jalan", 50_000)),
            listOf(entry("Perbaikan jalan", 50_000))
        )

        assertEquals(1, callback.oldListSize)
        assertEquals(1, callback.newListSize)
        assertTrue(callback.areItemsTheSame(0, 0))
        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback separates entries that share a description but differ in cost`() {
        val callback = PemanafploanEntries(
            old = entry("Perbaikan jalan", 50_000),
            new = entry("Perbaikan jalan", 99_000)
        )

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun `PemanfaatanDiffCallback reports the old and new list sizes`() {
        val callback = PemanafploanEntries(
            old = entry("a", 1),
            new = entry("a", 1),
            extraOld = entry("b", 2)
        )

        assertEquals(2, callback.oldListSize)
        assertEquals(1, callback.newListSize)
    }

    private fun PemanafploanEntries(
        old: DataItem,
        new: DataItem,
        extraOld: DataItem? = null
    ) = PemanfaatanAdapter.PemanfaatanDiffCallback(
        listOfNotNull(old, extraOld),
        listOf(new)
    )
}