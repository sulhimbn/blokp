package com.example.iurankomplek.receipt

import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.transaction.Transaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Date

class ReceiptGeneratorTest {

    private val generator = ReceiptGenerator()

    private fun transaction(id: String = "txn_1") = Transaction(
        id = id,
        userId = "user_1",
        amount = BigDecimal("125000"),
        currency = "IDR",
        status = PaymentStatus.COMPLETED,
        paymentMethod = PaymentMethod.CREDIT_CARD,
        description = "Monthly dues",
        createdAt = Date(),
        updatedAt = Date(),
        metadata = emptyMap()
    )

    @Test
    fun generatesReceiptNumberWithDateAndNumericSuffix() {
        val receipt = generator.generateReceipt(transaction())

        val pattern = Regex("^RCPT-\\d{8}-\\d{4}$")
        assertTrue(
            "Receipt number should match RCPT-yyyyMMdd-nnnn but was ${receipt.receiptNumber}",
            pattern.matches(receipt.receiptNumber)
        )
    }

    @Test
    fun receiptNumberUsesBasicIsoDateForToday() {
        val receipt = generator.generateReceipt(transaction())
        val expectedDate = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(Date())

        assertTrue(
            "Receipt number should embed today's date $expectedDate",
            receipt.receiptNumber.startsWith("RCPT-$expectedDate-")
        )
    }

    @Test
    fun copiesTransactionFieldsOntoTheReceipt() {
        val source = transaction(id = "txn_42")
        val receipt = generator.generateReceipt(source)

        assertEquals(source.id, receipt.transactionId)
        assertEquals(source.userId, receipt.userId)
        assertEquals(source.amount, receipt.amount)
        assertEquals(source.description, receipt.description)
        assertEquals(PaymentMethod.CREDIT_CARD.name, receipt.paymentMethod)
        assertEquals(source.createdAt, receipt.transactionDate)
        assertNotNull("Receipt should have an id", receipt.id)
    }

    @Test
    fun generatesDistinctNumbersForSequentialReceipts() {
        val numbers = (1..25).map { generator.generateReceipt(transaction(id = "txn_$it")).receiptNumber }

        assertEquals("Every receipt number should be unique", 25, numbers.toSet().size)
    }

    @Test
    fun qrCodeIsDerivedFromTheTransactionId() {
        val receipt = generator.generateReceipt(transaction(id = "txn_qr"))

        assertEquals("QR:txn_qr", receipt.qrCode)
    }
}
