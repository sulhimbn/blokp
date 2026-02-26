package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor() : ViewModel() {

    private val _communityPostsState = MutableStateFlow<UiState<List<CommunityPost>>>(UiState.Loading)
    val communityPostsState: StateFlow<UiState<List<CommunityPost>>> = _communityPostsState

    init {
        loadCommunityPosts()
    }

    fun loadCommunityPosts() {
        viewModelScope.launch {
            _communityPostsState.value = UiState.Loading
            try {
                val response = ApiConfig.getApiService().getCommunityPosts()
                if (response.isSuccessful) {
                    val posts = response.body()
                    if (posts != null) {
                        _communityPostsState.value = UiState.Success(posts)
                    } else {
                        _communityPostsState.value = UiState.Error("No community posts available")
                    }
                } else {
                    _communityPostsState.value = UiState.Error("Failed to load community posts")
                }
            } catch (e: Exception) {
                _communityPostsState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
