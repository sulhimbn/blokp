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
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
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

    private fun sampleVendor(
        id: String = "1",
        name: String = "Vendor 1",
        specialty: String = "Cleaning",
        contactPerson: String = "John Doe",
        email: String = "vendor1@example.com",
        rating: Double = 4.5
    ) = Vendor(
        id = id,
        name = name,
        contactPerson = contactPerson,
        phoneNumber = "1234567890",
        email = email,
        specialty = specialty,
        address = "123 Main St",
        licenseNumber = "LICENSE123",
        insuranceInfo = "INSURANCE123",
        certifications = listOf("ISO9001"),
        rating = rating,
        totalReviews = 10,
        contractStart = "2024-01-01",
        contractEnd = "2024-12-31",
        isActive = true
    )

    private fun sampleWorkOrder(id: String = "wo1") = WorkOrder(
        id = id,
        title = "Fix Leaking Pipe",
        description = "Pipe is leaking in bathroom",
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
        Dispatchers.setMain(testDispatcher)
        CacheManager.getInstance().clearSync()
        repository = VendorRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
        Dispatchers.resetMain()
    }

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
        val errorResponse: Response<VendorResponse> =
            Response.error(404, "Not Found".toResponseBody(null))

        `when`(apiService.getVendors()).thenReturn(errorResponse)

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
    fun `getVendors should serve the second call from cache`() = runTest {
        val mockResponse = VendorResponse(data = listOf(sampleVendor()))

        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        repository.getVendors()
        val second = repository.getVendors()

        assertEquals(mockResponse, second.getOrNull())
        verify(apiService, times(1)).getVendors()
    }

    @Test
    fun `getVendor should return success when API returns valid response`() = runTest {
        val mockResponse = SingleVendorResponse(data = sampleVendor())

        `when`(apiService.getVendor("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getVendor("1")

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getVendor should return failure when vendor not found`() = runTest {
        val errorResponse: Response<SingleVendorResponse> =
            Response.error(404, "Not Found".toResponseBody(null))

        `when`(apiService.getVendor("999")).thenReturn(errorResponse)

        val result = repository.getVendor("999")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendor should return failure when body is null`() = runTest {
        `when`(apiService.getVendor("1")).thenReturn(Response.success(null))

        val result = repository.getVendor("1")

        assertTrue(result.isFailure)
        assertEquals("Response body is null", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createVendor should return success on valid input`() = runTest {
        val mockResponse = SingleVendorResponse(data = sampleVendor())

        `when`(
            apiService.createVendor(
                "Vendor 1",
                "John Doe",
                "1234567890",
                "vendor1@example.com",
                "Cleaning",
                "123 Main St",
                "LICENSE123",
                "INSURANCE123",
                "2024-01-01",
                "2024-12-31"
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
    fun `createVendor should return failure on invalid input`() = runTest {
        val errorResponse: Response<SingleVendorResponse> =
            Response.error(400, "Bad Request".toResponseBody(null))

        `when`(
            apiService.createVendor(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
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
        val mockResponse = SingleVendorResponse(data = sampleVendor(name = "Updated Vendor 1"))

        `when`(
            apiService.updateVendor(
                "1",
                "Updated Vendor 1",
                "Updated Person",
                "9876543210",
                "updated@example.com",
                "Updated Cleaning",
                "456 Oak Ave",
                "UPDATED_LICENSE",
                "UPDATED_INSURANCE",
                "2024-01-01",
                "2024-12-31",
                true
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
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getWorkOrder should return success when API returns valid response`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = sampleWorkOrder())

        `when`(apiService.getWorkOrder("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrder("1")

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `createWorkOrder should return success on valid input`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = sampleWorkOrder())

        `when`(
            apiService.createWorkOrder(
                "Fix Leaking Pipe",
                "Pipe is leaking in bathroom",
                "Plumbing",
                "high",
                "prop1",
                "user1",
                150.0
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
        val mockResponse = SingleWorkOrderResponse(data = sampleWorkOrder())

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
    fun `updateWorkOrderStatus should return success`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = sampleWorkOrder())

        `when`(
            apiService.updateWorkOrderStatus("wo1", "in_progress", "Work started")
        ).thenReturn(Response.success(mockResponse))

        val result = repository.updateWorkOrderStatus(
            workOrderId = "wo1",
            status = "in_progress",
            notes = "Work started"
        )

        assertTrue(result.isSuccess)
    }
}