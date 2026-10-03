package com.example.iurankomplek.payment

import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionDao
import com.example.iurankomplek.transaction.TransactionRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.util.Date

class PaymentProcessingTest {
    private lateinit var mockPaymentGateway: PaymentGateway
    private lateinit var mockTransactionDao: TransactionDao
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var receiptGenerator: ReceiptGenerator
    private fun persistedStatuses(name: String): List<PaymentStatus> =
        Mockito.mockingDetails(mockTransactionDao).invocations
            .filter { it.method.name == name }
            .map { (it.arguments[0] as Transaction).status }

    @Before
    fun setup() {
        mockPaymentGateway = mock(PaymentGateway::class.java)
        mockTransactionDao = mock(TransactionDao::class.java)
        // Assertions read the recorded invocations directly: an argument matcher returns
        // null, which Kotlin's non-null check on the DAO parameter rejects.
        transactionRepository = TransactionRepository(mockPaymentGateway, mockTransactionDao)
        receiptGenerator = ReceiptGenerator()
    }

    @Test
    fun `processPayment successfully processes payment and updates transaction status`() = runBlocking {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
        
        val mockResponse = PaymentResponse(
            transactionId = "test_transaction_id",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            amount = BigDecimal("100.00"),
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "ref123"
        )
        
        `when`(mockPaymentGateway.processPayment(request)).thenReturn(Result.success(mockResponse))
        
        // Act
        val result = transactionRepository.processPayment(request)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(
            listOf(PaymentStatus.PENDING, PaymentStatus.COMPLETED),
            persistedStatuses("insert") + persistedStatuses("update")
        )
    }

    @Test
    fun `processPayment handles failure and updates transaction status to FAILED`() = runBlocking {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
        
        val exception = Exception("Payment gateway error")
        `when`(mockPaymentGateway.processPayment(request)).thenReturn(Result.failure(exception))
        
        // Act
        val result = transactionRepository.processPayment(request)
        
        // Assert
        assertTrue(result.isFailure)
        assertEquals(listOf(PaymentStatus.PENDING), persistedStatuses("insert")) // Initial insert
        assertEquals(
            listOf(PaymentStatus.PENDING, PaymentStatus.FAILED),
            persistedStatuses("insert") + persistedStatuses("update")
        )
    }

    @Test
    fun `receiptGenerator creates proper receipt from transaction`() {
        // Arrange
        val transaction = Transaction(
            id = "test_transaction_id",
            userId = "test_user",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            description = "Test transaction",
            createdAt = Date(),
            updatedAt = Date()
        )

        // Act
        val receipt = receiptGenerator.generateReceipt(transaction)

        // Assert
        assertNotNull(receipt.id)
        assertEquals(transaction.id, receipt.transactionId)
        assertEquals(transaction.userId, receipt.userId)
        assertEquals(transaction.amount, receipt.amount)
        assertTrue(receipt.receiptNumber.startsWith("RCPT-"))
        assertTrue(receipt.qrCode?.startsWith("QR:") ?: false)
    }

    @Test
    fun `receipt number carries an eight digit date and a four digit suffix`() {
        // The date must come from a formatter available on minSdk 24; java.time would throw
        // on Android 7.x and the prefix-only assertion above would not have caught it.
        val transaction = Transaction(
            id = "txn_format",
            userId = "test_user",
            amount = BigDecimal("10.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            description = "Test transaction",
            createdAt = Date(),
            updatedAt = Date()
        )

        val receiptNumber = receiptGenerator.generateReceipt(transaction).receiptNumber

        assertTrue(
            "expected RCPT-yyyyMMdd-nnnn but got $receiptNumber",
            Regex("^RCPT-\\d{8}-\\d{4}$").matches(receiptNumber)
        )
    }

    @Test
    fun `payment request is created with correct defaults`() {
        // Arrange & Act
        val request = PaymentRequest(
            amount = BigDecimal("50.00"),
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = PaymentMethod.E_WALLET
        )

        // Assert
        assertEquals(BigDecimal("50.00"), request.amount)
        assertEquals("IDR", request.currency) // Default currency
        assertEquals("Test payment", request.description)
        assertEquals(PaymentMethod.E_WALLET, request.paymentMethod)
        assertTrue(request.metadata.isEmpty())
    }

    @Test
    fun `refundPayment successfully processes refund and updates transaction status`() = runBlocking {
        // Arrange
        val transactionId = "test_transaction_id"
        val mockResponse = RefundResponse(
            refundId = "test_refund_id",
            transactionId = transactionId,
            amount = BigDecimal("100.00"),
            status = RefundStatus.COMPLETED,
            refundTime = System.currentTimeMillis(),
            reason = "Test refund"
        )

        val storedTransaction = Transaction(
            id = transactionId,
            userId = "test_user",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test transaction",
            createdAt = Date(),
            updatedAt = Date()
        )

        `when`(mockPaymentGateway.refundPayment(transactionId)).thenReturn(Result.success(mockResponse))
        `when`(mockTransactionDao.getTransactionById(transactionId)).thenReturn(storedTransaction)

        // Act
        val result = transactionRepository.refundPayment(transactionId, "Test refund")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(listOf(PaymentStatus.REFUNDED), persistedStatuses("update"))
    }

    @Test
    fun `refundPayment handles failure gracefully`() = runBlocking {
        // Arrange
        val transactionId = "test_transaction_id"
        val exception = Exception("Refund gateway error")
        `when`(mockPaymentGateway.refundPayment(transactionId)).thenReturn(Result.failure(exception))

        // Act
        val result = transactionRepository.refundPayment(transactionId, "Test refund")

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Refund gateway error", result.exceptionOrNull()?.message)
    }
}