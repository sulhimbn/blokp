package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import retrofit2.Response

class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun getUsers(): Result<UserResponse> {
        return try {
            val response: Response<UserResponse> = apiService.getUsers()
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Result.success(responseBody)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("API request failed with code: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}