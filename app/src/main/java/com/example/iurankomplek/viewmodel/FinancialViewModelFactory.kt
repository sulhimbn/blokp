package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.data.repository.PemanfaatanRepository

class FinancialViewModelFactory(
    private val pemanfaatanRepository: PemanfaatanRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
            // NOTE: UNCHECKED_CAST is required for ViewModelProvider.Factory.
            // The create() method returns T but we must cast from concrete ViewModel.
            // This is the standard Android pattern for ViewModel factories.
            // Safety: The isAssignableFrom check ensures type safety before casting.
            @Suppress("UNCHECKED_CAST")
            return FinancialViewModel(pemanfaatanRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}