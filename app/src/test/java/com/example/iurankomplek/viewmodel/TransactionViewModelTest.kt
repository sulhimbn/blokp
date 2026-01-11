package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException
import java.math.BigDecimal
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class TransactionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var viewModel: TransactionViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val now = Date()

    private val sampleTransactions = listOf(
        Transaction(
            id = "txn-001",
            userId = "user-001",
            amount = BigDecimal("100000"),
            currency = "IDR",
            status = PaymentStatus.PENDING,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Monthly fee",
            createdAt = now,
            updatedAt = now,
            metadata = mapOf("invoice" to "INV-001")
        ),
        Transaction(
            id = "txn-002",
            userId = "user-002",
            amount = BigDecimal("150000"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.E_WALLET,
            description = "Late fee payment",
            createdAt = now,
            updatedAt = now,
            metadata = mapOf("invoice" to "INV-002")
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TransactionViewModel(transactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAllTransactions should emit Loading state initially`() = runTest {
        // Given
        Mockito.`when`(transactionRepository.getAllTransactions())
            .thenReturn(flowOf(sampleTransactions))

        // When
        viewModel.loadAllTransactions()

        // Then
        val loadingState = viewModel.transactionsState.value
        assertTrue(loadingState is UiState.Loading)
    }

    @Test
    fun `loadAllTransactions should emit Success state with all transactions`() = runTest {
        // Given
        Mockito.`when`(transactionRepository.getAllTransactions())
            .thenReturn(flowOf(sampleTransactions))

        // When
        viewModel.loadAllTransactions()

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.size)
        assertEquals("txn-001", state.data[0].id)
        assertEquals("txn-002", state.data[1].id)
    }

    @Test
    fun `loadAllTransactions should emit Error state when repository throws exception`() = runTest {
        // Given
        val errorMessage = "Database error occurred"
        Mockito.`when`(transactionRepository.getAllTransactions())
            .thenThrow(IOException(errorMessage))

        // When
        viewModel.loadAllTransactions()

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }

    @Test
    fun `loadAllTransactions should handle empty transaction list`() = runTest {
        // Given
        Mockito.`when`(transactionRepository.getAllTransactions())
            .thenReturn(flowOf(emptyList()))

        // When
        viewModel.loadAllTransactions()

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.isEmpty())
    }

    @Test
    fun `loadTransactionsByStatus should emit Loading state initially`() = runTest {
        // Given
        val pendingTransactions = sampleTransactions.filter { it.status == PaymentStatus.PENDING }
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.PENDING))
            .thenReturn(flowOf(pendingTransactions))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.PENDING)

        // Then
        val loadingState = viewModel.transactionsState.value
        assertTrue(loadingState is UiState.Loading)
    }

    @Test
    fun `loadTransactionsByStatus with PENDING should filter transactions correctly`() = runTest {
        // Given
        val pendingTransactions = sampleTransactions.filter { it.status == PaymentStatus.PENDING }
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.PENDING))
            .thenReturn(flowOf(pendingTransactions))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.PENDING)

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).data.size)
        assertEquals(PaymentStatus.PENDING, state.data[0].status)
        assertEquals("txn-001", state.data[0].id)
    }

    @Test
    fun `loadTransactionsByStatus with COMPLETED should filter transactions correctly`() = runTest {
        // Given
        val completedTransactions = sampleTransactions.filter { it.status == PaymentStatus.COMPLETED }
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(completedTransactions))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.COMPLETED)

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).data.size)
        assertEquals(PaymentStatus.COMPLETED, state.data[0].status)
        assertEquals("txn-002", state.data[0].id)
    }

    @Test
    fun `loadTransactionsByStatus should emit Error state when repository throws exception`() = runTest {
        // Given
        val errorMessage = "Network error occurred"
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.FAILED))
            .thenThrow(IOException(errorMessage))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.FAILED)

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }

    @Test
    fun `loadTransactionsByStatus should handle empty filtered results`() = runTest {
        // Given
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.REFUNDED))
            .thenReturn(flowOf(emptyList()))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.REFUNDED)

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.isEmpty())
    }

    @Test
    fun `loadTransactionsByStatus with PROCESSING should handle status correctly`() = runTest {
        // Given
        val processingTransaction = Transaction(
            id = "txn-003",
            userId = "user-003",
            amount = BigDecimal("200000"),
            currency = "IDR",
            status = PaymentStatus.PROCESSING,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            description = "Processing payment",
            createdAt = now,
            updatedAt = now,
            metadata = emptyMap()
        )
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.PROCESSING))
            .thenReturn(flowOf(listOf(processingTransaction)))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.PROCESSING)

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).data.size)
        assertEquals(PaymentStatus.PROCESSING, state.data[0].status)
    }

    @Test
    fun `loadTransactionsByStatus with FAILED should handle status correctly`() = runTest {
        // Given
        val failedTransaction = Transaction(
            id = "txn-004",
            userId = "user-004",
            amount = BigDecimal("50000"),
            currency = "IDR",
            status = PaymentStatus.FAILED,
            paymentMethod = PaymentMethod.VIRTUAL_ACCOUNT,
            description = "Failed payment",
            createdAt = now,
            updatedAt = now,
            metadata = mapOf("error" to "insufficient_funds")
        )
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.FAILED))
            .thenReturn(flowOf(listOf(failedTransaction)))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.FAILED)

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        val successState = state as UiState.Success
        assertEquals(1, successState.data.size)
        assertEquals(PaymentStatus.FAILED, successState.data[0].status)
    }

    @Test
    fun `loadTransactionsByStatus with CANCELLED should handle status correctly`() = runTest {
        // Given
        val cancelledTransaction = Transaction(
            id = "txn-005",
            userId = "user-005",
            amount = BigDecimal("75000"),
            currency = "IDR",
            status = PaymentStatus.CANCELLED,
            paymentMethod = PaymentMethod.CREDIT_CARD,
            description = "Cancelled payment",
            createdAt = now,
            updatedAt = now,
            metadata = mapOf("reason" to "user_requested")
        )
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.CANCELLED))
            .thenReturn(flowOf(listOf(cancelledTransaction)))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.CANCELLED)

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).data.size)
        assertEquals(PaymentStatus.CANCELLED, state.data[0].status)
    }

    @Test
    fun `loadTransactionsByStatus with REFUNDED should handle status correctly`() = runTest {
        // Given
        val refundedTransaction = Transaction(
            id = "txn-006",
            userId = "user-006",
            amount = BigDecimal("120000"),
            currency = "IDR",
            status = PaymentStatus.REFUNDED,
            paymentMethod = PaymentMethod.E_WALLET,
            description = "Refunded payment",
            createdAt = now,
            updatedAt = now,
            metadata = mapOf("refundId" to "ref-001")
        )
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.REFUNDED))
            .thenReturn(flowOf(listOf(refundedTransaction)))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.REFUNDED)

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).data.size)
        assertEquals(PaymentStatus.REFUNDED, state.data[0].status)
    }

    @Test
    fun `loadAllTransactions should handle transactions with different payment methods`() = runTest {
        // Given
        val transactions = listOf(
            Transaction(
                id = "txn-001",
                userId = "user-001",
                amount = BigDecimal("100000"),
                currency = "IDR",
                status = PaymentStatus.PENDING,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                description = "Credit card payment",
                createdAt = now,
                updatedAt = now,
                metadata = emptyMap()
            ),
            Transaction(
                id = "txn-002",
                userId = "user-002",
                amount = BigDecimal("150000"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                description = "Bank transfer payment",
                createdAt = now,
                updatedAt = now,
                metadata = emptyMap()
            ),
            Transaction(
                id = "txn-003",
                userId = "user-003",
                amount = BigDecimal("200000"),
                currency = "IDR",
                status = PaymentStatus.PROCESSING,
                paymentMethod = PaymentMethod.E_WALLET,
                description = "E-wallet payment",
                createdAt = now,
                updatedAt = now,
                metadata = emptyMap()
            ),
            Transaction(
                id = "txn-004",
                userId = "user-004",
                amount = BigDecimal("50000"),
                currency = "IDR",
                status = PaymentStatus.FAILED,
                paymentMethod = PaymentMethod.VIRTUAL_ACCOUNT,
                description = "Virtual account payment",
                createdAt = now,
                updatedAt = now,
                metadata = emptyMap()
            )
        )
        Mockito.`when`(transactionRepository.getAllTransactions())
            .thenReturn(flowOf(transactions))

        // When
        viewModel.loadAllTransactions()

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertEquals(4, (state as UiState.Success).data.size)
        assertEquals(PaymentMethod.CREDIT_CARD, state.data[0].paymentMethod)
        assertEquals(PaymentMethod.BANK_TRANSFER, state.data[1].paymentMethod)
        assertEquals(PaymentMethod.E_WALLET, state.data[2].paymentMethod)
        assertEquals(PaymentMethod.VIRTUAL_ACCOUNT, state.data[3].paymentMethod)
    }

    @Test
    fun `loadAllTransactions should preserve transaction metadata`() = runTest {
        // Given
        val transactionsWithMetadata = sampleTransactions.map { tx ->
            tx.copy(metadata = mapOf(
                "invoice" to "INV-${tx.id}",
                "customer" to "customer-${tx.userId}",
                "tags" to "test,qa"
            ))
        }
        Mockito.`when`(transactionRepository.getAllTransactions())
            .thenReturn(flowOf(transactionsWithMetadata))

        // When
        viewModel.loadAllTransactions()

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        val metadata = (state as UiState.Success).data[0].metadata
        assertEquals("INV-txn-001", metadata["invoice"])
        assertEquals("customer-user-001", metadata["customer"])
        assertEquals("test,qa", metadata["tags"])
    }

    @Test
    fun `loadTransactionsByStatus should handle large transaction list efficiently`() = runTest {
        // Given
        val largeTransactionList = (1..100).map { index ->
            Transaction(
                id = "txn-$index",
                userId = "user-$index",
                amount = BigDecimal(index * 1000L),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                description = "Transaction $index",
                createdAt = now,
                updatedAt = now,
                metadata = emptyMap()
            )
        }
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(largeTransactionList))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.COMPLETED)

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertEquals(100, (state as UiState.Success).data.size)
    }

    @Test
    fun `loadTransactionsByStatus should handle different currencies`() = runTest {
        // Given
        val transactions = listOf(
            Transaction(
                id = "txn-001",
                userId = "user-001",
                amount = BigDecimal("100000"),
                currency = "IDR",
                status = PaymentStatus.PENDING,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                description = "IDR payment",
                createdAt = now,
                updatedAt = now,
                metadata = emptyMap()
            ),
            Transaction(
                id = "txn-002",
                userId = "user-002",
                amount = BigDecimal("100"),
                currency = "USD",
                status = PaymentStatus.PENDING,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                description = "USD payment",
                createdAt = now,
                updatedAt = now,
                metadata = emptyMap()
            )
        )
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.PENDING))
            .thenReturn(flowOf(transactions))

        // When
        viewModel.loadTransactionsByStatus(PaymentStatus.PENDING)

        // Then
        advanceUntilIdle()
        val state = viewModel.transactionsState.value
        assertTrue(state is UiState.Success)
        assertEquals(2, (state as UiState.Success).data.size)
        assertEquals("IDR", state.data[0].currency)
        assertEquals("USD", state.data[1].currency)
    }
}
