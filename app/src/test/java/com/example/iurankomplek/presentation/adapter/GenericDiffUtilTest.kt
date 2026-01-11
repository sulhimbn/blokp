package com.example.iurankomplek.presentation.adapter

import org.junit.Assert.*
import org.junit.Test

data class TestItem(val id: Long, val name: String, val value: Int)

class GenericDiffUtilTest {

    @Test
    fun `byId should compare items by id`() {
        val diffUtil = GenericDiffUtil.byId<TestItem> { it.id }

        val item1 = TestItem(id = 1, name = "Test", value = 100)
        val item2 = TestItem(id = 1, name = "Test", value = 100)
        val item3 = TestItem(id = 2, name = "Test", value = 100)

        assertTrue("Items with same ID should be the same", diffUtil.areItemsTheSame(item1, item2))
        assertFalse("Items with different ID should not be the same", diffUtil.areItemsTheSame(item1, item3))
    }

    @Test
    fun `byId should compare contents by equality`() {
        val diffUtil = GenericDiffUtil.byId<TestItem> { it.id }

        val item1 = TestItem(id = 1, name = "Test", value = 100)
        val item2 = TestItem(id = 1, name = "Test", value = 100)
        val item3 = TestItem(id = 1, name = "Different", value = 100)
        val item4 = TestItem(id = 1, name = "Test", value = 200)

        assertTrue("Identical items should have same content", diffUtil.areContentsTheSame(item1, item2))
        assertFalse("Items with different name should have different content", diffUtil.areContentsTheSame(item1, item3))
        assertFalse("Items with different value should have different content", diffUtil.areContentsTheSame(item1, item4))
    }

    @Test
    fun `byId should handle null selector values`() {
        val diffUtil = GenericDiffUtil.byId<TestItem> { it.name }

        val item1 = TestItem(id = 1, name = null, value = 100)
        val item2 = TestItem(id = 2, name = null, value = 200)
        val item3 = TestItem(id = 3, name = "Test", value = 100)

        assertTrue("Items with same null selector value should be the same", diffUtil.areItemsTheSame(item1, item2))
        assertFalse("Items with different selector values should not be the same", diffUtil.areItemsTheSame(item1, item3))
    }

    @Test
    fun `constructor with custom callbacks should work`() {
        val diffUtil = GenericDiffUtil<TestItem>(
            areItemsTheSameCallback = { old, new -> old.value == new.value },
            areContentsTheSameCallback = { old, new -> old == new }
        )

        val item1 = TestItem(id = 1, name = "Test1", value = 100)
        val item2 = TestItem(id = 2, name = "Test2", value = 100)
        val item3 = TestItem(id = 3, name = "Test3", value = 200)

        assertTrue("Custom callback should use value for item comparison", diffUtil.areItemsTheSame(item1, item2))
        assertFalse("Items with different value should not be the same", diffUtil.areItemsTheSame(item1, item3))
    }

    @Test
    fun `custom callback should respect areContentsTheSame`() {
        val diffUtil = GenericDiffUtil<TestItem>(
            areItemsTheSameCallback = { old, new -> old.id == new.id },
            areContentsTheSameCallback = { old, new -> old.value == new.value }
        )

        val item1 = TestItem(id = 1, name = "Test1", value = 100)
        val item2 = TestItem(id = 1, name = "Test2", value = 100)
        val item3 = TestItem(id = 1, name = "Test3", value = 200)

        assertTrue("Same value should have same content", diffUtil.areContentsTheSame(item1, item2))
        assertFalse("Different value should have different content", diffUtil.areContentsTheSame(item1, item3))
    }

    @Test
    fun `byId should work with string selector`() {
        val diffUtil = GenericDiffUtil.byId<TestItem> { it.name }

        val item1 = TestItem(id = 1, name = "SameName", value = 100)
        val item2 = TestItem(id = 2, name = "SameName", value = 200)
        val item3 = TestItem(id = 3, name = "DifferentName", value = 100)

        assertTrue("Items with same name should be the same", diffUtil.areItemsTheSame(item1, item2))
        assertFalse("Items with different names should not be the same", diffUtil.areItemsTheSame(item1, item3))
    }
}
