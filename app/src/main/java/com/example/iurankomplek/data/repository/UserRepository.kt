package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.data.api.models.UserResponse

interface UserRepository {
    suspend fun getUsers(forceRefresh: Boolean = false): OperationResult<UserResponse>
    suspend fun getCachedUsers(): OperationResult<UserResponse>
    suspend fun clearCache(): OperationResult<Unit>
}