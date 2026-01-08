package com.example.iurankomplek.network.resilience

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

sealed class CircuitBreakerState {
    object Closed : CircuitBreakerState()
    object Open : CircuitBreakerState()
    object HalfOpen : CircuitBreakerState()
}

sealed class CircuitBreakerResult<out T> {
    data class Success<T>(val value: T) : CircuitBreakerResult<T>()
    data class Failure(val exception: Throwable) : CircuitBreakerResult<Nothing>()
    object CircuitOpen : CircuitBreakerResult<Nothing>()
}

class CircuitBreaker(
    private val failureThreshold: Int = com.example.iurankomplek.utils.Constants.CircuitBreaker.DEFAULT_FAILURE_THRESHOLD,
    private val successThreshold: Int = com.example.iurankomplek.utils.Constants.CircuitBreaker.DEFAULT_SUCCESS_THRESHOLD,
    private val timeout: Long = com.example.iurankomplek.utils.Constants.CircuitBreaker.DEFAULT_TIMEOUT_MS,
    private val halfOpenMaxCalls: Int = com.example.iurankomplek.utils.Constants.CircuitBreaker.DEFAULT_HALF_OPEN_MAX_CALLS
) {
    private val stateMutex = Mutex()
    
    private var currentState: CircuitBreakerState = CircuitBreakerState.Closed
    
    private val failureCount = AtomicInteger(0)
    private val successCount = AtomicInteger(0)
    private val halfOpenCallCount = AtomicInteger(0)
    private val lastFailureTime = AtomicLong(0)
    
    suspend fun <T> execute(block: suspend () -> T): CircuitBreakerResult<T> {
        return stateMutex.withLock {
            when (currentState) {
                is CircuitBreakerState.Open -> {
                    if (shouldAttemptReset()) {
                        currentState = CircuitBreakerState.HalfOpen
                        successCount.set(0)
                        halfOpenCallCount.set(0)
                        attemptExecution(block)
                    } else {
                        CircuitBreakerResult.CircuitOpen
                    }
                }
                is CircuitBreakerState.HalfOpen -> {
                    attemptExecution(block)
                }
                is CircuitBreakerState.Closed -> {
                    attemptExecution(block)
                }
            }
        }
    }
    
    private suspend fun <T> attemptExecution(block: suspend () -> T): CircuitBreakerResult<T> {
        return try {
            val result = block()
            onSuccess()
            CircuitBreakerResult.Success(result)
        } catch (e: Exception) {
            onFailure(e)
            CircuitBreakerResult.Failure(e)
        }
    }
    
    private fun onSuccess() {
        when (currentState) {
            is CircuitBreakerState.HalfOpen -> {
                val currentHalfOpenCalls = halfOpenCallCount.incrementAndGet()
                val currentSuccesses = successCount.incrementAndGet()
                
                if (currentSuccesses >= successThreshold || currentHalfOpenCalls >= halfOpenMaxCalls) {
                    if (currentSuccesses >= successThreshold) {
                        resetToClosed()
                    } else {
                        tripToOpen()
                    }
                }
            }
            is CircuitBreakerState.Closed -> {
                failureCount.set(0)
            }
            is CircuitBreakerState.Open -> {
            }
        }
    }
    
    private fun onFailure(_exception: Throwable) {
        lastFailureTime.set(System.currentTimeMillis())
        
        when (currentState) {
            is CircuitBreakerState.Closed -> {
                val currentFailures = failureCount.incrementAndGet()
                if (currentFailures >= failureThreshold) {
                    tripToOpen()
                }
            }
            is CircuitBreakerState.HalfOpen -> {
                tripToOpen()
            }
            is CircuitBreakerState.Open -> {
            }
        }
    }
    
    private fun tripToOpen() {
        currentState = CircuitBreakerState.Open
        failureCount.set(0)
        successCount.set(0)
        halfOpenCallCount.set(0)
    }
    
    private fun resetToClosed() {
        currentState = CircuitBreakerState.Closed
        failureCount.set(0)
        successCount.set(0)
        halfOpenCallCount.set(0)
    }
    
    private fun shouldAttemptReset(): Boolean {
        val timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get()
        return timeSinceLastFailure >= timeout
    }
    
    fun getState(): CircuitBreakerState = currentState
    
    fun getFailureCount(): Int = failureCount.get()
    
    fun getSuccessCount(): Int = successCount.get()
    
    fun getLastFailureTime(): Long = lastFailureTime.get()
    
    suspend fun reset() {
        stateMutex.withLock {
            resetToClosed()
        }
    }
}

class CircuitBreakerException(message: String) : Exception(message)
