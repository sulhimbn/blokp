package com.example.iurankomplek

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.Vendor
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class VendorAdapterTest {
    
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
        
        vendorAdapter = VendorAdapter { /* Handle vendor click */ }
        vendorAdapter.submitList(vendors)
    }
    
@Test
    fun `adapter should have correct item count`() {
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