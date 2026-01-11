package com.example.iurankomplek.network.interceptor

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CompressionInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private val jsonContentType = "application/json".toMediaType()
    private val plainTextContentType = "text/plain".toMediaType()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun compressionInterceptor_compressesLargeJsonRequest() {
        val jsonData = buildString {
            repeat(100) {
                append("""{"key":"value${it}","number":${it * 100},"description":"This is a test data item with some text content"}""")
                if (it < 99) append(",")
            }
        }
        val requestBody = jsonData.toRequestBody(jsonContentType)

        val compressionInterceptor = CompressionInterceptor(
            enableCompression = true,
            minSizeToCompress = 1024,
            enableLogging = true
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        assertTrue("Request should succeed", response.isSuccessful)

        val recordedRequest = mockWebServer.takeRequest()
        val contentEncoding = recordedRequest.getHeader("Content-Encoding")

        assertNotNull("Content-Encoding header should be present", contentEncoding)
        assertTrue("Content-Encoding should be gzip", contentEncoding!!.contains("gzip"))
    }

    @Test
    fun compressionInterceptor_skipsCompressionForSmallRequest() {
        val jsonData = """{"key":"value"}"""
        val requestBody = jsonData.toRequestBody(jsonContentType)

        val compressionInterceptor = CompressionInterceptor(
            enableCompression = true,
            minSizeToCompress = 1024,
            enableLogging = false
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        assertTrue("Request should succeed", response.isSuccessful)

        val recordedRequest = mockWebServer.takeRequest()
        val contentEncoding = recordedRequest.getHeader("Content-Encoding")

        assertNull("Content-Encoding header should not be present for small request", contentEncoding)
    }

    @Test
    fun compressionInterceptor_skipsNonCompressibleContent() {
        val imageData = ByteArray(2000) { it.toByte() }
        val requestBody = imageData.toRequestBody("application/octet-stream".toMediaType())

        val compressionInterceptor = CompressionInterceptor(
            enableCompression = true,
            minSizeToCompress = 1024,
            enableLogging = false
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        assertTrue("Request should succeed", response.isSuccessful)

        val recordedRequest = mockWebServer.takeRequest()
        val contentEncoding = recordedRequest.getHeader("Content-Encoding")

        assertNull("Content-Encoding header should not be present for non-compressible content", contentEncoding)
    }

    @Test
    fun compressionInterceptor_compressesTextContent() {
        val textData = buildString {
            repeat(100) {
                append("This is a line of text that will be compressed to save bandwidth\n")
            }
        }
        val requestBody = textData.toRequestBody(plainTextContentType)

        val compressionInterceptor = CompressionInterceptor(
            enableCompression = true,
            minSizeToCompress = 1024,
            enableLogging = false
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        assertTrue("Request should succeed", response.isSuccessful)

        val recordedRequest = mockWebServer.takeRequest()
        val contentEncoding = recordedRequest.getHeader("Content-Encoding")

        assertNotNull("Content-Encoding header should be present for text content", contentEncoding)
        assertTrue("Content-Encoding should be gzip", contentEncoding!!.contains("gzip"))
    }

    @Test
    fun compressionInterceptor_decompressesGzipResponse() {
        val responseData = """{"result":"success","data":"test data"}"""

        val compressedResponse = MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Encoding", "gzip")
            .setBody(compressData(responseData))

        mockWebServer.enqueue(compressedResponse)

        val compressionInterceptor = CompressionInterceptor(
            enableCompression = true,
            minSizeToCompress = 1024,
            enableLogging = false
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertTrue("Response should be successful", response.isSuccessful)

        val responseBody = response.body?.string()
        assertNotNull("Response body should not be null", responseBody)
        assertTrue("Response should be decompressed", responseBody!!.contains("success"))
    }

    @Test
    fun compressionInterceptor_passesThroughNonGzipResponse() {
        val responseData = """{"result":"success"}"""

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(responseData)

        mockWebServer.enqueue(mockResponse)

        val compressionInterceptor = CompressionInterceptor(
            enableCompression = true,
            minSizeToCompress = 1024,
            enableLogging = false
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertTrue("Response should be successful", response.isSuccessful)

        val responseBody = response.body?.string()
        assertNotNull("Response body should not be null", responseBody)
        assertTrue("Response should contain original data", responseBody!!.contains("success"))
    }

    @Test
    fun compressionInterceptor_disabled_skipsCompression() {
        val jsonData = buildString {
            repeat(100) {
                append("""{"key":"value${it}"}""")
                if (it < 99) append(",")
            }
        }
        val requestBody = jsonData.toRequestBody(jsonContentType)

        val compressionInterceptor = CompressionInterceptor(
            enableCompression = false,
            minSizeToCompress = 1024,
            enableLogging = false
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        assertTrue("Request should succeed", response.isSuccessful)

        val recordedRequest = mockWebServer.takeRequest()
        val contentEncoding = recordedRequest.getHeader("Content-Encoding")

        assertNull("Content-Encoding header should not be present when compression disabled", contentEncoding)
    }

    @Test
    fun compressionInterceptor_handlesGETRequests() {
        val compressionInterceptor = CompressionInterceptor(
            enableCompression = true,
            minSizeToCompress = 1024,
            enableLogging = false
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertTrue("GET request should succeed", response.isSuccessful)

        val recordedRequest = mockWebServer.takeRequest()
        val contentEncoding = recordedRequest.getHeader("Content-Encoding")

        assertNull("GET requests should not have Content-Encoding header", contentEncoding)
    }

    @Test
    fun compressionInterceptor_handlesRequestWithoutBody() {
        val compressionInterceptor = CompressionInterceptor(
            enableCompression = true,
            minSizeToCompress = 1024,
            enableLogging = false
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .head()
            .build()

        val response = client.newCall(request).execute()

        assertTrue("HEAD request should succeed", response.isSuccessful)
    }

    @Test
    fun compressionInterceptor_calculateCompressionRatio() {
        val interceptor = CompressionInterceptor(enableCompression = false, enableLogging = false)

        val testCases = listOf(
            Pair(1000L, 400L),
            Pair(10000L, 3000L),
            Pair(100000L, 25000L)
        )

        testCases.forEach { (original, compressed) ->
            val ratio = interceptor.calculateCompressionRatio(original, compressed)

            assertTrue("Compression ratio should be positive", ratio > 0)
            assertTrue("Compression ratio should be <= 100", ratio <= 100)
            assertTrue("Compression ratio should be reasonable", ratio < 100)
        }
    }

    @Test
    fun compressionInterceptor_zeroOriginalSize_returnsZeroRatio() {
        val interceptor = CompressionInterceptor(enableCompression = false, enableLogging = false)

        val ratio = interceptor.calculateCompressionRatio(0L, 100L)

        assertEquals("Compression ratio should be 0 for zero original size", 0.0, ratio, 0.01)
    }

    @Test
    fun compressionInterceptor_compressesXmlContent() {
        val xmlData = buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            append("<root>")
            repeat(100) { i ->
                append("<item id=\"$i\"><name>Item $i</name></item>")
            }
            append("</root>")
        }
        val requestBody = xmlData.toRequestBody("application/xml".toMediaType())

        val compressionInterceptor = CompressionInterceptor(
            enableCompression = true,
            minSizeToCompress = 1024,
            enableLogging = false
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        assertTrue("Request should succeed", response.isSuccessful)

        val recordedRequest = mockWebServer.takeRequest()
        val contentEncoding = recordedRequest.getHeader("Content-Encoding")

        assertNotNull("Content-Encoding header should be present for XML content", contentEncoding)
        assertTrue("Content-Encoding should be gzip", contentEncoding!!.contains("gzip"))
    }

    @Test
    fun compressionInterceptor_compressesUrlEncodedContent() {
        val formData = buildString {
            append("field1=value1&")
            repeat(100) { i ->
                append("field${i}=${"a".repeat(50)}&")
            }
        }
        val requestBody = formData.toRequestBody("application/x-www-form-urlencoded".toMediaType())

        val compressionInterceptor = CompressionInterceptor(
            enableCompression = true,
            minSizeToCompress = 1024,
            enableLogging = false
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(compressionInterceptor)
            .build()

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        assertTrue("Request should succeed", response.isSuccessful)

        val recordedRequest = mockWebServer.takeRequest()
        val contentEncoding = recordedRequest.getHeader("Content-Encoding")

        assertNotNull("Content-Encoding header should be present for URL-encoded content", contentEncoding)
        assertTrue("Content-Encoding should be gzip", contentEncoding!!.contains("gzip"))
    }

    @Test
    fun compressionInterceptor_createDefault_returnsProperConfiguration() {
        val defaultInterceptor = CompressionInterceptor.createDefault()

        assertNotNull("Default interceptor should not be null", defaultInterceptor)

        val client = OkHttpClient.Builder()
            .addInterceptor(defaultInterceptor)
            .build()

        assertTrue("Client should be created with default interceptor", client.interceptors.isNotEmpty())
    }

    private fun compressData(data: String): ByteArray {
        val buffer = Buffer()
        val gzipSink = okio.GzipSink(buffer)
        gzipSink.write(data.toByteArray())
        gzipSink.close()
        return buffer.readByteArray()
    }
}
