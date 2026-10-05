package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.api.models.SingleVendorResponse
import com.example.iurankomplek.data.api.models.SingleWorkOrderResponse
import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.data.api.models.WorkOrderResponse
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.WorkOrder
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.CacheManager
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/**
 * Repository-level tests driven through a real Retrofit client against MockWebServer.
 *
 * ApiService is a suspend-only Retrofit interface, which Mockito cannot stub reliably,
 * so the transport boundary is faked at the socket level instead. That also lets these
 * tests assert the actual HTTP contract (paths, verbs, query parameters) rather than
 * only the repository's branching.
 */
class VendorRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: VendorRepositoryImpl
    private val gson = Gson()

    private fun vendor(id: String = "v1", name: String = "Vendor 1") = Vendor(
        id = id,
        name = name,
        contactPerson = "John Doe",
        phoneNumber = "1234567890",
        email = "vendor1@example.com",
        specialty = "Cleaning",
        address = "123 Main St",
        licenseNumber = "LICENSE123",
        insuranceInfo = "INSURANCE123",
        certifications = listOf("ISO9001"),
        rating = 4.5,
        totalReviews = 12,
        contractStart = "2024-01-01",
        contractEnd = "2024-12-31",
        isActive = true
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

    private fun jsonResponse(code: Int, body: Any): MockResponse =
        MockResponse()
            .setResponseCode(code)
            .setHeader("Content-Type", "application/json")
            .setBody(gson.toJson(body))

    private fun buildService(baseUrl: String): ApiService =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

    @Before
    fun setup() {
        // CacheManager is a process-wide singleton; without clearing it, a cached
        // response from a previous test would satisfy the next one and the network
        // assertions below would silently stop being exercised.
        CacheManager.getInstance().clearSync()

        server = MockWebServer()
        server.start()
        repository = VendorRepositoryImpl(buildService(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
        CacheManager.getInstance().clearSync()
    }

    @Test
    fun `getVendors returns parsed vendors on 200`() = runTest {
        server.enqueue(jsonResponse(200, VendorResponse(listOf(vendor()))))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        val body = result.getOrNull()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("Vendor 1", body.data[0].name)
        assertEquals(4.5, body.data[0].rating, 0.0)
        assertEquals("/vendors", server.takeRequest().path)
    }

    @Test
    fun `getVendors returns HttpException on 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `getVendors returns IOException when transport fails`() = runTest {
        // Port 1 is not listening, so the call fails at the socket layer.
        val offline = VendorRepositoryImpl(buildService("http://127.0.0.1:1/"))

        val result = offline.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getVendors returns failure when body is null`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(""))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
    }

    @Test
    fun `getVendors returns empty list on 200`() = runTest {
        server.enqueue(jsonResponse(200, VendorResponse(emptyList())))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.data.isEmpty())
    }

    @Test
    fun `getVendors serves the second call from cache without a second request`() = runTest {
        server.enqueue(jsonResponse(200, VendorResponse(listOf(vendor()))))

        val first = repository.getVendors()
        val second = repository.getVendors()

        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `getVendor targets the id path segment`() = runTest {
        server.enqueue(jsonResponse(200, SingleVendorResponse(vendor(id = "v42"))))

        val result = repository.getVendor("v42")

        assertTrue(result.isSuccess)
        assertEquals("v42", result.getOrNull()!!.data.id)
        assertEquals("/vendors/v42", server.takeRequest().path)
    }

    @Test
    fun `getVendor returns HttpException on 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val result = repository.getVendor("999")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `createVendor posts to vendors and returns the created vendor`() = runTest {
        server.enqueue(jsonResponse(201, SingleVendorResponse(vendor())))

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
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.startsWith("/vendors"))
        assertTrue(recorded.path.orEmpty().contains("licenseNumber=LICENSE123"))
    }

    @Test
    fun `createVendor returns HttpException on 400`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("Bad Request"))

        val result = repository.createVendor(
            name = "", contactPerson = "", phoneNumber = "", email = "",
            specialty = "", address = "", licenseNumber = "",
            insuranceInfo = "", contractStart = "", contractEnd = ""
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
    }

    @Test
    fun `updateVendor puts to the id path and includes isActive`() = runTest {
        server.enqueue(jsonResponse(200, SingleVendorResponse(vendor(name = "Updated Vendor 1"))))

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
        assertEquals("Updated Vendor 1", result.getOrNull()!!.data.name)
        val recorded = server.takeRequest()
        assertEquals("PUT", recorded.method)
        assertTrue(recorded.path!!.startsWith("/vendors/1"))
        assertTrue(recorded.path!!.contains("isActive=true"))
    }

    @Test
    fun `getWorkOrders returns the list on 200`() = runTest {
        server.enqueue(jsonResponse(200, WorkOrderResponse(listOf(workOrder()))))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.data.size)
        assertEquals("/work-orders", server.takeRequest().path)
    }

    @Test
    fun `getWorkOrder targets the id path segment`() = runTest {
        server.enqueue(jsonResponse(200, SingleWorkOrderResponse(workOrder(id = "wo7"))))

        val result = repository.getWorkOrder("wo7")

        assertTrue(result.isSuccess)
        assertEquals("wo7", result.getOrNull()!!.data.id)
        assertEquals("/work-orders/wo7", server.takeRequest().path)
    }

    @Test
    fun `createWorkOrder posts estimated cost and returns the work order`() = runTest {
        server.enqueue(jsonResponse(201, SingleWorkOrderResponse(workOrder())))

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
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.startsWith("/work-orders"))
        assertTrue(recorded.path.orEmpty().contains("estimatedCost=150.0"))
    }

    @Test
    fun `assignVendorToWorkOrder puts to the assign sub-resource`() = runTest {
        server.enqueue(jsonResponse(200, SingleWorkOrderResponse(workOrder())))

        val result = repository.assignVendorToWorkOrder(
            workOrderId = "wo1",
            vendorId = "vendor1",
            scheduledDate = "2024-01-15"
        )

        assertTrue(result.isSuccess)
        val recorded = server.takeRequest()
        assertEquals("PUT", recorded.method)
        assertTrue(recorded.path!!.startsWith("/work-orders/wo1/assign"))
        assertTrue(recorded.path!!.contains("vendorId=vendor1"))
    }

    @Test
    fun `updateWorkOrderStatus puts to the status sub-resource`() = runTest {
        server.enqueue(jsonResponse(200, SingleWorkOrderResponse(workOrder())))

        val result = repository.updateWorkOrderStatus(
            workOrderId = "wo1",
            status = "in_progress",
            notes = "Work started"
        )

        assertTrue(result.isSuccess)
        val recorded = server.takeRequest()
        assertEquals("PUT", recorded.method)
        assertTrue(recorded.path!!.startsWith("/work-orders/wo1/status"))
        assertTrue(recorded.path!!.contains("status=in_progress"))
    }
}