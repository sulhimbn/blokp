package com.example.iurankomplek

import com.example.iurankomplek.utils.DataValidator
import org.junit.Test
import org.junit.Assert.*

class DataValidatorTest {

    @Test
    fun `sanitizeName should return valid name when input is valid`() {
        val input = "John Doe"
        val result = DataValidator.sanitizeName(input)
        assertEquals("John Doe", result)
    }

    @Test
    fun `sanitizeName should return Unknown when input is null`() {
        val result = DataValidator.sanitizeName(null)
        assertEquals("Unknown", result)
    }

    @Test
    fun `sanitizeName should return Unknown when input is blank`() {
        val result = DataValidator.sanitizeName("")
        assertEquals("Unknown", result)
        
        val result2 = DataValidator.sanitizeName("   ")
        assertEquals("Unknown", result2)
    }

    @Test
    fun `sanitizeName should return Unknown when input exceeds 50 characters`() {
        val longName = "a".repeat(51)
        val result = DataValidator.sanitizeName(longName)
        assertEquals("Unknown", result)
    }

    @Test
    fun `sanitizeName should trim whitespace`() {
        val input = "  John Doe  "
        val result = DataValidator.sanitizeName(input)
        assertEquals("John Doe", result)
    }

    @Test
    fun `sanitizeName should return Unknown when input contains harmful script tags`() {
        val input = "<script>alert('xss')</script>John"
        val result = DataValidator.sanitizeName(input)
        assertEquals("Unknown", result)
    }

    @Test
    fun `sanitizeEmail should return valid email when input is valid`() {
        val input = "user@example.com"
        val result = DataValidator.sanitizeEmail(input)
        assertEquals("user@example.com", result)
    }

    @Test
    fun `sanitizeEmail should return invalid@email.com when input is null`() {
        val result = DataValidator.sanitizeEmail(null)
        assertEquals("invalid@email.com", result)
    }

    @Test
    fun `sanitizeEmail should return invalid@email.com when input is not a valid email`() {
        val result = DataValidator.sanitizeEmail("not-an-email")
        assertEquals("invalid@email.com", result)
        
        val result2 = DataValidator.sanitizeEmail("user@")
        assertEquals("invalid@email.com", result2)
        
        val result3 = DataValidator.sanitizeEmail("@example.com")
        assertEquals("invalid@email.com", result3)
    }

    @Test
    fun `sanitizeEmail should return invalid@email.com when email exceeds 100 characters`() {
        val longEmail = "${"a".repeat(90)}@example.com"
        val result = DataValidator.sanitizeEmail(longEmail)
        assertEquals("invalid@email.com", result)
    }

    @Test
    fun `sanitizeAddress should return valid address when input is valid`() {
        val input = "123 Main St, City, Country"
        val result = DataValidator.sanitizeAddress(input)
        assertEquals("123 Main St, City, Country", result)
    }

    @Test
    fun `sanitizeAddress should return Address not available when input is null`() {
        val result = DataValidator.sanitizeAddress(null)
        assertEquals("Address not available", result)
    }

    @Test
    fun `sanitizeAddress should return Address not available when input is blank`() {
        val result = DataValidator.sanitizeAddress("")
        assertEquals("Address not available", result)
        
        val result2 = DataValidator.sanitizeAddress("   ")
        assertEquals("Address not available", result2)
    }

    @Test
    fun `sanitizeAddress should return Address not available when input exceeds 200 characters`() {
        val longAddress = "a".repeat(201)
        val result = DataValidator.sanitizeAddress(longAddress)
        assertEquals("Address not available", result)
    }

    @Test
    fun `sanitizeAddress should return Address not available when input contains harmful content`() {
        val input = "<script>alert('xss')</script>123 Main St"
        val result = DataValidator.sanitizeAddress(input)
        assertEquals("Address not available", result)
    }

    @Test
    fun `sanitizePemanfaatan should return valid pemanfaatan when input is valid`() {
        val input = "Utilities expense"
        val result = DataValidator.sanitizePemanfaatan(input)
        assertEquals("Utilities expense", result)
    }

    @Test
    fun `sanitizePemanfaatan should return Unknown expense when input is null`() {
        val result = DataValidator.sanitizePemanfaatan(null)
        assertEquals("Unknown expense", result)
    }

    @Test
    fun `sanitizePemanfaatan should return Unknown expense when input is blank`() {
        val result = DataValidator.sanitizePemanfaatan("")
        assertEquals("Unknown expense", result)
        
        val result2 = DataValidator.sanitizePemanfaatan("   ")
        assertEquals("Unknown expense", result2)
    }

    @Test
    fun `sanitizePemanfaatan should return Unknown expense when input exceeds 100 characters`() {
        val longText = "a".repeat(101)
        val result = DataValidator.sanitizePemanfaatan(longText)
        assertEquals("Unknown expense", result)
    }

    @Test
    fun `formatCurrency should return formatted currency for valid positive amount`() {
        val result = DataValidator.formatCurrency(1000)
        assertEquals("Iuran Perwarga Rp.1,000", result)
    }

    @Test
    fun `formatCurrency should return formatted currency for zero amount`() {
        val result = DataValidator.formatCurrency(0)
        assertEquals("Iuran Perwarga Rp.0", result)
    }

    @Test
    fun `formatCurrency should return formatted currency for zero when amount is null`() {
        val result = DataValidator.formatCurrency(null)
        assertEquals("Iuran Perwarga Rp.0", result)
    }

    @Test
    fun `formatCurrency should return formatted currency for zero when amount is negative`() {
        val result = DataValidator.formatCurrency(-100)
        assertEquals("Iuran Perwarga Rp.0", result)
    }

    @Test
    fun `formatCurrency should return formatted currency for zero when amount exceeds max safe value`() {
        val result = DataValidator.formatCurrency(1000000000) // 1 billion, exceeds 999999999
        assertEquals("Iuran Perwarga Rp.0", result)
    }
}