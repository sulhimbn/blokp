package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.WorkOrder
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.util.Date

class LoadWorkOrdersUseCaseTest {

    @Mock
    private lateinit var mockRepository: VendorRepository

    private lateinit var useCase: LoadWorkOrdersUseCase

    private val testWorkOrder = WorkOrder(
        id = "wo1",
        title = "Fix Leaky Faucet",
        description = "Kitchen faucet is leaking badly",
        category = "plumbing",
        priority = "high",
        status = "pending",
        vendorId = null,
        vendorName = null,
        assignedAt = null,
        scheduledDate = null,
        completedAt = null,
        estimatedCost = 150.0,
        actualCost = 0.0,
        propertyId = "prop123",
        reporterId = "user456",
        createdAt = "2024-01-15T10:30:00Z",
        updatedAt = "2024-01-15T10:30:00Z",
        attachments = emptyList(),
        notes = emptyList()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadWorkOrdersUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val expectedResponse = WorkOrderResponse(data = listOf(testWorkOrder))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with empty list`() = runTest {
        val expectedResponse = WorkOrderResponse(data = emptyList())
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        assertEquals(0, result.getOrNull()?.data?.size)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with multiple work orders`() = runTest {
        val workOrder2 = testWorkOrder.copy(
            id = "wo2",
            title = "Repair Electrical Outlet",
            category = "electrical",
            priority = "medium"
        )
        val workOrder3 = testWorkOrder.copy(
            id = "wo3",
            title = "Trim Bushes",
            category = "landscaping",
            priority = "low"
        )
        val expectedResponse = WorkOrderResponse(data = listOf(testWorkOrder, workOrder2, workOrder3))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.data?.size)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with work orders of different statuses`() = runTest {
        val pendingOrder = testWorkOrder.copy(id = "wo1", status = "pending")
        val assignedOrder = testWorkOrder.copy(id = "wo2", status = "assigned", vendorId = "v1", vendorName = "ABC Plumbing")
        val inProgressOrder = testWorkOrder.copy(id = "wo3", status = "in_progress", vendorId = "v2", vendorName = "XYZ Electrical")
        val completedOrder = testWorkOrder.copy(id = "wo4", status = "completed", vendorId = "v1", vendorName = "ABC Plumbing", completedAt = "2024-01-20T15:00:00Z")
        val cancelledOrder = testWorkOrder.copy(id = "wo5", status = "cancelled")

        val expectedResponse = WorkOrderResponse(data = listOf(pendingOrder, assignedOrder, inProgressOrder, completedOrder, cancelledOrder))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()

        assertTrue(result.isSuccess)
        val workOrders = result.getOrNull()?.data
        assertEquals(5, workOrders?.size)
        assertEquals("pending", workOrders?.get(0)?.status)
        assertEquals("assigned", workOrders?.get(1)?.status)
        assertEquals("in_progress", workOrders?.get(2)?.status)
        assertEquals("completed", workOrders?.get(3)?.status)
        assertEquals("cancelled", workOrders?.get(4)?.status)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with work orders of different priorities`() = runTest {
        val urgentOrder = testWorkOrder.copy(id = "wo1", priority = "urgent", title = "Emergency Repair")
        val highOrder = testWorkOrder.copy(id = "wo2", priority = "high", title = "Major Issue")
        val mediumOrder = testWorkOrder.copy(id = "wo3", priority = "medium", title = "Routine Maintenance")
        val lowOrder = testWorkOrder.copy(id = "wo4", priority = "low", title = "Minor Issue")

        val expectedResponse = WorkOrderResponse(data = listOf(urgentOrder, highOrder, mediumOrder, lowOrder))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()

        assertTrue(result.isSuccess)
        val workOrders = result.getOrNull()?.data
        assertEquals(4, workOrders?.size)
        assertEquals("urgent", workOrders?.get(0)?.priority)
        assertEquals("high", workOrders?.get(1)?.priority)
        assertEquals("medium", workOrders?.get(2)?.priority)
        assertEquals("low", workOrders?.get(3)?.priority)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with work orders of different categories`() = runTest {
        val plumbingOrder = testWorkOrder.copy(id = "wo1", category = "plumbing", title = "Fix Leak")
        val electricalOrder = testWorkOrder.copy(id = "wo2", category = "electrical", title = "Repair Outlet")
        val landscapingOrder = testWorkOrder.copy(id = "wo3", category = "landscaping", title = "Trim Trees")
        val hvacOrder = testWorkOrder.copy(id = "wo4", category = "hvac", title = "AC Repair")

        val expectedResponse = WorkOrderResponse(data = listOf(plumbingOrder, electricalOrder, landscapingOrder, hvacOrder))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()

        assertTrue(result.isSuccess)
        val workOrders = result.getOrNull()?.data
        assertEquals(4, workOrders?.size)
        assertEquals("plumbing", workOrders?.get(0)?.category)
        assertEquals("electrical", workOrders?.get(1)?.category)
        assertEquals("landscaping", workOrders?.get(2)?.category)
        assertEquals("hvac", workOrders?.get(3)?.category)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).getWorkOrders()

        val result = useCase()

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Network error", result.errorMessage)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Connection timeout")
        doThrow(exception).`when`(mockRepository).getWorkOrders()

        val result = useCase()

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Connection timeout", result.errorMessage)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with default message when exception has no message`() = runTest {
        val exception = RuntimeException()
        doThrow(exception).`when`(mockRepository).getWorkOrders()

        val result = useCase()

        assertTrue(result.isError)
        assertEquals("Failed to load work orders", result.errorMessage)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke preserves all work order fields`() = runTest {
        val expectedResponse = WorkOrderResponse(data = listOf(testWorkOrder))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()
        val workOrder = result.getOrNull()?.data?.first()

        assertEquals("wo1", workOrder?.id)
        assertEquals("Fix Leaky Faucet", workOrder?.title)
        assertEquals("Kitchen faucet is leaking badly", workOrder?.description)
        assertEquals("plumbing", workOrder?.category)
        assertEquals("high", workOrder?.priority)
        assertEquals("pending", workOrder?.status)
        assertNull(workOrder?.vendorId)
        assertNull(workOrder?.vendorName)
        assertNull(workOrder?.assignedAt)
        assertNull(workOrder?.scheduledDate)
        assertNull(workOrder?.completedAt)
        assertEquals(150.0, workOrder?.estimatedCost, 0.001)
        assertEquals(0.0, workOrder?.actualCost, 0.001)
        assertEquals("prop123", workOrder?.propertyId)
        assertEquals("user456", workOrder?.reporterId)
        assertEquals("2024-01-15T10:30:00Z", workOrder?.createdAt)
        assertEquals("2024-01-15T10:30:00Z", workOrder?.updatedAt)
        assertEquals(0, workOrder?.attachments?.size)
        assertEquals(0, workOrder?.notes?.size)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles work orders with assigned vendors`() = runTest {
        val assignedOrder = testWorkOrder.copy(
            id = "wo2",
            vendorId = "vendor123",
            vendorName = "ABC Plumbing",
            assignedAt = "2024-01-16T09:00:00Z",
            scheduledDate = "2024-01-17T14:00:00Z"
        )
        val expectedResponse = WorkOrderResponse(data = listOf(testWorkOrder, assignedOrder))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()
        val workOrders = result.getOrNull()?.data

        assertEquals(2, workOrders?.size)
        assertNull(workOrders?.get(0)?.vendorId)
        assertEquals("vendor123", workOrders?.get(1)?.vendorId)
        assertEquals("ABC Plumbing", workOrders?.get(1)?.vendorName)
        assertEquals("2024-01-16T09:00:00Z", workOrders?.get(1)?.assignedAt)
        assertEquals("2024-01-17T14:00:00Z", workOrders?.get(1)?.scheduledDate)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles work orders with attachments`() = runTest {
        val orderWithAttachments = testWorkOrder.copy(
            id = "wo2",
            title = "Issue with Photos",
            attachments = listOf("photo1.jpg", "photo2.jpg", "document.pdf")
        )
        val expectedResponse = WorkOrderResponse(data = listOf(testWorkOrder, orderWithAttachments))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()
        val workOrders = result.getOrNull()?.data

        assertEquals(2, workOrders?.size)
        assertEquals(0, workOrders?.get(0)?.attachments?.size)
        assertEquals(3, workOrders?.get(1)?.attachments?.size)
        assertEquals("photo1.jpg", workOrders?.get(1)?.attachments?.get(0))
        assertEquals("photo2.jpg", workOrders?.get(1)?.attachments?.get(1))
        assertEquals("document.pdf", workOrders?.get(1)?.attachments?.get(2))
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles work orders with notes`() = runTest {
        val orderWithNotes = testWorkOrder.copy(
            id = "wo2",
            title = "Issue with Notes",
            notes = listOf("Initial report", "Vendor confirmed", "Parts ordered")
        )
        val expectedResponse = WorkOrderResponse(data = listOf(testWorkOrder, orderWithNotes))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()
        val workOrders = result.getOrNull()?.data

        assertEquals(2, workOrders?.size)
        assertEquals(0, workOrders?.get(0)?.notes?.size)
        assertEquals(3, workOrders?.get(1)?.notes?.size)
        assertEquals("Initial report", workOrders?.get(1)?.notes?.get(0))
        assertEquals("Vendor confirmed", workOrders?.get(1)?.notes?.get(1))
        assertEquals("Parts ordered", workOrders?.get(1)?.notes?.get(2))
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles work orders with actual cost`() = runTest {
        val completedOrder = testWorkOrder.copy(
            id = "wo2",
            status = "completed",
            estimatedCost = 200.0,
            actualCost = 185.50,
            completedAt = "2024-01-20T16:00:00Z"
        )
        val expectedResponse = WorkOrderResponse(data = listOf(testWorkOrder, completedOrder))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()
        val workOrders = result.getOrNull()?.data

        assertEquals(2, workOrders?.size)
        assertEquals(150.0, workOrders?.get(0)?.estimatedCost, 0.001)
        assertEquals(0.0, workOrders?.get(0)?.actualCost, 0.001)
        assertEquals(200.0, workOrders?.get(1)?.estimatedCost, 0.001)
        assertEquals(185.50, workOrders?.get(1)?.actualCost, 0.001)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles large work order list efficiently`() = runTest {
        val largeWorkOrderList = (1..100).map { i ->
            testWorkOrder.copy(
                id = "wo$i",
                title = "Work Order $i",
                priority = when (i % 4) {
                    0 -> "urgent"
                    1 -> "high"
                    2 -> "medium"
                    else -> "low"
                }
            )
        }
        val expectedResponse = WorkOrderResponse(data = largeWorkOrderList)
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getWorkOrders()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.data?.size)
        verify(mockRepository).getWorkOrders()
        verifyNoMoreInteractions(mockRepository)
    }
}
