package com.example.iurankomplek.domain.usecase
import com.example.iurankomplek.utils.OperationResult
import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.data.repository.UserRepository

class LoadUsersUseCase(
    private val userRepository: UserRepository
) {
    
    suspend operator fun invoke(): OperationResult<UserResponse> {
        return try {
            userRepository.getUsers()
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load users")
        }
    }
    
    suspend operator fun invoke(forceRefresh: Boolean): OperationResult<UserResponse> {
        return try {
            userRepository.getUsers(forceRefresh = forceRefresh)
        } catch (e: Exception) {
            OperationResult.Error(e, e.message ?: "Failed to load users")
        }
    }
}
