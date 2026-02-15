package com.example.iurankomplek

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.model.WorkOrder
import com.example.iurankomplek.data.api.models.WorkOrderResponse
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.VendorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class VendorViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: VendorRepository

    private lateinit var vendorViewModel: VendorViewModel

    private val testDispatcher = StandardTestDispatcher()

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
        `when`(mockRepository.getVendors()).thenReturn(Result.success(mockResponse))

        vendorViewModel.loadVendors()

        advanceUntilIdle()
        val state = vendorViewModel.vendorState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockVendors, (state as UiState.Success).data.data)
    }

    @Test
    fun `loadVendors should update vendorState to Error when repository call fails`() = runTest {
        val errorMessage = "Network error"
        `when`(mockRepository.getVendors()).thenReturn(Result.failure(IOException(errorMessage)))

        vendorViewModel.loadVendors()

        advanceUntilIdle()
        val state = vendorViewModel.vendorState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }

    @Test
    fun `loadVendors should update vendorState to Loading initially`() {
        vendorViewModel.loadVendors()

        val state = vendorViewModel.vendorState.value
        assertTrue(state is UiState.Loading)
    }

    @Test
    fun `loadVendors should handle empty vendor list`() = runTest {
        val mockResponse = VendorResponse(emptyList())
        `when`(mockRepository.getVendors()).thenReturn(Result.success(mockResponse))

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
        `when`(mockRepository.getWorkOrders()).thenReturn(Result.success(mockResponse))

        vendorViewModel.loadWorkOrders()

        advanceUntilIdle()
        val state = vendorViewModel.workOrderState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockWorkOrders, (state as UiState.Success).data.data)
    }

    @Test
    fun `loadWorkOrders should update workOrderState to Error when repository call fails`() = runTest {
        val errorMessage = "Server error"
        `when`(mockRepository.getWorkOrders()).thenReturn(Result.failure(IOException(errorMessage)))

        vendorViewModel.loadWorkOrders()

        advanceUntilIdle()
        val state = vendorViewModel.workOrderState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }

    @Test
    fun `loadWorkOrders should update workOrderState to Loading initially`() {
        vendorViewModel.loadWorkOrders()

        val state = vendorViewModel.workOrderState.value
        assertTrue(state is UiState.Loading)
    }

    @Test
    fun `loadWorkOrders should handle empty work order list`() = runTest {
        val mockResponse = WorkOrderResponse(emptyList())
        `when`(mockRepository.getWorkOrders()).thenReturn(Result.success(mockResponse))

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
        `when`(mockRepository.getVendors()).thenReturn(Result.success(mockResponse))

        vendorViewModel.loadVendors()

        advanceUntilIdle()
        val state = vendorViewModel.vendorState.value
        assertTrue(state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.data?.size)
    }
}

