package com.example.iurankomplek.payment

import com.example.iurankomplek.network.ApiService
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
import retrofit2.Response
import java.math.BigDecimal

@ExperimentalCoroutinesApi
class RealPaymentGatewayTest {

    @Mock
    private lateinit var mockApiService: ApiService

    private lateinit var realPaymentGateway: RealPaymentGateway

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        realPaymentGateway = RealPaymentGateway(mockApiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `processPayment returns success when API call succeeds`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "COMPLETED",
            paymentMethod = "CREDIT_CARD",
            amount = "100.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(
            mockApiService.initiatePayment(
                amount = "100.00",
                description = "Test payment",
                customerId = "test_customer",
                paymentMethod = "CREDIT_CARD"
            )
        ).thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        val paymentResponse = result.getOrNull()
        assertNotNull(paymentResponse)
        assertEquals("test_transaction_id", paymentResponse?.transactionId)
        assertEquals(PaymentStatus.COMPLETED, paymentResponse?.status)
        assertEquals(BigDecimal("100.00"), paymentResponse?.amount)
    }

    @Test
    fun `processPayment converts PENDING status`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("50.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.BANK_TRANSFER
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "PENDING",
            paymentMethod = "BANK_TRANSFER",
            amount = "50.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.PENDING, result.getOrNull()?.status)
    }

    @Test
    fun `processPayment converts PROCESSING status`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("75.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.E_WALLET
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "PROCESSING",
            paymentMethod = "E_WALLET",
            amount = "75.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.PROCESSING, result.getOrNull()?.status)
    }

    @Test
    fun `processPayment converts FAILED status`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("25.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.VIRTUAL_ACCOUNT
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "FAILED",
            paymentMethod = "VIRTUAL_ACCOUNT",
            amount = "25.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.FAILED, result.getOrNull()?.status)
    }

    @Test
    fun `processPayment converts REFUNDED status`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "REFUNDED",
            paymentMethod = "CREDIT_CARD",
            amount = "100.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.REFUNDED, result.getOrNull()?.status)
    }

    @Test
    fun `processPayment converts CANCELLED status`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "CANCELLED",
            paymentMethod = "CREDIT_CARD",
            amount = "100.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.CANCELLED, result.getOrNull()?.status)
    }

    @Test
    fun `processPayment converts SUCCESS status to COMPLETED`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "SUCCESS",
            paymentMethod = "CREDIT_CARD",
            amount = "100.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.COMPLETED, result.getOrNull()?.status)
    }

    @Test
    fun `processPayment converts ERROR status to FAILED`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "ERROR",
            paymentMethod = "CREDIT_CARD",
            amount = "100.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.FAILED, result.getOrNull()?.status)
    }

    @Test
    fun `processPayment handles case-insensitive status conversion`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "completed", // lowercase
            paymentMethod = "CREDIT_CARD",
            amount = "100.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.COMPLETED, result.getOrNull()?.status)
    }

    @Test
    fun `processPayment converts payment methods correctly`() = runTest {
        // Arrange
        val methods = listOf(
            Pair("CREDIT_CARD", PaymentMethod.CREDIT_CARD),
            Pair("BANK_TRANSFER", PaymentMethod.BANK_TRANSFER),
            Pair("E_WALLET", PaymentMethod.E_WALLET),
            Pair("VIRTUAL_ACCOUNT", PaymentMethod.VIRTUAL_ACCOUNT)
        )

        methods.forEach { (apiMethod, expectedMethod) ->
            val request = PaymentRequest(
                amount = BigDecimal("100.00"),
                description = "Test payment",
                customerId = "test_customer",
                paymentMethod = expectedMethod
            )

            val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
                transactionId = "test_transaction_id",
                status = "COMPLETED",
                paymentMethod = apiMethod,
                amount = "100.00",
                currency = "IDR",
                transactionTime = System.currentTimeMillis(),
                referenceNumber = "REF123"
            )

            whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
                .thenReturn(Response.success(apiResponse))

            // Act
            val result = realPaymentGateway.processPayment(request)

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(expectedMethod, result.getOrNull()?.paymentMethod)
        }
    }

    @Test
    fun `processPayment uses request amount when API returns empty amount`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "COMPLETED",
            paymentMethod = "CREDIT_CARD",
            amount = "", // Empty amount
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("100.00"), result.getOrNull()?.amount)
    }

    @Test
    fun `processPayment returns failure when API returns error`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.error(400, okhttp3.ResponseBody.create(null, "Bad Request")))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `processPayment returns failure when API returns null body`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(null))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `processPayment returns failure when API throws exception`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenThrow(RuntimeException("Network error"))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun `refundPayment returns success`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"

        // Act
        val result = realPaymentGateway.refundPayment(transactionId)

        // Assert
        assertTrue(result.isSuccess)
        val refundResponse = result.getOrNull()
        assertNotNull(refundResponse)
        assertEquals(transactionId, refundResponse?.transactionId)
        assertEquals(RefundStatus.COMPLETED, refundResponse?.status)
        assertNotNull(refundResponse?.refundId)
    }

    @Test
    fun `refundPayment generates unique refund ID`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"

        // Act
        val result1 = realPaymentGateway.refundPayment(transactionId)
        val result2 = realPaymentGateway.refundPayment(transactionId)

        // Assert
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        val refundId1 = result1.getOrNull()?.refundId
        val refundId2 = result2.getOrNull()?.refundId
        assertNotEquals(refundId1, refundId2)
    }

    @Test
    fun `getPaymentStatus returns success when API succeeds`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = transactionId,
            status = "COMPLETED",
            paymentMethod = "CREDIT_CARD",
            amount = "100.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.getPaymentStatus(transactionId))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.getPaymentStatus(transactionId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.COMPLETED, result.getOrNull())
    }

    @Test
    fun `getPaymentStatus returns failure when API returns error`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"

        whenever(mockApiService.getPaymentStatus(transactionId))
            .thenReturn(Response.error(404, okhttp3.ResponseBody.create(null, "Not Found")))

        // Act
        val result = realPaymentGateway.getPaymentStatus(transactionId)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `getPaymentStatus returns failure when API returns null body`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"

        whenever(mockApiService.getPaymentStatus(transactionId))
            .thenReturn(Response.success(null))

        // Act
        val result = realPaymentGateway.getPaymentStatus(transactionId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Empty response body", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getPaymentStatus returns failure when API throws exception`() = runTest {
        // Arrange
        val transactionId = "test_transaction_id"

        whenever(mockApiService.getPaymentStatus(transactionId))
            .thenThrow(RuntimeException("Network error"))

        // Act
        val result = realPaymentGateway.getPaymentStatus(transactionId)

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `unknown API status defaults to PENDING`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "UNKNOWN_STATUS",
            paymentMethod = "CREDIT_CARD",
            amount = "100.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.PENDING, result.getOrNull()?.status)
    }

    @Test
    fun `unknown API payment method defaults to CREDIT_CARD`() = runTest {
        // Arrange
        val request = PaymentRequest(
            amount = BigDecimal("100.00"),
            description = "Test payment",
            customerId = "test_customer",
            paymentMethod = PaymentMethod.CREDIT_CARD
        )

        val apiResponse = com.example.iurankomplek.network.model.PaymentResponse(
            transactionId = "test_transaction_id",
            status = "COMPLETED",
            paymentMethod = "UNKNOWN_METHOD",
            amount = "100.00",
            currency = "IDR",
            transactionTime = System.currentTimeMillis(),
            referenceNumber = "REF123"
        )

        whenever(mockApiService.initiatePayment(any(), any(), any(), any()))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = realPaymentGateway.processPayment(request)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(PaymentMethod.CREDIT_CARD, result.getOrNull()?.paymentMethod)
    }
}
