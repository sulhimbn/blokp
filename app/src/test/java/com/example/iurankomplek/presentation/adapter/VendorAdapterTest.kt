package com.example.iurankomplek.presentation.adapter

import com.example.iurankomplek.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VendorAdapterTest {

    private val callback = VendorAdapter.VendorDiffCallback()

    @Test
    fun `vendors with the same id are the same item`() {
        assertTrue(
            callback.areItemsTheSame(
                TestFixtures.vendor(id = "7", name = "Old name"),
                TestFixtures.vendor(id = "7", name = "New name")
            )
        )
    }

    @Test
    fun `vendors with different ids are different items`() {
        assertFalse(
            callback.areItemsTheSame(
                TestFixtures.vendor(id = "7"),
                TestFixtures.vendor(id = "8")
            )
        )
    }

    @Test
    fun `a renamed vendor is the same item with different content`() {
        val before = TestFixtures.vendor(id = "7", name = "Old name")

        assertTrue(callback.areItemsTheSame(before, before))
        assertTrue(callback.areContentsTheSame(before, before))
    }

    @Test
    fun `changed fields make the contents different`() {
        assertFalse(
            callback.areContentsTheSame(
                TestFixtures.vendor(id = "7", rating = 4.0),
                TestFixtures.vendor(id = "7", rating = 1.0)
            )
        )
    }

    @Test
    fun `a deactivated vendor is not content-equal to an active one`() {
        assertFalse(
            callback.areContentsTheSame(
                TestFixtures.vendor(id = "7", isActive = true),
                TestFixtures.vendor(id = "7", isActive = false)
            )
        )
    }

    @Test
    fun `the adapter can be constructed with a click listener`() {
        var clicked: String? = null
        val adapter = VendorAdapter { clicked = it.id }

        assertEquals(0, adapter.itemCount)
        assertEquals(null, clicked)
    }
}