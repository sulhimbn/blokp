package com.example.iurankomplek.utils

import org.junit.Assert.*
import org.junit.Test

class DataValidatorTest {

    @Test
    fun `sanitizeName should return valid name as-is`() {
        val result = DataValidator.sanitizeName("John Doe")
        assertEquals("John Doe", result)
    }

    @Test
    fun `sanitizeName should trim whitespace`() {
        val result = DataValidator.sanitizeName("  John Doe  ")
        assertEquals("John Doe", result)
    }

    @Test
    fun `sanitizeName should return Unknown for null input`() {
        val result = DataValidator.sanitizeName(null)
        assertEquals("Unknown", result)
    }

    @Test
    fun `sanitizeName should return Unknown for empty string`() {
        val result = DataValidator.sanitizeName("")
        assertEquals("Unknown", result)
    }

    @Test
    fun `sanitizeName should return Unknown for blank string`() {
        val result = DataValidator.sanitizeName("   ")
        assertEquals("Unknown", result)
    }

    @Test
    fun `sanitizeName should return Unknown for string exceeding max length`() {
        val longName = "A".repeat(Constants.Validation.MAX_NAME_LENGTH + 1)
        val result = DataValidator.sanitizeName(longName)
        assertEquals("Unknown", result)
    }

    @Test
    fun `sanitizeName should accept name at max length`() {
        val maxLengthName = "A".repeat(Constants.Validation.MAX_NAME_LENGTH)
        val result = DataValidator.sanitizeName(maxLengthName)
        assertEquals(maxLengthName, result)
    }

    @Test
    fun `sanitizeEmail should return valid email as-is`() {
        val result = DataValidator.sanitizeEmail("test@example.com")
        assertEquals("test@example.com", result)
    }

    @Test
    fun `sanitizeEmail should return invalid@email for null input`() {
        val result = DataValidator.sanitizeEmail(null)
        assertEquals("invalid@email.com", result)
    }

    @Test
    fun `sanitizeEmail should return invalid@email for empty string`() {
        val result = DataValidator.sanitizeEmail("")
        assertEquals("invalid@email.com", result)
    }

    @Test
    fun `sanitizeEmail should return invalid@email for invalid format`() {
        val result = DataValidator.sanitizeEmail("invalid-email")
        assertEquals("invalid@email.com", result)
    }

    @Test
    fun `sanitizeEmail should return invalid@email for missing domain`() {
        val result = DataValidator.sanitizeEmail("test@")
        assertEquals("invalid@email.com", result)
    }

    @Test
    fun `sanitizeEmail should return invalid@email for missing local part`() {
        val result = DataValidator.sanitizeEmail("@example.com")
        assertEquals("invalid@email.com", result)
    }

    @Test
    fun `sanitizeEmail should accept email with special characters`() {
        val result = DataValidator.sanitizeEmail("test.user+tag@example.co.uk")
        assertEquals("test.user+tag@example.co.uk", result)
    }

    @Test
    fun `sanitizeEmail should return invalid@email for string exceeding max length`() {
        val longEmail = "a@b" + "c".repeat(Constants.Validation.MAX_EMAIL_LENGTH)
        val result = DataValidator.sanitizeEmail(longEmail)
        assertEquals("invalid@email.com", result)
    }

    @Test
    fun `sanitizeAddress should return valid address as-is`() {
        val result = DataValidator.sanitizeAddress("123 Main St, City")
        assertEquals("123 Main St, City", result)
    }

    @Test
    fun `sanitizeAddress should trim whitespace`() {
        val result = DataValidator.sanitizeAddress("  123 Main St  ")
        assertEquals("123 Main St", result)
    }

    @Test
    fun `sanitizeAddress should return default message for null input`() {
        val result = DataValidator.sanitizeAddress(null)
        assertEquals("Address not available", result)
    }

    @Test
    fun `sanitizeAddress should return default message for empty string`() {
        val result = DataValidator.sanitizeAddress("")
        assertEquals("Address not available", result)
    }

    @Test
    fun `sanitizeAddress should return default message for blank string`() {
        val result = DataValidator.sanitizeAddress("   ")
        assertEquals("Address not available", result)
    }

    @Test
    fun `sanitizeAddress should return default message for string exceeding max length`() {
        val longAddress = "A".repeat(Constants.Validation.MAX_ADDRESS_LENGTH + 1)
        val result = DataValidator.sanitizeAddress(longAddress)
        assertEquals("Address not available", result)
    }

    @Test
    fun `sanitizeAddress should accept address at max length`() {
        val maxLengthAddress = "A".repeat(Constants.Validation.MAX_ADDRESS_LENGTH)
        val result = DataValidator.sanitizeAddress(maxLengthAddress)
        assertEquals(maxLengthAddress, result)
    }

    @Test
    fun `sanitizePemanfaatan should return valid pemanfaatan as-is`() {
        val result = DataValidator.sanitizePemanfaatan("Maintenance")
        assertEquals("Maintenance", result)
    }

    @Test
    fun `sanitizePemanfaatan should trim whitespace`() {
        val result = DataValidator.sanitizePemanfaatan("  Maintenance  ")
        assertEquals("Maintenance", result)
    }

    @Test
    fun `sanitizePemanfaatan should return default for null input`() {
        val result = DataValidator.sanitizePemanfaatan(null)
        assertEquals("Unknown expense", result)
    }

    @Test
    fun `sanitizePemanfaatan should return default for empty string`() {
        val result = DataValidator.sanitizePemanfaatan("")
        assertEquals("Unknown expense", result)
    }

    @Test
    fun `sanitizePemanfaatan should return default for blank string`() {
        val result = DataValidator.sanitizePemanfaatan("   ")
        assertEquals("Unknown expense", result)
    }

    @Test
    fun `sanitizePemanfaatan should return default for string exceeding max length`() {
        val longPemanfaatan = "A".repeat(Constants.Validation.MAX_PEMANFAATAN_LENGTH + 1)
        val result = DataValidator.sanitizePemanfaatan(longPemanfaatan)
        assertEquals("Unknown expense", result)
    }

    @Test
    fun `sanitizePemanfaatan should accept pemanfaatan at max length`() {
        val maxLengthPemanfaatan = "A".repeat(Constants.Validation.MAX_PEMANFAATAN_LENGTH)
        val result = DataValidator.sanitizePemanfaatan(maxLengthPemanfaatan)
        assertEquals(maxLengthPemanfaatan, result)
    }

    @Test
    fun `formatCurrency should format valid amount correctly`() {
        val result = DataValidator.formatCurrency(1500000)
        assertEquals("Rp.1,500,000", result)
    }

    @Test
    fun `formatCurrency should format zero correctly`() {
        val result = DataValidator.formatCurrency(0)
        assertEquals("Rp.0", result)
    }

    @Test
    fun `formatCurrency should handle large numbers`() {
        val result = DataValidator.formatCurrency(1000000000)
        assertEquals("Rp.1,000,000,000", result)
    }

    @Test
    fun `formatCurrency should return default for null input`() {
        val result = DataValidator.formatCurrency(null)
        assertEquals("Rp.0", result)
    }

    @Test
    fun `formatCurrency should return default for negative numbers`() {
        val result = DataValidator.formatCurrency(-100)
        assertEquals("Rp.0", result)
    }

    @Test
    fun `isValidUrl should return true for valid http URL`() {
        val result = DataValidator.isValidUrl("http://example.com")
        assertTrue(result)
    }

    @Test
    fun `isValidUrl should return true for valid https URL`() {
        val result = DataValidator.isValidUrl("https://example.com")
        assertTrue(result)
    }

    @Test
    fun `isValidUrl should return false for ftp URL`() {
        val result = DataValidator.isValidUrl("ftp://example.com")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for file URL`() {
        val result = DataValidator.isValidUrl("file:///path/to/file")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for null input`() {
        val result = DataValidator.isValidUrl(null)
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for empty string`() {
        val result = DataValidator.isValidUrl("")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for blank string`() {
        val result = DataValidator.isValidUrl("   ")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for invalid URL format`() {
        val result = DataValidator.isValidUrl("not-a-url")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return true for valid URL with path`() {
        val result = DataValidator.isValidUrl("https://example.com/path/to/resource")
        assertTrue(result)
    }

    @Test
    fun `isValidUrl should return true for valid URL with query parameters`() {
        val result = DataValidator.isValidUrl("https://example.com?param=value")
        assertTrue(result)
    }

    @Test
    fun `isValidUrl should return true for valid URL with fragment`() {
        val result = DataValidator.isValidUrl("https://example.com#section")
        assertTrue(result)
    }
}
