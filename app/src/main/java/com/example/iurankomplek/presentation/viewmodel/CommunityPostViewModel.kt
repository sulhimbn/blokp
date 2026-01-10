package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CommunityPostViewModel(
    private val communityPostRepository: CommunityPostRepository
) : BaseViewModel() {

    private val _postsState = createMutableStateFlow<List<CommunityPost>>(UiState.Loading)
    val postsState: StateFlow<UiState<List<CommunityPost>>> = _postsState

    private val _createPostState = createMutableStateFlow<CommunityPost>(UiState.Idle)
    val createPostState: StateFlow<UiState<CommunityPost>> = _createPostState

    fun loadPosts(forceRefresh: Boolean = false) {
        executeWithLoadingStateForResult(_postsState, preventDuplicate = !forceRefresh) {
            communityPostRepository.getCommunityPosts(forceRefresh)
        }
    }

    fun refreshPosts() {
        loadPosts(forceRefresh = true)
    }

    fun createPost(authorId: String, title: String, content: String, category: String) {
        executeWithLoadingStateForResult(_createPostState, preventDuplicate = false) {
            communityPostRepository.createCommunityPost(authorId, title, content, category)
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