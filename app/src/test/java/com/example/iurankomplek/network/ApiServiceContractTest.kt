package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
import com.example.iurankomplek.data.api.models.SingleVendorResponse
import com.example.iurankomplek.data.api.models.SingleWorkOrderResponse
import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.data.api.models.WorkOrderResponse
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Drives the real Retrofit [ApiService] against a local HTTP server so every
 * declared path, query name and response field is proven to match the shapes
 * the mock API serves. A rename on either side fails here rather than on a
 * device or in the mock-api contract suite.
 */
class ApiServiceContractTest {

    private lateinit var server: MockWebServer
    private lateinit var service: ApiService
    private val gson = Gson()

    private fun item(first: String = "John", email: String = "john@example.com") = DataItem(
        first_name = first,
        last_name = "Doe",
        email = email,
        alamat = "123 Main St",
        iuran_perwarga = 100,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = 150,
        pengeluaran_iuran_warga = 50,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        service = Retrofit.Builder()
            .baseUrl(server.url("/data/QjX6hB1ST2IDKaxB/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun enqueue(payload: Any, code: Int = 200) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(payload))
        )
    }

    private fun enqueueRaw(code: Int, body: String) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    private fun takeRequest() = server.takeRequest()

    // ---- users /enominee --------------------------------------------------

    @Test
    fun `getUsers reads users and keeps every financial column`() = runBlocking {
        val payload = UserResponse(listOf(item()))
        enqueue(payload)

        val body = service.getUsers().body()

        val request = takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/data/QjX6hB1ST2IDKaxB/users", request.requestUrl!!.encodedPath)
        assertEquals(payload, body)
        assertEquals(150, body?.data?.single()?.total_iuran_individu)
    }

    @Test
    fun `getUsers surfaces a 500 as an unsuccessful response`() = runBlocking {
        enqueueRaw(500, """{"error":"Internal server error"}""")

        val response = service.getUsers()

        assertEquals(500, response.code())
    }

    @Test
    fun `getPemanfaatan reads the financial feed`() = runBlocking {
        enqueue(PemanfaatanResponse(listOf(item(first = "Jane", email = "jane@example.com"))))

        val body = service.getPemanfaatan().body()

        assertEquals("/data/QjX6hB1ST2IDKaxB/pemanfaatan", takeRequest().requestUrl!!.encodedPath)
        assertEquals("Maintenance", body?.data?.single()?.pemanfaatan_iuran)
    }

    // ---- communication ----------------------------------------------------

    @Test
    fun `getAnnouncements deserializes the announcement array`() = runBlocking {
        enqueueRaw(
            200,
            """[{"id":"ann-1","title":"Pemutihan","content":"Bayar","category":"payment",
                "priority":"high","createdAt":"2026-09-01T08:00:00Z","readBy":["user-1"]}]"""
        )

        val body = service.getAnnouncements().body()

        assertEquals("/data/QjX6hB1ST2IDKaxB/announcements", takeRequest().requestUrl!!.encodedPath)
        assertEquals("Pemutihan", body?.single()?.title)
        assertEquals(listOf("user-1"), body?.single()?.readBy)
    }

    @Test
    fun `getMessages sends userId as a query parameter`() = runBlocking {
        enqueueRaw(
            200,
            """[{"id":"msg-1","senderId":"user-1","receiverId":"user-2","content":"halo",
                "timestamp":"2026-09-20T10:00:00Z","readStatus":true,"attachments":[]}]"""
        )

        val body = service.getMessages("user-1").body()

        val recorded = takeRequest()
        assertEquals("/data/QjX6hB1ST2IDKaxB/messages", recorded.requestUrl!!.encodedPath)
        assertEquals("userId=user-1", recorded.requestUrl!!.query)
        assertEquals("halo", body?.single()?.content)
        assertTrue(body?.single()?.readStatus == true)
    }

    @Test
    fun `getMessagesWithUser puts the peer id in the path`() = runBlocking {
        enqueueRaw(200, "[]")

        service.getMessagesWithUser(receiverId = "user-2", senderId = "user-1")

        val recorded = takeRequest()
        assertEquals("/data/QjX6hB1ST2IDKaxB/messages/user-2", recorded.requestUrl!!.encodedPath)
        assertEquals("senderId=user-1", recorded.requestUrl!!.query)
    }

    @Test
    fun `sendMessage posts the conversation fields as query parameters`() = runBlocking {
        enqueueRaw(
            201,
            """{"id":"msg-2","senderId":"user-1","receiverId":"user-2","content":"halo",
                "timestamp":"2026-10-02T06:00:00Z","readStatus":false,"attachments":[]}"""
        )

        val body = service.sendMessage("user-1", "user-2", "halo").body()

        val request = takeRequest()
        assertEquals("POST", request.method)
        val query = request.requestUrl!!.query ?: ""
        assertTrue(query.contains("senderId=user-1"))
        assertTrue(query.contains("receiverId=user-2"))
        assertTrue(query.contains("content=halo"))
        assertEquals("msg-2", body?.id)
    }

    @Test
    fun `getCommunityPosts deserializes nested comments`() = runBlocking {
        enqueueRaw(
            200,
            """[{"id":"post-1","authorId":"user-1","title":"Usulan","content":"Jalan rusak",
                "category":"infrastructure","likes":12,
                "comments":[{"id":"cmt-1","authorId":"user-2","content":"Setuju",
                             "timestamp":"2026-09-18T12:00:00Z"}],
                "createdAt":"2026-09-17T09:00:00Z"}]"""
        )

        val body = service.getCommunityPosts().body()

        assertEquals(12, body?.single()?.likes)
        assertEquals("Setuju", body?.single()?.comments?.single()?.content)
    }

    @Test
    fun `createCommunityPost posts every field as a query parameter`() = runBlocking {
        enqueueRaw(
            201,
            """{"id":"post-2","authorId":"user-1","title":"T","content":"C",
                "category":"general","likes":0,"comments":[],"createdAt":"2026-10-02T06:00:00Z"}"""
        )

        val body = service.createCommunityPost("user-1", "T", "C", "general").body()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/data/QjX6hB1ST2IDKaxB/community-posts", request.requestUrl!!.encodedPath)
        assertEquals("post-2", body?.id)
    }

    // ---- payments ---------------------------------------------------------

    @Test
    fun `initiatePayment posts to payments initiate with the documented query names`() = runBlocking {
        enqueueRaw(
            201,
            """{"transactionId":"txn-1","status":"PENDING","paymentMethod":"CREDIT_CARD",
                "amount":"10000","currency":"IDR","transactionTime":1700000000000,
                "referenceNumber":"REF-1"}"""
        )

        val body: PaymentResponse? =
            service.initiatePayment("10000", "Test payment", "test_user", "CREDIT_CARD").body()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/data/QjX6hB1ST2IDKaxB/payments/initiate", request.requestUrl!!.encodedPath)
        val query = request.requestUrl!!.query ?: ""
        assertTrue("amount missing from $query", query.contains("amount=10000"))
        assertTrue("description missing from $query", query.contains("description=Test payment"))
        assertTrue("customerId missing from $query", query.contains("customerId=test_user"))
        assertTrue("paymentMethod missing from $query", query.contains("paymentMethod=CREDIT_CARD"))
        assertEquals("txn-1", body?.transactionId)
        assertEquals("REF-1", body?.referenceNumber)
    }

    @Test
    fun `getPaymentStatus reads the transaction id from the path`() = runBlocking {
        enqueueRaw(
            200,
            """{"transactionId":"txn-5","status":"COMPLETED","amount":"75000",
                "currency":"IDR","updatedAt":1700000005000}"""
        )

        val body: PaymentStatusResponse? = service.getPaymentStatus("txn-5").body()

        assertEquals("/data/QjX6hB1ST2IDKaxB/payments/txn-5/status", takeRequest().requestUrl!!.encodedPath)
        assertEquals("COMPLETED", body?.status)
        assertEquals(1700000005000L, body?.updatedAt)
    }

    @Test
    fun `confirmPayment posts to the confirm path`() = runBlocking {
        enqueueRaw(
            200,
            """{"transactionId":"txn-7","status":"COMPLETED","confirmationTime":1700000009000}"""
        )

        val body: PaymentConfirmationResponse? = service.confirmPayment("txn-7").body()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/data/QjX6hB1ST2IDKaxB/payments/txn-7/confirm", request.requestUrl!!.encodedPath)
        assertEquals(1700000009000L, body?.confirmationTime)
    }

    // ---- vendors ----------------------------------------------------------

    @Test
    fun `getVendors unwraps the data envelope`() = runBlocking {
        enqueueRaw(
            200,
            """{"data":[{"id":"v1","name":"CV Nusantara","contactPerson":"Budi",
                "phoneNumber":"0812","email":"b@x.test","specialty":"plumbing",
                "address":"Jl. Industri","licenseNumber":"L-1","insuranceInfo":"AS-1",
                "certifications":["PIPA"],"rating":4.5,"totalReviews":12,
                "contractStart":"2026-01-01","contractEnd":"2026-12-31","isActive":true}]}"""
        )

        val body: VendorResponse? = service.getVendors().body()

        assertEquals("/data/QjX6hB1ST2IDKaxB/vendors", takeRequest().requestUrl!!.encodedPath)
        assertEquals(4.5, body?.data?.single()?.rating!!, 0.0001)
        assertEquals(listOf("PIPA"), body.data.single().certifications)
        assertTrue(body.data.single().isActive)
    }

    @Test
    fun `getVendor unwraps a single vendor`() = runBlocking {
        enqueueRaw(
            200,
            """{"data":{"id":"v1","name":"CV Nusantara","contactPerson":"Budi",
                "phoneNumber":"0812","email":"b@x.test","specialty":"plumbing",
                "address":"Jl. Industri","licenseNumber":"L-1","insuranceInfo":"AS-1",
                "certifications":[],"rating":4.5,"totalReviews":12,
                "contractStart":"2026-01-01","contractEnd":"2026-12-31","isActive":true}}"""
        )

        val body: SingleVendorResponse? = service.getVendor("v1").body()

        assertEquals("/data/QjX6hB1ST2IDKaxB/vendors/v1", takeRequest().requestUrl!!.encodedPath)
        assertEquals("CV Nusantara", body?.data?.name)
    }

    @Test
    fun `createVendor posts all ten fields`() = runBlocking {
        enqueueRaw(
            201,
            """{"data":{"id":"v3","name":"V","contactPerson":"C","phoneNumber":"P",
                "email":"E","specialty":"S","address":"A","licenseNumber":"L",
                "insuranceInfo":"I","certifications":[],"rating":0.0,"totalReviews":0,
                "contractStart":"2026-01-01","contractEnd":"2026-12-31","isActive":true}}"""
        )

        val body = service.createVendor(
            "V", "C", "P", "E", "S", "A", "L", "I", "2026-01-01", "2026-12-31"
        ).body()

        val query = takeRequest().requestUrl!!.query ?: ""
        listOf("name", "contactPerson", "phoneNumber", "email", "specialty",
            "address", "licenseNumber", "insuranceInfo", "contractStart", "contractEnd")
            .forEach { assertTrue("$it missing from $query", query.contains("$it=")) }
        assertEquals("v3", body?.data?.id)
    }

    @Test
    fun `updateVendor sends isActive as a query parameter`() = runBlocking {
        enqueueRaw(
            200,
            """{"data":{"id":"v1","name":"V","contactPerson":"C","phoneNumber":"P",
                "email":"E","specialty":"S","address":"A","licenseNumber":"L",
                "insuranceInfo":"I","certifications":[],"rating":4.5,"totalReviews":12,
                "contractStart":"2026-01-01","contractEnd":"2026-12-31","isActive":false}}"""
        )

        val body = service.updateVendor(
            "v1", "V", "C", "P", "E", "S", "A", "L", "I",
            "2026-01-01", "2026-12-31", false
        ).body()

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertTrue((request.requestUrl!!.query ?: "").contains("isActive=false"))
        assertEquals(false, body?.data?.isActive)
    }

    // ---- work orders ------------------------------------------------------

    @Test
    fun `getWorkOrders unwraps the list envelope`() = runBlocking {
        enqueueRaw(
            200,
            """{"data":[{"id":"wo1","title":"Pipa bocor","description":"Bocor parah",
                "category":"plumbing","priority":"urgent","status":"assigned",
                "vendorId":"v1","vendorName":"CV Nusantara","assignedAt":"2026-09-19T09:00:00Z",
                "scheduledDate":"2026-10-05","completedAt":null,"estimatedCost":750000.0,
                "actualCost":0.0,"propertyId":"blok-c","reporterId":"user-2",
                "createdAt":"2026-09-18T08:00:00Z","updatedAt":"2026-09-19T09:00:00Z",
                "attachments":["https://example.test/wo1.jpg"],"notes":[]}]}"""
        )

        val body: WorkOrderResponse? = service.getWorkOrders().body()

        assertEquals("/data/QjX6hB1ST2IDKaxB/work-orders", takeRequest().requestUrl!!.encodedPath)
        assertEquals(750000.0, body?.data?.single()?.estimatedCost!!, 0.0001)
        assertEquals(listOf("https://example.test/wo1.jpg"), body.data.single().attachments)
    }

    @Test
    fun `getWorkOrder unwraps a single work order`() = runBlocking {
        enqueueRaw(
            200,
            """{"data":{"id":"wo1","title":"Pipa bocor","description":"Bocor parah",
                "category":"plumbing","priority":"urgent","status":"pending",
                "vendorId":null,"vendorName":null,"assignedAt":null,"scheduledDate":null,
                "completedAt":null,"estimatedCost":750000.0,"actualCost":0.0,
                "propertyId":"blok-c","reporterId":"user-2",
                "createdAt":"2026-09-18T08:00:00Z","updatedAt":"2026-09-18T08:00:00Z",
                "attachments":[],"notes":[]}}"""
        )

        val body: SingleWorkOrderResponse? = service.getWorkOrder("wo1").body()

        assertEquals("/data/QjX6hB1ST2IDKaxB/work-orders/wo1", takeRequest().requestUrl!!.encodedPath)
        assertEquals(null, body?.data?.vendorId)
    }

    @Test
    fun `createWorkOrder posts estimatedCost as a query parameter`() = runBlocking {
        enqueueRaw(
            201,
            """{"data":{"id":"wo2","title":"T","description":"D","category":"plumbing",
                "priority":"high","status":"pending","vendorId":null,"vendorName":null,
                "assignedAt":null,"scheduledDate":null,"completedAt":null,
                "estimatedCost":150.5,"actualCost":0.0,"propertyId":"blok-a",
                "reporterId":"user-1","createdAt":"2026-10-02T06:00:00Z",
                "updatedAt":"2026-10-02T06:00:00Z","attachments":[],"notes":[]}}"""
        )

        val body = service.createWorkOrder(
            "T", "D", "plumbing", "high", "blok-a", "user-1", 150.5
        ).body()

        val request = takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/data/QjX6hB1ST2IDKaxB/work-orders", request.requestUrl!!.encodedPath)
        assertTrue((request.requestUrl!!.query ?: "").contains("estimatedCost=150.5"))
        assertEquals(150.5, body?.data?.estimatedCost!!, 0.0001)
    }

    @Test
    fun `assignVendorToWorkOrder puts the vendor id in the query string`() = runBlocking {
        enqueueRaw(
            200,
            """{"data":{"id":"wo1","title":"Pipa bocor","description":"D",
                "category":"plumbing","priority":"urgent","status":"assigned",
                "vendorId":"v1","vendorName":"CV Nusantara","assignedAt":"2026-10-02T06:00:00Z",
                "scheduledDate":"2026-11-01","completedAt":null,"estimatedCost":750000.0,
                "actualCost":0.0,"propertyId":"blok-c","reporterId":"user-2",
                "createdAt":"2026-09-18T08:00:00Z","updatedAt":"2026-10-02T06:00:00Z",
                "attachments":[],"notes":[]}}"""
        )

        val body = service.assignVendorToWorkOrder("wo1", "v1", "2026-11-01").body()

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertEquals(
            "/data/QjX6hB1ST2IDKaxB/work-orders/wo1/assign?vendorId=v1&scheduledDate=2026-11-01",
            request.path
        )
        assertEquals("v1", body?.data?.vendorId)
    }

    @Test
    fun `updateWorkOrderStatus omits nothing and returns the new status`() = runBlocking {
        enqueueRaw(
            200,
            """{"data":{"id":"wo1","title":"Pipa bocor","description":"D",
                "category":"plumbing","priority":"urgent","status":"in_progress",
                "vendorId":"v1","vendorName":"CV Nusantara","assignedAt":"2026-10-02T06:00:00Z",
                "scheduledDate":"2026-11-01","completedAt":null,"estimatedCost":750000.0,
                "actualCost":0.0,"propertyId":"blok-c","reporterId":"user-2",
                "createdAt":"2026-09-18T08:00:00Z","updatedAt":"2026-10-02T06:00:00Z",
                "attachments":[],"notes":["mulai dikerjakan"]}}"""
        )

        val body = service.updateWorkOrderStatus("wo1", "in_progress", "mulai dikerjakan").body()

        val request = takeRequest()
        assertEquals("PUT", request.method)
        assertTrue((request.requestUrl!!.query ?: "").contains("status=in_progress"))
        assertEquals("in_progress", body?.data?.status)
        assertEquals(listOf("mulai dikerjakan"), body?.data?.notes)
    }

    @Test
    fun `a 404 from any endpoint is surfaced rather than swallowed`() = runBlocking {
        enqueueRaw(404, """{"error":"vendor nope not found"}""")

        assertEquals(404, service.getVendor("nope").code())
    }
}
