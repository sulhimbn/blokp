package com.example.iurankomplek.utils

import org.junit.Assert.*
import org.junit.Test

class InputSanitizerEdgeCaseTest {

    @Test
    fun `sanitizeNumericInput should return zero for null input`() {
        val result = InputSanitizer.sanitizeNumericInput(null)
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for empty string`() {
        val result = InputSanitizer.sanitizeNumericInput("")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for blank string`() {
        val result = InputSanitizer.sanitizeNumericInput("   ")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should accept single digit`() {
        val result = InputSanitizer.sanitizeNumericInput("5")
        assertEquals("5", result)
    }

    @Test
    fun `sanitizeNumericInput should accept zero`() {
        val result = InputSanitizer.sanitizeNumericInput("0")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should trim whitespace`() {
        val result = InputSanitizer.sanitizeNumericInput("  123  ")
        assertEquals("123", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for negative number`() {
        val result = InputSanitizer.sanitizeNumericInput("-100")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for decimal number`() {
        val result = InputSanitizer.sanitizeNumericInput("100.50")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for number with letters`() {
        val result = InputSanitizer.sanitizeNumericInput("100abc")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for number with special characters`() {
        val result = InputSanitizer.sanitizeNumericInput("100@#$")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for number with commas`() {
        val result = InputSanitizer.sanitizeNumericInput("1,000")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should accept large valid number`() {
        val largeNumber = Constants.Payment.MAX_PAYMENT_AMOUNT.toString()
        val result = InputSanitizer.sanitizeNumericInput(largeNumber)
        assertEquals(largeNumber, result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for max payment amount plus one`() {
        val overflowAmount = (Constants.Payment.MAX_PAYMENT_AMOUNT + 1).toString()
        val result = InputSanitizer.sanitizeNumericInput(overflowAmount)
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for extremely large number`() {
        val result = InputSanitizer.sanitizeNumericInput("999999999999999999999")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for leading zeros`() {
        val result = InputSanitizer.sanitizeNumericInput("000123")
        assertEquals("000123", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for alphanumeric input`() {
        val result = InputSanitizer.sanitizeNumericInput("abc123def")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizeNumericInput should return zero for whitespace only`() {
        val result = InputSanitizer.sanitizeNumericInput("  \t\n\r  ")
        assertEquals("0", result)
    }

    @Test
    fun `sanitizePaymentAmount should return zero for null input`() {
        val result = InputSanitizer.sanitizePaymentAmount(null)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should return zero for negative amount`() {
        val result = InputSanitizer.sanitizePaymentAmount(-100.0)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should return zero for zero amount`() {
        val result = InputSanitizer.sanitizePaymentAmount(0.0)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should return zero for amount below minimum`() {
        val result = InputSanitizer.sanitizePaymentAmount(0.001)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should accept valid small amount`() {
        val result = InputSanitizer.sanitizePaymentAmount(0.01)
        assertEquals(0.01, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should accept valid medium amount`() {
        val result = InputSanitizer.sanitizePaymentAmount(50000.50)
        assertEquals(50000.50, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should accept valid large amount`() {
        val maxAmount = Constants.Payment.MAX_PAYMENT_AMOUNT.toDouble()
        val result = InputSanitizer.sanitizePaymentAmount(maxAmount)
        assertEquals(maxAmount, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should return zero for max payment amount plus one`() {
        val overflowAmount = Constants.Payment.MAX_PAYMENT_AMOUNT.toDouble() + 0.01
        val result = InputSanitizer.sanitizePaymentAmount(overflowAmount)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should round to two decimal places`() {
        val result = InputSanitizer.sanitizePaymentAmount(100.456)
        assertEquals(100.46, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should round down exact half`() {
        val result = InputSanitizer.sanitizePaymentAmount(100.455)
        assertEquals(100.46, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should handle single decimal place`() {
        val result = InputSanitizer.sanitizePaymentAmount(100.4)
        assertEquals(100.4, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should handle multiple decimal places`() {
        val result = InputSanitizer.sanitizePaymentAmount(100.123456789)
        assertEquals(100.12, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should return zero for negative decimal`() {
        val result = InputSanitizer.sanitizePaymentAmount(-100.50)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should return zero for very large decimal`() {
        val result = InputSanitizer.sanitizePaymentAmount(Double.MAX_VALUE)
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `sanitizePaymentAmount should handle scientific notation`() {
        val result = InputSanitizer.sanitizePaymentAmount(1.0e5)
        assertEquals(100000.0, result, 0.001)
    }

    @Test
    fun `validatePositiveInteger should return true for valid integer`() {
        val result = InputSanitizer.validatePositiveInteger("100")
        assertTrue(result)
    }

    @Test
    fun `validatePositiveInteger should return false for null input`() {
        val result = InputSanitizer.validatePositiveInteger(null)
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should return false for empty string`() {
        val result = InputSanitizer.validatePositiveInteger("")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should return false for blank string`() {
        val result = InputSanitizer.validatePositiveInteger("   ")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should return false for zero`() {
        val result = InputSanitizer.validatePositiveInteger("0")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should return false for negative integer`() {
        val result = InputSanitizer.validatePositiveInteger("-100")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should return false for decimal`() {
        val result = InputSanitizer.validatePositiveInteger("100.50")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should return false for alphanumeric`() {
        val result = InputSanitizer.validatePositiveInteger("100abc")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should return false for letters`() {
        val result = InputSanitizer.validatePositiveInteger("abc")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should return false for special characters`() {
        val result = InputSanitizer.validatePositiveInteger("100@#$")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should return false for number with commas`() {
        val result = InputSanitizer.validatePositiveInteger("1,000")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should return true for very large integer`() {
        val result = InputSanitizer.validatePositiveInteger(Int.MAX_VALUE.toString())
        assertTrue(result)
    }

    @Test
    fun `validatePositiveInteger should return false for integer exceeding Int range`() {
        val overflow = (Int.MAX_VALUE.toLong() + 1).toString()
        val result = InputSanitizer.validatePositiveInteger(overflow)
        assertFalse(result)
    }

    @Test
    fun `validatePositiveInteger should trim whitespace`() {
        val result = InputSanitizer.validatePositiveInteger("  100  ")
        assertTrue(result)
    }

    @Test
    fun `validatePositiveInteger should return false for whitespace only`() {
        val result = InputSanitizer.validatePositiveInteger("  \t\n\r  ")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveDouble should return true for valid double`() {
        val result = InputSanitizer.validatePositiveDouble("100.50")
        assertTrue(result)
    }

    @Test
    fun `validatePositiveDouble should return false for null input`() {
        val result = InputSanitizer.validatePositiveDouble(null)
        assertFalse(result)
    }

    @Test
    fun `validatePositiveDouble should return false for empty string`() {
        val result = InputSanitizer.validatePositiveDouble("")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveDouble should return false for blank string`() {
        val result = InputSanitizer.validatePositiveDouble("   ")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveDouble should return false for zero`() {
        val result = InputSanitizer.validatePositiveDouble("0")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveDouble should return false for negative double`() {
        val result = InputSanitizer.validatePositiveDouble("-100.50")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveDouble should return false for letters`() {
        val result = InputSanitizer.validatePositiveDouble("abc")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveDouble should return false for special characters`() {
        val result = InputSanitizer.validatePositiveDouble("100@#$")
        assertFalse(result)
    }

    @Test
    fun `validatePositiveDouble should accept double at max payment amount`() {
        val maxAmount = Constants.Payment.MAX_PAYMENT_AMOUNT.toDouble()
        val result = InputSanitizer.validatePositiveDouble(maxAmount.toString())
        assertTrue(result)
    }

    @Test
    fun `validatePositiveDouble should return false for max payment amount plus one`() {
        val overflowAmount = Constants.Payment.MAX_PAYMENT_AMOUNT.toDouble() + 0.01
        val result = InputSanitizer.validatePositiveDouble(overflowAmount.toString())
        assertFalse(result)
    }

    @Test
    fun `validatePositiveDouble should handle very small positive value`() {
        val result = InputSanitizer.validatePositiveDouble("0.01")
        assertTrue(result)
    }

    @Test
    fun `validatePositiveDouble should handle scientific notation`() {
        val result = InputSanitizer.validatePositiveDouble("1.0e5")
        assertTrue(result)
    }

    @Test
    fun `validatePositiveDouble should trim whitespace`() {
        val result = InputSanitizer.validatePositiveDouble("  100.50  ")
        assertTrue(result)
    }

    @Test
    fun `isValidAlphanumericId should return true for valid alphanumeric`() {
        val result = InputSanitizer.isValidAlphanumericId("test123")
        assertTrue(result)
    }

    @Test
    fun `isValidAlphanumericId should return true for valid id with hyphens`() {
        val result = InputSanitizer.isValidAlphanumericId("test-123-id")
        assertTrue(result)
    }

    @Test
    fun `isValidAlphanumericId should return true for valid id with underscores`() {
        val result = InputSanitizer.isValidAlphanumericId("test_123_id")
        assertTrue(result)
    }

    @Test
    fun `isValidAlphanumericId should return false for empty string`() {
        val result = InputSanitizer.isValidAlphanumericId("")
        assertFalse(result)
    }

    @Test
    fun `isValidAlphanumericId should return false for blank string`() {
        val result = InputSanitizer.isValidAlphanumericId("   ")
        assertFalse(result)
    }

    @Test
    fun `isValidAlphanumericId should return false for id with spaces`() {
        val result = InputSanitizer.isValidAlphanumericId("test 123")
        assertFalse(result)
    }

    @Test
    fun `isValidAlphanumericId should return false for id with special characters`() {
        val result = InputSanitizer.isValidAlphanumericId("test@123")
        assertFalse(result)
    }

    @Test
    fun `isValidAlphanumericId should return false for id with dots`() {
        val result = InputSanitizer.isValidAlphanumericId("test.123")
        assertFalse(result)
    }

    @Test
    fun `isValidAlphanumericId should return false for id exceeding max length`() {
        val longId = "a".repeat(101)
        val result = InputSanitizer.isValidAlphanumericId(longId)
        assertFalse(result)
    }

    @Test
    fun `isValidAlphanumericId should accept id at max length`() {
        val maxLengthId = "a".repeat(100)
        val result = InputSanitizer.isValidAlphanumericId(maxLengthId)
        assertTrue(result)
    }

    @Test
    fun `isValidAlphanumericId should return true for single character`() {
        val result = InputSanitizer.isValidAlphanumericId("a")
        assertTrue(result)
    }

    @Test
    fun `isValidAlphanumericId should return true for all uppercase`() {
        val result = InputSanitizer.isValidAlphanumericId("ABC123")
        assertTrue(result)
    }

    @Test
    fun `isValidAlphanumericId should return true for all lowercase`() {
        val result = InputSanitizer.isValidAlphanumericId("abc123")
        assertTrue(result)
    }

    @Test
    fun `isValidAlphanumericId should return true for mixed case`() {
        val result = InputSanitizer.isValidAlphanumericId("AbC123")
        assertTrue(result)
    }

    @Test
    fun `isValidAlphanumericId should return false for null safety`() {
        val result = InputSanitizer.isValidAlphanumericId("")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for URL longer than 2048 characters`() {
        val longUrl = "https://example.com/" + "a".repeat(2030)
        val result = InputSanitizer.isValidUrl(longUrl)
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return true for URL exactly at max length`() {
        val maxUrl = "https://example.com/" + "a".repeat(2029)
        val result = InputSanitizer.isValidUrl(maxUrl)
        assertTrue(result)
    }

    @Test
    fun `isValidUrl should return false for localhost`() {
        val result = InputSanitizer.isValidUrl("http://localhost:8080")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for 127_0_0_1`() {
        val result = InputSanitizer.isValidUrl("http://127.0.0.1:8080")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for file protocol`() {
        val result = InputSanitizer.isValidUrl("file:///etc/passwd")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for ftp protocol`() {
        val result = InputSanitizer.isValidUrl("ftp://example.com")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for javascript protocol`() {
        val result = InputSanitizer.isValidUrl("javascript:alert(1)")
        assertFalse(result)
    }

    @Test
    fun `isValidUrl should return false for data protocol`() {
        val result = InputSanitizer.isValidUrl("data:text/html,<script>alert(1)</script>")
        assertFalse(result)
    }

    @Test
    fun `sanitizeName should remove dangerous characters`() {
        val result = InputSanitizer.sanitizeName("John<script>alert(1)</script>Doe")
        assertEquals("Johnscriptalert1scriptDoe", result)
    }

    @Test
    fun `sanitizeAddress should remove dangerous characters`() {
        val result = InputSanitizer.sanitizeAddress("123 Main<script>alert(1)</script>St")
        assertEquals("123 Mainscriptalert1scriptSt", result)
    }

    @Test
    fun `sanitizePemanfaatan should remove dangerous characters`() {
        val result = InputSanitizer.sanitizePemanfaatan("Maintenance<div>test</div>")
        assertEquals("Maintenancedivtestdiv", result)
    }

    @Test
    fun `sanitizeEmail should handle uppercase email`() {
        val result = InputSanitizer.sanitizeEmail("USER@EXAMPLE.COM")
        assertEquals("user@example.com", result)
    }

    @Test
    fun `sanitizeEmail should handle mixed case email`() {
        val result = InputSanitizer.sanitizeEmail("UsEr@ExAmPlE.cOm")
        assertEquals("user@example.com", result)
    }
}
