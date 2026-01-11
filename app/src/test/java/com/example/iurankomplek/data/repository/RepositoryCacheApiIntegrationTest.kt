package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.cache.DatabasePreloader
import com.example.iurankomplek.data.cache.InMemoryCacheStrategy
import com.example.iurankomplek.data.cache.NoCacheStrategy
import com.example.iurankomplek.data.entity.FinancialRecordEntity
import com.example.iurankomplek.data.entity.UserEntity
import com.example.iurankomplek.data.entity.UserWithFinancialRecords
import com.example.iurankomplek.data.mapper.EntityMapper
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.data.repository.PemanfaatanRepositoryImpl
import com.example.iurankomplek.domain.usecase.CalculateFinancialTotalsUseCase
import com.example.iurankomplek.domain.usecase.ValidateFinancialDataUseCase
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiServiceV1
import com.example.iurankomplek.network.model.NetworkError
import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryCacheApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiServiceV1
    private lateinit var userRepository: UserRepositoryImpl
    private lateinit var pemanfaatanRepository: PemanfaatanRepositoryImpl
    private lateinit var inMemoryCacheStrategy: InMemoryCacheStrategy
    private lateinit var noCacheStrategy: NoCacheStrategy

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())

        mockWebServer = MockWebServer()
        mockWebServer.start(8081)

        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiServiceV1::class.java)

        userRepository = UserRepositoryImpl(apiService)
        pemanfaatanRepository = PemanfaatanRepositoryImpl(apiService)

        inMemoryCacheStrategy = InMemoryCacheStrategy()
        noCacheStrategy = NoCacheStrategy()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        Dispatchers.resetMain()
        inMemoryCacheStrategy.clear()
        CacheManager.clearAllCaches()
    }

    @Test
    fun `userRepository loads from API when cache is empty`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "success": true,
                        "data": [
                            {
                                "id": 1,
                                "first_name": "John",
                                "last_name": "Doe",
                                "email": "john.doe@example.com",
                                "alamat": "123 Main St",
                                "iuran_perwarga": 100000,
                                "total_iuran_individu": 50000,
                                "pengeluaran_iuran_warga": 20000,
                                "pemanfaatan_iuran": "Maintenance",
                                "avatar": "https://example.com/avatar.jpg",
                                "created_at": ${System.currentTimeMillis()},
                                "updated_at": ${System.currentTimeMillis()},
                                "is_deleted": false
                            }
                        ]
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val result = userRepository.getUsers(forceRefresh = true)

        assertTrue("Result should be Success", result is OperationResult.Success)
        val userResponse = (result as OperationResult.Success).data
        assertEquals("Should have 1 user", 1, userResponse.data.size)
        assertEquals("First name should match", "John", userResponse.data[0].first_name)
    }

    @Test
    fun `userRepository loads from cache when available and fresh`() {
        val cachedUser = UserWithFinancialRecords(
            user = UserEntity(
                id = 1,
                firstName = "Cached",
                lastName = "User",
                email = "cached@example.com",
                alamat = "Cached Address",
                iuranPerwarga = 100000,
                totalIuranIndividu = 50000,
                pengeluaranIuranWarga = 20000,
                pemanfaatanIuran = "Cached Usage",
                avatar = "https://example.com/cached.jpg",
                createdAt = Date(),
                updatedAt = Date(),
                isDeleted = false
            ),
            financialRecords = emptyList()
        )

        CacheManager.getUserDao().insert(cachedUser.user)
        CacheManager.getUserDao().insertFinancialRecords(emptyList())

        Thread.sleep(100)

        val result = userRepository.getUsers(forceRefresh = false)

        assertTrue("Result should be Success", result is OperationResult.Success)
        val userResponse = (result as OperationResult.Success).data
        assertEquals("Should load from cache", "Cached", userResponse.data[0].first_name)
    }

    @Test
    fun `userRepository bypasses cache when forceRefresh is true`() {
        val cachedUser = UserEntity(
            id = 1,
            firstName = "Cached",
            lastName = "User",
            email = "cached@example.com",
            alamat = "Cached Address",
            iuranPerwarga = 100000,
            totalIuranIndividu = 50000,
            pengeluaranIuranWarga = 20000,
            pemanfaatanIuran = "Cached Usage",
            avatar = "https://example.com/cached.jpg",
            createdAt = Date(),
            updatedAt = Date(),
            isDeleted = false
        )

        CacheManager.getUserDao().insert(cachedUser)

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "success": true,
                        "data": [
                            {
                                "id": 1,
                                "first_name": "Fresh",
                                "last_name": "User",
                                "email": "fresh@example.com",
                                "alamat": "Fresh Address",
                                "iuran_perwarga": 100000,
                                "total_iuran_individu": 50000,
                                "pengeluaran_iuran_warga": 20000,
                                "pemanfaatan_iuran": "Fresh Usage",
                                "avatar": "https://example.com/fresh.jpg",
                                "created_at": ${System.currentTimeMillis()},
                                "updated_at": ${System.currentTimeMillis()},
                                "is_deleted": false
                            }
                        ]
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val result = userRepository.getUsers(forceRefresh = true)

        assertTrue("Result should be Success", result is OperationResult.Success)
        val userResponse = (result as OperationResult.Success).data
        assertEquals("Should load fresh data from API", "Fresh", userResponse.data[0].first_name)
    }

    @Test
    fun `userRepository handles API errors gracefully`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""
                    {
                        "success": false,
                        "error": "Internal server error"
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val result = userRepository.getUsers(forceRefresh = true)

        assertTrue("Result should be Error", result is OperationResult.Error)
        val error = (result as OperationResult.Error)
        assertNotNull("Error should not be null", error.throwable)
        assertNotNull("Error message should not be null", error.message)
    }

    @Test
    fun `userRepository handles network errors`() {
        mockWebServer.shutdown()

        val result = userRepository.getUsers(forceRefresh = true)

        assertTrue("Result should be Error", result is OperationResult.Error)
        val error = (result as OperationResult.Error)
        assertNotNull("Error should not be null", error.throwable)
    }

    @Test
    fun `pemanfaatanRepository loads financial data with validation`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "success": true,
                        "data": [
                            {
                                "id": 1,
                                "first_name": "Alice",
                                "last_name": "Smith",
                                "email": "alice@example.com",
                                "alamat": "456 Oak Ave",
                                "iuran_perwarga": 100000,
                                "total_iuran_individu": 50000,
                                "pengeluaran_iuran_warga": 20000,
                                "pemanfaatan_iuran": "Maintenance",
                                "avatar": "https://example.com/alice.jpg",
                                "created_at": ${System.currentTimeMillis()},
                                "updated_at": ${System.currentTimeMillis()},
                                "is_deleted": false
                            }
                        ]
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val result = pemanfaatanRepository.getPemanfaatan(forceRefresh = true)

        assertTrue("Result should be Success", result is OperationResult.Success)
        val pemanfaatanResponse = (result as OperationResult.Success).data
        assertEquals("Should have 1 financial record", 1, pemanfaatanResponse.data.size)
        assertEquals("First name should match", "Alice", pemanfaatanResponse.data[0].first_name)
    }

    @Test
    fun `pemanfaatanRepository validates financial calculations`() {
        val validateFinancialDataUseCase = ValidateFinancialDataUseCase()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "success": true,
                        "data": [
                            {
                                "id": 1,
                                "first_name": "Bob",
                                "last_name": "Johnson",
                                "email": "bob@example.com",
                                "alamat": "789 Pine St",
                                "iuran_perwarga": 100000,
                                "total_iuran_individu": 50000,
                                "pengeluaran_iuran_warga": 20000,
                                "pemanfaatan_iuran": "Cleaning",
                                "avatar": "https://example.com/bob.jpg",
                                "created_at": ${System.currentTimeMillis()},
                                "updated_at": ${System.currentTimeMillis()},
                                "is_deleted": false
                            }
                        ]
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val result = pemanfaatanRepository.getPemanfaatan(forceRefresh = true)

        assertTrue("Result should be Success", result is OperationResult.Success)
        val pemanfaatanResponse = (result as OperationResult.Success).data

        val validationResult = validateFinancialDataUseCase.invoke(pemanfaatanResponse.data)
        assertTrue("Financial data should be valid", validationResult)
    }

    @Test
    fun `repositories handle concurrent requests safely`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "success": true,
                        "data": [
                            {
                                "id": 1,
                                "first_name": "Concurrent",
                                "last_name": "Test",
                                "email": "concurrent@example.com",
                                "alamat": "Concurrent Address",
                                "iuran_perwarga": 100000,
                                "total_iuran_individu": 50000,
                                "pengeluaran_iuran_warga": 20000,
                                "pemanfaatan_iuran": "Concurrent Usage",
                                "avatar": "https://example.com/concurrent.jpg",
                                "created_at": ${System.currentTimeMillis()},
                                "updated_at": ${System.currentTimeMillis()},
                                "is_deleted": false
                            }
                        ]
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val latch = java.util.concurrent.CountDownLatch(3)
        val results = mutableListOf<OperationResult<*>>()

        for (i in 1..3) {
            kotlinx.coroutines.GlobalScope.launch(Dispatchers.Default) {
                val result = userRepository.getUsers(forceRefresh = true)
                synchronized(results) {
                    results.add(result)
                }
                latch.countDown()
            }
        }

        assertTrue("All requests should complete", latch.await(10, java.util.concurrent.TimeUnit.SECONDS))

        assertTrue("All results should be Success", results.all { it is OperationResult.Success })
    }

    @Test
    fun `repositories cache invalidation works correctly`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "success": true,
                        "data": [
                            {
                                "id": 1,
                                "first_name": "Initial",
                                "last_name": "User",
                                "email": "initial@example.com",
                                "alamat": "Initial Address",
                                "iuran_perwarga": 100000,
                                "total_iuran_individu": 50000,
                                "pengeluaran_iuran_warga": 20000,
                                "pemanfaatan_iuran": "Initial Usage",
                                "avatar": "https://example.com/initial.jpg",
                                "created_at": ${System.currentTimeMillis()},
                                "updated_at": ${System.currentTimeMillis()},
                                "is_deleted": false
                            }
                        ]
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val firstResult = userRepository.getUsers(forceRefresh = true)
        assertTrue("First load should succeed", firstResult is OperationResult.Success)

        userRepository.clearCache()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "success": true,
                        "data": [
                            {
                                "id": 1,
                                "first_name": "Updated",
                                "last_name": "User",
                                "email": "updated@example.com",
                                "alamat": "Updated Address",
                                "iuran_perwarga": 100000,
                                "total_iuran_individu": 50000,
                                "pengeluaran_iuran_warga": 20000,
                                "pemanfaatan_iuran": "Updated Usage",
                                "avatar": "https://example.com/updated.jpg",
                                "created_at": ${System.currentTimeMillis()},
                                "updated_at": ${System.currentTimeMillis()},
                                "is_deleted": false
                            }
                        ]
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val secondResult = userRepository.getUsers(forceRefresh = false)
        assertTrue("Second load should succeed", secondResult is OperationResult.Success)

        val userResponse = (secondResult as OperationResult.Success).data
        assertEquals("Should load updated data", "Updated", userResponse.data[0].first_name)
    }

    @Test
    fun `repositories handle empty API responses`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "success": true,
                        "data": []
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val result = userRepository.getUsers(forceRefresh = true)

        assertTrue("Result should be Success", result is OperationResult.Success)
        val userResponse = (result as OperationResult.Success).data
        assertEquals("Data should be empty", 0, userResponse.data.size)
    }

    @Test
    fun `repositories validate data integrity on cache load`() {
        val validateFinancialDataUseCase = ValidateFinancialDataUseCase()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "success": true,
                        "data": [
                            {
                                "id": 1,
                                "first_name": "Integrity",
                                "last_name": "Test",
                                "email": "integrity@example.com",
                                "alamat": "Integrity Address",
                                "iuran_perwarga": 100000,
                                "total_iuran_individu": 50000,
                                "pengeluaran_iuran_warga": 20000,
                                "pemanfaatan_iuran": "Integrity Usage",
                                "avatar": "https://example.com/integrity.jpg",
                                "created_at": ${System.currentTimeMillis()},
                                "updated_at": ${System.currentTimeMillis()},
                                "is_deleted": false
                            }
                        ]
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val loadResult = userRepository.getUsers(forceRefresh = true)
        assertTrue("Load should succeed", loadResult is OperationResult.Success)

        val cacheResult = userRepository.getCachedUsers()
        assertTrue("Cache load should succeed", cacheResult is OperationResult.Success)

        val cachedData = (cacheResult as OperationResult.Success).data
        val validationResult = validateFinancialDataUseCase.invoke(cachedData.data)
        assertTrue("Cached data should be valid", validationResult)
    }

    @Test
    fun `inMemoryCacheStrategy stores and retrieves data correctly`() {
        val testData = UserWithFinancialRecords(
            user = UserEntity(
                id = 1,
                firstName = "Memory",
                lastName = "Cache",
                email = "memory@example.com",
                alamat = "Memory Address",
                iuranPerwarga = 100000,
                totalIuranIndividu = 50000,
                pengeluaranIuranWarga = 20000,
                pemanfaatanIuran = "Memory Usage",
                avatar = "https://example.com/memory.jpg",
                createdAt = Date(),
                updatedAt = Date(),
                isDeleted = false
            ),
            financialRecords = emptyList()
        )

        inMemoryCacheStrategy.put("test_key", testData, 30000)

        val retrieved = inMemoryCacheStrategy.get("test_key") { testData }
        assertNotNull("Should retrieve cached data", retrieved)
        assertEquals("Data should match", "Memory", retrieved!!.user.firstName)
    }

    @Test
    fun `noCacheStrategy always fetches from source`() {
        val testData = UserWithFinancialRecords(
            user = UserEntity(
                id = 1,
                firstName = "No",
                lastName = "Cache",
                email = "nocache@example.com",
                alamat = "No Cache Address",
                iuranPerwarga = 100000,
                totalIuranIndividu = 50000,
                pengeluaranIuranWarga = 20000,
                pemanfaatanIuran = "No Cache Usage",
                avatar = "https://example.com/nocache.jpg",
                createdAt = Date(),
                updatedAt = Date(),
                isDeleted = false
            ),
            financialRecords = emptyList()
        )

        noCacheStrategy.put("test_key", testData, 30000)

        val retrievalCount = intArrayOf(0)
        val retrieved = noCacheStrategy.get("test_key") {
            retrievalCount[0]++
            testData
        }

        assertEquals("Cache getter should be called", 1, retrievalCount[0])
        assertNotNull("Should retrieve from source", retrieved)
        assertEquals("Data should match", "No", retrieved!!.user.firstName)
    }

    @Test
    fun `repositories handle large datasets efficiently`() {
        val largeUserList = (1..100).map { id ->
            """
                {
                    "id": $id,
                    "first_name": "User$id",
                    "last_name": "Test",
                    "email": "user$id@example.com",
                    "alamat": "Address $id",
                    "iuran_perwarga": 100000,
                    "total_iuran_individu": 50000,
                    "pengeluaran_iuran_warga": 20000,
                    "pemanfaatan_iuran": "Usage $id",
                    "avatar": "https://example.com/user$id.jpg",
                    "created_at": ${System.currentTimeMillis()},
                    "updated_at": ${System.currentTimeMillis()},
                    "is_deleted": false
                }
            """
        }.joinToString(",")

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""
                    {
                        "success": true,
                        "data": [$largeUserList]
                    }
                """)
                .addHeader("Content-Type", "application/json")
        )

        val startTime = System.currentTimeMillis()
        val result = userRepository.getUsers(forceRefresh = true)
        val endTime = System.currentTimeMillis()

        assertTrue("Result should be Success", result is OperationResult.Success)
        val userResponse = (result as OperationResult.Success).data
        assertEquals("Should have 100 users", 100, userResponse.data.size)

        val duration = endTime - startTime
        assertTrue("Large dataset should load within reasonable time", duration < 5000)
    }
}
