package com.example.iurankomplek.transaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.util.Date

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `Date should round trip through epoch millis`() {
        val original = Date(1_700_000_000_000L)

        val stored = converters.fromDate(original)

        assertEquals(1_700_000_000_000L, stored)
        assertEquals(original, converters.toDate(stored))
    }

    @Test
    fun `null Date should round trip as null`() {
        assertNull(converters.fromDate(null))
        assertNull(converters.toDate(null))
    }

    @Test
    fun `BigDecimal should round trip without losing precision`() {
        val original = BigDecimal("12345678901234567890.123456789")

        val stored = converters.fromBigDecimal(original)

        assertEquals("12345678901234567890.123456789", stored)
        assertEquals(original, converters.toBigDecimal(stored))
    }

    @Test
    fun `BigDecimal should not use scientific notation on disk`() {
        val stored = converters.fromBigDecimal(BigDecimal("0.00000001"))

        assertEquals("0.00000001", stored)
        assertEquals(BigDecimal("0.00000001"), converters.toBigDecimal(stored))
    }

    @Test
    fun `null BigDecimal should round trip as null`() {
        assertNull(converters.fromBigDecimal(null))
        assertNull(converters.toBigDecimal(null))
    }

    @Test
    fun `string map should round trip`() {
        val original = mapOf("channel" to "mobile", "ref" to "abc-123")

        val stored = converters.fromStringMap(original)

        assertEquals(original, converters.toStringMap(stored))
    }

    @Test
    fun `empty string map should round trip as empty`() {
        assertEquals(emptyMap<String, String>(), converters.toStringMap(converters.fromStringMap(emptyMap())))
    }

    @Test
    fun `null string map should round trip as null`() {
        assertNull(converters.fromStringMap(null))
        assertNull(converters.toStringMap(null))
    }

    @Test
    fun `blank string should read as null`() {
        assertNull(converters.toStringMap("   "))
    }

    @Test
    fun `string map value containing separators should round trip`() {
        val original = mapOf("note" to "a;b=c")

        val stored = converters.fromStringMap(original)

        assertEquals("a;b=c", converters.toStringMap(stored)?.get("note"))
    }
}
