package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.model.UserResponse
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
}