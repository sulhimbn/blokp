package com.example.iurankomplek

import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class VendorAdapterTest {

    private val recyclerView = RecyclerView(org.robolectric.RuntimeEnvironment.getApplication())
    private lateinit var vendorAdapter: VendorAdapter

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        
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
        recyclerView.adapter = vendorAdapter
        vendorAdapter.submitList(vendors)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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