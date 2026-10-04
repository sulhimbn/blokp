package com.example.iurankomplek.viewmodel

import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.TransactionRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.payment.PaymentRequest
import com.example.iurankomplek.payment.RefundResponse
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.data.api.models.PaymentResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FinancialViewModelTest {

    private class FakePemanfaatanRepository : PemanfaatanRepository {
        var getPemanfaatanCalls = 0
        var result: Result<PemanfaatanResponse> = Result.success(PemanfaatanResponse(data = emptyList()))

        override suspend fun getPemanfaatan(): Result<PemanfaatanResponse> {
            getPemanfaatanCalls++
            return result
        }
    }

    private class FakeTransactionRepository : TransactionRepository {
        override suspend fun initiatePaymentViaApi(
            amount: String,
            description: String,
            customerId: String,
            paymentMethod: String
        ): Result<PaymentResponse> = Result.failure(UnsupportedOperationException())

        override suspend fun processPayment(request: PaymentRequest): Result<Transaction> =
            Result.failure(UnsupportedOperationException())

        override suspend fun getTransactionById(id: String): Transaction? = null

        override fun getTransactionsByUserId(userId: String): Flow<List<Transaction>> = flowOf(emptyList())

        override fun getTransactionsByStatus(status: PaymentStatus): Flow<List<Transaction>> = flowOf(emptyList())

        override suspend fun updateTransaction(transaction: Transaction) = Unit

        override suspend fun refundPayment(transactionId: String, reason: String?): Result<RefundResponse> =
            Result.failure(UnsupportedOperationException())

        override suspend fun deleteTransaction(transaction: Transaction) = Unit
    }

    private lateinit var pemanfaatanRepository: FakePemanfaatanRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var viewModel: FinancialViewModel

    private val testDispatcher = StandardTestDispatcher()

    private fun item(
        iuranPerwarga: Int = 100,
        pengeluaran: Int = 50,
        totalIndividu: Int = 150
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        pemanfaatanRepository = FakePemanfaatanRepository()
        transactionRepository = FakeTransactionRepository()
        viewModel = FinancialViewModel(pemanfaatanRepository, EventBus(), transactionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadFinancialDataEmitsLoadingWhileRequestIsInFlight() = runTest {
        pemanfaatanRepository.result = Result.success(PemanfaatanResponse(data = listOf(item())))

        viewModel.loadFinancialData()

        assertTrue(viewModel.financialState.value is FinancialDataState.Loading)
    }

    @Test
    fun loadFinancialDataEmitsSuccessWithAggregatedSummary() = runTest {
        val response = PemanfaatanResponse(data = listOf(item()))
        pemanfaatanRepository.result = Result.success(response)

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Success)
        val success = state as FinancialDataState.Success
        assertEquals(response, success.response)
        assertEquals(100, success.summary.totalIuranBulanan)
        assertEquals(50, success.summary.totalPengeluaran)
        assertTrue(success.summary.isValid)
    }

    @Test
    fun loadFinancialDataRekapAppliesTheMultiplierFormula() = runTest {
        pemanfaatanRepository.result = Result.success(PemanfaatanResponse(data = listOf(item(totalIndividu = 150))))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value as FinancialDataState.Success
        assertEquals(150 * 3 - 50, state.summary.rekapIuran)
    }

    @Test
    fun loadFinancialDataEmitsErrorCarryingTheRepositoryMessage() = runTest {
        val errorMessage = "Network error occurred"
        pemanfaatanRepository.result = Result.failure(IOException(errorMessage))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value
        assertTrue(state is FinancialDataState.Error)
        assertEquals(errorMessage, (state as FinancialDataState.Error).message)
    }

    @Test
    fun loadFinancialDataDoesNotIssueDuplicateConcurrentRequests() = runTest {
        pemanfaatanRepository.result = Result.success(PemanfaatanResponse(data = listOf(item())))

        viewModel.loadFinancialData()
        viewModel.loadFinancialData()
        advanceUntilIdle()

        assertEquals(1, pemanfaatanRepository.getPemanfaatanCalls)
    }

    @Test
    fun loadFinancialDataTreatsEmptyPayloadAsValidZeroedSummary() = runTest {
        pemanfaatanRepository.result = Result.success(PemanfaatanResponse(data = emptyList()))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value as FinancialDataState.Success
        assertEquals(0, state.summary.totalIuranBulanan)
        assertEquals(0, state.summary.rekapIuran)
        assertTrue(state.summary.isValid)
    }

    @Test
    fun loadFinancialDataFlagsSummaryInvalidForNegativeFinancialData() = runTest {
        pemanfaatanRepository.result = Result.success(PemanfaatanResponse(data = listOf(item(iuranPerwarga = -1))))

        viewModel.loadFinancialData()
        advanceUntilIdle()

        val state = viewModel.financialState.value as FinancialDataState.Success
        assertFalse(state.summary.isValid)
    }
}
