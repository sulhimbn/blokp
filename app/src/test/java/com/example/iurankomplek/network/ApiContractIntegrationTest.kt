package com.example.iurankomplek.network

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
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
import java.net.SocketTimeoutException

/**
 * Contract tests for [ApiService] against a real HTTP server.
 *
 * These replace three earlier Call-based suites that were written before the endpoints
 * became `suspend`, plus a payment suite that asserted only that the methods existed.
 * Everything they intended to check is covered here, and the assertions are on the real
 * wire format rather than on the service object.
 */
class ApiContractIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService

    private fun json(code: Int, body: String) = MockResponse()
        .setResponseCode(code)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private fun dataItem(email: String) = DataItem(
        first_name = "John",
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
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getUsers requests the users path and parses the payload`() = runTest {
        val payload = UserResponse(data = listOf(dataItem("john.doe@example.com")))
        server.enqueue(json(200, Gson().toJson(payload)))

        val response = api.getUsers()

        assertTrue(response.isSuccessful)
        assertEquals("/users", server.takeRequest().path)
        val item = response.body()!!.data.single()
        assertEquals("john.doe@example.com", item.email)
        assertEquals(150, item.total_iuran_individu)
        assertEquals("Maintenance", item.pemanfaatan_iuran)
    }

    @Test
    fun `getUsers reports an empty data set as an empty list`() = runTest {
        server.enqueue(json(200, Gson().toJson(UserResponse(data = emptyList()))))

        val response = api.getUsers()

        assertTrue(response.isSuccessful)
        assertTrue(response.body()!!.data.isEmpty())
    }

    @Test
    fun `getUsers surfaces a server error as an unsuccessful response`() = runTest {
        server.enqueue(json(500, "Internal Server Error"))

        val response = api.getUsers()

        assertEquals(500, response.code())
        assertTrue(!response.isSuccessful)
    }

    @Test
    fun `getUsers surfaces an unauthorized status`() = runTest {
        server.enqueue(json(401, "Unauthorized"))

        val response = api.getUsers()

        assertEquals(401, response.code())
    }

    @Test
    fun `getUsers rejects a malformed body instead of returning partial data`() = runTest {
        server.enqueue(json(200, "{ this is not json"))

        val thrown = runCatching { api.getUsers() }.exceptionOrNull()

        assertNotNull("malformed JSON should fail the call", thrown)
    }

    @Test
    fun `getPemanfaatan parses the financial payload`() = runTest {
        val payload = PemanfaatanResponse(data = listOf(dataItem("john.doe@example.com").copy(pengeluaran_iuran_warga = 75)))
        server.enqueue(json(200, Gson().toJson(payload)))

        val response = api.getPemanfaatan()

        assertTrue(response.isSuccessful)
        assertEquals("/pemanfaatan", server.takeRequest().path)
        assertEquals(75, response.body()!!.data.single().pengeluaran_iuran_warga)
    }

    @Test
    fun `getAnnouncements parses the announcement list`() = runTest {
        val payload = listOf(
            Announcement(
                id = "a1",
                title = "Water shutoff",
                content = "Maintenance on Tuesday",
                category = "maintenance",
                priority = "high",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = listOf("user-1")
            )
        )
        server.enqueue(json(200, Gson().toJson(payload)))

        val response = api.getAnnouncements()

        assertTrue(response.isSuccessful)
        assertEquals("Water shutoff", response.body()!!.single().title)
    }

    @Test
    fun `getMessages passes userId as a query parameter`() = runTest {
        val payload = listOf(
            Message(
                id = "m1",
                senderId = "user-2",
                receiverId = "user-1",
                content = "Hello",
                timestamp = "2024-01-01T00:00:00Z",
                readStatus = false,
                attachments = emptyList()
            )
        )
        server.enqueue(json(200, Gson().toJson(payload)))

        val response = api.getMessages("user-1")

        assertTrue(response.isSuccessful)
        val recorded = server.takeRequest()
        assertEquals("/messages", recorded.path?.substringBefore("?"))
        assertEquals("userId=user-1", recorded.path?.substringAfter("?"))
        assertEquals("Hello", response.body()!!.single().content)
    }

    @Test
    fun `sendMessage transmits every value as a query parameter`() = runTest {
        val payload = Message(
            id = "m2",
            senderId = "user-1",
            receiverId = "user-2",
            content = "Hi there",
            timestamp = "2024-01-01T00:00:00Z",
            readStatus = false,
            attachments = emptyList()
        )
        server.enqueue(json(200, Gson().toJson(payload)))

        val response = api.sendMessage("user-1", "user-2", "Hi there")

        assertTrue(response.isSuccessful)
        val query = server.takeRequest().path!!.substringAfter("?")
        assertTrue("senderId missing: $query", query.contains("senderId=user-1"))
        assertTrue("receiverId missing: $query", query.contains("receiverId=user-2"))
        assertTrue("content missing: $query", query.contains("content=Hi%20there"))
        assertEquals("m2", response.body()?.id)
    }

    @Test
    fun `initiatePayment posts to the payments path`() = runTest {
        val payload = com.example.iurankomplek.data.api.models.PaymentResponse(
            transactionId = "pay_1",
            status = "PENDING",
            paymentMethod = "CREDIT_CARD",
            amount = "10000",
            currency = "IDR",
            transactionTime = 1_700_000_000_000,
            referenceNumber = "REF-1"
        )
        server.enqueue(json(200, Gson().toJson(payload)))

        val response = api.initiatePayment("10000", "Test payment", "test_user", "CREDIT_CARD")

        assertTrue(response.isSuccessful)
        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/payments/initiate", recorded.path?.substringBefore("?"))
        assertTrue(response.body()!!.transactionId == "pay_1")
    }

    @Test
    fun `getVendors parses the vendor list`() = runTest {
        val body = """
            {"data":[{"id":"v1","name":"Acme","contactPerson":"Budi","phoneNumber":"0812",
            "email":"a@b.c","specialty":"plumbing","address":"Jl. X","licenseNumber":"L1",
            "insuranceInfo":"I1","certifications":["S1"],"rating":4.5,"totalReviews":10,
            "contractStart":"2024-01-01","contractEnd":"2025-01-01","isActive":true}]}
        """.trimIndent()
        server.enqueue(json(200, body))

        val response = api.getVendors()

        assertTrue(response.isSuccessful)
        val vendor = response.body()!!.data.single()
        assertEquals("v1", vendor.id)
        assertEquals("Budi", vendor.contactPerson)
        assertEquals(4.5, vendor.rating, 0.001)
        assertTrue(vendor.isActive)
    }

    @Test
    fun `getVendors surfaces a transport timeout`() = runTest {
        server.enqueue(
            MockResponse()
                .setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AT_START)
        )

        val thrown = runCatching { api.getVendors() }.exceptionOrNull()

        assertTrue(
            "expected a transport failure but got $thrown",
            thrown is SocketTimeoutException || thrown is java.io.IOException
        )
    }

    @Test
    fun `a 404 from the API can be inspected as an HttpException`() = runTest {
        server.enqueue(json(404, "Not Found"))

        val response = api.getWorkOrder("missing")
        val error = HttpException(response)

        assertEquals(404, error.code())
    }
}