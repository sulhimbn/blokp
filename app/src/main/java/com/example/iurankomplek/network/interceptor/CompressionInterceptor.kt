package com.example.iurankomplek.network.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.GzipSource
import okio.buffer
import java.io.IOException
import java.util.zip.GZIPOutputStream

class CompressionInterceptor(
    private val enableCompression: Boolean = true,
    private val minSizeToCompress: Int = 1024,
    private val enableLogging: Boolean = false
) : Interceptor {

    private val tag = "CompressionInterceptor"
    private val gzipEncoding = "gzip"
    private val contentType = "Content-Type"
    private val acceptEncoding = "Accept-Encoding"
    private val contentEncoding = "Content-Encoding"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (!enableCompression) {
            return chain.proceed(originalRequest)
        }

        var request = originalRequest

        originalRequest.body?.let { body ->
            if (isCompressible(body) && shouldCompress(body)) {
                val compressedBody = compress(body)
                request = originalRequest.newBuilder()
                    .method(originalRequest.method, compressedBody)
                    .header(contentEncoding, gzipEncoding)
                    .build()

                if (enableLogging) {
                    android.util.Log.d(tag, buildString {
                        append("Request compressed\n")
                        append("Original size: ${body.contentLength()} bytes\n")
                        append("Compressed size: ${compressedBody.contentLength()} bytes\n")
                        append("Compression ratio: ${calculateCompressionRatio(body.contentLength(), compressedBody.contentLength())}%")
                    })
                }
            }
        }

        val response = chain.proceed(request)

        response.body?.let { responseBody ->
            val encoding = response.header(contentEncoding)
            if (encoding != null && encoding.contains(gzipEncoding)) {
                if (enableLogging) {
                    android.util.Log.d(tag, buildString {
                        append("Response decompressed\n")
                        append("Compressed size: ${responseBody.contentLength()} bytes\n")
                    })
                }

                val decompressedBody = decompress(responseBody)
                return response.newBuilder()
                    .body(decompressedBody)
                    .removeHeader(contentEncoding)
                    .build()
            }
        }

        return response
    }

    private fun isCompressible(body: RequestBody): Boolean {
        val mediaType = body.contentType() ?: return false
        val type = mediaType.type ?: return false
        val subType = mediaType.subtype ?: return false

        return type == "text" ||
                subType == "json" ||
                subType == "xml" ||
                subType == "javascript" ||
                subType == "x-www-form-urlencoded"
    }

    private fun shouldCompress(body: RequestBody): Boolean {
        val contentLength = body.contentLength()
        return contentLength > 0 && contentLength >= minSizeToCompress
    }

    private fun compress(body: RequestBody): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? = body.contentType()

            override fun writeTo(sink: okio.BufferedSink) {
                val gzipSink = okio.GzipSink(sink).buffer()
                body.writeTo(gzipSink)
                gzipSink.close()
            }

            override fun contentLength(): Long {
                val buffer = Buffer()
                body.writeTo(buffer)
                val gzippedBuffer = Buffer()
                val gzipOutputStream = GZIPOutputStream(gzippedBuffer.outputStream())
                buffer.writeTo(gzipOutputStream)
                gzipOutputStream.close()
                return gzippedBuffer.size
            }
        }
    }

    private fun decompress(body: ResponseBody): ResponseBody {
        return ResponseBody.create(
            body.contentType(),
            body.source().use { source ->
                val gzipSource = GzipSource(source)
                gzipSource.buffer().readByteString()
            }
        )
    }

    private fun calculateCompressionRatio(original: Long, compressed: Long): Double {
        if (original == 0L) return 0.0
        val ratio = ((original - compressed).toDouble() / original.toDouble()) * 100.0
        return String.format("%.2f", ratio).toDouble()
    }

    companion object {
        const val DEFAULT_MIN_SIZE_TO_COMPRESS = 1024

        fun createDefault(): CompressionInterceptor {
            return CompressionInterceptor(
                enableCompression = true,
                minSizeToCompress = DEFAULT_MIN_SIZE_TO_COMPRESS,
                enableLogging = false
            )
        }
    }
}
