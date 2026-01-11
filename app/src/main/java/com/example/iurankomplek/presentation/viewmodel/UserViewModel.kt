package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.domain.usecase.LoadUsersUseCase
import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel(
    private val loadUsersUseCase: LoadUsersUseCase
) : BaseViewModel() {
    
    private val _usersState = createMutableStateFlow<UserResponse>(UiState.Loading)
    val usersState: StateFlow<UiState<UserResponse>> = _usersState
    
    fun loadUsers() {
        executeWithLoadingStateForResult(_usersState) {
            loadUsersUseCase()
        }
    }
    
    class Factory(private val loadUsersUseCase: LoadUsersUseCase) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(loadUsersUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}