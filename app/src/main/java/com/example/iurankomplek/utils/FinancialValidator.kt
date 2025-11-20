package com.example.iurankomplek.utils

import com.example.iurankomplek.model.DataItem

/**
 * Utility class for financial data validation and sanitization
 */
object FinancialValidator {
    
    // Define reasonable financial limits to prevent overflow and invalid data
    private const val MAX_FINANCIAL_VALUE = 100_000_000 // 100 million
    private const val MAX_TOTAL_CALCULATION = Int.MAX_VALUE / 2 // Prevent overflow in calculations
    
    /**
     * Validates a financial value to ensure it's within acceptable limits
     * @param value the financial value to validate
     * @param fieldName the name of the field being validated (for error reporting)
     * @return validation result
     */
    fun validateFinancialValue(value: Int, fieldName: String): ValidationResult {
        return when {
            value < 0 -> ValidationResult.Invalid("Negative value not allowed for $fieldName: $value")
            value > MAX_FINANCIAL_VALUE -> ValidationResult.Invalid("Value too large for $fieldName: $value")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validates a DataItem to ensure all financial fields are within acceptable limits
     */
    fun validateDataItem(item: DataItem): ValidationResult {
        val validations = listOf(
            validateFinancialValue(item.iuran_perwarga, "iuran_perwarga"),
            validateFinancialValue(item.total_iuran_rekap, "total_iuran_rekap"),
            validateFinancialValue(item.jumlah_iuran_bulanan, "jumlah_iuran_bulanan"),
            validateFinancialValue(item.total_iuran_individu, "total_iuran_individu"),
            validateFinancialValue(item.pengeluaran_iuran_warga, "pengeluaran_iuran_warga")
        )
        
        val invalidResults = validations.filterIsInstance<ValidationResult.Invalid>()
        
        return if (invalidResults.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(invalidResults.joinToString("; ") { it.message })
        }
    }
    
    /**
     * Validates a collection of DataItems
     */
    fun validateDataItems(items: List<DataItem>): ValidationResult {
        if (items.isEmpty()) {
            return ValidationResult.Invalid("Financial data list is empty")
        }
        
        val invalidItems = items.mapIndexedNotNull { index, item ->
            val result = validateDataItem(item)
            if (result is ValidationResult.Invalid) {
                "Item at index $index: ${result.message}"
            } else null
        }
        
        return if (invalidItems.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(invalidItems.joinToString("; "))
        }
    }
    
    /**
     * Validates financial calculations to prevent overflow
     */
    fun validateCalculationOverflow(
        currentValue: Int, 
        valueToAdd: Int, 
        operationName: String
    ): ValidationResult {
        if (valueToAdd < 0) {
            return ValidationResult.Invalid("Cannot add negative value to $operationName")
        }
        
        return if (currentValue > Int.MAX_VALUE - valueToAdd) {
            ValidationResult.Invalid("Calculation overflow detected in $operationName: $currentValue + $valueToAdd")
        } else {
            ValidationResult.Valid
        }
    }
    
    /**
     * Validates multiplication that could cause overflow
     */
    fun validateMultiplicationOverflow(
        value: Int,
        multiplier: Int,
        operationName: String
    ): ValidationResult {
        if (multiplier < 0 || value < 0) {
            return ValidationResult.Invalid("Negative values not allowed for $operationName")
        }
        
        return if (value > Int.MAX_VALUE / multiplier) {
            ValidationResult.Invalid("Multiplication overflow detected in $operationName: $value * $multiplier")
        } else {
            ValidationResult.Valid
        }
    }
    
    /**
     * Validates the final financial result
     */
    fun validateFinalResult(result: Int, operationName: String): ValidationResult {
        return when {
            result < 0 -> ValidationResult.Invalid("Negative result not allowed for $operationName: $result")
            result > MAX_FINANCIAL_VALUE -> ValidationResult.Invalid("Result too large for $operationName: $result")
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Sealed class to represent validation results
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}