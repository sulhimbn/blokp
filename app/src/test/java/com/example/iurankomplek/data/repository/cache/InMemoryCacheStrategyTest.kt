package com.example.iurankomplek.data.repository.cache

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

/**
 * Comprehensive test coverage for InMemoryCacheStrategy.
 * Tests thread safety, cache operations, and edge cases.
 */
class InMemoryCacheStrategyTest {

    @Test
    fun `put and get should store and retrieve data`() = runTest {
        val cache = InMemoryCacheStrategy<String>()
        val key = "test_key"
        val value = "test_value"

        cache.put(key, value)
        val retrieved = cache.get(key)

        assertEquals(value, retrieved, "Retrieved value should match stored value")
    }

    @Test
    fun `get with null key should return null`() = runTest {
        val cache = InMemoryCacheStrategy<String>()

        val result = cache.get(null)

        assertNull(result, "get(null) should return null")
    }

    @Test
    fun `get with non-existent key should return null`() = runTest {
        val cache = InMemoryCacheStrategy<String>()

        val result = cache.get("non_existent_key")

        assertNull(result, "get(non_existent_key) should return null")
    }

    @Test
    fun `put should overwrite existing value`() = runTest {
        val cache = InMemoryCacheStrategy<String>()
        val key = "test_key"

        cache.put(key, "value1")
        cache.put(key, "value2")

        val retrieved = cache.get(key)

        assertEquals("value2", retrieved, "Second put should overwrite first value")
    }

    @Test
    fun `isValid with cached data should return true`() = runTest {
        val cache = InMemoryCacheStrategy<String>()
        val cachedValue = "cached_data"

        cache.put("key", cachedValue)

        val isValid = cache.isValid(cachedValue, forceRefresh = false)

        assertTrue(isValid, "isValid should return true for cached data when forceRefresh is false")
    }

    @Test
    fun `isValid with forceRefresh should return false`() = runTest {
        val cache = InMemoryCacheStrategy<String>()
        val cachedValue = "cached_data"

        cache.put("key", cachedValue)

        val isValid = cache.isValid(cachedValue, forceRefresh = true)

        assertFalse(isValid, "isValid should return false when forceRefresh is true")
    }

    @Test
    fun `isValid with null cached value should return false`() = runTest {
        val cache = InMemoryCacheStrategy<String>()

        val isValid = cache.isValid(null, forceRefresh = false)

        assertFalse(isValid, "isValid should return false when cached value is null")
    }

    @Test
    fun `clear should remove all cached data`() = runTest {
        val cache = InMemoryCacheStrategy<String>()

        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3")

        cache.clear()

        assertNull(cache.get("key1"), "key1 should be null after clear")
        assertNull(cache.get("key2"), "key2 should be null after clear")
        assertNull(cache.get("key3"), "key3 should be null after clear")
    }

    @Test
    fun `should handle multiple types`() = runTest {
        val stringCache = InMemoryCacheStrategy<String>()
        val intCache = InMemoryCacheStrategy<Int>()
        val listCache = InMemoryCacheStrategy<List<String>>()

        stringCache.put("key", "string_value")
        intCache.put("key", 42)
        listCache.put("key", listOf("item1", "item2"))

        assertEquals("string_value", stringCache.get("key"))
        assertEquals(42, intCache.get("key"))
        assertEquals(listOf("item1", "item2"), listCache.get("key"))
    }

    @Test
    fun `should handle special characters in keys`() = runTest {
        val cache = InMemoryCacheStrategy<String>()
        val specialKeys = listOf(
            "key_with_underscore",
            "key-with-dash",
            "key.with.dot",
            "key with space",
            "key@with#special\$chars"
        )

        specialKeys.forEach { key ->
            cache.put(key, "value_$key")
            val value = cache.get(key)
            assertEquals("value_$key", value, "Should handle key: $key")
        }
    }

    @Test
    fun `should handle empty values`() = runTest {
        val cache = InMemoryCacheStrategy<String>()

        cache.put("empty_key", "")

        val value = cache.get("empty_key")
        assertEquals("", value, "Should handle empty string value")
    }

    @Test
    fun `should be thread-safe for concurrent puts`() = runTest {
        val cache = InMemoryCacheStrategy<Int>()
        val iterations = 1000

        val results = (1..iterations).map { i ->
            kotlinx.coroutines.async {
                cache.put("key_$i", i)
                kotlinx.coroutines.delay(1)
                cache.get("key_$i")
            }
        }.awaitAll()

        results.forEachIndexed { index, result ->
            assertEquals(index + 1, result, "Concurrent put/get should be consistent at index $index")
        }
    }

    @Test
    fun `should be thread-safe for concurrent gets`() = runTest {
        val cache = InMemoryCacheStrategy<String>()
        cache.put("shared_key", "shared_value")

        val results = (1..100).map {
            kotlinx.coroutines.async {
                cache.get("shared_key")
            }
        }.awaitAll()

        results.forEach { result ->
            assertEquals("shared_value", result, "Concurrent gets should return same value")
        }
    }
}
