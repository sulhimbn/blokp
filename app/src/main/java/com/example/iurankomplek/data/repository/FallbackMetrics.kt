package com.example.iurankomplek.data.repository

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class FallbackUsageStats(
    val reason: FallbackReason,
    val count: Long,
    val lastUsedAt: Long = System.currentTimeMillis()
)

object FallbackMetrics {
    private val fallbackCounts = ConcurrentHashMap<FallbackReason, AtomicLong>()
    private val lastUsedAt = ConcurrentHashMap<FallbackReason, Long>()

    fun recordFallback(reason: FallbackReason) {
        val counter = fallbackCounts.getOrPut(reason) { AtomicLong(0) }
        counter.incrementAndGet()
        lastUsedAt[reason] = System.currentTimeMillis()

        android.util.Log.d(
            "FallbackMetrics",
            "Fallback used: $reason, total count: ${counter.get()}"
        )
    }

    fun getStats(): List<FallbackUsageStats> {
        return fallbackCounts.map { (reason, counter) ->
            FallbackUsageStats(
                reason = reason,
                count = counter.get(),
                lastUsedAt = lastUsedAt[reason] ?: 0L
            )
        }.sortedByDescending { it.count }
    }

    fun getCount(reason: FallbackReason): Long {
        return fallbackCounts[reason]?.get() ?: 0L
    }

    fun reset() {
        fallbackCounts.clear()
        lastUsedAt.clear()
        android.util.Log.d("FallbackMetrics", "Metrics reset")
    }
}
