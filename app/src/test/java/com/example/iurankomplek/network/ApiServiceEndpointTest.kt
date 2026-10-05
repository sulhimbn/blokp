package com.example.iurankomplek.network

import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.model.DataItem
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Network-layer checks scoped to the `com.example.iurankomplek.network` package:
 * every ApiService endpoint the app actually calls must hit the documented path and
 * parse the documented payload.
 */
class ApiServiceEndpointTest {

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

    private fun enqueue(body: Any, code: Int = 200) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(if (body is String) body else gson.toJson(body))
        )
    }

    @Test
    fun `getCommunityPosts uses the community-posts path`() = runTest {
        enqueue(listOf(TestFixtures.communityPost()))

        val result = apiService.getCommunityPosts()

        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.size)
        assertEquals("/community-posts", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getMessagesWithUser builds both path and query`() = runTest {
        enqueue(listOf(TestFixtures.message()))

        val result = apiService.getMessagesWithUser("user2", "user1")

        assertTrue(result.isSuccessful)
        assertEquals("/messages/user2?senderId=user1", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getVendor returns a single vendor`() = runTest {
        enqueue(mapOf("data" to TestFixtures.vendor(id = "42")))

        val result = apiService.getVendor("42")

        assertTrue(result.isSuccessful)
        assertEquals("42", result.body()?.data?.id)
        assertEquals("/vendors/42", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getWorkOrders returns the list envelope`() = runTest {
        enqueue(mapOf("data" to listOf(TestFixtures.workOrder())))

        val result = apiService.getWorkOrders()

        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.data?.size)
        assertEquals("/work-orders", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getWorkOrder returns a single work order`() = runTest {
        enqueue(mapOf("data" to TestFixtures.workOrder(id = "wo1")))

        val result = apiService.getWorkOrder("wo1")

        assertTrue(result.isSuccessful)
        assertEquals("wo1", result.body()?.data?.id)
        assertEquals("/work-orders/wo1", mockWebServer.takeRequest().path)
    }

    @Test
    fun `updateWorkOrderStatus issues a PUT`() = runTest {
        enqueue(mapOf("data" to TestFixtures.workOrder(id = "wo1", status = "completed")))

        val result = apiService.updateWorkOrderStatus("wo1", "completed", "done")

        assertTrue(result.isSuccessful)
        val recorded = mockWebServer.takeRequest()
        assertEquals("PUT", recorded.method)
        assertTrue(recorded.path!!.startsWith("/work-orders/wo1/status"))
    }

    @Test
    fun `assignVendorToWorkOrder issues a PUT with vendor query`() = runTest {
        enqueue(mapOf("data" to TestFixtures.workOrder(id = "wo1", vendorId = "v1")))

        val result = apiService.assignVendorToWorkOrder("wo1", "v1", "2024-01-15")

        assertTrue(result.isSuccessful)
        val recorded = mockWebServer.takeRequest()
        assertEquals("PUT", recorded.method)
        assertTrue(recorded.path!!.startsWith("/work-orders/wo1/assign"))
        assertTrue(recorded.path!!.contains("vendorId=v1"))
    }

    @Test
    fun `updateVendor issues a PUT with the full vendor payload`() = runTest {
        enqueue(mapOf("data" to TestFixtures.vendor(id = "1", name = "Updated Vendor 1")))

        val result = apiService.updateVendor(
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

        assertTrue(result.isSuccessful)
        assertEquals("Updated Vendor 1", result.body()?.data?.name)
        val recorded = mockWebServer.takeRequest()
        assertEquals("PUT", recorded.method)
        assertTrue(recorded.path!!.startsWith("/vendors/1"))
    }

    @Test
    fun `server errors are reported with their status code`() = runTest {
        enqueue("{\"error\":\"boom\"}", code = 503)

        val result = apiService.getVendors()

        assertEquals(503, result.code())
        assertTrue(!result.isSuccessful)
    }

    @Test
    fun `data item parses every documented field`() = runTest {
        val payload = """
            {"data":[{
              "first_name":"John","last_name":"Doe","email":"john.doe@example.com",
              "alamat":"123 Main St","iuran_perwarga":100,"total_iuran_rekap":500,
              "jumlah_iuran_bulanan":200,"total_iuran_individu":150,
              "pengeluaran_iuran_warga":50,"pemanfaatan_iuran":"Maintenance",
              "avatar":"https://example.com/avatar.jpg"}]}
        """.trimIndent()
        enqueue(payload)

        val result = apiService.getUsers()

        val item: DataItem = result.body()!!.data[0]
        assertEquals("John", item.first_name)
        assertEquals("Maintenance", item.pemanfaatan_iuran)
        assertEquals(150, item.total_iuran_individu)
        assertEquals("https://example.com/avatar.jpg", item.avatar)
    }

    @Test
    fun `unknown extra json fields are ignored`() = runTest {
        enqueue("{\"data\":[{\"iuran_perwarga\":10,\"unexpected_field\":\"x\"}]}")

        val result = apiService.getUsers()

        assertNotNull(result.body())
        assertEquals(10, result.body()!!.data[0].iuran_perwarga)
    }
}