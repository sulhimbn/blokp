package com.example.iurankomplek

import com.example.iurankomplek.data.repository.BaseNetworkRepository
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.ErrorHandler
import com.example.iurankomplek.utils.Result
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.BaseViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import retrofit2.HttpException
import kotlin.math.min

class FoundationInfrastructureTest {

    private class ProbeRepository : BaseNetworkRepository() {
        override val errorHandler = ErrorHandler()
        fun retryable(code: Int) = isRetryableError(code)
        fun retryable(t: Throwable) = isRetryableException(t)
        fun delayFor(retry: Int) =
            calculateDelay(retry, Constants.Network.INITIAL_RETRY_DELAY_MS, Constants.Network.MAX_RETRY_DELAY_MS)
    }

    private class ProbeViewModel : BaseViewModel<String>() {
        fun route(result: Result<String>, onSuccess: (String) -> Unit) =
            handleResult(result, onSuccess = onSuccess)

        fun succeed(data: String) = setSuccess(data)
        fun fail(message: String) = setError(message)
        fun beginLoading() = setLoading()
    }

    @Test
    fun `only 408 and 429 are retryable among 4xx responses`() {
        val repo = ProbeRepository()

        assertTrue(repo.retryable(408))
        assertTrue(repo.retryable(429))
        for (code in listOf(400, 401, 403, 404, 409, 410, 418, 422, 426, 428)) {
            assertFalse("HTTP $code is permanent and must not be retried", repo.retryable(code))
        }
    }

    @Test
    fun `BaseViewModel is abstract`() {
        assertTrue(
            "BaseViewModel should be an abstract class",
            Modifier.isAbstract(BaseViewModel::class.java.modifiers)
        )
    }

    @Test
    fun `5xx responses are treated as retryable`() {
        val repo = ProbeRepository()

        assertTrue(repo.retryable(500))
        assertTrue(repo.retryable(502))
        assertTrue(repo.retryable(503))
    }

    @Test
    fun `408 and 429 are treated as retryable`() {
        val repo = ProbeRepository()

        assertTrue(repo.retryable(408))
        assertTrue(repo.retryable(429))
    }

    @Test
    fun `4xx responses other than 408 and 429 are not retryable`() {
        val repo = ProbeRepository()

        assertFalse(repo.retryable(400))
        assertFalse(repo.retryable(401))
        assertFalse(repo.retryable(404))
        assertFalse(repo.retryable(422))
    }

    @Test
    fun `transient IO and TLS failures are retryable but programming errors are not`() {
        val repo = ProbeRepository()

        assertTrue(repo.retryable(UnknownHostException("dns")))
        assertTrue(repo.retryable(SocketTimeoutException("slow")))
        assertTrue(repo.retryable(SSLException("handshake")))
        assertFalse(repo.retryable(IllegalStateException("bug")))
    }

    @Test
    fun `backoff grows exponentially and is capped at the configured maximum`() {
        val repo = ProbeRepository()
        val initial = Constants.Network.INITIAL_RETRY_DELAY_MS
        val max = Constants.Network.MAX_RETRY_DELAY_MS

        val first = repo.delayFor(1)
        val second = repo.delayFor(2)
        val third = repo.delayFor(3)

        assertTrue("first backoff should be around ${initial}ms but was ${first}ms", first in initial..(initial * 2))
        assertTrue("backoff must grow: $second should exceed $first", second > first)
        assertTrue("backoff must grow: $third should exceed $second", third > second)
        assertTrue("backoff must never exceed ${max}ms but was ${third}ms", third <= max)
        assertEquals(minOf(third, max), third)
    }

    @Test
    fun `backoff for a very large retry number is still capped`() {
        val repo = ProbeRepository()

        assertTrue(
            repo.delayFor(64) <= Constants.Network.MAX_RETRY_DELAY_MS
        )
    }

    @Test
    fun `UiState companion helpers build the matching states`() {
        val success: UiState<String> = UiState.success("payload")
        val error: UiState<String> = UiState.error("boom")
        val loading: UiState<String> = UiState.loading()

        assertEquals(UiState.Success("payload"), success)
        assertEquals(UiState.Error("boom"), error)
        assertEquals(UiState.Loading, loading)
    }

    @Test
    fun `handleResult forwards a success payload to the caller`() = runTest {
        val vm = ProbeViewModel()
        var captured: String? = null

        vm.route(Result.Success("ok")) { captured = it }

        assertEquals("ok", captured)
    }

    @Test
    fun `handleResult writes Error state for a failure`() = runTest {
        val vm = ProbeViewModel()

        vm.route(Result.Error(RuntimeException("bad"), "bad")) { }

        assertEquals(UiState.Error("bad"), vm.uiState.value)
    }

    @Test
    fun `handleResult writes Loading state for the Loading variant`() = runTest {
        val vm = ProbeViewModel()

        vm.route(Result.Loading) { }

        assertEquals(UiState.Loading, vm.uiState.value)
    }

    @Test
    fun `handleResult reports Empty as no data available`() = runTest {
        val vm = ProbeViewModel()

        vm.route(Result.Empty) { }

        assertEquals(UiState.Error("No data available"), vm.uiState.value)
    }

    @Test
    fun `setSuccess setError and setLoading drive the exposed state`() = runTest {
        val vm = ProbeViewModel()

        vm.beginLoading()
        assertEquals(UiState.Loading, vm.uiState.value)

        vm.succeed("payload")
        assertEquals(UiState.Success("payload"), vm.uiState.value)

        vm.fail("nope")
        assertEquals(UiState.Error("nope"), vm.uiState.value)
    }

    @Test
    fun `ErrorHandler maps transport and HTTP failures to user facing text`() {
        val handler = ErrorHandler()

        assertEquals("No internet connection", handler.handleError(UnknownHostException("dns")))
        assertEquals("Connection timeout", handler.handleError(SocketTimeoutException("slow")))
        assertEquals(
            "Resource not found",
            handler.handleError(HttpException(retrofit2.Response.error<Any>(404, EMPTY_BODY)))
        )
        assertEquals(
            "Server error",
            handler.handleError(HttpException(retrofit2.Response.error<Any>(500, EMPTY_BODY)))
        )
        assertEquals("An error occurred: mystery", handler.handleError(RuntimeException("mystery")))
    }

    private companion object {
        val EMPTY_BODY = okhttp3.ResponseBody.create(null, "")
    }
}
