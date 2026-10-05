package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.event.AppEvent
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val eventBus: EventBus
) : ViewModel() {

    private val _usersState = MutableStateFlow<UiState<UserResponse>>(UiState.Loading)
    private var _loadInFlight = false
    val usersState: StateFlow<UiState<UserResponse>> = _usersState

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is AppEvent.UserProfileUpdated,
                    is AppEvent.RefreshAllData -> loadUsers()
                    else -> {}
                }
            }
        }
    }

    fun loadUsers() {
        if (_loadInFlight) return

        _loadInFlight = true
        viewModelScope.launch {
            try {
                _usersState.value = UiState.Loading
                userRepository.getUsers()
                    .onSuccess { response ->
                        _usersState.value = UiState.Success(response)
                    }
                    .onFailure { exception ->
                        _usersState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                    }
            } finally {
                _loadInFlight = false
            }
        }
    }
}
