package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.model.WorkOrder
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
import org.mockito.kotlin.any

class CreateWorkOrderUseCaseTest {

    @Mock
    private lateinit var mockRepository: VendorRepository

    private lateinit var useCase: CreateWorkOrderUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = CreateWorkOrderUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val expectedWorkOrder = WorkOrder(
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
        val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createWorkOrder(
            title = "Fix Leaky Faucet",
            description = "Kitchen faucet is leaking badly",
            category = "plumbing",
            priority = "high",
            propertyId = "prop123",
            reporterId = "user456",
            estimatedCost = 150.0
        )

        val result = useCase(
            title = "Fix Leaky Faucet",
            description = "Kitchen faucet is leaking badly",
            category = "plumbing",
            priority = "high",
            propertyId = "prop123",
            reporterId = "user456",
            estimatedCost = 150.0
        )

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(mockRepository).createWorkOrder(
            title = "Fix Leaky Faucet",
            description = "Kitchen faucet is leaking badly",
            category = "plumbing",
            priority = "high",
            propertyId = "prop123",
            reporterId = "user456",
            estimatedCost = 150.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Fix Leaky Faucet",
            description = "Kitchen faucet is leaking badly",
            category = "plumbing",
            priority = "high",
            propertyId = "prop123",
            reporterId = "user456",
            estimatedCost = 150.0
        )

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Network error", result.errorMessage)
        verify(mockRepository).createWorkOrder(
            title = "Fix Leaky Faucet",
            description = "Kitchen faucet is leaking badly",
            category = "plumbing",
            priority = "high",
            propertyId = "prop123",
            reporterId = "user456",
            estimatedCost = 150.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Connection timeout")
        doThrow(exception).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Repair Electrical Outlet",
            description = "Outlet in living room not working",
            category = "electrical",
            priority = "medium",
            propertyId = "prop789",
            reporterId = "user123",
            estimatedCost = 200.0
        )

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Connection timeout", result.errorMessage)
        verify(mockRepository).createWorkOrder(
            title = "Repair Electrical Outlet",
            description = "Outlet in living room not working",
            category = "electrical",
            priority = "medium",
            propertyId = "prop789",
            reporterId = "user123",
            estimatedCost = 200.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with default message when exception has no message`() = runTest {
        val exception = RuntimeException()
        doThrow(exception).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Test Work Order",
            description = "Test description",
            category = "test",
            priority = "low",
            propertyId = "test",
            reporterId = "test",
            estimatedCost = 100.0
        )

        assertTrue(result.isError)
        assertEquals("Failed to create work order", result.errorMessage)
        verify(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates work order with plumbing category`() = runTest {
        val expectedResponse = SingleWorkOrderResponse(
            data = WorkOrder(
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
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Fix Leaky Faucet",
            description = "Kitchen faucet is leaking badly",
            category = "plumbing",
            priority = "high",
            propertyId = "prop123",
            reporterId = "user456",
            estimatedCost = 150.0
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createWorkOrder(
            title = "Fix Leaky Faucet",
            description = "Kitchen faucet is leaking badly",
            category = "plumbing",
            priority = "high",
            propertyId = "prop123",
            reporterId = "user456",
            estimatedCost = 150.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates work order with electrical category`() = runTest {
        val expectedResponse = SingleWorkOrderResponse(
            data = WorkOrder(
                id = "wo2",
                title = "Repair Electrical Outlet",
                description = "Outlet in living room not working",
                category = "electrical",
                priority = "medium",
                status = "pending",
                vendorId = null,
                vendorName = null,
                assignedAt = null,
                scheduledDate = null,
                completedAt = null,
                estimatedCost = 200.0,
                actualCost = 0.0,
                propertyId = "prop789",
                reporterId = "user123",
                createdAt = "2024-01-15T11:00:00Z",
                updatedAt = "2024-01-15T11:00:00Z",
                attachments = emptyList(),
                notes = emptyList()
            )
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Repair Electrical Outlet",
            description = "Outlet in living room not working",
            category = "electrical",
            priority = "medium",
            propertyId = "prop789",
            reporterId = "user123",
            estimatedCost = 200.0
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createWorkOrder(
            title = "Repair Electrical Outlet",
            description = "Outlet in living room not working",
            category = "electrical",
            priority = "medium",
            propertyId = "prop789",
            reporterId = "user123",
            estimatedCost = 200.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates work order with landscaping category`() = runTest {
        val expectedResponse = SingleWorkOrderResponse(
            data = WorkOrder(
                id = "wo3",
                title = "Trim Bushes",
                description = "Bushes in front yard need trimming",
                category = "landscaping",
                priority = "low",
                status = "pending",
                vendorId = null,
                vendorName = null,
                assignedAt = null,
                scheduledDate = null,
                completedAt = null,
                estimatedCost = 75.0,
                actualCost = 0.0,
                propertyId = "prop456",
                reporterId = "user789",
                createdAt = "2024-01-15T12:00:00Z",
                updatedAt = "2024-01-15T12:00:00Z",
                attachments = emptyList(),
                notes = emptyList()
            )
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Trim Bushes",
            description = "Bushes in front yard need trimming",
            category = "landscaping",
            priority = "low",
            propertyId = "prop456",
            reporterId = "user789",
            estimatedCost = 75.0
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createWorkOrder(
            title = "Trim Bushes",
            description = "Bushes in front yard need trimming",
            category = "landscaping",
            priority = "low",
            propertyId = "prop456",
            reporterId = "user789",
            estimatedCost = 75.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates work order with urgent priority`() = runTest {
        val expectedResponse = SingleWorkOrderResponse(
            data = WorkOrder(
                id = "wo4",
                title = "Emergency Water Leak",
                description = "Water pipe burst in basement",
                category = "plumbing",
                priority = "urgent",
                status = "pending",
                vendorId = null,
                vendorName = null,
                assignedAt = null,
                scheduledDate = null,
                completedAt = null,
                estimatedCost = 500.0,
                actualCost = 0.0,
                propertyId = "prop999",
                reporterId = "user999",
                createdAt = "2024-01-15T13:00:00Z",
                updatedAt = "2024-01-15T13:00:00Z",
                attachments = emptyList(),
                notes = emptyList()
            )
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Emergency Water Leak",
            description = "Water pipe burst in basement",
            category = "plumbing",
            priority = "urgent",
            propertyId = "prop999",
            reporterId = "user999",
            estimatedCost = 500.0
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createWorkOrder(
            title = "Emergency Water Leak",
            description = "Water pipe burst in basement",
            category = "plumbing",
            priority = "urgent",
            propertyId = "prop999",
            reporterId = "user999",
            estimatedCost = 500.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates work order with high priority`() = runTest {
        val expectedResponse = SingleWorkOrderResponse(
            data = WorkOrder(
                id = "wo5",
                title = "Major Issue",
                description = "Important repair needed",
                category = "electrical",
                priority = "high",
                status = "pending",
                vendorId = null,
                vendorName = null,
                assignedAt = null,
                scheduledDate = null,
                completedAt = null,
                estimatedCost = 300.0,
                actualCost = 0.0,
                propertyId = "prop111",
                reporterId = "user111",
                createdAt = "2024-01-15T14:00:00Z",
                updatedAt = "2024-01-15T14:00:00Z",
                attachments = emptyList(),
                notes = emptyList()
            )
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Major Issue",
            description = "Important repair needed",
            category = "electrical",
            priority = "high",
            propertyId = "prop111",
            reporterId = "user111",
            estimatedCost = 300.0
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createWorkOrder(
            title = "Major Issue",
            description = "Important repair needed",
            category = "electrical",
            priority = "high",
            propertyId = "prop111",
            reporterId = "user111",
            estimatedCost = 300.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates work order with medium priority`() = runTest {
        val expectedResponse = SingleWorkOrderResponse(
            data = WorkOrder(
                id = "wo6",
                title = "Routine Maintenance",
                description = "Scheduled maintenance task",
                category = "hvac",
                priority = "medium",
                status = "pending",
                vendorId = null,
                vendorName = null,
                assignedAt = null,
                scheduledDate = null,
                completedAt = null,
                estimatedCost = 100.0,
                actualCost = 0.0,
                propertyId = "prop222",
                reporterId = "user222",
                createdAt = "2024-01-15T15:00:00Z",
                updatedAt = "2024-01-15T15:00:00Z",
                attachments = emptyList(),
                notes = emptyList()
            )
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Routine Maintenance",
            description = "Scheduled maintenance task",
            category = "hvac",
            priority = "medium",
            propertyId = "prop222",
            reporterId = "user222",
            estimatedCost = 100.0
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createWorkOrder(
            title = "Routine Maintenance",
            description = "Scheduled maintenance task",
            category = "hvac",
            priority = "medium",
            propertyId = "prop222",
            reporterId = "user222",
            estimatedCost = 100.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates work order with low priority`() = runTest {
        val expectedResponse = SingleWorkOrderResponse(
            data = WorkOrder(
                id = "wo7",
                title = "Minor Issue",
                description = "Non-urgent repair needed",
                category = "plumbing",
                priority = "low",
                status = "pending",
                vendorId = null,
                vendorName = null,
                assignedAt = null,
                scheduledDate = null,
                completedAt = null,
                estimatedCost = 50.0,
                actualCost = 0.0,
                propertyId = "prop333",
                reporterId = "user333",
                createdAt = "2024-01-15T16:00:00Z",
                updatedAt = "2024-01-15T16:00:00Z",
                attachments = emptyList(),
                notes = emptyList()
            )
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Minor Issue",
            description = "Non-urgent repair needed",
            category = "plumbing",
            priority = "low",
            propertyId = "prop333",
            reporterId = "user333",
            estimatedCost = 50.0
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createWorkOrder(
            title = "Minor Issue",
            description = "Non-urgent repair needed",
            category = "plumbing",
            priority = "low",
            propertyId = "prop333",
            reporterId = "user333",
            estimatedCost = 50.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates work order with different estimated costs`() = runTest {
        val highCostOrder = WorkOrder(
            id = "wo8",
            title = "Expensive Repair",
            description = "Major renovation needed",
            category = "plumbing",
            priority = "high",
            status = "pending",
            vendorId = null,
            vendorName = null,
            assignedAt = null,
            scheduledDate = null,
            completedAt = null,
            estimatedCost = 5000.0,
            actualCost = 0.0,
            propertyId = "prop444",
            reporterId = "user444",
            createdAt = "2024-01-15T17:00:00Z",
            updatedAt = "2024-01-15T17:00:00Z",
            attachments = emptyList(),
            notes = emptyList()
        )
        val expectedResponse = SingleWorkOrderResponse(data = highCostOrder)
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Expensive Repair",
            description = "Major renovation needed",
            category = "plumbing",
            priority = "high",
            propertyId = "prop444",
            reporterId = "user444",
            estimatedCost = 5000.0
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createWorkOrder(
            title = "Expensive Repair",
            description = "Major renovation needed",
            category = "plumbing",
            priority = "high",
            propertyId = "prop444",
            reporterId = "user444",
            estimatedCost = 5000.0
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates work order with minimal estimated cost`() = runTest {
        val lowCostOrder = WorkOrder(
            id = "wo9",
            title = "Minor Fix",
            description = "Small repair needed",
            category = "plumbing",
            priority = "low",
            status = "pending",
            vendorId = null,
            vendorName = null,
            assignedAt = null,
            scheduledDate = null,
            completedAt = null,
            estimatedCost = 10.0,
            actualCost = 0.0,
            propertyId = "prop555",
            reporterId = "user555",
            createdAt = "2024-01-15T18:00:00Z",
            updatedAt = "2024-01-15T18:00:00Z",
            attachments = emptyList(),
            notes = emptyList()
        )
        val expectedResponse = SingleWorkOrderResponse(data = lowCostOrder)
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createWorkOrder(
            any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            title = "Minor Fix",
            description = "Small repair needed",
            category = "plumbing",
            priority = "low",
            propertyId = "prop555",
            reporterId = "user555",
            estimatedCost = 10.0
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createWorkOrder(
            title = "Minor Fix",
            description = "Small repair needed",
            category = "plumbing",
            priority = "low",
            propertyId = "prop555",
            reporterId = "user555",
            estimatedCost = 10.0
        )
        verifyNoMoreInteractions(mockRepository)
    }
}
