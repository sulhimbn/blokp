package com.example.iurankomplek.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import com.example.iurankomplek.utils.OperationResult

class FallbackManagerTest {

    @Test
    fun `executeWithFallback should return primary operation success`() = runTest {
        val fallbackManager = FallbackManager<String>(
            fallbackStrategy = StaticDataFallback("fallback_value"),
            config = FallbackConfig()
        )
        
        val result = fallbackManager.executeWithFallback(
            primaryOperation = { OperationResult.Success("primary_value") }
        )
        
        assertTrue(result is OperationResult.Success)
        assertEquals("primary_value", (result as OperationResult.Success).value)
    }

    @Test
    fun `executeWithFallback should use fallback when primary fails`() = runTest {
        val fallbackManager = FallbackManager<String>(
            fallbackStrategy = StaticDataFallback("fallback_value"),
            config = FallbackConfig()
        )
        
        val result = fallbackManager.executeWithFallback(
            primaryOperation = { 
                OperationResult.Error(Exception("Primary failed"), "Error") 
            }
        )
        
        assertTrue(result is OperationResult.Success)
        assertEquals("fallback_value", (result as OperationResult.Success).value)
    }

    @Test
    fun `executeWithFallback should fail when fallback disabled`() = runTest {
        val fallbackManager = FallbackManager<String>(
            fallbackStrategy = StaticDataFallback("fallback_value"),
            config = FallbackConfig(enableFallback = false)
        )
        
        val result = fallbackManager.executeWithFallback(
            primaryOperation = { 
                OperationResult.Error(Exception("Primary failed"), "Error") 
            }
        )
        
        assertTrue(result is OperationResult.Error)
    }

    @Test
    fun `executeWithFallback should fail when no fallback provided`() = runTest {
        val fallbackManager = FallbackManager<String>(
            fallbackStrategy = null,
            config = FallbackConfig()
        )
        
        val result = fallbackManager.executeWithFallback(
            primaryOperation = { 
                OperationResult.Error(Exception("Primary failed"), "Error") 
            }
        )
        
        assertTrue(result is OperationResult.Error)
    }

    @Test
    fun `CompositeFallbackStrategy should use first available fallback`() = runTest {
        val fallback1 = StaticDataFallback("fallback_1")
        val fallback2 = StaticDataFallback("fallback_2")
        
        val composite = CompositeFallbackStrategy(
            strategies = listOf(fallback1, fallback2)
        )
        
        val result = composite.getFallback()
        
        assertEquals("fallback_1", result)
    }

    @Test
    fun `CompositeFallbackStrategy should use second fallback when first null`() = runTest {
        val fallback1 = object : FallbackStrategy<String> {
            override suspend fun getFallback() = null
            override val isEnabled: Boolean = true
            override val priority: Int = 1
        }
        val fallback2 = StaticDataFallback("fallback_2")
        
        val composite = CompositeFallbackStrategy(
            strategies = listOf(fallback1, fallback2)
        )
        
        val result = composite.getFallback()
        
        assertEquals("fallback_2", result)
    }

    @Test
    fun `CompositeFallbackStrategy should respect priority order`() = runTest {
        val fallback1 = object : FallbackStrategy<String> {
            override suspend fun getFallback() = "high_priority"
            override val isEnabled: Boolean = true
            override val priority: Int = 1
        }
        val fallback2 = object : FallbackStrategy<String> {
            override suspend fun getFallback() = "low_priority"
            override val isEnabled: Boolean = true
            override val priority: Int = 10
        }
        
        val composite = CompositeFallbackStrategy(
            strategies = listOf(fallback2, fallback1)
        )
        
        val result = composite.getFallback()
        
        assertEquals("high_priority", result)
    }

    @Test
    fun `StaticDataFallback should always return configured value`() = runTest {
        val fallback = StaticDataFallback("static_value")
        
        val result = fallback.getFallback()
        
        assertEquals("static_value", result)
    }

    @Test
    fun `EmptyDataFallback should return empty list`() = runTest {
        val fallback = object : EmptyDataFallback<List<String>>() {
            override val emptyValue = emptyList<String>()
        }
        
        val result = fallback.getFallback()
        
        assertTrue(result.isEmpty())
    }

    @Test
    fun `CachedDataFallback should return cached data`() = runTest {
        val cachedData = listOf("item1", "item2")
        val fallback = object : CachedDataFallback<List<String>>() {
            override suspend fun getCachedData() = cachedData
        }
        
        val result = fallback.getFallback()
        
        assertEquals(cachedData, result)
    }

    @Test
    fun `executeWithFallback should handle exception in primary operation`() = runTest {
        val fallbackManager = FallbackManager<String>(
            fallbackStrategy = StaticDataFallback("fallback_value"),
            config = FallbackConfig()
        )
        
        val result = fallbackManager.executeWithFallback(
            primaryOperation = { throw RuntimeException("Primary exception") }
        )
        
        assertTrue(result is OperationResult.Success)
        assertEquals("fallback_value", (result as OperationResult.Success).value)
    }

    @Test
    fun `executeWithFallback should handle exception in fallback`() = runTest {
        val fallbackManager = FallbackManager<String>(
            fallbackStrategy = object : FallbackStrategy<String> {
                override suspend fun getFallback() = throw RuntimeException("Fallback exception")
                override val isEnabled: Boolean = true
                override val priority: Int = 1
            },
            config = FallbackConfig()
        )
        
        val result = fallbackManager.executeWithFallback(
            primaryOperation = { throw RuntimeException("Primary exception") }
        )
        
        assertTrue(result is OperationResult.Error)
    }

    @Test
    fun `executeWithFallback should respect fallback timeout`() = runTest {
        val fallbackManager = FallbackManager<String>(
            fallbackStrategy = object : FallbackStrategy<String> {
                override suspend fun getFallback(): String {
                    kotlinx.coroutines.delay(10000)
                    return "late_value"
                }
                override val isEnabled: Boolean = true
                override val priority: Int = 1
            },
            config = FallbackConfig(fallbackTimeoutMs = 100)
        )
        
        val result = fallbackManager.executeWithFallback(
            primaryOperation = { 
                OperationResult.Error(Exception("Primary failed"), "Error") 
            }
        )
        
        assertTrue(result is OperationResult.Error)
    }

    @Test
    fun `executeWithFallback with custom fallback operation should use provided fallback`() = runTest {
        val fallbackManager = FallbackManager<String>(
            fallbackStrategy = StaticDataFallback("default_fallback"),
            config = FallbackConfig()
        )
        
        val customFallbackValue = "custom_fallback"
        val result = fallbackManager.executeWithFallback(
            primaryOperation = { 
                OperationResult.Error(Exception("Primary failed"), "Error") 
            },
            fallbackOperation = { customFallbackValue }
        )
        
        assertTrue(result is OperationResult.Success)
        assertEquals(customFallbackValue, (result as OperationResult.Success).value)
    }
}
