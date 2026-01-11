package com.example.iurankomplek.network

import com.example.iurankomplek.BuildConfig
import com.example.iurankomplek.network.interceptor.HealthCheckInterceptor
import com.example.iurankomplek.network.interceptor.IdempotencyInterceptor
import com.example.iurankomplek.network.interceptor.NetworkErrorInterceptor
import com.example.iurankomplek.network.interceptor.RequestIdInterceptor
import com.example.iurankomplek.network.interceptor.RetryableRequestInterceptor
import com.example.iurankomplek.network.interceptor.RateLimiterInterceptor
import com.example.iurankomplek.network.interceptor.TimeoutInterceptor
import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerState
import com.example.iurankomplek.utils.Constants
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

 object ApiConfig {
    // Use mock API in debug mode or when running in Docker
    private val USE_MOCK_API = BuildConfig.DEBUG || System.getenv(Constants.Api.DOCKER_ENV_KEY) != null
    private val BASE_URL = if (USE_MOCK_API) {
        Constants.Api.MOCK_BASE_URL + BuildConfig.API_SPREADSHEET_ID + "/"
    } else {
        Constants.Api.PRODUCTION_BASE_URL + BuildConfig.API_SPREADSHEET_ID + "/"
    }
    
    // Connection pool for efficient HTTP connection reuse
    private val connectionPool = ConnectionPool(
        Constants.Network.MAX_IDLE_CONNECTIONS,
        Constants.Network.KEEP_ALIVE_DURATION_MINUTES,
        TimeUnit.MINUTES
    )
    
    // Cache Retrofit instances to avoid recreation
    @Volatile
    private var apiServiceInstance: ApiService? = null
    
    @Volatile
    private var apiServiceV1Instance: ApiServiceV1? = null

    // Circuit breaker for service resilience
    val circuitBreaker: CircuitBreaker = CircuitBreaker(
        failureThreshold = Constants.Network.MAX_RETRIES,
        successThreshold = 2,
        timeout = Constants.Network.MAX_RETRY_DELAY_MS,
        halfOpenMaxCalls = 3
    )

    // Rate limiter for preventing API overload
    val rateLimiter: RateLimiterInterceptor = RateLimiterInterceptor(
        maxRequestsPerSecond = Constants.Network.MAX_REQUESTS_PER_SECOND,
        maxRequestsPerMinute = Constants.Network.MAX_REQUESTS_PER_MINUTE,
        enableLogging = BuildConfig.DEBUG
    )
    
    fun getApiService(): ApiService {
        return apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: createApiService().also { apiServiceInstance = it }
        }
    }
    
    fun getApiServiceV1(): ApiServiceV1 {
        return apiServiceV1Instance ?: synchronized(this) {
            apiServiceV1Instance ?: createApiServiceV1().also { apiServiceV1Instance = it }
        }
    }
    
    private fun createApiService(): ApiService {
        return createRetrofitService(ApiService::class.java)
    }

    private fun createApiServiceV1(): ApiServiceV1 {
        return createRetrofitService(ApiServiceV1::class.java)
    }

    private fun <T> createRetrofitService(serviceClass: Class<T>): T {
        val okHttpClient = if (!USE_MOCK_API) {
            SecurityConfig.getSecureOkHttpClient()
                .newBuilder()
                .connectionPool(connectionPool)
                .addInterceptor(TimeoutInterceptor())
                .addInterceptor(RequestIdInterceptor())
                .addInterceptor(IdempotencyInterceptor())
                .addInterceptor(rateLimiter)
                .addInterceptor(RetryableRequestInterceptor())
                .addInterceptor(HealthCheckInterceptor(enableLogging = BuildConfig.DEBUG))
                .addInterceptor(NetworkErrorInterceptor(enableLogging = BuildConfig.DEBUG))
                .build()
        } else {
            val clientBuilder = OkHttpClient.Builder()
                .connectTimeout(Constants.Network.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.Network.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.Network.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .connectionPool(connectionPool)
                .addInterceptor(TimeoutInterceptor())
                .addInterceptor(RequestIdInterceptor())
                .addInterceptor(IdempotencyInterceptor())
                .addInterceptor(rateLimiter)
                .addInterceptor(RetryableRequestInterceptor())
                .addInterceptor(HealthCheckInterceptor(enableLogging = true))
                .addInterceptor(NetworkErrorInterceptor(enableLogging = true))

            if (BuildConfig.DEBUG) {
                val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                }
                clientBuilder.addInterceptor(loggingInterceptor)
            }

            clientBuilder.build()
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(serviceClass)
    }
    
    suspend fun resetCircuitBreaker() {
        circuitBreaker.reset()
    }
    
    fun getCircuitBreakerState(): CircuitBreakerState {
        return circuitBreaker.getState()
    }
    
    fun getRateLimiterStats(): Map<String, RateLimiterInterceptor.EndpointStats> {
        return rateLimiter.getAllStats()
    }
    
    fun resetRateLimiter() {
        rateLimiter.reset()
    }
}