package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.data.repository.PemanfaatanRepository

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