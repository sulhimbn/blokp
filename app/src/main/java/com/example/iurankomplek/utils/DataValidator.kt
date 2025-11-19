package com.example.iurankomplek.utils

object DataValidator {
    fun sanitizeName(input: String?): String {
        return input?.trim()?.takeIf { it.isNotBlank() && it.length <= 50 } 
            ?: "Unknown"
    }
    
    fun sanitizeEmail(input: String?): String {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (input?.matches(emailPattern.toRegex()) == true) {
            input
        } else "invalid@email.com"
    }
    
    fun sanitizeAddress(input: String?): String {
        return input?.trim()?.takeIf { it.isNotBlank() && it.length <= 200 } 
            ?: "Address not available"
    }
    
    fun sanitizePemanfaatan(input: String?): String {
        return input?.trim()?.takeIf { it.isNotBlank() && it.length <= 100 } 
            ?: "Unknown expense"
    }
    
    fun formatCurrency(amount: Int?): String {
        return if (amount != null && amount >= 0) {
            "Rp.${String.format("%,d", amount)}"
        } else "Rp.0"
    }
    
    fun sanitizeString(input: String?, maxLength: Int = 100): String {
        return input?.trim()?.takeIf { it.length <= maxLength && !it.contains("\n\r\t".toRegex()) } 
            ?: "Invalid data"
    }
    
    fun validateFinancialAmount(amount: Int?): Int {
        return if (amount != null && amount >= 0) amount else 0
    }
}