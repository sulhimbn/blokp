package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.api.models.SingleVendorResponse
import com.example.iurankomplek.data.api.models.SingleWorkOrderResponse
import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.data.api.models.WorkOrderResponse
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.WorkOrder
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.CacheManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class VendorRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: VendorRepositoryImpl

    private fun vendor(
        id: String = "1",
        name: String = "Vendor 1",
        specialty: String = "Cleaning"
    ) = Vendor(
        id = id,
        name = name,
        contactPerson = "John Doe",
        phoneNumber = "1234567890",
        email = "vendor1@example.com",
        specialty = specialty,
        address = "123 Main St",
        licenseNumber = "LICENSE123",
        insuranceInfo = "INSURANCE123",
        certifications = listOf("ISO9001"),
        rating = 4.5,
        totalReviews = 10,
        contractStart = "2024-01-01",
        contractEnd = "2024-12-31",
        isActive = true
    )

    private fun workOrder(id: String = "wo1") = WorkOrder(
        id = id,
        title = "Fix Leaking Pipe",
        description = "Pipe is leaking in bathroom",
        category = "Plumbing",
        priority = "high",
        status = "pending",
        vendorId = null,
        vendorName = null,
        assignedAt = null,
        scheduledDate = null,
        completedAt = null,
        estimatedCost = 150.0,
        actualCost = 0.0,
        propertyId = "prop1",
        reporterId = "user1",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        attachments = emptyList(),
        notes = emptyList()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        CacheManager.getInstance().clearSync()
        repository = VendorRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
    }

    @Test
    fun `getVendors returns success when API returns valid response`() = runTest {
        val mockResponse = VendorResponse(data = listOf(vendor()))
        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
        assertEquals(1, result.getOrNull()?.data?.size)
    }

    @Test
    fun `getVendors returns failure when response is unsuccessful`() = runTest {
        val errorResponse = Response.error<VendorResponse>(
            404,
            "Not Found".toResponseBody(null)
        )
        `when`(apiService.getVendors()).thenReturn(errorResponse)

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendors returns failure on IOException`() = runTest {
        `when`(apiService.getVendors()).thenAnswer { throw IOException("Network error") }

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getVendors returns empty list successfully`() = runTest {
        val mockResponse = VendorResponse(data = emptyList())
        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getVendors serves second call from cache`() = runTest {
        val mockResponse = VendorResponse(data = listOf(vendor()))
        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val first = repository.getVendors()
        val second = repository.getVendors()

        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)
        assertEquals(first.getOrNull(), second.getOrNull())
        org.mockito.Mockito.verify(apiService, org.mockito.Mockito.times(1)).getVendors()
    }

    @Test
    fun `getVendor returns success when API returns valid response`() = runTest {
        val mockResponse = SingleVendorResponse(data = vendor())
        `when`(apiService.getVendor("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getVendor("1")

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getVendor returns failure when vendor not found`() = runTest {
        val errorResponse = Response.error<SingleVendorResponse>(
            404,
            "Not Found".toResponseBody(null)
        )
        `when`(apiService.getVendor("999")).thenReturn(errorResponse)

        val result = repository.getVendor("999")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `createVendor returns success on valid input`() = runTest {
        val mockResponse = SingleVendorResponse(data = vendor())
        `when`(
            apiService.createVendor(
                "Vendor 1", "John Doe", "1234567890", "vendor1@example.com",
                "Cleaning", "123 Main St", "LICENSE123", "INSURANCE123",
                "2024-01-01", "2024-12-31"
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
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `createVendor returns failure on invalid input`() = runTest {
        val errorResponse = Response.error<SingleVendorResponse>(
            400,
            "Bad Request".toResponseBody(null)
        )
        `when`(
            apiService.createVendor(
                "", "", "", "", "", "", "", "", "", ""
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
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `updateVendor returns success on valid update`() = runTest {
        val updated = vendor(name = "Updated Vendor 1", specialty = "Updated Cleaning")
        val mockResponse = SingleVendorResponse(data = updated)
        `when`(
            apiService.updateVendor(
                "1", "Updated Vendor 1", "Updated Person", "9876543210",
                "updated@example.com", "Updated Cleaning", "456 Oak Ave",
                "UPDATED_LICENSE", "UPDATED_INSURANCE", "2024-01-01",
                "2024-12-31", true
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
        assertEquals("Updated Vendor 1", result.getOrNull()?.data?.name)
    }

    @Test
    fun `getWorkOrders returns success when API returns valid response`() = runTest {
        val mockResponse = WorkOrderResponse(data = emptyList())
        `when`(apiService.getWorkOrders()).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getWorkOrder returns success when API returns valid response`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder())
        `when`(apiService.getWorkOrder("wo1")).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrder("wo1")

        assertTrue(result.isSuccess)
        assertEquals("wo1", result.getOrNull()?.data?.id)
    }

    @Test
    fun `createWorkOrder returns success on valid input`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder())
        `when`(
            apiService.createWorkOrder(
                "Fix Leaking Pipe", "Pipe is leaking in bathroom", "Plumbing",
                "high", "prop1", "user1", 150.0
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
        assertEquals(150.0, result.getOrNull()?.data?.estimatedCost!!, 0.001)
    }

    @Test
    fun `assignVendorToWorkOrder returns success`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder())
        `when`(
            apiService.assignVendorToWorkOrder("wo1", "vendor1", "2024-01-15")
        ).thenReturn(Response.success(mockResponse))

        val result = repository.assignVendorToWorkOrder(
            workOrderId = "wo1",
            vendorId = "vendor1",
            scheduledDate = "2024-01-15"
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateWorkOrderStatus returns success`() = runTest {
        val updated = workOrder().copy(status = "in_progress")
        val mockResponse = SingleWorkOrderResponse(data = updated)
        `when`(
            apiService.updateWorkOrderStatus("wo1", "in_progress", "Work started")
        ).thenReturn(Response.success(mockResponse))

        val result = repository.updateWorkOrderStatus(
            workOrderId = "wo1",
            status = "in_progress",
            notes = "Work started"
        )

        assertTrue(result.isSuccess)
        assertEquals("in_progress", result.getOrNull()?.data?.status)
    }
}