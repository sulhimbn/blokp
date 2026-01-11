package com.example.iurankomplek.data.cache

object CacheConstants {
    const val DEFAULT_CACHE_FRESHNESS_MS = 5L * 60 * 1000L // 5 minutes
    
    const val SHORT_CACHE_MS = 1L * 60 * 1000L // 1 minute (for real-time data)
    
    const val LONG_CACHE_MS = 30L * 60 * 1000L // 30 minutes (for rarely changing data)
    
    const val MAX_CACHE_SIZE_MB = 50
    
    const val CACHE_CLEANUP_THRESHOLD_MS = 7L * 24 * 60 * 60 * 1000L // 7 days
    
    object CacheType {
        const val USERS = "users"
        const val FINANCIAL_RECORDS = "financial_records"
        const val VENDORS = "vendors"
        const val TRANSACTIONS = "transactions"
    }
    
    object SyncStatus {
        const val PENDING = "pending"
        const val SYNCED = "synced"
        const val FAILED = "failed"
    }
}
