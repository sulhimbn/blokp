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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.wheneverBlocking
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
        Dispatchers.setMain(testDispatcher)
        repository = VendorRepositoryImpl(apiService)
        CacheManager.getInstance().clearSync()
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
        Dispatchers.resetMain()
    }

    private fun vendor(
        id: String = "1",
        name: String = "Vendor 1",
        specialty: String = "Cleaning",
        contactPerson: String = "John Doe",
        phoneNumber: String = "1234567890",
        email: String = "vendor1@example.com",
        address: String = "123 Main St",
        licenseNumber: String = "LICENSE123",
        insuranceInfo: String = "INSURANCE123",
        contractStart: String = "2024-01-01",
        contractEnd: String = "2024-12-31",
        isActive: Boolean = true,
        rating: Double = 4.5,
        totalReviews: Int = 10
    ) = Vendor(
        id = id,
        name = name,
        contactPerson = contactPerson,
        phoneNumber = phoneNumber,
        email = email,
        specialty = specialty,
        address = address,
        licenseNumber = licenseNumber,
        insuranceInfo = insuranceInfo,
        certifications = listOf("ISO9001"),
        rating = rating,
        totalReviews = totalReviews,
        contractStart = contractStart,
        contractEnd = contractEnd,
        isActive = isActive
    )

    private fun workOrder(
        id: String = "wo1",
        title: String = "Fix Leaking Pipe",
        description: String = "Pipe is leaking in bathroom",
        category: String = "Plumbing",
        priority: String = "high",
        status: String = "pending",
        vendorId: String? = null,
        estimatedCost: Double = 150.0
    ) = WorkOrder(
        id = id,
        title = title,
        description = description,
        category = category,
        priority = priority,
        status = status,
        vendorId = vendorId,
        vendorName = vendorId?.let { "Vendor $it" },
        assignedAt = null,
        scheduledDate = null,
        completedAt = null,
        estimatedCost = estimatedCost,
        actualCost = 0.0,
        propertyId = "prop1",
        reporterId = "user1",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        attachments = emptyList(),
        notes = emptyList()
    )

    @Test
    fun `getVendors should return success when API returns valid response`() = runTest {
        val mockResponse = VendorResponse(data = listOf(vendor()))

        wheneverBlocking { apiService.getVendors() }.thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getVendors should return failure when response is unsuccessful`() = runTest {
        val errorResponse = Response.error<VendorResponse>(404, "Not Found".toResponseBody())

        wheneverBlocking { apiService.getVendors() }.thenReturn(errorResponse)

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendors should return failure on IOException`() = runTest {
        doAnswer { throw IOException("Network error") }.`when`(apiService).getVendors()

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getVendors should return empty list successfully`() = runTest {
        val mockResponse = VendorResponse(data = emptyList())

        wheneverBlocking { apiService.getVendors() }.thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getVendors should serve second call from cache without hitting the API`() = runTest {
        val mockResponse = VendorResponse(data = listOf(vendor()))
        wheneverBlocking { apiService.getVendors() }.thenReturn(Response.success(mockResponse))

        val first = repository.getVendors()
        val second = repository.getVendors()

        assertTrue(first.isSuccess)
        assertEquals(first.getOrNull(), second.getOrNull())
        org.mockito.Mockito.verify(apiService, org.mockito.Mockito.times(1)).getVendors()
    }

    @Test
    fun `getVendor should return success when API returns valid response`() = runTest {
        val mockResponse = SingleVendorResponse(data = vendor())

        wheneverBlocking { apiService.getVendor("1") }.thenReturn(Response.success(mockResponse))

        val result = repository.getVendor("1")

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getVendor should return failure when vendor not found`() = runTest {
        val errorResponse = Response.error<SingleVendorResponse>(404, "Not Found".toResponseBody())

        wheneverBlocking { apiService.getVendor("999") }.thenReturn(errorResponse)

        val result = repository.getVendor("999")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `createVendor should return success on valid input`() = runTest {
        val mockResponse = SingleVendorResponse(data = vendor())

        wheneverBlocking {
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
        }.thenReturn(Response.success(mockResponse))

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
    fun `createVendor should return failure on invalid input`() = runTest {
        val errorResponse = Response.error<SingleVendorResponse>(400, "Bad Request".toResponseBody())

        wheneverBlocking {
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
        }.thenReturn(errorResponse)

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
        val mockResponse = SingleVendorResponse(data = vendor(name = "Updated Vendor 1"))

        wheneverBlocking {
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
        }.thenReturn(Response.success(mockResponse))

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
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getWorkOrders should return success when API returns valid response`() = runTest {
        val mockResponse = WorkOrderResponse(data = listOf(workOrder()))

        wheneverBlocking { apiService.getWorkOrders() }.thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getWorkOrder should return success when API returns valid response`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder())

        wheneverBlocking { apiService.getWorkOrder("1") }.thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrder("1")

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `createWorkOrder should return success on valid input`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder())

        wheneverBlocking {
            apiService.createWorkOrder(
                title = "Fix Leaking Pipe",
                description = "Pipe is leaking in bathroom",
                category = "Plumbing",
                priority = "high",
                propertyId = "prop1",
                reporterId = "user1",
                estimatedCost = 150.0
            )
        }.thenReturn(Response.success(mockResponse))

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
            data = workOrder(status = "assigned", vendorId = "vendor1")
        )

        wheneverBlocking {
            apiService.assignVendorToWorkOrder(
                id = "wo1",
                vendorId = "vendor1",
                scheduledDate = "2024-01-15"
            )
        }.thenReturn(Response.success(mockResponse))

        val result = repository.assignVendorToWorkOrder(
            workOrderId = "wo1",
            vendorId = "vendor1",
            scheduledDate = "2024-01-15"
        )

        assertTrue(result.isSuccess)
        assertEquals("assigned", result.getOrNull()?.data?.status)
    }

    @Test
    fun `updateWorkOrderStatus should return success`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder(status = "in_progress"))

        wheneverBlocking {
            apiService.updateWorkOrderStatus(
                id = "wo1",
                status = "in_progress",
                notes = "Work started"
            )
        }.thenReturn(Response.success(mockResponse))

        val result = repository.updateWorkOrderStatus(
            workOrderId = "wo1",
            status = "in_progress",
            notes = "Work started"
        )

        assertTrue(result.isSuccess)
        assertEquals("in_progress", result.getOrNull()?.data?.status)
    }
}