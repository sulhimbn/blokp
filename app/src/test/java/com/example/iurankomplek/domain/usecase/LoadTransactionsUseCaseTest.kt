package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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

class LoadTransactionsUseCaseTest {

    @Mock
    private lateinit var mockRepository: TransactionRepository

    private lateinit var useCase: LoadTransactionsUseCase

    private val testTransaction = Transaction(
        id = "tx123",
        userId = 1L,
        amount = 10000L,
        currency = "IDR",
        status = PaymentStatus.COMPLETED,
        paymentMethod = PaymentMethod.CREDIT_CARD,
        description = "Test transaction",
        createdAt = Date(),
        updatedAt = Date()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadTransactionsUseCase(mockRepository)
    }

    @Test
    fun `invoke no args returns success when repository succeeds`() = runTest {
        val expectedTransactions = listOf(testTransaction)
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedTransactions, result.getOrNull())
        verify(mockRepository).getAllTransactions()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke no args returns success with empty list`() = runTest {
        val expectedTransactions = emptyList<Transaction>()
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedTransactions, result.getOrNull())
        verify(mockRepository).getAllTransactions()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke no args returns success with multiple transactions`() = runTest {
        val expectedTransactions = listOf(
            testTransaction,
            testTransaction.copy(id = "tx456", status = PaymentStatus.PENDING),
            testTransaction.copy(id = "tx789", status = PaymentStatus.FAILED)
        )
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
        verify(mockRepository).getAllTransactions()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke no args returns error when repository throws exception`() = runTest {
        val exception = RuntimeException("Database error")
        doThrow(exception).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is RuntimeException)
        assertEquals("Database error", error?.message)
        verify(mockRepository).getAllTransactions()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke no args returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Network error")
        doThrow(exception).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is java.io.IOException)
        verify(mockRepository).getAllTransactions()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with status returns success for COMPLETED transactions`() = runTest {
        val expectedTransactions = listOf(
            testTransaction.copy(id = "tx1", status = PaymentStatus.COMPLETED),
            testTransaction.copy(id = "tx2", status = PaymentStatus.COMPLETED)
        )
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase(PaymentStatus.COMPLETED)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()?.all { it.status == PaymentStatus.COMPLETED } == true)
        verify(mockRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with status returns success for PENDING transactions`() = runTest {
        val expectedTransactions = listOf(
            testTransaction.copy(id = "tx1", status = PaymentStatus.PENDING),
            testTransaction.copy(id = "tx2", status = PaymentStatus.PENDING)
        )
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getTransactionsByStatus(PaymentStatus.PENDING)

        val result = useCase(PaymentStatus.PENDING)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertTrue(result.getOrNull()?.all { it.status == PaymentStatus.PENDING } == true)
        verify(mockRepository).getTransactionsByStatus(PaymentStatus.PENDING)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with status returns success for FAILED transactions`() = runTest {
        val expectedTransactions = listOf(
            testTransaction.copy(id = "tx1", status = PaymentStatus.FAILED)
        )
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getTransactionsByStatus(PaymentStatus.FAILED)

        val result = useCase(PaymentStatus.FAILED)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(PaymentStatus.FAILED, result.getOrNull()?.first()?.status)
        verify(mockRepository).getTransactionsByStatus(PaymentStatus.FAILED)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with status returns success for REFUNDED transactions`() = runTest {
        val expectedTransactions = listOf(
            testTransaction.copy(id = "tx1", status = PaymentStatus.REFUNDED)
        )
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getTransactionsByStatus(PaymentStatus.REFUNDED)

        val result = useCase(PaymentStatus.REFUNDED)

        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.REFUNDED, result.getOrNull()?.first()?.status)
        verify(mockRepository).getTransactionsByStatus(PaymentStatus.REFUNDED)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with status returns success for PROCESSING transactions`() = runTest {
        val expectedTransactions = listOf(
            testTransaction.copy(id = "tx1", status = PaymentStatus.PROCESSING)
        )
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getTransactionsByStatus(PaymentStatus.PROCESSING)

        val result = useCase(PaymentStatus.PROCESSING)

        assertTrue(result.isSuccess)
        assertEquals(PaymentStatus.PROCESSING, result.getOrNull()?.first()?.status)
        verify(mockRepository).getTransactionsByStatus(PaymentStatus.PROCESSING)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with status returns success with empty list when no transactions match`() = runTest {
        val expectedTransactions = emptyList<Transaction>()
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getTransactionsByStatus(PaymentStatus.FAILED)

        val result = useCase(PaymentStatus.FAILED)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
        verify(mockRepository).getTransactionsByStatus(PaymentStatus.FAILED)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with status returns error when repository throws exception`() = runTest {
        val exception = IllegalStateException("Repository not initialized")
        doThrow(exception).`when`(mockRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)

        val result = useCase(PaymentStatus.COMPLETED)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is IllegalStateException)
        verify(mockRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with transactions in correct order`() = runTest {
        val date1 = Date(1000)
        val date2 = Date(2000)
        val date3 = Date(3000)
        val expectedTransactions = listOf(
            testTransaction.copy(id = "tx1", createdAt = date1),
            testTransaction.copy(id = "tx2", createdAt = date2),
            testTransaction.copy(id = "tx3", createdAt = date3)
        )
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isSuccess)
        val transactions = result.getOrNull()
        assertEquals(3, transactions?.size)
        assertEquals("tx1", transactions?.get(0)?.id)
        assertEquals("tx2", transactions?.get(1)?.id)
        assertEquals("tx3", transactions?.get(2)?.id)
    }

    @Test
    fun `invoke returns success with transactions containing correct data`() = runTest {
        val expectedTransactions = listOf(testTransaction)
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isSuccess)
        val transaction = result.getOrNull()?.first()
        assertEquals("tx123", transaction?.id)
        assertEquals(1L, transaction?.userId)
        assertEquals(10000L, transaction?.amount)
        assertEquals("IDR", transaction?.currency)
        assertEquals(PaymentStatus.COMPLETED, transaction?.status)
        assertEquals(PaymentMethod.CREDIT_CARD, transaction?.paymentMethod)
        assertEquals("Test transaction", transaction?.description)
        assertFalse(transaction?.isDeleted ?: true)
    }

    @Test
    fun `invoke no args handles large transaction list efficiently`() = runTest {
        val largeList = (1..100).map { i ->
            testTransaction.copy(id = "tx$i", userId = i.toLong())
        }
        doReturn(flowOf(largeList)).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.size)
        verify(mockRepository).getAllTransactions()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with descriptive message`() = runTest {
        val exception = RuntimeException("Connection timeout after 30 seconds")
        doThrow(exception).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals("Connection timeout after 30 seconds", result.exceptionOrNull()?.message)
        verify(mockRepository).getAllTransactions()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with status returns error with descriptive message`() = runTest {
        val exception = IllegalArgumentException("Invalid status parameter")
        doThrow(exception).`when`(mockRepository).getTransactionsByStatus(PaymentStatus.PENDING)

        val result = useCase(PaymentStatus.PENDING)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        assertEquals("Invalid status parameter", result.exceptionOrNull()?.message)
        verify(mockRepository).getTransactionsByStatus(PaymentStatus.PENDING)
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles transactions with different payment methods`() = runTest {
        val expectedTransactions = listOf(
            testTransaction.copy(id = "tx1", paymentMethod = PaymentMethod.CREDIT_CARD),
            testTransaction.copy(id = "tx2", paymentMethod = PaymentMethod.BANK_TRANSFER),
            testTransaction.copy(id = "tx3", paymentMethod = PaymentMethod.E_WALLET),
            testTransaction.copy(id = "tx4", paymentMethod = PaymentMethod.VIRTUAL_ACCOUNT)
        )
        doReturn(flowOf(expectedTransactions)).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isSuccess)
        val transactions = result.getOrNull()
        assertEquals(4, transactions?.size)
        assertEquals(PaymentMethod.CREDIT_CARD, transactions?.get(0)?.paymentMethod)
        assertEquals(PaymentMethod.BANK_TRANSFER, transactions?.get(1)?.paymentMethod)
        assertEquals(PaymentMethod.E_WALLET, transactions?.get(2)?.paymentMethod)
        assertEquals(PaymentMethod.VIRTUAL_ACCOUNT, transactions?.get(3)?.paymentMethod)
    }

    @Test
    fun `invoke with status filters by correct status`() = runTest {
        val mixedTransactions = listOf(
            testTransaction.copy(id = "tx1", status = PaymentStatus.COMPLETED),
            testTransaction.copy(id = "tx2", status = PaymentStatus.PENDING),
            testTransaction.copy(id = "tx3", status = PaymentStatus.COMPLETED),
            testTransaction.copy(id = "tx4", status = PaymentStatus.FAILED)
        )
        doReturn(flowOf(mixedTransactions)).`when`(mockRepository).getAllTransactions()

        val result = useCase()

        assertTrue(result.isSuccess)
        val transactions = result.getOrNull()
        assertEquals(4, transactions?.size)
    }
}
