package com.example.iurankomplek.utils

import java.net.URL
import java.util.regex.Pattern

 object InputSanitizer {
    
    private val CURRENCY_FORMATTER = java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).apply {
        isGroupingUsed = true
        minimumIntegerDigits = 1
    }

    // SECURITY: Improved email validation following RFC 5322 recommendations
    // Prevents email injection attacks and limits input to reasonable patterns
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )
    
    // SECURITY: Pre-compiled regex for better performance and prevents ReDoS attacks
    private val SANITIZATION_PATTERN = Pattern.compile("[<>\"'&]")
    
    fun sanitizeName(input: String?): String {
        val sanitized = input?.trim()
            ?.takeIf { it.isNotBlank() && it.length <= Constants.Validation.MAX_NAME_LENGTH }
            ?: "Unknown"
        
        // SECURITY: Remove potentially dangerous characters
        return removeDangerousCharacters(sanitized)
    }
    
    fun sanitizeEmail(input: String?): String {
        // SECURITY: Validate email before accepting
        if (input.isNullOrBlank()) {
            return "invalid@email.com"
        }
        
        // SECURITY: Check length before regex to prevent ReDoS attacks
        if (input.length > Constants.Validation.MAX_EMAIL_LENGTH) {
            return "invalid@email.com"
        }
        
        // SECURITY: Use pre-compiled Pattern for better performance and ReDoS protection
        return if (EMAIL_PATTERN.matcher(input).matches()) {
            // SECURITY: Lowercase email for consistency and prevent case-insensitive issues
            input.lowercase().trim()
        } else {
            "invalid@email.com"
        }
    }
    
    fun sanitizeAddress(input: String?): String {
        val sanitized = input?.trim()
            ?.takeIf { it.isNotBlank() && it.length <= Constants.Validation.MAX_ADDRESS_LENGTH }
            ?: "Address not available"
        
        // SECURITY: Remove potentially dangerous characters to prevent injection
        return removeDangerousCharacters(sanitized)
    }
    
    fun sanitizePemanfaatan(input: String?): String {
        val sanitized = input?.trim()
            ?.takeIf { it.isNotBlank() && it.length <= Constants.Validation.MAX_PEMANFAATAN_LENGTH }
            ?: "Unknown expense"
        
        // SECURITY: Remove potentially dangerous characters
        return removeDangerousCharacters(sanitized)
    }
    
    fun formatCurrency(amount: Int?): String {
        return if (amount != null && amount >= 0) {
            "Rp.${CURRENCY_FORMATTER.format(amount.toLong())}"
        } else "Rp.0"
    }

    fun sanitizeNumericInput(input: String?): String {
        if (input.isNullOrBlank()) {
            return "0"
        }
        
        val sanitized = input.trim()
        
        if (!sanitized.matches(Regex("^\\d+$"))) {
            return "0"
        }
        
        val num = sanitized.toLongOrNull() ?: 0L
        
        return if (num >= 0 && num <= Constants.Payment.MAX_PAYMENT_AMOUNT.toLong()) {
            sanitized
        } else {
            "0"
        }
    }

    fun sanitizePaymentAmount(amount: Double?): Double {
        return if (amount != null && amount > 0 && amount <= Constants.Payment.MAX_PAYMENT_AMOUNT) {
            val rounded = Math.round(amount * 100.0) / 100.0
            rounded
        } else {
            0.0
        }
    }

    fun validatePositiveInteger(input: String?): Boolean {
        if (input.isNullOrBlank()) {
            return false
        }

        return try {
            val num = input.trim().toInt()
            num > 0
        } catch (e: NumberFormatException) {
            android.util.Log.d("InputSanitizer", "Invalid positive integer format")
            false
        }
    }

    fun validatePositiveDouble(input: String?): Boolean {
        if (input.isNullOrBlank()) {
            return false
        }

        return try {
            val num = input.trim().toDouble()
            num > 0 && num <= Constants.Payment.MAX_PAYMENT_AMOUNT
        } catch (e: NumberFormatException) {
            android.util.Log.d("InputSanitizer", "Invalid positive double format")
            false
        }
    }
    
    fun isValidUrl(input: String?): Boolean {
        return try {
            if (input.isNullOrBlank()) {
                false
            } else {
                // SECURITY: Validate URL length to prevent DoS attacks
                if (input.length > 2048) {
                    return false
                }

                // Additional validation to prevent potential security issues with URLs
                val url = URL(input)

                // SECURITY: Only allow http and https protocols
                val protocol = url.protocol
                if (protocol != "http" && protocol != "https") {
                    return false
                }

                // SECURITY: Check for suspicious URL patterns
                val host = url.host?.lowercase() ?: ""
                if (host.contains("localhost") || host.contains("127.0.0.1")) {
                    return false
                }

                // Check that URL doesn't contain dangerous characters after validation
                URL(input).toURI()
                true
            }
        } catch (e: Exception) {
            android.util.Log.d("InputSanitizer", "Invalid URL format")
            false
        }
    }
    
    /**
     * Validates that input is a safe alphanumeric ID
     * Used for validating IDs from Intent extras, database lookups, etc.
     * Only allows alphanumeric characters, hyphens, and underscores
     */
    fun isValidAlphanumericId(input: String): Boolean {
        if (input.isBlank()) return false
        if (input.length > 100) return false
        
        val idPattern = Regex("^[a-zA-Z0-9_-]+$")
        return idPattern.matches(input)
    }
    
    /**
     * Removes potentially dangerous characters to prevent injection attacks
     * Protects against XSS and other injection vectors
     */
    private fun removeDangerousCharacters(input: String): String {
        // SECURITY: Remove characters that could be used in injection attacks
        // This provides defense-in-depth against XSS, SQL injection, etc.
        return SANITIZATION_PATTERN.matcher(input).replaceAll("")
    }
}
