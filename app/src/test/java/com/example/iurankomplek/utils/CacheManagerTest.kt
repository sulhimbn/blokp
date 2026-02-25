package com.example.iurankomplek.utils

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.lang.reflect.Field

/**
 * Unit tests for CacheManager class.
 * Tests cover: put/get operations, TTL expiry, remove, clear, contains, size, evictExpired
 */
class CacheManagerTest {

    @Before
    fun setUp() {
        // Reset singleton instance before each test to ensure test isolation
        resetCacheManagerInstance()
    }

    private fun resetCacheManagerInstance() {
        try {
            val instanceField = CacheManager::class.java.getDeclaredField("instance")
            instanceField.isAccessible = true
            instanceField.set(null, null)
        } catch (e: Exception) {
            // Ignore - singleton may not be initialized yet
        }
    }

    @Test
    fun testPutAndGetSync_basicOperations() {
        val cacheManager = CacheManager.getInstance()
        
        // Put a value
        cacheManager.put("key1", "value1")
        
        // Get should return the value
        val result = cacheManager.getSync<String>("key1")
        assertEquals("value1", result)
    }

    @Test
    fun testPutAndGetSync_withCustomTTL() {
        val cacheManager = CacheManager.getInstance()
        
        // Put with very short TTL (1ms - already expired)
        cacheManager.put("key1", "value1", 1L)
        
        // Small delay to ensure expiry
        Thread.sleep(10)
        
        // Get should return null because entry is expired
        val result = cacheManager.getSync<String>("key1")
        assertNull(result)
    }

    @Test
    fun testPutAndGetSync_storesDifferentTypes() {
        val cacheManager = CacheManager.getInstance()
        
        // Test with Integer
        cacheManager.put("intKey", 42)
        assertEquals(42, cacheManager.getSync<Int>("intKey"))
        
        // Test with custom data class
        val testData = TestData("test", 123)
        cacheManager.put("objectKey", testData)
        val retrieved = cacheManager.getSync<TestData>("objectKey")
        assertNotNull(retrieved)
        assertEquals("test", retrieved?.name)
        assertEquals(123, retrieved?.value)
    }

    @Test
    fun testGetSync_cacheMiss() {
        val cacheManager = CacheManager.getInstance()
        
        // Get non-existent key should return null
        val result = cacheManager.getSync<String>("nonexistent")
        assertNull(result)
    }

    @Test
    fun testContains_existingKey() {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1")
        
        assertTrue(cacheManager.contains("key1"))
    }

    @Test
    fun testContains_expiredKey() {
        val cacheManager = CacheManager.getInstance()
        
        // Put with immediate expiry
        cacheManager.put("key1", "value1", 1L)
        
        // Wait for expiry
        Thread.sleep(10)
        
        // Contains should return false for expired entry
        assertFalse(cacheManager.contains("key1"))
    }

    @Test
    fun testContains_nonExistingKey() {
        val cacheManager = CacheManager.getInstance()
        
        assertFalse(cacheManager.contains("nonexistent"))
    }

    @Test
    fun testSize_emptyCache() {
        val cacheManager = CacheManager.getInstance()
        
        assertEquals(0, cacheManager.size())
    }

    @Test
    fun testSize_multipleEntries() {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1")
        cacheManager.put("key2", "value2")
        cacheManager.put("key3", "value3")
        
        assertEquals(3, cacheManager.size())
    }

    @Test
    fun testSize_afterRemove() {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1")
        cacheManager.put("key2", "value2")
        
        runBlocking {
            cacheManager.remove("key1")
        }
        
        assertEquals(1, cacheManager.size())
    }

    @Test
    fun testRemove_existingKey() {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1")
        
        runBlocking {
            cacheManager.remove("key1")
        }
        
        assertFalse(cacheManager.contains("key1"))
        assertNull(cacheManager.getSync<String>("key1"))
    }

    @Test
    fun testRemove_nonExistingKey() {
        val cacheManager = CacheManager.getInstance()
        
        // Should not throw when removing non-existent key
        runBlocking {
            cacheManager.remove("nonexistent")
        }
        
        assertEquals(0, cacheManager.size())
    }

    @Test
    fun testClearSync() {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1")
        cacheManager.put("key2", "value2")
        cacheManager.put("key3", "value3")
        
        cacheManager.clearSync()
        
        assertEquals(0, cacheManager.size())
        assertFalse(cacheManager.contains("key1"))
    }

    @Test
    fun testClear_suspend() {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1")
        cacheManager.put("key2", "value2")
        
        runBlocking {
            cacheManager.clear()
        }
        
        assertEquals(0, cacheManager.size())
    }

    @Test
    fun testEvictExpired_multipleEntries() {
        val cacheManager = CacheManager.getInstance()
        
        // Add entries with different TTLs
        cacheManager.put("key1", "value1", 1L)    // Expires quickly
        cacheManager.put("key2", "value2", 10000L) // Expires later
        cacheManager.put("key3", "value3", 10000L) // Expires later
        
        // Wait for key1 to expire
        Thread.sleep(10)
        
        runBlocking {
            cacheManager.evictExpired()
        }
        
        // key1 should be evicted, key2 and key3 should remain
        assertFalse(cacheManager.contains("key1"))
        assertTrue(cacheManager.contains("key2"))
        assertTrue(cacheManager.contains("key3"))
        assertEquals(2, cacheManager.size())
    }

    @Test
    fun testEvictExpired_noExpiredEntries() {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1")
        cacheManager.put("key2", "value2")
        
        runBlocking {
            cacheManager.evictExpired()
        }
        
        // All entries should still exist
        assertEquals(2, cacheManager.size())
    }

    @Test
    fun testDefaultTTL() {
        // Verify default TTL is 5 minutes (300000ms)
        assertEquals(5 * 60 * 1000L, CacheManager.DEFAULT_TTL_MS)
    }

    @Test
    fun testGet_suspend() = runBlocking {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1")
        
        val result = cacheManager.get<String>("key1")
        assertEquals("value1", result)
    }

    @Test
    fun testGet_suspend_expired() = runBlocking {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1", 1L)
        
        // Wait for expiry
        Thread.sleep(10)
        
        val result = cacheManager.get<String>("key1")
        assertNull(result)
    }

    @Test
    fun testRemove_suspend() = runBlocking {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1")
        
        cacheManager.remove("key1")
        
        assertFalse(cacheManager.contains("key1"))
    }

    @Test
    fun testConcurrentPutOperations() {
        val cacheManager = CacheManager.getInstance()
        
        // Put multiple values rapidly
        for (i in 1..100) {
            cacheManager.put("key$i", "value$i")
        }
        
        assertEquals(100, cacheManager.size())
        
        // Verify some values
        assertEquals("value1", cacheManager.getSync<String>("key1"))
        assertEquals("value50", cacheManager.getSync<String>("key50"))
        assertEquals("value100", cacheManager.getSync<String>("key100"))
    }

    @Test
    fun testUpdateExistingKey() {
        val cacheManager = CacheManager.getInstance()
        
        cacheManager.put("key1", "value1")
        assertEquals("value1", cacheManager.getSync<String>("key1"))
        
        // Update with new value
        cacheManager.put("key1", "value2")
        assertEquals("value2", cacheManager.getSync<String>("key1"))
        
        // Size should remain 1
        assertEquals(1, cacheManager.size())
    }

    // Helper data class for testing object storage
    private data class TestData(
        val name: String,
        val value: Int
    )
}
