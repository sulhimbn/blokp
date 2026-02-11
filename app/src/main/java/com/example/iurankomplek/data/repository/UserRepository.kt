package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUsers(): Result<UserResponse>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    fun getCurrentUserFlow(): Flow<User?>
}