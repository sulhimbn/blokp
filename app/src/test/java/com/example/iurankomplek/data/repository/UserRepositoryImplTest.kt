package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
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
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * UserRepositoryImpl is driven through a real Retrofit client against MockWebServer
 * because ApiService is suspend-only, which Mockito cannot stub.
 */
@RunWith(MockitoJUnitRunner::class)
class UserRepositoryImplTest {

    @Mock
    private lateinit var sessionManager: UserSessionManager

    private lateinit var server: MockWebServer
    private lateinit var repository: UserRepositoryImpl
    private val gson = Gson()

    private fun dataItem(email: String = "john.doe@example.com") = DataItem(
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
        repository = UserRepositoryImpl(buildService(server.url("/").toString()), sessionManager)
    }

    @After
    fun tearDown() {
        server.shutdown()
        CacheManager.getInstance().clearSync()
    }

    @Test
    fun `getUsers returns parsed users on 200`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(UserResponse(listOf(dataItem()))))
        )

        val result = repository.getUsers()

        assertTrue(result.isSuccess)
        val body = result.getOrNull()
        assertNotNull(body)
        assertEquals(1, body!!.data.size)
        assertEquals("john.doe@example.com", body.data[0].email)
        assertEquals("/users", server.takeRequest().path)
    }

    @Test
    fun `getUsers fails on 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `getUsers fails on 500 after exhausting retries`() = runTest {
        repeat(4) {
            server.enqueue(MockResponse().setResponseCode(500).setBody("Server error"))
        }

        val result = repository.getUsers()

        assertTrue(result.isFailure)
        // 5xx is retryable, so the repository exhausts its retry budget.
        assertEquals(4, server.requestCount)
    }

    @Test
    fun `getUsers fails when transport is unavailable`() = runTest {
        val offline = UserRepositoryImpl(buildService("http://127.0.0.1:1/"), sessionManager)

        val result = offline.getUsers()

        assertTrue(result.isFailure)
        assertEquals("Network error occurred", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUsers serves the second call from cache`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(UserResponse(listOf(dataItem()))))
        )

        val first = repository.getUsers()
        val second = repository.getUsers()

        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `login returns the authenticated user for a known email`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(UserResponse(listOf(dataItem()))))
        )

        val result = repository.login("john.doe@example.com", "irrelevant")

        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("john.doe@example.com", user!!.email)
        assertEquals("John Doe", user.fullName)
        server.takeRequest()
    }

    @Test
    fun `login fails for an unknown email`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(UserResponse(listOf(dataItem()))))
        )

        val result = repository.login("nobody@example.com", "irrelevant")

        assertTrue(result.isFailure)
        assertEquals("Invalid credentials", result.exceptionOrNull()?.message)
    }
}