package com.example.iurankomplek

import com.example.iurankomplek.data.repository.PemanfaatanRepositoryImpl
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.CacheManager
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: UserSessionManager

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        sessionManager = mock(UserSessionManager::class.java)
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        CacheManager.getInstance().clearSync()
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
        mockWebServer.shutdown()
    }

    @Test
    fun `UserRepositoryImpl should return users fetched over the wire`() {
        mockWebServer.enqueue(
            jsonResponse(
                Gson().toJson(UserResponse(data = listOf(TestFixtures.dataItem(first_name = "Ada"))))
            )
        )
        val repository = UserRepositoryImpl(apiService, sessionManager)

        val result = runBlocking { repository.getUsers() }

        assertTrue(result.isSuccess)
        assertEquals("Ada", result.getOrNull()?.data?.single()?.first_name)
        assertEquals("/users", mockWebServer.takeRequest().path)
    }

    @Test
    fun `UserRepositoryImpl should fail on a server error`() {
        mockWebServer.enqueue(MockResponse().apply {
            setResponseCode(500)
            setBody("boom")
        })
        val repository = UserRepositoryImpl(apiService, sessionManager)

        val result = runBlocking { repository.getUsers() }

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `UserRepositoryImpl should serve the second call from cache`() {
        mockWebServer.enqueue(
            jsonResponse(
                Gson().toJson(UserResponse(data = listOf(TestFixtures.dataItem(first_name = "Ada"))))
            )
        )
        val repository = UserRepositoryImpl(apiService, sessionManager)

        val first = runBlocking { repository.getUsers() }
        val second = runBlocking { repository.getUsers() }

        assertEquals(first.getOrNull(), second.getOrNull())
        assertEquals(1, mockWebServer.requestCount)
    }

    @Test
    fun `PemanfaatanRepositoryImpl should return pemanfaatan fetched over the wire`() {
        mockWebServer.enqueue(
            jsonResponse(
                Gson().toJson(
                    PemanfaatanResponse(
                        data = listOf(
                            TestFixtures.dataItem(
                                pemanfaatan_iuran = "Perbaikan jalan",
                                pengeluaran_iuran_warga = 50000
                            )
                        )
                    )
                )
            )
        )
        val repository = PemanfaatanRepositoryImpl(apiService)

        val result = runBlocking { repository.getPemanfaatan() }

        assertTrue(result.isSuccess)
        assertEquals(50000, result.getOrNull()?.data?.single()?.pengeluaran_iuran_warga)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `PemanfaatanRepositoryImpl should fail when the body is empty`() {
        mockWebServer.enqueue(MockResponse().apply { setResponseCode(200) })
        val repository = PemanfaatanRepositoryImpl(apiService)

        val result = runBlocking { repository.getPemanfaatan() }

        assertTrue(result.isFailure)
    }

    @Test
    fun `ApiConfig should build a usable ApiService`() {
        assertNotNull(ApiConfig.getApiService())
    }

    private fun jsonResponse(body: String) = MockResponse().apply {
            setResponseCode(200)
            setHeader("Content-Type", "application/json")
            setBody(body)
        }
}