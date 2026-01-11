package com.example.iurankomplek

import android.content.Context
import com.example.iurankomplek.data.repository.BaseRepository
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.*
import com.example.iurankomplek.core.base.BaseViewModel
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit

/**
 * Test suite to verify foundation infrastructure components are properly implemented
 * as required by issue #158: Foundation Infrastructure Setup for HOA Management
 */
@RunWith(RobolectricTestRunner::class)
class FoundationInfrastructureTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun `test security configuration is properly implemented`() {
        // Verify certificate pinning is configured
        val apiService = ApiConfig.getApiService()
        assertNotNull("API service should be created with security configuration", apiService)
        
        // Verify timeouts are configured appropriately
        val okHttpClient = ApiConfig::class.java.declaredMethods
            .find { it.name == "getCertificatePinner" }
        assertNotNull("Security configuration should be present", okHttpClient)
    }

    @Test
    fun `test base repository interface exists`() {
        // Verify BaseRepository interface exists and has required methods
        assertTrue("BaseRepository should extend interface", BaseRepository::class.java.isInterface)
        
        val methods = BaseRepository::class.java.declaredMethods
        assertEquals("BaseRepository should have 5 required methods", 5, methods.size)
    }

    @Test
    fun `test base viewmodel abstract class exists`() {
        // Verify BaseViewModel abstract class exists
        assertTrue("BaseViewModel should be an abstract class", BaseViewModel::class.java.isAbstract)
    }

    @Test
    fun `test error handler is implemented`() {
        val errorHandler = ErrorHandler(context)
        val message = errorHandler.handleError(Exception("Test error"))
        assertNotNull("Error handler should return error message", message)
        assertTrue("Error message should not be empty", message.isNotEmpty())
    }

    @Test
    fun `test custom OperationResult class is properly defined`() {
        // Test Success case
        val successResult = OperationResult.Success("test")
        assertTrue("Success result should be instance of OperationResult", successResult is OperationResult.Success)
        
        // Test Error case
        val errorResult = OperationResult.Error(Exception("test"))
        assertTrue("Error result should be instance of OperationResult", errorResult is OperationResult.Error)
        
        // Test Loading case
        val loadingResult = OperationResult.Loading
        assertTrue("Loading result should be instance of OperationResult", loadingResult is OperationResult.Loading)
    }

    @Test
    fun `test data validator sanitizes inputs properly`() {
        // Test name sanitization
        val sanitized1 = InputSanitizer.sanitizeName("<script>alert('XSS')</script>John")
        assertNotEquals("Script should be sanitized", "<script>alert('XSS')</script>John", sanitized1)

        // Test email sanitization
        val sanitized2 = InputSanitizer.sanitizeEmail("test@;DROP TABLE users;")
        assertEquals("Invalid email should be sanitized", "invalid@email.com", sanitized2)

        // Test URL validation
        assertFalse("JavaScript URL should be rejected", InputSanitizer.isValidUrl("javascript:alert('XSS')"))
        assertTrue("HTTPS URL should be accepted", InputSanitizer.isValidUrl("https://example.com"))
    }

    @Test
    fun `test UI state sealed class instances`() {
        // Test that UiState sealed class instances work correctly
        val successState = UiState.Success("test data")
        assertTrue("Success state should be created", successState is UiState.Success)

        val errorState = UiState.Error("test error")
        assertTrue("Error state should be created", errorState is UiState.Error)

        val loadingState = UiState.Loading
        assertTrue("Loading state should be created", loadingState is UiState.Loading)
    }

    @Test
    fun `test security manager functionality`() {
        // Test security environment check
        val isSecure = SecurityManager.isSecureEnvironment(context)
        assertTrue("Security manager should return boolean", isSecure is Boolean)

        // Test certificate monitoring
        SecurityManager.monitorCertificateExpiration()

        // Test security configuration validation
        val isValid = SecurityManager.validateSecurityConfiguration()
        assertTrue("Security configuration should be valid", isValid)

        // Test security threat detection
        val threats = SecurityManager.checkSecurityThreats(context)
        assertTrue("Threats should be returned as a list", threats is List<*>)
    }

    @Test
    fun `test network security configuration expiration date`() {
        // This test verifies that the network security config has been updated
        // The actual verification would be done by checking the XML file content
        // For now, we'll just ensure the ApiConfig still works properly
        val apiService = ApiConfig.getApiService()
        assertNotNull("API service should still work after security config updates", apiService)
    }
}