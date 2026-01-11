package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions

class RefundPaymentUseCaseTest {

    @Mock
    private lateinit var mockRepository: TransactionRepository

    private lateinit var useCase: RefundPaymentUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = RefundPaymentUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment(
            "tx123", "Customer requested refund"
        )

        val result = useCase("tx123", "Customer requested refund")

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(mockRepository).refundPayment("tx123", "Customer requested refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with valid transaction ID and reason`() = runTest {
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment(
            "tx456", "Product damaged"
        )

        val result = useCase("tx456", "Product damaged")

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(mockRepository).refundPayment("tx456", "Product damaged")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        val exception = RuntimeException("Refund failed")
        doReturn(OperationResult.Error(exception, "Refund failed")).`when`(mockRepository).refundPayment(
            "tx123", "Customer requested refund"
        )

        val result = useCase("tx123", "Customer requested refund")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).refundPayment("tx123", "Customer requested refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws exception`() = runTest {
        val exception = IllegalStateException("Transaction not found")
        doThrow(exception).`when`(mockRepository).refundPayment("tx123", "Customer requested refund")

        val result = useCase("tx123", "Customer requested refund")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalStateException)
        assertEquals("Transaction not found", error?.message)
        verify(mockRepository).refundPayment("tx123", "Customer requested refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Network error during refund")
        doThrow(exception).`when`(mockRepository).refundPayment("tx123", "Customer requested refund")

        val result = useCase("tx123", "Customer requested refund")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is java.io.IOException)
        assertEquals("Network error during refund", error?.message)
        verify(mockRepository).refundPayment("tx123", "Customer requested refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with empty reason`() = runTest {
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", "")

        val result = useCase("tx123", "")

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(mockRepository).refundPayment("tx123", "")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with whitespace reason`() = runTest {
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", "   ")

        val result = useCase("tx123", "   ")

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(mockRepository).refundPayment("tx123", "   ")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with long reason`() = runTest {
        val longReason = "a".repeat(1000)
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", longReason)

        val result = useCase("tx123", longReason)

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(mockRepository).refundPayment("tx123", longReason)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with reason containing special characters`() = runTest {
        val specialReason = "Product was damaged! @#$%^&*()"
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", specialReason)

        val result = useCase("tx123", specialReason)

        assertTrue(result.isSuccess)
        verify(mockRepository).refundPayment("tx123", specialReason)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with reason containing unicode characters`() = runTest {
        val unicodeReason = "产品损坏 退货"
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", unicodeReason)

        val result = useCase("tx123", unicodeReason)

        assertTrue(result.isSuccess)
        verify(mockRepository).refundPayment("tx123", unicodeReason)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with reason containing numbers`() = runTest {
        val numericReason = "Refund request #12345"
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", numericReason)

        val result = useCase("tx123", numericReason)

        assertTrue(result.isSuccess)
        verify(mockRepository).refundPayment("tx123", numericReason)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with numeric transaction ID`() = runTest {
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("12345", "Refund")

        val result = useCase("12345", "Refund")

        assertTrue(result.isSuccess)
        verify(mockRepository).refundPayment("12345", "Refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with alphanumeric transaction ID`() = runTest {
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx_abc_123", "Refund")

        val result = useCase("tx_abc_123", "Refund")

        assertTrue(result.isSuccess)
        verify(mockRepository).refundPayment("tx_abc_123", "Refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with UUID transaction ID`() = runTest {
        val uuid = "550e8400-e29b-41d4-a716-446655440000"
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment(uuid, "Refund")

        val result = useCase(uuid, "Refund")

        assertTrue(result.isSuccess)
        verify(mockRepository).refundPayment(uuid, "Refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with descriptive message`() = runTest {
        val exception = RuntimeException("Refund failed: transaction already refunded")
        doReturn(OperationResult.Error(exception, "Refund failed: transaction already refunded")).`when`(
            mockRepository
        ).refundPayment("tx123", "Duplicate refund")

        val result = useCase("tx123", "Duplicate refund")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals("Refund failed: transaction already refunded", result.exceptionOrNull()?.message)
        verify(mockRepository).refundPayment("tx123", "Duplicate refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles null repository response gracefully`() = runTest {
        val exception = NullPointerException("Repository returned null")
        doReturn(OperationResult.Error(exception, "Repository returned null")).`when`(
            mockRepository
        ).refundPayment("tx123", "Refund")

        val result = useCase("tx123", "Refund")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NullPointerException)
        verify(mockRepository).refundPayment("tx123", "Refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with reason containing newlines`() = runTest {
        val multilineReason = "Reason line 1\nReason line 2\nReason line 3"
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", multilineReason)

        val result = useCase("tx123", multilineReason)

        assertTrue(result.isSuccess)
        verify(mockRepository).refundPayment("tx123", multilineReason)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with reason containing emoji`() = runTest {
        val emojiReason = "Product was damaged 😢 Customer unhappy 😞"
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", emojiReason)

        val result = useCase("tx123", emojiReason)

        assertTrue(result.isSuccess)
        verify(mockRepository).refundPayment("tx123", emojiReason)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with reason containing URL`() = runTest {
        val urlReason = "See proof at https://example.com/evidence"
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", urlReason)

        val result = useCase("tx123", urlReason)

        assertTrue(result.isSuccess)
        verify(mockRepository).refundPayment("tx123", urlReason)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws validation exception`() = runTest {
        val exception = IllegalArgumentException("Invalid transaction ID format")
        doThrow(exception).`when`(mockRepository).refundPayment("", "Refund")

        val result = useCase("", "Refund")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertEquals("Invalid transaction ID format", error?.message)
        verify(mockRepository).refundPayment("", "Refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws timeout exception`() = runTest {
        val exception = java.util.concurrent.TimeoutException("Refund processing timeout")
        doThrow(exception).`when`(mockRepository).refundPayment("tx123", "Refund")

        val result = useCase("tx123", "Refund")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is java.util.concurrent.TimeoutException)
        assertEquals("Refund processing timeout", error?.message)
        verify(mockRepository).refundPayment("tx123", "Refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success when repository returns successful operation result`() = runTest {
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment(
            "tx789", "Customer changed mind"
        )

        val result = useCase("tx789", "Customer changed mind")

        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
        assertNotNull(result.getOrNull())
        assertNull(result.exceptionOrNull())
        verify(mockRepository).refundPayment("tx789", "Customer changed mind")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository returns error operation result`() = runTest {
        val exception = RuntimeException("Payment gateway error")
        doReturn(OperationResult.Error(exception, "Payment gateway error")).`when`(
            mockRepository
        ).refundPayment("tx123", "Refund")

        val result = useCase("tx123", "Refund")

        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
        assertNull(result.getOrNull())
        assertNotNull(result.exceptionOrNull())
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).refundPayment("tx123", "Refund")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with common refund reasons`() = runTest {
        val commonReasons = listOf(
            "Customer requested refund",
            "Product damaged",
            "Wrong item delivered",
            "Delivery late",
            "Customer changed mind",
            "Duplicate order"
        )

        commonReasons.forEach { reason ->
            doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", reason)
            val result = useCase("tx123", reason)
            assertTrue("Failed for reason: $reason", result.isSuccess)
            verify(mockRepository).refundPayment("tx123", reason)
        }
    }

    @Test
    fun `invoke handles single word reason`() = runTest {
        val singleWordReason = "Damaged"
        doReturn(OperationResult.Success(Unit)).`when`(mockRepository).refundPayment("tx123", singleWordReason)

        val result = useCase("tx123", singleWordReason)

        assertTrue(result.isSuccess)
        verify(mockRepository).refundPayment("tx123", singleWordReason)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with null transaction ID`() = runTest {
        val exception = IllegalArgumentException("Transaction ID cannot be null")
        doThrow(exception).`when`(mockRepository).refundPayment(null, "Refund")

        val result = useCase(null, "Refund")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        verify(mockRepository).refundPayment(null, "Refund")
        verifyNoMoreInteractions(mockRepository)
    }
}
