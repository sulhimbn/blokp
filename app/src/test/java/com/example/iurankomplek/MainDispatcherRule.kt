package com.example.iurankomplek

import com.example.iurankomplek.utils.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Replaces [Dispatchers.Main] with an [UnconfinedTestDispatcher] for the duration of a test.
 *
 * Required by every test that exercises code which hops to `Dispatchers.Main` (adapters using
 * DiffUtil, ViewModels using `viewModelScope`). Without this, `Dispatchers.Main` throws
 * "Module with the Main dispatcher had failed to initialize" on a plain JVM.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    val dispatcher = testDispatcher

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * Spins until [condition] holds, failing after [timeoutMs].
 *
 * Production adapters compute a DiffUtil diff on `Dispatchers.Default` and then apply it on
 * `Dispatchers.Main`, so the mutation lands on a background thread a moment after the call.
 * Polling keeps the assertions deterministic without weakening them.
 */
fun awaitCondition(
    description: String,
    timeoutMs: Long = 5_000,
    pollIntervalMs: Long = 10,
    condition: () -> Boolean
) {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
        if (condition()) return
        Thread.sleep(pollIntervalMs)
    }
    throw AssertionError("Timed out after ${timeoutMs}ms waiting for: $description")
}

/**
 * Clears the JVM-wide [com.example.iurankomplek.utils.CacheManager] singleton around each test.
 *
 * Every network repository serves `getCachedOrNetwork(...)` results for a 5-minute TTL. Without
 * this, the first successful fetch in a test class is replayed to every later test, so the API
 * mock is never called and retry/`verify` assertions fail for reasons unrelated to the code
 * under test.
 */
class CacheResetRule : TestWatcher() {
    override fun starting(description: Description) {
        CacheManager.getInstance().clearSync()
    }

    override fun finished(description: Description) {
        CacheManager.getInstance().clearSync()
    }
}