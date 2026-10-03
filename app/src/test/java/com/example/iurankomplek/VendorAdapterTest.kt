package com.example.iurankomplek

import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Rule
import org.junit.Test

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VendorAdapterTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var vendorAdapter: VendorAdapter

    @Before
    fun setup() {
        vendorAdapter = VendorAdapter { /* Handle vendor click */ }
    }

    @Test
    fun `adapter should start empty`() {
        assertEquals(0, vendorAdapter.itemCount)
    }

    @Test
    fun `submitList should expose the submitted vendors through itemCount`() {
        vendorAdapter.submitList(
            listOf(
                TestFixtures.vendor(id = "1", name = "Plumbing Services Inc"),
                TestFixtures.vendor(
                    id = "2",
                    name = "Electrical Services Co",
                    contactPerson = "Jane Doe",
                    phoneNumber = "098-765-4321",
                    email = "info@electrical.com",
                    specialty = "electrical",
                    address = "456 Oak Ave",
                    licenseNumber = "EL-67890",
                    rating = 4.2,
                    totalReviews = 18
                )
            )
        )

        assertEquals(2, awaitItemCount(vendorAdapter, expected = 2))
    }

    @Test
    fun `diff callback should identify same items correctly`() {
        val vendor1 = TestFixtures.vendor(id = "1", name = "Plumbing Services Inc")
        // Same ID, different content.
        val vendor2 = TestFixtures.vendor(id = "1", name = "Plumbing Services Updated")

        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertTrue(diffCallback.areItemsTheSame(vendor1, vendor2))
        assertFalse(diffCallback.areContentsTheSame(vendor1, vendor2))
    }

    @Test
    fun `diff callback should identify different items correctly`() {
        val vendor1 = TestFixtures.vendor(id = "1", name = "Plumbing Services Inc")
        val vendor2 = TestFixtures.vendor(
            id = "2",
            name = "Electrical Services Co",
            contactPerson = "Jane Doe",
            phoneNumber = "098-765-4321",
            email = "info@electrical.com",
            specialty = "electrical",
            address = "456 Oak Ave",
            licenseNumber = "EL-67890",
            rating = 4.2,
            totalReviews = 18
        )

        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertFalse(diffCallback.areItemsTheSame(vendor1, vendor2))
    }

    @Test
    fun `diff callback should treat identical vendors as same and equal`() {
        val vendor = TestFixtures.vendor(id = "1")
        val sameVendor = TestFixtures.vendor(id = "1")

        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertTrue(diffCallback.areItemsTheSame(vendor, sameVendor))
        assertTrue(diffCallback.areContentsTheSame(vendor, sameVendor))
    }

    @Test
    fun `Vendor fixture exposes the specialty rendered by the adapter`() {
        // Guards the fixture itself: the adapter binds specialty to vendorSpecialty.
        val vendor: Vendor = TestFixtures.vendor(specialty = "landscaping")
        assertEquals("landscaping", vendor.specialty)
    }

    /**
     * ListAdapter computes diffs on a background thread, so itemCount is not updated
     * synchronously after submitList. Poll instead of assuming it already settled.
     */
    private fun awaitItemCount(adapter: VendorAdapter, expected: Int, timeoutMs: Long = 5_000): Int {
        val deadline = System.currentTimeMillis() + timeoutMs
        var count = adapter.itemCount
        while (count != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
            count = adapter.itemCount
        }
        return count
    }
}
