package com.example.iurankomplek.payment

import com.example.iurankomplek.utils.Constants
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class WebhookRetryCalculatorTest {

    private val calculator = WebhookRetryCalculator()

    @Test
    fun `calculateRetryDelay should use exponential backoff`() {
        val retryDelay0 = calculator.calculateRetryDelay(0)
        val retryDelay1 = calculator.calculateRetryDelay(1)
        val retryDelay2 = calculator.calculateRetryDelay(2)

        assertTrue(retryDelay1 > retryDelay0, "Delay should increase with retry count")
        assertTrue(retryDelay2 > retryDelay1, "Delay should increase with retry count")
        assertTrue(retryDelay0 >= 500, "Initial delay should be around 1000ms with jitter")
        assertTrue(retryDelay1 >= 1500, "Second delay should be around 2000ms with jitter")
    }

    @Test
    fun `calculateRetryDelay should cap at max retry delay`() {
        val retryDelay10 = calculator.calculateRetryDelay(10)
        val retryDelay100 = calculator.calculateRetryDelay(100)

        assertTrue(retryDelay10 <= 65000, "Delay should be capped at 60000ms + jitter")
        assertTrue(retryDelay100 <= 65000, "Delay should be capped at 60000ms + jitter")
    }

    @Test
    fun `calculateRetryDelay should produce consistent results`() {
        val results = mutableListOf<Long>()
        repeat(10) {
            results.add(calculator.calculateRetryDelay(0))
        }

        val uniqueResults = results.toSet()
        assertTrue(uniqueResults.size > 1, "Jitter should produce some variation")
        assertTrue(uniqueResults.size <= 10, "Results should be reasonable range")
    }

    @Test
    fun `calculateRetryDelay should be non-negative`() {
        val result = calculator.calculateRetryDelay(1000)
        assertTrue(result >= 0, "Delay should never be negative")
    }

    @Test
    fun `calculateRetryDelay with zero retries`() {
        val delay = calculator.calculateRetryDelay(0)
        val expectedBase = Constants.Webhook.INITIAL_RETRY_DELAY_MS.toLong()
        val jitterRange = Constants.Webhook.RETRY_JITTER_MS.toLong()
        
        assertTrue(
            delay >= expectedBase - jitterRange && delay <= expectedBase + jitterRange,
            "Initial delay should be within expected range with jitter"
        )
    }
}
