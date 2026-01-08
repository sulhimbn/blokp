package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.transaction.Transaction
import com.example.iurankomplek.payment.PaymentStatus
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
import java.util.Date

class PaymentSummaryIntegrationUseCaseTest {

    @Mock
    private lateinit var mockTransactionRepository: com.example.iurankomplek.data.repository.TransactionRepository

    private lateinit var useCase: PaymentSummaryIntegrationUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = PaymentSummaryIntegrationUseCase(mockTransactionRepository)
    }

    @Test
    fun `invoke returns zero payment total for no completed transactions`() = runTest {
        val completedTransactions = emptyList<Transaction>()

        doReturn(kotlinx.coroutines.flow.FlowKt.emptyFlow()).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(0, result.paymentTotal)
        assertEquals(0, result.transactionCount)
        assertFalse(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke returns correct payment total for completed transactions`() = runTest {
        val completedTransactions = listOf(
            Transaction(
                id = "1",
                amount = "100000",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "2",
                amount = "50000",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        )

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(completedTransactions)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(150000, result.paymentTotal)
        assertEquals(2, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke handles exception during transaction fetch`() = runTest {
        val exception = RuntimeException("Database error")
        doThrow(exception).`when`(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(0, result.paymentTotal)
        assertEquals(0, result.transactionCount)
        assertFalse(result.isIntegrated)
        assertNotNull(result.error)
        assertTrue(result.error?.contains("Failed to integrate payment data") ?: false)
        assertTrue(result.error?.contains(exception.message) ?: false)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke returns correct total for large payment amounts`() = runTest {
        val largeAmount = 1000000
        val completedTransactions = listOf(
            Transaction(
                id = "1",
                amount = largeAmount.toString(),
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "2",
                amount = largeAmount.toString(),
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "3",
                amount = largeAmount.toString(),
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        )

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(completedTransactions)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(3000000, result.paymentTotal)
        assertEquals(3, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke handles zero amount transactions`() = runTest {
        val zeroAmountTransactions = listOf(
            Transaction(
                id = "1",
                amount = "0",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "2",
                amount = "0",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        )

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(zeroAmountTransactions)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(0, result.paymentTotal)
        assertEquals(2, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke calculates payment total correctly for mixed amounts`() = runTest {
        val mixedAmountTransactions = listOf(
            Transaction(
                id = "1",
                amount = "100000",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "2",
                amount = "50000",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "3",
                amount = "75000",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        )

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(mixedAmountTransactions)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(225000, result.paymentTotal)
        assertEquals(3, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }
}
