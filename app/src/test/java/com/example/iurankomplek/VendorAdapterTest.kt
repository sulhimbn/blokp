package com.example.iurankomplek

import android.os.Looper
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class VendorAdapterTest {

    private lateinit var vendorAdapter: VendorAdapter

    private fun vendor(
        id: String,
        name: String = "Plumbing Services Inc",
        specialty: String = "plumbing",
        rating: Double = 4.5
    ) = Vendor(
        id = id,
        name = name,
        contactPerson = "John Smith",
        phoneNumber = "123-456-7890",
        email = "contact@plumbing.com",
        specialty = specialty,
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

    private fun submitAndSettle(vendors: List<Vendor>) {
        vendorAdapter.submitList(vendors)
        // AsyncListDiffer computes the diff on a background executor and posts the result to the
        // main looper, so both must be drained before itemCount is meaningful.
        awaitCondition("itemCount == ${vendors.size}") {
            shadowOf(Looper.getMainLooper()).idle()
            vendorAdapter.itemCount == vendors.size
        }
    }

    @Test
    fun `adapter should have correct item count`() {
        submitAndSettle(listOf(vendor("1"), vendor("2")))
        assertEquals(2, vendorAdapter.itemCount)
    }

    @Test
    fun `submitList should replace the previous list`() {
        submitAndSettle(listOf(vendor("1")))
        assertEquals(1, vendorAdapter.itemCount)

        submitAndSettle(listOf(vendor("1"), vendor("2"), vendor("3")))
        assertEquals(3, vendorAdapter.itemCount)
    }

    @Test
    fun `diff callback should identify same items correctly`() {
        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertTrue(diffCallback.areItemsTheSame(vendor("1"), vendor("1", name = "Renamed")))
        assertFalse(diffCallback.areContentsTheSame(vendor("1"), vendor("1", name = "Renamed")))
    }

    @Test
    fun `diff callback should identify different items correctly`() {
        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertFalse(diffCallback.areItemsTheSame(vendor("1"), vendor("2")))
    }

    @Test
    fun `diff callback should treat identical vendors as same contents`() {
        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertTrue(diffCallback.areContentsTheSame(vendor("1"), vendor("1")))
    }
}