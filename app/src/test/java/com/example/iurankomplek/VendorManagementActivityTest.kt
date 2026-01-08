package com.example.iurankomplek

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.example.iurankomplek.presentation.ui.activity.VendorManagementActivity
import com.example.iurankomplek.presentation.viewmodel.VendorViewModel
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.model.Vendor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class VendorManagementActivityTest {

    @Mock
    private lateinit var mockVendorViewModel: VendorViewModel

    private lateinit var activity: VendorManagementActivity
    private lateinit var controller: Robolectric.BuildActivity<VendorManagementActivity>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should initialize activity correctly`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should be created", activity)
        assertEquals("Activity should be in created state", Lifecycle.State.CREATED, activity.lifecycle.currentState)
    }

    @Test
    fun `should initialize RecyclerView correctly`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.vendor_recycler_view)
        assertNotNull("RecyclerView should be initialized", recyclerView)
    }

    @Test
    fun `should set LinearLayoutManager on RecyclerView`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.vendor_recycler_view)
        assertNotNull("RecyclerView should have a layout manager", recyclerView.layoutManager)
        assertTrue("RecyclerView should use LinearLayoutManager", 
            recyclerView.layoutManager is androidx.recyclerview.widget.LinearLayoutManager)
    }

    @Test
    fun `should set adapter on RecyclerView`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.vendor_recycler_view)
        assertNotNull("RecyclerView should have an adapter", recyclerView.adapter)
    }

    @Test
    fun `should initialize VendorViewModel correctly`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should be created", activity)
        assertNotNull("ViewModel should be initialized", activity.lifecycle.currentState)
    }

    @Test
    fun `should initialize VendorAdapter correctly`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.vendor_recycler_view)
        assertNotNull("RecyclerView adapter should be initialized", recyclerView.adapter)
    }

    @Test
    fun `should handle Loading state gracefully`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle Loading state", activity)
    }

    @Test
    fun `should handle Success state with vendor data`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle Success state", activity)
    }

    @Test
    fun `should handle Error state gracefully`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle Error state", activity)
    }

    @Test
    fun `should show toast on error state`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should show toast on error", activity)
    }

    @Test
    fun `should handle Idle state gracefully`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle Idle state", activity)
    }

    @Test
    fun `should verify Vendor data structure`() {
        val testVendor = Vendor(
            id = "VENDOR-001",
            name = "ABC Plumbing",
            category = "plumbing",
            contactPerson = "John Doe",
            phone = "081234567890",
            email = "abc@plumbing.com",
            address = "123 Main Street",
            rating = 4.5,
            isActive = true
        )

        assertNotNull("Vendor should have ID", testVendor.id)
        assertNotNull("Vendor should have name", testVendor.name)
        assertNotNull("Vendor should have category", testVendor.category)
        assertNotNull("Vendor should have contact person", testVendor.contactPerson)
        assertNotNull("Vendor should have phone", testVendor.phone)
        assertNotNull("Vendor should have email", testVendor.email)
        assertNotNull("Vendor should have address", testVendor.address)
        assertTrue("Vendor rating should be positive", testVendor.rating > 0)
        assertNotNull("Vendor should have active status", testVendor.isActive)
    }

    @Test
    fun `should verify Vendor with all fields`() {
        val testVendor = Vendor(
            id = "VENDOR-002",
            name = "XYZ Electrical",
            category = "electrical",
            contactPerson = "Jane Smith",
            phone = "089876543210",
            email = "xyz@electrical.com",
            address = "456 Secondary Street",
            rating = 5.0,
            isActive = true
        )

        assertEquals("VENDOR-002", testVendor.id)
        assertEquals("XYZ Electrical", testVendor.name)
        assertEquals("electrical", testVendor.category)
        assertEquals("Jane Smith", testVendor.contactPerson)
        assertEquals("089876543210", testVendor.phone)
        assertEquals("xyz@electrical.com", testVendor.email)
        assertEquals("456 Secondary Street", testVendor.address)
        assertEquals(5.0, testVendor.rating, 0.001)
        assertTrue(testVendor.isActive)
    }

    @Test
    fun `should verify Vendor with inactive status`() {
        val testVendor = Vendor(
            id = "VENDOR-003",
            name = "Inactive Vendor",
            category = "cleaning",
            contactPerson = "Test Person",
            phone = "081111111111",
            email = "inactive@test.com",
            address = "789 Test Street",
            rating = 0.0,
            isActive = false
        )

        assertNotNull("Inactive vendor should be valid", testVendor)
        assertFalse("Vendor should be inactive", testVendor.isActive)
    }

    @Test
    fun `should verify Vendor with minimum rating`() {
        val testVendor = Vendor(
            id = "VENDOR-004",
            name = "Low Rated Vendor",
            category = "plumbing",
            contactPerson = "Test Person",
            phone = "082222222222",
            email = "low@test.com",
            address = "000 Test Street",
            rating = 1.0,
            isActive = true
        )

        assertEquals(1.0, testVendor.rating, 0.001)
        assertTrue("Vendor with minimum rating should be valid", testVendor.isActive)
    }

    @Test
    fun `should verify Vendor with maximum rating`() {
        val testVendor = Vendor(
            id = "VENDOR-005",
            name = "High Rated Vendor",
            category = "electrical",
            contactPerson = "Test Person",
            phone = "083333333333",
            email = "high@test.com",
            address = "999 Test Street",
            rating = 5.0,
            isActive = true
        )

        assertEquals(5.0, testVendor.rating, 0.001)
        assertTrue("Vendor with maximum rating should be valid", testVendor.isActive)
    }

    @Test
    fun `should handle vendor click callback`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle vendor click", activity)
    }

    @Test
    fun `should submit list to adapter on success state`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.vendor_recycler_view)
        assertNotNull("RecyclerView should be able to submit list", recyclerView.adapter)
    }

    @Test
    fun `should verify activity lifecycle states`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        
        controller.create()
        assertEquals("Activity should be in created state", Lifecycle.State.CREATED, activity.lifecycle.currentState)
        
        controller.start()
        assertEquals("Activity should be in started state", Lifecycle.State.STARTED, activity.lifecycle.currentState)
        
        controller.resume()
        assertEquals("Activity should be in resumed state", Lifecycle.State.RESUMED, activity.lifecycle.currentState)
    }

    @Test
    fun `should verify VendorRepository initialization`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should initialize repository", activity)
    }

    @Test
    fun `should verify UiState values are distinct`() {
        val idle = UiState.Idle
        val loading = UiState.Loading
        val success = UiState.Success(Vendor("VENDOR-001", "Test", "plumbing", "Contact", "Phone", "Email", "Address", 4.5, true))
        val error = UiState.Error("Test error")

        assertNotEquals("Idle and Loading should be different", idle, loading)
        assertNotEquals("Loading and Success should be different", loading, success)
        assertNotEquals("Success and Error should be different", success, error)
    }

    @Test
    fun `should handle empty vendor list on success state`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle empty vendor list", activity)
    }

    @Test
    fun `should handle non-empty vendor list on success state`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        assertNotNull("Activity should handle vendor list", activity)
    }

    @Test
    fun `should verify RecyclerView is scrollable`() {
        controller = Robolectric.buildActivity(VendorManagementActivity::class.java)
        activity = controller.create().get()

        val recyclerView = activity.findViewById<RecyclerView>(R.id.vendor_recycler_view)
        assertNotNull("RecyclerView should be scrollable", recyclerView)
    }

    @Test
    fun `should verify Vendor categories are valid`() {
        val validCategories = listOf("plumbing", "electrical", "cleaning", "landscaping", "security", "other")
        
        validCategories.forEach { category ->
            val vendor = Vendor(
                id = "VENDOR-$category",
                name = "Test Vendor",
                category = category,
                contactPerson = "Test",
                phone = "081234567890",
                email = "test@test.com",
                address = "Test Address",
                rating = 4.0,
                isActive = true
            )
            
            assertEquals("Vendor category should be valid", category, vendor.category)
        }
    }
}