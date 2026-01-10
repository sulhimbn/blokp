package com.example.iurankomplek.domain.usecase
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.data.repository.UserRepository

/**
 * Use case for loading users with business logic
 * Encapsulates user loading logic and business rules
 */
class LoadUsersUseCase(
    private val userRepository: UserRepository
) {
    
    /**
     * Loads users from repository
     * Includes business logic for user loading
     * 
     * @return Result<UserResponse> with success or error
     */
    suspend operator fun invoke(): Result<UserResponse> {
        return try {
            userRepository.getUsers()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Loads users with additional business rules
     * 
     * @param forceRefresh If true, bypasses cache if available
     * @return Result<UserResponse> with success or error
     */
    suspend operator fun invoke(forceRefresh: Boolean): Result<UserResponse> {
        return try {
            userRepository.getUsers(forceRefresh = forceRefresh)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
