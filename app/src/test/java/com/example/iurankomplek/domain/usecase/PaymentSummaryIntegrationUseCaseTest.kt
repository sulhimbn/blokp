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

    @Test
    fun `invoke handles large transaction count`() = runTest {
        val manyTransactions = (1..100).map { index ->
            Transaction(
                id = index.toString(),
                amount = "50000",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        }

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(manyTransactions)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(5000000, result.paymentTotal)
        assertEquals(100, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke handles near-maximum int values`() = runTest {
        val nearMaxInt = Int.MAX_VALUE / 2
        val largeTransactions = listOf(
            Transaction(
                id = "1",
                amount = nearMaxInt.toString(),
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "2",
                amount = nearMaxInt.toString(),
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        )

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(largeTransactions)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(nearMaxInt * 2, result.paymentTotal)
        assertEquals(2, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke handles decimal amount truncation`() = runTest {
        val decimalTransactions = listOf(
            Transaction(
                id = "1",
                amount = "100000.50",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "2",
                amount = "50000.99",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "3",
                amount = "75000.33",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        )

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(decimalTransactions)).`when`(
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

    @Test
    fun `invoke handles very small amounts`() = runTest {
        val smallTransactions = listOf(
            Transaction(
                id = "1",
                amount = "1",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "2",
                amount = "2",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "3",
                amount = "3",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        )

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(smallTransactions)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(6, result.paymentTotal)
        assertEquals(3, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke handles alternating small and large amounts`() = runTest {
        val alternatingTransactions = listOf(
            Transaction(
                id = "1",
                amount = "1000000",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "2",
                amount = "1000",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "3",
                amount = "5000000",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            ),
            Transaction(
                id = "4",
                amount = "500",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        )

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(alternatingTransactions)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(6001500, result.paymentTotal)
        assertEquals(4, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke handles single large transaction`() = runTest {
        val singleLargeTransaction = listOf(
            Transaction(
                id = "1",
                amount = "10000000",
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        )

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(singleLargeTransaction)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(10000000, result.paymentTotal)
        assertEquals(1, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke handles boundary value just below overflow threshold`() = runTest {
        val boundaryValue = Int.MAX_VALUE / 2
        val boundaryTransactions = listOf(
            Transaction(
                id = "1",
                amount = boundaryValue.toString(),
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        )

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(boundaryTransactions)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(boundaryValue, result.paymentTotal)
        assertEquals(1, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }

    @Test
    fun `invoke calculates total for repeating same amounts`() = runTest {
        val sameAmount = "25000"
        val repeatingTransactions = (1..50).map { index ->
            Transaction(
                id = index.toString(),
                amount = sameAmount,
                status = PaymentStatus.COMPLETED,
                timestamp = Date()
            )
        }

        doReturn(kotlinx.coroutines.flow.FlowKt.flowOf(repeatingTransactions)).`when`(
            mockTransactionRepository
        ).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase()

        assertEquals(25000 * 50, result.paymentTotal)
        assertEquals(50, result.transactionCount)
        assertTrue(result.isIntegrated)
        assertNull(result.error)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockTransactionRepository)
    }
}
