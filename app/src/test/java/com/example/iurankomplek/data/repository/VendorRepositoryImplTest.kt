package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.api.models.SingleVendorResponse
import com.example.iurankomplek.data.api.models.SingleWorkOrderResponse
import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.data.api.models.WorkOrderResponse
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.WorkOrder
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        runBlocking { CacheManager.getInstance().clear() }
        Dispatchers.setMain(testDispatcher)
        repository = VendorRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleVendor(id: String = "1", name: String = "Vendor 1") = Vendor(
        id = id,
        name = name,
        contactPerson = "John Doe",
        phoneNumber = "1234567890",
        email = "vendor1@example.com",
        specialty = "Cleaning",
        address = "123 Main St",
        licenseNumber = "LICENSE123",
        insuranceInfo = "INSURANCE123",
        certifications = listOf("Certified"),
        rating = 4.5,
        totalReviews = 10,
        contractStart = "2024-01-01",
        contractEnd = "2024-12-31",
        isActive = true
    )

    private fun sampleWorkOrder(id: String = "1") = WorkOrder(
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

    private fun <T> error(code: Int): Response<T> =
        Response.error(code, "".toResponseBody("text/plain".toMediaType()))

    @Test
    fun `getVendors should return success when API returns valid response`() = runTest {
        val mockResponse = VendorResponse(data = listOf(sampleVendor()))
        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getVendors should return failure when response is unsuccessful`() = runTest {
        `when`(apiService.getVendors()).thenReturn(error(404))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendors should return failure on IOException`() = runTest {
        `when`(apiService.getVendors()).thenAnswer { throw IOException("Network error") }

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getVendors should return empty list successfully`() = runTest {
        val mockResponse = VendorResponse(data = emptyList())
        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getVendor should return success when API returns valid response`() = runTest {
        val mockResponse = SingleVendorResponse(data = sampleVendor())
        `when`(apiService.getVendor("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getVendor("1")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getVendor should return failure when vendor not found`() = runTest {
        `when`(apiService.getVendor("999")).thenReturn(error(404))

        val result = repository.getVendor("999")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `createVendor should return success on valid input`() = runTest {
        val mockResponse = SingleVendorResponse(data = sampleVendor())
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
    fun `createVendor should return failure on server rejection`() = runTest {
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
        ).thenReturn(error(400))

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
        val mockResponse = SingleVendorResponse(data = sampleVendor(id = "1", name = "Updated Vendor 1"))
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
        val mockResponse = WorkOrderResponse(data = listOf(sampleWorkOrder()))
        `when`(apiService.getWorkOrders()).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.data?.size)
    }

    @Test
    fun `getWorkOrder should return success when API returns valid response`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = sampleWorkOrder())
        `when`(apiService.getWorkOrder("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrder("1")

        assertTrue(result.isSuccess)
        assertEquals("1", result.getOrNull()?.data?.id)
    }

    @Test
    fun `createWorkOrder should return success on valid input`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = sampleWorkOrder())
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
            data = sampleWorkOrder(id = "wo1").copy(
                vendorId = "vendor1",
                status = "assigned",
                scheduledDate = "2024-01-15"
            )
        )
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
        assertEquals("vendor1", result.getOrNull()?.data?.vendorId)
    }

    @Test
    fun `updateWorkOrderStatus should return success`() = runTest {
        val mockResponse = SingleWorkOrderResponse(
            data = sampleWorkOrder(id = "wo1").copy(status = "in_progress")
        )
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