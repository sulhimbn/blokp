package com.example.iurankomplek.payment

import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionDao
import com.example.iurankomplek.transaction.TransactionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking
import java.math.BigDecimal
import java.util.Date

class PaymentProcessingTest {

    private lateinit var mockPaymentGateway: PaymentGateway
    private lateinit var mockTransactionDao: TransactionDao
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var receiptGenerator: ReceiptGenerator

    private val validRequest = PaymentRequest(
        amount = BigDecimal("100.00"),
        description = "Test payment",
        customerId = "test_user",
        paymentMethod = PaymentMethod.CREDIT_CARD
    )

    private val completedResponse = PaymentResponse(
        transactionId = "test_transaction_id",
        status = PaymentStatus.COMPLETED,
        paymentMethod = PaymentMethod.CREDIT_CARD,
        amount = BigDecimal("100.00"),
        currency = "IDR",
        transactionTime = System.currentTimeMillis(),
        referenceNumber = "ref123"
    )

    @Before
    fun setup() {
        mockPaymentGateway = mock(PaymentGateway::class.java)
        mockTransactionDao = mock(TransactionDao::class.java)
        transactionRepository = TransactionRepository(mockPaymentGateway, mockTransactionDao)
        receiptGenerator = ReceiptGenerator()
    }

    @Test
    fun `processPayment stores the transaction and marks it completed on gateway success`() = runBlocking {
        `when`(mockPaymentGateway.processPayment(validRequest))
            .thenReturn(Result.success(completedResponse))

        val result = transactionRepository.processPayment(validRequest)

        assertTrue("expected success but got $result", result.isSuccess)
        assertEquals("100.00", result.getOrNull()?.amount?.toPlainString())

        verifyBlocking(mockTransactionDao, times(1)) { insert(any()) }
        verifyBlocking(mockTransactionDao, times(1)) { update(any()) }
    }

    @Test
    fun `processPayment writes PENDING first then COMPLETED`() = runBlocking {
        `when`(mockPaymentGateway.processPayment(validRequest))
            .thenReturn(Result.success(completedResponse))

        transactionRepository.processPayment(validRequest)

        val inserted = argumentCaptor<Transaction>()
        val updated = argumentCaptor<Transaction>()
        verifyBlocking(mockTransactionDao) { insert(inserted.capture()) }
        verifyBlocking(mockTransactionDao) { update(updated.capture()) }

        assertEquals(PaymentStatus.PENDING, inserted.firstValue.status)
        assertEquals(PaymentStatus.COMPLETED, updated.firstValue.status)
        assertEquals(BigDecimal("100.00"), updated.firstValue.amount)
        assertEquals("test_user", updated.firstValue.userId)
    }

    @Test
    fun `processPayment surfaces the failure and still records the attempt`() = runBlocking {
        `when`(mockPaymentGateway.processPayment(validRequest))
            .thenReturn(Result.failure(Exception("Payment gateway error")))

        val result = transactionRepository.processPayment(validRequest)

        assertTrue("expected failure but got $result", result.isFailure)

        val updated = argumentCaptor<Transaction>()
        verifyBlocking(mockTransactionDao) { insert(any()) }
        verifyBlocking(mockTransactionDao) { update(updated.capture()) }
        assertEquals(PaymentStatus.FAILED, updated.firstValue.status)
    }

    @Test
    fun `processPayment rejects a non-positive amount before touching the gateway`() = runBlocking {
        val result = transactionRepository.processPayment(
            validRequest.copy(amount = BigDecimal.ZERO)
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verifyBlocking(mockPaymentGateway, never()) { processPayment(any()) }
        verifyBlocking(mockTransactionDao, never()) { insert(any()) }
    }

    @Test
    fun `processPayment rejects a blank description`() = runBlocking {
        val result = transactionRepository.processPayment(validRequest.copy(description = "  "))

        assertTrue(result.isFailure)
        assertEquals("Description cannot be blank", result.exceptionOrNull()?.message)
        verifyBlocking(mockTransactionDao, never()) { insert(any()) }
    }

    @Test
    fun `processPayment rejects a blank customer id`() = runBlocking {
        val result = transactionRepository.processPayment(validRequest.copy(customerId = ""))

        assertTrue(result.isFailure)
        assertEquals("Customer ID cannot be blank", result.exceptionOrNull()?.message)
        verifyBlocking(mockTransactionDao, never()) { insert(any()) }
    }

    @Test
    fun `refundPayment delegates to the gateway`() = runBlocking {
        val refund = RefundResponse(
            refundId = "test_refund_id",
            transactionId = "test_transaction_id",
            amount = BigDecimal("100.00"),
            status = RefundStatus.COMPLETED,
            refundTime = System.currentTimeMillis(),
            reason = "Test refund"
        )
        `when`(mockPaymentGateway.refundPayment("test_transaction_id"))
            .thenReturn(Result.success(refund))

        val result = transactionRepository.refundPayment("test_transaction_id", "Test refund")

        assertTrue("expected success but got $result", result.isSuccess)
        assertEquals("test_refund_id", result.getOrNull()?.refundId)
        verifyBlocking(mockPaymentGateway) { refundPayment("test_transaction_id") }
    }

    @Test
    fun `receiptGenerator creates proper receipt from transaction`() {
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

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertNotNull(receipt.id)
        assertEquals(transaction.id, receipt.transactionId)
        assertEquals(transaction.userId, receipt.userId)
        assertEquals(transaction.amount, receipt.amount)
        assertTrue(receipt.receiptNumber.startsWith("RCPT-"))
        assertTrue(receipt.qrCode?.startsWith("QR:") ?: false)
    }

    @Test
    fun `payment request is created with correct defaults`() {
        val request = PaymentRequest(
            amount = BigDecimal("50.00"),
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = PaymentMethod.E_WALLET
        )

        assertEquals(BigDecimal("50.00"), request.amount)
        assertEquals("IDR", request.currency)
        assertEquals("Test payment", request.description)
        assertEquals(PaymentMethod.E_WALLET, request.paymentMethod)
        assertTrue(request.metadata.isEmpty())
    }
}