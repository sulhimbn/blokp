package com.example.iurankomplek.payment

import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.transaction.TransactionRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WebhookQueueTest {

    private lateinit var webhookEventDao: WebhookEventDao
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var webhookQueue: WebhookQueue
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        webhookEventDao = mockk(relaxed = true)
        transactionRepository = mockk(relaxed = true)
        webhookQueue = WebhookQueue(webhookEventDao, transactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        webhookQueue.destroy()
    }

    @Test
    fun `enqueue should create webhook event with idempotency key`() = runTest {
        coEvery { webhookEventDao.insert(any()) } returns 1L

        val id = webhookQueue.enqueue(
            eventType = "payment.success",
            payload = """{"eventType":"payment.success","transactionId":"tx123"}""",
            transactionId = "tx123"
        )

        coVerify { webhookEventDao.insert(match<WebhookEvent> { 
            it.eventType == "payment.success" && 
            it.idempotencyKey.startsWith("whk_") &&
            it.status == WebhookDeliveryStatus.PENDING
        }) }
        assertEquals(1L, id)
    }

    @Test
    fun `enqueue should add metadata to payload`() = runTest {
        coEvery { webhookEventDao.insert(any()) } returns 1L
        val metadata = mapOf("source" to "mobile", "version" to "1.0")

        webhookQueue.enqueue(
            eventType = "payment.success",
            payload = """{"eventType":"payment.success","transactionId":"tx123"}""",
            transactionId = "tx123",
            metadata = metadata
        )

        coVerify { webhookEventDao.insert(match<WebhookEvent> { 
            it.payload.contains("\"source\":\"mobile\"") &&
            it.payload.contains("\"version\":\"1.0\"")
        }) }
    }

    @Test
    fun `processEvent should mark as delivered on successful processing`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.success",
            payload = """{"eventType":"payment.success","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING,
            retryCount = 0
        )
        
        val transaction = Transaction(
            id = "tx123",
            amount = 100000.0,
            paymentMethod = "CREDIT_CARD",
            status = PaymentStatus.PENDING
        )

        coEvery { webhookEventDao.getEventById(1L) } returns event
        coEvery { transactionRepository.getTransactionById("tx123") } returns transaction
        coEvery { transactionRepository.updateTransaction(any()) } just Runs
        coEvery { webhookEventDao.updateStatus(1L, WebhookDeliveryStatus.PROCESSING) } just Runs
        coEvery { webhookEventDao.markAsDelivered(1L) } just Runs

        advanceUntilIdle()

        coVerify { webhookEventDao.markAsDelivered(1L) }
    }

    @Test
    fun `processEvent should retry on failure`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.success",
            payload = """{"eventType":"payment.success","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING,
            retryCount = 0,
            maxRetries = 3
        )

        coEvery { webhookEventDao.getEventById(1L) } returns event
        coEvery { webhookEventDao.updateStatus(1L, WebhookDeliveryStatus.PROCESSING) } just Runs
        coEvery { transactionRepository.getTransactionById("tx123") } returns null
        coEvery { webhookEventDao.updateRetryInfo(
            id = 1L,
            retryCount = 1,
            nextRetryAt = any(),
            lastError = any()
        ) } just Runs

        advanceUntilIdle()

        coVerify { webhookEventDao.updateRetryInfo(
            id = 1L,
            retryCount = 1,
            nextRetryAt = any(),
            lastError = any()
        ) }
    }

    @Test
    fun `processEvent should mark as failed after max retries`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.success",
            payload = """{"eventType":"payment.success","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING,
            retryCount = 5,
            maxRetries = 5
        )

        coEvery { webhookEventDao.getEventById(1L) } returns event
        coEvery { webhookEventDao.updateStatus(1L, WebhookDeliveryStatus.PROCESSING) } just Runs
        coEvery { transactionRepository.getTransactionById("tx123") } returns null
        coEvery { webhookEventDao.markAsFailed(1L) } just Runs

        advanceUntilIdle()

        coVerify { webhookEventDao.markAsFailed(1L) }
    }

    @Test
    fun `calculateRetryDelay should use exponential backoff`() = runTest {
        val retryDelay0 = webhookQueue.calculateRetryDelay(0)
        val retryDelay1 = webhookQueue.calculateRetryDelay(1)
        val retryDelay2 = webhookQueue.calculateRetryDelay(2)

        assertTrue(retryDelay1 > retryDelay0, "Delay should increase with retry count")
        assertTrue(retryDelay2 > retryDelay1, "Delay should increase with retry count")
        assertTrue(retryDelay0 >= 500, "Initial delay should be around 1000ms with jitter")
        assertTrue(retryDelay1 >= 1500, "Second delay should be around 2000ms with jitter")
    }

    @Test
    fun `calculateRetryDelay should cap at max retry delay`() = runTest {
        val retryDelay10 = webhookQueue.calculateRetryDelay(10)
        val retryDelay100 = webhookQueue.calculateRetryDelay(100)

        assertTrue(retryDelay10 <= 65000, "Delay should be capped at 60000ms + jitter")
        assertTrue(retryDelay100 <= 65000, "Delay should be capped at 60000ms + jitter")
    }

    @Test
    fun `retryFailedEvents should retry failed events`() = runTest {
        val events = listOf(
            WebhookEvent(
                id = 1L,
                idempotencyKey = "whk_1",
                eventType = "payment.success",
                payload = "{}",
                status = WebhookDeliveryStatus.FAILED,
                retryCount = 5
            ),
            WebhookEvent(
                id = 2L,
                idempotencyKey = "whk_2",
                eventType = "payment.failed",
                payload = "{}",
                status = WebhookDeliveryStatus.FAILED,
                retryCount = 3
            )
        )

        coEvery {
            webhookEventDao.getPendingEventsByStatus(
                WebhookDeliveryStatus.FAILED,
                any<Long>(),
                50
            )
        } returns events
        coEvery { webhookEventDao.updateStatus(any<Long>(), WebhookDeliveryStatus.PENDING) } just Runs

        val count = webhookQueue.retryFailedEvents()

        assertEquals(2, count)
        coVerify(exactly = 2) { webhookEventDao.updateStatus(any<Long>(), WebhookDeliveryStatus.PENDING) }
    }

    @Test
    fun `cleanupOldEvents should delete old events`() = runTest {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)
        
        coEvery { webhookEventDao.deleteEventsOlderThan(any<Long>()) } returns 5

        val count = webhookQueue.cleanupOldEvents()

        assertEquals(5, count)
        coVerify { webhookEventDao.deleteEventsOlderThan(any<Long>()) }
    }

    @Test
    fun `getPendingEventCount should return count of pending events`() = runTest {
        coEvery { webhookEventDao.countByStatus(WebhookDeliveryStatus.PENDING) } returns 10

        val count = webhookQueue.getPendingEventCount()

        assertEquals(10, count)
    }

    @Test
    fun `getFailedEventCount should return count of failed events`() = runTest {
        coEvery { webhookEventDao.countByStatus(WebhookDeliveryStatus.FAILED) } returns 3

        val count = webhookQueue.getFailedEventCount()

        assertEquals(3, count)
    }

    @Test
    fun `processEvent should update transaction status for payment success`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.success",
            payload = """{"eventType":"payment.success","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING,
            retryCount = 0
        )
        
        val transaction = Transaction(
            id = "tx123",
            amount = 100000.0,
            paymentMethod = "CREDIT_CARD",
            status = PaymentStatus.PENDING
        )

        coEvery { webhookEventDao.getEventById(1L) } returns event
        coEvery { transactionRepository.getTransactionById("tx123") } returns transaction
        coEvery { webhookEventDao.updateStatus(1L, WebhookDeliveryStatus.PROCESSING) } just Runs
        coEvery { webhookEventDao.markAsDelivered(1L) } just Runs
        coEvery { webhookEventDao.markAsFailed(any<Long>()) } just Runs

        advanceUntilIdle()

        coVerify { 
            transactionRepository.updateTransaction(
                match<Transaction> { it.status == PaymentStatus.COMPLETED }
            ) 
        }
    }

    @Test
    fun `processEvent should update transaction status for payment failed`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.failed",
            payload = """{"eventType":"payment.failed","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING,
            retryCount = 0
        )
        
        val transaction = Transaction(
            id = "tx123",
            amount = 100000.0,
            paymentMethod = "CREDIT_CARD",
            status = PaymentStatus.PENDING
        )

        coEvery { webhookEventDao.getEventById(1L) } returns event
        coEvery { transactionRepository.getTransactionById("tx123") } returns transaction
        coEvery { webhookEventDao.updateStatus(1L, WebhookDeliveryStatus.PROCESSING) } just Runs
        coEvery { webhookEventDao.markAsDelivered(1L) } just Runs
        coEvery { webhookEventDao.markAsFailed(any<Long>()) } just Runs

        advanceUntilIdle()

        coVerify { 
            transactionRepository.updateTransaction(
                match<Transaction> { it.status == PaymentStatus.FAILED }
            ) 
        }
    }

    @Test
    fun `processEvent should update transaction status for payment refunded`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "payment.refunded",
            payload = """{"eventType":"payment.refunded","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING,
            retryCount = 0
        )
        
        val transaction = Transaction(
            id = "tx123",
            amount = 100000.0,
            paymentMethod = "CREDIT_CARD",
            status = PaymentStatus.COMPLETED
        )

        coEvery { webhookEventDao.getEventById(1L) } returns event
        coEvery { transactionRepository.getTransactionById("tx123") } returns transaction
        coEvery { webhookEventDao.updateStatus(1L, WebhookDeliveryStatus.PROCESSING) } just Runs
        coEvery { webhookEventDao.markAsDelivered(1L) } just Runs
        coEvery { webhookEventDao.markAsFailed(any<Long>()) } just Runs

        advanceUntilIdle()

        coVerify { 
            transactionRepository.updateTransaction(
                match<Transaction> { it.status == PaymentStatus.REFUNDED }
            ) 
        }
    }

    @Test
    fun `processEvent should skip unknown event types`() = runTest {
        val event = WebhookEvent(
            id = 1L,
            idempotencyKey = "whk_123",
            eventType = "unknown.event",
            payload = """{"eventType":"unknown.event","transactionId":"tx123"}""",
            transactionId = "tx123",
            status = WebhookDeliveryStatus.PENDING,
            retryCount = 0
        )

        coEvery { webhookEventDao.getEventById(1L) } returns event
        coEvery { webhookEventDao.updateStatus(1L, WebhookDeliveryStatus.PROCESSING) } just Runs
        coEvery { webhookEventDao.markAsDelivered(1L) } just Runs
        coEvery { webhookEventDao.markAsFailed(any<Long>()) } just Runs

        advanceUntilIdle()

        coVerify(exactly = 0) { transactionRepository.updateTransaction(any()) }
        coVerify { webhookEventDao.markAsDelivered(1L) }
    }
}
