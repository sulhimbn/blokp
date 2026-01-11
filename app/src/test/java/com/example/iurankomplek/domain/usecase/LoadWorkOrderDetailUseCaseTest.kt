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

class LoadWorkOrderDetailUseCaseTest {

    @Mock
    private lateinit var mockRepository: VendorRepository

    private lateinit var useCase: LoadWorkOrderDetailUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadWorkOrderDetailUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val expectedWorkOrder = WorkOrder(
            id = "wo1",
            vendorId = "vendor1",
            vendorName = "ABC Plumbing",
            title = "Fix Leaky Pipe",
            description = "Kitchen sink pipe is leaking, needs immediate repair.",
            category = "plumbing",
            priority = "high",
            status = "pending",
            estimatedCost = 5000,
            actualCost = 0,
            createdDate = "2024-01-15T10:00:00Z",
            updatedDate = "2024-01-15T10:00:00Z",
            completedDate = null,
            assignedTo = "plumber1",
            attachments = emptyList(),
            notes = emptyList()
        )
        val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
        doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("wo1")

        val result = useCase("wo1")

        assertTrue(result.isSuccess)
        val workOrder = result.getOrNull()
        assertNotNull(workOrder)
        assertEquals(expectedWorkOrder, workOrder?.data)
        assertEquals("wo1", workOrder?.data?.id)
        assertEquals("Fix Leaky Pipe", workOrder?.data?.title)
        verify(mockRepository).getWorkOrder("wo1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Network error")
        doThrow(exception).`when`(mockRepository).getWorkOrder("wo1")

        val result = useCase("wo1")

        assertTrue(result.isError)
        val error = result.errorOrNull()
        assertNotNull(error)
        assertEquals(exception, error?.exception)
        assertEquals("Network error", error?.message)
        verify(mockRepository).getWorkOrder("wo1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with default message when repository throws exception without message`() = runTest {
        val exception = RuntimeException()
        doThrow(exception).`when`(mockRepository).getWorkOrder("wo1")

        val result = useCase("wo1")

        assertTrue(result.isError)
        val error = result.errorOrNull()
        assertNotNull(error)
        assertEquals(exception, error?.exception)
        assertEquals("Failed to load work order details", error?.message)
        verify(mockRepository).getWorkOrder("wo1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with all fields preserved`() = runTest {
        val expectedWorkOrder = WorkOrder(
            id = "wo2",
            vendorId = "vendor2",
            vendorName = "XYZ Electrical",
            title = "Install Ceiling Fan",
            description = "Install new ceiling fan in living room with remote control.",
            category = "electrical",
            priority = "medium",
            status = "in_progress",
            estimatedCost = 75000,
            actualCost = 80000,
            createdDate = "2024-01-10T09:00:00Z",
            updatedDate = "2024-01-12T14:30:00Z",
            completedDate = "2024-01-15T17:00:00Z",
            assignedTo = "electrician1",
            attachments = listOf("attachment1.jpg", "attachment2.pdf"),
            notes = listOf("Initial inspection complete", "Parts ordered")
        )
        val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
        doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("wo2")

        val result = useCase("wo2")

        assertTrue(result.isSuccess)
        val workOrder = result.getOrNull()
        assertNotNull(workOrder)
        assertEquals("wo2", workOrder?.data?.id)
        assertEquals("vendor2", workOrder?.data?.vendorId)
        assertEquals("XYZ Electrical", workOrder?.data?.vendorName)
        assertEquals("Install Ceiling Fan", workOrder?.data?.title)
        assertEquals("Install new ceiling fan in living room with remote control.", workOrder?.data?.description)
        assertEquals("electrical", workOrder?.data?.category)
        assertEquals("medium", workOrder?.data?.priority)
        assertEquals("in_progress", workOrder?.data?.status)
        assertEquals(75000, workOrder?.data?.estimatedCost)
        assertEquals(80000, workOrder?.data?.actualCost)
        assertEquals("2024-01-10T09:00:00Z", workOrder?.data?.createdDate)
        assertEquals("2024-01-12T14:30:00Z", workOrder?.data?.updatedDate)
        assertEquals("2024-01-15T17:00:00Z", workOrder?.data?.completedDate)
        assertEquals("electrician1", workOrder?.data?.assignedTo)
        assertEquals(listOf("attachment1.jpg", "attachment2.pdf"), workOrder?.data?.attachments)
        assertEquals(listOf("Initial inspection complete", "Parts ordered"), workOrder?.data?.notes)
    }

    @Test
    fun `invoke returns success with different priorities`() = runTest {
        val priorities = listOf("urgent", "high", "medium", "low")

        priorities.forEach { priority ->
            val expectedWorkOrder = WorkOrder(
                id = "wo_$priority",
                vendorId = "vendor1",
                vendorName = "ABC Plumbing",
                title = "Test Work Order",
                description = "Test description",
                category = "plumbing",
                priority = priority,
                status = "pending",
                estimatedCost = 5000,
                actualCost = 0,
                createdDate = "2024-01-15T10:00:00Z",
                updatedDate = "2024-01-15T10:00:00Z",
                completedDate = null,
                assignedTo = "plumber1",
                attachments = emptyList(),
                notes = emptyList()
            )
            val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
            doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("wo_$priority")

            val result = useCase("wo_$priority")

            assertTrue(result.isSuccess)
            assertEquals(priority, result.getOrNull()?.data?.priority)
        }
    }

    @Test
    fun `invoke returns success with different statuses`() = runTest {
        val statuses = listOf("pending", "in_progress", "completed", "cancelled")

        statuses.forEach { status ->
            val expectedWorkOrder = WorkOrder(
                id = "wo_$status",
                vendorId = "vendor1",
                vendorName = "ABC Plumbing",
                title = "Test Work Order",
                description = "Test description",
                category = "plumbing",
                priority = "medium",
                status = status,
                estimatedCost = 5000,
                actualCost = 0,
                createdDate = "2024-01-15T10:00:00Z",
                updatedDate = "2024-01-15T10:00:00Z",
                completedDate = if (status == "completed") "2024-01-16T10:00:00Z" else null,
                assignedTo = "plumber1",
                attachments = emptyList(),
                notes = emptyList()
            )
            val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
            doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("wo_$status")

            val result = useCase("wo_$status")

            assertTrue(result.isSuccess)
            assertEquals(status, result.getOrNull()?.data?.status)
        }
    }

    @Test
    fun `invoke returns success with different categories`() = runTest {
        val categories = listOf("plumbing", "electrical", "landscaping", "hvac", "general")

        categories.forEach { category ->
            val expectedWorkOrder = WorkOrder(
                id = "wo_$category",
                vendorId = "vendor1",
                vendorName = "ABC Plumbing",
                title = "Test Work Order",
                description = "Test description",
                category = category,
                priority = "medium",
                status = "pending",
                estimatedCost = 5000,
                actualCost = 0,
                createdDate = "2024-01-15T10:00:00Z",
                updatedDate = "2024-01-15T10:00:00Z",
                completedDate = null,
                assignedTo = "plumber1",
                attachments = emptyList(),
                notes = emptyList()
            )
            val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
            doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("wo_$category")

            val result = useCase("wo_$category")

            assertTrue(result.isSuccess)
            assertEquals(category, result.getOrNull()?.data?.category)
        }
    }

    @Test
    fun `invoke returns success with zero cost values`() = runTest {
        val expectedWorkOrder = WorkOrder(
            id = "wo3",
            vendorId = "vendor1",
            vendorName = "ABC Plumbing",
            title = "Free Consultation",
            description = "Initial consultation visit.",
            category = "plumbing",
            priority = "low",
            status = "pending",
            estimatedCost = 0,
            actualCost = 0,
            createdDate = "2024-01-15T10:00:00Z",
            updatedDate = "2024-01-15T10:00:00Z",
            completedDate = null,
            assignedTo = "plumber1",
            attachments = emptyList(),
            notes = emptyList()
        )
        val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
        doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("wo3")

        val result = useCase("wo3")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.data?.estimatedCost)
        assertEquals(0, result.getOrNull()?.data?.actualCost)
    }

    @Test
    fun `invoke returns success with large cost values`() = runTest {
        val expectedWorkOrder = WorkOrder(
            id = "wo4",
            vendorId = "vendor2",
            vendorName = "Major Renovations Inc",
            title = "Complete Kitchen Renovation",
            description = "Full kitchen remodel including cabinets, appliances, and flooring.",
            category = "general",
            priority = "urgent",
            status = "in_progress",
            estimatedCost = 15000000,
            actualCost = 14500000,
            createdDate = "2024-01-01T09:00:00Z",
            updatedDate = "2024-01-20T14:30:00Z",
            completedDate = null,
            assignedTo = "contractor1",
            attachments = emptyList(),
            notes = emptyList()
        )
        val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
        doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("wo4")

        val result = useCase("wo4")

        assertTrue(result.isSuccess)
        assertEquals(15000000, result.getOrNull()?.data?.estimatedCost)
        assertEquals(14500000, result.getOrNull()?.data?.actualCost)
    }

    @Test
    fun `invoke returns success with multiple attachments and notes`() = runTest {
        val expectedWorkOrder = WorkOrder(
            id = "wo5",
            vendorId = "vendor1",
            vendorName = "ABC Plumbing",
            title = "Complex Repair",
            description = "Complex repair with multiple documentation.",
            category = "plumbing",
            priority = "high",
            status = "in_progress",
            estimatedCost = 20000,
            actualCost = 15000,
            createdDate = "2024-01-15T10:00:00Z",
            updatedDate = "2024-01-16T10:00:00Z",
            completedDate = null,
            assignedTo = "plumber1",
            attachments = listOf("photo1.jpg", "photo2.jpg", "document.pdf", "drawing.dwg"),
            notes = listOf("Initial visit scheduled", "Problem identified", "Parts ordered", "In progress")
        )
        val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
        doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("wo5")

        val result = useCase("wo5")

        assertTrue(result.isSuccess)
        val workOrder = result.getOrNull()
        assertNotNull(workOrder)
        assertEquals(4, workOrder?.data?.attachments?.size)
        assertEquals(4, workOrder?.data?.notes?.size)
    }

    @Test
    fun `invoke returns success with empty attachments and notes`() = runTest {
        val expectedWorkOrder = WorkOrder(
            id = "wo6",
            vendorId = "vendor1",
            vendorName = "ABC Plumbing",
            title = "Simple Repair",
            description = "Simple repair with no attachments or notes.",
            category = "plumbing",
            priority = "low",
            status = "completed",
            estimatedCost = 5000,
            actualCost = 5000,
            createdDate = "2024-01-15T10:00:00Z",
            updatedDate = "2024-01-15T10:00:00Z",
            completedDate = "2024-01-15T14:00:00Z",
            assignedTo = "plumber1",
            attachments = emptyList(),
            notes = emptyList()
        )
        val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
        doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("wo6")

        val result = useCase("wo6")

        assertTrue(result.isSuccess)
        val workOrder = result.getOrNull()
        assertNotNull(workOrder)
        assertTrue(workOrder?.data?.attachments?.isEmpty() == true)
        assertTrue(workOrder?.data?.notes?.isEmpty() == true)
    }

    @Test
    fun `invoke passes ID correctly to repository`() = runTest {
        val testId = "work_order_id_12345_abc"
        val expectedWorkOrder = WorkOrder(
            id = testId,
            vendorId = "vendor1",
            vendorName = "ABC Plumbing",
            title = "Test Work Order",
            description = "Test description",
            category = "plumbing",
            priority = "medium",
            status = "pending",
            estimatedCost = 5000,
            actualCost = 0,
            createdDate = "2024-01-15T10:00:00Z",
            updatedDate = "2024-01-15T10:00:00Z",
            completedDate = null,
            assignedTo = "plumber1",
            attachments = emptyList(),
            notes = emptyList()
        )
        val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
        doReturn(expectedResponse).`when`(mockRepository).getWorkOrder(testId)

        val result = useCase(testId)

        assertTrue(result.isSuccess)
        verify(mockRepository).getWorkOrder(testId)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with numeric ID`() = runTest {
        val expectedWorkOrder = WorkOrder(
            id = "12345",
            vendorId = "vendor1",
            vendorName = "ABC Plumbing",
            title = "Test Work Order",
            description = "Test description",
            category = "plumbing",
            priority = "medium",
            status = "pending",
            estimatedCost = 5000,
            actualCost = 0,
            createdDate = "2024-01-15T10:00:00Z",
            updatedDate = "2024-01-15T10:00:00Z",
            completedDate = null,
            assignedTo = "plumber1",
            attachments = emptyList(),
            notes = emptyList()
        )
        val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
        doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("12345")

        val result = useCase("12345")

        assertTrue(result.isSuccess)
        assertEquals("12345", result.getOrNull()?.data?.id)
    }

    @Test
    fun `invoke returns success with alphanumeric ID`() = runTest {
        val expectedWorkOrder = WorkOrder(
            id = "wo_abc123_xyz_789",
            vendorId = "vendor1",
            vendorName = "ABC Plumbing",
            title = "Test Work Order",
            description = "Test description",
            category = "plumbing",
            priority = "medium",
            status = "pending",
            estimatedCost = 5000,
            actualCost = 0,
            createdDate = "2024-01-15T10:00:00Z",
            updatedDate = "2024-01-15T10:00:00Z",
            completedDate = null,
            assignedTo = "plumber1",
            attachments = emptyList(),
            notes = emptyList()
        )
        val expectedResponse = SingleWorkOrderResponse(data = expectedWorkOrder)
        doReturn(expectedResponse).`when`(mockRepository).getWorkOrder("wo_abc123_xyz_789")

        val result = useCase("wo_abc123_xyz_789")

        assertTrue(result.isSuccess)
        assertEquals("wo_abc123_xyz_789", result.getOrNull()?.data?.id)
    }
}
