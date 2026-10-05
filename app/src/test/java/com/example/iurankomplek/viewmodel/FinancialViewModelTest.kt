package com.example.iurankomplek.viewmodel

import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.payment.PaymentStatus
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
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FinancialViewModelTest {

    private val pemanfaatanRepository = Mockito.mock(PemanfaatanRepository::class.java)
    private val transactionRepository = Mockito.mock(TransactionRepository::class.java)
    private val eventBus = EventBus()

    private lateinit var viewModel: FinancialViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        Mockito.`when`(transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED))
            .thenReturn(flowOf(emptyList()))
        viewModel = FinancialViewModel(pemanfaatanRepository, eventBus, transactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun dataItem(
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

    @Test
    fun `initial state is Loading before any load`() {
        assertTrue(viewModel.financialState.value is FinancialDataState.Loading)
    }

    @Test
    fun `first loadFinancialData call is not skipped by the initial Loading state`() =
        runTest(testDispatcher) {
            val response = PemanfaatanResponse(data = listOf(dataItem()))
            Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
                .thenReturn(Result.success(response))

            viewModel.loadFinancialData()
            advanceUntilIdle()

            Mockito.verify(pemanfaatanRepository, Mockito.times(1)).getPemanfaatan()
            assertTrue(viewModel.financialState.value is FinancialDataState.Success)
        }

    @Test
    fun `loadFinancialData should emit Success state with calculated summary`() =
        runTest(testDispatcher) {
            val response = PemanfaatanResponse(data = listOf(dataItem()))
            Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
                .thenReturn(Result.success(response))

            viewModel.loadFinancialData()
            advanceUntilIdle()

            val state = viewModel.financialState.value
            assertTrue(state is FinancialDataState.Success)
            val successState = state as FinancialDataState.Success
            assertEquals(response, successState.response)
            assertTrue(successState.summary.isValid)
            assertEquals(100, successState.summary.totalIuranBulanan)
            assertEquals(50, successState.summary.totalPengeluaran)
            assertEquals(150 * 3, successState.summary.totalIuranIndividu)
            assertEquals((150 * 3) - 50, successState.summary.rekapIuran)
        }

    @Test
    fun `loadFinancialData should emit Error state when repository returns error`() =
        runTest(testDispatcher) {
            Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
                .thenReturn(Result.failure(IOException("Network error occurred")))

            viewModel.loadFinancialData()
            advanceUntilIdle()

            val state = viewModel.financialState.value
            assertTrue(state is FinancialDataState.Error)
            assertEquals("Network error occurred", (state as FinancialDataState.Error).message)
        }

    @Test
    fun `loadFinancialData should not start a second request while one is in flight`() =
        runTest(testDispatcher) {
            Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
                .thenReturn(Result.success(PemanfaatanResponse(data = emptyList())))

            viewModel.loadFinancialData()
            viewModel.loadFinancialData()
            advanceUntilIdle()

            Mockito.verify(pemanfaatanRepository, Mockito.times(1)).getPemanfaatan()
        }

    @Test
    fun `loadFinancialData should handle empty data correctly`() = runTest(testDispatcher) {
        val response = PemanfaatanResponse(data = emptyList())
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.success(response))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        val successState = state as FinancialDataState.Success
        assertEquals(response, successState.response)
        assertTrue(successState.summary.isValid)
        assertEquals(0, successState.summary.totalIuranBulanan)
        assertEquals(0, successState.summary.totalPengeluaran)
        assertEquals(0, successState.summary.totalIuranIndividu)
        assertEquals(0, successState.summary.rekapIuran)
    }

    @Test
    fun `loadFinancialData should mark the summary invalid for negative financial data`() =
        runTest(testDispatcher) {
            val response = PemanfaatanResponse(data = listOf(dataItem(iuranPerwarga = -100)))
            Mockito.`when`(pemanfaatanRepository.getPemanfaatan())
                .thenReturn(Result.success(response))

            viewModel.loadFinancialData()
            advanceUntilIdle()

            val state = viewModel.financialState.value
            assertTrue(state is FinancialDataState.Success)
            assertFalse((state as FinancialDataState.Success).summary.isValid)
        }

    @Test
    fun `FinancialViewModelFactory builds a FinancialViewModel for its own class`() {
        val factory =
            FinancialViewModelFactory(pemanfaatanRepository, eventBus, transactionRepository)
        assertTrue(factory.create(FinancialViewModel::class.java) is FinancialViewModel)
    }

    @Test
    fun `FinancialViewModelFactory rejects an unrelated ViewModel class`() {
        val factory =
            FinancialViewModelFactory(pemanfaatanRepository, eventBus, transactionRepository)
        val error = runCatching { factory.create(androidx.lifecycle.ViewModel::class.java) }
        assertTrue(error.isFailure)
    }
}