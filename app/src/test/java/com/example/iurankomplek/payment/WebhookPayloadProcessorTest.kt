package com.example.iurankomplek.payment

import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebhookPayloadProcessorTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var payloadProcessor: WebhookPayloadProcessor

    @Before
    fun setup() {
        transactionRepository = mockk(relaxed = true)
        payloadProcessor = WebhookPayloadProcessor(transactionRepository)
    }

    @Test
    fun `processWebhookPayload should process payment success and update transaction`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.success",
            payload = """{"eventType":"payment.success","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val transaction = Transaction(
            id = "tx123",
            amount = 100000.0,
            paymentMethod = "CREDIT_CARD",
            status = PaymentStatus.PENDING
        )

        coEvery { transactionRepository.getTransactionById("tx123") } returns transaction
        coEvery { transactionRepository.updateTransaction(any()) } just Runs

        val result = payloadProcessor.processWebhookPayload(event)

        assertTrue(result, "Processing should succeed")
        coVerify { transactionRepository.updateTransaction(match<Transaction> { it.status == PaymentStatus.COMPLETED }) }
    }

    @Test
    fun `processWebhookPayload should process payment failed and update transaction`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.failed",
            payload = """{"eventType":"payment.failed","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val transaction = Transaction(
            id = "tx123",
            amount = 100000.0,
            paymentMethod = "CREDIT_CARD",
            status = PaymentStatus.PENDING
        )

        coEvery { transactionRepository.getTransactionById("tx123") } returns transaction
        coEvery { transactionRepository.updateTransaction(any()) } just Runs

        val result = payloadProcessor.processWebhookPayload(event)

        assertTrue(result, "Processing should succeed")
        coVerify { transactionRepository.updateTransaction(match<Transaction> { it.status == PaymentStatus.FAILED }) }
    }

    @Test
    fun `processWebhookPayload should process payment refunded and update transaction`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.refunded",
            payload = """{"eventType":"payment.refunded","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val transaction = Transaction(
            id = "tx123",
            amount = 100000.0,
            paymentMethod = "CREDIT_CARD",
            status = PaymentStatus.COMPLETED
        )

        coEvery { transactionRepository.getTransactionById("tx123") } returns transaction
        coEvery { transactionRepository.updateTransaction(any()) } just Runs

        val result = payloadProcessor.processWebhookPayload(event)

        assertTrue(result, "Processing should succeed")
        coVerify { transactionRepository.updateTransaction(match<Transaction> { it.status == PaymentStatus.REFUNDED }) }
    }

    @Test
    fun `processWebhookPayload should skip unknown event types`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "unknown.event",
            payload = """{"eventType":"unknown.event","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val result = payloadProcessor.processWebhookPayload(event)

        assertTrue(result, "Processing should succeed for unknown events")
        coVerify(exactly = 0) { transactionRepository.updateTransaction(any()) }
    }

    @Test
    fun `processWebhookPayload should return false on invalid JSON`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.success",
            payload = """invalid json""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        val result = payloadProcessor.processWebhookPayload(event)

        assertFalse(result, "Processing should fail on invalid JSON")
    }

    @Test
    fun `processWebhookPayload should return false when transaction not found`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.success",
            payload = """{"eventType":"payment.success","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING
        )

        coEvery { transactionRepository.getTransactionById("tx123") } returns null

        val result = payloadProcessor.processWebhookPayload(event)

        assertFalse(result, "Processing should fail when transaction not found")
    }

    @Test
    fun `processWebhookPayload should return false when transaction ID is null`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.success",
            payload = """{"eventType":"payment.success"}""",
            transactionId = null,
            status = WebhookDeliveryStatus.PENDING
        )

        val result = payloadProcessor.processWebhookPayload(event)

        assertFalse(result, "Processing should fail when transaction ID is null")
    }

    @Test
    fun `processWebhookPayload should return false when transaction ID is blank`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.success",
            payload = """{"eventType":"payment.success","transactionId":"   "}""",
            transactionId = "   ",
            status = WebhookDeliveryStatus.PENDING
        )

        val result = payloadProcessor.processWebhookPayload(event)

        assertFalse(result, "Processing should fail when transaction ID is blank")
    }
}
