package com.example.iurankomplek.network

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
import java.net.URLDecoder

class ApiIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService

    private val usersJson = """
        {"data":[
          {"first_name":"John","last_name":"Doe","email":"john@example.com",
           "alamat":"Jl. Merdeka 1","iuran_perwarga":150000,
           "total_iuran_rekap":1800000,"jumlah_iuran_bulanan":150000,
           "total_iuran_individu":150000,"pengeluaran_iuran_warga":50000,
           "pemanfaatan_iuran":"Perbaikan jalan komplek",
           "avatar":"https://example.com/avatar1.jpg"},
          {"first_name":"Jane","last_name":"Smith","email":"jane@example.com",
           "alamat":"Jl. Anggrek 2","iuran_perwarga":200000,
           "total_iuran_rekap":2400000,"jumlah_iuran_bulanan":200000,
           "total_iuran_individu":200000,"pengeluaran_iuran_warga":75000,
           "pemanfaatan_iuran":"Perbaikan jalan",
           "avatar":"https://example.com/avatar2.jpg"}
        ]}
    """.trimIndent()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/data/QjX6hB1ST2IDKaxB/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getUsers hits the users path and parses every DataItem field`() {
        server.enqueue(MockResponse().setBody(usersJson).setHeader("Content-Type", "application/json"))

        val body = runBlockingApi { api.getUsers() }

        val request = server.takeRequest()
        assertEquals("/data/QjX6hB1ST2IDKaxB/users", request.path)
        assertEquals("GET", request.method)
        assertTrue(body.isSuccessful)

        val rows = requireNotNull(body.body()).data
        assertEquals(2, rows.size)

        val john = rows.first()
        assertEquals("John", john.first_name)
        assertEquals("Doe", john.last_name)
        assertEquals("john@example.com", john.email)
        assertEquals("Jl. Merdeka 1", john.alamat)
        assertEquals(150000, john.iuran_perwarga)
        assertEquals(1800000, john.total_iuran_rekap)
        assertEquals(150000, john.jumlah_iuran_bulanan)
        assertEquals(150000, john.total_iuran_individu)
        assertEquals(50000, john.pengeluaran_iuran_warga)
        assertEquals("Perbaikan jalan komplek", john.pemanfaatan_iuran)
        assertEquals("https://example.com/avatar1.jpg", john.avatar)
    }

    @Test
    fun `getPemanfaatan hits the pemanfaatan path`() {
        server.enqueue(MockResponse().setBody(usersJson).setHeader("Content-Type", "application/json"))

        val body = runBlockingApi { api.getPemanfaatan() }

        assertEquals("/data/QjX6hB1ST2IDKaxB/pemanfaatan", server.takeRequest().path)
        assertEquals(2, body.body()?.data?.size)
    }

    @Test
    fun `a 500 response is reported as unsuccessful rather than parsed`() {
        server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))

        val body = runBlockingApi { api.getUsers() }

        assertTrue(!body.isSuccessful)
        assertEquals(500, body.code())
        assertNull(body.body())
    }

    @Test
    fun `a 404 response is reported as unsuccessful`() {
        server.enqueue(MockResponse().setResponseCode(404))

        val body = runBlockingApi { api.getUsers() }

        assertEquals(404, body.code())
    }

    @Test
    fun `missing JSON fields leave nullable strings null and numeric fields at zero`() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""{"data":[{"email":"only@email.com","iuran_perwarga":10}]}""")
        )

        val row = runBlockingApi { api.getUsers() }.body()!!.data.single()

        assertEquals("only@email.com", row.email)
        assertNull(row.first_name)
        assertNull(row.last_name)
        assertNull(row.alamat)
        assertNull(row.pemanfaatan_iuran)
        assertNull(row.avatar)
        assertEquals(0, row.total_iuran_individu)
        assertEquals(0, row.total_iuran_rekap)
    }

    @Test
    fun `getMessagesWithUser encodes path and query parameters`() {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("[]"))

        runBlockingApi { api.getMessagesWithUser("user 2", "user-1") }

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        val path = URLDecoder.decode(requireNotNull(request.path), "UTF-8")
        assertTrue("path was $path", path.startsWith("/data/QjX6hB1ST2IDKaxB/messages/user 2"))
        assertTrue("path was $path", path.contains("senderId=user-1"))
    }

    @Test
    fun `sendMessage serialises its arguments as query parameters`() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":"m-1","senderId":"u1","receiverId":"u2","content":"hi","timestamp":"t","readStatus":false,"attachments":[]}""")
        )

        val sent = runBlockingApi { api.sendMessage("u1", "u2", "hi") }

        assertTrue(sent.isSuccessful)
        assertEquals("m-1", sent.body()!!.id)
        val path = URLDecoder.decode(requireNotNull(server.takeRequest().path), "UTF-8")
        assertTrue("path was $path", path.contains("senderId=u1"))
        assertTrue("path was $path", path.contains("receiverId=u2"))
        assertTrue("path was $path", path.contains("content=hi"))
    }

    @Test
    fun `a DataItem built from JSON preserves the exact integer amounts`() {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(usersJson))

        val rows = runBlockingApi { api.getUsers() }.body()!!.data

        assertEquals(listOf(150000, 200000), rows.map { it.iuran_perwarga })
        assertEquals(listOf(150000, 200000), rows.map { it.total_iuran_individu })
    }

    private fun <T> runBlockingApi(block: suspend () -> T): T =
        kotlinx.coroutines.runBlocking { block() }

}
