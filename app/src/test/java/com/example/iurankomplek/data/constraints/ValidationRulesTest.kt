package com.example.iurankomplek.data.constraints

import org.junit.Assert.*
import org.junit.Test

class ValidationRulesTest {

    @Test
    fun `EMAIL_PATTERN should match valid email addresses`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.com",
            "user_name@example.co.uk",
            "u123@example-domain.com",
            "a@b.c"
        )

        validEmails.forEach { email ->
            assertTrue("Email '$email' should match pattern", email.matches(Regex(ValidationRules.EMAIL_PATTERN)))
        }
    }

    @Test
    fun `EMAIL_PATTERN should reject invalid email addresses`() {
        val invalidEmails = listOf(
            "invalid",
            "invalid@",
            "@example.com",
            "invalid@domain",
            "invalid@@example.com",
            "invalid@example",
            "invalid@example..com",
            "invalid@example.c"
        )

        invalidEmails.forEach { email ->
            assertFalse("Email '$email' should not match pattern", email.matches(Regex(ValidationRules.EMAIL_PATTERN)))
        }
    }

    @Test
    fun `EMAIL_PATTERN should handle edge cases`() {
        val edgeCases = mapOf(
            "" to false,
            " " to false,
            "UPPERCASE@EXAMPLE.COM" to true,
            "lowercase@example.com" to true,
            "MixedCase@Example.Com" to true,
            "123@example.com" to true,
            "test.123@example.com" to true,
            "test_123@example.com" to true,
            "test+123@example.com" to true,
            "test-123@example.com" to true,
            "test@123.example.com" to true
        )

        edgeCases.forEach { (email, expected) ->
            val matches = email.matches(Regex(ValidationRules.EMAIL_PATTERN))
            if (expected) {
                assertTrue("Email '$email' should match pattern", matches)
            } else {
                assertFalse("Email '$email' should not match pattern", matches)
            }
        }
    }

    @Test
    fun `Numeric MIN_VALUE should be zero`() {
        assertEquals(0, ValidationRules.Numeric.MIN_VALUE)
    }

    @Test
    fun `Numeric MAX_VALUE should be positive`() {
        assertTrue(ValidationRules.Numeric.MAX_VALUE > 0)
    }

    @Test
    fun `Numeric MAX_VALUE should be consistent with constraints`() {
        assertEquals(FinancialRecordConstraints.Constraints.MAX_NUMERIC_VALUE, ValidationRules.Numeric.MAX_VALUE)
    }

    @Test
    fun `Numeric MIN_VALUE should be less than MAX_VALUE`() {
        assertTrue(ValidationRules.Numeric.MIN_VALUE < ValidationRules.Numeric.MAX_VALUE)
    }

    @Test
    fun `Text MIN_LENGTH should be at least 1`() {
        assertTrue(ValidationRules.Text.MIN_LENGTH >= 1)
    }

    @Test
    fun `Text MIN_LENGTH should be 1`() {
        assertEquals(1, ValidationRules.Text.MIN_LENGTH)
    }
}
