package com.example.iurankomplek.utils

object DataValidator {
    /**
     * Sanitizes user names to prevent XSS and ensure data integrity
     */
    fun sanitizeName(input: String?): String {
        return input?.trim()?.takeIf { 
            it.isNotBlank() && it.length <= 50 && isValidText(it) 
        } ?: "Unknown"
    }
    
    /**
     * Validates and sanitizes email addresses
     */
    fun sanitizeEmail(input: String?): String {
        val emailPattern = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return if (input != null && input.matches(emailPattern) && input.length <= 100) {
            input
        } else "invalid@email.com"
    }
    
    /**
     * Sanitizes address fields
     */
    fun sanitizeAddress(input: String?): String {
        return input?.trim()?.takeIf { 
            it.isNotBlank() && it.length <= 200 && isValidText(it) 
        } ?: "Address not available"
    }
    
    /**
     * Sanitizes pemanfaatan descriptions
     */
    fun sanitizePemanfaatan(input: String?): String {
        return input?.trim()?.takeIf { 
            it.isNotBlank() && it.length <= 100 && isValidText(it) 
        } ?: "Unknown expense"
    }
    
    /**
     * Formats currency values safely without prefix
     */
    fun formatCurrency(amount: Int?): String {
        val safeAmount = if (amount != null && amount >= 0 && amount <= 999999999) {
            amount
        } else {
            0
        }
        return "Rp.${String.format("%,d", safeAmount)}"
    }
    
    /**
     * Formats iuran perwarga with appropriate label
     */
    fun formatIuranPerwarga(amount: Int?): String {
        val safeAmount = if (amount != null && amount >= 0 && amount <= 999999999) {
            amount
        } else {
            0
        }
        return "Iuran Perwarga Rp.${String.format("%,d", safeAmount)}"
    }
    
    /**
     * Formats total iuran individu with appropriate label
     */
    fun formatTotalIuranIndividu(amount: Int?): String {
        val safeAmount = if (amount != null && amount >= 0 && amount <= 999999999) {
            amount
        } else {
            0
        }
        return "Total Iuran Individu Rp.${String.format("%,d", safeAmount)}"
    }
    
    /**
     * Validates text for potentially harmful content
     */
    private fun isValidText(text: String): Boolean {
        // Check for potential script tags or other harmful content
        val harmfulPatterns = listOf(
            Regex("<script.*?>.*?</script>", RegexOption.IGNORE_CASE),
            Regex("javascript:", RegexOption.IGNORE_CASE),
            Regex("vbscript:", RegexOption.IGNORE_CASE),
            Regex("onload", RegexOption.IGNORE_CASE),
            Regex("onerror", RegexOption.IGNORE_CASE)
        )
        
        return !harmfulPatterns.any { pattern -> 
            pattern.containsMatchIn(text) 
        }
    }
}