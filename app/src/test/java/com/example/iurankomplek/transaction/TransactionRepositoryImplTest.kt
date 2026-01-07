package com.example.iurankomplek.transaction

import com.example.iurankomplek.model.PaymentResponse as ApiPaymentResponse
import com.example.iurankomplek.model.PaymentModels
import com.example.iurankomplek.payment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.math.BigDecimal
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionRepositoryImplTest {

    @Mock
    private lateinit var paymentGateway: PaymentGateway

    @Mock
    private lateinit var transactionDao: TransactionDao

    private lateinit var repository: TransactionRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        repository = TransactionRepositoryImpl(paymentGateway, transactionDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initiatePaymentViaApi should return success for CREDIT_CARD payment method`() = runTest {
        val mockPaymentResponse = PaymentResponse(
            transactionId = "txn_123",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            amount = BigDecimal("100.00"),
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        `when`(paymentGateway.processPayment(any())).thenReturn(Result.success(mockPaymentResponse))

        val result = repository.initiatePaymentViaApi(
            amount = "100.00",
            description = "Test payment",
            customerId = "user123",
            paymentMethod = "CREDIT_CARD"
        )

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("txn_123", response?.transactionId)
        assertEquals("COMPLETED", response?.status)
        assertEquals("CREDIT_CARD", response?.paymentMethod)
    }

    @Test
    fun `initiatePaymentViaApi should return success for BANK_TRANSFER payment method`() = runTest {
        val mockPaymentResponse = PaymentResponse(
            transactionId = "txn_456",
            status = PaymentStatus.PROCESSING,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            amount = BigDecimal("500.00"),
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF456"
        )

        `when`(paymentGateway.processPayment(any())).thenReturn(Result.success(mockPaymentResponse))

        val result = repository.initiatePaymentViaApi(
            amount = "500.00",
            description = "Test payment",
            customerId = "user456",
            paymentMethod = "BANK_TRANSFER"
        )

        assertTrue(result.isSuccess)
        assertEquals("BANK_TRANSFER", result.getOrNull()?.paymentMethod)
    }

    @Test
    fun `initiatePaymentViaApi should return success for E_WALLET payment method`() = runTest {
        val mockPaymentResponse = PaymentResponse(
            transactionId = "txn_789",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.E_WALLET,
            amount = BigDecimal("50.00"),
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF789"
        )

        `when`(paymentGateway.processPayment(any())).thenReturn(Result.success(mockPaymentResponse))

        val result = repository.initiatePaymentViaApi(
            amount = "50.00",
            description = "Test payment",
            customerId = "user789",
            paymentMethod = "E_WALLET"
        )

        assertTrue(result.isSuccess)
        assertEquals("E_WALLET", result.getOrNull()?.paymentMethod)
    }

    @Test
    fun `initiatePaymentViaApi should return success for VIRTUAL_ACCOUNT payment method`() = runTest {
        val mockPaymentResponse = PaymentResponse(
            transactionId = "txn_999",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.VIRTUAL_ACCOUNT,
            amount = BigDecimal("250.00"),
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF999"
        )

        `when`(paymentGateway.processPayment(any())).thenReturn(Result.success(mockPaymentResponse))

        val result = repository.initiatePaymentViaApi(
            amount = "250.00",
            description = "Test payment",
            customerId = "user999",
            paymentMethod = "VIRTUAL_ACCOUNT"
        )

        assertTrue(result.isSuccess)
        assertEquals("VIRTUAL_ACCOUNT", result.getOrNull()?.paymentMethod)
    }

    @Test
    fun `initiatePaymentViaApi should default to CREDIT_CARD for unknown payment method`() = runTest {
        val mockPaymentResponse = PaymentResponse(
            transactionId = "txn_000",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            amount = BigDecimal("100.00"),
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF000"
        )

        `when`(paymentGateway.processPayment(any())).thenReturn(Result.success(mockPaymentResponse))

        val result = repository.initiatePaymentViaApi(
            amount = "100.00",
            description = "Test payment",
            customerId = "user000",
            paymentMethod = "UNKNOWN_METHOD"
        )

        assertTrue(result.isSuccess)
        assertEquals("CREDIT_CARD", result.getOrNull()?.paymentMethod)
    }

    @Test
    fun `initiatePaymentViaApi should return failure when payment gateway throws exception`() = runTest {
        `when`(paymentGateway.processPayment(any())).thenThrow(RuntimeException("Payment failed"))

        val result = repository.initiatePaymentViaApi(
            amount = "100.00",
            description = "Test payment",
            customerId = "user123",
            paymentMethod = "CREDIT_CARD"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `processPayment should return success and save transaction with COMPLETED status`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "user123",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
        val mockPaymentResponse = PaymentResponse(
            transactionId = "txn_123",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            amount = BigDecimal("100.00"),
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        `when`(paymentGateway.processPayment(request)).thenReturn(Result.success(mockPaymentResponse))

        val result = repository.processPayment(request)

        assertTrue(result.isSuccess)
        verify(transactionDao).insert(any())
        verify(transactionDao).update(argThat { it.status == PaymentStatus.COMPLETED })
    }

    @Test
    fun `processPayment should update transaction to FAILED when payment gateway returns failure`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "user123",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        `when`(paymentGateway.processPayment(request)).thenReturn(Result.failure(RuntimeException("Payment failed")))

        val result = repository.processPayment(request)

        assertTrue(result.isFailure)
        verify(transactionDao).insert(any())
        verify(transactionDao).update(argThat { it.status == PaymentStatus.FAILED })
    }

    @Test
    fun `processPayment should return exception on error`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "user123",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        `when`(paymentGateway.processPayment(request)).thenThrow(RuntimeException("Unexpected error"))

        val result = repository.processPayment(request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getTransactionById should return transaction when exists`() = runTest {
        val mockTransaction = Transaction(
            id = "txn_123",
            userId = "user123",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        `when`(transactionDao.getTransactionById("txn_123")).thenReturn(mockTransaction)

        val result = repository.getTransactionById("txn_123")

        assertNotNull(result)
        assertEquals("txn_123", result?.id)
        assertEquals("user123", result?.userId)
    }

    @Test
    fun `getTransactionById should return null when transaction not found`() = runTest {
        `when`(transactionDao.getTransactionById("non_existent")).thenReturn(null)

        val result = repository.getTransactionById("non_existent")

        assertNull(result)
    }

    @Test
    fun `getTransactionsByUserId should return flow of transactions`() = runTest {
        val mockTransactions = listOf(
            Transaction(
                id = "txn_1",
                userId = "user123",
                amount = BigDecimal("100.00"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                description = "Payment 1",
                createdAt = Date(),
                updatedAt = Date()
            ),
            Transaction(
                id = "txn_2",
                userId = "user123",
                amount = BigDecimal("200.00"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                description = "Payment 2",
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        val flow = kotlinx.coroutines.flow.flowOf(mockTransactions)
        `when`(transactionDao.getTransactionsByUserId("user123")).thenReturn(flow)

        val result = repository.getTransactionsByUserId("user123").first()

        assertEquals(2, result.size)
        assertEquals("txn_1", result[0].id)
        assertEquals("txn_2", result[1].id)
    }

    @Test
    fun `getTransactionsByUserId should return empty list for user with no transactions`() = runTest {
        val flow = kotlinx.coroutines.flow.flowOf(emptyList<Transaction>())
        `when`(transactionDao.getTransactionsByUserId("user456")).thenReturn(flow)

        val result = repository.getTransactionsByUserId("user456").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTransactionsByStatus should return flow of transactions with matching status`() = runTest {
        val mockTransactions = listOf(
            Transaction(
                id = "txn_1",
                userId = "user123",
                amount = BigDecimal("100.00"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                description = "Payment 1",
                createdAt = Date(),
                updatedAt = Date()
            ),
            Transaction(
                id = "txn_2",
                userId = "user456",
                amount = BigDecimal("200.00"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                description = "Payment 2",
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        val flow = kotlinx.coroutines.flow.flowOf(mockTransactions)
        `when`(transactionDao.getTransactionsByStatus(PaymentStatus.COMPLETED)).thenReturn(flow)

        val result = repository.getTransactionsByStatus(PaymentStatus.COMPLETED).first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.status == PaymentStatus.COMPLETED })
    }

    @Test
    fun `getTransactionsByStatus should return empty list for status with no transactions`() = runTest {
        val flow = kotlinx.coroutines.flow.flowOf(emptyList<Transaction>())
        `when`(transactionDao.getTransactionsByStatus(PaymentStatus.FAILED)).thenReturn(flow)

        val result = repository.getTransactionsByStatus(PaymentStatus.FAILED).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `updateTransaction should call dao update`() = runTest {
        val transaction = Transaction(
            id = "txn_123",
            userId = "user123",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        repository.updateTransaction(transaction)

        verify(transactionDao).update(transaction)
    }

    @Test
    fun `deleteTransaction should call dao delete`() = runTest {
        val transaction = Transaction(
            id = "txn_123",
            userId = "user123",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        repository.deleteTransaction(transaction)

        verify(transactionDao).delete(transaction)
    }

    @Test
    fun `refundPayment should update transaction status to REFUNDED on success`() = runTest {
        val transactionId = "txn_123"
        val mockTransaction = Transaction(
            id = transactionId,
            userId = "user123",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )
        val mockRefundResponse = RefundResponse(
            refundId = "refund_123",
            transactionId = transactionId,
            amount = BigDecimal("100.00"),
            status = RefundStatus.COMPLETED,
            refundTime = System.currentTimeMillis(),
            reason = "Customer request"
        )

        `when`(transactionDao.getTransactionById(transactionId)).thenReturn(mockTransaction)
        `when`(paymentGateway.refundPayment(transactionId)).thenReturn(Result.success(mockRefundResponse))

        val result = repository.refundPayment(transactionId, "Customer request")

        assertTrue(result.isSuccess)
        verify(transactionDao).update(argThat { it.status == PaymentStatus.REFUNDED })
    }

    @Test
    fun `refundPayment should return failure when transaction not found`() = runTest {
        val transactionId = "non_existent"

        `when`(transactionDao.getTransactionById(transactionId)).thenReturn(null)
        `when`(paymentGateway.refundPayment(transactionId)).thenReturn(
            Result.success(
                RefundResponse(
                    refundId = "refund_123",
                    transactionId = transactionId,
                    amount = BigDecimal("100.00"),
                    status = RefundStatus.COMPLETED,
                    refundTime = System.currentTimeMillis()
                )
            )
        )

        val result = repository.refundPayment(transactionId, "Customer request")

        assertTrue(result.isSuccess)
        verify(transactionDao, never()).update(any())
    }

    @Test
    fun `refundPayment should return failure when payment gateway returns failure`() = runTest {
        val transactionId = "txn_123"
        val mockTransaction = Transaction(
            id = transactionId,
            userId = "user123",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        `when`(transactionDao.getTransactionById(transactionId)).thenReturn(mockTransaction)
        `when`(paymentGateway.refundPayment(transactionId)).thenReturn(
            Result.failure(RuntimeException("Refund failed"))
        )

        val result = repository.refundPayment(transactionId, "Customer request")

        assertTrue(result.isFailure)
        verify(transactionDao, never()).update(any())
    }

    @Test
    fun `refundPayment should return failure on exception`() = runTest {
        val transactionId = "txn_123"

        `when`(paymentGateway.refundPayment(transactionId)).thenThrow(RuntimeException("Unexpected error"))

        val result = repository.refundPayment(transactionId, "Customer request")

        assertTrue(result.isFailure)
    }

    @Test
    fun `refundPayment should handle null reason parameter`() = runTest {
        val transactionId = "txn_123"
        val mockTransaction = Transaction(
            id = transactionId,
            userId = "user123",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )
        val mockRefundResponse = RefundResponse(
            refundId = "refund_123",
            transactionId = transactionId,
            amount = BigDecimal("100.00"),
            status = RefundStatus.COMPLETED,
            refundTime = System.currentTimeMillis()
        )

        `when`(transactionDao.getTransactionById(transactionId)).thenReturn(mockTransaction)
        `when`(paymentGateway.refundPayment(transactionId)).thenReturn(Result.success(mockRefundResponse))

        val result = repository.refundPayment(transactionId, null)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `processPayment should handle zero amount`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal.ZERO,
            description = "Zero amount payment",
            customerId = "user123",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
        val mockPaymentResponse = PaymentResponse(
            transactionId = "txn_000",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            amount = BigDecimal.ZERO,
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF000"
        )

        `when`(paymentGateway.processPayment(request)).thenReturn(Result.success(mockPaymentResponse))

        val result = repository.processPayment(request)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `processPayment should handle very large amount`() = runTest {
        val largeAmount = BigDecimal("999999999.99")
        val request = PaymentRequest(
            amount = largeAmount,
            description = "Large amount payment",
            customerId = "user123",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
        val mockPaymentResponse = PaymentResponse(
            transactionId = "txn_large",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            amount = largeAmount,
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF_LARGE"
        )

        `when`(paymentGateway.processPayment(request)).thenReturn(Result.success(mockPaymentResponse))

        val result = repository.processPayment(request)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `processPayment should create transaction with PENDING status initially`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "user123",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        `when`(paymentGateway.processPayment(request)).thenReturn(
            Result.success(
                PaymentResponse(
                    transactionId = "txn_123",
                    status = PaymentStatus.COMPLETED,
                    paymentMethod = PaymentMethod.CREDIT_CARD,
                    amount = BigDecimal("100.00"),
                    currency = "IDR",
                    transactionTime = System.currentTimeMillis(),
                    referenceNumber = "REF123"
                )
            )
        )

        repository.processPayment(request)

        verify(transactionDao).insert(argThat { it.status == PaymentStatus.PENDING })
    }
}