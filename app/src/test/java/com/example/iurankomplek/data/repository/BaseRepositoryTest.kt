package com.example.iurankomplek.data.repository

import com.example.iurankomplek.network.resilience.CircuitBreaker
import com.example.iurankomplek.network.resilience.CircuitBreakerResult
import com.example.iurankomplek.network.model.NetworkError
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BaseRepositoryTest {
    
    private lateinit var mockCircuitBreaker: CircuitBreaker
    private lateinit var mockApiConfig: com.example.iurankomplek.network.ApiConfig
    private lateinit var testRepository: TestRepository
    
    @Before
    fun setup() {
        mockCircuitBreaker = mockk()
        mockApiConfig = mockk()
        testRepository = TestRepository(mockCircuitBreaker)
    }
    
    @Test
    fun `executeWithCircuitBreaker should return success when circuit breaker succeeds`() = runTest {
        val expectedResponse = Response.success("test data")
        val mockApiCall = mockk<suspend () -> retrofit2.Response<String>>()
        
        every {
            mockCircuitBreaker.execute(any<suspend () -> retrofit2.Response<String>>())
        } returns CircuitBreakerResult.Success(expectedResponse)
        
        every {
            mockApiCall.invoke()
        } returns expectedResponse
        
        val result = testRepository.callTestExecuteWithCircuitBreaker(mockApiCall)
        
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
    }
    
    @Test
    fun `executeWithCircuitBreaker should return failure when circuit breaker fails`() = runTest {
        val exception = RuntimeException("API call failed")
        val mockApiCall = mockk<suspend () -> retrofit2.Response<String>>()
        
        every {
            mockCircuitBreaker.execute(any<suspend () -> retrofit2.Response<String>>())
        } returns CircuitBreakerResult.Failure(exception)
        
        every {
            mockApiCall.invoke()
        } throws exception
        
        val result = testRepository.callTestExecuteWithCircuitBreaker(mockApiCall)
        
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
    
    @Test
    fun `executeWithCircuitBreaker should return failure when circuit breaker is open`() = runTest {
        val mockApiCall = mockk<suspend () -> retrofit2.Response<String>>()
        
        every {
            mockCircuitBreaker.execute(any<suspend () -> retrofit2.Response<String>>())
        } returns CircuitBreakerResult.CircuitOpen
        
        val result = testRepository.callTestExecuteWithCircuitBreaker(mockApiCall)
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is NetworkError.CircuitBreakerError)
    }
    
    @Test
    fun `executeWithCircuitBreaker should handle null responses correctly`() = runTest {
        val expectedResponse = Response.success<String?>(null)
        val mockApiCall = mockk<suspend () -> retrofit2.Response<String?>>()
        
        every {
            mockCircuitBreaker.execute(any<suspend () -> retrofit2.Response<String?>>())
        } returns CircuitBreakerResult.Success(expectedResponse)
        
        every {
            mockApiCall.invoke()
        } returns expectedResponse
        
        val result = testRepository.callTestExecuteWithCircuitBreakerNullable(mockApiCall)
        
        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
    }
    
    @Test
    fun `executeWithCircuitBreaker should preserve exception details on failure`() = runTest {
        val exception = IllegalStateException("Validation failed with detailed error message")
        val mockApiCall = mockk<suspend () -> retrofit2.Response<String>>()
        
        every {
            mockCircuitBreaker.execute(any<suspend () -> retrofit2.Response<String>>())
        } returns CircuitBreakerResult.Failure(exception)
        
        every {
            mockApiCall.invoke()
        } throws exception
        
        val result = testRepository.callTestExecuteWithCircuitBreaker(mockApiCall)
        
        assertTrue(result.isFailure)
        val actualException = result.exceptionOrNull()
        assertNotNull(actualException)
        assertEquals(exception, actualException)
        assertEquals("Validation failed with detailed error message", actualException.message)
    }
    
    @Test
    fun `circuitBreaker property should be accessible and non-null`() {
        assertNotNull(testRepository.testCircuitBreaker)
    }
    
    @Test
    fun `maxRetries property should be accessible and positive`() {
        val maxRetries = testRepository.testMaxRetries
        assertTrue(maxRetries > 0)
    }
    
    @Test
    fun `executeWithCircuitBreaker should handle network exceptions`() = runTest {
        val exception = java.net.UnknownHostException("Unknown host")
        val mockApiCall = mockk<suspend () -> retrofit2.Response<String>>()
        
        every {
            mockCircuitBreaker.execute(any<suspend () -> retrofit2.Response<String>>())
        } returns CircuitBreakerResult.Failure(exception)
        
        every {
            mockApiCall.invoke()
        } throws exception
        
        val result = testRepository.callTestExecuteWithCircuitBreaker(mockApiCall)
        
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
    
    @Test
    fun `executeWithCircuitBreaker should handle timeout exceptions`() = runTest {
        val exception = java.net.SocketTimeoutException("Connection timed out")
        val mockApiCall = mockk<suspend () -> retrofit2.Response<String>>()
        
        every {
            mockCircuitBreaker.execute(any<suspend () -> retrofit2.Response<String>>())
        } returns CircuitBreakerResult.Failure(exception)
        
        every {
            mockApiCall.invoke()
        } throws exception
        
        val result = testRepository.callTestExecuteWithCircuitBreaker(mockApiCall)
        
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
    
    @Test
    fun `BaseRepository should provide protected properties to subclasses`() {
        val circuitBreaker = testRepository.testCircuitBreaker
        val maxRetries = testRepository.testMaxRetries
        
        assertNotNull(circuitBreaker)
        assertTrue(maxRetries > 0)
    }
}

class TestRepository(circuitBreaker: CircuitBreaker) : BaseRepository() {
    
    private val customCircuitBreaker = circuitBreaker
    
    val testCircuitBreaker: CircuitBreaker
        get() = customCircuitBreaker
    
    val testMaxRetries: Int
        get() = maxRetries
    
    suspend fun callTestExecuteWithCircuitBreaker(
        apiCall: suspend () -> retrofit2.Response<String>
    ): Result<retrofit2.Response<String>> {
        return executeWithCircuitBreaker(apiCall)
    }
    
    suspend fun callTestExecuteWithCircuitBreakerNullable(
        apiCall: suspend () -> retrofit2.Response<String?>
    ): Result<retrofit2.Response<String?>> {
        @Suppress("UNCHECKED_CAST")
        return executeWithCircuitBreaker(apiCall as suspend () -> retrofit2.Response<String>) as Result<retrofit2.Response<String?>>
    }
}
