package com.example.iurankomplek.data

import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.util.Date

class DataTypeConvertersTest {

    private val converters = DataTypeConverters()

    @Test
    fun `fromPaymentMethod should convert enum to string`() {
        assertEquals("CREDIT_CARD", converters.fromPaymentMethod(PaymentMethod.CREDIT_CARD))
        assertEquals("BANK_TRANSFER", converters.fromPaymentMethod(PaymentMethod.BANK_TRANSFER))
        assertEquals("E_WALLET", converters.fromPaymentMethod(PaymentMethod.E_WALLET))
        assertEquals("CASH", converters.fromPaymentMethod(PaymentMethod.CASH))
    }

    @Test
    fun `toPaymentMethod should convert string to enum`() {
        assertEquals(PaymentMethod.CREDIT_CARD, converters.toPaymentMethod("CREDIT_CARD"))
        assertEquals(PaymentMethod.BANK_TRANSFER, converters.toPaymentMethod("BANK_TRANSFER"))
        assertEquals(PaymentMethod.E_WALLET, converters.toPaymentMethod("E_WALLET"))
        assertEquals(PaymentMethod.CASH, converters.toPaymentMethod("CASH"))
    }

    @Test
    fun `payment method conversion should be round-trip consistent`() {
        val methods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.BANK_TRANSFER,
            PaymentMethod.E_WALLET,
            PaymentMethod.CASH
        )

        methods.forEach { method ->
            val string = converters.fromPaymentMethod(method)
            val convertedBack = converters.toPaymentMethod(string)
            assertEquals(method, convertedBack)
        }
    }

    @Test
    fun `fromPaymentStatus should convert enum to string`() {
        assertEquals("PENDING", converters.fromPaymentStatus(PaymentStatus.PENDING))
        assertEquals("COMPLETED", converters.fromPaymentStatus(PaymentStatus.COMPLETED))
        assertEquals("FAILED", converters.fromPaymentStatus(PaymentStatus.FAILED))
        assertEquals("REFUNDED", converters.fromPaymentStatus(PaymentStatus.REFUNDED))
        assertEquals("CANCELLED", converters.fromPaymentStatus(PaymentStatus.CANCELLED))
    }

    @Test
    fun `toPaymentStatus should convert string to enum`() {
        assertEquals(PaymentStatus.PENDING, converters.toPaymentStatus("PENDING"))
        assertEquals(PaymentStatus.COMPLETED, converters.toPaymentStatus("COMPLETED"))
        assertEquals(PaymentStatus.FAILED, converters.toPaymentStatus("FAILED"))
        assertEquals(PaymentStatus.REFUNDED, converters.toPaymentStatus("REFUNDED"))
        assertEquals(PaymentStatus.CANCELLED, converters.toPaymentStatus("CANCELLED"))
    }

    @Test
    fun `payment status conversion should be round-trip consistent`() {
        val statuses = listOf(
            PaymentStatus.PENDING,
            PaymentStatus.COMPLETED,
            PaymentStatus.FAILED,
            PaymentStatus.REFUNDED,
            PaymentStatus.CANCELLED
        )

        statuses.forEach { status ->
            val string = converters.fromPaymentStatus(status)
            val convertedBack = converters.toPaymentStatus(string)
            assertEquals(status, convertedBack)
        }
    }

    @Test
    fun `fromBigDecimal should convert to plain string`() {
        assertEquals("0", converters.fromBigDecimal(BigDecimal.ZERO))
        assertEquals("100", converters.fromBigDecimal(BigDecimal("100")))
        assertEquals("100.50", converters.fromBigDecimal(BigDecimal("100.50")))
        assertEquals("0.01", converters.fromBigDecimal(BigDecimal("0.01")))
        assertEquals("999999999.99", converters.fromBigDecimal(BigDecimal("999999999.99")))
    }

    @Test
    fun `fromBigDecimal with null should return 0`() {
        assertEquals("0", converters.fromBigDecimal(null))
    }

    @Test
    fun `toBigDecimal should convert string to BigDecimal`() {
        assertEquals(BigDecimal.ZERO, converters.toBigDecimal("0"))
        assertEquals(BigDecimal("100"), converters.toBigDecimal("100"))
        assertEquals(BigDecimal("100.50"), converters.toBigDecimal("100.50"))
        assertEquals(BigDecimal("0.01"), converters.toBigDecimal("0.01"))
        assertEquals(BigDecimal("999999999.99"), converters.toBigDecimal("999999999.99"))
    }

    @Test
    fun `toBigDecimal with null should return ZERO`() {
        assertEquals(BigDecimal.ZERO, converters.toBigDecimal(null))
    }

    @Test
    fun `toBigDecimal with empty string should return ZERO`() {
        assertEquals(BigDecimal.ZERO, converters.toBigDecimal(""))
    }

    @Test
    fun `BigDecimal conversion should be round-trip consistent`() {
        val values = listOf(
            BigDecimal.ZERO,
            BigDecimal("100"),
            BigDecimal("100.50"),
            BigDecimal("0.01"),
            BigDecimal("999999999.99"),
            BigDecimal("1234567.89")
        )

        values.forEach { value ->
            val string = converters.fromBigDecimal(value)
            val convertedBack = converters.toBigDecimal(string)
            assertEquals(value, convertedBack)
        }
    }

    @Test
    fun `fromDate should convert Date to Long`() {
        val date = Date(1000000L)
        assertEquals(1000000L, converters.fromDate(date))
    }

    @Test
    fun `fromDate with null should return 0`() {
        assertEquals(0L, converters.fromDate(null))
    }

    @Test
    fun `fromDate with current time should return valid timestamp`() {
        val date = Date()
        val timestamp = converters.fromDate(date)
        assertEquals(date.time, timestamp)
    }

    @Test
    fun `toDate should convert Long to Date`() {
        assertEquals(Date(1000000L), converters.toDate(1000000L))
    }

    @Test
    fun `toDate with null should return current Date`() {
        val before = Date()
        val result = converters.toDate(null)
        val after = Date()

        assertNotNull(result)
        assertTrue(result.time >= before.time)
        assertTrue(result.time <= after.time)
    }

    @Test
    fun `toDate with 0 should return current Date`() {
        val before = Date()
        val result = converters.toDate(0L)
        val after = Date()

        assertNotNull(result)
        assertTrue(result.time >= before.time)
        assertTrue(result.time <= after.time)
    }

    @Test
    fun `toDate with negative value should return current Date`() {
        val before = Date()
        val result = converters.toDate(-1L)
        val after = Date()

        assertNotNull(result)
        assertTrue(result.time >= before.time)
        assertTrue(result.time <= after.time)
    }

    @Test
    fun `Date conversion should be round-trip consistent`() {
        val dates = listOf(
            Date(0L),
            Date(1000000L),
            Date(System.currentTimeMillis()),
            Date(946684800000L) // 2000-01-01
        )

        dates.forEach { date ->
            val timestamp = converters.fromDate(date)
            val convertedBack = converters.toDate(timestamp)
            assertEquals(date, convertedBack)
        }
    }

    @Test
    fun `fromStringMap should convert Map to JSON string`() {
        val map = mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to "value3"
        )

        val result = converters.fromStringMap(map)

        assertTrue(result.contains("key1"))
        assertTrue(result.contains("value1"))
        assertTrue(result.contains("key2"))
        assertTrue(result.contains("value2"))
        assertTrue(result.contains("key3"))
        assertTrue(result.contains("value3"))
    }

    @Test
    fun `fromStringMap with null should return empty map JSON`() {
        val result = converters.fromStringMap(null)
        assertEquals("{}", result)
    }

    @Test
    fun `fromStringMap with empty map should return empty JSON object`() {
        val result = converters.fromStringMap(emptyMap())
        assertEquals("{}", result)
    }

    @Test
    fun `toStringMap should convert JSON string to Map`() {
        val jsonString = """{"key1":"value1","key2":"value2","key3":"value3"}"""
        val result = converters.toStringMap(jsonString)

        assertEquals(3, result.size)
        assertEquals("value1", result["key1"])
        assertEquals("value2", result["key2"])
        assertEquals("value3", result["key3"])
    }

    @Test
    fun `toStringMap with null should return empty map`() {
        val result = converters.toStringMap(null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toStringMap with empty string should return empty map`() {
        val result = converters.toStringMap("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toStringMap with empty JSON object should return empty map`() {
        val result = converters.toStringMap("{}")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Map conversion should be round-trip consistent`() {
        val maps = listOf(
            emptyMap<String, String>(),
            mapOf("key" to "value"),
            mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
            mapOf(
                "name" to "John Doe",
                "email" to "john@example.com",
                "address" to "123 Main St",
                "phone" to "555-1234"
            )
        )

        maps.forEach { map ->
            val jsonString = converters.fromStringMap(map)
            val convertedBack = converters.toStringMap(jsonString)
            assertEquals(map, convertedBack)
        }
    }

    @Test
    fun `toStringMap should handle special characters in values`() {
        val jsonString = """{"key":"value with spaces & symbols!@#$%"}"""
        val result = converters.toStringMap(jsonString)

        assertEquals("value with spaces & symbols!@#$%", result["key"])
    }

    @Test
    fun `toStringMap should handle Unicode characters`() {
        val jsonString = """{"name":"José García","email":"test@ñoño.com"}"""
        val result = converters.toStringMap(jsonString)

        assertEquals("José García", result["name"])
        assertEquals("test@ñoño.com", result["email"])
    }

    @Test
    fun `fromStringMap should handle special characters in values`() {
        val map = mapOf(
            "name" to "José García",
            "email" to "test@ñoño.com",
            "special" to "!@#$%^&*()"
        )

        val jsonString = converters.fromStringMap(map)

        assertTrue(jsonString.contains("José"))
        assertTrue(jsonString.contains("García"))
        assertTrue(jsonString.contains("test@ñoño.com"))
        assertTrue(jsonString.contains("!@#$%^&*()"))
    }

    @Test
    fun `fromBigDecimal should preserve precision`() {
        val value = BigDecimal("123456789.123456789")
        val string = converters.fromBigDecimal(value)
        val convertedBack = converters.toBigDecimal(string)

        assertEquals(value, convertedBack)
    }

    @Test
    fun `toBigDecimal should handle scientific notation`() {
        val result = converters.toBigDecimal("1E+10")
        assertEquals(BigDecimal("10000000000"), result)
    }

    @Test
    fun `fromBigDecimal should handle very large numbers`() {
        val value = BigDecimal("999999999999999999999.99")
        val string = converters.fromBigDecimal(value)
        val convertedBack = converters.toBigDecimal(string)

        assertEquals(value, convertedBack)
    }

    @Test
    fun `fromBigDecimal should handle very small numbers`() {
        val value = BigDecimal("0.000000001")
        val string = converters.fromBigDecimal(value)
        val convertedBack = converters.toBigDecimal(string)

        assertEquals(value, convertedBack)
    }

    @Test
    fun `fromDate should handle epoch start`() {
        val date = Date(0L)
        assertEquals(0L, converters.fromDate(date))
    }

    @Test
    fun `toDate should handle epoch start`() {
        assertEquals(Date(0L), converters.toDate(0L))
    }

    @Test
    fun `fromDate should handle far future date`() {
        val date = Date(9999999999999L) // 2286-11-20
        assertEquals(9999999999999L, converters.fromDate(date))
    }

    @Test
    fun `toDate should handle far future timestamp`() {
        assertEquals(Date(9999999999999L), converters.toDate(9999999999999L))
    }

    @Test
    fun `fromStringMap should handle nested map structure`() {
        val map = mapOf(
            "user" to "john",
            "metadata" to "age=30,city=NYC",
            "tags" to "vip,priority,important"
        )

        val jsonString = converters.fromStringMap(map)

        assertTrue(jsonString.contains("user"))
        assertTrue(jsonString.contains("metadata"))
        assertTrue(jsonString.contains("tags"))
    }

    @Test
    fun `toStringMap should handle escaped quotes in JSON`() {
        val jsonString = """{"key":"value with \"quotes\" inside"}"""
        val result = converters.toStringMap(jsonString)

        assertEquals("value with \"quotes\" inside", result["key"])
    }

    @Test
    fun `Map conversion should handle numeric string keys`() {
        val map = mapOf(
            "1" to "value1",
            "2" to "value2",
            "100" to "value100"
        )

        val jsonString = converters.fromStringMap(map)
        val convertedBack = converters.toStringMap(jsonString)

        assertEquals(map, convertedBack)
    }
}
