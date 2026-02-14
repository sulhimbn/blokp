package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.ErrorHandler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: UserSessionManager
) : BaseNetworkRepository(), UserRepository {
    
    override val errorHandler = ErrorHandler()

    override suspend fun getUsers(): Result<UserResponse> {
        return executeWithRetry(
            operation = { apiService.getUsers() },
            transform = { it }
        )
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            getUsers().fold(
                onSuccess = { response ->
                    val user = response.data?.find { it.email == email }
                    if (user != null) {
                        val authenticatedUser = User(
                            id = user.email,
                            email = user.email,
                            firstName = user.first_name,
                            lastName = user.last_name,
                            avatar = user.avatar
                        )
                        sessionManager.setCurrentUser(authenticatedUser)
                        Result.success(authenticatedUser)
                    } else {
                        Result.failure(Exception("Invalid credentials"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Login failed: ${e.message}"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Logout failed: ${e.message}"))
        }
    }

    override fun getCurrentUserFlow(): Flow<User?> {
        return sessionManager.currentUser
    }
}
