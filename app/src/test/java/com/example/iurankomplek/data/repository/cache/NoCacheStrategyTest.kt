package com.example.iurankomplek.data.repository.cache

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Test coverage for NoCacheStrategy.
 * Tests that it never caches and always forces network fetch.
 */
class NoCacheStrategyTest {

    @Test
    fun `get should always return null`() = runTest {
        val cache = NoCacheStrategy<String>()

        val result1 = cache.get("any_key")
        val result2 = cache.get(null)

        assertNull(result1, "NoCacheStrategy.get should always return null")
        assertNull(result2, "NoCacheStrategy.get(null) should return null")
    }

    @Test
    fun `put should do nothing`() = runTest {
        val cache = NoCacheStrategy<String>()

        cache.put("key", "value")

        val result = cache.get("key")
        assertNull(result, "put should have no effect in NoCacheStrategy")
    }

    @Test
    fun `isValid should always return false`() = runTest {
        val cache = NoCacheStrategy<String>()

        val isValid1 = cache.isValid("any_value", forceRefresh = false)
        val isValid2 = cache.isValid(null, forceRefresh = false)
        val isValid3 = cache.isValid("any_value", forceRefresh = true)

        assertFalse(isValid1, "isValid should always return false")
        assertFalse(isValid2, "isValid(null) should return false")
        assertFalse(isValid3, "isValid with forceRefresh should return false")
    }

    @Test
    fun `clear should do nothing`() = runTest {
        val cache = NoCacheStrategy<String>()

        cache.clear()

        val result = cache.get("any_key")
        assertNull(result, "clear should have no effect in NoCacheStrategy")
    }

    @Test
    fun `should ignore all operations`() = runTest {
        val cache = NoCacheStrategy<String>()

        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.clear()

        val result1 = cache.get("key1")
        val result2 = cache.get("key2")

        assertNull(result1, "Should ignore all put operations")
        assertNull(result2, "Should ignore clear operation")
    }
}
