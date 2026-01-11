package com.example.iurankomplek.network.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

class RateLimiterInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var rateLimiter: RateLimiterInterceptor

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        rateLimiter = RateLimiterInterceptor(
            maxRequestsPerSecond = 2,
            maxRequestsPerMinute = 10,
            enableLogging = false
        )

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(rateLimiter)
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should allow requests within rate limit`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request1 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val request2 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        Thread.sleep(500)

        val response1 = okHttpClient.newCall(request1).execute()
        val response2 = okHttpClient.newCall(request2).execute()

        assertEquals(200, response1.code)
        assertEquals(200, response2.code)
    }

    @Test
    fun `should block requests exceeding rate per second limit`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request1 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val request2 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val request3 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val response1 = okHttpClient.newCall(request1).execute()
        Thread.sleep(100)
        val response2 = okHttpClient.newCall(request2).execute()
        Thread.sleep(100)
        val response3 = okHttpClient.newCall(request3).execute()

        assertEquals(200, response1.code)
        assertEquals(200, response2.code)
        
        val exception = response3.body?.string() ?: ""
        assertTrue("Request should be rate limited", exception.contains("Too many requests") || response3.code == 429)
    }

    @Test
    fun `should block requests exceeding rate per minute limit`() {
        for (i in 1..10) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        for (i in 1..10) {
            val request = Request.Builder()
                .url(mockWebServer.url("/api/users"))
                .build()

            val response = okHttpClient.newCall(request).execute()
            Thread.sleep(100)
            assertEquals(200, response.code)
        }

        val request11 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val response11 = okHttpClient.newCall(request11).execute()

        val exception = response11.body?.string() ?: ""
        assertTrue("Request should be rate limited", exception.contains("Too many requests") || response11.code == 429)
    }

    @Test
    fun `should track endpoint statistics correctly`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request1 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val request2 = Request.Builder()
            .url(mockWebServer.url("/api/data"))
            .build()

        okHttpClient.newCall(request1).execute()
        Thread.sleep(100)
        okHttpClient.newCall(request2).execute()

        val userStats = rateLimiter.getEndpointStats("GET:/api/users")
        val dataStats = rateLimiter.getEndpointStats("GET:/api/data")

        assertNotNull("User stats should exist", userStats)
        assertNotNull("Data stats should exist", dataStats)
        assertEquals(1, userStats?.getRequestCount())
        assertEquals(1, dataStats?.getRequestCount())
    }

    @Test
    fun `should reset rate limiter correctly`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        okHttpClient.newCall(request).execute()
        Thread.sleep(100)

        val statsBefore = rateLimiter.getEndpointStats("GET:/api/users")
        assertTrue("Should have request count > 0", statsBefore?.getRequestCount()!! > 0)

        rateLimiter.reset()

        val statsAfter = rateLimiter.getEndpointStats("GET:/api/users")
        assertEquals(0, statsAfter?.getRequestCount() ?: 0)
    }

    @Test
    fun `should differentiate between different endpoints`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val requestUsers = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val requestPemanfaatan = Request.Builder()
            .url(mockWebServer.url("/api/pemanfaatan"))
            .build()

        val requestUsers2 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        okHttpClient.newCall(requestUsers).execute()
        Thread.sleep(500)
        okHttpClient.newCall(requestPemanfaatan).execute()
        Thread.sleep(500)
        okHttpClient.newCall(requestUsers2).execute()

        val userStats = rateLimiter.getEndpointStats("GET:/api/users")
        val pemanfaatanStats = rateLimiter.getEndpointStats("GET:/api/pemanfaatan")

        assertEquals(2, userStats?.getRequestCount())
        assertEquals(1, pemanfaatanStats?.getRequestCount())
    }

    @Test
    fun `should handle concurrent requests safely`() {
        val threadCount = 10
        val requestsPerThread = 2

        for (i in 1..(threadCount * requestsPerThread)) {
            mockWebServer.enqueue(MockResponse().setResponseCode(200))
        }

        val threads = mutableListOf<Thread>()
        val successfulRequests = mutableListOf<Int>()

        for (t in 0 until threadCount) {
            val thread = Thread {
                for (r in 0 until requestsPerThread) {
                    try {
                        val request = Request.Builder()
                            .url(mockWebServer.url("/api/users"))
                            .build()

                        val response = okHttpClient.newCall(request).execute()
                        if (response.code == 200) {
                            synchronized(successfulRequests) {
                                successfulRequests.add(1)
                            }
                        }
                        Thread.sleep(100)
                    } catch (e: Exception) {
                        // Rate limit exceeded, expected for some requests
                    }
                }
            }
            threads.add(thread)
            thread.start()
        }

        threads.forEach { it.join() }

        assertTrue("Should have some successful requests", successfulRequests.size > 0)
        assertTrue("Should have rate limited some requests", successfulRequests.size < (threadCount * requestsPerThread))
    }

    @Test
    fun `should clean up old timestamps correctly`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        for (i in 1..8) {
            okHttpClient.newCall(request).execute()
            Thread.sleep(100)
        }

        val statsBefore = rateLimiter.getEndpointStats("GET:/api/users")
        assertTrue("Should have request count > 0", statsBefore?.getRequestCount()!! > 0)

        Thread.sleep(61000)

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request2 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val response = okHttpClient.newCall(request2).execute()
        assertEquals(200, response.code)
    }

    @Test
    fun `should respect min interval between requests`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request1 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val request2 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val request3 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val response1 = okHttpClient.newCall(request1).execute()
        Thread.sleep(50)
        val response2 = okHttpClient.newCall(request2).execute()
        Thread.sleep(50)
        val response3 = okHttpClient.newCall(request3).execute()

        assertEquals(200, response1.code)
        assertEquals(200, response2.code)
        
        val exception = response3.body?.string() ?: ""
        assertTrue("Request should be rate limited due to min interval", 
                   exception.contains("Too many requests") || response3.code == 429)
    }

    @Test
    fun `should return all stats correctly`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request1 = Request.Builder()
            .url(mockWebServer.url("/api/users"))
            .build()

        val request2 = Request.Builder()
            .url(mockWebServer.url("/api/data"))
            .build()

        okHttpClient.newCall(request1).execute()
        Thread.sleep(100)
        okHttpClient.newCall(request2).execute()

        val allStats = rateLimiter.getAllStats()

        assertTrue("Should have stats for multiple endpoints", allStats.size >= 2)
        assertTrue("Should have user stats", allStats.containsKey("GET:/api/users"))
        assertTrue("Should have data stats", allStats.containsKey("GET:/api/data"))
    }
}
