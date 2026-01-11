package com.example.iurankomplek.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.entity.Transaction
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var mockTransactionRepository: TransactionRepository

    private lateinit var viewModel: TransactionViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = TransactionViewModel(mockTransactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAllTransactions should set Loading then Success states`() = runTest {
        val transactions = listOf(
            Transaction(id = "1", amount = 10000, status = PaymentStatus.COMPLETED, createdAt = Date()),
            Transaction(id = "2", amount = 20000, status = PaymentStatus.PENDING, createdAt = Date())
        )
        whenever(mockTransactionRepository.getAllTransactions()).thenReturn(flowOf(transactions))

        val states = mutableListOf<UiState<List<Transaction>> >()
        val job = launch {
            viewModel.transactionsState.collect { states.add(it) }
        }

        viewModel.loadAllTransactions()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Success state", states.any { it is UiState.Success })
        val successState = states.filterIsInstance<UiState.Success<List<Transaction>>>().last()
        assertEquals(transactions, successState.data)
        job.cancel()
    }

    @Test
    fun `loadAllTransactions should set Error state when repository throws exception`() = runTest {
        whenever(mockTransactionRepository.getAllTransactions()).thenReturn(
            flow { throw RuntimeException("Database error") }
        )

        val states = mutableListOf<UiState<List<Transaction>> >()
        val job = launch {
            viewModel.transactionsState.collect { states.add(it) }
        }

        viewModel.loadAllTransactions()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Error state", states.any { it is UiState.Error })
        val errorState = states.filterIsInstance<UiState.Error<List<Transaction>>>().last()
        assertTrue("Error message should contain error", errorState.message.contains("error"))
        job.cancel()
    }

    @Test
    fun `loadAllTransactions should set Error state for empty flow`() = runTest {
        whenever(mockTransactionRepository.getAllTransactions()).thenReturn(emptyFlow())

        val states = mutableListOf<UiState<List<Transaction>> >()
        val job = launch {
            viewModel.transactionsState.collect { states.add(it) }
        }

        viewModel.loadAllTransactions()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Error state", states.any { it is UiState.Error })
        val errorState = states.filterIsInstance<UiState.Error<List<Transaction>>>().last()
        assertEquals("No data available", errorState.message)
        job.cancel()
    }

    @Test
    fun `loadTransactionsByStatus should filter transactions by COMPLETED status`() = runTest {
        val completedTransactions = listOf(
            Transaction(id = "1", amount = 10000, status = PaymentStatus.COMPLETED, createdAt = Date()),
            Transaction(id = "2", amount = 20000, status = PaymentStatus.COMPLETED, createdAt = Date())
        )
        whenever(mockTransactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(completedTransactions))

        val states = mutableListOf<UiState<List<Transaction>> >()
        val job = launch {
            viewModel.transactionsState.collect { states.add(it) }
        }

        viewModel.loadTransactionsByStatus(PaymentStatus.COMPLETED)
        advanceUntilIdle()

        val successState = states.filterIsInstance<UiState.Success<List<Transaction>>>().last()
        assertEquals(2, successState.data.size)
        successState.data.forEach { assertEquals(PaymentStatus.COMPLETED, it.status) }
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        job.cancel()
    }

    @Test
    fun `loadTransactionsByStatus should filter transactions by PENDING status`() = runTest {
        val pendingTransactions = listOf(
            Transaction(id = "1", amount = 10000, status = PaymentStatus.PENDING, createdAt = Date()),
            Transaction(id = "2", amount = 20000, status = PaymentStatus.PENDING, createdAt = Date())
        )
        whenever(mockTransactionRepository.getTransactionsByStatus(PaymentStatus.PENDING))
            .thenReturn(flowOf(pendingTransactions))

        val states = mutableListOf<UiState<List<Transaction>> >()
        val job = launch {
            viewModel.transactionsState.collect { states.add(it) }
        }

        viewModel.loadTransactionsByStatus(PaymentStatus.PENDING)
        advanceUntilIdle()

        val successState = states.filterIsInstance<UiState.Success<List<Transaction>>>().last()
        assertEquals(2, successState.data.size)
        successState.data.forEach { assertEquals(PaymentStatus.PENDING, it.status) }
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.PENDING)
        job.cancel()
    }

    @Test
    fun `loadTransactionsByStatus should filter transactions by FAILED status`() = runTest {
        val failedTransactions = listOf(
            Transaction(id = "1", amount = 10000, status = PaymentStatus.FAILED, createdAt = Date()),
            Transaction(id = "2", amount = 20000, status = PaymentStatus.FAILED, createdAt = Date())
        )
        whenever(mockTransactionRepository.getTransactionsByStatus(PaymentStatus.FAILED))
            .thenReturn(flowOf(failedTransactions))

        val states = mutableListOf<UiState<List<Transaction>> >()
        val job = launch {
            viewModel.transactionsState.collect { states.add(it) }
        }

        viewModel.loadTransactionsByStatus(PaymentStatus.FAILED)
        advanceUntilIdle()

        val successState = states.filterIsInstance<UiState.Success<List<Transaction>>>().last()
        assertEquals(2, successState.data.size)
        successState.data.forEach { assertEquals(PaymentStatus.FAILED, it.status) }
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.FAILED)
        job.cancel()
    }

    @Test
    fun `loadTransactionsByStatus should filter transactions by REFUNDED status`() = runTest {
        val refundedTransactions = listOf(
            Transaction(id = "1", amount = 10000, status = PaymentStatus.REFUNDED, createdAt = Date())
        )
        whenever(mockTransactionRepository.getTransactionsByStatus(PaymentStatus.REFUNDED))
            .thenReturn(flowOf(refundedTransactions))

        val states = mutableListOf<UiState<List<Transaction>> >()
        val job = launch {
            viewModel.transactionsState.collect { states.add(it) }
        }

        viewModel.loadTransactionsByStatus(PaymentStatus.REFUNDED)
        advanceUntilIdle()

        val successState = states.filterIsInstance<UiState.Success<List<Transaction>>>().last()
        assertEquals(1, successState.data.size)
        assertEquals(PaymentStatus.REFUNDED, successState.data[0].status)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.REFUNDED)
        job.cancel()
    }

    @Test
    fun `loadTransactionsByStatus should handle empty result`() = runTest {
        whenever(mockTransactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(emptyFlow())

        val states = mutableListOf<UiState<List<Transaction>> >()
        val job = launch {
            viewModel.transactionsState.collect { states.add(it) }
        }

        viewModel.loadTransactionsByStatus(PaymentStatus.COMPLETED)
        advanceUntilIdle()

        assertTrue("Should have Error state", states.any { it is UiState.Error })
        val errorState = states.filterIsInstance<UiState.Error<List<Transaction>>>().last()
        assertEquals("No data available", errorState.message)
        job.cancel()
    }

    @Test
    fun `refundPayment should set Success state and reload all transactions`() = runTest {
        val transactionId = "txn_123"
        val reason = "Customer request"
        val transactions = listOf(
            Transaction(id = "1", amount = 10000, status = PaymentStatus.REFUNDED, createdAt = Date())
        )
        whenever(mockTransactionRepository.refundPayment(transactionId, reason)).thenReturn(Unit)
        whenever(mockTransactionRepository.getAllTransactions()).thenReturn(flowOf(transactions))

        val refundStates = mutableListOf<UiState<Unit>> >()
        val refundJob = launch {
            viewModel.refundState.collect { refundStates.add(it) }
        }

        val transactionStates = mutableListOf<UiState<List<Transaction>> >()
        val transactionJob = launch {
            viewModel.transactionsState.collect { transactionStates.add(it) }
        }

        viewModel.refundPayment(transactionId, reason)
        advanceUntilIdle()

        val successRefundState = refundStates.filterIsInstance<UiState.Success<Unit>>()  .firstOrNull()
        assertNotNull("Refund should be Success", successRefundState)
        verify(mockTransactionRepository).refundPayment(transactionId, reason)
        verify(mockTransactionRepository).getAllTransactions()
        refundJob.cancel()
        transactionJob.cancel()
    }

    @Test
    fun `refundPayment should set Error state when repository throws exception`() = runTest {
        val transactionId = "txn_123"
        val reason = "Customer request"
        whenever(mockTransactionRepository.refundPayment(transactionId, reason))
            .thenThrow(RuntimeException("Refund failed"))

        val refundStates = mutableListOf<UiState<Unit>> >()
        val job = launch {
            viewModel.refundState.collect { refundStates.add(it) }
        }

        viewModel.refundPayment(transactionId, reason)
        advanceUntilIdle()

        assertTrue("Should have Error state", refundStates.any { it is UiState.Error })
        val errorState = refundStates.filterIsInstance<UiState.Error<Unit>>()  .last()
        assertTrue("Error message should contain error", errorState.message.contains("error"))
        job.cancel()
    }

    @Test
    fun `refundPayment should handle null transaction id`() = runTest {
        whenever(mockTransactionRepository.refundPayment(null, any()))
            .thenThrow(IllegalArgumentException("Transaction ID cannot be null"))

        val refundStates = mutableListOf<UiState<Unit>> >()
        val job = launch {
            viewModel.refundState.collect { refundStates.add(it) }
        }

        viewModel.refundPayment(null, "reason")
        advanceUntilIdle()

        assertTrue("Should have Error state", refundStates.any { it is UiState.Error })
        job.cancel()
    }

    @Test
    fun `refundPayment should handle empty reason`() = runTest {
        val transactionId = "txn_123"
        whenever(mockTransactionRepository.refundPayment(transactionId, ""))
            .thenThrow(IllegalArgumentException("Reason cannot be empty"))

        val refundStates = mutableListOf<UiState<Unit>> >()
        val job = launch {
            viewModel.refundState.collect { refundStates.add(it) }
        }

        viewModel.refundPayment(transactionId, "")
        advanceUntilIdle()

        assertTrue("Should have Error state", refundStates.any { it is UiState.Error })
        job.cancel()
    }

    @Test
    fun `loadAllTransactions should be idempotent`() = runTest {
        val transactions = listOf(
            Transaction(id = "1", amount = 10000, status = PaymentStatus.COMPLETED, createdAt = Date())
        )
        whenever(mockTransactionRepository.getAllTransactions()).thenReturn(flowOf(transactions))

        viewModel.loadAllTransactions()
        advanceUntilIdle()

        val firstSuccess = viewModel.transactionsState.value
        assertTrue("First load should be Success", firstSuccess is UiState.Success)

        viewModel.loadAllTransactions()
        advanceUntilIdle()

        val secondSuccess = viewModel.transactionsState.value
        assertTrue("Second load should be Success", secondSuccess is UiState.Success)
        assertEquals("Both loads should return same data", 
            (firstSuccess as UiState.Success).data,
            (secondSuccess as UiState.Success).data
        )
        verify(mockTransactionRepository, times(2)).getAllTransactions()
    }

    @Test
    fun `transactionsState should emit states in correct order`() = runTest {
        val transactions = listOf(
            Transaction(id = "1", amount = 10000, status = PaymentStatus.COMPLETED, createdAt = Date())
        )
        whenever(mockTransactionRepository.getAllTransactions()).thenReturn(flowOf(transactions))

        val states = mutableListOf<UiState<List<Transaction>> >()
        val job = launch {
            viewModel.transactionsState.collect { states.add(it) }
        }

        viewModel.loadAllTransactions()
        advanceUntilIdle()

        assertTrue("First state should be Loading", states[0] is UiState.Loading)
        assertTrue("Second state should be Success", states[1] is UiState.Success)
        assertEquals("Should have exactly 2 states", 2, states.size)
        job.cancel()
    }

    @Test
    fun `refundState should emit Idle then Success`() = runTest {
        val transactionId = "txn_123"
        whenever(mockTransactionRepository.refundPayment(transactionId, any())).thenReturn(Unit)
        whenever(mockTransactionRepository.getAllTransactions()).thenReturn(flowOf(emptyList()))

        val states = mutableListOf<UiState<Unit>> >()
        val job = launch {
            viewModel.refundState.collect { states.add(it) }
        }

        viewModel.refundPayment(transactionId, "reason")
        advanceUntilIdle()

        assertTrue("First state should be Idle", states[0] is UiState.Idle)
        assertTrue("Should have Success state", states.any { it is UiState.Success })
        job.cancel()
    }

    @Test
    fun `multiple load calls should not cause duplicate Loading states`() = runTest {
        val transactions = listOf(
            Transaction(id = "1", amount = 10000, status = PaymentStatus.COMPLETED, createdAt = Date())
        )
        whenever(mockTransactionRepository.getAllTransactions()).thenReturn(flowOf(transactions))

        val states = mutableListOf<UiState<List<Transaction>> >()
        val job = launch {
            viewModel.transactionsState.collect { states.add(it) }
        }

        viewModel.loadAllTransactions()
        viewModel.loadAllTransactions()
        advanceUntilIdle()

        val loadingCount = states.count { it is UiState.Loading }
        assertEquals("Should have exactly 1 Loading state", 1, loadingCount)
        job.cancel()
    }

    @Test
    fun `loadTransactionsByStatus with different statuses should call repository correctly`() = runTest {
        val testCases = mapOf(
            PaymentStatus.PENDING to listOf(Transaction(id = "1", amount = 10000, status = PaymentStatus.PENDING, createdAt = Date())),
            PaymentStatus.COMPLETED to listOf(Transaction(id = "2", amount = 20000, status = PaymentStatus.COMPLETED, createdAt = Date())),
            PaymentStatus.FAILED to listOf(Transaction(id = "3", amount = 30000, status = PaymentStatus.FAILED, createdAt = Date())),
            PaymentStatus.REFUNDED to listOf(Transaction(id = "4", amount = 40000, status = PaymentStatus.REFUNDED, createdAt = Date()))
        )

        testCases.forEach { (status, transactions) ->
            whenever(mockTransactionRepository.getTransactionsByStatus(status)).thenReturn(flowOf(transactions))

            val states = mutableListOf<UiState<List<Transaction>> >()
            val job = launch {
                viewModel.transactionsState.collect { states.add(it) }
            }

            viewModel.loadTransactionsByStatus(status)
            advanceUntilIdle()

            val successState = states.filterIsInstance<UiState.Success<List<Transaction>>>().last()
            assertEquals(transactions, successState.data)
            job.cancel()
        }

        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.PENDING)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.COMPLETED)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.FAILED)
        verify(mockTransactionRepository).getTransactionsByStatus(PaymentStatus.REFUNDED)
    }

    @Test
    fun `refundPayment should not call repository with empty transaction id`() = runTest {
        viewModel.refundPayment("", "reason")
        advanceUntilIdle()

        verify(mockTransactionRepository, never()).refundPayment(any(), any())
    }

    @Test
    fun `refundPayment should not call repository with whitespace only transaction id`() = runTest {
        viewModel.refundPayment("   ", "reason")
        advanceUntilIdle()

        verify(mockTransactionRepository, never()).refundPayment(any(), any())
    }
}
