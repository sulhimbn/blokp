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
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * End-to-end contract tests for [ApiService] against a real HTTP server.
 *
 * These replace four near-duplicate suites that targeted Retrofit 1's
 * Call/enqueue/execute API (removed in Retrofit 2) and pointed a MockWebServer
 * at a port ApiConfig never used, so they could not exercise the transport.
 * Every endpoint declared on ApiService is covered here with real request
 * dispatch and real JSON (de)serialization.
 */
class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

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

    private fun enqueueJson(body: String, code: Int = 200) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    private val usersPayload = """
        {"data":[
          {"first_name":"John","last_name":"Doe","email":"john@example.com",
           "alamat":"Jl. 1","iuran_perwarga":150000,"total_iuran_rekap":1800000,
           "jumlah_iuran_bulanan":150000,"total_iuran_individu":150000,
           "pengeluaran_iuran_warga":50000,"pemanfaatan_iuran":"Jalan","avatar":"http://a/1.jpg"}
        ]}
    """.trimIndent()

    private val vendorJson = """
        {"id":"V1","name":"Acme Plumbing","contactPerson":"John","phoneNumber":"0812",
         "email":"a@b.c","specialty":"plumbing","address":"Jl. 3","licenseNumber":"L1",
         "insuranceInfo":"INS","certifications":["Licensed"],"rating":4.5,"totalReviews":10,
         "contractStart":"2024-01-01","contractEnd":"2024-12-31","isActive":true}
    """.trimIndent()

    private val workOrderJson = """
        {"id":"WO1","title":"Leak","description":"Pipe leaks","category":"plumbing",
         "priority":"high","status":"pending","vendorId":null,"vendorName":null,
         "assignedAt":null,"scheduledDate":null,"completedAt":null,"estimatedCost":150.0,
         "actualCost":0.0,"propertyId":"P1","reporterId":"U1",
         "createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z",
         "attachments":[],"notes":[]}
    """.trimIndent()

    @Test
    fun `getUsers parses the spreadsheet envelope`() = runBlocking {
        enqueueJson(usersPayload)

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        val body: UserResponse = requireNotNull(response.body())
        assertEquals(1, body.data.size)
        assertEquals("John", body.data[0].first_name)
        assertEquals(150000, body.data[0].iuran_perwarga)

        val recorded = mockWebServer.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/users", recorded.path)
    }

    @Test
    fun `getUsers handles an empty data array`() = runBlocking {
        enqueueJson("""{"data":[]}""")

        val body = apiService.getUsers().body()

        assertTrue(body!!.data.isEmpty())
    }

    @Test
    fun `getUsers surfaces an HTTP error without throwing`() = runBlocking {
        enqueueJson("""{"error":"boom"}""", code = 500)

        val response = apiService.getUsers()

        assertEquals(500, response.code())
        assertNull(response.body())
    }

    @Test
    fun `getUsers rejects malformed JSON`() = runBlocking {
        enqueueJson("not json at all")

        val threw = runCatching { apiService.getUsers() }.exceptionOrNull()

        assertTrue("malformed JSON must not be silently accepted", threw != null)
    }

    @Test
    fun `getPemanfaatan parses financial rows`() = runBlocking {
        enqueueJson(usersPayload)

        val response = apiService.getPemanfaatan()

        assertTrue(response.isSuccessful)
        val body: PemanfaatanResponse = requireNotNull(response.body())
        assertEquals("Jalan", body.data[0].pemanfaatan_iuran)
        assertEquals(50000, body.data[0].pengeluaran_iuran_warga)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getAnnouncements parses a list payload`() = runBlocking {
        enqueueJson(
            """
            [{"id":"A1","title":"Water shutdown","content":"Brief","category":"utility",
              "priority":"high","createdAt":"2024-01-01","readBy":["U1"]}]
            """.trimIndent()
        )

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        val list: List<Announcement> = requireNotNull(response.body())
        assertEquals(1, list.size)
        assertEquals("A1", list[0].id)
        assertEquals(listOf("U1"), list[0].readBy)
    }

    @Test
    fun `getMessages passes userId as a query parameter`() = runBlocking {
        enqueueJson(
            """
            [{"id":"M1","senderId":"U1","receiverId":"U2","content":"hi","timestamp":"2024-01-01",
              "readStatus":false,"attachments":[]}]
            """.trimIndent()
        )

        val response = apiService.getMessages("U1")

        assertTrue(response.isSuccessful)
        val list: List<Message> = requireNotNull(response.body())
        assertEquals("M1", list[0].id)

        val recorded = mockWebServer.takeRequest()
        assertEquals("/messages", recorded.requestUrl?.encodedPath)
        assertEquals("U1", recorded.requestUrl?.queryParameter("userId"))
    }

    @Test
    fun `getMessagesWithUser targets the receiver path segment`() = runBlocking {
        enqueueJson("[]")

        apiService.getMessagesWithUser("U2", "U1")

        val recorded = mockWebServer.takeRequest()
        assertEquals("/messages/U2", recorded.requestUrl?.encodedPath)
        assertEquals("U1", recorded.requestUrl?.queryParameter("senderId"))
    }

    @Test
    fun `sendMessage posts and returns the created message`() = runBlocking {
        enqueueJson(
            """
            {"id":"M2","senderId":"U1","receiverId":"U2","content":"hello","timestamp":"2024-01-02",
             "readStatus":false,"attachments":[]}
            """.trimIndent()
        )

        val response = apiService.sendMessage("U1", "U2", "hello")

        assertTrue(response.isSuccessful)
        assertEquals("M2", requireNotNull(response.body()).id)
        assertEquals("POST", mockWebServer.takeRequest().method)
    }

    @Test
    fun `getCommunityPosts parses nested comments`() = runBlocking {
        enqueueJson(
            """
            [{"id":"P1","authorId":"U1","title":"Meeting","content":"Sunday 9am","category":"event",
              "likes":3,"comments":[{"id":"C1","authorId":"U2","content":"ok","timestamp":"2024-01-01"}],
              "createdAt":"2024-01-01"}]
            """.trimIndent()
        )

        val list = apiService.getCommunityPosts().body()

        val post: CommunityPost = requireNotNull(list).first()
        assertEquals("Meeting", post.title)
        assertEquals(3, post.likes)
        assertEquals("C1", post.comments.first().id)
    }

    @Test
    fun `createCommunityPosts posts the new post`() = runBlocking {
        enqueueJson(
            """
            {"id":"P2","authorId":"U1","title":"New","content":"Body","category":"event",
             "likes":0,"comments":[],"createdAt":"2024-01-03"}
            """.trimIndent()
        )

        val created = apiService.createCommunityPost("U1", "New", "Body", "event")

        assertEquals("P2", requireNotNull(created.body()).id)
        assertEquals("POST", mockWebServer.takeRequest().method)
    }

    @Test
    fun `initiatePayment returns the transaction envelope`() = runBlocking {
        enqueueJson(
            """
            {"transactionId":"TX1","status":"PENDING","paymentMethod":"CREDIT_CARD","amount":"10000",
             "currency":"IDR","transactionTime":1700000000000,"referenceNumber":"REF1"}
            """.trimIndent()
        )

        val response = apiService.initiatePayment("10000", "Test", "U1", "CREDIT_CARD")

        val body: PaymentResponse = requireNotNull(response.body())
        assertEquals("TX1", body.transactionId)
        assertEquals("IDR", body.currency)
        assertEquals("PENDING", body.status)
        assertEquals(1700000000000L, body.transactionTime)
    }

    @Test
    fun `getPaymentStatus targets the id path segment`() = runBlocking {
        enqueueJson(
            """
            {"transactionId":"TX1","status":"COMPLETED","amount":"10000","currency":"IDR",
             "updatedAt":1700000001000}
            """.trimIndent()
        )

        val response = apiService.getPaymentStatus("TX1")

        val body: PaymentStatusResponse = requireNotNull(response.body())
        assertEquals("COMPLETED", body.status)
        assertEquals("/payments/TX1/status", mockWebServer.takeRequest().requestUrl?.encodedPath)
    }

    @Test
    fun `confirmPayment returns the confirmation envelope`() = runBlocking {
        enqueueJson(
            """{"transactionId":"TX1","status":"CONFIRMED","confirmationTime":1700000002000}"""
        )

        val response = apiService.confirmPayment("TX1")

        val body: PaymentConfirmationResponse = requireNotNull(response.body())
        assertEquals("CONFIRMED", body.status)
        assertEquals("/payments/TX1/confirm", mockWebServer.takeRequest().requestUrl?.encodedPath)
    }

    @Test
    fun `getVendors parses the vendor list envelope`() = runBlocking {
        enqueueJson("""{"data":[$vendorJson]}""")

        val response = apiService.getVendors()

        val body: VendorResponse = requireNotNull(response.body())
        assertEquals(1, body.data.size)
        assertEquals("Acme Plumbing", body.data[0].name)
        assertEquals(listOf("Licensed"), body.data[0].certifications)
        assertEquals("/vendors", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getVendor parses the single vendor envelope`() = runBlocking {
        enqueueJson("""{"data":$vendorJson}""")

        val body: SingleVendorResponse = requireNotNull(apiService.getVendor("V1").body())

        assertEquals("V1", body.data.id)
        assertEquals("/vendors/V1", mockWebServer.takeRequest().requestUrl?.encodedPath)
    }

    @Test
    fun `createVendor issues a POST and returns the created vendor`() = runBlocking {
        enqueueJson("""{"data":$vendorJson}""", code = 201)

        val response = apiService.createVendor(
            name = "Acme Plumbing",
            contactPerson = "John",
            phoneNumber = "0812",
            email = "a@b.c",
            specialty = "plumbing",
            address = "Jl. 3",
            licenseNumber = "L1",
            insuranceInfo = "INS",
            contractStart = "2024-01-01",
            contractEnd = "2024-12-31"
        )

        assertEquals(201, response.code())
        assertEquals("Acme Plumbing", requireNotNull(response.body()).data.name)
        assertEquals("POST", mockWebServer.takeRequest().method)
    }

    @Test
    fun `updateVendor issues a PUT`() = runBlocking {
        enqueueJson("""{"data":$vendorJson}""")

        val response = apiService.updateVendor(
            id = "V1",
            name = "Acme Plumbing",
            contactPerson = "John",
            phoneNumber = "0812",
            email = "a@b.c",
            specialty = "plumbing",
            address = "Jl. 3",
            licenseNumber = "L1",
            insuranceInfo = "INS",
            contractStart = "2024-01-01",
            contractEnd = "2024-12-31",
            isActive = false
        )

        assertTrue(response.isSuccessful)
        assertEquals("PUT", mockWebServer.takeRequest().method)
    }

    @Test
    fun `getWorkOrders parses the work order envelope`() = runBlocking {
        enqueueJson("""{"data":[$workOrderJson]}""")

        val response = apiService.getWorkOrders()

        val body: WorkOrderResponse = requireNotNull(response.body())
        assertEquals(1, body.data.size)
        assertEquals("Leak", body.data[0].title)
        assertNull(body.data[0].vendorId)
        assertEquals("/work-orders", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getWorkOrder parses the single work order envelope`() = runBlocking {
        enqueueJson("""{"data":$workOrderJson}""")

        val body: SingleWorkOrderResponse = requireNotNull(apiService.getWorkOrder("WO1").body())

        assertEquals("WO1", body.data.id)
        assertEquals(150.0, body.data.estimatedCost, 0.001)
    }

    @Test
    fun `createWorkOrder issues a POST`() = runBlocking {
        enqueueJson("""{"data":$workOrderJson}""", code = 201)

        val response = apiService.createWorkOrder(
            title = "Leak",
            description = "Pipe leaks",
            category = "plumbing",
            priority = "high",
            propertyId = "P1",
            reporterId = "U1",
            estimatedCost = 150.0
        )

        assertEquals(201, response.code())
        assertEquals("POST", mockWebServer.takeRequest().method)
    }

    @Test
    fun `assignVendorToWorkOrder issues a PUT to the assign path`() = runBlocking {
        enqueueJson("""{"data":$workOrderJson}""")

        val response = apiService.assignVendorToWorkOrder("WO1", "V1", "2024-02-01")

        assertTrue(response.isSuccessful)
        val recorded = mockWebServer.takeRequest()
        assertEquals("PUT", recorded.method)
        assertEquals("/work-orders/WO1/assign", recorded.requestUrl?.encodedPath)
    }

    @Test
    fun `updateWorkOrderStatus issues a PUT to the status path`() = runBlocking {
        enqueueJson("""{"data":$workOrderJson}""")

        val response = apiService.updateWorkOrderStatus("WO1", "in_progress", "started")

        assertTrue(response.isSuccessful)
        val recorded = mockWebServer.takeRequest()
        assertEquals("PUT", recorded.method)
        assertEquals("/work-orders/WO1/status", recorded.requestUrl?.encodedPath)
    }
}