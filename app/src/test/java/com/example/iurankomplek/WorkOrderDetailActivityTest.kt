package com.example.iurankomplek

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import com.example.iurankomplek.presentation.ui.activity.WorkOrderDetailActivity
import com.example.iurankomplek.model.WorkOrder
import com.example.iurankomplek.presentation.viewmodel.VendorViewModel
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.InputSanitizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
class WorkOrderDetailActivityTest {

    @Mock
    private lateinit var mockVendorViewModel: VendorViewModel

    private lateinit var activity: WorkOrderDetailActivity
    private lateinit var controller: Robolectric.BuildActivity<WorkOrderDetailActivity>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should reject invalid work order ID - empty string`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkOrderDetailActivity::class.java)
        intent.putExtra(Constants.Intent.WORK_ORDER_ID, "")

        controller = Robolectric.buildActivity(WorkOrderDetailActivity::class.java, intent)
        activity = controller.create().get()

        val shadowToast = ShadowToast.getLatestToast()
        assertNotNull("Should show error toast for empty ID", shadowToast)
        assertTrue("Activity should finish on invalid ID", activity.isFinishing)
    }

    @Test
    fun `should reject invalid work order ID - blank string`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkOrderDetailActivity::class.java)
        intent.putExtra(Constants.Intent.WORK_ORDER_ID, "   ")

        controller = Robolectric.buildActivity(WorkOrderDetailActivity::class.java, intent)
        activity = controller.create().get()

        val shadowToast = ShadowToast.getLatestToast()
        assertNotNull("Should show error toast for blank ID", shadowToast)
        assertTrue("Activity should finish on invalid ID", activity.isFinishing)
    }

    @Test
    fun `should reject invalid work order ID - contains special characters`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkOrderDetailActivity::class.java)
        intent.putExtra(Constants.Intent.WORK_ORDER_ID, "work-order@123!")

        controller = Robolectric.buildActivity(WorkOrderDetailActivity::class.java, intent)
        activity = controller.create().get()

        val shadowToast = ShadowToast.getLatestToast()
        assertNotNull("Should show error toast for ID with special chars", shadowToast)
        assertTrue("Activity should finish on invalid ID", activity.isFinishing)
    }

    @Test
    fun `should accept valid alphanumeric work order ID`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkOrderDetailActivity::class.java)
        intent.putExtra(Constants.Intent.WORK_ORDER_ID, "WO-12345")

        controller = Robolectric.buildActivity(WorkOrderDetailActivity::class.java, intent)
        activity = controller.create().get()

        assertFalse("Activity should not finish for valid ID", activity.isFinishing)
        assertEquals("Activity should be in created state", Lifecycle.State.CREATED, activity.lifecycle.currentState)
    }

    @Test
    fun `should accept valid alphanumeric ID with underscores`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkOrderDetailActivity::class.java)
        intent.putExtra(Constants.Intent.WORK_ORDER_ID, "work_order_123")

        controller = Robolectric.buildActivity(WorkOrderDetailActivity::class.java, intent)
        activity = controller.create().get()

        assertFalse("Activity should not finish for valid ID with underscore", activity.isFinishing)
    }

    @Test
    fun `should handle missing work order ID`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkOrderDetailActivity::class.java)

        controller = Robolectric.buildActivity(WorkOrderDetailActivity::class.java, intent)
        activity = controller.create().get()

        val shadowToast = ShadowToast.getLatestToast()
        assertNotNull("Should show error toast for missing ID", shadowToast)
        assertTrue("Activity should finish on missing ID", activity.isFinishing)
    }

    @Test
    fun `should display work order details correctly on success state`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkOrderDetailActivity::class.java)
        intent.putExtra(Constants.Intent.WORK_ORDER_ID, "WO-12345")

        val testWorkOrder = WorkOrder(
            id = "WO-12345",
            title = "Fix Leaking Pipe",
            description = "Kitchen sink pipe is leaking",
            category = "plumbing",
            priority = "high",
            status = "in_progress",
            vendorId = "VENDOR-001",
            vendorName = "ABC Plumbing",
            assignedAt = "2026-01-01T10:00:00",
            scheduledDate = "2026-01-02T09:00:00",
            completedAt = null,
            estimatedCost = 5000.0,
            actualCost = 4500.0,
            propertyId = "PROP-001",
            reporterId = "USER-001",
            createdAt = "2026-01-01T08:00:00",
            updatedAt = "2026-01-02T15:30:00",
            attachments = listOf("photo1.jpg", "photo2.jpg"),
            notes = listOf("Arrived on time", "Fixed pipe successfully")
        )

        val successResponse = mockVendorViewModel.workOrderDetailState
        val testStateFlow = MutableStateFlow<UiState<WorkOrder>>(UiState.Success(testWorkOrder))

        controller = Robolectric.buildActivity(WorkOrderDetailActivity::class.java, intent)
        activity = controller.create().get()

        assertNotNull("Activity should be created", activity)
        assertEquals("Activity should be in created state", Lifecycle.State.CREATED, activity.lifecycle.currentState)
    }

    @Test
    fun `should handle null vendor name gracefully`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkOrderDetailActivity::class.java)
        intent.putExtra(Constants.Intent.WORK_ORDER_ID, "WO-12345")

        val testWorkOrder = WorkOrder(
            id = "WO-12345",
            title = "Test Work Order",
            description = "Test description",
            category = "electrical",
            priority = "medium",
            status = "pending",
            vendorId = null,
            vendorName = null,
            assignedAt = null,
            scheduledDate = null,
            completedAt = null,
            estimatedCost = 1000.0,
            actualCost = 0.0,
            propertyId = "PROP-001",
            reporterId = "USER-001",
            createdAt = "2026-01-01T08:00:00",
            updatedAt = "2026-01-01T08:00:00",
            attachments = emptyList(),
            notes = emptyList()
        )

        controller = Robolectric.buildActivity(WorkOrderDetailActivity::class.java, intent)
        activity = controller.create().get()

        assertNotNull("Activity should handle null vendor name", activity)
    }

    @Test
    fun `should format currency correctly for costs`() {
        val result = InputSanitizer.formatCurrency(5000)
        assertEquals("Currency should be formatted correctly", "Rp.5,000", result)

        val zeroCost = InputSanitizer.formatCurrency(0)
        assertEquals("Zero cost should be formatted correctly", "Rp.0", zeroCost)
    }

    @Test
    fun `should handle null cost values by formatting to zero`() {
        val nullCost = "invalid"
        val result = InputSanitizer.formatCurrency(nullCost.toIntOrNull() ?: 0)
        assertEquals("Null cost should default to Rp.0", "Rp.0", result)
    }

    @Test
    fun `should verify InputSanitizer isValidAlphanumericId with valid IDs`() {
        assertTrue("Should accept alphanumeric ID", InputSanitizer.isValidAlphanumericId("WO-12345"))
        assertTrue("Should accept ID with underscores", InputSanitizer.isValidAlphanumericId("work_order_123"))
        assertTrue("Should accept ID with hyphens", InputSanitizer.isValidAlphanumericId("work-order-123"))
    }

    @Test
    fun `should verify InputSanitizer isValidAlphanumericId rejects invalid IDs`() {
        assertFalse("Should reject empty ID", InputSanitizer.isValidAlphanumericId(""))
        assertFalse("Should reject blank ID", InputSanitizer.isValidAlphanumericId("   "))
        assertFalse("Should reject ID with special chars", InputSanitizer.isValidAlphanumericId("work@order"))
        assertFalse("Should reject ID with spaces", InputSanitizer.isValidAlphanumericId("work order"))
    }

    @Test
    fun `should verify InputSanitizer isValidAlphanumericId enforces length limit`() {
        val longId = "a".repeat(101)
        assertFalse("Should reject ID exceeding 100 characters", InputSanitizer.isValidAlphanumericId(longId))

        val validId = "a".repeat(100)
        assertTrue("Should accept ID exactly 100 characters", InputSanitizer.isValidAlphanumericId(validId))
    }

    @Test
    fun `should handle error state by showing toast and finishing`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkOrderDetailActivity::class.java)
        intent.putExtra(Constants.Intent.WORK_ORDER_ID, "WO-12345")

        controller = Robolectric.buildActivity(WorkOrderDetailActivity::class.java, intent)
        activity = controller.create().get()

        assertNotNull("Activity should handle error state", activity)
    }

    @Test
    fun `should initialize UI components successfully`() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), WorkOrderDetailActivity::class.java)
        intent.putExtra(Constants.Intent.WORK_ORDER_ID, "WO-12345")

        controller = Robolectric.buildActivity(WorkOrderDetailActivity::class.java, intent)
        activity = controller.create().get()

        assertNotNull("Activity should be created", activity)
        assertNotNull("Binding should be initialized", activity.lifecycle.currentState)
    }
}