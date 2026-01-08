package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.repository.cache.CacheStrategy
import com.example.iurankomplek.data.repository.cache.InMemoryCacheStrategy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import retrofit2.Response

/**
 * Test coverage for BaseRepositoryV2 unified repository pattern.
 * Tests cache strategies, circuit breaker, and error handling.
 */
class BaseRepositoryV2Test {

    private lateinit var cacheStrategy: CacheStrategy<List<String>>
    private lateinit var testRepository: TestRepository
    private var networkCallCount = 0
    private var shouldSucceed = true

    @Before
    fun setup() {
        cacheStrategy = InMemoryCacheStrategy()
        networkCallCount = 0
        shouldSucceed = true
    }

    @Test
    fun `fetchWithCache should return cached data when valid`() = runTest {
        val cachedData = listOf("cached_item")
        cacheStrategy.put("test_key", cachedData)

        val result = testRepository.fetchData(
            cacheKey = "test_key",
            forceRefresh = false,
            fromNetwork = { createNetworkResponse(listOf("network_item")) }
        )

        assertTrue(result.isSuccess, "Result should be success")
        assertEquals(cachedData, result.getOrNull(), "Should return cached data")
        assertEquals(0, networkCallCount, "Should not make network call when cache is valid")
    }

    @Test
    fun `fetchWithCache should fetch from network when forceRefresh`() = runTest {
        cacheStrategy.put("test_key", listOf("cached_item"))

        val result = testRepository.fetchData(
            cacheKey = "test_key",
            forceRefresh = true,
            fromNetwork = { createNetworkResponse(listOf("network_item")) }
        )

        assertTrue(result.isSuccess, "Result should be success")
        assertEquals(listOf("network_item"), result.getOrNull(), "Should return network data")
        assertEquals(1, networkCallCount, "Should make network call when forceRefresh is true")
    }

    @Test
    fun `fetchWithCache should fetch from network when cache is empty`() = runTest {
        val result = testRepository.fetchData(
            cacheKey = "test_key",
            forceRefresh = false,
            fromNetwork = { createNetworkResponse(listOf("network_item")) }
        )

        assertTrue(result.isSuccess, "Result should be success")
        assertEquals(listOf("network_item"), result.getOrNull(), "Should return network data")
        assertEquals(1, networkCallCount, "Should make network call when cache is empty")
    }

    @Test
    fun `fetchWithCache should update cache on network success`() = runTest {
        cacheStrategy.put("test_key", listOf("old_cached_item"))

        testRepository.fetchData(
            cacheKey = "test_key",
            forceRefresh = true,
            fromNetwork = { createNetworkResponse(listOf("new_network_item")) }
        )

        val cachedData = cacheStrategy.get("test_key")
        assertEquals(listOf("new_network_item"), cachedData, "Should update cache with network data")
    }

    @Test
    fun `fetchWithCache should not update cache on network failure`() = runTest {
        val oldCache = listOf("old_cached_item")
        cacheStrategy.put("test_key", oldCache)

        shouldSucceed = false
        val result = testRepository.fetchData(
            cacheKey = "test_key",
            forceRefresh = true,
            fromNetwork = { createNetworkResponse(listOf("new_network_item")) }
        )

        assertTrue(result.isFailure, "Result should be failure")
        val cachedData = cacheStrategy.get("test_key")
        assertEquals(oldCache, cachedData, "Should not update cache on network failure")
    }

    @Test
    fun `fetchWithCache should work without cache key`() = runTest {
        val result = testRepository.fetchData(
            cacheKey = null,
            forceRefresh = false,
            fromNetwork = { createNetworkResponse(listOf("network_item")) }
        )

        assertTrue(result.isSuccess, "Result should be success")
        assertEquals(listOf("network_item"), result.getOrNull(), "Should return network data")
        assertEquals(1, networkCallCount, "Should make network call when no cache key")
    }

    @Test
    fun `clearCache should call cache strategy clear`() = runTest {
        cacheStrategy.put("key1", listOf("value1"))
        cacheStrategy.put("key2", listOf("value2"))

        testRepository.clearCache()

        assertNull(cacheStrategy.get("key1"), "Should clear key1")
        assertNull(cacheStrategy.get("key2"), "Should clear key2")
    }

    @Test
    fun `should handle network exceptions`() = runTest {
        shouldSucceed = false
        val exception = Exception("Network error")

        val result = testRepository.fetchData(
            cacheKey = "test_key",
            forceRefresh = true,
            fromNetwork = { throw exception }
        )

        assertTrue(result.isFailure, "Result should be failure")
        assertEquals(exception, result.exceptionOrNull(), "Should return network exception")
    }

    @Test
    fun `should handle null cache data`() = runTest {
        val result = testRepository.fetchData(
            cacheKey = "test_key",
            forceRefresh = false,
            fromCache = { null },
            fromNetwork = { createNetworkResponse(listOf("network_item")) }
        )

        assertTrue(result.isSuccess, "Result should be success")
        assertEquals(1, networkCallCount, "Should make network call when cache returns null")
    }

    private suspend fun createNetworkResponse(data: List<String>): Response<List<String>> {
        networkCallCount++
        return if (shouldSucceed) {
            Response.success(data)
        } else {
            Response.error(500, okhttp3.ResponseBody.create(null, "Error"))
        }
    }

    private inner class TestRepository : BaseRepositoryV2<List<String>>() {
        override val cacheStrategy: CacheStrategy<List<String>> = this@BaseRepositoryV2Test.cacheStrategy

        suspend fun fetchData(
            cacheKey: String? = null,
            forceRefresh: Boolean = false,
            fromNetwork: suspend () -> retrofit2.Response<List<String>>,
            fromCache: (suspend () -> List<String>?)? = null
        ): Result<List<String>> {
            return fetchWithCache(cacheKey, forceRefresh, fromNetwork, fromCache)
        }
    }
}
