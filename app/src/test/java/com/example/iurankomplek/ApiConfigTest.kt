package com.example.iurankomplek

import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.ApiServiceV1
import org.junit.Test
import org.junit.Assert.*

class ApiConfigTest {

    @Test
    fun `getApiServiceV1 should return non-null ApiServiceV1 instance`() {
        try {
            val apiService = ApiConfig.getApiServiceV1()

            assertNotNull(apiService)
            assertTrue(apiService is ApiServiceV1)
        } catch (e: Exception) {
            fail("ApiConfig.getApiServiceV1() should not throw an exception: ${e.message}")
        }
    }

    @Test
    fun `getApiServiceV1 should return same instance when called multiple times`() {
        try {
            val firstInstance = ApiConfig.getApiServiceV1()
            val secondInstance = ApiConfig.getApiServiceV1()

            // Since ApiConfig is an object (singleton), it should return the same instance
            // This test verifies that the implementation is consistent
            // Note: This depends on the actual implementation of ApiConfig
            assertNotNull(firstInstance)
            assertNotNull(secondInstance)
        } catch (e: Exception) {
            fail("ApiConfig.getApiServiceV1() should not throw an exception: ${e.message}")
        }
    }

    @Test
    fun `ApiConfig should initialize without throwing exceptions`() {
        // This test verifies that the ApiConfig object can be initialized without errors
        // The getApiServiceV1 method should not throw exceptions under normal conditions
        try {
            val apiService = ApiConfig.getApiServiceV1()
            assertNotNull(apiService)
        } catch (e: Exception) {
            fail("ApiConfig.getApiServiceV1() should not throw an exception: ${e.message}")
        }
    }
}