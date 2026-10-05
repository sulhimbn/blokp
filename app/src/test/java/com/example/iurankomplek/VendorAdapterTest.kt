package com.example.iurankomplek

import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.app.Application
import android.os.Looper
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class VendorAdapterTest {

    private val clicked = mutableListOf<Vendor>()
    private lateinit var adapter: VendorAdapter

    @Before
    fun setup() {
        clicked.clear()
        adapter = VendorAdapter { vendor -> clicked.add(vendor) }
    }

    private fun awaitItemCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (adapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun vendor(id: String = "v1", name: String = "Vendor 1") = Vendor(
        id = id,
        name = name,
        contactPerson = "John Doe",
        phoneNumber = "1234567890",
        email = "vendor1@example.com",
        specialty = "Cleaning",
        address = "123 Main St",
        licenseNumber = "LIC-1",
        insuranceInfo = "INS-1",
        certifications = listOf("ISO9001"),
        rating = 4.5,
        totalReviews = 10,
        contractStart = "2024-01-01",
        contractEnd = "2024-12-31",
        isActive = true
    )

    private fun submitAndAwait(list: List<Vendor>) {
        adapter.submitList(list)
        awaitItemCount(list.size)
    }

    @Test
    fun `empty adapter reports zero items`() {
        awaitItemCount(0)
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `submitList should surface the submitted vendors`() {
        submitAndAwait(listOf(vendor("v1"), vendor("v2", "Vendor 2")))

        awaitItemCount(2)

        assertEquals(2, adapter.itemCount)
        assertEquals("Vendor 1", adapter.currentList[0].name)
        assertEquals("Vendor 2", adapter.currentList[1].name)
    }

    @Test
    fun `submitList should replace the previous list`() {
        submitAndAwait(listOf(vendor("v1")))
        awaitItemCount(1)
        assertEquals(1, adapter.itemCount)

        submitAndAwait(listOf(vendor("v2"), vendor("v3"), vendor("v4")))

        awaitItemCount(3)

        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `submitList with an empty list should clear the adapter`() {
        submitAndAwait(listOf(vendor()))

        submitAndAwait(emptyList())

        awaitItemCount(0)

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `submiting identical content should not change item count`() {
        submitAndAwait(listOf(vendor("v1"), vendor("v2")))

        submitAndAwait(listOf(vendor("v1"), vendor("v2")))

        awaitItemCount(2)

        assertEquals(2, adapter.itemCount)
    }
}