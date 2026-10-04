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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class VendorRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: VendorRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = VendorRepositoryImpl(apiService)
        CacheManager.getInstance().clearSync()
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
    }

    private fun sampleVendor(
        id: String = "1",
        name: String = "Vendor 1",
        isActive: Boolean = true
    ) = Vendor(
        id = id,
        name = name,
        contactPerson = "John Smith",
        phoneNumber = "1234567890",
        email = "vendor1@example.com",
        specialty = "Cleaning",
        address = "123 Main St",
        licenseNumber = "LICENSE123",
        insuranceInfo = "INSURANCE123",
        certifications = listOf("Licensed"),
        rating = 4.5,
        totalReviews = 10,
        contractStart = "2024-01-01",
        contractEnd = "2024-12-31",
        isActive = isActive
    )

    private fun sampleWorkOrder(id: String = "WO-1") = WorkOrder(
        id = id,
        title = "Fix leaking pipe",
        description = "Kitchen sink pipe is leaking",
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
        propertyId = "PROPERTY-001",
        reporterId = "USER-001",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        attachments = emptyList(),
        notes = emptyList()
    )

    private inline fun <reified T> errorRetrofit(code: Int): Response<T> =
        Response.error(
            code,
            "{\"error\":\"$code\"}".toResponseBody("application/json".toMediaType())
        )

    @Test
    fun `getVendors returns success when API returns valid response`() = runTest {
        val mockData = listOf(sampleVendor())
        val mockResponse = VendorResponse(data = mockData)
        `when`(runBlocking { apiService.getVendors() }).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        val body = result.getOrNull()
        assertNotNull(body)
        assertEquals(mockData, body!!.data)
    }

    @Test
    fun `getVendors returns failure when HTTP response is unsuccessful`() = runTest {
        `when`(runBlocking { apiService.getVendors() }).thenReturn(errorRetrofit(404))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendors returns failure when network throws IOException`() = runTest {
        val failing = Mockito.mock(ApiService::class.java) {
            throw IOException("Network error")
        }

        val result = VendorRepositoryImpl(failing).getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getVendors returns failure when response body is null`() = runTest {
        `when`(runBlocking { apiService.getVendors() }).thenReturn(Response.success(null))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getVendors returns success with empty list`() = runTest {
        val mockResponse = VendorResponse(data = emptyList())
        `when`(runBlocking { apiService.getVendors() }).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.data.isEmpty())
    }

    @Test
    fun `getVendors serves the second call from cache`() = runTest {
        val mockResponse = VendorResponse(data = listOf(sampleVendor()))
        `when`(runBlocking { apiService.getVendors() }).thenReturn(Response.success(mockResponse))

        val first = repository.getVendors()
        val second = repository.getVendors()

        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)
        assertEquals(first.getOrNull(), second.getOrNull())
        verify(apiService, times(1)).getVendors()
    }

    @Test
    fun `getVendor returns success when API returns valid response`() = runTest {
        val mockVendor = sampleVendor(id = "7", name = "Vendor 7")
        val mockResponse = SingleVendorResponse(data = mockVendor)
        `when`(runBlocking { apiService.getVendor("7") }).thenReturn(Response.success(mockResponse))

        val result = repository.getVendor("7")

        assertTrue(result.isSuccess)
        assertEquals(mockVendor, result.getOrNull()!!.data)
    }

    @Test
    fun `getVendor returns failure when vendor not found`() = runTest {
        `when`(runBlocking { apiService.getVendor("999") }).thenReturn(errorRetrofit(404))

        val result = repository.getVendor("999")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendor returns failure when network throws IOException`() = runTest {
        val failing = Mockito.mock(ApiService::class.java) {
            throw IOException("boom")
        }

        val result = VendorRepositoryImpl(failing).getVendor("3")

        assertTrue(result.isFailure)
    }

    @Test
    fun `createVendor returns success on valid input`() = runTest {
        val mockResponse = SingleVendorResponse(data = sampleVendor())
        `when`(
            runBlocking {
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
            }
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
    fun `createVendor returns failure on HTTP 400`() = runTest {
        `when`(
            runBlocking {
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
            }
        ).thenReturn(errorRetrofit(400))

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
        val mockResponse = SingleVendorResponse(
            data = sampleVendor(name = "Updated Vendor 1", isActive = false)
        )
        `when`(
            runBlocking {
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
            }
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
        assertEquals(false, result.getOrNull()!!.data.isActive)
    }

    @Test
    fun `getWorkOrders returns success when API returns valid response`() = runTest {
        val mockResponse = WorkOrderResponse(data = emptyList())
        `when`(runBlocking { apiService.getWorkOrders() }).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.data.isEmpty())
    }

    @Test
    fun `getWorkOrders returns failure on HTTP 500`() = runTest {
        `when`(runBlocking { apiService.getWorkOrders() }).thenReturn(errorRetrofit(500))

        val result = repository.getWorkOrders()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getWorkOrder returns success when API returns valid response`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = sampleWorkOrder(id = "WO-1"))
        `when`(runBlocking { apiService.getWorkOrder("WO-1") })
            .thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrder("WO-1")

        assertTrue(result.isSuccess)
        assertEquals("WO-1", result.getOrNull()!!.data.id)
    }

    @Test
    fun `getWorkOrder returns failure when not found`() = runTest {
        `when`(runBlocking { apiService.getWorkOrder("nope") }).thenReturn(errorRetrofit(404))

        val result = repository.getWorkOrder("nope")

        assertTrue(result.isFailure)
    }

    @Test
    fun `createWorkOrder returns success on valid input`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = sampleWorkOrder())
        `when`(
            runBlocking {
                apiService.createWorkOrder(
                    title = "Fix Leaking Pipe",
                    description = "Pipe is leaking in bathroom",
                    category = "Plumbing",
                    priority = "high",
                    propertyId = "prop1",
                    reporterId = "user1",
                    estimatedCost = 150.0
                )
            }
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
    fun `assignVendorToWorkOrder returns success`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = sampleWorkOrder())
        `when`(
            runBlocking {
                apiService.assignVendorToWorkOrder(
                    id = "WO-1",
                    vendorId = "vendor1",
                    scheduledDate = "2024-01-15"
                )
            }
        ).thenReturn(Response.success(mockResponse))

        val result = repository.assignVendorToWorkOrder(
            workOrderId = "WO-1",
            vendorId = "vendor1",
            scheduledDate = "2024-01-15"
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `assignVendorToWorkOrder returns failure on server error`() = runTest {
        `when`(
            runBlocking {
                apiService.assignVendorToWorkOrder(
                    id = "WO-1",
                    vendorId = "vendor1",
                    scheduledDate = null
                )
            }
        ).thenReturn(errorRetrofit(500))

        val result = repository.assignVendorToWorkOrder(
            workOrderId = "WO-1",
            vendorId = "vendor1",
            scheduledDate = null
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateWorkOrderStatus returns success`() = runTest {
        val mockResponse = SingleWorkOrderResponse(
            data = sampleWorkOrder().copy(status = "in_progress")
        )
        `when`(
            runBlocking {
                apiService.updateWorkOrderStatus(
                    id = "WO-1",
                    status = "in_progress",
                    notes = "Work started"
                )
            }
        ).thenReturn(Response.success(mockResponse))

        val result = repository.updateWorkOrderStatus(
            workOrderId = "WO-1",
            status = "in_progress",
            notes = "Work started"
        )

        assertTrue(result.isSuccess)
        assertEquals("in_progress", result.getOrNull()!!.data.status)
    }

    @Test
    fun `updateWorkOrderStatus returns failure on HTTP 400`() = runTest {
        `when`(
            runBlocking {
                apiService.updateWorkOrderStatus(
                    id = "WO-1",
                    status = "",
                    notes = null
                )
            }
        ).thenReturn(errorRetrofit(400))

        val result = repository.updateWorkOrderStatus(
            workOrderId = "WO-1",
            status = "",
            notes = null
        )

        assertTrue(result.isFailure)
    }
}