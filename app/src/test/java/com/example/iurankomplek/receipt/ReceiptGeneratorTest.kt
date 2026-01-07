package com.example.iurankomplek.receipt

import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.transaction.Transaction
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.util.Date

class ReceiptGeneratorTest {

    private val receiptGenerator = ReceiptGenerator()

    @Test
    fun generateReceipt_createsValidReceipt() {
        val transaction = createTestTransaction(
            id = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000.00")
        )

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertNotNull(receipt)
        assertNotNull(receipt.id)
        assertEquals(transaction.id, receipt.transactionId)
        assertEquals(transaction.userId, receipt.userId)
        assertEquals(transaction.amount, receipt.amount)
        assertEquals(transaction.description, receipt.description)
        assertEquals(transaction.paymentMethod.name, receipt.paymentMethod)
        assertEquals(transaction.createdAt, receipt.transactionDate)
    }

    @Test
    fun generateReceipt_generatesUniqueReceiptId() {
        val transaction1 = createTestTransaction(id = "txn-001")
        val transaction2 = createTestTransaction(id = "txn-002")

        val receipt1 = receiptGenerator.generateReceipt(transaction1)
        val receipt2 = receiptGenerator.generateReceipt(transaction2)

        assertNotEquals(receipt1.id, receipt2.id)
    }

    @Test
    fun generateReceipt_generatesReceiptNumberWithCorrectFormat() {
        val transaction = createTestTransaction()

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertNotNull(receipt.receiptNumber)
        assertTrue("Receipt number should start with 'RCPT-'", receipt.receiptNumber.startsWith("RCPT-"))
        assertTrue("Receipt number should contain date format", receipt.receiptNumber.contains("-"))
    }

    @Test
    fun generateReceipt_generatesValidQRCode() {
        val transaction = createTestTransaction(id = "txn-12345")

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertNotNull(receipt.qrCode)
        assertTrue("QR code should contain transaction ID", receipt.qrCode!!.contains("txn-12345"))
        assertTrue("QR code should start with 'QR:' prefix", receipt.qrCode!!.startsWith("QR:"))
    }

    @Test
    fun generateReceipt_withZeroAmount() {
        val transaction = createTestTransaction(amount = BigDecimal("0.00"))

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals(BigDecimal.ZERO, receipt.amount)
    }

    @Test
    fun generateReceipt_withLargeAmount() {
        val largeAmount = BigDecimal("999999999999.99")
        val transaction = createTestTransaction(amount = largeAmount)

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals(largeAmount, receipt.amount)
    }

    @Test
    fun generateReceipt_withDecimalAmount() {
        val amount = BigDecimal("125000.50")
        val transaction = createTestTransaction(amount = amount)

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals(amount, receipt.amount)
    }

    @Test
    fun generateReceipt_withEmptyDescription() {
        val transaction = createTestTransaction(description = "")

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals("", receipt.description)
    }

    @Test
    fun generateReceipt_withLongDescription() {
        val longDescription = "Payment for monthly maintenance fee covering common area cleaning, security services, and facility maintenance for the month of January 2026"
        val transaction = createTestTransaction(description = longDescription)

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals(longDescription, receipt.description)
    }

    @Test
    fun generateReceipt_withSpecialCharactersInDescription() {
        val description = "Payment for maintenance & repair (Jan'26) - Block A, Unit #123"
        val transaction = createTestTransaction(description = description)

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals(description, receipt.description)
    }

    @Test
    fun generateReceipt_withDifferentPaymentMethods() {
        val methods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.BANK_TRANSFER,
            PaymentMethod.E_WALLET,
            PaymentMethod.CASH
        )

        methods.forEach { method ->
            val transaction = createTestTransaction(paymentMethod = method)
            val receipt = receiptGenerator.generateReceipt(transaction)

            assertEquals(method.name, receipt.paymentMethod)
        }
    }

    @Test
    fun generateReceipt_preservesTransactionTimestamp() {
        val transactionDate = Date(1704625200000L) // Fixed date
        val transaction = createTestTransaction(createdAt = transactionDate)

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals(transactionDate, receipt.transactionDate)
    }

    @Test
    fun generateReceipt_handlesDifferentStatuses() {
        val statuses = listOf(
            PaymentStatus.PENDING,
            PaymentStatus.COMPLETED,
            PaymentStatus.FAILED,
            PaymentStatus.REFUNDED
        )

        statuses.forEach { status ->
            val transaction = createTestTransaction(status = status)
            val receipt = receiptGenerator.generateReceipt(transaction)

            assertEquals(status, transaction.status)
        }
    }

    @Test
    fun generateReceipt_withNullUserId_throwsException() {
        val transaction = Transaction(
            id = "txn-001",
            userId = "",
            amount = BigDecimal("100000.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals("", receipt.userId)
    }

    @Test
    fun generateReceipt_generatesMultipleReceiptsForSameTransaction() {
        val transaction = createTestTransaction(id = "txn-001")

        val receipt1 = receiptGenerator.generateReceipt(transaction)
        val receipt2 = receiptGenerator.generateReceipt(transaction)

        assertEquals(receipt1.transactionId, receipt2.transactionId)
        assertNotEquals(receipt1.id, receipt2.id)
        assertNotEquals(receipt1.receiptNumber, receipt2.receiptNumber)
    }

    @Test
    fun generateReceipt_withNegativeAmount() {
        val transaction = createTestTransaction(amount = BigDecimal("-100000.00"))

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals(BigDecimal("-100000.00"), receipt.amount)
    }

    @Test
    fun generateReceipt_withDifferentCurrencies() {
        val currencies = listOf("IDR", "USD", "EUR", "SGD")

        currencies.forEach { currency ->
            val transaction = createTestTransaction(currency = currency)
            val receipt = receiptGenerator.generateReceipt(transaction)

            assertEquals(transaction.currency, currency)
        }
    }

    @Test
    fun generateReceipt_withUnicodeCharactersInDescription() {
        val description = "Pembayaran iuran rumah Blok A No. 12 - 维护费用 - 维修費用"
        val transaction = createTestTransaction(description = description)

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals(description, receipt.description)
    }

    @Test
    fun generateReceipt_preservesTransactionMetadata() {
        val metadata = mapOf(
            "invoiceNumber" to "INV-2026-001",
            "customerName" to "John Doe",
            "customerEmail" to "john@example.com"
        )
        val transaction = createTestTransaction(metadata = metadata)

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertEquals(metadata, transaction.metadata)
    }

    private fun createTestTransaction(
        id: String = "txn-test-001",
        userId: String = "user-test-001",
        amount: BigDecimal = BigDecimal("100000.00"),
        currency: String = "IDR",
        status: PaymentStatus = PaymentStatus.COMPLETED,
        paymentMethod: PaymentMethod = PaymentMethod.BANK_TRANSFER,
        description: String = "Test payment description",
        createdAt: Date = Date(),
        updatedAt: Date = Date(),
        metadata: Map<String, String> = emptyMap()
    ): Transaction {
        return Transaction(
            id = id,
            userId = userId,
            amount = amount,
            currency = currency,
            status = status,
            paymentMethod = paymentMethod,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            metadata = metadata
        )
    }
}