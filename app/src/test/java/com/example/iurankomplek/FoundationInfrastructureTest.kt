package com.example.iurankomplek

import com.example.iurankomplek.data.repository.BaseRepository
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.utils.ErrorHandler
import com.example.iurankomplek.utils.LoggingUtils
import com.example.iurankomplek.utils.Result
import com.example.iurankomplek.utils.SecurityManager
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.BaseViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

class FoundationInfrastructureTest {

    @Test
    fun `ApiConfig builds a service without throwing`() {
        assertNotNull(ApiConfig.getApiService())
    }

    @Test
    fun `BaseRepository is an interface exposing the CRUD contract`() {
        assertTrue(BaseRepository::class.java.isInterface)

        // suspend functions get name-mangled signatures in the JVM (getById-<hash>)
        val methods = BaseRepository::class.java.declaredMethods.map { it.name }.toSet()
        assertEquals(6, methods.size)
        assertEquals(
            setOf("getAll", "getById", "create", "update", "delete", "observeAll"),
            methods.map { it.substringBefore('-') }.toSet()
        )
    }

    @Test
    fun `BaseViewModel is abstract`() {
        assertTrue(Modifier.isAbstract(BaseViewModel::class.java.modifiers))
    }

    @Test
    fun `ErrorHandler returns a non-empty message`() {
        val message = ErrorHandler().handleError(Exception("Test error"))

        assertNotNull(message)
        assertTrue(message.isNotEmpty())
    }

    @Test
    fun `the custom Result type covers every state`() {
        assertTrue(Result.Success("test") is Result.Success)
        assertTrue(Result.Error(Exception("test"), "error message") is Result.Error)
        assertTrue(Result.Loading is Result.Loading)
        assertTrue(Result.Empty is Result.Empty)
    }

    @Test
    fun `DataValidator strips script content from names`() {
        assertNotEquals(
            "<script>alert('XSS')</script>John",
            DataValidator.sanitizeName("<script>alert('XSS')</script>John")
        )
    }

    @Test
    fun `DataValidator rejects malformed emails`() {
        assertEquals("invalid@email.com", DataValidator.sanitizeEmail("test@;DROP TABLE users;"))
    }

    @Test
    fun `DataValidator rejects javascript urls but accepts https`() {
        assertFalse(DataValidator.isValidUrl("javascript:alert('XSS')"))
        assertTrue(DataValidator.isValidUrl("https://example.com"))
    }

    @Test
    fun `logging helpers are callable`() {
        LoggingUtils.d("Test debug message", "FoundationTest")
        LoggingUtils.i("Test info message", "FoundationTest")
        LoggingUtils.w("Test warning message", "FoundationTest")
        LoggingUtils.e("Test error message", null, "FoundationTest")
        LoggingUtils.logNetworkSecurityWarning("Test security warning")

        assertTrue(true)
    }

    @Test
    fun `UiState companion factories build the right variants`() {
        assertTrue(UiState.success("test data") is UiState.Success<*>)
        assertTrue(UiState.error<String>("test error") is UiState.Error)
        assertTrue(UiState.loading<String>() is UiState.Loading)
    }

    @Test
    fun `SecurityManager exposes its checks`() {
        assertTrue(SecurityManager.isSecureEnvironment())
        SecurityManager.monitorCertificateExpiration()
        assertTrue(SecurityManager.validateSecurityConfiguration())
        assertTrue(SecurityManager.checkSecurityThreats() is List<*>)
    }
}