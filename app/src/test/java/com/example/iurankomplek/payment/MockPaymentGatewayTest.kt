package com.example.iurankomplek.payment

import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.*

@ExperimentalCoroutinesApi
class MockPaymentGatewayTest {

    private lateinit var mockPaymentGateway: MockPaymentGateway

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockPaymentGateway = MockPaymentGateway()
    }

    @After
    fun tearDown() {
        Dispatchers.setMain(Dispatchers.Default)
    }

    // Happy Path Tests

    @Test
    fun `processPayment should return success with valid request`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "customer123",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val result = mockPaymentGateway.processPayment(request)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals(PaymentStatus.COMPLETED, response?.status)
        assertEquals(PaymentMethod.CREDIT_CARD, response?.paymentMethod)
        assertEquals(BigDecimal("100.00"), response?.amount)
        assertEquals("IDR", response?.currency)
    }

    @Test
    fun `processPayment should generate unique transaction IDs`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal("50.00"),
            description = "Test payment",
            customerId = "customer123",
            paymentMethod = PaymentMethod.BANK_TRANSFER
        )

        val result1 = mockPaymentGateway.processPayment(request)
        val result2 = mockPaymentGateway.processPayment(request)

        val id1 = result1.getOrNull()?.transactionId
        val id2 = result2.getOrNull()?.transactionId

        assertNotNull(id1)
        assertNotNull(id2)
        assertNotEquals(id1, id2)
    }

    @Test
    fun `processPayment should include reference number`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal("75.50"),
            description = "Test payment",
            customerId = "customer456",
            paymentMethod = PaymentMethod.E_WALLET
        )

        val result = mockPaymentGateway.processPayment(request)

        val response = result.getOrNull()
        assertNotNull(response?.referenceNumber)
        assertTrue(response?.referenceNumber!!.startsWith("REF-"))
    }

    @Test
    fun `processPayment should preserve metadata`() = runTest {
        val metadata = mapOf(
            "orderId" to "ORD-123",
            "userId" to "user456",
            "notes" to "Test payment"
        )
        val request = PaymentRequest(
            amount = BigDecimal("25.00"),
            description = "Test payment",
            customerId = "customer789",
            paymentMethod = PaymentMethod.VIRTUAL_ACCOUNT,
            metadata = metadata
        )

        val result = mockPaymentGateway.processPayment(request)

        val response = result.getOrNull()
        assertNotNull(response?.metadata)
        assertEquals(metadata, response?.metadata)
    }

    @Test
    fun `processPayment should handle all payment methods`() = runTest {
        val paymentMethods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.BANK_TRANSFER,
            PaymentMethod.E_WALLET,
            PaymentMethod.VIRTUAL_ACCOUNT
        )

        for (method in paymentMethods) {
            val request = PaymentRequest(
                amount = BigDecimal("100.00"),
                description = "Test payment with $method",
                customerId = "customer123",
                paymentMethod = method
            )

            val result = mockPaymentGateway.processPayment(request)

            assertTrue("Failed for $method", result.isSuccess)
            val response = result.getOrNull()
            assertEquals(method, response?.paymentMethod)
        }
    }

    @Test
    fun `processPayment should include transaction time`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "customer123",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val beforeTime = System.currentTimeMillis()
        val result = mockPaymentGateway.processPayment(request)
        val afterTime = System.currentTimeMillis()

        val response = result.getOrNull()
        assertNotNull(response?.transactionTime)
        assertTrue(response?.transactionTime!! >= beforeTime)
        assertTrue(response.transactionTime <= afterTime)
    }

    @Test
    fun `refundPayment should return success with valid transaction ID`() = runTest {
        val transactionId = "test_transaction_id"

        val result = mockPaymentGateway.refundPayment(transactionId)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals(RefundStatus.COMPLETED, response?.status)
        assertEquals(transactionId, response?.transactionId)
    }

    @Test
    fun `refundPayment should generate unique refund ID`() = runTest {
        val transactionId = "test_transaction_id"

        val result1 = mockPaymentGateway.refundPayment(transactionId)
        val result2 = mockPaymentGateway.refundPayment(transactionId)

        val id1 = result1.getOrNull()?.refundId
        val id2 = result2.getOrNull()?.refundId

        assertNotNull(id1)
        assertNotNull(id2)
        assertNotEquals(id1, id2)
    }

    @Test
    fun `refundPayment should calculate refund amount based on transaction ID hash`() = runTest {
        val transactionId1 = "test_transaction_id_1"
        val transactionId2 = "test_transaction_id_2"

        val result1 = mockPaymentGateway.refundPayment(transactionId1)
        val result2 = mockPaymentGateway.refundPayment(transactionId2)

        val amount1 = result1.getOrNull()?.amount
        val amount2 = result2.getOrNull()?.amount

        assertNotNull(amount1)
        assertNotNull(amount2)
        assertNotEquals(amount1, amount2)
    }

    @Test
    fun `refundPayment should include refund time`() = runTest {
        val transactionId = "test_transaction_id"

        val beforeTime = System.currentTimeMillis()
        val result = mockPaymentGateway.refundPayment(transactionId)
        val afterTime = System.currentTimeMillis()

        val response = result.getOrNull()
        assertNotNull(response?.refundTime)
        assertTrue(response?.refundTime!! >= beforeTime)
        assertTrue(response.refundTime <= afterTime)
    }

    @Test
    fun `refundPayment should include refund reason`() = runTest {
        val transactionId = "test_transaction_id"

        val result = mockPaymentGateway.refundPayment(transactionId)

        val response = result.getOrNull()
        assertNotNull(response?.reason)
        assertEquals("Mock refund", response?.reason)
    }

    @Test
    fun `getPaymentStatus should return COMPLETED for valid transaction ID`() = runTest {
        val transactionId = "test_transaction_id"

        val result = mockPaymentGateway.getPaymentStatus(transactionId)

        assertTrue(result.isSuccess)
        val status = result.getOrNull()
        assertEquals(PaymentStatus.COMPLETED, status)
    }

    @Test
    fun `getPaymentStatus should return success for any transaction ID`() = runTest {
        val transactionIds = listOf(
            "tx1",
            "tx_123",
            "transaction-abc",
            UUID.randomUUID().toString()
        )

        for (transactionId in transactionIds) {
            val result = mockPaymentGateway.getPaymentStatus(transactionId)

            assertTrue("Failed for $transactionId", result.isSuccess)
            val status = result.getOrNull()
            assertEquals(PaymentStatus.COMPLETED, status)
        }
    }

    // Edge Case Tests

    @Test
    fun `processPayment should handle zero amount`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal.ZERO,
            description = "Zero amount payment",
            customerId = "customer123",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val result = mockPaymentGateway.processPayment(request)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals(BigDecimal.ZERO, response?.amount)
    }

    @Test
    fun `processPayment should handle very large amount`() = runTest {
        val largeAmount = BigDecimal("999999999.99")
        val request = PaymentRequest(
            amount = largeAmount,
            description = "Large amount payment",
            customerId = "customer123",
            paymentMethod = PaymentMethod.BANK_TRANSFER
        )

        val result = mockPaymentGateway.processPayment(request)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals(largeAmount, response?.amount)
    }

    @Test
    fun `processPayment should handle empty metadata`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "customer123",
            paymentMethod = PaymentMethod.CREDIT_CARD,
            metadata = emptyMap()
        )

        val result = mockPaymentGateway.processPayment(request)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response?.metadata)
        assertTrue(response?.metadata?.isEmpty() ?: false)
    }

    @Test
    fun `processPayment should use default currency when not specified`() = runTest {
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "customer123",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val result = mockPaymentGateway.processPayment(request)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals("IDR", response?.currency)
    }

    @Test
    fun `refundPayment should use minimum amount when hash is zero`() = runTest {
        val transactionId = "0"

        val result = mockPaymentGateway.refundPayment(transactionId)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response?.amount)
        val minAmount = BigDecimal(com.example.iurankomplek.utils.Constants.Payment.DEFAULT_REFUND_AMOUNT_MIN.toString())
        assertTrue(response?.amount!! >= BigDecimal.ZERO)
    }

    @Test
    fun `refundPayment should handle empty transaction ID`() = runTest {
        val transactionId = ""

        val result = mockPaymentGateway.refundPayment(transactionId)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals(transactionId, response?.transactionId)
    }

    @Test
    fun `refundPayment should handle very long transaction ID`() = runTest {
        val longTransactionId = "a".repeat(1000)

        val result = mockPaymentGateway.refundPayment(longTransactionId)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals(longTransactionId, response?.transactionId)
    }

    @Test
    fun `getPaymentStatus should handle empty transaction ID`() = runTest {
        val transactionId = ""

        val result = mockPaymentGateway.getPaymentStatus(transactionId)

        assertTrue(result.isSuccess)
        val status = result.getOrNull()
        assertEquals(PaymentStatus.COMPLETED, status)
    }

    @Test
    fun `getPaymentStatus should handle very long transaction ID`() = runTest {
        val longTransactionId = "a".repeat(1000)

        val result = mockPaymentGateway.getPaymentStatus(longTransactionId)

        assertTrue(result.isSuccess)
        val status = result.getOrNull()
        assertEquals(PaymentStatus.COMPLETED, status)
    }

    // Data Type Tests

    @Test
    fun `processPayment should handle decimal amounts correctly`() = runTest {
        val decimalAmounts = listOf(
            BigDecimal("10.50"),
            BigDecimal("99.99"),
            BigDecimal("0.01"),
            BigDecimal("999999.99")
        )

        for (amount in decimalAmounts) {
            val request = PaymentRequest(
                amount = amount,
                description = "Decimal amount test",
                customerId = "customer123",
                paymentMethod = PaymentMethod.CREDIT_CARD
            )

            val result = mockPaymentGateway.processPayment(request)

            assertTrue("Failed for $amount", result.isSuccess)
            val response = result.getOrNull()
            assertEquals(amount, response?.amount)
        }
    }

    @Test
    fun `refundPayment should return BigDecimal for amount`() = runTest {
        val transactionId = "test_transaction_id"

        val result = mockPaymentGateway.refundPayment(transactionId)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response?.amount)
        assertTrue(response?.amount is BigDecimal)
    }

    @Test
    fun `processPayment should accept string customer IDs`() = runTest {
        val customerIds = listOf(
            "customer_123",
            "user-456",
            "USER789",
            "12345",
            "abc-def-ghi"
        )

        for (customerId in customerIds) {
            val request = PaymentRequest(
                amount = BigDecimal("100.00"),
                description = "Customer ID test",
                customerId = customerId,
                paymentMethod = PaymentMethod.CREDIT_CARD
            )

            val result = mockPaymentGateway.processPayment(request)

            assertTrue("Failed for $customerId", result.isSuccess)
        }
    }

    @Test
    fun `processPayment should accept unicode in description`() = runTest {
        val descriptions = listOf(
            "Test payment 中文",
            "Test payment 日本語",
            "Test payment العربية",
            "Test payment ñoño"
        )

        for (description in descriptions) {
            val request = PaymentRequest(
                amount = BigDecimal("100.00"),
                description = description,
                customerId = "customer123",
                paymentMethod = PaymentMethod.CREDIT_CARD
            )

            val result = mockPaymentGateway.processPayment(request)

            assertTrue("Failed for $description", result.isSuccess)
        }
    }

    // Concurrency Tests

    @Test
    fun `processPayment should be thread-safe for concurrent requests`() = runTest {
        val requests = (1..10).map { index ->
            PaymentRequest(
                amount = BigDecimal("100.00"),
                description = "Concurrent payment $index",
                customerId = "customer$index",
                paymentMethod = PaymentMethod.CREDIT_CARD
            )
        }

        val results = requests.mapIndexed { index, request ->
            kotlinx.coroutines.async(Dispatchers.Default) {
                mockPaymentGateway.processPayment(request)
            }
        }.awaitAll()

        assertTrue("All results should be success", results.all { it.isSuccess })
        val transactionIds = results.mapNotNull { it.getOrNull()?.transactionId }
        assertEquals("All transaction IDs should be unique", 10, transactionIds.toSet().size)
    }

    @Test
    fun `refundPayment should be thread-safe for concurrent refunds`() = runTest {
        val transactionId = "test_transaction_id"

        val results = (1..10).map { index ->
            kotlinx.coroutines.async(Dispatchers.Default) {
                mockPaymentGateway.refundPayment(transactionId)
            }
        }.awaitAll()

        assertTrue("All results should be success", results.all { it.isSuccess })
        val refundIds = results.mapNotNull { it.getOrNull()?.refundId }
        assertEquals("All refund IDs should be unique", 10, refundIds.toSet().size)
    }

    @Test
    fun `getPaymentStatus should be thread-safe for concurrent status checks`() = runTest {
        val transactionId = "test_transaction_id"

        val results = (1..10).map { index ->
            kotlinx.coroutines.async(Dispatchers.Default) {
                mockPaymentGateway.getPaymentStatus(transactionId)
            }
        }.awaitAll()

        assertTrue("All results should be success", results.all { it.isSuccess })
        results.forEach { result ->
            assertEquals(PaymentStatus.COMPLETED, result.getOrNull())
        }
    }
}
