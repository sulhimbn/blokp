package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.*
import retrofit2.Response
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class VendorRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: VendorRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        repository = VendorRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getVendors should return success when API returns valid response`() = runTest {
        val mockData = listOf(
            Vendor(
                id = 1,
                name = "Vendor 1",
                service = "Cleaning",
                contact = "vendor1@example.com",
                rating = 4.5
            )
        )
        val mockResponse = VendorResponse(
            success = true,
            message = "Vendors fetched successfully",
            data = mockData
        )

        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertNotNull(responseBody)
        assertEquals(mockResponse, responseBody)
    }

    @Test
    fun `getVendors should return failure when response is unsuccessful`() = runTest {
        val errorResponse = Response.error<VendorResponse>(
            404,
            okhttp3.ResponseBody.create(null, "Not Found")
        )

        `when`(apiService.getVendors()).thenReturn(errorResponse)

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendors should return failure on IOException`() = runTest {
        `when`(apiService.getVendors()).thenThrow(IOException("Network error"))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getVendors should return empty list successfully`() = runTest {
        val mockResponse = VendorResponse(success = true, message = "No vendors", data = emptyList())

        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertTrue(responseBody?.data?.isEmpty() == true)
    }

    @Test
    fun `getVendor should return success when API returns valid response`() = runTest {
        val mockVendor = Vendor(
            id = 1,
            name = "Vendor 1",
            service = "Cleaning",
            contact = "vendor1@example.com",
            rating = 4.5
        )
        val mockResponse = SingleVendorResponse(
            success = true,
            message = "Vendor fetched successfully",
            data = mockVendor
        )

        `when`(apiService.getVendor("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getVendor("1")

        assertTrue(result.isSuccess)
        val responseBody = result.getOrNull()
        assertNotNull(responseBody)
        assertEquals(mockResponse, responseBody)
    }

    @Test
    fun `getVendor should return failure when vendor not found`() = runTest {
        val errorResponse = Response.error<SingleVendorResponse>(
            404,
            okhttp3.ResponseBody.create(null, "Not Found")
        )

        `when`(apiService.getVendor("999")).thenReturn(errorResponse)

        val result = repository.getVendor("999")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `createVendor should return success on valid input`() = runTest {
        val mockVendor = Vendor(
            id = 1,
            name = "Vendor 1",
            service = "Cleaning",
            contact = "vendor1@example.com",
            rating = 4.5
        )
        val mockResponse = SingleVendorResponse(
            success = true,
            message = "Vendor created successfully",
            data = mockVendor
        )

        `when`(
            apiService.createVendor(
                name = "Vendor 1",
                contactPerson = "John Doe",
                phoneNumber = "1234567890",
                email = "vendor1@example.com",
                specialty = "Cleaning",
                address = "123 Main St",
                licenseNumber = "LICENSE123",
                insuranceInfo = "INSURANCE123",
                contractStart = "2024-01-01",
                contractEnd = "2024-12-31"
            )
        ).thenReturn(Response.success(mockResponse))

        val result = repository.createVendor(
            name = "Vendor 1",
            contactPerson = "John Doe",
            phoneNumber = "1234567890",
            email = "vendor1@example.com",
            specialty = "Cleaning",
            address = "123 Main St",
            licenseNumber = "LICENSE123",
            insuranceInfo = "INSURANCE123",
            contractStart = "2024-01-01",
            contractEnd = "2024-12-31"
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `createVendor should return failure on invalid input`() = runTest {
        val errorResponse = Response.error<SingleVendorResponse>(
            400,
            okhttp3.ResponseBody.create(null, "Bad Request")
        )

        `when`(
            apiService.createVendor(
                name = "",
                contactPerson = "",
                phoneNumber = "",
                email = "",
                specialty = "",
                address = "",
                licenseNumber = "",
                insuranceInfo = "",
                contractStart = "",
                contractEnd = ""
            )
        ).thenReturn(errorResponse)

        val result = repository.createVendor(
            name = "",
            contactPerson = "",
            phoneNumber = "",
            email = "",
            specialty = "",
            address = "",
            licenseNumber = "",
            insuranceInfo = "",
            contractStart = "",
            contractEnd = ""
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateVendor should return success on valid update`() = runTest {
        val mockVendor = Vendor(
            id = 1,
            name = "Updated Vendor 1",
            service = "Updated Cleaning",
            contact = "updated@example.com",
            rating = 5.0
        )
        val mockResponse = SingleVendorResponse(
            success = true,
            message = "Vendor updated successfully",
            data = mockVendor
        )

        `when`(
            apiService.updateVendor(
                id = "1",
                name = "Updated Vendor 1",
                contactPerson = "Updated Person",
                phoneNumber = "9876543210",
                email = "updated@example.com",
                specialty = "Updated Cleaning",
                address = "456 Oak Ave",
                licenseNumber = "UPDATED_LICENSE",
                insuranceInfo = "UPDATED_INSURANCE",
                contractStart = "2024-01-01",
                contractEnd = "2024-12-31",
                isActive = true
            )
        ).thenReturn(Response.success(mockResponse))

        val result = repository.updateVendor(
            id = "1",
            name = "Updated Vendor 1",
            contactPerson = "Updated Person",
            phoneNumber = "9876543210",
            email = "updated@example.com",
            specialty = "Updated Cleaning",
            address = "456 Oak Ave",
            licenseNumber = "UPDATED_LICENSE",
            insuranceInfo = "UPDATED_INSURANCE",
            contractStart = "2024-01-01",
            contractEnd = "2024-12-31",
            isActive = true
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getWorkOrders should return success when API returns valid response`() = runTest {
        val mockResponse = WorkOrderResponse(
            success = true,
            message = "Work orders fetched successfully",
            data = emptyList()
        )

        `when`(apiService.getWorkOrders()).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getWorkOrder should return success when API returns valid response`() = runTest {
        val mockResponse = SingleWorkOrderResponse(
            success = true,
            message = "Work order fetched successfully",
            data = null
        )

        `when`(apiService.getWorkOrder("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrder("1")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `createWorkOrder should return success on valid input`() = runTest {
        val mockResponse = SingleWorkOrderResponse(
            success = true,
            message = "Work order created successfully",
            data = null
        )

        `when`(
            apiService.createWorkOrder(
                title = "Fix Leaking Pipe",
                description = "Pipe is leaking in bathroom",
                category = "Plumbing",
                priority = "high",
                propertyId = "prop1",
                reporterId = "user1",
                estimatedCost = 150.0
            )
        ).thenReturn(Response.success(mockResponse))

        val result = repository.createWorkOrder(
            title = "Fix Leaking Pipe",
            description = "Pipe is leaking in bathroom",
            category = "Plumbing",
            priority = "high",
            propertyId = "prop1",
            reporterId = "user1",
            estimatedCost = 150.0
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `assignVendorToWorkOrder should return success`() = runTest {
        val mockResponse = SingleWorkOrderResponse(
            success = true,
            message = "Vendor assigned successfully",
            data = null
        )

        `when`(
            apiService.assignVendorToWorkOrder(
                workOrderId = "wo1",
                vendorId = "vendor1",
                scheduledDate = "2024-01-15"
            )
        ).thenReturn(Response.success(mockResponse))

        val result = repository.assignVendorToWorkOrder(
            workOrderId = "wo1",
            vendorId = "vendor1",
            scheduledDate = "2024-01-15"
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateWorkOrderStatus should return success`() = runTest {
        val mockResponse = SingleWorkOrderResponse(
            success = true,
            message = "Status updated successfully",
            data = null
        )

        `when`(
            apiService.updateWorkOrderStatus(
                workOrderId = "wo1",
                status = "in_progress",
                notes = "Work started"
            )
        ).thenReturn(Response.success(mockResponse))

        val result = repository.updateWorkOrderStatus(
            workOrderId = "wo1",
            status = "in_progress",
            notes = "Work started"
        )

        assertTrue(result.isSuccess)
    }
}
