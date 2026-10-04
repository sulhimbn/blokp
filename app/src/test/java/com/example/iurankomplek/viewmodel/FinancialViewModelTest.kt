package com.example.iurankomplek.viewmodel

import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.event.AppEvent
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.FinancialCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.io.IOException
import java.math.BigDecimal
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class FinancialViewModelTest {

    private lateinit var repository: PemanfaatanRepositoryStub
    private lateinit var eventBus: EventBus
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var viewModel: FinancialViewModel
    private val testDispatcher = StandardTestDispatcher()

    private class PemanfaatanRepositoryStub(
        private val result: Result<PemanfaatanResponse>
    ) : PemanfaatanRepository {
        var callCount = 0
        override suspend fun getPemanfaatan(): Result<PemanfaatanResponse> {
            callCount++
            return result
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        eventBus = EventBus()
        transactionRepository = mock(TransactionRepository::class.java)
        repository = PemanfaatanRepositoryStub(Result.success(PemanfaatanResponse(emptyList())))
        viewModel = FinancialViewModel(repository, eventBus, transactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun seed(rows: Int = 2) {
        val items = (1..rows).map {
            TestFixtures.dataItem(
                iuranPerwarga = 100_000,
                totalIuranIndividu = 150_000,
                pengeluaranIuranWarga = 50_000
            )
        }
        repository = PemanfaatanRepositoryStub(Result.success(PemanfaatanResponse(items)))
        viewModel = FinancialViewModel(repository, eventBus, transactionRepository)
    }

    @Test
    fun `starts in Loading before any load is requested`() = runTest(testDispatcher) {
        assertTrue(viewModel.financialState.value is FinancialDataState.Loading)
    }

    @Test
    fun `the very first loadFinancialData call reaches the repository`() = runTest(testDispatcher) {
        seed()

        viewModel.loadFinancialData()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.financialState.value is FinancialDataState.Success)
        assertEquals(1, repository.callCount)
    }

    @Test
    fun `a duplicate call while the first is in flight does not re-query the repository`() =
        runTest(testDispatcher) {
            seed()

            viewModel.loadFinancialData()
            viewModel.loadFinancialData()
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, repository.callCount)
        }

    @Test
    fun `the summary matches FinancialCalculator for the loaded rows`() = runTest(testDispatcher) {
        seed(rows = 2)
        `when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(emptyList()))

        viewModel.loadFinancialData()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.financialState.value as FinancialDataState.Success
        val expected = FinancialCalculator.calculateRekapIuran(state.response.data)

        assertTrue(state.summary.isValid)
        assertEquals(expected, state.summary.rekapIuran)
        assertEquals(2 * 100_000, state.summary.totalIuranBulanan)
        assertEquals(2 * 50_000, state.summary.totalPengeluaran)
        assertEquals(
            2 * 150_000 * Constants.Financial.IURAN_MULTIPLIER,
            state.summary.totalIuranIndividu
        )
    }

    @Test
    fun `completed transactions are folded into the payment totals`() = runTest(testDispatcher) {
        seed(rows = 1)
        val completed = listOf(
            transaction("t-1", "1000"),
            transaction("t-2", "2500")
        )
        `when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(completed))

        viewModel.loadFinancialData()
        testDispatcher.scheduler.advanceUntilIdle()

        val summary = (viewModel.financialState.value as FinancialDataState.Success).summary
        assertEquals(2, summary.completedTransactionsCount)
        assertEquals(3500, summary.totalPaymentsProcessed)
    }

    @Test
    fun `a failing transaction lookup does not invalidate the financial summary`() =
        runTest(testDispatcher) {
            seed(rows = 1)
            `when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
                .thenReturn(flowOf(emptyList()))

            viewModel.loadFinancialData()
            testDispatcher.scheduler.advanceUntilIdle()

            val summary = (viewModel.financialState.value as FinancialDataState.Success).summary
            assertTrue(summary.isValid)
            assertEquals(0, summary.completedTransactionsCount)
            assertEquals(0, summary.totalPaymentsProcessed)
        }

    @Test
    fun `an empty dataset yields a valid zeroed summary`() = runTest(testDispatcher) {
        `when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(emptyList()))

        viewModel.loadFinancialData()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.financialState.value as FinancialDataState.Success
        assertTrue(state.summary.isValid)
        assertEquals(0, state.summary.rekapIuran)
        assertEquals(0, state.summary.totalIuranBulanan)
    }

    @Test
    fun `surfaces a repository failure as an error state`() = runTest(testDispatcher) {
        repository = PemanfaatanRepositoryStub(Result.failure(IOException("Resource not found")))
        viewModel = FinancialViewModel(repository, eventBus, transactionRepository)

        viewModel.loadFinancialData()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Error)
        assertEquals("Resource not found", (state as FinancialDataState.Error).message)
    }

    @Test
    fun `a FinancialDataUpdated event triggers a reload`() = runTest(testDispatcher) {
        seed()
        `when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(emptyList()))

        viewModel.loadFinancialData()
        testDispatcher.scheduler.advanceUntilIdle()

        eventBus.publishBlocking(AppEvent.FinancialDataUpdated)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, repository.callCount)
    }

    @Test
    fun `an event unrelated to financials does not trigger a reload`() = runTest(testDispatcher) {
        seed()

        viewModel.loadFinancialData()
        testDispatcher.scheduler.advanceUntilIdle()

        eventBus.publishBlocking(AppEvent.NewAnnouncement("ann-1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.callCount)
    }

    @Test
    fun `a dataset that fails validation is reported as invalid`() = runTest(testDispatcher) {
        val negative = TestFixtures.dataItem(totalIuranIndividu = Int.MAX_VALUE)
        repository = PemanfaatanRepositoryStub(Result.success(PemanfaatanResponse(listOf(negative))))
        viewModel = FinancialViewModel(repository, eventBus, transactionRepository)
        `when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(emptyList()))

        viewModel.loadFinancialData()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.financialState.value as FinancialDataState.Success
        assertFalse(state.summary.isValid)
    }

    private fun transaction(id: String, amount: String) = Transaction(
        id = id,
        userId = "user-1",
        amount = BigDecimal(amount),
        currency = "IDR",
        status = PaymentStatus.COMPLETED,
        paymentMethod = com.example.iurankomplek.payment.PaymentMethod.BANK_TRANSFER,
        description = "iuran",
        createdAt = Date(0),
        updatedAt = Date(0)
    )
}
