package com.example.iurankomplek.network.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class IdempotencyInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(IdempotencyInterceptor())
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `POST request should include Idempotency-Key header`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"success\":true}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/messages")}")
            .post(okhttp3.RequestBody.create(null, "{\"message\":\"test\"}"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        Assert.assertEquals(200, response.code)
        val recordedRequest = mockWebServer.takeRequest()
        val idempotencyKey = recordedRequest.getHeader("X-Idempotency-Key")

        Assert.assertNotNull("Idempotency-Key header should be present", idempotencyKey)
        Assert.assertTrue("Idempotency-Key should start with idk_", idempotencyKey!!.startsWith("idk_"))
    }

    @Test
    fun `PUT request should include Idempotency-Key header`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"success\":true}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/vendors/123")}")
            .put(okhttp3.RequestBody.create(null, "{\"name\":\"test\"}"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        Assert.assertEquals(200, response.code)
        val recordedRequest = mockWebServer.takeRequest()
        val idempotencyKey = recordedRequest.getHeader("X-Idempotency-Key")

        Assert.assertNotNull("Idempotency-Key header should be present", idempotencyKey)
        Assert.assertTrue("Idempotency-Key should start with idk_", idempotencyKey!!.startsWith("idk_"))
    }

    @Test
    fun `DELETE request should include Idempotency-Key header`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"success\":true}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/messages/123")}")
            .delete(okhttp3.RequestBody.create(null, ""))
            .build()

        val response = okHttpClient.newCall(request).execute()

        Assert.assertEquals(200, response.code)
        val recordedRequest = mockWebServer.takeRequest()
        val idempotencyKey = recordedRequest.getHeader("X-Idempotency-Key")

        Assert.assertNotNull("Idempotency-Key header should be present", idempotencyKey)
        Assert.assertTrue("Idempotency-Key should start with idk_", idempotencyKey!!.startsWith("idk_"))
    }

    @Test
    fun `PATCH request should include Idempotency-Key header`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"success\":true}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/payments/123")}")
            .patch(okhttp3.RequestBody.create(null, "{\"status\":\"PAID\"}"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        Assert.assertEquals(200, response.code)
        val recordedRequest = mockWebServer.takeRequest()
        val idempotencyKey = recordedRequest.getHeader("X-Idempotency-Key")

        Assert.assertNotNull("Idempotency-Key header should be present", idempotencyKey)
        Assert.assertTrue("Idempotency-Key should start with idk_", idempotencyKey!!.startsWith("idk_"))
    }

    @Test
    fun `GET request should NOT include Idempotency-Key header`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"users\":[]}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/users")}")
            .get()
            .build()

        val response = okHttpClient.newCall(request).execute()

        Assert.assertEquals(200, response.code)
        val recordedRequest = mockWebServer.takeRequest()
        val idempotencyKey = recordedRequest.getHeader("X-Idempotency-Key")

        Assert.assertNull("Idempotency-Key header should NOT be present for GET requests", idempotencyKey)
    }

    @Test
    fun `Idempotency-Key should be unique per request`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"success\":true}"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"success\":true}"))
        mockWebServer.start()

        val request1 = Request.Builder()
            .url("${mockWebServer.url("/api/v1/messages")}")
            .post(okhttp3.RequestBody.create(null, "{\"message\":\"test1\"}"))
            .build()

        val request2 = Request.Builder()
            .url("${mockWebServer.url("/api/v1/messages")}")
            .post(okhttp3.RequestBody.create(null, "{\"message\":\"test2\"}"))
            .build()

        val response1 = okHttpClient.newCall(request1).execute()
        val response2 = okHttpClient.newCall(request2).execute()

        Assert.assertEquals(200, response1.code)
        Assert.assertEquals(200, response2.code)

        val recordedRequest1 = mockWebServer.takeRequest()
        val recordedRequest2 = mockWebServer.takeRequest()
        val idempotencyKey1 = recordedRequest1.getHeader("X-Idempotency-Key")
        val idempotencyKey2 = recordedRequest2.getHeader("X-Idempotency-Key")

        Assert.assertNotNull("First request should have Idempotency-Key", idempotencyKey1)
        Assert.assertNotNull("Second request should have Idempotency-Key", idempotencyKey2)
        Assert.assertNotEquals("Idempotency keys should be unique", idempotencyKey1, idempotencyKey2)
    }

    @Test
    fun `Idempotency-Key should contain timestamp`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"success\":true}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/messages")}")
            .post(okhttp3.RequestBody.create(null, "{\"message\":\"test\"}"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        Assert.assertEquals(200, response.code)
        val recordedRequest = mockWebServer.takeRequest()
        val idempotencyKey = recordedRequest.getHeader("X-Idempotency-Key")

        Assert.assertNotNull("Idempotency-Key header should be present", idempotencyKey)
        val parts = idempotencyKey!!.split("_")
        Assert.assertTrue("Idempotency-Key should have 3 parts", parts.size == 3)
        Assert.assertEquals("First part should be 'idk'", "idk", parts[0])
        val timestamp = kotlin.math.abs(parts[1].toLongOrNull())
        Assert.assertTrue("Second part should be timestamp", timestamp != null)
    }

    @Test
    fun `Idempotency-Key should contain random number`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{\"success\":true}"))
        mockWebServer.start()

        val request = Request.Builder()
            .url("${mockWebServer.url("/api/v1/messages")}")
            .post(okhttp3.RequestBody.create(null, "{\"message\":\"test\"}"))
            .build()

        val response = okHttpClient.newCall(request).execute()

        Assert.assertEquals(200, response.code)
        val recordedRequest = mockWebServer.takeRequest()
        val idempotencyKey = recordedRequest.getHeader("X-Idempotency-Key")

        Assert.assertNotNull("Idempotency-Key header should be present", idempotencyKey)
        val parts = idempotencyKey!!.split("_")
        Assert.assertTrue("Idempotency-Key should have 3 parts", parts.size == 3)
        val random = kotlin.math.abs(parts[2].toIntOrNull())
        Assert.assertTrue("Third part should be random number", random != null)
    }

    @Test
    fun `generate should create unique keys`() {
        val key1 = IdempotencyKeyGenerator.generate()
        val key2 = IdempotencyKeyGenerator.generate()

        Assert.assertNotEquals("Generated keys should be unique", key1, key2)
        Assert.assertTrue("Keys should start with idk_", key1.startsWith("idk_"))
        Assert.assertTrue("Keys should start with idk_", key2.startsWith("idk_"))
    }

    @Test
    fun `generate should contain timestamp`() {
        val key = IdempotencyKeyGenerator.generate()
        val parts = key.split("_")

        Assert.assertTrue("Key should have 3 parts", parts.size == 3)
        Assert.assertEquals("First part should be 'idk'", "idk", parts[0])
        val timestamp = kotlin.math.abs(parts[1].toLongOrNull())
        Assert.assertTrue("Second part should be timestamp", timestamp != null)
        Assert.assertTrue("Timestamp should be close to current time", kotlin.math.abs((timestamp!! - System.currentTimeMillis()) / 1000) < 2)
    }

    @Test
    fun `generate should contain random number`() {
        val key = IdempotencyKeyGenerator.generate()
        val parts = key.split("_")

        Assert.assertTrue("Key should have 3 parts", parts.size == 3)
        val random = kotlin.math.abs(parts[2].toIntOrNull())
        Assert.assertNotNull("Third part should be number", random)
        Assert.assertTrue("Random should be within int range", kotlin.math.abs(random!!) < Int.MAX_VALUE)
    }
}
