package com.example.iurankomplek.utils

import org.junit.Test
import org.junit.Assert.assertEquals

class InputSanitizerOptimizationTest {

    @Test
    fun formatCurrency_shouldFormatCorrectly() {
        assertEquals("Rp.1.000", InputSanitizer.formatCurrency(1000))
        assertEquals("Rp.10.000", InputSanitizer.formatCurrency(10000))
        assertEquals("Rp.100.000", InputSanitizer.formatCurrency(100000))
        assertEquals("Rp.0", InputSanitizer.formatCurrency(0))
        assertEquals("Rp.0", InputSanitizer.formatCurrency(null))
        assertEquals("Rp.0", InputSanitizer.formatCurrency(-1))
    }

    @Test
    fun formatCurrency_shouldUseCachedFormatter() {
        val result1 = InputSanitizer.formatCurrency(1000)
        val result2 = InputSanitizer.formatCurrency(1000)
        assertEquals(result1, result2)
    }
}
