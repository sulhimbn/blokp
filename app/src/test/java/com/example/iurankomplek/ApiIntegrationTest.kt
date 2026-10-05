package com.example.iurankomplek

import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.google.gson.Gson
import com.google.gson.JsonParseException
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Verifies that [ApiService] correctly deserialises the real wire contract.
 *
 * ApiService exposes suspend functions returning retrofit2.Response, so these tests
 * drive it directly against a MockWebServer and assert on the parsed body.
 */
class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
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

    @Test
    fun `getUsers should parse response correctly`() = runTest {
        enqueueJson(gson.toJson(TestFixtures.userResponse(TestFixtures.dataItem())))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("John", body.data[0].first_name)
        assertEquals("Doe", body.data[0].last_name)
        assertEquals("john.doe@example.com", body.data[0].email)
        assertEquals("/users", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getUsers should handle empty response`() = runTest {
        enqueueJson(gson.toJson(TestFixtures.userResponse()))

        val result = apiService.getUsers()

        assertTrue(result.isSuccessful)
        assertTrue(result.body()?.data?.isEmpty() == true)
    }

    @Test
    fun `getUsers should surface server error`() = runTest {
        enqueueJson("{\"error\": \"Internal Server Error\"}", code = 500)

        val result = apiService.getUsers()

        assertEquals(500, result.code())
        assertTrue(!result.isSuccessful)
        assertNull(result.body())
    }

    @Test
    fun `getPemanfaatan should parse response correctly`() = runTest {
        val item = TestFixtures.dataItem(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com",
            pemanfaatanIuran = "Repairs"
        )
        enqueueJson(gson.toJson(TestFixtures.pemanfaatanResponse(item)))

        val result = apiService.getPemanfaatan()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("Jane", body.data[0].first_name)
        assertEquals("Repairs", body.data[0].pemanfaatan_iuran)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `getPemanfaatan should surface 404`() = runTest {
        enqueueJson("{\"error\": \"Not found\"}", code = 404)

        val result = apiService.getPemanfaatan()

        assertEquals(404, result.code())
        assertTrue(!result.isSuccessful)
    }

    @Test
    fun `getAnnouncements should parse response correctly`() = runTest {
        enqueueJson(gson.toJson(listOf(TestFixtures.announcement())))

        val result = apiService.getAnnouncements()

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Community Meeting", body[0].title)
        assertEquals("high", body[0].priority)
    }

    @Test
    fun `getMessages should parse response correctly`() = runTest {
        enqueueJson(gson.toJson(listOf(TestFixtures.message())))

        val result = apiService.getMessages("user1")

        assertTrue(result.isSuccessful)
        val body = result.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Hello, how are you?", body[0].content)
        assertEquals("/messages?userId=user1", mockWebServer.takeRequest().path)
    }

    @Test
    fun `sendMessage should post query parameters`() = runTest {
        val sent = TestFixtures.message(id = "99", senderId = "user1", receiverId = "user2", content = "Hi")
        enqueueJson(gson.toJson(sent))

        val result = apiService.sendMessage("user1", "user2", "Hi")

        assertTrue(result.isSuccessful)
        assertEquals("99", result.body()?.id)
        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.startsWith("/messages?"))
        assertTrue(recorded.path!!.contains("content=Hi"))
    }

    @Test
    fun `getVendors should parse response correctly`() = runTest {
        enqueueJson("{\"data\": [${gson.toJson(TestFixtures.vendor())}]}")

        val result = apiService.getVendors()

        assertTrue(result.isSuccessful)
        assertEquals(1, result.body()?.data?.size)
        assertEquals("Plumbing Services Inc", result.body()?.data?.get(0)?.name)
        assertEquals("/vendors", mockWebServer.takeRequest().path)
    }

    @Test
    fun `createWorkOrder should post the full work order contract`() = runTest {
        enqueueJson("{\"data\": ${gson.toJson(TestFixtures.workOrder())}}")

        val result = apiService.createWorkOrder(
            title = "Fix Leaking Pipe",
            description = "Pipe is leaking in bathroom",
            category = "Plumbing",
            priority = "high",
            propertyId = "prop1",
            reporterId = "user1",
            estimatedCost = 150.0
        )

        assertTrue(result.isSuccessful)
        assertEquals("WO-001", result.body()?.data?.id)
        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.startsWith("/work-orders?"))
        assertTrue(recorded.path!!.contains("estimatedCost=150.0"))
    }

    @Test
    fun `malformed json body should surface a parse failure`() = runTest {
        enqueueJson("this-is-not-json")

        val thrown = runCatching { apiService.getUsers() }.exceptionOrNull()

        assertTrue(
            "expected a deserialisation failure but got $thrown",
            thrown is JsonParseException || thrown is IOException
        )
    }

    @Test
    fun `user response keeps nullable fields null when absent`() = runTest {
        enqueueJson("{\"data\": [{\"iuran_perwarga\": 250, \"total_iuran_individu\": 90}]}")

        val result: retrofit2.Response<UserResponse> = apiService.getUsers()

        assertTrue(result.isSuccessful)
        val item = result.body()!!.data[0]
        assertEquals(250, item.iuran_perwarga)
        assertEquals(90, item.total_iuran_individu)
        assertNull(item.first_name)
        assertNull(item.email)
        assertNull(item.avatar)
    }

    @Test
    fun `pemanfaatan response keeps nullable fields null when absent`() = runTest {
        enqueueJson("{\"data\": [{\"pengeluaran_iuran_warga\": 12}]}")

        val result: retrofit2.Response<PemanfaatanResponse> = apiService.getPemanfaatan()

        assertTrue(result.isSuccessful)
        val item = result.body()!!.data[0]
        assertEquals(12, item.pengeluaran_iuran_warga)
        assertNull(item.pemanfaatan_iuran)
        assertNull(item.alamat)
    }
}