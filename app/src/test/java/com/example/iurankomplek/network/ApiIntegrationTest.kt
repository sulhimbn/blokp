package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
import com.example.iurankomplek.data.api.models.SingleVendorResponse
import com.example.iurankomplek.data.api.models.SingleWorkOrderResponse
import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.data.api.models.WorkOrderResponse
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.WorkOrder
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Contract tests for the Retrofit layer: every [ApiService] endpoint is exercised over a real
 * HTTP connection against [MockWebServer], asserting the request that goes out (path + query)
 * and that the JSON body deserialises into the current model shapes.
 */
class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun enqueueJson(code: Int = 200, body: Any) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(body))
        )
    }

    private fun sampleDataItem() = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = "john.doe@example.com",
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )

    private fun sampleVendor() = Vendor(
        id = "1",
        name = "Plumbing Services Inc",
        contactPerson = "John Smith",
        phoneNumber = "123-456-7890",
        email = "contact@plumbing.com",
        specialty = "plumbing",
        address = "123 Main St",
        licenseNumber = "PL-12345",
        insuranceInfo = "General liability coverage",
        certifications = listOf("Licensed", "Bonded"),
        rating = 4.5,
        totalReviews = 25,
        contractStart = "2023-01-01",
        contractEnd = "2024-12-31",
        isActive = true
    )

    private fun sampleWorkOrder() = WorkOrder(
        id = "wo-1",
        title = "Leaking pipe",
        description = "Water leak in unit 12",
        category = "plumbing",
        priority = "high",
        status = "pending",
        vendorId = null,
        vendorName = null,
        assignedAt = null,
        scheduledDate = null,
        completedAt = null,
        estimatedCost = 500000.0,
        actualCost = 0.0,
        propertyId = "unit-12",
        reporterId = "user-1",
        createdAt = "2023-01-01T00:00:00Z",
        updatedAt = "2023-01-01T00:00:00Z",
        attachments = emptyList(),
        notes = emptyList()
    )

    @Test
    fun `getUsers should parse response correctly`() = runBlocking {
        enqueueJson(body = UserResponse(data = listOf(sampleDataItem())))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertEquals("/users", mockWebServer.takeRequest().path)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("John", body.data[0].first_name)
        assertEquals("Doe", body.data[0].last_name)
        assertEquals("john.doe@example.com", body.data[0].email)
    }

    @Test
    fun `getUsers should handle empty response`() = runBlocking {
        enqueueJson(body = UserResponse(data = emptyList()))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertTrue(result.body()!!.data.isEmpty())
    }

    @Test
    fun `getUsers should surface server error code`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":"Internal Server Error"}""")
        )

        val result = apiService.getUsers()

        assertFalse(result.isSuccessful)
        assertEquals(500, result.code())
    }

    @Test
    fun `getPemanfaatan should parse financial response correctly`() = runBlocking {
        enqueueJson(
            body = PemanfaatanResponse(
                data = listOf(sampleDataItem().copy(first_name = "Jane", pemanfaatan_iuran = "Repairs"))
            )
        )

        val result = apiService.getPemanfaatan()

        assertTrue(result.isSuccessful)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
        assertEquals("Jane", result.body()!!.data[0].first_name)
        assertEquals("Repairs", result.body()!!.data[0].pemanfaatan_iuran)
    }

    @Test
    fun `getPemanfaatan should surface not found code`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":"Not found"}"""))

        val result = apiService.getPemanfaatan()

        assertFalse(result.isSuccessful)
        assertEquals(404, result.code())
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runBlocking {
        enqueueJson(
            body = listOf(
                Announcement(
                    id = "ann-1",
                    title = "Community Meeting",
                    content = "Meeting at 7 PM",
                    category = "community",
                    priority = "high",
                    createdAt = "2023-01-01T00:00:00Z",
                    readBy = listOf("user-1")
                )
            )
        )

        val result = apiService.getAnnouncements()

        assertTrue(result.isSuccessful)
        assertEquals("/announcements", mockWebServer.takeRequest().path)
        assertEquals("Community Meeting", result.body()!![0].title)
        assertEquals(listOf("user-1"), result.body()!![0].readBy)
    }

    @Test
    fun `getMessages should send userId query and parse response`() = runBlocking {
        enqueueJson(
            body = listOf(
                Message(
                    id = "msg-1",
                    senderId = "user-2",
                    receiverId = "user-1",
                    content = "Hello",
                    timestamp = "2023-01-01T00:00:00Z",
                    readStatus = false,
                    attachments = emptyList()
                )
            )
        )

        val result = apiService.getMessages(userId = "user-1")

        assertTrue(result.isSuccessful)
        assertEquals("/messages?userId=user-1", mockWebServer.takeRequest().path)
        assertEquals("Hello", result.body()!![0].content)
    }

    @Test
    fun `getMessagesWithUser should send both query params and parse response`() = runBlocking {
        enqueueJson(body = emptyList<Message>())

        val result = apiService.getMessagesWithUser(receiverId = "user-2", senderId = "user-1")

        assertTrue(result.isSuccessful)
        assertEquals("/messages/user-2?senderId=user-1", mockWebServer.takeRequest().path)
        assertTrue(result.body()!!.isEmpty())
    }

    @Test
    fun `sendMessage should post to messages endpoint`() = runBlocking {
        enqueueJson(
            body = Message(
                id = "msg-2",
                senderId = "user-1",
                receiverId = "user-2",
                content = "Hi",
                timestamp = "2023-01-02T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )

        val result = apiService.sendMessage("user-1", "user-2", "Hi")

        assertTrue(result.isSuccessful)
        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.startsWith("/messages?"))
        assertTrue(recorded.path!!.contains("content=Hi"))
        assertEquals("msg-2", result.body()!!.id)
    }

    @Test
    fun `getCommunityPosts should parse response correctly`() = runBlocking {
        enqueueJson(
            body = listOf(
                CommunityPost(
                    id = "post-1",
                    authorId = "user-1",
                    title = "Barbecue",
                    content = "Saturday at 5 PM",
                    category = "event",
                    likes = 3,
                    comments = emptyList(),
                    createdAt = "2023-01-01T00:00:00Z"
                )
            )
        )

        val result = apiService.getCommunityPosts()

        assertTrue(result.isSuccessful)
        assertEquals("/community-posts", mockWebServer.takeRequest().path)
        assertEquals("Barbecue", result.body()!![0].title)
        assertEquals(3, result.body()!![0].likes)
    }

    @Test
    fun `getVendors should parse wrapped vendor list`() = runBlocking {
        enqueueJson(body = VendorResponse(data = listOf(sampleVendor())))

        val result = apiService.getVendors()

        assertTrue(result.isSuccessful)
        assertEquals("/vendors", mockWebServer.takeRequest().path)
        assertEquals("Plumbing Services Inc", (result.body() as VendorResponse).data[0].name)
        assertEquals(4.5, (result.body() as VendorResponse).data[0].rating, 0.001)
    }

    @Test
    fun `getVendor should parse single vendor and request path variable`() = runBlocking {
        enqueueJson(body = SingleVendorResponse(data = sampleVendor()))

        val result = apiService.getVendor("1")

        assertTrue(result.isSuccessful)
        assertEquals("/vendors/1", mockWebServer.takeRequest().path)
        assertEquals("PL-12345", (result.body() as SingleVendorResponse).data.licenseNumber)
    }

    @Test
    fun `getWorkOrders should parse wrapped work order list`() = runBlocking {
        enqueueJson(body = WorkOrderResponse(data = listOf(sampleWorkOrder())))

        val result = apiService.getWorkOrders()

        assertTrue(result.isSuccessful)
        assertEquals("/work-orders", mockWebServer.takeRequest().path)
        val data = (result.body() as WorkOrderResponse).data
        assertEquals("wo-1", data[0].id)
        assertEquals(500000.0, data[0].estimatedCost, 0.001)
        assertEquals(null, data[0].vendorId)
    }

    @Test
    fun `getWorkOrder should parse single work order and request path variable`() = runBlocking {
        enqueueJson(body = SingleWorkOrderResponse(data = sampleWorkOrder()))

        val result = apiService.getWorkOrder("wo-1")

        assertTrue(result.isSuccessful)
        assertEquals("/work-orders/wo-1", mockWebServer.takeRequest().path)
        assertEquals("Leaking pipe", (result.body() as SingleWorkOrderResponse).data.title)
    }

    @Test
    fun `initiatePayment should post amount and parse payment response`() = runBlocking {
        enqueueJson(
            body = PaymentResponse(
                transactionId = "txn-1",
                status = "PENDING",
                paymentMethod = "CREDIT_CARD",
                amount = "150000",
                currency = "IDR",
                transactionTime = 1672531200000L,
                referenceNumber = "REF-1"
            )
        )

        val result = apiService.initiatePayment("150000", "Iuran", "user-1", "CREDIT_CARD")

        assertTrue(result.isSuccessful)
        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.startsWith("/payments/initiate?"))
        assertTrue(recorded.path!!.contains("amount=150000"))
        assertEquals("txn-1", (result.body() as PaymentResponse).transactionId)
    }

    @Test
    fun `getPaymentStatus should request status path and parse response`() = runBlocking {
        enqueueJson(
            body = PaymentStatusResponse(
                transactionId = "txn-1",
                status = "COMPLETED",
                amount = "150000",
                currency = "IDR",
                updatedAt = 1672531200000L
            )
        )

        val result = apiService.getPaymentStatus("txn-1")

        assertTrue(result.isSuccessful)
        assertEquals("/payments/txn-1/status", mockWebServer.takeRequest().path)
        assertEquals("COMPLETED", (result.body() as PaymentStatusResponse).status)
    }

    @Test
    fun `confirmPayment should post and parse confirmation`() = runBlocking {
        enqueueJson(
            body = PaymentConfirmationResponse(
                transactionId = "txn-1",
                status = "CONFIRMED",
                confirmationTime = 1672531200000L
            )
        )

        val result = apiService.confirmPayment("txn-1")

        assertTrue(result.isSuccessful)
        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/payments/txn-1/confirm", recorded.path)
        assertEquals("CONFIRMED", (result.body() as PaymentConfirmationResponse).status)
    }

    @Test
    fun `malformed JSON body should surface a parse failure`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("this-is-not-json")
        )

        var thrown: Throwable? = null
        try {
            apiService.getUsers()
        } catch (e: Throwable) {
            thrown = e
        }

        assertNotNull("Malformed JSON must not parse silently", thrown)
    }
}