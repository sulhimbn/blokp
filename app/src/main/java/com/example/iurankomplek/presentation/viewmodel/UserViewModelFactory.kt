package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.domain.usecase.LoadUsersUseCase

class UserViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val loadUsersUseCase = LoadUsersUseCase(userRepository)
            return UserViewModel(loadUsersUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}