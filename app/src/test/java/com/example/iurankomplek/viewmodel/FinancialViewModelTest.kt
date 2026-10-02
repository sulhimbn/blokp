package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.transaction.Transaction
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
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.math.BigDecimal
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner.Silent::class)
class FinancialViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var pemanfaatanRepository: PemanfaatanRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var eventBus: EventBus
    private lateinit var viewModel: FinancialViewModel
    private val testDispatcher = StandardTestDispatcher()

    private fun item(
        iuranPerwarga: Int = 100,
        totalIndividu: Int = 150,
        pengeluaran: Int = 50
    ) = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = "john.doe@example.com",
        alamat = "123 Main St",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 500,
        jumlah_iuran_bulanan = 200,
        total_iuran_individu = totalIndividu,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = "Maintenance",
        avatar = "https://example.com/avatar.jpg"
    )

    private fun completedTransaction(amount: Int) = Transaction(
        id = "txn-$amount",
        userId = "user-1",
        amount = BigDecimal(amount),
        currency = "IDR",
        status = PaymentStatus.COMPLETED,
        paymentMethod = PaymentMethod.BANK_TRANSFER,
        description = "iuran",
        createdAt = Date(0),
        updatedAt = Date(0)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        eventBus = EventBus()
        viewModel = FinancialViewModel(pemanfaatanRepository, eventBus, transactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state starts as Loading before any load is requested`() {
        assertTrue(viewModel.financialState.value is FinancialDataState.Loading)
    }

    @Test
    fun `loadFinancialData reports Success with a computed summary`() = runTest(testDispatcher) {
        val response = PemanfaatanResponse(listOf(item()))
        whenever(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.success(response))
        whenever(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(emptyList()))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        val success = state as FinancialDataState.Success
        assertEquals(response, success.response)
        assertTrue(success.summary.isValid)
        assertEquals(100, success.summary.totalIuranBulanan)
        assertEquals(50, success.summary.totalPengeluaran)
        assertEquals(150 * 3, success.summary.totalIuranIndividu)
        assertEquals((150 * 3) - 50, success.summary.rekapIuran)
    }

    @Test
    fun `completed payment totals feed the summary but never inflate iuran figures`() = runTest(testDispatcher) {
        whenever(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(PemanfaatanResponse(listOf(item()))))
        whenever(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(listOf(completedTransaction(50), completedTransaction(25))))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val summary = (viewModel.financialState.value as FinancialDataState.Success).summary
        assertEquals(75, summary.totalPaymentsProcessed)
        assertEquals(2, summary.completedTransactionsCount)
        assertEquals("payments must not be folded into the iuran totals",
            100, summary.totalIuranBulanan)
        assertEquals(150 * 3, summary.totalIuranIndividu)
        assertEquals((150 * 3) - 50, summary.rekapIuran)
    }

    @Test
    fun `loadFinancialData reports Error carrying the repository message`() = runTest(testDispatcher) {
        whenever(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.failure(IOException("Network error occurred")))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Error)
        assertEquals("Network error occurred", (state as FinancialDataState.Error).message)
    }

    @Test
    fun `loadFinancialData falls back to a generic message when the failure carries none`() = runTest(testDispatcher) {
        whenever(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.failure(IOException()))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        assertEquals(
            "Unknown error occurred",
            (viewModel.financialState.value as FinancialDataState.Error).message
        )
    }

    @Test
    fun `a second loadFinancialData call is ignored while the first is in flight`() = runTest(testDispatcher) {
        whenever(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(PemanfaatanResponse(listOf(item()))))
        whenever(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(emptyList()))

        viewModel.loadFinancialData()
        viewModel.loadFinancialData()
        advanceUntilIdle()

        verify(pemanfaatanRepository, times(1)).getPemanfaatan()
    }

    @Test
    fun `loadFinancialData can be retried after a previous attempt settled`() = runTest(testDispatcher) {
        whenever(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(PemanfaatanResponse(listOf(item()))))
        whenever(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(emptyList()))

        viewModel.loadFinancialData()
        advanceUntilIdle()
        viewModel.loadFinancialData()
        advanceUntilIdle()

        verify(pemanfaatanRepository, times(2)).getPemanfaatan()
    }

    @Test
    fun `empty financial data yields an all-zero valid summary`() = runTest(testDispatcher) {
        val empty = PemanfaatanResponse(emptyList())
        whenever(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.success(empty))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val success = viewModel.financialState.value as FinancialDataState.Success
        assertEquals(empty, success.response)
        assertTrue(success.summary.isValid)
        assertEquals(0, success.summary.totalIuranBulanan)
        assertEquals(0, success.summary.totalPengeluaran)
        assertEquals(0, success.summary.totalIuranIndividu)
        assertEquals(0, success.summary.rekapIuran)
    }

    @Test
    fun `an empty ledger never touches the transaction repository`() = runTest(testDispatcher) {
        whenever(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(PemanfaatanResponse(emptyList())))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        verify(transactionRepository, times(0))
            .getTransactionsByStatus(PaymentStatus.COMPLETED)
    }

    @Test
    fun `negative financial input marks the summary invalid instead of throwing`() = runTest(testDispatcher) {
        whenever(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(PemanfaatanResponse(listOf(item(iuranPerwarga = -100)))))
        whenever(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(emptyList()))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val success = viewModel.financialState.value as FinancialDataState.Success
        assertFalse(success.summary.isValid)
    }

    @Test
    fun `a failing transaction lookup degrades payment totals to zero without failing the load`() = runTest(testDispatcher) {
        whenever(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(PemanfaatanResponse(listOf(item()))))
        whenever(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenThrow(IllegalStateException("database locked"))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val success = viewModel.financialState.value as FinancialDataState.Success
        assertTrue(success.summary.isValid)
        assertEquals(0, success.summary.totalPaymentsProcessed)
        assertEquals(0, success.summary.completedTransactionsCount)
        assertEquals(100, success.summary.totalIuranBulanan)
    }
}
