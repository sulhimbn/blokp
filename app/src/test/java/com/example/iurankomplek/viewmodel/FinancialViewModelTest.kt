package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FinancialViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var pemanfaatanRepository: PemanfaatanRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var eventBus: EventBus

    private lateinit var viewModel: FinancialViewModel

    private val testDispatcher = StandardTestDispatcher()

    private fun dataItem(
        iuranPerwarga: Int = 100,
        totalIuranIndividu: Int = 50,
        pengeluaran: Int = 25
    ) = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = "john@example.com",
        alamat = "123 Main St",
        iuran_perwarga = iuranPerwarga,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = iuranPerwarga,
        total_iuran_individu = totalIuranIndividu,
        pengeluaran_iuran_warga = pengeluaran,
        pemanfaatan_iuran = "Maintenance",
        avatar = null
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        `when`(eventBus.events).thenReturn(MutableSharedFlow())
        viewModel = FinancialViewModel(pemanfaatanRepository, eventBus, transactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFinancialData should emit Loading state while the request is in flight`() = runTest {
        `when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(PemanfaatanResponse(data = listOf(dataItem()))))

        viewModel.loadFinancialData()

        assertTrue(viewModel.financialState.value is FinancialDataState.Loading)
    }

    @Test
    fun `loadFinancialData should emit Success with a calculated summary`() = runTest {
        val items = listOf(
            dataItem(iuranPerwarga = 100, totalIuranIndividu = 50, pengeluaran = 25),
            dataItem(iuranPerwarga = 200, totalIuranIndividu = 75, pengeluaran = 30)
        )
        val response = PemanfaatanResponse(data = items)
        `when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.success(response))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue("expected Success but was $state", state is FinancialDataState.Success)
        val success = state as FinancialDataState.Success
        assertEquals(300, success.summary.totalIuranBulanan)
        assertEquals(55, success.summary.totalPengeluaran)
        assertEquals(375, success.summary.totalIuranIndividu)
        assertEquals(320, success.summary.rekapIuran)
        assertTrue(success.summary.isValid)
    }

    @Test
    fun `loadFinancialData should emit Error when the repository fails`() = runTest {
        `when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.failure(IOException("Network error occurred")))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue("expected Error but was $state", state is FinancialDataState.Error)
        assertEquals("Network error occurred", (state as FinancialDataState.Error).message)
    }

    @Test
    fun `loadFinancialData should not make duplicate calls while a load is in flight`() = runTest {
        `when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(PemanfaatanResponse(data = listOf(dataItem()))))

        viewModel.loadFinancialData()
        viewModel.loadFinancialData()
        advanceUntilIdle()

        verifyBlocking(pemanfaatanRepository, times(1)) { getPemanfaatan() }
    }

    @Test
    fun `loadFinancialData should handle empty data with a valid summary`() = runTest {
        `when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(PemanfaatanResponse(data = emptyList())))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        val summary = (state as FinancialDataState.Success).summary
        assertEquals(0, summary.totalIuranBulanan)
        assertTrue(summary.isValid)
    }

    @Test
    fun `loadFinancialData should mark the summary invalid for negative amounts`() = runTest {
        val invalid = DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = -1,
            total_iuran_rekap = 0,
            jumlah_iuran_bulanan = -1,
            total_iuran_individu = 0,
            pengeluaran_iuran_warga = 0,
            pemanfaatan_iuran = "Bad",
            avatar = null
        )
        `when`(pemanfaatanRepository.getPemanfaatan())
            .thenReturn(Result.success(PemanfaatanResponse(data = listOf(invalid))))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        assertTrue(
            "invalid rows must be flagged in the summary",
            !(state as FinancialDataState.Success).summary.isValid
        )
    }
}