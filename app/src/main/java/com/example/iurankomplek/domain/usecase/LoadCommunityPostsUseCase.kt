package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.utils.OperationResult

class LoadCommunityPostsUseCase(
    private val communityPostRepository: CommunityPostRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): OperationResult<List<CommunityPost>> {
        return try {
            communityPostRepository.getCommunityPosts(forceRefresh)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load posts")
        }
    }
}
