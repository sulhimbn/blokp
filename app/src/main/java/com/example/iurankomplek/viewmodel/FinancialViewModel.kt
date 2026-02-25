package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.event.AppEvent
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.transaction.TransactionRepository
import com.example.iurankomplek.utils.FinancialCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FinancialSummary(
    val totalIuranBulanan: Int = 0,
    val totalPengeluaran: Int = 0,
    val totalIuranIndividu: Int = 0,
    val rekapIuran: Int = 0,
    val totalPaymentsProcessed: Int = 0,
    val completedTransactionsCount: Int = 0,
    val isValid: Boolean = true
)

sealed class FinancialDataState {
    data object Loading : FinancialDataState()
    data class Success(
        val response: PemanfaatanResponse,
        val summary: FinancialSummary
    ) : FinancialDataState()
    data class Error(val message: String) : FinancialDataState()
}

@HiltViewModel
class FinancialViewModel @Inject constructor(
    private val pemanfaatanRepository: PemanfaatanRepository,
    private val eventBus: EventBus,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _financialState = MutableStateFlow<FinancialDataState>(FinancialDataState.Loading)
    val financialState: StateFlow<FinancialDataState> = _financialState

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is AppEvent.PaymentCompleted,
                    is AppEvent.TransactionCreated,
                    is AppEvent.FinancialDataUpdated,
                    is AppEvent.RefreshAllData -> loadFinancialData()
                    else -> {}
                }
            }
        }
    }

    fun loadFinancialData() {
        if (_financialState.value is FinancialDataState.Loading) return

        viewModelScope.launch {
            _financialState.value = FinancialDataState.Loading
            pemanfaatanRepository.getPemanfaatan()
                .onSuccess { response ->
                    val items = response.data
                    val summary = calculateFinancialSummary(items)
                    _financialState.value = FinancialDataState.Success(response, summary)
                }
                .onFailure { exception ->
                    _financialState.value = FinancialDataState.Error(
                        exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    private suspend fun calculateFinancialSummary(items: List<DataItem>): FinancialSummary {
        return try {
            if (items.isEmpty()) {
                return FinancialSummary(isValid = true)
            }

            if (!FinancialCalculator.validateDataItems(items)) {
                return FinancialSummary(isValid = false)
            }

            // Get payment transaction data from repository
            val paymentData = getPaymentTransactionData()

            FinancialSummary(
                totalIuranBulanan = FinancialCalculator.calculateTotalIuranBulanan(items),
                totalPengeluaran = FinancialCalculator.calculateTotalPengeluaran(items),
                totalIuranIndividu = FinancialCalculator.calculateTotalIuranIndividu(items),
                rekapIuran = FinancialCalculator.calculateRekapIuran(items),
                totalPaymentsProcessed = paymentData.first,
                completedTransactionsCount = paymentData.second,
                isValid = true
            )
        } catch (e: Exception) {
            FinancialSummary(isValid = false)
        }
    }

    private suspend fun getPaymentTransactionData(): Pair<Int, Int> {
        return try {
            val completedTransactions = transactionRepository.getTransactionsByStatus(PaymentStatus.COMPLETED).first()
            val paymentTotal = completedTransactions.sumOf { it.amount.toInt() }
            Pair(paymentTotal, completedTransactions.size)
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }
}

class FinancialViewModelFactory(
    private val pemanfaatanRepository: PemanfaatanRepository,
    private val eventBus: EventBus,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
            // Legitimate: Safe cast due to isAssignableFrom check above. Kotlin type erasure requires this cast from ViewModel to generic T.
            @Suppress("UNCHECKED_CAST")
            return FinancialViewModel(pemanfaatanRepository, eventBus, transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
