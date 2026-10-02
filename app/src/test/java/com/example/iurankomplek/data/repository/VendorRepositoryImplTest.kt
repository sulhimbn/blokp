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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class VendorRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: VendorRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    private fun vendor(id: String = "v1", active: Boolean = true) = Vendor(
        id = id,
        name = "Vendor 1",
        contactPerson = "John Doe",
        phoneNumber = "1234567890",
        email = "vendor1@example.com",
        specialty = "Cleaning",
        address = "123 Main St",
        licenseNumber = "LICENSE123",
        insuranceInfo = "INSURANCE123",
        certifications = listOf("PIPA"),
        rating = 4.5,
        totalReviews = 12,
        contractStart = "2024-01-01",
        contractEnd = "2024-12-31",
        isActive = active
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

    private fun errorResponse(code: Int, message: String) =
        Response.error<Any>(code, message.toResponseBody("text/plain".toMediaType()))

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
    fun `getVendors returns the payload the API produced`() = runTest {
        val expected = VendorResponse(listOf(vendor()))
        whenever(apiService.getVendors()).thenReturn(Response.success(expected))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getVendors returns HttpException for an unsuccessful response`() = runTest {
        whenever(apiService.getVendors())
            .thenReturn(errorResponse(404, "Not Found") as Response<VendorResponse>)

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendors surfaces an IOException as a failure`() = runTest {
        whenever(apiService.getVendors()).thenAnswer { throw IOException("Network error") }

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getVendors fails when a successful response carries no body`() = runTest {
        whenever(apiService.getVendors())
            .thenReturn(Response.success<VendorResponse>(null))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getVendors returns an empty list as a success, not a failure`() = runTest {
        whenever(apiService.getVendors())
            .thenReturn(Response.success(VendorResponse(emptyList())))

        val result = repository.getVendors()

        assertTrue("empty vendor list must not be an error", result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getVendors serves the second call from cache without hitting the API`() = runTest {
        val expected = VendorResponse(listOf(vendor()))
        whenever(apiService.getVendors()).thenReturn(Response.success(expected))

        val first = repository.getVendors()
        val second = repository.getVendors()

        assertEquals(expected, first.getOrNull())
        assertEquals(expected, second.getOrNull())
        org.mockito.Mockito.verify(apiService, org.mockito.Mockito.times(1)).getVendors()
    }

    @Test
    fun `getVendor returns the requested vendor`() = runTest {
        val expected = SingleVendorResponse(vendor("v1"))
        whenever(apiService.getVendor("v1")).thenReturn(Response.success(expected))

        val result = repository.getVendor("v1")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getVendor caches per vendor id`() = runTest {
        whenever(apiService.getVendor("v1"))
            .thenReturn(Response.success(SingleVendorResponse(vendor("v1"))))
        whenever(apiService.getVendor("v2"))
            .thenReturn(Response.success(SingleVendorResponse(vendor("v2"))))

        assertEquals("v1", repository.getVendor("v1").getOrNull()?.data?.id)
        assertEquals("v2", repository.getVendor("v2").getOrNull()?.data?.id)
        assertEquals("v2", repository.getVendor("v2").getOrNull()?.data?.id)

        org.mockito.Mockito.verify(apiService, org.mockito.Mockito.times(1)).getVendor("v2")
    }

    @Test
    fun `getVendor returns HttpException when the vendor is unknown`() = runTest {
        whenever(apiService.getVendor("999"))
            .thenReturn(errorResponse(404, "Not Found") as Response<SingleVendorResponse>)

        val result = repository.getVendor("999")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `createVendor forwards every field and returns the created vendor`() = runTest {
        whenever(apiService.createVendor(any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any()))
            .thenReturn(Response.success(SingleVendorResponse(vendor("v-new"))))

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
        assertEquals("v-new", result.getOrNull()?.data?.id)
    }

    @Test
    fun `createVendor invalidates the cached vendor list`() = runTest {
        whenever(apiService.getVendors())
            .thenReturn(Response.success(VendorResponse(listOf(vendor()))))
        repository.getVendors()
        whenever(apiService.createVendor(any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any()))
            .thenReturn(Response.success(SingleVendorResponse(vendor("v-new"))))

        repository.createVendor("V", "C", "P", "E", "S", "A", "L", "I", "2024-01-01", "2024-12-31")
        repository.getVendors()

        org.mockito.Mockito.verify(apiService, org.mockito.Mockito.times(2)).getVendors()
    }

    @Test
    fun `createVendor propagates a 400 as a failure`() = runTest {
        whenever(apiService.createVendor(any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any()))
            .thenReturn(errorResponse(400, "Bad Request") as Response<SingleVendorResponse>)

        val result = repository.createVendor(
            name = "", contactPerson = "", phoneNumber = "", email = "",
            specialty = "", address = "", licenseNumber = "", insuranceInfo = "",
            contractStart = "", contractEnd = ""
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `updateVendor returns the updated vendor`() = runTest {
        val updated = vendor("v1", active = false).copy(name = "Updated Vendor 1")
        whenever(apiService.updateVendor(eq("v1"), any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any(), eq(false)))
            .thenReturn(Response.success(SingleVendorResponse(updated)))

        val result = repository.updateVendor(
            id = "v1",
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
            isActive = false
        )

        assertTrue(result.isSuccess)
        assertEquals("Updated Vendor 1", result.getOrNull()?.data?.name)
    }

    @Test
    fun `updateVendor propagates a 404 as a failure`() = runTest {
        whenever(apiService.updateVendor(any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any(), any()))
            .thenReturn(errorResponse(404, "Not Found") as Response<SingleVendorResponse>)

        val result = repository.updateVendor(
            "missing", "N", "C", "P", "E", "S", "A", "L", "I", "2024-01-01", "2024-12-31", true
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getWorkOrders returns the list payload`() = runTest {
        val expected = WorkOrderResponse(listOf(workOrder()))
        whenever(apiService.getWorkOrders()).thenReturn(Response.success(expected))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getWorkOrder returns the requested work order`() = runTest {
        val expected = SingleWorkOrderResponse(workOrder("wo1"))
        whenever(apiService.getWorkOrder("wo1")).thenReturn(Response.success(expected))

        val result = repository.getWorkOrder("wo1")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getWorkOrder propagates a 500 as a failure`() = runTest {
        whenever(apiService.getWorkOrder("wo1"))
            .thenReturn(errorResponse(500, "Server Error") as Response<SingleWorkOrderResponse>)

        val result = repository.getWorkOrder("wo1")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `createWorkOrder forwards the payload and returns the created order`() = runTest {
        whenever(apiService.createWorkOrder(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Response.success(SingleWorkOrderResponse(workOrder("wo-new"))))

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
        assertEquals("wo-new", result.getOrNull()?.data?.id)
    }

    @Test
    fun `assignVendorToWorkOrder returns the assigned order`() = runTest {
        val assigned = workOrder().copy(vendorId = "vendor1", status = "assigned")
        whenever(apiService.assignVendorToWorkOrder(eq("wo1"), eq("vendor1"), eq("2024-01-15")))
            .thenReturn(Response.success(SingleWorkOrderResponse(assigned)))

        val result = repository.assignVendorToWorkOrder("wo1", "vendor1", "2024-01-15")

        assertTrue(result.isSuccess)
        assertEquals("vendor1", result.getOrNull()?.data?.vendorId)
    }

    @Test
    fun `assignVendorToWorkOrder accepts a null schedule`() = runTest {
        whenever(apiService.assignVendorToWorkOrder(eq("wo1"), eq("vendor1"), eq(null)))
            .thenReturn(Response.success(SingleWorkOrderResponse(workOrder())))

        val result = repository.assignVendorToWorkOrder("wo1", "vendor1", null)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun `updateWorkOrderStatus returns the updated order`() = runTest {
        val updated = workOrder().copy(status = "in_progress")
        whenever(apiService.updateWorkOrderStatus(eq("wo1"), eq("in_progress"), eq("Work started")))
            .thenReturn(Response.success(SingleWorkOrderResponse(updated)))

        val result = repository.updateWorkOrderStatus("wo1", "in_progress", "Work started")

        assertTrue(result.isSuccess)
        assertEquals("in_progress", result.getOrNull()?.data?.status)
    }

    @Test
    fun `updateWorkOrderStatus propagates a 500 as a failure`() = runTest {
        // any(Class) does not match null, so the nullable notes needs anyOrNull().
        whenever(apiService.updateWorkOrderStatus(any(), any(), anyOrNull()))
            .thenReturn(errorResponse(500, "Server Error") as Response<SingleWorkOrderResponse>)

        val result = repository.updateWorkOrderStatus("wo1", "in_progress", null)

        assertTrue(result.isFailure)
        assertTrue(
            "expected HttpException but was ${result.exceptionOrNull()}",
            result.exceptionOrNull() is HttpException
        )
    }
}
