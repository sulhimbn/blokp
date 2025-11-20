package com.example.iurankomplek.utils

import java.net.URL
import java.util.regex.Pattern

object DataValidator {
    fun sanitizeName(input: String?): String {
        return input?.trim()?.takeIf { it.isNotBlank() && it.length <= 50 && isValidText(it) } 
            ?: "Unknown"
    }
    
    fun sanitizeEmail(input: String?): String {
        val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        return if (input != null && input.matches(emailPattern.toRegex()) && input.length <= 100) {
            input
        } else "invalid@email.com"
    }
    
    fun sanitizeAddress(input: String?): String {
        return input?.trim()?.takeIf { it.isNotBlank() && it.length <= 200 && isValidText(it) } 
            ?: "Address not available"
    }
    
    fun sanitizePemanfaatan(input: String?): String {
        return input?.trim()?.takeIf { it.isNotBlank() && it.length <= 100 && isValidText(it) } 
            ?: "Unknown expense"
    }
    
    fun formatCurrency(amount: Int?): String {
        return if (amount != null && amount >= 0 && amount <= Int.MAX_VALUE) {
            "Rp.${String.format("%,d", amount)}"
        } else "Rp.0"
    }
    
    fun isValidUrl(input: String?): Boolean {
        return try {
            if (input.isNullOrBlank()) {
                false
            } else {
                // Additional validation to prevent potential security issues with URLs
                val url = URL(input)
                // Only allow http and https protocols for security
                val protocol = url.protocol
                if (protocol != "http" && protocol != "https") {
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
    
    fun validateFinancialValue(value: Int?): Boolean {
        return value != null && value >= 0 && value <= 999999999 // Max reasonable financial value
    }
    
    private fun isValidText(input: String): Boolean {
        // Check for potentially dangerous characters or patterns
        return !Pattern.compile(".*[<>'\"&].*").matcher(input).matches()
    }
    
    fun sanitizeFinancialValue(value: Int?): Int {
        return if (validateFinancialValue(value)) {
            value!!
        } else {
            0
        }
    }
}