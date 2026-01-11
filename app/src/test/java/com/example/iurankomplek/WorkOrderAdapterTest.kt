package com.example.iurankomplek

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.model.WorkOrder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class WorkOrderAdapterTest {

    @get:Rule
    @Suppress("unused")
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var inflater: LayoutInflater

    @Mock
    private lateinit var parent: RecyclerView.ViewHolder

    @Mock
    private lateinit var mockView: View

    private lateinit var adapter: WorkOrderAdapter
    private var clickCallbackInvoked = false
    private var clickedWorkOrder: WorkOrder? = null

    @Before
    fun setup() {
        val onWorkOrderClick: (WorkOrder) -> Unit = { workOrder ->
            clickCallbackInvoked = true
            clickedWorkOrder = workOrder
        }
        adapter = WorkOrderAdapter(onWorkOrderClick)
        clickCallbackInvoked = false
        clickedWorkOrder = null
    }

    @Test
    fun `submitList should update adapter data correctly`() {
        val workOrders = listOf(
            createWorkOrder(id = "1", title = "Test Work Order")
        )

        adapter.submitList(workOrders)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList with empty list should clear adapter`() {
        val workOrders = listOf(
            createWorkOrder(id = "1", title = "Test")
        )
        adapter.submitList(workOrders)
        advanceExecutor()

        adapter.submitList(emptyList())
        advanceExecutor()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `submitList should handle single work order`() {
        val workOrder = createWorkOrder(id = "1", title = "Single Work Order")

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle low priority work orders`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "Low Priority",
            priority = "low"
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle medium priority work orders`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "Medium Priority",
            priority = "medium"
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle high priority work orders`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "High Priority",
            priority = "high"
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle urgent priority work orders`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "Urgent Priority",
            priority = "urgent"
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle all priority levels`() {
        val workOrders = listOf(
            createWorkOrder(id = "1", title = "Low", priority = "low"),
            createWorkOrder(id = "2", title = "Medium", priority = "medium"),
            createWorkOrder(id = "3", title = "High", priority = "high"),
            createWorkOrder(id = "4", title = "Urgent", priority = "urgent")
        )

        adapter.submitList(workOrders)
        advanceExecutor()

        assertEquals(4, adapter.itemCount)
    }

    @Test
    fun `submitList should handle pending status`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "Pending",
            status = "pending"
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle assigned status`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "Assigned",
            status = "assigned"
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle in_progress status`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "In Progress",
            status = "in_progress"
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle completed status`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "Completed",
            status = "completed"
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle cancelled status`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "Cancelled",
            status = "cancelled"
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle all status types`() {
        val workOrders = listOf(
            createWorkOrder(id = "1", title = "Pending", status = "pending"),
            createWorkOrder(id = "2", title = "Assigned", status = "assigned"),
            createWorkOrder(id = "3", title = "In Progress", status = "in_progress"),
            createWorkOrder(id = "4", title = "Completed", status = "completed"),
            createWorkOrder(id = "5", title = "Cancelled", status = "cancelled")
        )

        adapter.submitList(workOrders)
        advanceExecutor()

        assertEquals(5, adapter.itemCount)
    }

    @Test
    fun `submitList should handle work order with vendor`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "With Vendor",
            vendorId = "vendor1",
            vendorName = "Plumbing Co."
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle work order without vendor`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "Without Vendor",
            vendorId = null,
            vendorName = null
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle different categories`() {
        val workOrders = listOf(
            createWorkOrder(id = "1", title = "Plumbing", category = "Plumbing"),
            createWorkOrder(id = "2", title = "Electrical", category = "Electrical"),
            createWorkOrder(id = "3", title = "HVAC", category = "HVAC"),
            createWorkOrder(id = "4", title = "Roofing", category = "Roofing"),
            createWorkOrder(id = "5", title = "General", category = "General")
        )

        adapter.submitList(workOrders)
        advanceExecutor()

        assertEquals(5, adapter.itemCount)
    }

    @Test
    fun `submitList should handle work order with cost`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "With Cost",
            estimatedCost = 500.0,
            actualCost = 450.0
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle work order with zero cost`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "Zero Cost",
            estimatedCost = 0.0,
            actualCost = 0.0
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle work order with attachments`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "With Attachments",
            attachments = listOf("photo1.jpg", "document.pdf")
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle work order with notes`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "With Notes",
            notes = listOf("Note 1", "Note 2")
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle work order with long description`() {
        val longDescription = "A".repeat(1000)
        val workOrder = createWorkOrder(
            id = "1",
            title = "Long Description",
            description = longDescription
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle work order with special characters`() {
        val workOrder = createWorkOrder(
            id = "1",
            title = "Special Ch@rs & Émojis! 🎉",
            description = "Description with spëcial çh@racters"
        )

        adapter.submitList(listOf(workOrder))
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle null list`() {
        adapter.submitList(null)
        advanceExecutor()

        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `submitList should handle data update`() {
        val initialWorkOrders = listOf(
            createWorkOrder(id = "1", title = "Initial", description = "Initial desc")
        )
        val updatedWorkOrders = listOf(
            createWorkOrder(id = "1", title = "Updated", description = "Updated desc")
        )

        adapter.submitList(initialWorkOrders)
        advanceExecutor()
        assertEquals(1, adapter.itemCount)

        adapter.submitList(updatedWorkOrders)
        advanceExecutor()
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `submitList should handle large list`() {
        val workOrders = (1..100).map { id ->
            createWorkOrder(
                id = id.toString(),
                title = "Work Order $id",
                description = "Description $id"
            )
        }

        adapter.submitList(workOrders)
        advanceExecutor()

        assertEquals(100, adapter.itemCount)
    }

    @Test
    fun `clickCallback should be invoked when item is clicked`() {
        val workOrder = createWorkOrder(id = "1", title = "Clickable")
        val onWorkOrderClick: (WorkOrder) -> Unit = { clicked ->
            clickCallbackInvoked = true
            clickedWorkOrder = clicked
        }
        
        val clickAdapter = WorkOrderAdapter(onWorkOrderClick)
        clickAdapter.submitList(listOf(workOrder))
        advanceExecutor()

        clickAdapter.onBindViewHolder(
            clickAdapter.onCreateViewHolder(mockView, 0),
            0
        )

        assertTrue(clickCallbackInvoked)
        assertEquals(workOrder, clickedWorkOrder)
    }

    @Test
    fun `DiffCallback should identify items with same ID as same item`() {
        val workOrders1 = listOf(
            createWorkOrder(id = "1", title = "Work Order 1", status = "pending")
        )
        val workOrders2 = listOf(
            createWorkOrder(id = "1", title = "Work Order 1 Updated", status = "completed")
        )

        adapter.submitList(workOrders1)
        advanceExecutor()
        adapter.submitList(workOrders2)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `DiffCallback should identify items with different ID as different items`() {
        val workOrders1 = listOf(
            createWorkOrder(id = "1", title = "Work Order 1", status = "pending")
        )
        val workOrders2 = listOf(
            createWorkOrder(id = "2", title = "Work Order 2", status = "pending")
        )

        adapter.submitList(workOrders1)
        advanceExecutor()
        adapter.submitList(workOrders2)
        advanceExecutor()

        assertEquals(1, adapter.itemCount)
    }

    private fun createWorkOrder(
        id: String,
        title: String,
        description: String = "Description",
        category: String = "General",
        priority: String = "medium",
        status: String = "pending",
        vendorId: String? = null,
        vendorName: String? = null,
        estimatedCost: Double = 0.0,
        actualCost: Double = 0.0,
        attachments: List<String> = emptyList(),
        notes: List<String> = emptyList()
    ) = WorkOrder(
        id = id,
        title = title,
        description = description,
        category = category,
        priority = priority,
        status = status,
        vendorId = vendorId,
        vendorName = vendorName,
        assignedAt = null,
        scheduledDate = null,
        completedAt = null,
        estimatedCost = estimatedCost,
        actualCost = actualCost,
        propertyId = "prop1",
        reporterId = "user1",
        createdAt = "2024-01-01T10:00:00Z",
        updatedAt = "2024-01-01T10:00:00Z",
        attachments = attachments,
        notes = notes
    )

    private fun advanceExecutor() {
        Thread.sleep(50)
    }
}
