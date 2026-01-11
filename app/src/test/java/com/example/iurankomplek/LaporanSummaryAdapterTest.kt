package com.example.iurankomplek

import androidx.recyclerview.widget.ListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LaporanSummaryAdapterTest {

    private lateinit var adapter: LaporanSummaryAdapter
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        adapter = LaporanSummaryAdapter(
            coroutineScope = TestScope(testDispatcher)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LaporanSummaryAdapter should have correct initial item count`() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `LaporanSummaryAdapter should set items correctly`() = runTest {
        val items = listOf(
            LaporanSummaryItem(title = "Total Iuran", value = "Rp 500.000"),
            LaporanSummaryItem(title = "Total Pengeluaran", value = "Rp 200.000")
        )

        adapter.setItems(items)
        advanceUntilIdle()

        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `LaporanSummaryAdapter should set empty list`() = runTest {
        val items = listOf(
            LaporanSummaryItem(title = "Total Iuran", value = "Rp 500.000")
        )

        adapter.setItems(items)
        advanceUntilIdle()
        assertEquals(1, adapter.itemCount)

        adapter.setItems(emptyList())
        advanceUntilIdle()
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `LaporanSummaryAdapter should create ViewHolder correctly`() {
        val context = RuntimeEnvironment.getApplication()
        val parent = androidx.recyclerview.widget.RecyclerView(context)

        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(viewHolder)
        assertNotNull(viewHolder.itemView)
    }

    @Test
    fun `LaporanSummaryAdapter should bind ViewHolder correctly`() = runTest {
        val items = listOf(
            LaporanSummaryItem(title = "Total Iuran", value = "Rp 500.000"),
            LaporanSummaryItem(title = "Total Pengeluaran", value = "Rp 200.000")
        )

        adapter.setItems(items)
        advanceUntilIdle()

        val context = RuntimeEnvironment.getApplication()
        val parent = androidx.recyclerview.widget.RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals("Total Iuran", viewHolder.tvTitle.text.toString())
        assertEquals("Rp 500.000", viewHolder.tvValue.text.toString())
    }

    @Test
    fun `LaporanSummaryAdapter should bind second item correctly`() = runTest {
        val items = listOf(
            LaporanSummaryItem(title = "Total Iuran", value = "Rp 500.000"),
            LaporanSummaryItem(title = "Total Pengeluaran", value = "Rp 200.000")
        )

        adapter.setItems(items)
        advanceUntilIdle()

        val context = RuntimeEnvironment.getApplication()
        val parent = androidx.recyclerview.widget.RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 1)

        assertEquals("Total Pengeluaran", viewHolder.tvTitle.text.toString())
        assertEquals("Rp 200.000", viewHolder.tvValue.text.toString())
    }

    @Test
    fun `LaporanSummaryAdapter DiffCallback should identify same items by title`() = runTest {
        val oldItems = listOf(
            LaporanSummaryItem(title = "Total Iuran", value = "Rp 500.000")
        )
        val newItems = listOf(
            LaporanSummaryItem(title = "Total Iuran", value = "Rp 600.000") // Same title, different value
        )

        adapter.setItems(oldItems)
        advanceUntilIdle()

        adapter.setItems(newItems)
        advanceUntilIdle()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `LaporanSummaryAdapter DiffCallback should detect content changes`() = runTest {
        val oldItems = listOf(
            LaporanSummaryItem(title = "Total Iuran", value = "Rp 500.000")
        )
        val newItems = listOf(
            LaporanSummaryItem(title = "Total Iuran", value = "Rp 600.000")
        )

        adapter.setItems(oldItems)
        advanceUntilIdle()

        adapter.setItems(newItems)
        advanceUntilIdle()

        val context = RuntimeEnvironment.getApplication()
        val parent = androidx.recyclerview.widget.RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals("Rp 600.000", viewHolder.tvValue.text.toString())
    }

    @Test
    fun `LaporanSummaryAdapter should handle items with empty strings`() = runTest {
        val items = listOf(
            LaporanSummaryItem(title = "", value = ""),
            LaporanSummaryItem(title = "Test", value = "")
        )

        adapter.setItems(items)
        advanceUntilIdle()

        assertEquals(2, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = androidx.recyclerview.widget.RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals("", viewHolder.tvTitle.text.toString())
        assertEquals("", viewHolder.tvValue.text.toString())
    }

    @Test
    fun `LaporanSummaryAdapter should handle items with special characters`() = runTest {
        val items = listOf(
            LaporanSummaryItem(title = "Iuran (2024)", value = "Rp 500.000,-"),
            LaporanSummaryItem(title = "Pengeluaran & Biaya", value = "$100.00")
        )

        adapter.setItems(items)
        advanceUntilIdle()

        assertEquals(2, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = androidx.recyclerview.widget.RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals("Iuran (2024)", viewHolder.tvTitle.text.toString())
        assertEquals("Rp 500.000,-", viewHolder.tvValue.text.toString())
    }

    @Test
    fun `LaporanSummaryAdapter should handle items with very long strings`() = runTest {
        val longTitle = "A".repeat(500)
        val longValue = "B".repeat(500)
        val items = listOf(
            LaporanSummaryItem(title = longTitle, value = longValue)
        )

        adapter.setItems(items)
        advanceUntilIdle()

        assertEquals(1, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = androidx.recyclerview.widget.RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals(longTitle, viewHolder.tvTitle.text.toString())
        assertEquals(longValue, viewHolder.tvValue.text.toString())
    }

    @Test
    fun `LaporanSummaryAdapter should handle single item`() = runTest {
        val items = listOf(
            LaporanSummaryItem(title = "Single Item", value = "Rp 100")
        )

        adapter.setItems(items)
        advanceUntilIdle()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `LaporanSummaryAdapter should handle large number of items`() = runTest {
        val items = (1..100).map { index ->
            LaporanSummaryItem(title = "Item $index", value = "Rp $index")
        }

        adapter.setItems(items)
        advanceUntilIdle()

        assertEquals(100, adapter.itemCount)
    }

    @Test
    fun `LaporanSummaryAdapter should update items incrementally`() = runTest {
        val initialItems = listOf(
            LaporanSummaryItem(title = "Item 1", value = "Rp 100")
        )

        adapter.setItems(initialItems)
        advanceUntilIdle()
        assertEquals(1, adapter.itemCount)

        val updatedItems = initialItems + listOf(
            LaporanSummaryItem(title = "Item 2", value = "Rp 200"),
            LaporanSummaryItem(title = "Item 3", value = "Rp 300")
        )

        adapter.setItems(updatedItems)
        advanceUntilIdle()

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `LaporanSummaryAdapter should handle items with unicode characters`() = runTest {
        val items = listOf(
            LaporanSummaryItem(title = "Iuran Warga 🏠", value = "Rp 500.000"),
            LaporanSummaryItem(title = "Pengeluaran 💰", value = "Rp 200.000")
        )

        adapter.setItems(items)
        advanceUntilIdle()

        assertEquals(2, adapter.itemCount)

        val context = RuntimeEnvironment.getApplication()
        val parent = androidx.recyclerview.widget.RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals("Iuran Warga 🏠", viewHolder.tvTitle.text.toString())
    }

    @Test
    fun `LaporanSummaryAdapter should be instance of ListAdapter`() {
        assertTrue(adapter is ListAdapter<*, *>)
    }

    @Test
    fun `LaporanSummaryAdapter ViewHolder should have correct view references`() = runTest {
        val items = listOf(
            LaporanSummaryItem(title = "Test", value = "Rp 100")
        )

        adapter.setItems(items)
        advanceUntilIdle()

        val context = RuntimeEnvironment.getApplication()
        val parent = androidx.recyclerview.widget.RecyclerView(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        assertNotNull(viewHolder.tvTitle)
        assertNotNull(viewHolder.tvValue)
    }
}