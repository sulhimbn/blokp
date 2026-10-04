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
    private val testDispatcher = StandardTestDispatcher()

    private fun vendor(
        id: String = "1",
        name: String = "Vendor 1",
        specialty: String = "Cleaning",
        rating: Double = 4.5,
        isActive: Boolean = true
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
        rating = rating,
        totalReviews = 10,
        contractStart = "2024-01-01",
        contractEnd = "2024-12-31",
        isActive = isActive
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
        createdAt = "2024-01-10T00:00:00Z",
        updatedAt = "2024-01-10T00:00:00Z",
        attachments = emptyList(),
        notes = emptyList()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        CacheManager.getInstance().clearSync()
        Dispatchers.setMain(testDispatcher)
        repository = VendorRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getVendors returns success carrying the parsed payload`() = runTest {
        val mockResponse = VendorResponse(data = listOf(vendor()))
        `when`(apiService.getVendors()).thenReturn(Response.success(mockResponse))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getVendors returns error for an unsuccessful HTTP status`() = runTest {
        `when`(apiService.getVendors())
            .thenReturn(Response.error<VendorResponse>(404, "Not Found".toResponseBody(null)))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendors returns error when the network call throws IOException`() = runTest {
        `when`(apiService.getVendors()).thenAnswer { throw IOException("Network error") }

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getVendors returns an empty payload successfully`() = runTest {
        `when`(apiService.getVendors()).thenReturn(Response.success(VendorResponse(data = emptyList())))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getVendor returns success carrying the requested vendor`() = runTest {
        val mockResponse = SingleVendorResponse(data = vendor())
        `when`(apiService.getVendor("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getVendor("1")

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getVendor returns error when the vendor is not found`() = runTest {
        `when`(apiService.getVendor("999"))
            .thenReturn(Response.error<SingleVendorResponse>(404, "Not Found".toResponseBody(null)))

        val result = repository.getVendor("999")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `createVendor returns success on valid input`() = runTest {
        val mockResponse = SingleVendorResponse(data = vendor())
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
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `createVendor returns error when the API rejects the payload`() = runTest {
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
        ).thenReturn(Response.error<SingleVendorResponse>(400, "Bad Request".toResponseBody(null)))

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
    fun `updateVendor returns success on valid update`() = runTest {
        val mockResponse = SingleVendorResponse(data = vendor(name = "Updated Vendor 1", rating = 5.0))
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
    fun `getWorkOrders returns success for an empty list`() = runTest {
        `when`(apiService.getWorkOrders())
            .thenReturn(Response.success(WorkOrderResponse(data = emptyList())))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.data?.isEmpty() == true)
    }

    @Test
    fun `getWorkOrders returns success carrying the parsed work orders`() = runTest {
        val mockResponse = WorkOrderResponse(data = listOf(workOrder()))
        `when`(apiService.getWorkOrders()).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `getWorkOrder returns success carrying the requested work order`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder())
        `when`(apiService.getWorkOrder("1")).thenReturn(Response.success(mockResponse))

        val result = repository.getWorkOrder("1")

        assertTrue(result.isSuccess)
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `createWorkOrder returns success on valid input`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder())
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
        assertEquals(mockResponse, result.getOrNull())
    }

    @Test
    fun `assignVendorToWorkOrder returns success`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder())
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
    }

    @Test
    fun `assignVendorToWorkOrder accepts a null schedule date`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder())
        `when`(
            apiService.assignVendorToWorkOrder(
                id = "wo1",
                vendorId = "vendor1",
                scheduledDate = null
            )
        ).thenReturn(Response.success(mockResponse))

        val result = repository.assignVendorToWorkOrder(
            workOrderId = "wo1",
            vendorId = "vendor1",
            scheduledDate = null
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `updateWorkOrderStatus returns success`() = runTest {
        val mockResponse = SingleWorkOrderResponse(data = workOrder())
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
    }
}