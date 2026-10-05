package com.example.iurankomplek

import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Covers [VendorAdapter], which is a [androidx.recyclerview.widget.ListAdapter] and
 * therefore keys identity off the vendor id.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VendorAdapterTest {

    private lateinit var vendorAdapter: VendorAdapter

    @Before
    fun setup() {
        vendorAdapter = VendorAdapter { }
        vendorAdapter.submitList(
            listOf(
                TestFixtures.vendor(
                    id = "1",
                    name = "Plumbing Services Inc",
                    specialty = "plumbing",
                    phoneNumber = "123-456-7890"
                ),
                TestFixtures.vendor(
                    id = "2",
                    name = "Electrical Services Co",
                    specialty = "electrical",
                    phoneNumber = "098-765-4321"
                )
            )
        )
    }

    @Test
    fun `adapter reports the submitted item count`() {
        assertEquals(2, vendorAdapter.itemCount)
    }

    @Test
    fun `adapter exposes the submitted vendors in order`() {
        assertEquals("Plumbing Services Inc", vendorAdapter.currentList[0].name)
        assertEquals("Electrical Services Co", vendorAdapter.currentList[1].name)
    }

    @Test
    fun `diff callback treats the same id as the same item`() {
        val original = TestFixtures.vendor(id = "1", name = "Plumbing Services Inc")
        val renamed = TestFixtures.vendor(id = "1", name = "Plumbing Services Updated")

        val callback = VendorAdapter.VendorDiffCallback()

        assertTrue(callback.areItemsTheSame(original, renamed))
        assertFalse(callback.areContentsTheSame(original, renamed))
    }

    @Test
    fun `diff callback treats different ids as different items`() {
        val plumbing: Vendor = TestFixtures.vendor(id = "1")
        val electrical: Vendor = TestFixtures.vendor(id = "2")

        val callback = VendorAdapter.VendorDiffCallback()

        assertFalse(callback.areItemsTheSame(plumbing, electrical))
    }

    @Test
    fun `diff callback reports equal contents as unchanged`() {
        val vendor = TestFixtures.vendor(id = "1")

        val callback = VendorAdapter.VendorDiffCallback()

        assertTrue(callback.areContentsTheSame(vendor, vendor.copy()))
    }
}