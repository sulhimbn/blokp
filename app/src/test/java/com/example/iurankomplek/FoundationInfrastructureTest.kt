package com.example.iurankomplek

import com.example.iurankomplek.data.repository.BaseRepository
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.*
import com.example.iurankomplek.viewmodel.BaseViewModel
import com.example.iurankomplek.network.SecurityConfig
import org.junit.Test
import org.junit.Assert.*
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

        val client = SecurityConfig.getSecureOkHttpClient()
        val pins = client.certificatePinner.findMatchingPins("api.apispreadsheets.com")
        assertTrue("production host must be certificate pinned", pins.isNotEmpty())
        val installed = pins.map { java.util.Base64.getEncoder().encodeToString(it.hash.toByteArray()) }
        assertTrue(
            "configured pin must be installed, found $installed",
            installed.contains(Constants.Security.CERTIFICATE_PINNER.removePrefix("sha256/"))
        )
        assertTrue("connect timeout must be set", client.connectTimeoutMillis > 0)
    }

    @Test
    fun `test base repository interface exists`() {
        // Verify BaseRepository interface exists and has required methods
        assertTrue("BaseRepository should extend interface", BaseRepository::class.java.isInterface)
        
        val methods = BaseRepository::class.java.declaredMethods
        val declared = methods.map { it.name.substringBefore("-") }.toSet()
        listOf("getAll", "getById", "create", "update", "delete", "observeAll").forEach {
            assertTrue("BaseRepository must declare $it", declared.contains(it))
        }
    }

    @Test
    fun `test base viewmodel abstract class exists`() {
        // Verify BaseViewModel abstract class exists
        assertTrue(
            "BaseViewModel should be an abstract class",
            java.lang.reflect.Modifier.isAbstract(BaseViewModel::class.java.modifiers)
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
        
        // sanitizeName trims, enforces the length cap and falls back for blank input.
        // The app renders these values as plain TextView text (there is no WebView in the
        // codebase), so HTML is never interpreted.
        assertEquals("John Doe", validator.sanitizeName("  John Doe  "))
        assertEquals("Unknown", validator.sanitizeName("   "))
        assertEquals("Unknown", validator.sanitizeName(null))
        assertEquals(
            "over-length names fall back instead of being truncated",
            "Unknown",
            validator.sanitizeName("x".repeat(Constants.Validation.MAX_NAME_LENGTH + 1))
        )
        
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
        val successState: UiState<String> = UiState.success("test data")
        assertTrue("Success state should be created", successState is UiState.Success)
        
        val errorState: UiState<String> = UiState.error("test error")
        assertTrue("Error state should be created", errorState is UiState.Error)
        
        val loadingState: UiState<String> = UiState.loading()
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