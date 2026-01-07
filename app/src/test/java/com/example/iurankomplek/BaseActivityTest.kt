package com.example.iurankomplek

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.network.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class BaseActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var activity: TestBaseActivity
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiService = ApiConfig.getApiService(mockWebServer.url("/").toString())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockWebServer.shutdown()
    }

    @Test
    fun `executeWithRetry should call onSuccess on successful response`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var successCalled = false

        activity.executeWithRetry(
            operation = { Response.success("Success") },
            onSuccess = { 
                successCalled = true
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertTrue(successCalled)
    }

    @Test
    fun `executeWithRetry should call onError when response body is null`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var errorCalled = false
        var errorMessage = ""

        activity.executeWithRetry(
            operation = { Response.success(null) },
            onSuccess = { fail("onSuccess should not be called") },
            onError = { 
                errorCalled = true
                errorMessage = it
                latch.countDown()
            }
        )

        advanceUntilIdle()
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertTrue(errorCalled)
        assertTrue(errorMessage.contains("Invalid"))
    }

    @Test
    fun `executeWithRetry should retry on 408 Request Timeout error`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var successCalled = false
        var retryCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                retryCount++
                if (retryCount < 2) {
                    Response.error(408, okhttp3.ResponseBody.create(null, "Timeout"))
                } else {
                    Response.success("Success")
                }
            },
            onSuccess = { 
                successCalled = true
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(successCalled)
        assertEquals(2, retryCount)
    }

    @Test
    fun `executeWithRetry should retry on 429 Too Many Requests error`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var successCalled = false
        var retryCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                retryCount++
                if (retryCount < 2) {
                    Response.error(429, okhttp3.ResponseBody.create(null, "Too Many Requests"))
                } else {
                    Response.success("Success")
                }
            },
            onSuccess = { 
                successCalled = true
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(successCalled)
        assertEquals(2, retryCount)
    }

    @Test
    fun `executeWithRetry should retry on 500 Internal Server Error`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var successCalled = false
        var retryCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                retryCount++
                if (retryCount < 2) {
                    Response.error(500, okhttp3.ResponseBody.create(null, "Internal Server Error"))
                } else {
                    Response.success("Success")
                }
            },
            onSuccess = { 
                successCalled = true
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(successCalled)
        assertEquals(2, retryCount)
    }

    @Test
    fun `executeWithRetry should retry on 503 Service Unavailable`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var successCalled = false
        var retryCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                retryCount++
                if (retryCount < 2) {
                    Response.error(503, okhttp3.ResponseBody.create(null, "Service Unavailable"))
                } else {
                    Response.success("Success")
                }
            },
            onSuccess = { 
                successCalled = true
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(successCalled)
        assertEquals(2, retryCount)
    }

    @Test
    fun `executeWithRetry should not retry on 400 Bad Request error`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var errorCalled = false
        var callCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                callCount++
                Response.error(400, okhttp3.ResponseBody.create(null, "Bad Request"))
            },
            onSuccess = { fail("onSuccess should not be called") },
            onError = { 
                errorCalled = true
                latch.countDown()
            }
        )

        advanceUntilIdle()
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertTrue(errorCalled)
        assertEquals(1, callCount)
    }

    @Test
    fun `executeWithRetry should not retry on 404 Not Found error`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var errorCalled = false
        var callCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                callCount++
                Response.error(404, okhttp3.ResponseBody.create(null, "Not Found"))
            },
            onSuccess = { fail("onSuccess should not be called") },
            onError = { 
                errorCalled = true
                latch.countDown()
            }
        )

        advanceUntilIdle()
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertTrue(errorCalled)
        assertEquals(1, callCount)
    }

    @Test
    fun `executeWithRetry should retry on SocketTimeoutException`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var successCalled = false
        var retryCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                retryCount++
                if (retryCount < 2) {
                    throw SocketTimeoutException()
                } else {
                    Response.success("Success")
                }
            },
            onSuccess = { 
                successCalled = true
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(successCalled)
        assertEquals(2, retryCount)
    }

    @Test
    fun `executeWithRetry should retry on UnknownHostException`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var successCalled = false
        var retryCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                retryCount++
                if (retryCount < 2) {
                    throw UnknownHostException()
                } else {
                    Response.success("Success")
                }
            },
            onSuccess = { 
                successCalled = true
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(successCalled)
        assertEquals(2, retryCount)
    }

    @Test
    fun `executeWithRetry should retry on SSLException`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var successCalled = false
        var retryCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                retryCount++
                if (retryCount < 2) {
                    throw SSLException("SSL Error")
                } else {
                    Response.success("Success")
                }
            },
            onSuccess = { 
                successCalled = true
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(successCalled)
        assertEquals(2, retryCount)
    }

    @Test
    fun `executeWithRetry should not retry on non-retryable exception`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var errorCalled = false
        var callCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                callCount++
                throw IllegalArgumentException("Invalid argument")
            },
            onSuccess = { fail("onSuccess should not be called") },
            onError = { 
                errorCalled = true
                latch.countDown()
            }
        )

        advanceUntilIdle()
        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertTrue(errorCalled)
        assertEquals(1, callCount)
    }

    @Test
    fun `executeWithRetry should call onError after max retries exhausted`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var errorCalled = false
        var callCount = 0

        activity.executeWithRetry(
            maxRetries = 2,
            operation = {
                callCount++
                throw SocketTimeoutException()
            },
            onSuccess = { fail("onSuccess should not be called") },
            onError = { 
                errorCalled = true
                latch.countDown()
            }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(errorCalled)
        assertEquals(3, callCount)
    }

    @Test
    fun `executeWithRetry should respect custom maxRetries parameter`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var callCount = 0

        activity.executeWithRetry(
            maxRetries = 5,
            operation = {
                callCount++
                throw SocketTimeoutException()
            },
            onSuccess = { fail("onSuccess should not be called") },
            onError = { 
                latch.countDown()
            }
        )

        advanceUntilIdle()
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        assertEquals(6, callCount)
    }

    @Test
    fun `executeWithRetry should use exponential backoff with jitter`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        val callTimes = mutableListOf<Long>()
        var callCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            initialDelayMs = 100,
            operation = {
                callTimes.add(System.currentTimeMillis())
                callCount++
                if (callCount < 2) {
                    throw SocketTimeoutException()
                } else {
                    Response.success("Success")
                }
            },
            onSuccess = { 
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(2, callTimes.size)
        val delay = callTimes[1] - callTimes[0]
        assertTrue(delay >= 100)
    }

    @Test
    fun `executeWithRetry should respect maxDelayMs parameter`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var callCount = 0

        activity.executeWithRetry(
            maxRetries = 10,
            initialDelayMs = 1000,
            maxDelayMs = 200,
            operation = {
                callCount++
                if (callCount < 3) {
                    throw SocketTimeoutException()
                } else {
                    Response.success("Success")
                }
            },
            onSuccess = { 
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertEquals(3, callCount)
    }

    @Test
    fun `executeWithRetry should handle mixed retry scenarios with eventual success`() = runTest {
        activity = TestBaseActivity()
        val latch = CountDownLatch(1)
        var successCalled = false
        var retryCount = 0

        activity.executeWithRetry(
            maxRetries = 3,
            operation = {
                retryCount++
                when (retryCount) {
                    1 -> throw SocketTimeoutException()
                    2 -> Response.error(500, okhttp3.ResponseBody.create(null, "Error"))
                    else -> Response.success("Success")
                }
            },
            onSuccess = { 
                successCalled = true
                latch.countDown()
            },
            onError = { fail("onError should not be called") }
        )

        advanceUntilIdle()
        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertTrue(successCalled)
        assertEquals(3, retryCount)
    }
}

class TestBaseActivity : BaseActivity()
