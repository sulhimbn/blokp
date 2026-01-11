package com.example.iurankomplek.presentation.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BaseListAdapterTest {

    @Mock
    private lateinit var parentViewGroup: ViewGroup

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun baseListAdapter_withDiffCallback_isCreatedSuccessfully() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        assertNotNull(adapter)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun onCreateViewHolder_callsCreateViewHolderInternal() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        val holder = adapter.onCreateViewHolder(parentViewGroup, 0)

        assertNotNull(holder)
        assertTrue(holder is TestViewHolder)
    }

    @Test
    fun onBindViewHolder_callsBindViewHolderInternal() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        val item = TestDataItem(id = 1, name = "Test Item")
        adapter.submitList(listOf(item))

        val holder = adapter.onCreateViewHolder(parentViewGroup, 0)
        adapter.onBindViewHolder(holder, 0)

        val testHolder = holder as TestViewHolder
        assertEquals(item, testHolder.boundItem)
        assertEquals(0, testHolder.boundPosition)
    }

    @Test
    fun submitList_withEmptyList_clearsAdapter() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        adapter.submitList(listOf(TestDataItem(id = 1, name = "Item 1")))
        adapter.submitList(emptyList())

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun submitList_withSingleItem_updatesItemCount() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        adapter.submitList(listOf(TestDataItem(id = 1, name = "Item 1")))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun submitList_withMultipleItems_updatesItemCount() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        val items = listOf(
            TestDataItem(id = 1, name = "Item 1"),
            TestDataItem(id = 2, name = "Item 2"),
            TestDataItem(id = 3, name = "Item 3")
        )
        adapter.submitList(items)

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun submitList_withSameIdButDifferentContent_updatesItem() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        val oldItem = TestDataItem(id = 1, name = "Old Name")
        val newItem = TestDataItem(id = 1, name = "New Name")

        adapter.submitList(listOf(oldItem))
        adapter.submitList(listOf(newItem))

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun getItemAt_returnsCorrectItemAtPosition() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        val items = listOf(
            TestDataItem(id = 1, name = "Item 1"),
            TestDataItem(id = 2, name = "Item 2"),
            TestDataItem(id = 3, name = "Item 3")
        )
        adapter.submitList(items)

        assertEquals(items[0], adapter.getItemAt(0))
        assertEquals(items[1], adapter.getItemAt(1))
        assertEquals(items[2], adapter.getItemAt(2))
    }

    @Test
    fun getItemAt_withEmptyList_throwsIndexOutOfBoundsException() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        try {
            adapter.getItemAt(0)
            fail("Expected IndexOutOfBoundsException")
        } catch (e: IndexOutOfBoundsException) {
            assertTrue(e.message?.contains("Index: 0, Size: 0") == true)
        }
    }

    @Test
    fun getItemAt_withInvalidPosition_throwsIndexOutOfBoundsException() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        adapter.submitList(listOf(TestDataItem(id = 1, name = "Item 1")))

        try {
            adapter.getItemAt(5)
            fail("Expected IndexOutOfBoundsException")
        } catch (e: IndexOutOfBoundsException) {
            assertTrue(e.message?.contains("Index: 5, Size: 1") == true)
        }
    }

    @Test
    fun diffById_createsCorrectDiffCallback() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }

        val item1 = TestDataItem(id = 1, name = "Item 1")
        val item2 = TestDataItem(id = 1, name = "Item 1 - Updated")
        val item3 = TestDataItem(id = 2, name = "Item 2")

        assertTrue(diffCallback.areItemsTheSame(item1, item2))
        assertFalse(diffCallback.areItemsTheSame(item1, item3))
        assertTrue(diffCallback.areContentsTheSame(item1, item2))
        assertFalse(diffCallback.areContentsTheSame(item1, item3))
    }

    @Test
    fun onBindViewHolder_withMultipleItems_bindsEachCorrectly() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        val items = listOf(
            TestDataItem(id = 1, name = "Item 1"),
            TestDataItem(id = 2, name = "Item 2"),
            TestDataItem(id = 3, name = "Item 3")
        )
        adapter.submitList(items)

        for (i in items.indices) {
            val holder = adapter.onCreateViewHolder(parentViewGroup, 0) as TestViewHolder
            adapter.onBindViewHolder(holder, i)
            assertEquals(items[i], holder.boundItem)
            assertEquals(i, holder.boundPosition)
        }
    }

    @Test
    fun submitList_withNull_clearsAdapter() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        adapter.submitList(listOf(TestDataItem(id = 1, name = "Item 1")))

        adapter.submitList(null)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun submitList_withLargeList_updatesItemCount() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        val largeList = (1..100).map { TestDataItem(id = it.toLong(), name = "Item $it") }
        adapter.submitList(largeList)

        assertEquals(100, adapter.itemCount)
    }

    @Test
    fun onCreateViewHolder_withDifferentViewTypes_returnsSameViewHolderType() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        val holder1 = adapter.onCreateViewHolder(parentViewGroup, 0)
        val holder2 = adapter.onCreateViewHolder(parentViewGroup, 1)
        val holder3 = adapter.onCreateViewHolder(parentViewGroup, 2)

        assertTrue(holder1 is TestViewHolder)
        assertTrue(holder2 is TestViewHolder)
        assertTrue(holder3 is TestViewHolder)
    }

    @Test
    fun onBindViewHolder_afterListUpdate_bindsNewItem() {
        val diffCallback = BaseListAdapter.diffById<TestDataItem> { it.id }
        val adapter = TestAdapter(diffCallback)

        val oldItems = listOf(TestDataItem(id = 1, name = "Old Item"))
        val newItems = listOf(TestDataItem(id = 1, name = "New Item"))

        adapter.submitList(oldItems)
        adapter.submitList(newItems)

        val holder = adapter.onCreateViewHolder(parentViewGroup, 0) as TestViewHolder
        adapter.onBindViewHolder(holder, 0)

        assertEquals(newItems[0], holder.boundItem)
    }

    data class TestDataItem(
        val id: Long,
        val name: String
    )

    class TestAdapter(diffCallback: androidx.recyclerview.widget.DiffUtil.ItemCallback<TestDataItem>) :
        BaseListAdapter<TestDataItem, TestViewHolder>(diffCallback) {

        override fun createViewHolderInternal(parent: ViewGroup): TestViewHolder {
            return TestViewHolder()
        }

        override fun bindViewHolderInternal(holder: TestViewHolder, item: TestDataItem) {
            holder.boundItem = item
        }
    }

    class TestViewHolder : RecyclerView.ViewHolder(null) {
        var boundItem: TestDataItem? = null
        var boundPosition: Int = -1

        init {
            super.itemView
        }
    }
}
