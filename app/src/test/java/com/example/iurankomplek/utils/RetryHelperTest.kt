package com.example.iurankomplek.utils

import com.example.iurankomplek.network.model.NetworkError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.*
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

@OptIn(ExperimentalCoroutinesApi::class)
class RetryHelperTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `executeWithRetry returns success on first successful API call`() = runTest {
        val mockApiCall = suspend {
            Response.success("Success response")
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success response", result)
    }

    @Test
    fun `executeWithRetry returns success after retrying on 408 Request Timeout`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                Response.error<String>(408, okhttp3.ResponseBody.create(null, "Timeout"))
            } else {
                Response.success("Success after retry")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after retry", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry returns success after retrying on 429 Too Many Requests`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                Response.error<String>(429, okhttp3.ResponseBody.create(null, "Rate limit"))
            } else {
                Response.success("Success after retry")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after retry", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry returns success after retrying on 500 Internal Server Error`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                Response.error<String>(500, okhttp3.ResponseBody.create(null, "Server error"))
            } else {
                Response.success("Success after retry")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after retry", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry returns success after retrying on 503 Service Unavailable`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                Response.error<String>(503, okhttp3.ResponseBody.create(null, "Service unavailable"))
            } else {
                Response.success("Success after retry")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after retry", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry returns success after retrying on SocketTimeoutException`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw SocketTimeoutException()
            } else {
                Response.success("Success after timeout")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after timeout", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry returns success after retrying on UnknownHostException`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw UnknownHostException()
            } else {
                Response.success("Success after connection error")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after connection error", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry returns success after retrying on SSLException`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw SSLException("SSL handshake failed")
            } else {
                Response.success("Success after SSL error")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after SSL error", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry does not retry on 400 Bad Request`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            Response.error<String>(400, okhttp3.ResponseBody.create(null, "Bad request"))
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall)
            fail("Expected exception for non-retryable 400 error")
        } catch (e: retrofit2.HttpException) {
            assertEquals(400, e.code())
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `executeWithRetry does not retry on 401 Unauthorized`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            Response.error<String>(401, okhttp3.ResponseBody.create(null, "Unauthorized"))
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall)
            fail("Expected exception for non-retryable 401 error")
        } catch (e: retrofit2.HttpException) {
            assertEquals(401, e.code())
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `executeWithRetry does not retry on 404 Not Found`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            Response.error<String>(404, okhttp3.ResponseBody.create(null, "Not found"))
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall)
            fail("Expected exception for non-retryable 404 error")
        } catch (e: retrofit2.HttpException) {
            assertEquals(404, e.code())
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `executeWithRetry throws exception when null response body is returned`() = runTest {
        val mockApiCall = suspend {
            Response.success<String>(null)
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall)
            fail("Expected exception for null response body")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Response body is null") == true)
        }
    }

    @Test
    fun `executeWithRetry retries up to max retries before giving up`() = runTest {
        val mockApiCall = suspend {
            Response.error<String>(503, okhttp3.ResponseBody.create(null, "Service unavailable"))
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall, maxRetries = 3)
            fail("Expected exception after max retries")
        } catch (e: retrofit2.HttpException) {
            assertEquals(503, e.code())
        }
    }

    @Test
    fun `executeWithRetry succeeds after max retries`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount < 3) {
                Response.error<String>(503, okhttp3.ResponseBody.create(null, "Service unavailable"))
            } else {
                Response.success("Success after max retries")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall, maxRetries = 3)

        assertEquals("Success after max retries", result)
        assertEquals(3, callCount)
    }

    @Test
    fun `executeWithRetry does not retry on generic Exception`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            throw IllegalArgumentException("Invalid argument")
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall)
            fail("Expected exception for non-retryable exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid argument", e.message)
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `executeWithRetry respects custom max retries`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount < 5) {
                Response.error<String>(503, okhttp3.ResponseBody.create(null, "Service unavailable"))
            } else {
                Response.success("Success")
            }
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall, maxRetries = 2)
            fail("Expected exception after custom max retries")
        } catch (e: retrofit2.HttpException) {
            assertEquals(503, e.code())
            assertEquals(3, callCount)
        }
    }

    @Test
    fun `executeWithRetry uses exponential backoff with jitter`() = runTest {
        var callCount = 0
        val delays = mutableListOf<Long>()
        val mockApiCall = suspend {
            callCount++
            if (callCount < 3) {
                Response.error<String>(503, okhttp3.ResponseBody.create(null, "Service unavailable"))
            } else {
                Response.success("Success")
            }
        }

        val startTime = System.currentTimeMillis()
        val result = RetryHelper.executeWithRetry(mockApiCall, maxRetries = 3)
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime

        assertEquals("Success", result)
        assertEquals(3, callCount)
        
        assertTrue("Total execution time should account for delays", totalTime > 1000)
    }

    @Test
    fun `executeWithRetry handles NetworkError TimeoutError correctly`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw NetworkError.TimeoutError("Request timeout")
            } else {
                Response.success("Success after timeout error")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after timeout error", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry handles NetworkError ConnectionError correctly`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw NetworkError.ConnectionError("Connection failed")
            } else {
                Response.success("Success after connection error")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after connection error", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry does not retry on NetworkError HttpError with 400`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            throw NetworkError.HttpError(400, "Bad request")
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall)
            fail("Expected exception for non-retryable HttpError")
        } catch (e: NetworkError.HttpError) {
            assertEquals(400, e.httpCode)
            assertEquals("Bad request", e.message)
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `executeWithRetry retries on NetworkError HttpError with 408`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw NetworkError.HttpError(408, "Request timeout")
            } else {
                Response.success("Success after 408 error")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after 408 error", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry retries on NetworkError HttpError with 429`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw NetworkError.HttpError(429, "Rate limit exceeded")
            } else {
                Response.success("Success after 429 error")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after 429 error", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry retries on NetworkError HttpError with 500`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw NetworkError.HttpError(500, "Internal server error")
            } else {
                Response.success("Success after 500 error")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after 500 error", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry retries on NetworkError HttpError with 502`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw NetworkError.HttpError(502, "Bad gateway")
            } else {
                Response.success("Success after 502 error")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after 502 error", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry retries on NetworkError HttpError with 503`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw NetworkError.HttpError(503, "Service unavailable")
            } else {
                Response.success("Success after 503 error")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after 503 error", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry retries on NetworkError HttpError with 504`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw NetworkError.HttpError(504, "Gateway timeout")
            } else {
                Response.success("Success after 504 error")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success after 504 error", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry handles empty data type correctly`() = runTest {
        val mockApiCall = suspend {
            Response.success("")
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("", result)
    }

    @Test
    fun `executeWithRetry handles zero numeric value correctly`() = runTest {
        val mockApiCall = suspend {
            Response.success(0)
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals(0, result)
    }

    @Test
    fun `executeWithRetry handles list data correctly`() = runTest {
        val mockApiCall = suspend {
            Response.success(listOf("item1", "item2", "item3"))
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals(listOf("item1", "item2", "item3"), result)
    }

    @Test
    fun `executeWithRetry does not retry on NetworkError ValidationError`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            throw NetworkError.ValidationError("Validation failed")
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall)
            fail("Expected exception for non-retryable ValidationError")
        } catch (e: NetworkError.ValidationError) {
            assertEquals("Validation failed", e.message)
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `executeWithRetry does not retry on NetworkError AuthenticationError`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            throw NetworkError.AuthenticationError("Authentication failed")
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall)
            fail("Expected exception for non-retryable AuthenticationError")
        } catch (e: NetworkError.AuthenticationError) {
            assertEquals("Authentication failed", e.message)
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `executeWithRetry does not retry on NetworkError NetworkUnavailableError`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            throw NetworkError.NetworkUnavailableError("Network unavailable")
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall)
            fail("Expected exception for non-retryable NetworkUnavailableError")
        } catch (e: NetworkError.NetworkUnavailableError) {
            assertEquals("Network unavailable", e.message)
            assertEquals(1, callCount)
        }
    }

    @Test
    fun `executeWithRetry handles mixed success and failure scenarios`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            when (callCount) {
                1 -> Response.error<String>(503, okhttp3.ResponseBody.create(null, "Error 1"))
                2 -> Response.error<String>(408, okhttp3.ResponseBody.create(null, "Error 2"))
                3 -> Response.success("Success after multiple retries")
                else -> throw IllegalStateException("Unexpected call count")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall, maxRetries = 3)

        assertEquals("Success after multiple retries", result)
        assertEquals(3, callCount)
    }

    @Test
    fun `executeWithRetry handles NetworkError with null message correctly`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            if (callCount == 1) {
                throw NetworkError.ConnectionError(null)
            } else {
                Response.success("Success")
            }
        }

        val result = RetryHelper.executeWithRetry(mockApiCall)

        assertEquals("Success", result)
        assertEquals(2, callCount)
    }

    @Test
    fun `executeWithRetry respects default max retries from Constants`() = runTest {
        var callCount = 0
        val mockApiCall = suspend {
            callCount++
            Response.error<String>(503, okhttp3.ResponseBody.create(null, "Service unavailable"))
        }

        try {
            RetryHelper.executeWithRetry(mockApiCall)
        } catch (e: retrofit2.HttpException) {
            assertEquals(503, e.code())
            assertTrue("Should use default max retries", callCount == Constants.Network.MAX_RETRIES + 1)
        }
    }
}
