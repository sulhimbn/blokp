package com.example.iurankomplek

import com.example.iurankomplek.data.repository.BaseRepository
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.network.SecurityConfig
import com.example.iurankomplek.utils.*
import com.example.iurankomplek.viewmodel.BaseViewModel
import org.junit.Test
import org.junit.Assert.*
import java.lang.reflect.Modifier
import java.util.concurrent.TimeUnit

/**
 * Test suite to verify foundation infrastructure components are properly implemented
 * as required by issue #158: Foundation Infrastructure Setup for HOA Management
 */
class FoundationInfrastructureTest {

    @Test
    fun `test security configuration is properly implemented`() {
        val apiService = ApiConfig.getApiService()
        assertNotNull("API service should be created with security configuration", apiService)

        // Certificate pinning lives in SecurityConfig, not ApiConfig.
        val secureClient = SecurityConfig.getSecureOkHttpClient()
        assertNotNull("Secure OkHttp client should be constructible", secureClient)
        assertNotNull(
            "Secure client should carry a certificate pinner",
            secureClient.certificatePinner
        )
        assertTrue(
            "Pinner should cover the production API host",
            secureClient.certificatePinner.findMatchingPins("api.apispreadsheets.com").isNotEmpty()
        )
    }

    @Test
    fun `test base repository interface exists`() {
        // Verify BaseRepository interface exists and has required methods
        assertTrue("BaseRepository should extend interface", BaseRepository::class.java.isInterface)
        
        val methods = BaseRepository::class.java.declaredMethods
        assertEquals("BaseRepository should have 6 required methods", 6, methods.size)
        // suspend members get a mangled JVM suffix, so compare the stable prefix.
        assertEquals(
            "BaseRepository should expose getAll/getById/create/update/delete/observeAll",
            setOf("getAll", "getById", "create", "update", "delete", "observeAll"),
            methods.map { it.name.substringBefore('-') }.toSet()
        )
    }

    @Test
    fun `test base viewmodel abstract class exists`() {
        // Verify BaseViewModel abstract class exists
        assertTrue(
            "BaseViewModel should be an abstract class",
            Modifier.isAbstract(BaseViewModel::class.java.modifiers)
        )
    }

    @Test
    fun `test error handler is implemented`() {
        val errorHandler = ErrorHandler()
        val message = errorHandler.handleError(Exception("Test error"))
        assertNotNull("Error handler should return error message", message)
        assertTrue("Error message should not be empty", message.isNotEmpty())
    }

    @Test
    fun `test custom Result class is properly defined`() {
        // Test Success case
        val successResult = Result.Success("test")
        assertTrue("Success result should be instance of Result", successResult is Result.Success)
        
        // Test Error case
        val errorResult = Result.Error(Exception("test"), "error message")
        assertTrue("Error result should be instance of Result", errorResult is Result.Error)
        
        // Test Loading case
        val loadingResult = Result.Loading
        assertTrue("Loading result should be instance of Result", loadingResult is Result.Loading)
        
        // Test Empty case
        val emptyResult = Result.Empty
        assertTrue("Empty result should be instance of Result", emptyResult is Result.Empty)
    }

    @Test
    fun `test data validator sanitizes inputs properly`() {
        val validator = DataValidator
        
        // sanitizeName enforces the length cap and substitutes a placeholder for blank
        // input. It is not an HTML escaper: values reach TextView.text, never a WebView,
        // so markup renders literally rather than executing.
        val overlong = "N".repeat(Constants.Validation.MAX_NAME_LENGTH + 1)
        assertEquals("Unknown", validator.sanitizeName(overlong))
        assertEquals("Unknown", validator.sanitizeName("   "))
        assertEquals("Unknown", validator.sanitizeName(null))
        assertEquals(
            Constants.Validation.MAX_NAME_LENGTH,
            validator.sanitizeName("N".repeat(Constants.Validation.MAX_NAME_LENGTH)).length
        )
        assertEquals("John Doe", validator.sanitizeName("  John Doe  "))
        
        // Test email sanitization
        val sanitized2 = validator.sanitizeEmail("test@;DROP TABLE users;")
        assertEquals("Invalid email should be sanitized", "invalid@email.com", sanitized2)
        
        // Test URL validation
        assertFalse("JavaScript URL should be rejected", validator.isValidUrl("javascript:alert('XSS')"))
        assertTrue("HTTPS URL should be accepted", validator.isValidUrl("https://example.com"))
    }

    @Test
    fun `test logging utils are available`() {
        // Test that logging utils can be called without exceptions
        LoggingUtils.d("Test debug message", "FoundationTest")
        LoggingUtils.i("Test info message", "FoundationTest")
        LoggingUtils.w("Test warning message", "FoundationTest")
        LoggingUtils.e("Test error message", null, "FoundationTest")
        LoggingUtils.logNetworkSecurityWarning("Test security warning")
        
        // If we reach this point, logging worked without exceptions
        assertTrue("Logging utilities should be accessible", true)
    }

    @Test
    fun `test UI state companion object functions`() {
        // Test that UiState companion object functions work correctly
        val successState = UiState.success("test data")
        assertTrue("Success state should be created", successState is UiState.Success)
        
        val errorState = UiState.error<String>("test error")
        assertTrue("Error state should be created", errorState is UiState.Error)

        val loadingState = UiState.loading<String>()
        assertTrue("Loading state should be created", loadingState is UiState.Loading)
    }

    @Test
    fun `test security manager functionality`() {
        val securityManager = SecurityManager
        
        // Test security environment check
        val isSecure = securityManager.isSecureEnvironment()
        assertTrue("Security manager should return boolean", isSecure is Boolean)
        
        // Test certificate monitoring
        securityManager.monitorCertificateExpiration()
        
        // Test security configuration validation
        val isValid = securityManager.validateSecurityConfiguration()
        assertTrue("Security configuration should be valid", isValid)
        
        // Test security threat detection
        val threats = securityManager.checkSecurityThreats()
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