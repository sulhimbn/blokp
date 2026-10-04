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
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import java.math.BigDecimal
import java.util.Date

class PaymentProcessingTest {
    private lateinit var mockPaymentGateway: PaymentGateway
    private lateinit var mockTransactionDao: TransactionDao
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var receiptGenerator: ReceiptGenerator

    @Before
    fun setup() {
        mockPaymentGateway = mock(PaymentGateway::class.java)
        mockTransactionDao = mock(TransactionDao::class.java)
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
        verify(mockTransactionDao, times(1)).insert(any<Transaction>())
        verify(mockTransactionDao, times(1)).update(any<Transaction>())
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
        verify(mockTransactionDao, times(1)).insert(any<Transaction>())
        verify(mockTransactionDao, times(1)).update(any<Transaction>())
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

        `when`(mockPaymentGateway.refundPayment(transactionId)).thenReturn(Result.success(mockResponse))
        `when`(mockTransactionDao.getTransactionById(transactionId)).thenReturn(
            Transaction(
                id = transactionId,
                userId = "test_user",
                amount = BigDecimal("100.00"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                description = "Test payment",
                createdAt = Date(0),
                updatedAt = Date(0)
            )
        )

        // Act
        val result = transactionRepository.refundPayment(transactionId, "Test refund")

        // Assert
        assertTrue(result.isSuccess)
        verify(mockTransactionDao, times(1)).update(any<Transaction>())
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

    @Test
    fun `refundPayment leaves the ledger untouched when the original is unknown`() = runBlocking {
        val transactionId = "missing_transaction"
        val mockResponse = RefundResponse(
            refundId = "refund-1",
            transactionId = transactionId,
            amount = BigDecimal("100.00"),
            status = RefundStatus.COMPLETED,
            refundTime = System.currentTimeMillis(),
            reason = "Test refund"
        )
        `when`(mockPaymentGateway.refundPayment(transactionId)).thenReturn(Result.success(mockResponse))
        `when`(mockTransactionDao.getTransactionById(transactionId)).thenReturn(null)

        val result = transactionRepository.refundPayment(transactionId, "Test refund")

        assertTrue(result.isSuccess)
        verify(mockTransactionDao, never()).update(any<Transaction>())
    }

    @Test
    fun `receipt number is ISO dated and carries four random digits`() {
        val generator = ReceiptGenerator()
        val transaction = Transaction(
            id = "txn-receipt",
            userId = "test_user",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(0),
            updatedAt = Date(0)
        )

        val receipt = generator.generateReceipt(transaction)
        val pattern = Regex("^RCPT-\\d{8}-\\d{4}$")

        assertTrue("unexpected receipt number: ${receipt.receiptNumber}", pattern.matches(receipt.receiptNumber))
    }

    @Test
    fun `receipt is linked to the transaction it was generated from`() {
        val generator = ReceiptGenerator()
        val transaction = Transaction(
            id = "txn-link",
            userId = "test_user",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(0),
            updatedAt = Date(0)
        )

        val receipt = generator.generateReceipt(transaction)

        assertEquals("txn-link", receipt.transactionId)
        assertEquals("test_user", receipt.userId)
        assertEquals(BigDecimal("100.00"), receipt.amount)
    }
}
