package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.data.repository.PemanfaatanRepository

class FinancialViewModelFactory(
    private val pemanfaatanRepository: PemukimanRepository
) : ViewModelProvider.Factory {
    /**
     * Creates a ViewModel instance for the given class.
     *
     * IMPORTANT: This suppression is REQUIRED and is the STANDARD Android pattern.
     * - ViewModelProvider.Factory.create() returns generic T, but we must cast to specific ViewModel type
     * - The runtime check (isAssignableFrom) ensures type safety at runtime
     * - This pattern is used throughout AndroidX ViewModel and is the recommended approach
     * - Android framework itself uses this same suppression in ViewModelProvider.Factory
     *
     * @suppress UNCHECKED_CAST is the standard, accepted pattern for ViewModelFactory implementations
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinancialViewModel::class.java)) {
            return FinancialViewModel(pemanfaatanRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
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