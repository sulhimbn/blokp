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
import java.math.BigDecimal
import java.util.Date

/**
 * In-memory TransactionDao. Mockito's Java matchers return null, which trips Kotlin's
 * non-null parameter checks, so the DAO is faked instead of mocked.
 */
class RecordingTransactionDao : TransactionDao {
    val inserted = mutableListOf<Transaction>()
    val updated = mutableListOf<Transaction>()
    val deleted = mutableListOf<Transaction>()

    override fun getAllTransactions() = kotlinx.coroutines.flow.flowOf(inserted.toList())

    override suspend fun getTransactionById(id: String): Transaction? =
        (inserted + updated).lastOrNull { it.id == id }

    override fun getTransactionsByUserId(userId: String) =
        kotlinx.coroutines.flow.flowOf(inserted.filter { it.userId == userId })

    override suspend fun insert(transaction: Transaction) {
        inserted += transaction
    }

    override suspend fun insertAll(transactions: List<Transaction>) {
        inserted += transactions
    }

    override suspend fun update(transaction: Transaction) {
        updated += transaction
    }

    override suspend fun delete(transaction: Transaction) {
        deleted += transaction
    }

    override suspend fun deleteById(id: String) {
        inserted.removeAll { it.id == id }
    }

    override fun getTransactionsByStatus(status: PaymentStatus) =
        kotlinx.coroutines.flow.flowOf((inserted + updated).filter { it.status == status })
}

class PaymentProcessingTest {
    private lateinit var mockPaymentGateway: PaymentGateway
    private lateinit var mockTransactionDao: RecordingTransactionDao
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var receiptGenerator: ReceiptGenerator

    @Before
    fun setup() {
        mockPaymentGateway = mock(PaymentGateway::class.java)
        mockTransactionDao = RecordingTransactionDao()
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
        assertEquals(PaymentStatus.PENDING, mockTransactionDao.inserted[0].status)
        assertEquals(1, mockTransactionDao.updated.size)
        assertEquals(PaymentStatus.COMPLETED, mockTransactionDao.updated[0].status)
        assertEquals(PaymentStatus.COMPLETED, result.getOrNull()!!.status)
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
        assertEquals(1, mockTransactionDao.inserted.size)
        assertEquals(1, mockTransactionDao.updated.size) // Status update to FAILED
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
        mockTransactionDao.inserted += Transaction(
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

        // Act
        val result = transactionRepository.refundPayment(transactionId, "Test refund")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.REFUNDED, mockTransactionDao.updated.single().status)
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