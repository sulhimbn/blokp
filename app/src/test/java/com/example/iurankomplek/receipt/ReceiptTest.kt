package com.example.iurankomplek.receipt

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.util.Date
import java.util.UUID

class ReceiptTest {

    @Test
    fun receipt_withAllFields_createsValidReceipt() {
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234",
            qrCode = "QR:txn-001"
        )

        assertEquals("receipt-001", receipt.id)
        assertEquals("txn-001", receipt.transactionId)
        assertEquals("user-001", receipt.userId)
        assertEquals(BigDecimal("100000.00"), receipt.amount)
        assertEquals("Test payment", receipt.description)
        assertEquals("BANK_TRANSFER", receipt.paymentMethod)
        assertEquals("RCPT-20260107-1234", receipt.receiptNumber)
        assertEquals("QR:txn-001", receipt.qrCode)
    }

    @Test
    fun receipt_withNullQrCode_createsValidReceipt() {
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        assertNull(receipt.qrCode)
        assertEquals("receipt-001", receipt.id)
    }

    @Test
    fun receipt_withZeroAmount() {
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal.ZERO,
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        assertEquals(BigDecimal.ZERO, receipt.amount)
    }

    @Test
    fun receipt_withLargeAmount() {
        val largeAmount = BigDecimal("999999999999.99")
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = largeAmount,
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        assertEquals(largeAmount, receipt.amount)
    }

    @Test
    fun receipt_withDecimalAmount() {
        val amount = BigDecimal("125000.50")
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = amount,
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        assertEquals(amount, receipt.amount)
    }

    @Test
    fun receipt_withNegativeAmount() {
        val amount = BigDecimal("-100000.00")
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = amount,
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        assertEquals(amount, receipt.amount)
    }

    @Test
    fun receipt_withEmptyDescription() {
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        assertEquals("", receipt.description)
    }

    @Test
    fun receipt_withNullQrCode_optionalParameter() {
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234",
            qrCode = null
        )

        assertNull(receipt.qrCode)
    }

    @Test
    fun receipt_dataClass_equality_sameReceiptsAreEqual() {
        val date = Date()
        val receipt1 = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = date,
            receiptNumber = "RCPT-20260107-1234",
            qrCode = "QR:txn-001"
        )

        val receipt2 = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = date,
            receiptNumber = "RCPT-20260107-1234",
            qrCode = "QR:txn-001"
        )

        assertEquals(receipt1, receipt2)
        assertEquals(receipt1.hashCode(), receipt2.hashCode())
    }

    @Test
    fun receipt_dataClass_inequality_differentIds() {
        val date = Date()
        val receipt1 = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = date,
            receiptNumber = "RCPT-20260107-1234"
        )

        val receipt2 = Receipt(
            id = "receipt-002",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = date,
            receiptNumber = "RCPT-20260107-1234"
        )

        assertNotEquals(receipt1, receipt2)
        assertNotEquals(receipt1.hashCode(), receipt2.hashCode())
    }

    @Test
    fun receipt_copy_createsNewInstanceWithSameValues() {
        val original = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234",
            qrCode = "QR:txn-001"
        )

        val copy = original.copy()

        assertEquals(original, copy)
        assertEquals(original.id, copy.id)
        assertEquals(original.transactionId, copy.transactionId)
    }

    @Test
    fun receipt_copy_withModifiedField_createsNewReceipt() {
        val original = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234",
            qrCode = "QR:txn-001"
        )

        val modified = original.copy(amount = BigDecimal("200000.00"))

        assertNotEquals(original, modified)
        assertEquals(original.id, modified.id)
        assertNotEquals(original.amount, modified.amount)
        assertEquals(BigDecimal("100000.00"), original.amount)
        assertEquals(BigDecimal("200000.00"), modified.amount)
    }

    @Test
    fun receipt_withDifferentPaymentMethods() {
        val methods = listOf("CREDIT_CARD", "BANK_TRANSFER", "E_WALLET", "CASH")

        methods.forEach { method ->
            val receipt = Receipt(
                id = "receipt-001",
                transactionId = "txn-001",
                userId = "user-001",
                amount = BigDecimal("100000.00"),
                description = "Test payment",
                paymentMethod = method,
                transactionDate = Date(),
                receiptNumber = "RCPT-20260107-1234"
            )

            assertEquals(method, receipt.paymentMethod)
        }
    }

    @Test
    fun receipt_toString_containsReceiptNumber() {
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        val toString = receipt.toString()

        assertTrue(toString.contains("RCPT-20260107-1234"))
    }

    @Test
    fun receipt_withSpecialCharactersInDescription() {
        val description = "Payment for maintenance & repair (Jan'26) - Blok A No. 12"
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = description,
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        assertEquals(description, receipt.description)
    }

    @Test
    fun receipt_withUnicodeCharacters() {
        val description = "Pembayaran iuran - 维护费用 - 维修費用"
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = description,
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        assertEquals(description, receipt.description)
    }

    @Test
    fun receipt_equality_nullObject() {
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        assertNotEquals(receipt, null)
    }

    @Test
    fun receipt_equality_differentType() {
        val receipt = Receipt(
            id = "receipt-001",
            transactionId = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00"),
            description = "Test payment",
            paymentMethod = "BANK_TRANSFER",
            transactionDate = Date(),
            receiptNumber = "RCPT-20260107-1234"
        )

        assertNotEquals(receipt, "receipt-001")
    }
}