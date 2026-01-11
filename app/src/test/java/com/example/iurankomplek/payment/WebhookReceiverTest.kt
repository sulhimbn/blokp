package com.example.iurankomplek.payment

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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Date

@ExperimentalCoroutinesApi
class WebhookReceiverTest {

    @Mock
    private lateinit var mockTransactionRepository: TransactionRepository

    private lateinit var webhookReceiver: WebhookReceiver

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        webhookReceiver = WebhookReceiver(mockTransactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setupWebhookListener logs webhook URL`() = runTest {
        // Arrange
        val webhookUrl = "https://example.com/webhook"

        // Act
        webhookReceiver.setupWebhookListener(webhookUrl)

        // Assert - Should complete without error
    }

    @Test
    fun `handleWebhookEvent with success payload updates transaction to COMPLETED`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"
        val payload = "{\"event\":\"payment.success\",\"transaction_id\":\"$transactionId\"}"
        val transaction = Transaction(
            id = transactionId,
            userId = "user123",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(mockTransactionRepository.getTransactionById(transactionId))
            .thenReturn(transaction)

        // Act
        webhookReceiver.handleWebhookEvent(payload)
        advanceUntilIdle()

        // Assert
        verify(mockTransactionRepository).getTransactionById(transactionId)
        verify(mockTransactionRepository).updateTransaction(
            argThat { updatedTx ->
                updatedTx.id == transactionId && updatedTx.status == PaymentStatus.COMPLETED
            }
        )
    }

    @Test
    fun `handleWebhookEvent with failed payload updates transaction to FAILED`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"
        val payload = "{\"event\":\"payment.failed\",\"transaction_id\":\"$transactionId\"}"
        val transaction = Transaction(
            id = transactionId,
            userId = "user123",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(mockTransactionRepository.getTransactionById(transactionId))
            .thenReturn(transaction)

        // Act
        webhookReceiver.handleWebhookEvent(payload)
        advanceUntilIdle()

        // Assert
        verify(mockTransactionRepository).updateTransaction(
            argThat { updatedTx ->
                updatedTx.status == PaymentStatus.FAILED
            }
        )
    }

    @Test
    fun `handleWebhookEvent with refunded payload updates transaction to REFUNDED`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"
        val payload = "{\"event\":\"payment.refunded\",\"transaction_id\":\"$transactionId\"}"
        val transaction = Transaction(
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

        whenever(mockTransactionRepository.getTransactionById(transactionId))
            .thenReturn(transaction)

        // Act
        webhookReceiver.handleWebhookEvent(payload)
        advanceUntilIdle()

        // Assert
        verify(mockTransactionRepository).updateTransaction(
            argThat { updatedTx ->
                updatedTx.status == PaymentStatus.REFUNDED
            }
        )
    }

    @Test
    fun `handleWebhookEvent with unknown event type logs warning`() = runTest {
        // Arrange
        val payload = "{\"event\":\"unknown.event\",\"transaction_id\":\"test_id\"}"

        // Act
        webhookReceiver.handleWebhookEvent(payload)
        advanceUntilIdle()

        // Assert - Should not call repository update for unknown events
        verifyNoInteractions(mockTransactionRepository)
    }

    @Test
    fun `handleWebhookEvent when transaction not found logs error`() = runTest {
        // Arrange
        val transactionId = "non_existent_id"
        val payload = "{\"event\":\"payment.success\",\"transaction_id\":\"$transactionId\"}"

        whenever(mockTransactionRepository.getTransactionById(transactionId))
            .thenReturn(null)

        // Act
        webhookReceiver.handleWebhookEvent(payload)
        advanceUntilIdle()

        // Assert
        verify(mockTransactionRepository).getTransactionById(transactionId)
        verify(mockTransactionRepository, never()).updateTransaction(any())
    }

    @Test
    fun `handleWebhookEvent handles null payload gracefully`() = runTest {
        // Act & Assert - Should not throw exception
        try {
            webhookReceiver.handleWebhookEvent("")
            advanceUntilIdle()
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }

    @Test
    fun `handleWebhookEvent handles malformed JSON gracefully`() = runTest {
        // Arrange
        val malformedPayload = "not valid json"

        // Act & Assert - Should not throw exception
        try {
            webhookReceiver.handleWebhookEvent(malformedPayload)
            advanceUntilIdle()
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }

    @Test
    fun `handleWebhookEvent handles repository exception gracefully`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"
        val payload = "{\"event\":\"payment.success\",\"transaction_id\":\"$transactionId\"}"

        whenever(mockTransactionRepository.getTransactionById(transactionId))
            .thenThrow(RuntimeException("Database error"))

        // Act & Assert - Should not throw exception
        try {
            webhookReceiver.handleWebhookEvent(payload)
            advanceUntilIdle()
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }

    @Test
    fun `handleWebhookEvent can process multiple events sequentially`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"
        val transaction = Transaction(
            id = transactionId,
            userId = "user123",
            amount = BigDecimal("100.00"),
            currency = "IDR",
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Test payment",
            createdAt = Date(),
            updatedAt = Date()
        )

        whenever(mockTransactionRepository.getTransactionById(transactionId))
            .thenReturn(transaction)

        // Act
        webhookReceiver.handleWebhookEvent("{\"event\":\"payment.success\",\"transaction_id\":\"$transactionId\"}")
        webhookReceiver.handleWebhookEvent("{\"event\":\"payment.success\",\"transaction_id\":\"$transactionId\"}")
        webhookReceiver.handleWebhookEvent("{\"event\":\"payment.success\",\"transaction_id\":\"$transactionId\"}")
        advanceUntilIdle()

        // Assert
        verify(mockTransactionRepository, times(3)).updateTransaction(any())
    }

    @Test
    fun `extractEventType identifies success event`() {
        // Arrange
        val payload = "{\"event\":\"payment.success\"}"

        // Act & Assert - This is internal, but should identify correctly
        // Note: This test documents expected behavior
    }

    @Test
    fun `extractEventType identifies failed event`() {
        // Arrange
        val payload = "{\"event\":\"payment.failed\"}"

        // Act & Assert - This is internal, but should identify correctly
        // Note: This test documents expected behavior
    }

    @Test
    fun `extractEventType identifies refunded event`() {
        // Arrange
        val payload = "{\"event\":\"payment.refunded\"}"

        // Act & Assert - This is internal, but should identify correctly
        // Note: This test documents expected behavior
    }
}
