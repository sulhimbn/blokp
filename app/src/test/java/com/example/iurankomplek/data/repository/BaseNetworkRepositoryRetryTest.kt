package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.utils.ErrorHandler
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class BaseNetworkRepositoryRetryTest {

    private class TestRepository : BaseNetworkRepository() {
        override val errorHandler = ErrorHandler()

        var attempts = 0

        suspend fun succeed(value: String, timesToThrow: Int = 0): Result<String> =
            executeWithRetry(
                operation = {
                    attempts++
                    if (attempts <= timesToThrow) {
                        throw SocketTimeoutException("attempt $attempts")
                    }
                    Response.success(DataItemPayload(value))
                },
                transform = { it.value }
            )

        suspend fun failWithHttp(code: Int, timesToSucceed: Int): Result<String> =
            executeWithRetry(
                operation = {
                    attempts++
                    if (attempts <= timesToSucceed) {
                        Response.error(code, "err".toResponseBody("text/plain".toMediaType()))
                    } else {
                        Response.success(DataItemPayload("ok"))
                    }
                },
                transform = { it.value }
            )

        suspend fun throwAlways(throwable: Throwable): Result<String> =
            executeWithRetry<String, String>(
                operation = { attempts++; throw throwable },
                transform = { it }
            )

        suspend fun nullBody(): Result<String> =
            executeWithRetry<String?, String>(
                operation = { Response.success<String?>(null) },
                transform = { it ?: "" }
            )

        fun isRetryable(code: Int) = isRetryableError(code)
        fun isRetryable(t: Throwable) = isRetryableException(t)
        fun backoff(retry: Int) = calculateDelay(retry, 1000L, 30_000L)
    }

    private data class DataItemPayload(val value: String)

    private fun item(total: Int) = DataItem(
        first_name = "A",
        last_name = "B",
        email = "a@b.c",
        alamat = "x",
        iuran_perwarga = total,
        total_iuran_rekap = 0,
        jumlah_iuran_bulanan = 0,
        total_iuran_individu = 0,
        pengeluaran_iuran_warga = 0,
        pemanfaatan_iuran = null,
        avatar = null
    )

    @Test
    fun `succeeds on first attempt without retrying`() = runTest {
        val repo = TestRepository()
        val result = repo.succeed("ok")
        assertTrue(result.isSuccess)
        assertEquals("ok", result.getOrNull())
        assertEquals(1, repo.attempts)
    }

    @Test
    fun `retries transient failures then succeeds`() = runTest {
        val repo = TestRepository()
        val result = repo.succeed("ok", timesToThrow = 2)
        assertTrue(result.isSuccess)
        assertEquals(3, repo.attempts)
    }

    @Test
    fun `gives up after max retries`() = runTest {
        val repo = TestRepository()
        val result = repo.succeed("ok", timesToThrow = Int.MAX_VALUE)
        assertTrue(result.isFailure)
        assertEquals(4, repo.attempts)
    }

    @Test
    fun `retries retryable http codes then succeeds`() = runTest {
        listOf(408, 429, 500, 502, 503).forEach { code ->
            val repo = TestRepository()
            val result = repo.failWithHttp(code, timesToSucceed = 1)
            assertTrue("code $code should retry", result.isSuccess)
            assertEquals(2, repo.attempts)
        }
    }

    @Test
    fun `does not retry non retryable http codes`() = runTest {
        listOf(400, 401, 403, 404, 409, 410, 422).forEach { code ->
            val repo = TestRepository()
            val result = repo.failWithHttp(code, timesToSucceed = Int.MAX_VALUE)
            assertTrue("code $code should fail", result.isFailure)
            assertEquals("code $code should not be retried", 1, repo.attempts)
        }
    }

    @Test
    fun `does not retry non retryable exceptions`() = runTest {
        val repo = TestRepository()
        val result = repo.throwAlways(java.io.IOException("File not found"))
        assertTrue(result.isFailure)
        assertEquals(1, repo.attempts)
    }

    @Test
    fun `classifies retryability of exceptions`() {
        val repo = TestRepository()
        assertTrue(repo.isRetryable(SocketTimeoutException()))
        assertTrue(repo.isRetryable(UnknownHostException()))
        assertTrue(repo.isRetryable(SSLException("bad")))
        assertFalse(repo.isRetryable(java.io.IOException("other")))
        assertFalse(repo.isRetryable(IllegalStateException()))
    }

    @Test
    fun `classifies retryability of http codes`() {
        val repo = TestRepository()
        assertTrue(repo.isRetryable(408))
        assertTrue(repo.isRetryable(429))
        assertTrue(repo.isRetryable(500))
        assertFalse(repo.isRetryable(400))
        assertFalse(repo.isRetryable(404))
        assertFalse(repo.isRetryable(200))
    }

    @Test
    fun `surfaces null response body as failure`() = runTest {
        val repo = TestRepository()
        val result = repo.nullBody()
        assertTrue(result.isFailure)
    }

    @Test
    fun `backoff grows exponentially and stays within cap`() {
        val repo = TestRepository()
        val first = repo.backoff(1)
        val fourth = repo.backoff(4)
        val capped = repo.backoff(20)
        assertTrue("first backoff should be ~1000ms, was $first", first in 1000..1999)
        assertTrue("fourth backoff should exceed first, was $fourth vs $first", fourth > first)
        assertTrue("backoff must respect the 30s cap, was $capped", capped <= 30_000L)
    }

    @Test
    fun `validates financial data before calculating`() {
        val repo = TestRepository()
        assertTrue(repo.isRetryable(200) == false)
        assertTrue(com.example.iurankomplek.utils.FinancialCalculator.validateDataItem(item(100)))
    }
}