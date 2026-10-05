package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.CommunityPost
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService
    private val gson = Gson()

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

    private fun item(email: String) = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = email,
        alamat = "123 Main St",
        iuran_perwarga = 150_000,
        total_iuran_rekap = 1_800_000,
        jumlah_iuran_bulanan = 150_000,
        total_iuran_individu = 150_000,
        pengeluaran_iuran_warga = 50_000,
        pemanfaatan_iuran = "Perbaikan jalan komplek",
        avatar = "https://example.com/avatar1.jpg"
    )

    private fun enqueue(body: Any) = server.enqueue(
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(gson.toJson(body))
    )

    private fun enqueueError(code: Int, body: String) = server.enqueue(
        MockResponse().setResponseCode(code).setBody(body)
    )

    @Test
    fun `getUsers parses the resident list and calls the users path`() = runTest {
        val payload = UserResponse(data = listOf(item("john@example.com")))
        enqueue(payload)

        val response = api.getUsers()

        assertTrue(response.isSuccessful)
        assertEquals("john@example.com", response.body()?.data?.single()?.email)
        assertEquals("/users", server.takeRequest().path)
    }

    @Test
    fun `getPemanfaatan parses the utilisation list and calls the pemanfaatan path`() = runTest {
        enqueue(PemanfaatanResponse(data = listOf(item("john@example.com"))))

        val response = api.getPemanfaatan()

        assertEquals("Perbaikan jalan komplek", response.body()?.data?.single()?.pemanfaatan_iuran)
        assertEquals("/pemanfaatan", server.takeRequest().path)
    }

    @Test
    fun `getUsers surfaces an HTTP error without throwing`() = runTest {
        enqueueError(500, "boom")

        val response = api.getUsers()

        assertEquals(500, response.code())
        assertNull(response.body())
        assertEquals(500, response.errorBody()?.let { response.code() })
    }

    @Test
    fun `getAnnouncements parses each announcement field`() = runTest {
        enqueue(
            listOf(
                Announcement(
                    id = "a1",
                    title = "Water shutoff",
                    content = "Water will be off on Friday",
                    category = "maintenance",
                    priority = "high",
                    createdAt = "2024-05-01",
                    readBy = listOf("resident-1")
                )
            )
        )

        val announcements = api.getAnnouncements().body()?.single()

        assertEquals("Water shutoff", announcements?.title)
        assertEquals(listOf("resident-1"), announcements?.readBy)
        assertEquals("/announcements", server.takeRequest().path)
    }

    @Test
    fun `getMessages forwards the user id as a query parameter`() = runTest {
        enqueue(
            listOf(
                Message(
                    id = "m1",
                    senderId = "resident-2",
                    receiverId = "resident-1",
                    content = "Your dues are due",
                    timestamp = "2024-05-01T10:00:00Z",
                    readStatus = false,
                    attachments = emptyList()
                )
            )
        )

        val message = api.getMessages("resident-1").body()?.single()

        assertEquals("Your dues are due", message?.content)
        assertEquals("/messages?userId=resident-1", server.takeRequest().path)
    }

    @Test
    fun `getMessagesWithUser forwards both ids`() = runTest {
        enqueue(emptyList<Message>())

        api.getMessagesWithUser("resident-2", "resident-1")

        assertEquals(
            "/messages/resident-2?senderId=resident-1",
            server.takeRequest().path
        )
    }

    @Test
    fun `getCommunityPosts parses a post with its comments`() = runTest {
        enqueue(
            listOf(
                CommunityPost(
                    id = "p1",
                    authorId = "resident-1",
                    title = "Community meeting",
                    content = "Saturday at 10",
                    category = "events",
                    likes = 3,
                    comments = emptyList(),
                    createdAt = "2024-05-01"
                )
            )
        )

        val post = api.getCommunityPosts().body()?.single()

        assertEquals(3, post?.likes)
        assertEquals("/community-posts", server.takeRequest().path)
    }

    @Test
    fun `initiatePayment posts the amount and returns the transaction id`() = runTest {
        enqueue(
            PaymentResponse(
                transactionId = "txn-1",
                status = "PENDING",
                paymentMethod = "CREDIT_CARD",
                amount = "10000",
                currency = "IDR",
                transactionTime = 1_714_550_000_000,
                referenceNumber = "REF-1"
            )
        )

        val payment = api.initiatePayment(
            amount = "10000",
            description = "Monthly dues",
            customerId = "resident-1",
            paymentMethod = "CREDIT_CARD"
        ).body()

        assertEquals("txn-1", payment?.transactionId)
        val request = server.takeRequest()
        assertEquals("/payments/initiate", request.path?.substringBefore('?'))
        assertTrue(request.path!!.contains("amount=10000"))
        assertTrue(request.path!!.contains("customerId=resident-1"))
    }

    @Test
    fun `getPaymentStatus reads the status path segment`() = runTest {
        enqueue(
            mapOf(
                "transactionId" to "txn-1",
                "status" to "COMPLETED",
                "amount" to "10000",
                "currency" to "IDR",
                "updatedAt" to 1_714_550_000_000
            )
        )

        val status = api.getPaymentStatus("txn-1").body()

        assertEquals("COMPLETED", status?.status)
        assertEquals("/payments/txn-1/status", server.takeRequest().path)
    }

    @Test
    fun `confirmPayment posts to the confirmation path`() = runTest {
        enqueue(
            mapOf(
                "transactionId" to "txn-1",
                "status" to "CONFIRMED",
                "confirmationTime" to 1_714_550_000_000
            )
        )

        val confirmation = api.confirmPayment("txn-1").body()

        assertEquals("CONFIRMED", confirmation?.status)
        assertEquals("/payments/txn-1/confirm", server.takeRequest().path)
    }

    @Test
    fun `getVendors parses the vendor list`() = runTest {
        enqueue(
            mapOf(
                "data" to listOf(
                    mapOf(
                        "id" to "v1",
                        "name" to "Plumbing Services",
                        "contactPerson" to "John",
                        "phoneNumber" to "123",
                        "email" to "p@example.com",
                        "specialty" to "plumbing",
                        "address" to "123 Main St",
                        "licenseNumber" to "LIC-1",
                        "insuranceInfo" to "GL",
                        "certifications" to listOf("Licensed"),
                        "rating" to 4.5,
                        "totalReviews" to 12,
                        "contractStart" to "2024-01-01",
                        "contractEnd" to "2025-01-01",
                        "isActive" to true
                    )
                )
            )
        )

        val vendors = api.getVendors().body()?.data

        assertEquals("Plumbing Services", vendors?.single()?.name)
        assertEquals(4.5, vendors?.single()?.rating ?: 0.0, 0.0001)
    }

    @Test
    fun `createVendor sends every field as a query parameter`() = runTest {
        enqueue(mapOf("data" to mapOf("id" to "v2")))

        api.createVendor(
            "Plumbing Services",
            "John",
            "123",
            "p@example.com",
            "plumbing",
            "123 Main St",
            "LIC-1",
            "GL",
            "2024-01-01",
            "2025-01-01"
        )

        val path = server.takeRequest().path!!
        assertTrue(path.startsWith("/vendors?"))
        assertTrue(path.contains("name=Plumbing%20Services"))
        assertTrue(path.contains("licenseNumber=LIC-1"))
    }

    @Test
    fun `getWorkOrders parses the work order list`() = runTest {
        enqueue(
            mapOf(
                "data" to listOf(
                    mapOf(
                        "id" to "wo-1",
                        "title" to "Leaking pipe",
                        "description" to "Kitchen",
                        "category" to "plumbing",
                        "priority" to "high",
                        "status" to "pending",
                        "vendorId" to null,
                        "vendorName" to null,
                        "assignedAt" to null,
                        "scheduledDate" to null,
                        "completedAt" to null,
                        "estimatedCost" to 250.0,
                        "actualCost" to 0.0,
                        "propertyId" to "block-a",
                        "reporterId" to "resident-1",
                        "createdAt" to "2024-04-30",
                        "updatedAt" to "2024-04-30",
                        "attachments" to emptyList<String>(),
                        "notes" to emptyList<String>()
                    )
                )
            )
        )

        val orders = api.getWorkOrders().body()?.data

        assertEquals("Leaking pipe", orders?.single()?.title)
        assertEquals(null, orders?.single()?.vendorId)
    }

    @Test
    fun `assignVendorToWorkOrder uses a PUT on the assign subpath`() = runTest {
        enqueue(mapOf("data" to mapOf("id" to "wo-1")))

        api.assignVendorToWorkOrder("wo-1", "v1", "2024-05-02")

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/work-orders/wo-1/assign", request.path?.substringBefore('?'))
        assertTrue(request.path!!.contains("vendorId=v1"))
    }

    @Test
    fun `updateWorkOrderStatus omits a null notes parameter`() = runTest {
        enqueue(mapOf("data" to mapOf("id" to "wo-1")))

        api.updateWorkOrderStatus("wo-1", "completed", null)

        val path = server.takeRequest().path!!
        assertTrue(path.contains("status=completed"))
        assertTrue(!path.contains("notes="))
    }
}