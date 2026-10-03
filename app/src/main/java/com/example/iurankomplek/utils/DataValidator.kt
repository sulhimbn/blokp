package com.example.iurankomplek.utils

import java.net.URL

object DataValidator {

    private val MARKUP = Regex("<[^>]*>")

    /**
     * Display names come from a spreadsheet API, so they are attacker-influenced.
     * Strip any markup and neutralise the remaining angle brackets before the value
     * reaches a TextView or an export.
     */
    private fun stripMarkup(input: String): String =
        input.replace(MARKUP, "")
            .replace("<", "‹")
            .replace(">", "›")

    fun sanitizeName(input: String?): String {
        return stripMarkup(input.orEmpty()).trim()
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
        return input?.trim()?.takeIf { it.isNotBlank() && it.length <= Constants.Validation.MAX_ADDRESS_LENGTH }
            ?: "Address not available"
    }
    
    fun sanitizePemanfaatan(input: String?): String {
        return stripMarkup(input.orEmpty()).trim()
            .takeIf { it.isNotBlank() && it.length <= Constants.Validation.MAX_PEMANFAATAN_LENGTH }
            ?: "Unknown expense"
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