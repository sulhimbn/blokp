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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
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
        CacheManager.getInstance().clearSync()
        repository = VendorRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vendor(id: String = "1") = Vendor(
        id = id,
        name = "Vendor $id",
        contactPerson = "John Smith",
        phoneNumber = "123-456-7890",
        email = "vendor$id@example.com",
        specialty = "Cleaning",
        address = "123 Main St",
        licenseNumber = "LIC-$id",
        insuranceInfo = "General liability",
        certifications = listOf("Licensed"),
        rating = 4.5,
        totalReviews = 12,
        contractStart = "2024-01-01",
        contractEnd = "2025-01-01",
        isActive = true
    )

    private fun workOrder(
        id: String = "wo-1",
        status: String = "pending",
        vendorId: String? = null
    ) = WorkOrder(
        id = id,
        title = "Leaking pipe",
        description = "Kitchen sink is leaking",
        category = "plumbing",
        priority = "high",
        status = status,
        vendorId = vendorId,
        vendorName = vendorId?.let { "Vendor $it" },
        assignedAt = vendorId?.let { "2024-05-01" },
        scheduledDate = null,
        completedAt = null,
        estimatedCost = 250.0,
        actualCost = 0.0,
        propertyId = "block-a",
        reporterId = "resident-1",
        createdAt = "2024-04-30",
        updatedAt = "2024-04-30",
        attachments = emptyList(),
        notes = emptyList()
    )

    private fun errorBody(message: String) = message.toResponseBody("text/plain".toMediaType())

    @Test
    fun `getVendors returns the vendor list on a successful response`() = runTest(testDispatcher) {
        val expected = VendorResponse(data = listOf(vendor("1"), vendor("2")))
        `when`(apiService.getVendors()).thenReturn(Response.success(expected))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `getVendors fails when the response body is missing`() = runTest(testDispatcher) {
        `when`(apiService.getVendors()).thenReturn(Response.success(null))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertEquals("Response body is null", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getVendors fails when the server returns an error status`() = runTest(testDispatcher) {
        `when`(apiService.getVendors())
            .thenReturn(Response.error(500, errorBody("Internal Server Error")))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is retrofit2.HttpException)
    }

    @Test
    fun `getVendors fails when the network throws`() = runTest(testDispatcher) {
        `when`(apiService.getVendors()).thenAnswer { throw IOException("Network error") }

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getVendors serves the second call from cache`() = runTest(testDispatcher) {
        `when`(apiService.getVendors())
            .thenReturn(Response.success(VendorResponse(data = listOf(vendor()))))

        repository.getVendors()
        repository.getVendors()

        verify(apiService, times(1)).getVendors()
    }

    @Test
    fun `getVendors returns the same data when the cache entry has not expired`() =
        runTest(testDispatcher) {
            `when`(apiService.getVendors())
                .thenReturn(Response.success(VendorResponse(data = listOf(vendor()))))

            val first = repository.getVendors()
            val second = repository.getVendors()

            assertNotNull(first.getOrNull())
            assertEquals(first.getOrNull(), second.getOrNull())
        }

    @Test
    fun `getVendor returns the single vendor for the requested id`() = runTest(testDispatcher) {
        val expected = SingleVendorResponse(data = vendor("7"))
        `when`(apiService.getVendor("7")).thenReturn(Response.success(expected))

        val result = repository.getVendor("7")

        assertTrue(result.isSuccess)
        assertEquals("7", result.getOrNull()?.data?.id)
    }

    @Test
    fun `getVendor fails when the vendor is missing`() = runTest(testDispatcher) {
        `when`(apiService.getVendor("404"))
            .thenReturn(Response.error(404, errorBody("Not Found")))

        val result = repository.getVendor("404")

        assertTrue(result.isFailure)
    }

    @Test
    fun `createVendor posts every field and returns the created vendor`() =
        runTest(testDispatcher) {
            val expected = SingleVendorResponse(data = vendor("9"))
            `when`(
                apiService.createVendor(
                    "Vendor 9", "John Smith", "123-456-7890", "vendor9@example.com",
                    "Cleaning", "123 Main St", "LIC-9", "General liability",
                    "2024-01-01", "2025-01-01"
                )
            ).thenReturn(Response.success(expected))

            val result = repository.createVendor(
                "Vendor 9", "John Smith", "123-456-7890", "vendor9@example.com",
                "Cleaning", "123 Main St", "LIC-9", "General liability",
                "2024-01-01", "2025-01-01"
            )

            assertTrue(result.isSuccess)
            assertEquals("9", result.getOrNull()?.data?.id)
        }

    @Test
    fun `createVendor fails when the response body is missing`() = runTest(testDispatcher) {
        `when`(
            apiService.createVendor(
                anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString()
            )
        ).thenReturn(Response.success(null))

        val result = repository.createVendor(
            "n", "c", "p", "e", "s", "a", "l", "i", "2024-01-01", "2025-01-01"
        )

        assertTrue(result.isFailure)
        assertEquals("Response body is null", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateVendor forwards the vendor id and every editable field`() =
        runTest(testDispatcher) {
            val expected = SingleVendorResponse(data = vendor("5").copy(isActive = false))
            `when`(
                apiService.updateVendor(
                    "5", "Vendor 5", "John Smith", "123-456-7890", "vendor5@example.com",
                    "Cleaning", "123 Main St", "LIC-5", "General liability",
                    "2024-01-01", "2025-01-01", false
                )
            ).thenReturn(Response.success(expected))

            val result = repository.updateVendor(
                "5", "Vendor 5", "John Smith", "123-456-7890", "vendor5@example.com",
                "Cleaning", "123 Main St", "LIC-5", "General liability",
                "2024-01-01", "2025-01-01", false
            )

            assertTrue(result.isSuccess)
            assertEquals(false, result.getOrNull()?.data?.isActive)
        }

    @Test
    fun `getWorkOrders returns the work order list`() = runTest(testDispatcher) {
        val expected = WorkOrderResponse(data = listOf(workOrder(), workOrder("wo-2")))
        `when`(apiService.getWorkOrders()).thenReturn(Response.success(expected))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.data?.size)
    }

    @Test
    fun `getWorkOrders fails on an error status`() = runTest(testDispatcher) {
        `when`(apiService.getWorkOrders())
            .thenReturn(Response.error(503, errorBody("Service Unavailable")))

        val result = repository.getWorkOrders()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getWorkOrder returns the single work order for the requested id`() =
        runTest(testDispatcher) {
            val expected = SingleWorkOrderResponse(data = workOrder("wo-42"))
            `when`(apiService.getWorkOrder("wo-42")).thenReturn(Response.success(expected))

            val result = repository.getWorkOrder("wo-42")

            assertTrue(result.isSuccess)
            assertEquals("wo-42", result.getOrNull()?.data?.id)
        }

    @Test
    fun `getWorkOrder fails when the response body is missing`() = runTest(testDispatcher) {
        `when`(apiService.getWorkOrder("wo-1")).thenReturn(Response.success(null))

        val result = repository.getWorkOrder("wo-1")

        assertTrue(result.isFailure)
        assertEquals("Response body is null", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createWorkOrder posts every field and returns the created work order`() =
        runTest(testDispatcher) {
            val expected = SingleWorkOrderResponse(data = workOrder("wo-new"))
            `when`(
                apiService.createWorkOrder(
                    "Leaking pipe", "Kitchen sink is leaking", "plumbing",
                    "high", "block-a", "resident-1", 250.0
                )
            ).thenReturn(Response.success(expected))

            val result = repository.createWorkOrder(
                "Leaking pipe", "Kitchen sink is leaking", "plumbing",
                "high", "block-a", "resident-1", 250.0
            )

            assertTrue(result.isSuccess)
            assertEquals("wo-new", result.getOrNull()?.data?.id)
        }

    @Test
    fun `assignVendorToWorkOrder forwards the work order id and vendor`() =
        runTest(testDispatcher) {
            val expected = SingleWorkOrderResponse(data = workOrder("wo-1", "assigned", "3"))
            `when`(apiService.assignVendorToWorkOrder("wo-1", "3", null))
                .thenReturn(Response.success(expected))

            val result = repository.assignVendorToWorkOrder("wo-1", "3", null)

            assertTrue(result.isSuccess)
            assertEquals("3", result.getOrNull()?.data?.vendorId)
        }

    @Test
    fun `assignVendorToWorkOrder fails when the server rejects the assignment`() =
        runTest(testDispatcher) {
            `when`(apiService.assignVendorToWorkOrder(anyString(), anyString(), any()))
                .thenReturn(Response.error(422, errorBody("Unprocessable")))

            val result = repository.assignVendorToWorkOrder("wo-1", "3", null)

            assertTrue(result.isFailure)
        }

    @Test
    fun `updateWorkOrderStatus forwards the new status and notes`() = runTest(testDispatcher) {
        val expected = SingleWorkOrderResponse(data = workOrder("wo-1", "completed"))
        `when`(apiService.updateWorkOrderStatus("wo-1", "completed", "Fixed on site"))
            .thenReturn(Response.success(expected))

        val result = repository.updateWorkOrderStatus("wo-1", "completed", "Fixed on site")

        assertTrue(result.isSuccess)
        assertEquals("completed", result.getOrNull()?.data?.status)
    }

    @Test
    fun `updateWorkOrderStatus fails on an error status`() = runTest(testDispatcher) {
        `when`(apiService.updateWorkOrderStatus(anyString(), anyString(), any()))
            .thenReturn(Response.error(400, errorBody("Bad Request")))

        val result = repository.updateWorkOrderStatus("wo-1", "completed", null)

        assertTrue(result.isFailure)
    }
}