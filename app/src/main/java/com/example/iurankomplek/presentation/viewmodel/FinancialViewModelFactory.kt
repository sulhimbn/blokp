package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.data.repository.PemanfaatanRepository
import com.example.iurankomplek.domain.usecase.LoadFinancialDataUseCase

class FinancialViewModelFactory(
    private val pemanfaatanRepository: PemanfaatanRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val loadFinancialDataUseCase = LoadFinancialDataUseCase(pemanfaatanRepository)
            return FinancialViewModel(loadFinancialDataUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}