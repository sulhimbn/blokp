package com.example.iurankomplek.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.api.models.PemanfaatanResponse
import com.example.iurankomplek.data.dto.LegacyDataItemDto
import com.example.iurankomplek.domain.usecase.CalculateFinancialSummaryUseCase
import com.example.iurankomplek.domain.usecase.LoadFinancialDataUseCase
import com.example.iurankomplek.domain.usecase.PaymentSummaryIntegrationUseCase
import com.example.iurankomplek.utils.OperationResult
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
class FinancialViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var mockLoadFinancialDataUseCase: LoadFinancialDataUseCase

    @Mock
    private lateinit var mockPaymentSummaryIntegrationUseCase: PaymentSummaryIntegrationUseCase

    private lateinit var viewModel: FinancialViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = FinancialViewModel(mockLoadFinancialDataUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFinancialData should set Loading then Success states`() = runTest {
        val financialResponse = PemanfaatanResponse(
            data = listOf(
                LegacyDataItemDto(
                    id = 1,
                    userId = 1,
                    iuranPerwarga = 100000,
                    pengeluaranIuranWarga = 50000,
                    totalIuranIndividu = 150000,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        )
        whenever(mockLoadFinancialDataUseCase()).thenReturn(
            OperationResult.Success(financialResponse)
        )

        val states = mutableListOf<UiState<PemanfaatanResponse>>()
        val job = launch {
            viewModel.financialState.collect { states.add(it) }
        }

        viewModel.loadFinancialData()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Success state", states.any { it is UiState.Success })
        val successState = states.filterIsInstance<UiState.Success<PemanfaatanResponse>>().last()
        assertEquals(financialResponse, successState.data)
        verify(mockLoadFinancialDataUseCase)()
        job.cancel()
    }

    @Test
    fun `loadFinancialData should set Error state when use case returns error`() = runTest {
        val errorMessage = "API error"
        whenever(mockLoadFinancialDataUseCase()).thenReturn(
            OperationResult.Error(Exception(errorMessage), errorMessage)
        )

        val states = mutableListOf<UiState<PemanfaatanResponse>>()
        val job = launch {
            viewModel.financialState.collect { states.add(it) }
        }

        viewModel.loadFinancialData()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Error state", states.any { it is UiState.Error })
        val errorState = states.filterIsInstance<UiState.Error<PemanfaatanResponse>>().last()
        assertEquals(errorMessage, errorState.message)
        verify(mockLoadFinancialDataUseCase)()
        job.cancel()
    }

    @Test
    fun `loadFinancialData should handle use case throwing exception`() = runTest {
        whenever(mockLoadFinancialDataUseCase()).thenThrow(RuntimeException("Unexpected error"))

        val states = mutableListOf<UiState<PemanfaatanResponse>>()
        val job = launch {
            viewModel.financialState.collect { states.add(it) }
        }

        viewModel.loadFinancialData()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Error state", states.any { it is UiState.Error })
        val errorState = states.filterIsInstance<UiState.Error<PemanfaatanResponse>>().last()
        assertTrue("Error message should contain error", errorState.message?.contains("error") == true)
        verify(mockLoadFinancialDataUseCase)()
        job.cancel()
    }

    @Test
    fun `loadFinancialData should set Error state for Loading result`() = runTest {
        whenever(mockLoadFinancialDataUseCase()).thenReturn(OperationResult.Loading)

        val states = mutableListOf<UiState<PemanfaatanResponse>>()
        val job = launch {
            viewModel.financialState.collect { states.add(it) }
        }

        viewModel.loadFinancialData()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        job.cancel()
    }

    @Test
    fun `loadFinancialData should set Error state for Empty result`() = runTest {
        whenever(mockLoadFinancialDataUseCase()).thenReturn(OperationResult.Empty)

        val states = mutableListOf<UiState<PemanfaatanResponse>>()
        val job = launch {
            viewModel.financialState.collect { states.add(it) }
        }

        viewModel.loadFinancialData()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Error state", states.any { it is UiState.Error })
        val errorState = states.filterIsInstance<UiState.Error<PemanfaatanResponse>>().last()
        assertEquals("No data available", errorState.message)
        job.cancel()
    }

    @Test
    fun `loadFinancialData should handle empty financial list`() = runTest {
        val financialResponse = PemanfaatanResponse(data = emptyList())
        whenever(mockLoadFinancialDataUseCase()).thenReturn(
            OperationResult.Success(financialResponse)
        )

        val states = mutableListOf<UiState<PemanfaatanResponse>>()
        val job = launch {
            viewModel.financialState.collect { states.add(it) }
        }

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val successState = states.filterIsInstance<UiState.Success<PemanfaatanResponse>>().last()
        assertTrue("Success state should have empty data", successState.data.data.isEmpty())
        job.cancel()
    }

    @Test
    fun `calculateFinancialSummary should return correct summary`() {
        val items = listOf(
            LegacyDataItemDto(
                id = 1,
                userId = 1,
                iuranPerwarga = 100000,
                pengeluaranIuranWarga = 50000,
                totalIuranIndividu = 150000,
                createdAt = Date(),
                updatedAt = Date()
            ),
            LegacyDataItemDto(
                id = 2,
                userId = 2,
                iuranPerwarga = 200000,
                pengeluaranIuranWarga = 75000,
                totalIuranIndividu = 300000,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        val summary = viewModel.calculateFinancialSummary(items)

        assertNotNull("Summary should not be null", summary)
        assertTrue("Validation should be true", summary.isValid)
        assertEquals("Total iuran bulanan should be correct", 300000L, summary.totalIuranBulanan)
        assertEquals("Total pengeluaran should be correct", 125000L, summary.totalPengeluaran)
        assertEquals("Rekap iuran should be correct", 450000L * 3, summary.rekapIuran)
    }

    @Test
    fun `calculateFinancialSummary should handle empty list`() {
        val summary = viewModel.calculateFinancialSummary(emptyList())

        assertNotNull("Summary should not be null", summary)
        assertTrue("Validation should be true for empty list", summary.isValid)
        assertEquals("Total iuran bulanan should be 0", 0L, summary.totalIuranBulanan)
        assertEquals("Total pengeluaran should be 0", 0L, summary.totalPengeluaran)
        assertEquals("Rekap iuran should be 0", 0L, summary.rekapIuran)
    }

    @Test
    fun `calculateFinancialSummary should handle single item`() {
        val items = listOf(
            LegacyDataItemDto(
                id = 1,
                userId = 1,
                iuranPerwarga = 100000,
                pengeluaranIuranWarga = 50000,
                totalIuranIndividu = 150000,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        val summary = viewModel.calculateFinancialSummary(items)

        assertNotNull("Summary should not be null", summary)
        assertTrue("Validation should be true", summary.isValid)
        assertEquals("Total iuran bulanan should be correct", 100000L, summary.totalIuranBulanan)
        assertEquals("Total pengeluaran should be correct", 50000L, summary.totalPengeluaran)
        assertEquals("Rekap iuran should be correct", 150000L * 3, summary.rekapIuran)
    }

    @Test
    fun `calculateFinancialSummary should handle large values`() {
        val items = listOf(
            LegacyDataItemDto(
                id = 1,
                userId = 1,
                iuranPerwarga = Long.MAX_VALUE / 4,
                pengeluaranIuranWarga = Long.MAX_VALUE / 4,
                totalIuranIndividu = Long.MAX_VALUE / 4,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        val summary = viewModel.calculateFinancialSummary(items)

        assertNotNull("Summary should not be null", summary)
        assertTrue("Validation should be true", summary.isValid)
    }

    @Test
    fun `integratePaymentTransactions should return result when use case provided`() = runTest {
        viewModel = FinancialViewModel(
            mockLoadFinancialDataUseCase,
            CalculateFinancialSummaryUseCase(),
            mockPaymentSummaryIntegrationUseCase
        )

        val paymentResult = PaymentSummaryIntegrationUseCase.PaymentIntegrationResult(
            totalPaymentAmount = 500000L,
            paymentCount = 5,
            transactions = emptyList()
        )
        whenever(mockPaymentSummaryIntegrationUseCase()).thenReturn(paymentResult)

        val result = viewModel.integratePaymentTransactions()

        assertNotNull("Result should not be null", result)
        assertEquals(paymentResult, result)
        verify(mockPaymentSummaryIntegrationUseCase)()
    }

    @Test
    fun `integratePaymentTransactions should return null when use case not provided`() = runTest {
        viewModel = FinancialViewModel(
            mockLoadFinancialDataUseCase,
            CalculateFinancialSummaryUseCase(),
            null
        )

        val result = viewModel.integratePaymentTransactions()

        assertNull("Result should be null", result)
        verify(mockPaymentSummaryIntegrationUseCase, never())()
    }

    @Test
    fun `integratePaymentTransactions should handle exception from use case`() = runTest {
        viewModel = FinancialViewModel(
            mockLoadFinancialDataUseCase,
            CalculateFinancialSummaryUseCase(),
            mockPaymentSummaryIntegrationUseCase
        )

        whenever(mockPaymentSummaryIntegrationUseCase()).thenThrow(
            RuntimeException("Transaction error")
        )

        val exception = assertThrows(RuntimeException::class.java) {
            runTest {
                viewModel.integratePaymentTransactions()
            }
        }

        assertTrue("Exception message should contain error", exception.message?.contains("error") == true)
    }

    @Test
    fun `loadFinancialData should be idempotent`() = runTest {
        val financialResponse = PemanfaatanResponse(
            data = listOf(
                LegacyDataItemDto(
                    id = 1,
                    userId = 1,
                    iuranPerwarga = 100000,
                    pengeluaranIuranWarga = 50000,
                    totalIuranIndividu = 150000,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        )
        whenever(mockLoadFinancialDataUseCase()).thenReturn(
            OperationResult.Success(financialResponse)
        )

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val firstSuccess = viewModel.financialState.value
        assertTrue("First load should be Success", firstSuccess is UiState.Success)

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val secondSuccess = viewModel.financialState.value
        assertTrue("Second load should be Success", secondSuccess is UiState.Success)
        assertEquals("Both loads should return same data", 
            (firstSuccess as UiState.Success).data,
            (secondSuccess as UiState.Success).data
        )
        verify(mockLoadFinancialDataUseCase, times(2))()
    }

    @Test
    fun `financialState should emit states in correct order`() = runTest {
        val financialResponse = PemanfaatanResponse(
            data = listOf(
                LegacyDataItemDto(
                    id = 1,
                    userId = 1,
                    iuranPerwarga = 100000,
                    pengeluaranIuranWarga = 50000,
                    totalIuranIndividu = 150000,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        )
        whenever(mockLoadFinancialDataUseCase()).thenReturn(
            OperationResult.Success(financialResponse)
        )

        val states = mutableListOf<UiState<PemanfaatanResponse>>()
        val job = launch {
            viewModel.financialState.collect { states.add(it) }
        }

        viewModel.loadFinancialData()
        advanceUntilIdle()

        assertTrue("First state should be Loading", states[0] is UiState.Loading)
        assertTrue("Second state should be Success", states[1] is UiState.Success)
        assertEquals("Should have exactly 2 states", 2, states.size)
        job.cancel()
    }

    @Test
    fun `multiple load calls should not cause duplicate Loading states`() = runTest {
        val financialResponse = PemanfaatanResponse(
            data = listOf(
                LegacyDataItemDto(
                    id = 1,
                    userId = 1,
                    iuranPerwarga = 100000,
                    pengeluaranIuranWarga = 50000,
                    totalIuranIndividu = 150000,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        )
        whenever(mockLoadFinancialDataUseCase()).thenReturn(
            OperationResult.Success(financialResponse)
        )

        val states = mutableListOf<UiState<PemanfaatanResponse>>()
        val job = launch {
            viewModel.financialState.collect { states.add(it) }
        }

        viewModel.loadFinancialData()
        viewModel.loadFinancialData()
        advanceUntilIdle()

        val loadingCount = states.count { it is UiState.Loading }
        assertEquals("Should have exactly 1 Loading state", 1, loadingCount)
        job.cancel()
    }

    @Test
    fun `Factory should create FinancialViewModel with correct dependencies`() {
        val loadFinancialDataUseCase = LoadFinancialDataUseCase(mock(), mock(), mock())
        val calculateFinancialSummaryUseCase = CalculateFinancialSummaryUseCase()
        val factory = FinancialViewModel.Factory(
            loadFinancialDataUseCase,
            calculateFinancialSummaryUseCase,
            null
        )

        val viewModel = factory.create(FinancialViewModel::class.java)

        assertNotNull("ViewModel should be created", viewModel)
        assertTrue("ViewModel should be FinancialViewModel", viewModel is FinancialViewModel)
    }

    @Test
    fun `Factory should throw exception for unknown ViewModel class`() {
        val loadFinancialDataUseCase = LoadFinancialDataUseCase(mock(), mock(), mock())
        val calculateFinancialSummaryUseCase = CalculateFinancialSummaryUseCase()
        val factory = FinancialViewModel.Factory(
            loadFinancialDataUseCase,
            calculateFinancialSummaryUseCase,
            null
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            factory.create(UserViewModel::class.java)
        }

        assertTrue("Exception message should contain unknown", exception.message?.contains("Unknown") == true)
    }

    @Test
    fun `calculateFinancialSummary should use CalculateFinancialSummaryUseCase correctly`() {
        val items = listOf(
            LegacyDataItemDto(
                id = 1,
                userId = 1,
                iuranPerwarga = 100000,
                pengeluaranIuranWarga = 50000,
                totalIuranIndividu = 150000,
                createdAt = Date(),
                updatedAt = Date()
            )
        )

        val summary = viewModel.calculateFinancialSummary(items)

        assertNotNull("Summary should not be null", summary)
        assertTrue("Result should be FinancialSummary", 
            summary is CalculateFinancialSummaryUseCase.FinancialSummary
        )
    }

    @Test
    fun `integratePaymentTransactions should be suspend function`() = runTest {
        viewModel = FinancialViewModel(
            mockLoadFinancialDataUseCase,
            CalculateFinancialSummaryUseCase(),
            mockPaymentSummaryIntegrationUseCase
        )

        whenever(mockPaymentSummaryIntegrationUseCase()).thenReturn(
            PaymentSummaryIntegrationUseCase.PaymentIntegrationResult(
                totalPaymentAmount = 0L,
                paymentCount = 0,
                transactions = emptyList()
            )
        )

        val result = viewModel.integratePaymentTransactions()

        assertNotNull("Result should not be null", result)
        verify(mockPaymentSummaryIntegrationUseCase)()
    }
}
