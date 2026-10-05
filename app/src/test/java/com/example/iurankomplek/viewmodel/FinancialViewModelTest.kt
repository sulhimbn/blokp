package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.transaction.Transaction
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any as anyKotlin
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FinancialViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var pemanfaatanRepository: PemanfaatanRepository
    @Mock
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var viewModel: FinancialViewModel
    private lateinit var eventBus: EventBus

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)
        eventBus = EventBus()
        viewModel = FinancialViewModel(pemanfaatanRepository, eventBus, transactionRepository)
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        assertTrue(viewModel.financialState.value is FinancialDataState.Loading)
    }

    @Test
    fun `the first load must not be swallowed by the duplicate-load guard`() = runTest {
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(TestFixtures.pemanfaatanResponse(TestFixtures.dataItem())))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        Mockito.verify(pemanfaatanRepository, Mockito.times(1)).getPemanfaatan()
        assertTrue(viewModel.financialState.value is FinancialDataState.Success)
    }

    @Test
    fun `successful load should compute the financial summary from repository data`() = runTest {
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(TestFixtures.pemanfaatanResponse(TestFixtures.dataItem())))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        val summary = (state as FinancialDataState.Success).summary
        assertEquals(100, summary.totalIuranBulanan)
        assertEquals(50, summary.totalPengeluaran)
        assertEquals(450, summary.totalIuranIndividu)
        assertEquals(400, summary.rekapIuran)
        assertTrue(summary.isValid)
    }

    @Test
    fun `empty data yields a valid zeroed summary`() = runTest {
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(TestFixtures.pemanfaatanResponse()))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        val summary = (state as FinancialDataState.Success).summary
        assertEquals(0, summary.totalIuranBulanan)
        assertEquals(0, summary.rekapIuran)
        assertTrue(summary.isValid)
    }

    @Test
    fun `repository failure should emit Error state`() = runTest {
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.failure(IOException("Backend unreachable")))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Error)
        assertEquals("Backend unreachable", (state as FinancialDataState.Error).message)
    }

    @Test
    fun `repository failure without a message should fall back to a generic string`() = runTest {
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.failure(IOException()))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertEquals("Unknown error occurred", (state as FinancialDataState.Error).message)
    }

    @Test
    fun `invalid financial input should mark the summary invalid`() = runTest {
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(
            Result.success(
                TestFixtures.pemanfaatanResponse(
                    TestFixtures.dataItem(totalIuranIndividu = -5)
                )
            )
        )

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        assertFalse((state as FinancialDataState.Success).summary.isValid)
    }

    @Test
    fun `completed transactions are folded into the summary`() = runTest {
        val completed = listOf(
            Transaction(
                id = "t1",
                userId = "u1",
                amount = BigDecimal("1000"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                description = "Monthly dues",
                createdAt = Date(0),
                updatedAt = Date(0)
            )
        )
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(completed))
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(TestFixtures.pemanfaatanResponse(TestFixtures.dataItem())))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val summary = (viewModel.financialState.value as FinancialDataState.Success).summary
        assertEquals(1000, summary.totalPaymentsProcessed)
        assertEquals(1, summary.completedTransactionsCount)
    }

    @Test
    fun `concurrent loads hit the repository only once`() = runTest {
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(TestFixtures.pemanfaatanResponse(TestFixtures.dataItem())))

        viewModel.loadFinancialData()
        viewModel.loadFinancialData()
        viewModel.loadFinancialData()
        advanceUntilIdle()

        Mockito.verify(pemanfaatanRepository, Mockito.times(1)).getPemanfaatan()
    }
}