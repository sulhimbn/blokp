package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.UserResponse

interface UserRepository {
    suspend fun getUsers(): Result<UserResponse>
}