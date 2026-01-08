package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _usersState = MutableStateFlow<UiState<UserResponse>>(UiState.Loading)
    val usersState: StateFlow<UiState<UserResponse>> = _usersState
    
    fun loadUsers() {
        if (_usersState.value is UiState.Loading) return // Prevent duplicate calls
        
        viewModelScope.launch {
            _usersState.value = UiState.Loading
            userRepository.getUsers()
                .onSuccess { response ->
                    _usersState.value = UiState.Success(response)
                }
                .onFailure { exception ->
                    _usersState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }
    
    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}