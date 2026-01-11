package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.flow.Flow

class LoadCommunityPostsUseCase(
    private val communityPostRepository: CommunityPostRepository
) {
    operator fun invoke(forceRefresh: Boolean = false): Flow<OperationResult<List<CommunityPost>>> {
        return try {
            communityPostRepository.getCommunityPosts(forceRefresh)
                .map { OperationResult.Success(it) }
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flowOf(OperationResult.Error(e, e.message ?: "Failed to load posts"))
        }
    }
}
