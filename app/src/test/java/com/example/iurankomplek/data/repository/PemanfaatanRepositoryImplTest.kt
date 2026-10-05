package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * PemanfaatanRepositoryImpl is driven through a real Retrofit client against
 * MockWebServer because ApiService is suspend-only, which Mockito cannot stub.
 */
class PemanfaatanRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: PemanfaatanRepositoryImpl
    private val gson = Gson()

    private fun dataItem() = DataItem(
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

    private fun buildService(baseUrl: String): ApiService =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

    @Before
    fun setup() {
        CacheManager.getInstance().clearSync()
        server = MockWebServer()
        server.start()
        repository = PemanfaatanRepositoryImpl(buildService(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
        CacheManager.getInstance().clearSync()
    }

    @Test
    fun `getPemanfaatan returns parsed items on 200`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(PemanfaatanResponse(listOf(dataItem()))))
        )

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        val body = result.getOrNull()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals(150, body.data[0].total_iuran_individu)
        assertEquals("/pemanfaatan", server.takeRequest().path)
    }

    @Test
    fun `getPemanfaatan returns an empty list on 200`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(PemanfaatanResponse(emptyList())))
        )

        val result = repository.getPemanfaatan()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.data.isEmpty())
    }

    @Test
    fun `getPemanfaatan fails on 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `getPemanfaatan fails after exhausting retries on 500`() = runTest {
        repeat(4) {
            server.enqueue(MockResponse().setResponseCode(500).setBody("Server error"))
        }

        val result = repository.getPemanfaatan()

        assertTrue(result.isFailure)
        assertEquals(4, server.requestCount)
    }

    @Test
    fun `getPemanfaatan fails when transport is unavailable`() = runTest {
        val offline = PemanfaatanRepositoryImpl(buildService("http://127.0.0.1:1/"))

        val result = offline.getPemanfaatan()

        assertTrue(result.isFailure)
        assertEquals("Network error occurred", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getPemanfaatan serves the second call from cache`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(PemanfaatanResponse(listOf(dataItem()))))
        )

        val first = repository.getPemanfaatan()
        val second = repository.getPemanfaatan()

        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)
        assertEquals(1, server.requestCount)
    }
}