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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class VendorRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: VendorRepositoryImpl

    private fun mockVendor() = Vendor(
        id = "1",
        name = "Vendor 1",
        contactPerson = "John Doe",
        phoneNumber = "1234567890",
        email = "vendor1@example.com",
        specialty = "Cleaning",
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

    private fun mockWorkOrder() = WorkOrder(
        id = "wo1",
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
        val mockResponse = VendorResponse(data = listOf(mockVendor()))
        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
        assertEquals(1, result.getOrNull()?.data?.size)
        assertEquals("Vendor 1", result.getOrNull()?.data?.first()?.name)
    }

    @Test
    fun `getVendors returns failure when response is unsuccessful`() = runTest {
        `when`(apiService.getVendors()).thenReturn(
            Response.error<VendorResponse>(404, "Not Found".toResponseBody())
        )

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
        assertEquals(404, (result.exceptionOrNull() as HttpException).code())
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
        `when`(apiService.getVendors()).thenReturn(
            Response.success(VendorResponse(data = emptyList()))
        )

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getVendor returns success when API returns valid response`() = runTest {
        val mockResponse = SingleVendorResponse(data = mockVendor())
        `when`(apiService.getVendor("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getVendor("1")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals("vendor1@example.com", result.getOrNull()?.data?.email)
    }

    @Test
    fun `getVendor returns failure when vendor not found`() = runTest {
        `when`(apiService.getVendor("999")).thenReturn(
            Response.error<SingleVendorResponse>(404, "Not Found".toResponseBody())
        )

        val result = repository.getVendor("999")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendor serves the second call from cache without hitting the API`() = runTest {
        val mockResponse = SingleVendorResponse(data = mockVendor())
        `when`(apiService.getVendor("1")).thenReturn(Response.success(mockResponse))

        val first = repository.getVendor("1")
        val second = repository.getVendor("1")

        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)
        assertEquals(first.getOrNull(), second.getOrNull())
        org.mockito.Mockito.verify(apiService, org.mockito.Mockito.times(1)).getVendor("1")
    }

    @Test
    fun `createVendor returns success on valid input`() = runTest {
        val mockResponse = SingleVendorResponse(data = mockVendor())
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
        assertEquals("LICENSE123", result.getOrNull()?.data?.licenseNumber)
    }

    @Test
    fun `createVendor returns failure when API rejects the payload`() = runTest {
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
        ).thenReturn(
            Response.error<SingleVendorResponse>(400, "Bad Request".toResponseBody())
        )

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
        val updated = mockVendor().copy(name = "Updated Vendor 1", rating = 5.0, isActive = true)
        val mockResponse = SingleVendorResponse(data = updated)
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
        assertEquals("Updated Vendor 1", result.getOrNull()?.data?.name)
    }

    @Test
    fun `getWorkOrders returns success when API returns valid response`() = runTest {
        `when`(apiService.getWorkOrders()).thenReturn(
            Response.success(WorkOrderResponse(data = listOf(mockWorkOrder())))
        )

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.data?.size)
        assertEquals("Plumbing", result.getOrNull()?.data?.first()?.category)
    }

    @Test
    fun `getWorkOrder returns success when API returns valid response`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = mockWorkOrder())
        `when`(apiService.getWorkOrder("wo1")).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrder("wo1")

        assertTrue(result.isSuccess)
        assertEquals("Fix Leaking Pipe", result.getOrNull()?.data?.title)
    }

    @Test
    fun `createWorkOrder returns success on valid input`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = mockWorkOrder())
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
        assertEquals(150.0, result.getOrNull()?.data?.estimatedCost)
    }

    @Test
    fun `assignVendorToWorkOrder returns success`() = runTest {
        val assigned = mockWorkOrder().copy(vendorId = "vendor1", status = "assigned")
        val mockResponse = SingleWorkOrderResponse(data = assigned)
        `when`(
            apiService.assignVendorToWorkOrder(
                id = "wo1",
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
        assertEquals("assigned", result.getOrNull()?.data?.status)
    }

    @Test
    fun `updateWorkOrderStatus returns success`() = runTest {
        val updated = mockWorkOrder().copy(status = "in_progress")
        val mockResponse = SingleWorkOrderResponse(data = updated)
        `when`(
            apiService.updateWorkOrderStatus(
                id = "wo1",
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
        assertEquals("in_progress", result.getOrNull()?.data?.status)
    }
}