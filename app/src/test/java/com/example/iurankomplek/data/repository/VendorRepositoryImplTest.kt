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
        Dispatchers.setMain(testDispatcher)
        // VendorRepositoryImpl memoises into the process-wide CacheManager singleton,
        // so a cache left over from an earlier test would short-circuit the API mock.
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
        val mockResponse = VendorResponse(data = listOf(TestFixtures.vendor()))

        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
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
        val mockResponse = SingleVendorResponse(data = TestFixtures.vendor())

        `when`(apiService.getVendor("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getVendor("1")

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
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
        val mockResponse = SingleVendorResponse(data = TestFixtures.vendor())

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
        assertNotNull(result.getOrNull())
    }

    @Test
    fun `createVendor should return failure on invalid input`() = runTest {
        val errorResponse = Response.error<SingleVendorResponse>(
            400,
            okhttp3.ResponseBody.create(null, "Bad Request")
        )

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
        val updated = TestFixtures.vendor(
            id = "1",
            name = "Updated Vendor 1",
            email = "updated@example.com",
            specialty = "Updated Cleaning",
            address = "456 Oak Ave",
            rating = 5.0
        )
        val mockResponse = SingleVendorResponse(data = updated)

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
        assertEquals(updated, result.getOrNull()?.data)
    }

    @Test
    fun `getWorkOrders should return success when API returns valid response`() = runTest {
        val mockResponse = WorkOrderResponse(data = emptyList())

        `when`(apiService.getWorkOrders()).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `getWorkOrder should return success when API returns valid response`() = runTest {
        val workOrder = TestFixtures.workOrder(id = "1")
        val mockResponse = SingleWorkOrderResponse(data = workOrder)

        `when`(apiService.getWorkOrder("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrder("1")

        assertTrue(result.isSuccess)
        assertEquals(workOrder, result.getOrNull()?.data)
    }

    @Test
    fun `createWorkOrder should return success on valid input`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = TestFixtures.workOrder())

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
        val mockResponse = SingleWorkOrderResponse(data = TestFixtures.workOrder(id = "wo1"))

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
        val mockResponse = SingleWorkOrderResponse(
            data = TestFixtures.workOrder(id = "wo1", status = "in_progress")
        )

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