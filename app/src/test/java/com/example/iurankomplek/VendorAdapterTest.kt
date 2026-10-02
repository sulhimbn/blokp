package com.example.iurankomplek
import android.app.Application

import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class VendorAdapterTest {

    private fun vendor(
        id: String = "v1",
        name: String = "CV Nusantara",
        phone: String = "081234567890",
        specialty: String = "plumbing",
        rating: Double = 4.5
    ) = Vendor(
        id = id,
        name = name,
        contactPerson = "Budi",
        phoneNumber = phone,
        email = "budi@x.test",
        specialty = specialty,
        address = "Jl. Industri 5",
        licenseNumber = "LIC-1",
        insuranceInfo = "AS-1",
        certifications = listOf("PIPA"),
        rating = rating,
        totalReviews = 10,
        contractStart = "2026-01-01",
        contractEnd = "2026-12-31",
        isActive = true
    )

    @Test
    fun `diff callback matches on vendor id`() {
        val callback = VendorAdapter.VendorDiffCallback()

        assertTrue(callback.areItemsTheSame(vendor(id = "v1"), vendor(id = "v1", name = "Renamed")))
        assertFalse(callback.areItemsTheSame(vendor(id = "v1"), vendor(id = "v2")))
    }

    @Test
    fun `diff callback reports different contents when any field changes`() {
        val callback = VendorAdapter.VendorDiffCallback()

        assertTrue(callback.areContentsTheSame(vendor(), vendor()))
        assertFalse(callback.areContentsTheSame(vendor(), vendor(rating = 3.0)))
        assertFalse(callback.areContentsTheSame(vendor(), vendor(phone = "0800")))
    }

    @Test
    fun `a click callback is required so the adapter cannot be built without one`() {
        val clicked = mutableListOf<Vendor>()

        val adapter = VendorAdapter { clicked += it }
        adapter.submitList(listOf(vendor()))

        assertEquals(1, adapter.itemCount)
        assertEquals("v1", adapter.currentList.first().id)
    }
}
