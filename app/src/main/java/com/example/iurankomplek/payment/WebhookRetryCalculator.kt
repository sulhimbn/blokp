package com.example.iurankomplek.payment

import com.example.iurankomplek.utils.Constants
import java.security.SecureRandom
import kotlin.math.min

class WebhookRetryCalculator {
    private val secureRandom = SecureRandom()

    fun calculateRetryDelay(retryCount: Int): Long {
        val exponentialDelay = (Constants.Webhook.INITIAL_RETRY_DELAY_MS *
            Math.pow(Constants.Webhook.RETRY_BACKOFF_MULTIPLIER, retryCount.toDouble())).toLong()

        val cappedDelay = min(exponentialDelay, Constants.Webhook.MAX_RETRY_DELAY_MS)

        val jitterMin = -Constants.Webhook.RETRY_JITTER_MS
        val jitterMax = Constants.Webhook.RETRY_JITTER_MS
        val jitter = secureRandom.nextLong() % (jitterMax - jitterMin + 1) + jitterMin

        return (cappedDelay + jitter).coerceAtLeast(0)
    }
}
