package com.example.iurankomplek

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.presentation.adapter.VendorAdapter

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*
import org.junit.Rule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VendorAdapterTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun awaitItemCount(expected: Int) {
        val deadline = System.currentTimeMillis() + 5_000
        while (vendorAdapter.itemCount != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        }
    }
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockView: View
    
    private lateinit var vendorAdapter: VendorAdapter
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        val vendors = listOf(
            Vendor(
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
            ),
            Vendor(
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
        )
        
        vendorAdapter = VendorAdapter { }
        vendorAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() = Unit
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = Unit
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = Unit
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = Unit
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = Unit
        })
        vendorAdapter.submitList(vendors)
    }
    
@Test
    fun `adapter should have correct item count`() {
        awaitItemCount(2)
        assertEquals(2, vendorAdapter.itemCount)
    }
    
    
    
    @Test
    fun `diff callback should identify same items correctly`() {
        val vendor1 = Vendor(
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
        
        val vendor2 = Vendor(
            id = "1",  // Same ID
            name = "Plumbing Services Updated",  // Different name
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
        
        val diffCallback = VendorAdapter.VendorDiffCallback()
        
        assertTrue(diffCallback.areItemsTheSame(vendor1, vendor2))  // Same ID
        assertFalse(diffCallback.areContentsTheSame(vendor1, vendor2))  // Different content
    }
    
    @Test
    fun `diff callback should identify different items correctly`() {
        val vendor1 = Vendor(
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
        
        val vendor2 = Vendor(
            id = "2",  // Different ID
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
        
        val diffCallback = VendorAdapter.VendorDiffCallback()
        
        assertFalse(diffCallback.areItemsTheSame(vendor1, vendor2))  // Different IDs
    }
}