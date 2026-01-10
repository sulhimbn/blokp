package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.utils.Result

interface UserRepository {
    suspend fun getUsers(forceRefresh: Boolean = false): Result<UserResponse>
    suspend fun getCachedUsers(): Result<UserResponse>
    suspend fun clearCache(): Result<Unit>
}