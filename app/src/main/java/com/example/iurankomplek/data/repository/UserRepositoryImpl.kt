package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.session.UserSessionManager
import com.example.iurankomplek.utils.ErrorHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLException
import kotlin.math.min
import kotlin.math.pow

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: UserSessionManager
) : UserRepository {
    private val maxRetries = 3
    private val errorHandler = ErrorHandler()

    override suspend fun getUsers(): Result<UserResponse> {
        var currentRetry = 0
        var lastException: Exception? = null

        while (currentRetry <= maxRetries) {
            try {
                val response: Response<UserResponse> = apiService.getUsers()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        return Result.success(responseBody)
                    } else {
                        return Result.failure(Exception("Response body is null"))
                    }
                } else {
                    val isRetryable = isRetryableError(response.code())
                    if (currentRetry < maxRetries && isRetryable) {
                        val delayMillis = calculateDelay(currentRetry + 1, 1000, 30000)
                        delay(delayMillis)
                        currentRetry++
                        continue
                    } else {
                        throw Exception("API request failed with code: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                lastException = e

                val isRetryable = isRetryableException(e)
                if (currentRetry < maxRetries && isRetryable) {
                    val delayMillis = calculateDelay(currentRetry + 1, 1000, 30000)
                    delay(delayMillis)
                    currentRetry++
                } else {
                    break
                }
            }
        }

        val errorMessage = if (lastException != null) {
            errorHandler.handleError(lastException)
        } else {
            "Unknown error occurred"
        }

        return Result.failure(Exception(errorMessage))
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val usersResult = getUsers()
            usersResult.fold(
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

    private fun isRetryableError(httpCode: Int): Boolean {
        return httpCode in 408..429 || httpCode / 100 == 5
    }

    private fun isRetryableException(t: Throwable): Boolean {
        return when (t) {
            is SocketTimeoutException,
            is UnknownHostException,
            is SSLException -> true
            else -> false
        }
    }

    private fun calculateDelay(currentRetry: Int, initialDelayMs: Long, maxDelayMs: Long): Long {
        val exponentialDelay = (initialDelayMs * Math.pow(2.0, (currentRetry - 1).toDouble())).toLong()
        val jitter = (Math.random() * initialDelayMs).toLong()
        return minOf(exponentialDelay + jitter, maxDelayMs)
    }
}
