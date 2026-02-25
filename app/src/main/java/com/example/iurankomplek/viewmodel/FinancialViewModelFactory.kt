package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.data.repository.PemanfaatanRepository

class FinancialViewModelFactory(
    private val pemanfaatanRepository: PemanfaatanRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
            // Legitimate: Safe cast due to isAssignableFrom check above. Kotlin type erasure requires this cast from ViewModel to generic T.
            @Suppress("UNCHECKED_CAST")
            return FinancialViewModel(pemanfaatanRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}