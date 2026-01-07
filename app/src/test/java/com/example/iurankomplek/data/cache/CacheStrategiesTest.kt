package com.example.iurankomplek.data.cache

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class CacheStrategiesTest {
    
    @Test
    fun cacheFirstStrategy_returnsCachedDataWhenFresh() = runTest {
        val cachedData = "cached_value"
        val networkData = "network_value"
        var cacheCalled = false
        var networkCalled = false
        
        val result = cacheFirstStrategy(
            getFromCache = {
                cacheCalled = true
                cachedData
            },
            getFromNetwork = {
                networkCalled = true
                networkData
            },
            isCacheFresh = { true },
            forceRefresh = false
        )
        
        assertTrue(cacheCalled)
        assertFalse(networkCalled)
        assertTrue(result.isSuccess)
        assertEquals(cachedData, result.getOrNull())
    }
    
    @Test
    fun cacheFirstStrategy_fetchesFromNetworkWhenCacheNotFresh() = runTest {
        val cachedData = "cached_value"
        val networkData = "network_value"
        var cacheCalled = false
        var networkCalled = false
        var saveCalled = false
        
        val result = cacheFirstStrategy(
            getFromCache = {
                cacheCalled = true
                cachedData
            },
            getFromNetwork = {
                networkCalled = true
                networkData
            },
            isCacheFresh = { false },
            saveToCache = {
                saveCalled = true
                assertEquals(networkData, it)
            },
            forceRefresh = false
        )
        
        assertTrue(cacheCalled)
        assertTrue(networkCalled)
        assertTrue(saveCalled)
        assertTrue(result.isSuccess)
        assertEquals(networkData, result.getOrNull())
    }
    
    @Test
    fun cacheFirstStrategy_forceRefreshBypassesCache() = runTest {
        val cachedData = "cached_value"
        val networkData = "network_value"
        var cacheCalled = false
        var networkCalled = false
        var saveCalled = false
        
        val result = cacheFirstStrategy(
            getFromCache = {
                cacheCalled = true
                cachedData
            },
            getFromNetwork = {
                networkCalled = true
                networkData
            },
            isCacheFresh = { true },
            saveToCache = {
                saveCalled = true
                assertEquals(networkData, it)
            },
            forceRefresh = true
        )
        
        assertTrue(cacheCalled)
        assertTrue(networkCalled)
        assertTrue(saveCalled)
        assertTrue(result.isSuccess)
        assertEquals(networkData, result.getOrNull())
    }
    
    @Test
    fun cacheFirstStrategy_fallsBackToCacheOnNetworkError() = runTest {
        val cachedData = "cached_value"
        var cacheCalledCount = 0
        var networkCalled = false
        
        val result = cacheFirstStrategy(
            getFromCache = {
                cacheCalledCount++
                if (cacheCalledCount == 1) cachedData else throw Exception("Cache error")
            },
            getFromNetwork = {
                networkCalled = true
                throw Exception("Network error")
            },
            isCacheFresh = { false },
            forceRefresh = false
        )
        
        assertEquals(2, cacheCalledCount)
        assertTrue(networkCalled)
        assertTrue(result.isSuccess)
        assertEquals(cachedData, result.getOrNull())
    }
    
    @Test
    fun cacheFirstStrategy_returnsFailureWhenBothCacheAndNetworkFail() = runTest {
        val result = cacheFirstStrategy<String>(
            getFromCache = {
                throw Exception("Cache error")
            },
            getFromNetwork = {
                throw Exception("Network error")
            },
            isCacheFresh = { false },
            forceRefresh = false
        )
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Network error"))
    }
    
    @Test
    fun cacheFirstStrategy_handlesNullCachedData() = runTest {
        val networkData = "network_value"
        var cacheCalled = false
        var networkCalled = false
        
        val result = cacheFirstStrategy(
            getFromCache = {
                cacheCalled = true
                null
            },
            getFromNetwork = {
                networkCalled = true
                networkData
            },
            isCacheFresh = { false },
            forceRefresh = false
        )
        
        assertTrue(cacheCalled)
        assertTrue(networkCalled)
        assertTrue(result.isSuccess)
        assertEquals(networkData, result.getOrNull())
    }
    
    @Test
    fun networkFirstStrategy_returnsNetworkData() = runTest {
        val networkData = "network_value"
        var networkCalled = false
        var saveCalled = false
        
        val result = networkFirstStrategy(
            getFromNetwork = {
                networkCalled = true
                networkData
            },
            saveToCache = {
                saveCalled = true
                assertEquals(networkData, it)
            }
        )
        
        assertTrue(networkCalled)
        assertTrue(saveCalled)
        assertTrue(result.isSuccess)
        assertEquals(networkData, result.getOrNull())
    }
    
    @Test
    fun networkFirstStrategy_fallsBackToCacheOnNetworkError() = runTest {
        val cachedData = "cached_value"
        var networkCalled = false
        var cacheCalled = false
        
        val result = networkFirstStrategy(
            getFromNetwork = {
                networkCalled = true
                throw Exception("Network error")
            },
            getFromCache = {
                cacheCalled = true
                cachedData
            }
        )
        
        assertTrue(networkCalled)
        assertTrue(cacheCalled)
        assertTrue(result.isSuccess)
        assertEquals(cachedData, result.getOrNull())
    }
    
    @Test
    fun networkFirstStrategy_returnsFailureWhenBothFail() = runTest {
        var networkCalled = false
        var cacheCalled = false
        
        val result = networkFirstStrategy<String>(
            getFromNetwork = {
                networkCalled = true
                throw Exception("Network error")
            },
            getFromCache = {
                cacheCalled = true
                throw Exception("Cache error")
            }
        )
        
        assertTrue(networkCalled)
        assertTrue(cacheCalled)
        assertTrue(result.isFailure)
    }
    
    @Test
    fun networkFirstStrategy_handlesNullCacheFallback() = runTest {
        val result = networkFirstStrategy<String>(
            getFromNetwork = {
                throw Exception("Network error")
            },
            getFromCache = {
                null
            }
        )
        
        assertTrue(result.isFailure)
    }
}
