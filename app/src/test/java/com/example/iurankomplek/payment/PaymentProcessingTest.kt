package com.example.iurankomplek.payment

import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionDao
import com.example.iurankomplek.transaction.TransactionRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.Date

/**
 * PaymentGateway and TransactionDao are suspend-only interfaces, and the inline
 * Mockito helpers for coroutines cannot be compiled against this module's JVM 1.8
 * target, so both collaborators are faked by hand.
 */
private class FakePaymentGateway : PaymentGateway {
    var processResult: Result<PaymentResponse> =
        Result.failure(UnsupportedOperationException())
    var refundResult: Result<RefundResponse> =
        Result.failure(UnsupportedOperationException())

    override suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse> =
        processResult

    override suspend fun refundPayment(transactionId: String): Result<RefundResponse> =
        refundResult

    override suspend fun getPaymentStatus(transactionId: String): Result<PaymentStatus> =
        Result.success(PaymentStatus.PENDING)
}

private class FakeTransactionDao : TransactionDao {
    val byIdRef: MutableMap<String, Transaction> = mutableMapOf()
    var insertCount = 0
    var updateCount = 0
    var lastInserted: Transaction? = null
    var lastUpdated: Transaction? = null

    override fun getAllTransactions(): Flow<List<Transaction>> = flowOf(byIdRef.values.toList())

    override suspend fun getTransactionById(id: String): Transaction? = byIdRef[id]

    override fun getTransactionsByUserId(userId: String): Flow<List<Transaction>> =
        flowOf(byIdRef.values.filter { it.userId == userId })

    override suspend fun insert(transaction: Transaction) {
        insertCount++
        lastInserted = transaction
    }

    override suspend fun insertAll(transactions: List<Transaction>) {
        insertCount += transactions.size
    }

    override suspend fun update(transaction: Transaction) {
        updateCount++
        lastUpdated = transaction
    }

    override suspend fun delete(transaction: Transaction) {
        byIdRef.remove(transaction.id)
    }

    override suspend fun deleteById(id: String) {
        byIdRef.remove(id)
    }

    override fun getTransactionsByStatus(
        status: com.example.iurankomplek.payment.PaymentStatus
    ): Flow<List<Transaction>> = flowOf(byIdRef.values.filter { it.status == status })
}

class PaymentProcessingTest {
    private lateinit var paymentGateway: FakePaymentGateway
    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var receiptGenerator: ReceiptGenerator

    @Before
    fun setup() {
        paymentGateway = FakePaymentGateway()
        transactionDao = FakeTransactionDao()
        transactionRepository = TransactionRepository(paymentGateway, transactionDao)
        receiptGenerator = ReceiptGenerator()
    }

    private fun request() = PaymentRequest(
        amount = BigDecimal("100.00"),
        description = "Test payment",
        customerId = "test_user",
        paymentMethod = PaymentMethod.CREDIT_CARD
    )

    @Test
    fun `processPayment successfully processes payment and updates transaction status`() =
        runBlocking {
            paymentGateway.processResult = Result.success(
                PaymentResponse(
                    transactionId = "test_transaction_id",
                    status = PaymentStatus.COMPLETED,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    amount = BigDecimal("100.00"),
                    currency = "IDR",
                    transactionTime = System.currentTimeMillis(),
                    referenceNumber = "ref123"
                )
            )

            val result = transactionRepository.processPayment(request())

            assertTrue(result.isSuccess)
            // The repository returns the created transaction; the status flip is persisted.
            assertEquals(transactionDao.lastInserted?.id, result.getOrNull()?.id)
            assertEquals(1, transactionDao.insertCount)
            assertEquals(1, transactionDao.updateCount)
            assertEquals(PaymentStatus.COMPLETED, transactionDao.lastUpdated?.status)
        }

    @Test
    fun `processPayment handles failure and updates transaction status to FAILED`() =
        runBlocking {
            paymentGateway.processResult =
                Result.failure(Exception("Payment gateway error"))

            val result = transactionRepository.processPayment(request())

            assertTrue(result.isFailure)
            assertEquals(1, transactionDao.insertCount)
            assertEquals(1, transactionDao.updateCount)
            assertEquals(PaymentStatus.FAILED, transactionDao.lastUpdated?.status)
        }

    @Test
    fun `processPayment rejects a non-positive amount without calling the gateway`() =
        runBlocking {
            val invalid = request().copy(amount = BigDecimal.ZERO)

            val result = transactionRepository.processPayment(invalid)

            assertTrue(result.isFailure)
            assertEquals(0, transactionDao.insertCount)
            assertEquals(0, transactionDao.updateCount)
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

    @Test
    fun `refundPayment successfully processes refund and updates transaction status`() =
        runBlocking {
            val transactionId = "test_transaction_id"
            transactionDao.byIdRef[transactionId] = Transaction(
                id = transactionId,
                userId = "test_user",
                amount = BigDecimal("100.00"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                description = "Test transaction",
                createdAt = Date(),
                updatedAt = Date()
            )
            paymentGateway.refundResult = Result.success(
                RefundResponse(
                    refundId = "test_refund_id",
                    transactionId = transactionId,
                    amount = BigDecimal("100.00"),
                    status = RefundStatus.COMPLETED,
                    refundTime = System.currentTimeMillis(),
                    reason = "Test refund"
                )
            )

            val result = transactionRepository.refundPayment(transactionId, "Test refund")

            assertTrue(result.isSuccess)
            assertEquals(1, transactionDao.updateCount)
            assertEquals(PaymentStatus.REFUNDED, transactionDao.lastUpdated?.status)
        }

    @Test
    fun `refundPayment handles failure gracefully`() = runBlocking {
        paymentGateway.refundResult = Result.failure(Exception("Refund gateway error"))

        val result =
            transactionRepository.refundPayment("test_transaction_id", "Test refund")

        assertTrue(result.isFailure)
        assertEquals("Refund gateway error", result.exceptionOrNull()?.message)
        assertEquals(0, transactionDao.updateCount)
    }
}