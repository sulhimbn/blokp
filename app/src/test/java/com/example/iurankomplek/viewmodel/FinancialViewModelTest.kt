package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FinancialViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var pemanfaatanRepository: PemanfaatanRepository

    @Mock
    private lateinit var eventBus: EventBus

    private lateinit var viewModel: FinancialViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FinancialViewModel(pemanfaatanRepository, eventBus)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFinancialData should emit Loading state initially`() = runTest {
        val mockData = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val mockResponse = PemanfaatanResponse(
            success = true,
            message = "Financial data fetched successfully",
            data = mockData
        )
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.success(mockResponse))

        viewModel.loadFinancialData()

        val loadingState = viewModel.financialState.value
        assertTrue(loadingState is FinancialDataState.Loading)
    }

    @Test
    fun `loadFinancialData should emit Success state with calculated summary when repository returns data`() = runTest {
        val mockData = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val mockResponse = PemanfaatanResponse(
            success = true,
            message = "Financial data fetched successfully",
            data = mockData
        )
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.success(mockResponse))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        val successState = state as FinancialDataState.Success
        assertEquals(mockResponse, successState.response)
        assertNotNull(successState.summary)
        assertTrue(successState.summary.isValid)
        assertEquals(100, successState.summary.totalIuranBulanan)
        assertEquals(50, successState.summary.totalPengeluaran)
        assertEquals(150 * 3, successState.summary.totalIuranIndividu)
        assertEquals((150 * 3) - 50, successState.summary.rekapIuran)
    }

    @Test
    fun `loadFinancialData should emit Error state when repository returns error`() = runTest {
        val errorMessage = "Network error occurred"
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.failure(IOException(errorMessage)))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Error)
        assertEquals(errorMessage, (state as FinancialDataState.Error).message)
    }

    @Test
    fun `loadFinancialData should not make duplicate calls when already loading`() = runTest {
        val mockData = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val mockResponse = PemanfaatanResponse(
            success = true,
            message = "Financial data fetched successfully",
            data = mockData
        )
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.success(mockResponse))

        viewModel.loadFinancialData()
        viewModel.loadFinancialData()

        Mockito.verify(pemanfaatanRepository).getPemanfaatan()
    }

    @Test
    fun `loadFinancialData should handle empty data correctly`() = runTest {
        val mockResponse = PemanfaatanResponse(
            success = true,
            message = "No financial data found",
            data = emptyList()
        )
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.success(mockResponse))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        val successState = state as FinancialDataState.Success
        assertEquals(mockResponse, successState.response)
        assertTrue(successState.summary.isValid)
        assertEquals(0, successState.summary.totalIuranBulanan)
        assertEquals(0, successState.summary.totalPengeluaran)
        assertEquals(0, successState.summary.totalIuranIndividu)
        assertEquals(0, successState.summary.rekapIuran)
    }

    @Test
    fun `loadFinancialData should handle invalid data gracefully`() = runTest {
        val invalidData = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = -100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val mockResponse = PemanfaatanResponse(
            success = true,
            message = "Financial data fetched successfully",
            data = invalidData
        )
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.success(mockResponse))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        val successState = state as FinancialDataState.Success
        assertFalse(successState.summary.isValid)
    }
}