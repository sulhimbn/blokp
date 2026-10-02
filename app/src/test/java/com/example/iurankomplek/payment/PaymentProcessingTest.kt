package com.example.iurankomplek.payment

import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionDao
import com.example.iurankomplek.transaction.TransactionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.wheneverBlocking
import java.math.BigDecimal
import java.util.Date

class PaymentProcessingTest {
    private lateinit var paymentGateway: PaymentGateway
    private lateinit var transactionDao: TransactionDao
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var receiptGenerator: ReceiptGenerator

    private val request = PaymentRequest(
        amount = BigDecimal("100.00"),
        description = "Test payment",
        customerId = "test_user",
        paymentMethod = PaymentMethod.CREDIT_CARD
    )

    @Before
    fun setup() {
        paymentGateway = mock()
        transactionDao = mock()
        transactionRepository = TransactionRepository(paymentGateway, transactionDao)
        receiptGenerator = ReceiptGenerator()
    }

    private fun completedResponse() = PaymentResponse(
        transactionId = "test_transaction_id",
        status = PaymentStatus.COMPLETED,
        paymentMethod = PaymentMethod.CREDIT_CARD,
        amount = BigDecimal("100.00"),
        currency = "IDR",
        transactionTime = System.currentTimeMillis(),
        referenceNumber = "ref123"
    )

    @Test
    fun `processPayment inserts a pending row then marks it COMPLETED`() = runBlocking {
        wheneverBlocking { paymentGateway.processPayment(request) }
            .thenReturn(Result.success(completedResponse()))

        val result = transactionRepository.processPayment(request)

        assertTrue(result.isSuccess)
        verifyBlocking(transactionDao, Mockito.times(1)) { transactionDao.insert(any<Transaction>()) }
        val updated = argumentCaptor<Transaction>()
        verifyBlocking(transactionDao, Mockito.times(1)) { transactionDao.update(updated.capture()) }
        assertEquals(PaymentStatus.COMPLETED, updated.firstValue.status)
    }

    @Test
    fun `processPayment propagates the gateway failure and marks the row FAILED`() = runBlocking {
        wheneverBlocking { paymentGateway.processPayment(request) }
            .thenReturn(Result.failure(Exception("Payment gateway error")))

        val result = transactionRepository.processPayment(request)

        assertTrue(result.isFailure)
        verifyBlocking(transactionDao, Mockito.times(1)) { transactionDao.insert(any<Transaction>()) }
        val updated = argumentCaptor<Transaction>()
        verifyBlocking(transactionDao, Mockito.times(1)) { transactionDao.update(updated.capture()) }
        assertEquals(PaymentStatus.FAILED, updated.firstValue.status)
    }

    @Test
    fun `processPayment rejects a non positive amount before touching the gateway`() = runBlocking {
        val bad = request.copy(amount = BigDecimal.ZERO)

        val result = transactionRepository.processPayment(bad)

        assertTrue(result.isFailure)
        assertEquals("Amount must be greater than zero", result.exceptionOrNull()?.message)
        verifyBlocking(transactionDao, Mockito.never()) { transactionDao.insert(any<Transaction>()) }
    }

    @Test
    fun `processPayment rejects a blank description`() = runBlocking {
        val result = transactionRepository.processPayment(request.copy(description = "   "))

        assertTrue(result.isFailure)
        assertEquals("Description cannot be blank", result.exceptionOrNull()?.message)
        verifyBlocking(transactionDao, Mockito.never()) { transactionDao.insert(any<Transaction>()) }
    }

    @Test
    fun `processPayment rejects a blank customer id`() = runBlocking {
        val result = transactionRepository.processPayment(request.copy(customerId = ""))

        assertTrue(result.isFailure)
        assertEquals("Customer ID cannot be blank", result.exceptionOrNull()?.message)
        verifyBlocking(transactionDao, Mockito.never()) { transactionDao.insert(any<Transaction>()) }
    }

    @Test
    fun `refundPayment marks the stored transaction REFUNDED`() = runBlocking {
        val original = Transaction(
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
        wheneverBlocking { paymentGateway.refundPayment("test_transaction_id") }
            .thenReturn(
                Result.success(
                    RefundResponse(
                        refundId = "test_refund_id",
                        transactionId = "test_transaction_id",
                        amount = BigDecimal("100.00"),
                        status = RefundStatus.COMPLETED,
                        refundTime = System.currentTimeMillis(),
                        reason = "Test refund"
                    )
                )
            )
        wheneverBlocking { transactionDao.getTransactionById("test_transaction_id") }
            .thenReturn(original)

        val result = transactionRepository.refundPayment("test_transaction_id", "Test refund")

        assertTrue(result.isSuccess)
        val updated = argumentCaptor<Transaction>()
        verifyBlocking(transactionDao, Mockito.times(1)) { transactionDao.update(updated.capture()) }
        assertEquals(PaymentStatus.REFUNDED, updated.firstValue.status)
    }

    @Test
    fun `refundPayment succeeds without a stored row and writes nothing`() = runBlocking {
        wheneverBlocking { paymentGateway.refundPayment("missing") }
            .thenReturn(
                Result.success(
                    RefundResponse(
                        refundId = "r1",
                        transactionId = "missing",
                        amount = BigDecimal("100.00"),
                        status = RefundStatus.COMPLETED,
                        refundTime = System.currentTimeMillis(),
                        reason = null
                    )
                )
            )
        wheneverBlocking { transactionDao.getTransactionById("missing") }.thenReturn(null)

        val result = transactionRepository.refundPayment("missing", null)

        assertTrue(result.isSuccess)
        verifyBlocking(transactionDao, Mockito.never()) { transactionDao.update(any<Transaction>()) }
    }

    @Test
    fun `refundPayment surfaces the gateway failure`() = runBlocking {
        wheneverBlocking { paymentGateway.refundPayment("test_transaction_id") }
            .thenReturn(Result.failure(Exception("Refund gateway error")))

        val result = transactionRepository.refundPayment("test_transaction_id", "Test refund")

        assertTrue(result.isFailure)
        assertEquals("Refund gateway error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getTransactionById returns null when the row is absent`() = runBlocking {
        wheneverBlocking { transactionDao.getTransactionById("nope") }.thenReturn(null)

        assertNull(transactionRepository.getTransactionById("nope"))
    }

    @Test
    fun `receiptGenerator builds a receipt that mirrors the transaction`() {
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
        assertTrue("expected a QR payload", receipt.qrCode?.startsWith("QR:") == true)
    }

    @Test
    fun `payment request defaults to IDR with no metadata`() {
        assertEquals("IDR", request.currency)
        assertEquals(BigDecimal("100.00"), request.amount)
        assertEquals("Test payment", request.description)
        assertEquals(PaymentMethod.CREDIT_CARD, request.paymentMethod)
        assertTrue(request.metadata.isEmpty())
    }

    @Test
    fun `initiatePaymentViaApi normalises a bank transfer method`() = runBlocking {
        wheneverBlocking {
            paymentGateway.processPayment(
                eq(request.copy(paymentMethod = PaymentMethod.BANK_TRANSFER))
            )
        }.thenReturn(Result.success(completedResponse()))

        val result = transactionRepository.initiatePaymentViaApi(
            amount = "100.00",
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = "BANK_TRANSFER"
        )

        assertTrue(result.isSuccess)
        assertEquals("ref123", result.getOrNull()?.referenceNumber)
    }
}
