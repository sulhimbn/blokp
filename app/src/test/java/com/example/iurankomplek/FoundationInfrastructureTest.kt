package com.example.iurankomplek

import com.example.iurankomplek.data.repository.BaseRepository
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.utils.ErrorHandler
import com.example.iurankomplek.utils.LoggingUtils
import com.example.iurankomplek.utils.SecurityManager
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.BaseViewModel
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.Result as AppResult
import com.example.iurankomplek.utils.onFailure
import com.example.iurankomplek.utils.onSuccess
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

/**
 * Guards the shapes other layers depend on: the repository contract, the
 * ViewModel base class, the custom Result type and the UI state helpers.
 */
class FoundationInfrastructureTest {

    @Test
    fun `BaseRepository is an interface exposing the six CRUD operations`() {
        assertTrue(BaseRepository::class.java.isInterface)

        val names = BaseRepository::class.java.declaredMethods
            // Kotlin suspend functions get a name-mangled JVM twin (getById-<hash>).
            .map { it.name.substringBefore('-') }
            .toSet()

        assertEquals(
            setOf("getAll", "getById", "create", "update", "delete", "observeAll"),
            names
        )
    }

    @Test
    fun `BaseViewModel is declared abstract`() {
        assertTrue(
            "BaseViewModel must stay abstract so subclasses supply the hooks",
            Modifier.isAbstract(BaseViewModel::class.java.modifiers)
        )
    }

    @Test
    fun `ErrorHandler maps an unknown throwable to a non empty message`() {
        val message = ErrorHandler().handleError(Exception("Test error"))

        assertTrue(message.isNotEmpty())
        assertEquals("An error occurred: Test error", message)
    }

    @Test
    fun `the custom Result type covers success, error, loading and empty`() {
        assertTrue(AppResult.Success("payload") is AppResult.Success)
        assertTrue(AppResult.Error(IllegalStateException("boom"), "error message") is AppResult.Error)
        assertTrue(AppResult.Loading is AppResult.Loading)
        assertTrue(AppResult.Empty is AppResult.Empty)
    }

    @Test
    fun `Result onSuccess runs only for a success`() = run {
        var ran = 0

        AppResult.Success("payload").onSuccess { ran++ }
        AppResult.Error(IllegalStateException("boom"), "boom").onSuccess { ran++ }
        AppResult.Loading.onSuccess { ran++ }

        assertEquals(1, ran)
    }

    @Test
    fun `Result onFailure runs only for an error and exposes the throwable`() = run {
        val boom = IllegalStateException("boom")
        var seen: Throwable? = null

        AppResult.Success("payload").onFailure { seen = it }
        AppResult.Error(boom, "boom").onFailure { seen = it }
        AppResult.Empty.onFailure { seen = it }

        assertEquals(boom, seen)
    }

    @Test
    fun `UiState factories build the matching variant`() {
        val payload = UserResponse(emptyList())

        assertEquals(payload, (UiState.success(payload) as UiState.Success).data)
        assertEquals("boom", (UiState.error<Unit>("boom") as UiState.Error).error)
        assertTrue(UiState.loading<Unit>() is UiState.Loading)
    }

    @Test
    fun `sanitizeName trims and falls back to Unknown for blank input`() {
        assertEquals("John Doe", DataValidator.sanitizeName("  John Doe  "))
        assertEquals("Unknown", DataValidator.sanitizeName("   "))
        assertEquals("Unknown", DataValidator.sanitizeName(null))
    }

    @Test
    fun `sanitizeName rejects a value past the configured length cap`() {
        val tooLong = "a".repeat(Constants.Validation.MAX_NAME_LENGTH + 1)

        assertEquals("Unknown", DataValidator.sanitizeName(tooLong))
        assertEquals("a".repeat(Constants.Validation.MAX_NAME_LENGTH),
            DataValidator.sanitizeName("a".repeat(Constants.Validation.MAX_NAME_LENGTH)))
    }

    @Test
    fun `sanitizeAddress and sanitizePemanfaatan fall back for blank input`() {
        assertEquals("Address not available", DataValidator.sanitizeAddress("  "))
        assertEquals("Unknown expense", DataValidator.sanitizePemanfaatan(null))
    }

    @Test
    fun `formatCurrency renders thousands separators and never a negative amount`() {
        assertEquals("Rp.1,500,000", DataValidator.formatCurrency(1_500_000))
        assertEquals("Rp.0", DataValidator.formatCurrency(-5))
        assertEquals("Rp.0", DataValidator.formatCurrency(null))
    }

    @Test
    fun `DataValidator rejects an injected email`() {
        assertEquals(
            "invalid@email.com",
            DataValidator.sanitizeEmail("test@;DROP TABLE users;")
        )
    }

    @Test
    fun `DataValidator accepts https and rejects javascript urls`() {
        assertFalse(DataValidator.isValidUrl("javascript:alert('XSS')"))
        assertTrue(DataValidator.isValidUrl("https://example.com"))
    }

    @Test
    fun `logging helpers are callable without throwing`() {
        LoggingUtils.d("Test debug message", "FoundationTest")
        LoggingUtils.i("Test info message", "FoundationTest")
        LoggingUtils.w("Test warning message", "FoundationTest")
        LoggingUtils.e("Test error message", null, "FoundationTest")
        LoggingUtils.logNetworkSecurityWarning("Test security warning")
    }

    @Test
    fun `security manager reports a boolean environment verdict`() {
        assertEquals(true, SecurityManager.isSecureEnvironment() is Boolean)
        assertEquals(true, SecurityManager.validateSecurityConfiguration() is Boolean)
        assertTrue(SecurityManager.checkSecurityThreats() is List<*>)
    }
}
