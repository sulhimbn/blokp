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

// ListAdapter/AsyncListDiffer need a real Looper, so this must run on Robolectric.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = android.app.Application::class)
class VendorAdapterTest {

    private lateinit var vendorAdapter: VendorAdapter

    @Before
    fun setup() {
        vendorAdapter = VendorAdapter { }
        vendorAdapter.submitList(listOf(plumbingVendor(), electricalVendor()))
    }

    private fun plumbingVendor() = Vendor(
        id = "1",
        name = "Plumbing Services Inc",
        contactPerson = "John Smith",
        phoneNumber = "123-456-7890",
        email = "contact@plumbing.com",
        specialty = "plumbing",
        address = "123 Main St",
        licenseNumber = "PL-12345",
        insuranceInfo = "General liability coverage",
        certifications = listOf("Licensed", "Bonded"),
        rating = 4.5,
        totalReviews = 25,
        contractStart = "2023-01-01",
        contractEnd = "2024-12-31",
        isActive = true
    )

    private fun electricalVendor() = Vendor(
        id = "2",
        name = "Electrical Services Co",
        contactPerson = "Jane Doe",
        phoneNumber = "098-765-4321",
        email = "info@electrical.com",
        specialty = "electrical",
        address = "456 Oak Ave",
        licenseNumber = "EL-67890",
        insuranceInfo = "Professional liability",
        certifications = listOf("Certified Electrician"),
        rating = 4.2,
        totalReviews = 18,
        contractStart = "2023-02-01",
        contractEnd = "2025-01-31",
        isActive = true
    )

    @Test
    fun `adapter should have correct item count`() {
        assertEquals(2, vendorAdapter.itemCount)
    }

    @Test
    fun `diff callback should identify same items correctly`() {
        val vendor1 = plumbingVendor()
        val vendor2 = plumbingVendor().copy(name = "Plumbing Services Updated")

        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertTrue(diffCallback.areItemsTheSame(vendor1, vendor2))
        assertFalse(diffCallback.areContentsTheSame(vendor1, vendor2))
    }

    @Test
    fun `diff callback should identify different items correctly`() {
        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertFalse(diffCallback.areItemsTheSame(plumbingVendor(), electricalVendor()))
    }

    @Test
    fun `diff callback should treat identical vendors as unchanged`() {
        val diffCallback = VendorAdapter.VendorDiffCallback()

        assertTrue(diffCallback.areItemsTheSame(plumbingVendor(), plumbingVendor()))
        assertTrue(diffCallback.areContentsTheSame(plumbingVendor(), plumbingVendor()))
    }
}