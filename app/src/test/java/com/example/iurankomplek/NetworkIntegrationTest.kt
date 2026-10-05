package com.example.iurankomplek

import com.example.iurankomplek.data.repository.PemanfaatanRepositoryImpl
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.CacheManager
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var userRepository: UserRepositoryImpl
    private lateinit var pemanfaatanRepository: PemanfaatanRepositoryImpl
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
        userRepository = UserRepositoryImpl(apiService, Mockito.mock(UserSessionManager::class.java))
        pemanfaatanRepository = PemanfaatanRepositoryImpl(apiService)
        CacheManager.getInstance().clearSync()
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
        mockWebServer.shutdown()
    }

    private fun alwaysRespond(code: Int, body: String) {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) =
                MockResponse().setResponseCode(code).setBody(body)
        }
    }

    private fun enqueueJson(code: Int, payload: Any) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(payload))
        )
    }

    private fun user(
        firstName: String = "John",
        lastName: String = "Doe",
        email: String = "john.doe@example.com",
        iuranPerwarga: Int = 100,
        totalIuranIndividu: Int = 150,
        pengeluaran: Int = 50
    ) = DataItem(
        first_name = firstName,
        last_name = lastName,
        email = email,
        alamat = "123 Main St",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = totalIuranIndividu,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = "Maintenance",
        avatar = null
    )

    @Test
    fun `UserRepository should fetch users over a real HTTP connection`() = runTest {
        enqueueJson(200, UserResponse(data = listOf(user())))

        val result = userRepository.getUsers()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.data?.size)
        assertEquals("john.doe@example.com", result.getOrNull()?.data?.first()?.email)
        assertEquals("/users", mockWebServer.takeRequest().path)
    }

    @Test
    fun `UserRepository should serve the second read from cache`() = runTest {
        enqueueJson(200, UserResponse(data = listOf(user())))

        val first = userRepository.getUsers()
        val second = userRepository.getUsers()

        assertTrue(first.isSuccess)
        assertEquals(first.getOrNull(), second.getOrNull())
        assertEquals(1, mockWebServer.requestCount)
    }

    @Test
    fun `UserRepository should fail when the server returns an error`() = runTest {
        alwaysRespond(500, "{\"error\": \"Internal Server Error\"}")

        val result = userRepository.getUsers()

        assertTrue(result.isFailure)
    }

    @Test
    fun `PemanfaatanRepository should parse financial response correctly`() = runTest {
        enqueueJson(
            200,
            PemanfaatanResponse(data = listOf(user(firstName = "Jane", lastName = "Smith")))
        )

        val result = pemanfaatanRepository.getPemanfaatan()

        assertTrue(result.isSuccess)
        val item = result.getOrNull()?.data?.first()
        assertEquals("Jane", item?.first_name)
        assertEquals("Smith", item?.last_name)
        assertEquals(100, item?.iuran_perwarga)
        assertEquals("/pemanfaatan", mockWebServer.takeRequest().path)
    }

    @Test
    fun `PemanfaatanRepository should surface an error for a 404`() = runTest {
        enqueueJson(404, mapOf("error" to "Not found"))

        val result = pemanfaatanRepository.getPemanfaatan()

        assertTrue(result.isFailure)
    }

    @Test
    fun `PemanfaatanRepository should not cache a failed response`() = runTest {
        alwaysRespond(500, "{\"error\": \"Internal Server Error\"}")
        val failed = pemanfaatanRepository.getPemanfaatan()
        assertTrue(failed.isFailure)

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(PemanfaatanResponse(data = listOf(user()))))
        }

        val retried = pemanfaatanRepository.getPemanfaatan()

        assertTrue(retried.isSuccess)
        assertEquals(1, retried.getOrNull()?.data?.size)
    }
}