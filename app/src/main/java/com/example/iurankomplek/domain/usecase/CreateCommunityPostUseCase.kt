package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.CommunityPostRepository
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.utils.OperationResult

class CreateCommunityPostUseCase(
    private val communityPostRepository: CommunityPostRepository
) {
    suspend operator fun invoke(authorId: String, title: String, content: String, category: String): OperationResult<CommunityPost> {
        return try {
            communityPostRepository.createCommunityPost(authorId, title, content, category)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to create post")
        }
    }
}
