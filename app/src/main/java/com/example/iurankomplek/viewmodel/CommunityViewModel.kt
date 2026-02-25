package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityViewModel(
    private val apiService: com.example.iurankomplek.network.ApiService
) : ViewModel() {

    private val _communityState = MutableStateFlow<UiState<List<CommunityPost>>>(UiState.Loading)
    val communityState: StateFlow<UiState<List<CommunityPost>>> = _communityState

    fun loadCommunityPosts() {
        if (_communityState.value is UiState.Loading) return

        viewModelScope.launch {
            _communityState.value = UiState.Loading
            try {
                val response = apiService.getCommunityPosts()
                if (response.isSuccessful) {
                    val posts = response.body()
                    if (posts != null) {
                        _communityState.value = UiState.Success(posts)
                    } else {
                        _communityState.value = UiState.Error("No community posts available")
                    }
                } else {
                    _communityState.value = UiState.Error("Failed to load community posts: ${response.code()}")
                }
            } catch (e: Exception) {
                _communityState.value = UiState.Error("Network error: ${e.message}")
            }
        }
    }

    class Factory(
        private val apiService: com.example.iurankomplek.network.ApiService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CommunityViewModel(apiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
