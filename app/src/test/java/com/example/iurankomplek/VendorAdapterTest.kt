package com.example.iurankomplek

import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.app.Application
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class VendorAdapterTest {

    private lateinit var vendorAdapter: VendorAdapter

    private fun vendor(
        id: String = "1",
        name: String = "Plumbing Services Inc",
        rating: Double = 4.5
    ) = Vendor(
        id = id,
        name = name,
        contactPerson = "John Smith",
        phoneNumber = "123-456-7890",
        email = "contact@plumbing.com",
        specialty = "plumbing",
        address = "123 Main St",
        licenseNumber = "PL-12345",
        insuranceInfo = "General liability coverage",
        certifications = listOf("Licensed", "Bonded"),
        rating = rating,
        totalReviews = 25,
        contractStart = "2023-01-01",
        contractEnd = "2024-12-31",
        isActive = true
    )

    @Before
    fun setup() {
        vendorAdapter = VendorAdapter { }
    }

    @Test
    fun `adapter should have correct item count`() {
        vendorAdapter.submitList(listOf(vendor(id = "1"), vendor(id = "2")))
        shadowOfMainLooper().idle()

        assertEquals(2, vendorAdapter.itemCount)
    }

    @Test
    fun `adapter should report zero items before a list is submitted`() {
        assertEquals(0, vendorAdapter.itemCount)
    }

    @Test
    fun `diff callback should identify same items correctly`() {
        val diff = VendorAdapter.VendorDiffCallback()

        assertTrue(diff.areItemsTheSame(vendor(id = "1"), vendor(id = "1", name = "Renamed")))
        assertFalse(diff.areContentsTheSame(vendor(id = "1"), vendor(id = "1", name = "Renamed")))
    }

    @Test
    fun `diff callback should identify different items correctly`() {
        val diff = VendorAdapter.VendorDiffCallback()

        assertFalse(diff.areItemsTheSame(vendor(id = "1"), vendor(id = "2")))
    }

    @Test
    fun `diff callback should report identical vendors as same content`() {
        val diff = VendorAdapter.VendorDiffCallback()

        assertTrue(diff.areContentsTheSame(vendor(id = "1"), vendor(id = "1")))
    }

    @Test
    fun `diff callback should treat a rating change as a content change`() {
        val diff = VendorAdapter.VendorDiffCallback()

        assertTrue(diff.areItemsTheSame(vendor(id = "1", rating = 4.5), vendor(id = "1", rating = 3.1)))
        assertFalse(diff.areContentsTheSame(vendor(id = "1", rating = 4.5), vendor(id = "1", rating = 3.1)))
    }

    private fun shadowOfMainLooper() =
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
}
