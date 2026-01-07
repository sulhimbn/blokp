package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityPostViewModel(
    private val communityPostRepository: CommunityPostRepository
) : ViewModel() {

    private val _postsState = MutableStateFlow<UiState<List<CommunityPost>>>(UiState.Loading)
    val postsState: StateFlow<UiState<List<CommunityPost>>> = _postsState

    private val _createPostState = MutableStateFlow<UiState<CommunityPost>>(UiState.Idle)
    val createPostState: StateFlow<UiState<CommunityPost>> = _createPostState

    fun loadPosts(forceRefresh: Boolean = false) {
        if (_postsState.value is UiState.Loading) return

        viewModelScope.launch {
            _postsState.value = UiState.Loading
            communityPostRepository.getCommunityPosts(forceRefresh)
                .onSuccess { posts ->
                    _postsState.value = UiState.Success(posts)
                }
                .onFailure { exception ->
                    _postsState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }

    fun refreshPosts() {
        loadPosts(forceRefresh = true)
    }

    fun createPost(authorId: String, title: String, content: String, category: String) {
        viewModelScope.launch {
            _createPostState.value = UiState.Loading
            communityPostRepository.createCommunityPost(authorId, title, content, category)
                .onSuccess { post ->
                    _createPostState.value = UiState.Success(post)
                }
                .onFailure { exception ->
                    _createPostState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }

    class Factory(private val communityPostRepository: CommunityPostRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CommunityPostViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CommunityPostViewModel(communityPostRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
