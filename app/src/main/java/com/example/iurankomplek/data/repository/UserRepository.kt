package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.UserResponse

interface UserRepository {
    suspend fun getUsers(forceRefresh: Boolean = false): Result<UserResponse>
    suspend fun getCachedUsers(): Result<UserResponse>
    suspend fun clearCache(): Result<Unit>
}