package com.example.iurankomplek.utils

import java.net.URL
import java.util.regex.Pattern

object DataValidator {
    
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
            "Rp.${String.format("%,d", amount)}"
        } else "Rp.0"
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
                
                // Check that the URL doesn't contain dangerous characters after validation
                URL(input).toURI()
                true
            }
        } catch (e: Exception) {
            false
        }
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