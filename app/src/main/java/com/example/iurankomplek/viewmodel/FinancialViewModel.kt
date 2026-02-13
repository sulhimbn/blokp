package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.event.AppEvent
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.PemanfaatanResponse
import com.example.iurankomplek.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinancialViewModel @Inject constructor(
    private val pemanfaatanRepository: PemanfaatanRepository,
    private val eventBus: EventBus
) : ViewModel() {
    
    private val _financialState = MutableStateFlow<UiState<PemanfaatanResponse>>(UiState.Loading)
    val financialState: StateFlow<UiState<PemanfaatanResponse>> = _financialState
    
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
        if (_financialState.value is UiState.Loading) return
        
        viewModelScope.launch {
            _financialState.value = UiState.Loading
            pemanfaatanRepository.getPemanfaatan()
                .onSuccess { response ->
                    _financialState.value = UiState.Success(response)
                }
                .onFailure { exception ->
                    _financialState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }
}

class FinancialViewModelFactory(
    private val pemanfaatanRepository: PemanfaatanRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinancialViewModel(pemanfaatanRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}