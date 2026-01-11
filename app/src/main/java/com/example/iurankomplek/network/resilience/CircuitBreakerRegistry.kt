package com.example.iurankomplek.network.resilience

import com.example.iurankomplek.utils.Constants
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

data class CircuitBreakerConfig(
    val failureThreshold: Int = Constants.CircuitBreaker.DEFAULT_FAILURE_THRESHOLD,
    val successThreshold: Int = Constants.CircuitBreaker.DEFAULT_SUCCESS_THRESHOLD,
    val timeoutMs: Long = Constants.CircuitBreaker.DEFAULT_TIMEOUT_MS,
    val halfOpenMaxCalls: Int = Constants.CircuitBreaker.DEFAULT_HALF_OPEN_MAX_CALLS
)

data class CircuitBreakerStats(
    val endpoint: String,
    val state: CircuitBreakerState,
    val failureCount: Int,
    val successCount: Int,
    val lastFailureTime: Long,
    val totalCalls: Int,
    val totalFailures: Int,
    val totalSuccesses: Int
)

class CircuitBreakerRegistry(
    private val defaultConfig: CircuitBreakerConfig = CircuitBreakerConfig()
) {
    private val circuitBreakers = ConcurrentHashMap<String, CircuitBreaker>()
    private val configs = ConcurrentHashMap<String, CircuitBreakerConfig>()
    private val mutex = Mutex()
    private val stats = ConcurrentHashMap<String, CircuitBreakerStats>()
    
    fun getEndpoint(endpoint: String): CircuitBreaker {
        return circuitBreakers.getOrPut(endpoint) {
            val config = configs[endpoint] ?: defaultConfig
            CircuitBreaker(
                failureThreshold = config.failureThreshold,
                successThreshold = config.successThreshold,
                timeout = config.timeoutMs,
                halfOpenMaxCalls = config.halfOpenMaxCalls
            )
        }
    }
    
    fun registerEndpoint(endpoint: String, config: CircuitBreakerConfig) {
        configs[endpoint] = config
    }
    
    fun unregisterEndpoint(endpoint: String) {
        circuitBreakers.remove(endpoint)
        configs.remove(endpoint)
        stats.remove(endpoint)
    }
    
    suspend fun <T> execute(
        endpoint: String,
        block: suspend () -> T
    ): CircuitBreakerResult<T> {
        val circuitBreaker = getEndpoint(endpoint)
        val result = circuitBreaker.execute(block)
        
        updateStats(endpoint, circuitBreaker, result)
        
        return result
    }
    
    private suspend fun updateStats(
        endpoint: String,
        circuitBreaker: CircuitBreaker,
        result: CircuitBreakerResult<*>
    ) {
        mutex.withLock {
            val currentState = circuitBreaker.getState()
            val currentStats = stats.getOrPut(endpoint) {
                CircuitBreakerStats(
                    endpoint = endpoint,
                    state = currentState,
                    failureCount = circuitBreaker.getFailureCount(),
                    successCount = circuitBreaker.getSuccessCount(),
                    lastFailureTime = circuitBreaker.getLastFailureTime(),
                    totalCalls = 0,
                    totalFailures = 0,
                    totalSuccesses = 0
                )
            }
            
            val updatedStats = currentStats.copy(
                state = currentState,
                failureCount = circuitBreaker.getFailureCount(),
                successCount = circuitBreaker.getSuccessCount(),
                lastFailureTime = circuitBreaker.getLastFailureTime(),
                totalCalls = currentStats.totalCalls + 1,
                totalFailures = currentStats.totalFailures + if (result is CircuitBreakerResult.Failure) 1 else 0,
                totalSuccesses = currentStats.totalSuccesses + if (result is CircuitBreakerResult.Success) 1 else 0
            )
            
            stats[endpoint] = updatedStats
        }
    }
    
    fun getStats(endpoint: String): CircuitBreakerStats? = stats[endpoint]
    
    fun getAllStats(): Map<String, CircuitBreakerStats> = stats.toMap()
    
    fun getState(endpoint: String): CircuitBreakerState? {
        return circuitBreakers[endpoint]?.getState()
    }
    
    fun getAllStates(): Map<String, CircuitBreakerState> {
        return circuitBreakers.mapValues { it.value.getState() }
    }
    
    suspend fun resetEndpoint(endpoint: String) {
        circuitBreakers[endpoint]?.reset()
        stats.remove(endpoint)
    }
    
    suspend fun resetAll() {
        circuitBreakers.values.forEach { it.reset() }
        stats.clear()
    }
    
    fun getOpenCircuits(): List<String> {
        return circuitBreakers.filter { 
            it.value.getState() is CircuitBreakerState.Open 
        }.keys.toList()
    }
    
    fun getHalfOpenCircuits(): List<String> {
        return circuitBreakers.filter { 
            it.value.getState() is CircuitBreakerState.HalfOpen 
        }.keys.toList()
    }
    
    fun getClosedCircuits(): List<String> {
        return circuitBreakers.filter { 
            it.value.getState() is CircuitBreakerState.Closed 
        }.keys.toList()
    }
    
    fun getFailureRate(endpoint: String): Double {
        val stat = stats[endpoint] ?: return 0.0
        return if (stat.totalCalls > 0) {
            stat.totalFailures.toDouble() / stat.totalCalls
        } else {
            0.0
        }
    }
    
    fun getAllFailureRates(): Map<String, Double> {
        return stats.keys.associateWith { endpoint ->
            getFailureRate(endpoint)
        }
    }
}
