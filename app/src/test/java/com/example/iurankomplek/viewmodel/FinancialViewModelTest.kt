package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import kotlinx.coroutines.Dispatchers
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.data.api.models.PaymentResponse
import kotlinx.coroutines.flow.flowOf
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.payment.RefundResponse
import com.example.iurankomplek.payment.PaymentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FinancialViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val pemanfaatanRepository = FakePemanfaatanRepository()

    private val eventBus = EventBus()

    private val transactionRepository = FakeTransactionRepository()

    private lateinit var viewModel: FinancialViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FinancialViewModel(pemanfaatanRepository, eventBus, transactionRepository)
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
            data = mockData
        )
        pemanfaatanRepository.result = (Result.success(mockResponse))

        assertTrue(viewModel.financialState.value is FinancialDataState.Loading)
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
data = mockData
        )
        pemanfaatanRepository.result = (Result.success(mockResponse))

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
        pemanfaatanRepository.result = (Result.failure(IOException(errorMessage)))

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
            data = mockData
        )
        pemanfaatanRepository.result = (Result.success(mockResponse))

        viewModel.loadFinancialData()
        advanceUntilIdle()
        viewModel.loadFinancialData()
        advanceUntilIdle()

        assertTrue(pemanfaatanRepository.calls > 0)
    }

    @Test
    fun `loadFinancialData should handle empty data correctly`() = runTest {
        val mockResponse = PemanfaatanResponse(
data = emptyList()
        )
        pemanfaatanRepository.result = (Result.success(mockResponse))

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
            data = invalidData
        )
        pemanfaatanRepository.result = (Result.success(mockResponse))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        val successState = state as FinancialDataState.Success
        assertFalse(successState.summary.isValid)
    }
}

/**
 * Mockito cannot stub suspend functions, so the repository is faked by hand.
 */
private class FakePemanfaatanRepository : PemanfaatanRepository {
    var result: Result<PemanfaatanResponse> = Result.success(PemanfaatanResponse(emptyList()))
    var calls = 0

    override suspend fun getPemanfaatan(): Result<PemanfaatanResponse> {
        calls++
        return result
    }
}

/**
 * Mockito cannot stub suspend functions, and getTransactionsByStatus returns null on a
 * mock, which the ViewModel then treats as "no payments".
 */
private class FakeTransactionRepository : TransactionRepository {
    override suspend fun initiatePaymentViaApi(
        amount: String,
        description: String,
        customerId: String,
        paymentMethod: String
    ): Result<PaymentResponse> = unsupported()

    override suspend fun processPayment(request: PaymentRequest): Result<Transaction> =
        unsupported()

    override suspend fun getTransactionById(id: String): Transaction? = null

    override fun getTransactionsByUserId(userId: String): Flow<List<Transaction>> =
        flowOf(emptyList())

    override fun getTransactionsByStatus(status: PaymentStatus): Flow<List<Transaction>> =
        flowOf(emptyList())

    override suspend fun updateTransaction(transaction: Transaction) = Unit

    override suspend fun refundPayment(transactionId: String, reason: String?): Result<RefundResponse> =
        unsupported()

    override suspend fun deleteTransaction(transaction: Transaction) = Unit

    private fun <T> unsupported(): Result<T> = Result.failure(UnsupportedOperationException())
}
