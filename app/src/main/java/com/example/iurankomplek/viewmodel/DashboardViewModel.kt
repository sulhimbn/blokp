package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.event.AppEvent
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.model.DashboardData
import com.example.iurankomplek.model.FinancialSummary
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.model.PaymentStatus
import com.example.iurankomplek.utils.FinancialCalculator
import com.example.iurankomplek.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pemanfaatanRepository: PemanfaatanRepository,
    private val eventBus: EventBus
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val dashboardState: StateFlow<UiState<DashboardData>> = _dashboardState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        observeEvents()
        loadDashboardData()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is AppEvent.PaymentCompleted,
                    is AppEvent.TransactionCreated,
                    is AppEvent.FinancialDataUpdated,
                    is AppEvent.NewAnnouncement,
                    is AppEvent.NewMessage,
                    is AppEvent.RefreshAllData -> refreshDashboard()
                    else -> {}
                }
            }
        }
    }

    fun loadDashboardData() {
        if (_dashboardState.value is UiState.Loading && _isRefreshing.value) return

        viewModelScope.launch {
            if (!_isRefreshing.value) {
                _dashboardState.value = UiState.Loading
            }

            try {
                val usersDeferred = async { userRepository.getUsers() }
                val pemanfaatanDeferred = async { pemanfaatanRepository.getPemanfaatan() }

                val usersResult = usersDeferred.await()
                val pemanfaatanResult = pemanfaatanDeferred.await()

                if (usersResult.isSuccess && pemanfaatanResult.isSuccess) {
                    val users = usersResult.getOrNull()?.data ?: emptyList()
                    val pemanfaatan = pemanfaatanResult.getOrNull()?.data ?: emptyList()

                    val financialSummary = calculateFinancialSummary(pemanfaatan, users.size)

                    val dashboardData = DashboardData(
                        financialSummary = financialSummary,
                        announcements = emptyList(),
                        messages = emptyList(),
                        communityPosts = emptyList(),
                        unreadAnnouncements = 0,
                        unreadMessages = 0
                    )

                    _dashboardState.value = UiState.Success(dashboardData)
                } else {
                    val error = usersResult.exceptionOrNull()?.message
                        ?: pemanfaatanResult.exceptionOrNull()?.message
                        ?: "Failed to load dashboard data"
                    _dashboardState.value = UiState.Error(error)
                }
            } catch (e: Exception) {
                _dashboardState.value = UiState.Error(e.message ?: "Unknown error occurred")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshDashboard() {
        _isRefreshing.value = true
        loadDashboardData()
    }

    private fun calculateFinancialSummary(
        dataItems: List<com.example.iurankomplek.model.DataItem>,
        totalResidents: Int
    ): FinancialSummary {
        val totalCollected = FinancialCalculator.calculateTotalIuranIndividu(dataItems)
        val totalExpenses = FinancialCalculator.calculateTotalPengeluaran(dataItems)
        val balance = FinancialCalculator.calculateRekapIuran(dataItems)
        val totalDue = FinancialCalculator.calculateTotalIuranBulanan(dataItems)

        val paymentStatus = when {
            balance > totalDue * 0.8 -> PaymentStatus.EXCELLENT
            balance > totalDue * 0.5 -> PaymentStatus.GOOD
            balance > totalDue * 0.2 -> PaymentStatus.FAIR
            else -> PaymentStatus.POOR
        }

        return FinancialSummary(
            totalDue = totalDue,
            totalCollected = totalCollected,
            totalExpenses = totalExpenses,
            balance = balance,
            paymentStatus = paymentStatus,
            lastPaymentDate = null,
            totalResidents = totalResidents,
            paidResidents = totalResidents
        )
    }

    fun formatCurrency(amount: Int): String {
        return FinancialCalculator.formatCurrency(amount)
    }
}
