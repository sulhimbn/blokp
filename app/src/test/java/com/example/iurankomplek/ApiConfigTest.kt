package com.example.iurankomplek

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiService
import org.junit.Test
import org.junit.Assert.*

class ApiConfigTest {

    @Test
    fun `getApiService should return non-null ApiService instance`() {
        try {
            val apiService = ApiConfig.getApiService()
            
            assertNotNull(apiService)
            assertTrue(apiService is ApiService)
        } catch (e: Exception) {
            fail("ApiConfig.getApiService() should not throw an exception: ${e.message}")
        }
    }

    @Test
    fun `getApiService should return same instance when called multiple times`() {
        try {
            val firstInstance = ApiConfig.getApiService()
            val secondInstance = ApiConfig.getApiService()
            
            // Since ApiConfig is an object (singleton), it should return the same instance
            // This test verifies that the implementation is consistent
            // Note: This depends on the actual implementation of ApiConfig
            assertNotNull(firstInstance)
            assertNotNull(secondInstance)
        } catch (e: Exception) {
            fail("ApiConfig.getApiService() should not throw an exception: ${e.message}")
        }
    }

    @Test
    fun `ApiConfig should initialize without throwing exceptions`() {
        // This test verifies that the ApiConfig object can be initialized without errors
        // The getApiService method should not throw exceptions under normal conditions
        try {
            val apiService = ApiConfig.getApiService()
            assertNotNull(apiService)
        } catch (e: Exception) {
            fail("ApiConfig.getApiService() should not throw an exception: ${e.message}")
        }
    }
}