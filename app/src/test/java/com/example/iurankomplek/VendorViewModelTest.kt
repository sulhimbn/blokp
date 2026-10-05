package com.example.iurankomplek

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.data.api.models.SingleVendorResponse
import com.example.iurankomplek.data.api.models.SingleWorkOrderResponse
import com.example.iurankomplek.model.WorkOrder
import com.example.iurankomplek.data.api.models.WorkOrderResponse
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.VendorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class VendorViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockRepository = FakeVendorRepository()

    private lateinit var vendorViewModel: VendorViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)
        vendorViewModel = VendorViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadVendors should update vendorState to Success when repository call is successful`() = runTest {
        val mockVendors = listOf(
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
            )
        )
        val mockResponse = VendorResponse(mockVendors)
        mockRepository.vendorsResult = (Result.success(mockResponse))

        vendorViewModel.loadVendors()

        advanceUntilIdle()
        val state = vendorViewModel.vendorState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockVendors, (state as UiState.Success).data.data)
    }

    @Test
    fun `loadVendors should update vendorState to Error when repository call fails`() = runTest {
        val errorMessage = "Network error"
        mockRepository.vendorsResult = (Result.failure(IOException(errorMessage)))

        vendorViewModel.loadVendors()

        advanceUntilIdle()
        val state = vendorViewModel.vendorState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).error)
    }

    @Test
    fun `vendorState starts as Loading before any load is triggered`() {
        assertTrue(vendorViewModel.vendorState.value is UiState.Loading)
    }

    @Test
    fun `loadVendors should handle empty vendor list`() = runTest {
        val mockResponse = VendorResponse(emptyList())
        mockRepository.vendorsResult = (Result.success(mockResponse))

        vendorViewModel.loadVendors()

        advanceUntilIdle()
        val state = vendorViewModel.vendorState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.data?.isEmpty() == true)
    }

    @Test
    fun `loadWorkOrders should update workOrderState to Success when repository call is successful`() = runTest {
        val mockWorkOrders = listOf(
            WorkOrder(
                id = "WO-001",
                title = "Fix leaking pipe",
                description = "Kitchen sink pipe is leaking",
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
                propertyId = "PROPERTY-001",
                reporterId = "USER-001",
                createdAt = "2023-06-15T10:30:00Z",
                updatedAt = "2023-06-15T10:30:00Z",
                attachments = listOf(),
                notes = listOf()
            )
        )
        val mockResponse = WorkOrderResponse(mockWorkOrders)
        mockRepository.workOrdersResult = (Result.success(mockResponse))

        vendorViewModel.loadWorkOrders()

        advanceUntilIdle()
        val state = vendorViewModel.workOrderState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockWorkOrders, (state as UiState.Success).data.data)
    }

    @Test
    fun `loadWorkOrders should update workOrderState to Error when repository call fails`() = runTest {
        val errorMessage = "Server error"
        mockRepository.workOrdersResult = (Result.failure(IOException(errorMessage)))

        vendorViewModel.loadWorkOrders()

        advanceUntilIdle()
        val state = vendorViewModel.workOrderState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).error)
    }

    @Test
    fun `workOrderState starts as Loading before any load is triggered`() {
        assertTrue(vendorViewModel.workOrderState.value is UiState.Loading)
    }

    @Test
    fun `loadWorkOrders should handle empty work order list`() = runTest {
        val mockResponse = WorkOrderResponse(emptyList())
        mockRepository.workOrdersResult = (Result.success(mockResponse))

        vendorViewModel.loadWorkOrders()

        advanceUntilIdle()
        val state = vendorViewModel.workOrderState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.data?.isEmpty() == true)
    }

    @Test
    fun `loadVendors should handle multiple vendors`() = runTest {
        val mockVendors = listOf(
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
                name = "Electrician Pro",
                contactPerson = "Jane Doe",
                phoneNumber = "098-765-4321",
                email = "info@electrician.com",
                specialty = "electrical",
                address = "456 Oak Ave",
                licenseNumber = "EL-54321",
                insuranceInfo = "Professional liability",
                certifications = listOf("Licensed", "Insured"),
                rating = 4.8,
                totalReviews = 50,
                contractStart = "2023-03-01",
                contractEnd = "2024-03-31",
                isActive = true
            )
        )
        val mockResponse = VendorResponse(mockVendors)
        mockRepository.vendorsResult = (Result.success(mockResponse))

        vendorViewModel.loadVendors()

        advanceUntilIdle()
        val state = vendorViewModel.vendorState.value
        assertTrue(state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.data?.size)
    }
}

/**
 * Mockito cannot stub suspend functions, so the repository is faked by hand.
 */
private class FakeVendorRepository : VendorRepository {
    var vendorsResult: Result<VendorResponse> = Result.failure(UnsupportedOperationException())
    var workOrdersResult: Result<WorkOrderResponse> = Result.failure(UnsupportedOperationException())
    var vendorCalls = 0
    var workOrderCalls = 0

    override suspend fun getVendors(): Result<VendorResponse> {
        vendorCalls++
        return vendorsResult
    }
    override suspend fun getVendor(id: String): Result<SingleVendorResponse> = unsupported()
    override suspend fun createVendor(name: String, contactPerson: String, phoneNumber: String, email: String, specialty: String, address: String, licenseNumber: String, insuranceInfo: String, contractStart: String, contractEnd: String): Result<SingleVendorResponse> = unsupported()
    override suspend fun updateVendor(id: String, name: String, contactPerson: String, phoneNumber: String, email: String, specialty: String, address: String, licenseNumber: String, insuranceInfo: String, contractStart: String, contractEnd: String, isActive: Boolean): Result<SingleVendorResponse> = unsupported()
    override suspend fun getWorkOrders(): Result<WorkOrderResponse> {
        workOrderCalls++
        return workOrdersResult
    }
    override suspend fun getWorkOrder(id: String): Result<SingleWorkOrderResponse> = unsupported()
    override suspend fun createWorkOrder(title: String, description: String, category: String, priority: String, propertyId: String, reporterId: String, estimatedCost: Double): Result<SingleWorkOrderResponse> = unsupported()
    override suspend fun assignVendorToWorkOrder(workOrderId: String, vendorId: String, scheduledDate: String?): Result<SingleWorkOrderResponse> = unsupported()
    override suspend fun updateWorkOrderStatus(workOrderId: String, status: String, notes: String?): Result<SingleWorkOrderResponse> = unsupported()

    private fun <T> unsupported(): Result<T> = Result.failure(UnsupportedOperationException())
}
