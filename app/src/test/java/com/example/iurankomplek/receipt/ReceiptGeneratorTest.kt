package com.example.iurankomplek.receipt

import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.transaction.Transaction
import java.math.BigDecimal
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptGeneratorTest {

    private val generator = ReceiptGenerator()

    private fun transaction() = Transaction(
        id = "tx-1",
        userId = "user-1",
        amount = BigDecimal("100.00"),
        currency = "IDR",
        status = PaymentStatus.COMPLETED,
        paymentMethod = PaymentMethod.BANK_TRANSFER,
        description = "Test",
        createdAt = Calendar.getInstance().time,
        updatedAt = Calendar.getInstance().time
    )

    /**
     * Guards the minSdk 24 contract: java.time is API 26+, so the receipt number date must come
     * from an API-24-safe formatter or receipt generation throws NoClassDefFoundError on
     * Android 7.0/7.1.
     */
    @Test
    fun `receipt number should embed an eight digit yyyyMMdd date`() {
        val receipt = generator.generateReceipt(transaction())

        assertTrue(receipt.receiptNumber.startsWith("RCPT-"))
        val datePart = receipt.receiptNumber.removePrefix("RCPT-").substringBeforeLast("-")
        assertEquals("date portion should be 8 digits", 8, datePart.length)
        assertTrue("date portion should be all digits: $datePart", datePart.all { it.isDigit() })
        val expected = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
            .format(Calendar.getInstance().time)
        assertEquals(expected, datePart)
    }

    @Test
    fun `receipt number should carry a four digit random suffix`() {
        val receipt = generator.generateReceipt(transaction())

        val suffix = receipt.receiptNumber.substringAfterLast("-")
        assertEquals("suffix should be 4 digits", 4, suffix.length)
        assertTrue("suffix should be numeric: $suffix", suffix.all { it.isDigit() })
    }

    @Test
    fun `receipt number should be unique across calls`() {
        val numbers = (1..50).map { generator.generateReceipt(transaction()).receiptNumber }

        assertTrue("receipt numbers should not all collide", numbers.toSet().size > 1)
    }

    @Test
    fun `receipt should copy the transaction fields`() {
        val tx = transaction()
        val receipt = generator.generateReceipt(tx)

        assertEquals(tx.id, receipt.transactionId)
        assertEquals(tx.userId, receipt.userId)
        assertEquals(tx.amount, receipt.amount)
        assertEquals(tx.description, receipt.description)
        assertEquals(tx.createdAt, receipt.transactionDate)
        assertEquals("QR:${tx.id}", receipt.qrCode)
    }

    @Test
    fun `create should produce a pending transaction for a request`() {
        val created = Transaction.create(
            PaymentRequest(
                amount = BigDecimal("250.00"),
                description = "Iuran",
                customerId = "user-9",
                paymentMethod = PaymentMethod.E_WALLET
            )
        )

        assertEquals(PaymentStatus.PENDING, created.status)
        assertEquals("user-9", created.userId)
        assertEquals(0, created.amount.compareTo(BigDecimal("250.00")))
    }
}