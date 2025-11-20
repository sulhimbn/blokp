package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.utils.UiState
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

    private lateinit var viewModel: FinancialViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FinancialViewModel(pemanfaatanRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFinancialData should emit Loading state initially`() = runTest {
        // Given
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

        // When
        viewModel.loadFinancialData()

        // Then
        val loadingState = viewModel.financialState.value
        assertTrue(loadingState is UiState.Loading)
    }

    @Test
    fun `loadFinancialData should emit Success state when repository returns data`() = runTest {
        // Given
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

        // When
        viewModel.loadFinancialData()

        // Then
        advanceUntilIdle()
        val state = viewModel.financialState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockResponse, (state as UiState.Success).data)
    }

    @Test
    fun `loadFinancialData should emit Error state when repository returns error`() = runTest {
        // Given
        val errorMessage = "Network error occurred"
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.failure(IOException(errorMessage)))

        // When
        viewModel.loadFinancialData()

        // Then
        advanceUntilIdle()
        val state = viewModel.financialState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).message)
    }

    @Test
    fun `loadFinancialData should not make duplicate calls when already loading`() = runTest {
        // Given
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

        // When
        viewModel.loadFinancialData()
        // Try to call loadFinancialData again while the first one is still in progress
        viewModel.loadFinancialData()

        // Then
        // Verify that pemanfaatanRepository.getPemanfaatan() was only called once
        Mockito.verify(pemanfaatanRepository).getPemanfaatan()
    }

    @Test
    fun `loadFinancialData should update state correctly for empty data`() = runTest {
        // Given
        val mockResponse = PemanfaatanResponse(
            success = true,
            message = "No financial data found",
            data = emptyList()
        )
        Mockito.`when`(pemanfaatanRepository.getPemanfaatan()).thenReturn(Result.success(mockResponse))

        // When
        viewModel.loadFinancialData()

        // Then
        advanceUntilIdle()
        val state = viewModel.financialState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockResponse, (state as UiState.Success).data)
        assertTrue((state as UiState.Success).data.data?.isEmpty() == true)
    }
}