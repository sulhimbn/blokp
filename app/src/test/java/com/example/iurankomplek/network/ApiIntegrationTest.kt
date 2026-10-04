package com.example.iurankomplek.network

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.model.DataItem
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Wire-level contract tests: a real Retrofit stack (Gson converter) talking to
 * [MockWebServer], so the declared @GET paths and response models are verified
 * end-to-end rather than against hand-built stubs.
 */
class ApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

    private fun sampleItem() = DataItem(
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

    private fun enqueue(code: Int, body: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

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

    @Test
    fun `getUsers requests the users path and parses the payload`() = runTest {
        enqueue(200, gson.toJson(UserResponse(data = listOf(sampleItem()))))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        assertEquals("/users", mockWebServer.takeRequest().requestUrl!!.encodedPath)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("John", body.data[0].first_name)
        assertEquals("Doe", body.data[0].last_name)
        assertEquals("john.doe@example.com", body.data[0].email)
        assertEquals(100, body.data[0].iuran_perwarga)
    }

    @Test
    fun `getUsers surfaces an empty payload as a successful response`() = runTest {
        enqueue(200, gson.toJson(UserResponse(data = emptyList())))

        val response = apiService.getUsers()

        assertTrue(response.isSuccessful)
        assertEquals(0, response.body()!!.data.size)
    }

    @Test
    fun `getUsers reports a server error response`() = runTest {
        enqueue(500, """{"error":"boom"}""")

        val response = apiService.getUsers()

        assertEquals(500, response.code())
        assertEquals(null, response.body())
    }

    @Test
    fun `getPemanfaatan requests the pemanfaatan path and parses financial rows`() = runTest {
        enqueue(200, gson.toJson(PemanfaatanResponse(data = listOf(sampleItem()))))

        val response = apiService.getPemanfaatan()

        assertTrue(response.isSuccessful)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().requestUrl!!.encodedPath)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals(150, body.data[0].total_iuran_individu)
        assertEquals("Maintenance", body.data[0].pemanfaatan_iuran)
    }

    @Test
    fun `getPemanfaatan reports a server error response`() = runTest {
        enqueue(503, """{"error":"unavailable"}""")

        val response = apiService.getPemanfaatan()

        assertEquals(503, response.code())
        assertEquals(null, response.body())
    }

    @Test
    fun `getAnnouncements requests the announcements path and parses the list`() = runTest {
        val announcements = listOf(
            Announcement(
                id = "a1",
                title = "Water shutoff",
                content = "Maintenance window",
                category = "maintenance",
                priority = "high",
                createdAt = "2024-01-01T00:00:00Z",
                readBy = listOf("user1")
            )
        )
        enqueue(200, gson.toJson(announcements))

        val response = apiService.getAnnouncements()

        assertTrue(response.isSuccessful)
        assertEquals("/announcements", mockWebServer.takeRequest().requestUrl!!.encodedPath)
        val body = response.body()
        assertNotNull(body)
        assertEquals(1, body!!.size)
        assertEquals("Water shutoff", body[0].title)
        assertEquals("high", body[0].priority)
        assertEquals(listOf("user1"), body[0].readBy)
    }

    @Test
    fun `getMessages passes the userId as a query parameter`() = runTest {
        enqueue(200, "[]")

        val response = apiService.getMessages("user1")

        assertTrue(response.isSuccessful)
        val recorded = mockWebServer.takeRequest()
        assertEquals("/messages", recorded.requestUrl!!.encodedPath)
        assertEquals("user1", recorded.requestUrl?.queryParameter("userId"))
    }

    @Test
    fun `getCommunityPosts requests the community-posts path`() = runTest {
        val posts = listOf(
            CommunityPost(
                id = "p1",
                authorId = "user1",
                title = "Quiet hours",
                content = "Please keep noise down after 22:00",
                category = "announcement",
                likes = 3,
                comments = emptyList(),
                createdAt = "2024-01-01T00:00:00Z"
            )
        )
        enqueue(200, gson.toJson(posts))

        val response = apiService.getCommunityPosts()

        assertTrue(response.isSuccessful)
        assertEquals("/community-posts", mockWebServer.takeRequest().requestUrl!!.encodedPath)
        assertEquals("Quiet hours", response.body()!![0].title)
    }

    @Test
    fun `an unexpected status leaves the body null`() = runTest {
        enqueue(401, """{"error":"unauthorized"}""")

        val response = apiService.getPemanfaatan()

        assertEquals(401, response.code())
        assertTrue(!response.isSuccessful)
        assertEquals(null, response.body())
    }
}