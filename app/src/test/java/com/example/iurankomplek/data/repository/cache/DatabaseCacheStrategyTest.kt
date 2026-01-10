package com.example.iurankomplek.data.repository.cache

import com.example.iurankomplek.data.cache.CacheManager
import com.example.iurankomplek.data.dao.UserDao
import com.example.iurankomplek.data.dao.FinancialRecordDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.Date

class DatabaseCacheStrategyTest {

    @Mock
    private lateinit var mockCacheManager: CacheManager

    @Mock
    private lateinit var mockUserDao: UserDao

    @Mock
    private lateinit var mockFinancialRecordDao: FinancialRecordDao

    private lateinit var databaseCacheStrategy: DatabaseCacheStrategy<String>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(mockCacheManager.getUserDao()).thenReturn(mockUserDao)
        whenever(mockCacheManager.getFinancialRecordDao()).thenReturn(mockFinancialRecordDao)
        
        val mockCacheGetter = suspend { "cached_data" }
        databaseCacheStrategy = DatabaseCacheStrategy(mockCacheGetter)
    }

    @Test
    fun `get should return cached value successfully`() = runTest {
        val key = "test_key"
        val expectedValue = "cached_value"
        
        val mockCacheGetter = suspend { expectedValue }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val result = cache.get(key)
        
        assertEquals(expectedValue, result, "Should return cached value from cacheGetter")
    }

    @Test
    fun `get should return null when cacheGetter throws exception`() = runTest {
        val key = "test_key"
        
        val mockCacheGetter = suspend { 
            throw RuntimeException("Cache access error") 
        }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val result = cache.get(key)
        
        assertNull(result, "Should return null when cacheGetter throws exception")
    }

    @Test
    fun `get should return null when cacheGetter returns null`() = runTest {
        val key = "test_key"
        
        val mockCacheGetter = suspend { null }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val result = cache.get(key)
        
        assertNull(result, "Should return null when cacheGetter returns null")
    }

    @Test
    fun `get with null key should still call cacheGetter`() = runTest {
        var cacheGetterCalled = false
        
        val mockCacheGetter = suspend { 
            cacheGetterCalled = true
            "value"
        }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val result = cache.get(null)
        
        assertTrue(cacheGetterCalled, "Cache getter should be called even with null key")
        assertEquals("value", result)
    }

    @Test
    fun `put should execute without error (database persists separately)`() = runTest {
        val key = "test_key"
        val value = "test_value"
        
        val mockCacheGetter = suspend { value }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        cache.put(key, value)
        
        assertTrue(true, "put should execute without throwing exception")
    }

    @Test
    fun `isValid with forceRefresh true should return false`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val isValid = cache.isValid("cached_data", forceRefresh = true)
        
        assertFalse(isValid, "isValid should return false when forceRefresh is true")
    }

    @Test
    fun `isValid with null cached value should return false`() = runTest {
        val mockCacheGetter = suspend { null }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val isValid = cache.isValid(null, forceRefresh = false)
        
        assertFalse(isValid, "isValid should return false when cached value is null")
    }

    @Test
    fun `isValid with fresh cache should return true`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val freshTimestamp = Date(System.currentTimeMillis() - 60000)
        whenever(mockUserDao.getLatestUpdatedAt()).thenReturn(freshTimestamp)
        whenever(CacheManager.isCacheFresh(freshTimestamp.time)).thenReturn(true)
        
        val isValid = cache.isValid("cached_data", forceRefresh = false)
        
        assertTrue(isValid, "isValid should return true when cache is fresh")
        verify(mockUserDao).getLatestUpdatedAt()
        verify(CacheManager).isCacheFresh(freshTimestamp.time)
    }

    @Test
    fun `isValid with stale cache should return false`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val staleTimestamp = Date(System.currentTimeMillis() - 7200000)
        whenever(mockUserDao.getLatestUpdatedAt()).thenReturn(staleTimestamp)
        whenever(CacheManager.isCacheFresh(staleTimestamp.time)).thenReturn(false)
        
        val isValid = cache.isValid("cached_data", forceRefresh = false)
        
        assertFalse(isValid, "isValid should return false when cache is stale")
        verify(mockUserDao).getLatestUpdatedAt()
        verify(CacheManager).isCacheFresh(staleTimestamp.time)
    }

    @Test
    fun `isValid with null latest update timestamp should return false`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        whenever(mockUserDao.getLatestUpdatedAt()).thenReturn(null)
        
        val isValid = cache.isValid("cached_data", forceRefresh = false)
        
        assertFalse(isValid, "isValid should return false when latest update timestamp is null")
        verify(mockUserDao).getLatestUpdatedAt()
        verify(CacheManager, never()).isCacheFresh(any())
    }

    @Test
    fun `isValid should handle exception from getLatestUpdatedAt`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        whenever(mockUserDao.getLatestUpdatedAt()).thenThrow(
            RuntimeException("Database error")
        )
        
        val isValid = cache.isValid("cached_data", forceRefresh = false)
        
        assertFalse(isValid, "isValid should return false when getLatestUpdatedAt throws exception")
        verify(mockUserDao).getLatestUpdatedAt()
        verify(CacheManager, never()).isCacheFresh(any())
    }

    @Test
    fun `isValid should handle exception from isCacheFresh`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val timestamp = Date()
        whenever(mockUserDao.getLatestUpdatedAt()).thenReturn(timestamp)
        whenever(CacheManager.isCacheFresh(timestamp.time)).thenThrow(
            RuntimeException("Cache check error")
        )
        
        val isValid = cache.isValid("cached_data", forceRefresh = false)
        
        assertFalse(isValid, "isValid should return false when isCacheFresh throws exception")
        verify(mockUserDao).getLatestUpdatedAt()
        verify(CacheManager).isCacheFresh(timestamp.time)
    }

    @Test
    fun `clear should delete all users and financial records`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        cache.clear()
        
        verify(mockUserDao).deleteAll()
        verify(mockFinancialRecordDao).deleteAll()
    }

    @Test
    fun `clear should handle exception from deleteUserAll`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        whenever(mockUserDao.deleteAll()).thenThrow(RuntimeException("Delete error"))
        
        cache.clear()
        
        verify(mockUserDao).deleteAll()
        verify(mockFinancialRecordDao).deleteAll()
    }

    @Test
    fun `clear should handle exception from deleteFinancialRecordsAll`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        whenever(mockFinancialRecordDao.deleteAll()).thenThrow(
            RuntimeException("Delete error")
        )
        
        cache.clear()
        
        verify(mockUserDao).deleteAll()
        verify(mockFinancialRecordDao).deleteAll()
    }

    @Test
    fun `should handle very old cache timestamps`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val veryOldTimestamp = Date(0)
        whenever(mockUserDao.getLatestUpdatedAt()).thenReturn(veryOldTimestamp)
        whenever(CacheManager.isCacheFresh(veryOldTimestamp.time)).thenReturn(false)
        
        val isValid = cache.isValid("cached_data", forceRefresh = false)
        
        assertFalse(isValid, "Should handle very old cache timestamps")
    }

    @Test
    fun `should handle future cache timestamps`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val futureTimestamp = Date(System.currentTimeMillis() + 3600000)
        whenever(mockUserDao.getLatestUpdatedAt()).thenReturn(futureTimestamp)
        whenever(CacheManager.isCacheFresh(futureTimestamp.time)).thenReturn(true)
        
        val isValid = cache.isValid("cached_data", forceRefresh = false)
        
        assertTrue(isValid, "Should handle future cache timestamps")
    }

    @Test
    fun `should handle empty string cached value`() = runTest {
        val mockCacheGetter = suspend { "" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val freshTimestamp = Date()
        whenever(mockUserDao.getLatestUpdatedAt()).thenReturn(freshTimestamp)
        whenever(CacheManager.isCacheFresh(freshTimestamp.time)).thenReturn(true)
        
        val isValid = cache.isValid("", forceRefresh = false)
        
        assertTrue(isValid, "Should handle empty string cached value")
    }

    @Test
    fun `isValid should not call CacheManager when cached value is null`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val isValid = cache.isValid(null, forceRefresh = false)
        
        assertFalse(isValid, "Should return false for null cached value")
        verify(mockUserDao, never()).getLatestUpdatedAt()
        verify(CacheManager, never()).isCacheFresh(any())
    }

    @Test
    fun `should work with complex data types`() = runTest {
        data class ComplexData(val id: String, val name: String, val value: Int)
        
        val complexData = ComplexData("123", "test", 42)
        val mockCacheGetter = suspend { complexData }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val result = cache.get("complex_key")
        
        assertEquals(complexData, result, "Should handle complex data types")
    }

    @Test
    fun `should be thread-safe for concurrent get operations`() = runTest {
        val mockCacheGetter = suspend { "concurrent_value" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val results = (1..100).map {
            kotlinx.coroutines.async {
                cache.get("concurrent_key")
            }
        }.awaitAll()
        
        results.forEach { result ->
            assertEquals("concurrent_value", result, "Concurrent gets should return same value")
        }
    }

    @Test
    fun `should be thread-safe for concurrent isValid operations`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val freshTimestamp = Date()
        whenever(mockUserDao.getLatestUpdatedAt()).thenReturn(freshTimestamp)
        whenever(CacheManager.isCacheFresh(freshTimestamp.time)).thenReturn(true)
        
        val results = (1..50).map {
            kotlinx.coroutines.async {
                cache.isValid("cached_data", forceRefresh = false)
            }
        }.awaitAll()
        
        results.forEach { result ->
            assertTrue(result, "Concurrent isValid should return true")
        }
    }

    @Test
    fun `should be thread-safe for concurrent clear operations`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val results = (1..10).map {
            kotlinx.coroutines.async {
                cache.clear()
            }
        }.awaitAll()
        
        verify(mockUserDao, times(10)).deleteAll()
        verify(mockFinancialRecordDao, times(10)).deleteAll()
    }

    @Test
    fun `isValid with cached value should check freshness only when not forceRefresh`() = runTest {
        val mockCacheGetter = suspend { "cached_data" }
        val cache = DatabaseCacheStrategy(mockCacheGetter)
        
        val freshTimestamp = Date()
        whenever(mockUserDao.getLatestUpdatedAt()).thenReturn(freshTimestamp)
        whenever(CacheManager.isCacheFresh(freshTimestamp.time)).thenReturn(true)
        
        cache.isValid("cached_data", forceRefresh = true)
        
        verify(mockUserDao, never()).getLatestUpdatedAt()
        verify(CacheManager, never()).isCacheFresh(any())
    }
}
