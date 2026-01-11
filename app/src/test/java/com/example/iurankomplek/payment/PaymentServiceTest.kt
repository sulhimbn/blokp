package com.example.iurankomplek.payment

import com.example.iurankomplek.receipt.Receipt
import com.example.iurankomplek.receipt.ReceiptGenerator
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Date

@ExperimentalCoroutinesApi
class PaymentServiceTest {

    @Mock
    private lateinit var mockTransactionRepository: TransactionRepository

    @Mock
    private lateinit var mockReceiptGenerator: ReceiptGenerator

    private lateinit var paymentService: PaymentService

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        paymentService = PaymentService(mockTransactionRepository, mockReceiptGenerator)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `processPayment calls onSuccess with receipt when payment succeeds`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        val description = "Test payment"
        val customerId = "user123"
        val paymentMethod = PaymentMethod.CREDIT_CARD

        val transaction = Transaction(
            id = "test_transaction_id",
            userId = customerId,
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = paymentMethod,
            description = description,
            createdAt = Date(),
            updatedAt = Date()
        )

        val receipt = Receipt(
            id = "receipt_123",
            receiptNumber = "RCPT-001",
            transactionId = transaction.id,
            userId = customerId,
            amount = amount,
            currency = "IDR",
            paymentMethod = paymentMethod,
            generatedAt = Date()
        )

        whenever(mockTransactionRepository.processPayment(any()))
            .thenReturn(Result.success(transaction))
        whenever(mockReceiptGenerator.generateReceipt(transaction))
            .thenReturn(receipt)

        var onSuccessCalled = false
        var onErrorCalled = false
        var receivedReceipt: Receipt? = null

        // Act
        paymentService.processPayment(
            amount = amount,
            description = description,
            customerId = customerId,
            paymentMethod = paymentMethod,
            onSuccess = { r ->
                onSuccessCalled = true
                receivedReceipt = r
            },
            onError = { _ ->
                onErrorCalled = true
            }
        )
        advanceUntilIdle()

        // Assert
        assertTrue(onSuccessCalled)
        assertFalse(onErrorCalled)
        assertNotNull(receivedReceipt)
        assertEquals(receipt.id, receivedReceipt?.id)
        assertEquals(receipt.receiptNumber, receivedReceipt?.receiptNumber)
    }

    @Test
    fun `processPayment calls onError when payment fails`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        val description = "Test payment"
        val customerId = "user123"
        val paymentMethod = PaymentMethod.CREDIT_CARD

        val error = Exception("Payment gateway error")

        whenever(mockTransactionRepository.processPayment(any()))
            .thenReturn(Result.failure(error))

        var onSuccessCalled = false
        var onErrorCalled = false
        var errorMessage: String? = null

        // Act
        paymentService.processPayment(
            amount = amount,
            description = description,
            customerId = customerId,
            paymentMethod = paymentMethod,
            onSuccess = { _ ->
                onSuccessCalled = true
            },
            onError = { msg ->
                onErrorCalled = true
                errorMessage = msg
            }
        )
        advanceUntilIdle()

        // Assert
        assertFalse(onSuccessCalled)
        assertTrue(onErrorCalled)
        assertEquals("Payment gateway error", errorMessage)
    }

    @Test
    fun `processPayment generates receipt after successful payment`() = runTest {
        // Arrange
        val amount = BigDecimal("50.00")
        val description = "Test payment"
        val customerId = "user123"
        val paymentMethod = PaymentMethod.BANK_TRANSFER

        val transaction = Transaction(
            id = "test_transaction_id",
            userId = customerId,
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = paymentMethod,
            description = description,
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(mockTransactionRepository.processPayment(any()))
            .thenReturn(Result.success(transaction))

        // Act
        paymentService.processPayment(
            amount = amount,
            description = description,
            customerId = customerId,
            paymentMethod = paymentMethod,
            onSuccess = { _ -> },
            onError = { _ -> }
        )
        advanceUntilIdle()

        // Assert
        verify(mockReceiptGenerator).generateReceipt(transaction)
    }

    @Test
    fun `processPayment creates correct payment request`() = runTest {
        // Arrange
        val amount = BigDecimal("75.50")
        val description = "HOA Payment"
        val customerId = "user456"
        val paymentMethod = PaymentMethod.E_WALLET

        val transaction = Transaction(
            id = "test_transaction_id",
            userId = customerId,
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = paymentMethod,
            description = description,
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(mockTransactionRepository.processPayment(any()))
            .thenReturn(Result.success(transaction))

        // Act
        paymentService.processPayment(
            amount = amount,
            description = description,
            customerId = customerId,
            paymentMethod = paymentMethod,
            onSuccess = { _ -> },
            onError = { _ -> }
        )
        advanceUntilIdle()

        // Assert
        verify(mockTransactionRepository).processPayment(
            argThat { request ->
                request.amount == amount &&
                request.description == description &&
                request.customerId == customerId &&
                request.paymentMethod == paymentMethod
            }
        )
    }

    @Test
    fun `processPayment does not generate receipt on failure`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        val description = "Test payment"
        val customerId = "user123"
        val paymentMethod = PaymentMethod.CREDIT_CARD

        whenever(mockTransactionRepository.processPayment(any()))
            .thenReturn(Result.failure(Exception("Error")))

        // Act
        paymentService.processPayment(
            amount = amount,
            description = description,
            customerId = customerId,
            paymentMethod = paymentMethod,
            onSuccess = { _ -> },
            onError = { _ -> }
        )
        advanceUntilIdle()

        // Assert
        verify(mockReceiptGenerator, never()).generateReceipt(any())
    }

    @Test
    fun `processPayment handles null error message`() = runTest {
        // Arrange
        val amount = BigDecimal("100.00")
        val error = Exception(null)

        whenever(mockTransactionRepository.processPayment(any()))
            .thenReturn(Result.failure(error))

        var errorMessage: String? = null

        // Act
        paymentService.processPayment(
            amount = amount,
            description = "Test",
            customerId = "user123",
            paymentMethod = PaymentMethod.CREDIT_CARD,
            onSuccess = { _ -> },
            onError = { msg -> errorMessage = msg }
        )
        advanceUntilIdle()

        // Assert
        assertEquals("Unknown error occurred", errorMessage)
    }

    @Test
    fun `refundPayment calls onSuccess with refund response when refund succeeds`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"
        val reason = "Customer request"

        val refundResponse = RefundResponse(
            refundId = "refund_123",
            transactionId = transactionId,
            amount = BigDecimal("50.00"),
            status = RefundStatus.COMPLETED,
            refundTime = System.currentTimeMillis(),
            reason = reason
        )

        whenever(mockTransactionRepository.refundPayment(transactionId, reason))
            .thenReturn(Result.success(refundResponse))

        var onSuccessCalled = false
        var onErrorCalled = false
        var receivedResponse: RefundResponse? = null

        // Act
        paymentService.refundPayment(
            transactionId = transactionId,
            reason = reason,
            onSuccess = { response ->
                onSuccessCalled = true
                receivedResponse = response
            },
            onError = { _ ->
                onErrorCalled = true
            }
        )
        advanceUntilIdle()

        // Assert
        assertTrue(onSuccessCalled)
        assertFalse(onErrorCalled)
        assertNotNull(receivedResponse)
        assertEquals(refundResponse.refundId, receivedResponse?.refundId)
    }

    @Test
    fun `refundPayment calls onError when refund fails`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"
        val reason = "Customer request"

        val error = Exception("Refund gateway error")

        whenever(mockTransactionRepository.refundPayment(transactionId, reason))
            .thenReturn(Result.failure(error))

        var onSuccessCalled = false
        var onErrorCalled = false
        var errorMessage: String? = null

        // Act
        paymentService.refundPayment(
            transactionId = transactionId,
            reason = reason,
            onSuccess = { _ ->
                onSuccessCalled = true
            },
            onError = { msg ->
                onErrorCalled = true
                errorMessage = msg
            }
        )
        advanceUntilIdle()

        // Assert
        assertFalse(onSuccessCalled)
        assertTrue(onErrorCalled)
        assertEquals("Refund gateway error", errorMessage)
    }

    @Test
    fun `refundPayment with null reason succeeds`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"

        val refundResponse = RefundResponse(
            refundId = "refund_123",
            transactionId = transactionId,
            amount = BigDecimal("50.00"),
            status = RefundStatus.COMPLETED,
            refundTime = System.currentTimeMillis(),
            reason = null
        )

        whenever(mockTransactionRepository.refundPayment(transactionId, null))
            .thenReturn(Result.success(refundResponse))

        var onSuccessCalled = false

        // Act
        paymentService.refundPayment(
            transactionId = transactionId,
            reason = null,
            onSuccess = { _ -> onSuccessCalled = true },
            onError = { _ -> }
        )
        advanceUntilIdle()

        // Assert
        assertTrue(onSuccessCalled)
    }

    @Test
    fun `refundPayment handles null error message`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"
        val error = Exception(null)

        whenever(mockTransactionRepository.refundPayment(transactionId, null))
            .thenReturn(Result.failure(error))

        var errorMessage: String? = null

        // Act
        paymentService.refundPayment(
            transactionId = transactionId,
            reason = null,
            onSuccess = { _ -> },
            onError = { msg -> errorMessage = msg }
        )
        advanceUntilIdle()

        // Assert
        assertEquals("Unknown error occurred", errorMessage)
    }

    @Test
    fun `processPayment supports all payment methods`() = runTest {
        val paymentMethods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.BANK_TRANSFER,
            PaymentMethod.E_WALLET,
            PaymentMethod.VIRTUAL_ACCOUNT
        )

        paymentMethods.forEach { method ->
            // Arrange
            val transaction = Transaction(
                id = "test_transaction_id",
                userId = "user123",
                amount = BigDecimal("100.00"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = method,
                description = "Test payment",
                createdAt = Date(),
                updatedAt = Date()
            )

            whenever(mockTransactionRepository.processPayment(any()))
                .thenReturn(Result.success(transaction))

            var onSuccessCalled = false

            // Act
            paymentService.processPayment(
                amount = BigDecimal("100.00"),
                description = "Test payment",
                customerId = "user123",
                paymentMethod = method,
                onSuccess = { _ -> onSuccessCalled = true },
                onError = { _ -> }
            )
            advanceUntilIdle()

            // Assert
            assertTrue("Should succeed for $method", onSuccessCalled)
        }
    }

    @Test
    fun `refundPayment can be called with empty reason string`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"
        val reason = ""

        val refundResponse = RefundResponse(
            refundId = "refund_123",
            transactionId = transactionId,
            amount = BigDecimal("50.00"),
            status = RefundStatus.COMPLETED,
            refundTime = System.currentTimeMillis(),
            reason = reason
        )

        whenever(mockTransactionRepository.refundPayment(transactionId, reason))
            .thenReturn(Result.success(refundResponse))

        var onSuccessCalled = false

        // Act
        paymentService.refundPayment(
            transactionId = transactionId,
            reason = reason,
            onSuccess = { _ -> onSuccessCalled = true },
            onError = { _ -> }
        )
        advanceUntilIdle()

        // Assert
        assertTrue(onSuccessCalled)
    }

    @Test
    fun `processPayment with zero amount works`() = runTest {
        // Arrange
        val transaction = Transaction(
            id = "test_transaction_id",
            userId = "user123",
            amount = BigDecimal.ZERO,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(mockTransactionRepository.processPayment(any()))
            .thenReturn(Result.success(transaction))

        var onSuccessCalled = false

        // Act
        paymentService.processPayment(
            amount = BigDecimal.ZERO,
            description = "Test payment",
            customerId = "user123",
            paymentMethod = PaymentMethod.CREDIT_CARD,
            onSuccess = { _ -> onSuccessCalled = true },
            onError = { _ -> }
        )
        advanceUntilIdle()

        // Assert
        assertTrue(onSuccessCalled)
    }

    @Test
    fun `processPayment with negative amount works`() = runTest {
        // Arrange
        val amount = BigDecimal("-50.00")
        val transaction = Transaction(
            id = "test_transaction_id",
            userId = "user123",
            amount = amount,
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(mockTransactionRepository.processPayment(any()))
            .thenReturn(Result.success(transaction))

        var onSuccessCalled = false

        // Act
        paymentService.processPayment(
            amount = amount,
            description = "Test payment",
            customerId = "user123",
            paymentMethod = PaymentMethod.CREDIT_CARD,
            onSuccess = { _ -> onSuccessCalled = true },
            onError = { _ -> }
        )
        advanceUntilIdle()

        // Assert
        assertTrue(onSuccessCalled)
    }
}
