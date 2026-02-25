package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.data.repository.UserRepository

class UserViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            // NOTE: UNCHECKED_CAST is required for ViewModelProvider.Factory.
            // The create() method returns T but we must cast from concrete ViewModel.
            // This is the standard Android pattern for ViewModel factories.
            // Safety: The isAssignableFrom check ensures type safety before casting.
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}