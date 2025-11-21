package com.example.iurankomplek

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.WorkOrder
import com.example.iurankomplek.model.WorkOrderResponse
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

@OptIn(ExperimentalCoroutinesApi::class)
class VendorViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var mockRepository: VendorRepository
    
    private lateinit var vendorViewModel: VendorViewModel
    
    private val testDispatcher = TestDispatcherProvider()
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        vendorViewModel = VendorViewModel(mockRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadVendors should update vendorState to Success when repository call is successful`() = runTest {
        // Given
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
        
        // When
        vendorViewModel.loadVendors()
        
        // Then
        val state = vendorViewModel.vendorState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockVendors, (state as UiState.Success).data.data)
    }
    
    @Test
    fun `loadWorkOrders should update workOrderState to Success when repository call is successful`() = runTest {
        // Given
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
        
        // When
        vendorViewModel.loadWorkOrders()
        
        // Then
        val state = vendorViewModel.workOrderState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockWorkOrders, (state as UiState.Success).data.data)
    }
}

// Helper class for test dispatcher
@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherProvider : kotlinx.coroutines.test.TestDispatcherProvider by TestDispatchers()