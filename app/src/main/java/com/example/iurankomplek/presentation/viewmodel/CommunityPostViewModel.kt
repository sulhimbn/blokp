package com.example.iurankomplek.presentation.viewmodel

import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.domain.usecase.LoadCommunityPostsUseCase
import com.example.iurankomplek.domain.usecase.CreateCommunityPostUseCase
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CommunityPostViewModel(
    private val loadCommunityPostsUseCase: LoadCommunityPostsUseCase,
    private val createCommunityPostUseCase: CreateCommunityPostUseCase
) : BaseViewModel() {

    private val _postsState = createMutableStateFlow<List<CommunityPost>>(UiState.Loading)
    val postsState: StateFlow<UiState<List<CommunityPost>>> = _postsState

    private val _createPostState = createMutableStateFlow<CommunityPost>(UiState.Idle)
    val createPostState: StateFlow<UiState<CommunityPost>> = _createPostState

    fun loadPosts(forceRefresh: Boolean = false) {
        executeWithLoadingStateForResult(_postsState, preventDuplicate = !forceRefresh) {
            loadCommunityPostsUseCase(forceRefresh)
        }
    }

    fun refreshPosts() {
        loadPosts(forceRefresh = true)
    }

    fun createPost(authorId: String, title: String, content: String, category: String) {
        executeWithLoadingStateForResult(_createPostState, preventDuplicate = false) {
            createCommunityPostUseCase(authorId, title, content, category)
        }
    }

    companion object {
        fun Factory(
            loadCommunityPostsUseCase: LoadCommunityPostsUseCase,
            createCommunityPostUseCase: CreateCommunityPostUseCase
        ) = viewModelInstance {
            CommunityPostViewModel(loadCommunityPostsUseCase, createCommunityPostUseCase)
        }
    }
}
