package com.example.iurankomplek.utils

import java.net.URL

object DataValidator {
    private val HTML_TAG = Regex("<[^>]*>")
    private val CONTROL_CHARS = Regex("[\\p{Cc}\\p{Cf}]")
    private val CSV_FORMULA_PREFIX = Regex("^[=+\\-@\\t\\r]")

    private fun stripMarkup(input: String): String =
        CONTROL_CHARS.replace(HTML_TAG.replace(input, ""), "")

    fun sanitizeName(input: String?): String {
        return stripMarkup(input?.trim().orEmpty())
            .takeIf { it.isNotBlank() && it.length <= Constants.Validation.MAX_NAME_LENGTH }
            ?: "Unknown"
    }
    
    fun sanitizeEmail(input: String?): String {
        val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        return if (input != null && input.matches(emailPattern.toRegex()) && input.length <= Constants.Validation.MAX_EMAIL_LENGTH) {
            input
        } else "invalid@email.com"
    }
    
    fun sanitizeAddress(input: String?): String {
        return stripMarkup(input?.trim().orEmpty())
            .takeIf { it.isNotBlank() && it.length <= Constants.Validation.MAX_ADDRESS_LENGTH }
            ?: "Address not available"
    }
    
    fun sanitizePemanfaatan(input: String?): String {
        return stripMarkup(input?.trim().orEmpty())
            .takeIf { it.isNotBlank() && it.length <= Constants.Validation.MAX_PEMANFAATAN_LENGTH }
            ?: "Unknown expense"
    }

    fun sanitizeForCsv(input: String?): String {
        val value = stripMarkup(input?.trim().orEmpty()).replace("\"", "'")
        return if (CSV_FORMULA_PREFIX.containsMatchIn(value)) "'$value" else value
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
}