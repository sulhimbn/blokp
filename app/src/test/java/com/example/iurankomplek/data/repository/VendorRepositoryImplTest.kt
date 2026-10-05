package com.example.iurankomplek.data.repository

import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.data.api.models.SingleVendorResponse
import com.example.iurankomplek.data.api.models.SingleWorkOrderResponse
import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.data.api.models.WorkOrderResponse
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response
import java.io.IOException

/**
 * Covers [VendorRepositoryImpl].
 *
 * Two behaviours worth pinning down:
 *  - every response is cached, so a repeat call must not re-hit the API;
 *  - `createVendor` / `updateVendor` invalidate that cache, otherwise a newly created
 *    vendor would not show up in the next `getVendors`.
 *
 * [CacheManager.getInstance] is process-wide, so it is cleared around every test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VendorRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: VendorRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    private fun errorBody(message: String) = message.toResponseBody("text/plain".toMediaType())

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
    fun `getVendors returns the vendor list on a successful response`() = runTest {
        val mockResponse = VendorResponse(data = listOf(TestFixtures.vendor(id = "1", name = "Vendor 1")))
        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertEquals("Vendor 1", result.getOrNull()?.data?.single()?.name)
    }

    @Test
    fun `getVendors fails when the HTTP status is an error`() = runTest {
        `when`(apiService.getVendors())
            .thenReturn(Response.error(500, errorBody("Internal Server Error")))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getVendors fails when the transport throws`() = runTest {
        `when`(apiService.getVendors()).thenAnswer { throw IOException("Network error") }

        val result = repository.getVendors()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getVendors surfaces an empty list as success`() = runTest {
        `when`(apiService.getVendors()).thenReturn(Response.success(VendorResponse(data = emptyList())))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `a second getVendors call is served from cache`() = runTest {
        `when`(apiService.getVendors())
            .thenReturn(Response.success(VendorResponse(data = listOf(TestFixtures.vendor()))))

        repository.getVendors()
        repository.getVendors()

        verify(apiService, times(1)).getVendors()
    }

    @Test
    fun `getVendor returns the requested vendor`() = runTest {
        val mockResponse = SingleVendorResponse(data = TestFixtures.vendor(id = "1", name = "Vendor 1"))
        `when`(apiService.getVendor("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getVendor("1")

        assertTrue(result.isSuccess)
        assertEquals("Vendor 1", result.getOrNull()?.data?.name)
        assertEquals("Budi Santoso", result.getOrNull()?.data?.contactPerson)
    }

    @Test
    fun `getVendor fails when the vendor does not exist`() = runTest {
        `when`(apiService.getVendor("404"))
            .thenReturn(Response.error(404, errorBody("Not Found")))

        val result = repository.getVendor("404")

        assertTrue(result.isFailure)
    }

    @Test
    fun `createVendor forwards every field to the API`() = runTest {
        val mockResponse = SingleVendorResponse(data = TestFixtures.vendor(id = "new-1", name = "Acme"))
        `when`(
            apiService.createVendor(
                name = "Acme",
                contactPerson = "Budi Santoso",
                phoneNumber = "08123456789",
                email = "acme@example.com",
                specialty = "plumbing",
                address = "Jl. Vendor 1",
                licenseNumber = "LIC-001",
                insuranceInfo = "INS-001",
                contractStart = "2024-01-01",
                contractEnd = "2025-01-01"
            )
        ).thenReturn(Response.success(mockResponse))

        val result = repository.createVendor(
            name = "Acme",
            contactPerson = "Budi Santoso",
            phoneNumber = "08123456789",
            email = "acme@example.com",
            specialty = "plumbing",
            address = "Jl. Vendor 1",
            licenseNumber = "LIC-001",
            insuranceInfo = "INS-001",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        assertTrue(result.isSuccess)
        assertEquals("new-1", result.getOrNull()?.data?.id)
    }

    @Test
    fun `createVendor fails when the API rejects the payload`() = runTest {
        `when`(
            apiService.createVendor(
                name = "Acme",
                contactPerson = "Budi Santoso",
                phoneNumber = "08123456789",
                email = "acme@example.com",
                specialty = "plumbing",
                address = "Jl. Vendor 1",
                licenseNumber = "LIC-001",
                insuranceInfo = "INS-001",
                contractStart = "2024-01-01",
                contractEnd = "2025-01-01"
            )
        ).thenReturn(Response.error(400, errorBody("Bad Request")))

        val result = repository.createVendor(
            name = "Acme",
            contactPerson = "Budi Santoso",
            phoneNumber = "08123456789",
            email = "acme@example.com",
            specialty = "plumbing",
            address = "Jl. Vendor 1",
            licenseNumber = "LIC-001",
            insuranceInfo = "INS-001",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `updateVendor forwards the id and isActive flag`() = runTest {
        val mockResponse = SingleVendorResponse(
            data = TestFixtures.vendor(id = "1", name = "Acme Renamed", isActive = false)
        )
        `when`(
            apiService.updateVendor(
                id = "1",
                name = "Acme Renamed",
                contactPerson = "Budi Santoso",
                phoneNumber = "08123456789",
                email = "acme@example.com",
                specialty = "plumbing",
                address = "Jl. Vendor 1",
                licenseNumber = "LIC-001",
                insuranceInfo = "INS-001",
                contractStart = "2024-01-01",
                contractEnd = "2025-01-01",
                isActive = false
            )
        ).thenReturn(Response.success(mockResponse))

        val result = repository.updateVendor(
            id = "1",
            name = "Acme Renamed",
            contactPerson = "Budi Santoso",
            phoneNumber = "08123456789",
            email = "acme@example.com",
            specialty = "plumbing",
            address = "Jl. Vendor 1",
            licenseNumber = "LIC-001",
            insuranceInfo = "INS-001",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01",
            isActive = false
        )

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrNull()?.data?.isActive)
    }

    @Test
    fun `getWorkOrders returns the work order list`() = runTest {
        val mockResponse = WorkOrderResponse(data = listOf(TestFixtures.workOrder(id = "wo1")))
        `when`(apiService.getWorkOrders()).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertEquals("Leaking pipe", result.getOrNull()?.data?.single()?.title)
    }

    @Test
    fun `getWorkOrder returns the requested work order`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = TestFixtures.workOrder(id = "1"))
        `when`(apiService.getWorkOrder("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrder("1")

        assertTrue(result.isSuccess)
        assertEquals("1", result.getOrNull()?.data?.id)
    }

    @Test
    fun `createWorkOrder forwards every field to the API`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = TestFixtures.workOrder(id = "new-wo"))
        `when`(
            apiService.createWorkOrder(
                title = "Fix Leaking Pipe",
                description = "Pipe is leaking in bathroom",
                category = "Plumbing",
                priority = "high",
                propertyId = "Blok A",
                reporterId = "user-1",
                estimatedCost = 250.0
            )
        ).thenReturn(Response.success(mockResponse))

        val result = repository.createWorkOrder(
            title = "Fix Leaking Pipe",
            description = "Pipe is leaking in bathroom",
            category = "Plumbing",
            priority = "high",
            propertyId = "Blok A",
            reporterId = "user-1",
            estimatedCost = 250.0
        )

        assertTrue(result.isSuccess)
        assertEquals("new-wo", result.getOrNull()?.data?.id)
    }

    @Test
    fun `assignVendorToWorkOrder forwards the id and vendor`() = runTest {
        val mockResponse = SingleWorkOrderResponse(
            data = TestFixtures.workOrder(id = "wo1", status = "assigned", vendorId = "vendor1")
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
        assertEquals("assigned", result.getOrNull()?.data?.status)
        assertEquals("vendor1", result.getOrNull()?.data?.vendorId)
    }

    @Test
    fun `updateWorkOrderStatus forwards the new status`() = runTest {
        val mockResponse = SingleWorkOrderResponse(
            data = TestFixtures.workOrder(id = "wo1", status = "in_progress")
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

    @Test
    fun `updateWorkOrderStatus accepts a null notes field`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = TestFixtures.workOrder(id = "wo1"))
        `when`(
            apiService.updateWorkOrderStatus(id = "wo1", status = "completed", notes = null)
        ).thenReturn(Response.success(mockResponse))

        val result = repository.updateWorkOrderStatus(workOrderId = "wo1", status = "completed", notes = null)

        assertTrue(result.isSuccess)
    }
}