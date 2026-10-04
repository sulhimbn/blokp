package com.example.iurankomplek.payment

import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionDao
import com.example.iurankomplek.transaction.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.math.BigDecimal
import java.util.Date

class PaymentProcessingTest {

    private class RecordingTransactionDao : TransactionDao {
        val inserted = mutableListOf<Transaction>()
        val updated = mutableListOf<Transaction>()
        var stored: Transaction? = null

        override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(listOfNotNull(stored))

        override suspend fun getTransactionById(id: String): Transaction? = stored?.takeIf { it.id == id }

        override fun getTransactionsByUserId(userId: String): Flow<List<Transaction>> = flowOf(emptyList())

        override suspend fun insert(transaction: Transaction) {
            inserted += transaction
            stored = transaction
        }

        override suspend fun insertAll(transactions: List<Transaction>) {
            inserted += transactions
            stored = transactions.lastOrNull()
        }

        override suspend fun update(transaction: Transaction) {
            updated += transaction
            stored = transaction
        }

        override suspend fun delete(transaction: Transaction) {
            stored = null
        }

        override suspend fun deleteById(id: String) {
            stored = null
        }

        override fun getTransactionsByStatus(status: PaymentStatus): Flow<List<Transaction>> = flowOf(emptyList())
    }

    private lateinit var mockPaymentGateway: PaymentGateway
    private lateinit var dao: RecordingTransactionDao
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var receiptGenerator: ReceiptGenerator

    @Before
    fun setup() {
        mockPaymentGateway = Mockito.mock(PaymentGateway::class.java)
        dao = RecordingTransactionDao()
        transactionRepository = TransactionRepository(mockPaymentGateway, dao)
        receiptGenerator = ReceiptGenerator()
    }

    @Test
    fun `processPayment persists then updates the transaction on success`() = runBlocking {
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
        Mockito.`when`(mockPaymentGateway.processPayment(request)).thenReturn(Result.success(mockResponse))

        val result = transactionRepository.processPayment(request)

        assertTrue("Payment should succeed", result.isSuccess)
        assertEquals("Transaction should be inserted once", 1, dao.inserted.size)
        assertEquals("Transaction should be updated once", 1, dao.updated.size)
        assertEquals(
            "Persisted status should be COMPLETED",
            PaymentStatus.COMPLETED,
            dao.updated.last().status
        )
        assertNotNull("A transaction should be returned", result.getOrNull())
    }

    @Test
    fun `processPayment records FAILED status when the gateway fails`() = runBlocking {
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
        val exception = Exception("Payment failed")
        Mockito.`when`(mockPaymentGateway.processPayment(request)).thenReturn(Result.failure(exception))

        val result = transactionRepository.processPayment(request)

        assertTrue("Payment should fail", result.isFailure)
        assertEquals("Payment should be inserted once", 1, dao.inserted.size)
        assertEquals("Transaction should be updated once", 1, dao.updated.size)
        assertEquals(
            "Persisted status should be FAILED",
            PaymentStatus.FAILED,
            dao.updated.last().status
        )
    }

    @Test
    fun `payment request defaults are sane`() {
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_user",
            paymentMethod = PaymentMethod.E_WALLET
        )

        assertEquals(BigDecimal("100.00"), request.amount)
        assertEquals("Test payment", request.description)
        assertEquals("test_user", request.customerId)
        assertEquals(PaymentMethod.E_WALLET, request.paymentMethod)
        assertTrue(request.metadata.isEmpty())
    }

    @Test
    fun `refundPayment updates the transaction status on success`() = runBlocking {
        val transactionId = "test_transaction_id"
        dao.insert(
            Transaction(
                id = transactionId,
                userId = "test_user",
                amount = BigDecimal("100.00"),
                currency = "IDR",
                description = "Test payment",
                paymentMethod = PaymentMethod.CREDIT_CARD,
                status = PaymentStatus.COMPLETED,
                createdAt = Date(),
                updatedAt = Date(),
                metadata = emptyMap()
            )
        )
        val mockResponse = RefundResponse(
            refundId = "test_refund_id",
            transactionId = transactionId,
            amount = BigDecimal("100.00"),
            status = RefundStatus.COMPLETED,
            refundTime = System.currentTimeMillis(),
            reason = "Test refund"
        )
        Mockito.`when`(mockPaymentGateway.refundPayment(transactionId)).thenReturn(Result.success(mockResponse))

        val result = transactionRepository.refundPayment(transactionId, "Test refund")

        assertTrue("Refund should succeed", result.isSuccess)
        assertEquals("Transaction should be updated once", 1, dao.updated.size)
        assertEquals(
            "Persisted status should be REFUNDED",
            PaymentStatus.REFUNDED,
            dao.updated.last().status
        )
    }

    @Test
    fun `refundPayment surfaces the gateway failure`() = runBlocking {
        val transactionId = "test_transaction_id"
        val exception = Exception("Refund gateway error")
        Mockito.`when`(mockPaymentGateway.refundPayment(transactionId)).thenReturn(Result.failure(exception))

        val result = transactionRepository.refundPayment(transactionId, "Test refund")

        assertTrue("Refund should fail", result.isFailure)
        assertEquals("Refund gateway error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `receiptGenerator builds a receipt from a transaction`() {
        val transaction = Transaction(
            id = "txn_receipt",
            userId = "test_user",
            amount = BigDecimal("100.00"),
            description = "Test payment",
            paymentMethod = PaymentMethod.CREDIT_CARD,
            status = PaymentStatus.COMPLETED,
            currency = "IDR",
            createdAt = Date(),
            updatedAt = Date(),
            metadata = emptyMap()
        )

        val receipt = receiptGenerator.generateReceipt(transaction)

        assertNotNull("Receipt should be generated", receipt)
    }
}
