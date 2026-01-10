package com.example.iurankomplek.data.repository.cache

import com.example.iurankomplek.data.cache.CacheManager
import kotlinx.coroutines.flow.first
import java.util.Date

/**
 * Database cache strategy using Room database via CacheManager.
 * Suitable for complex repositories requiring persistent caching.
 *
 * Features:
 * - Persistent caching (survives app restart)
 * - Cache freshness validation with timestamps
 * - Foreign key relationships (User with FinancialRecords)
 * - Thread-safe (Room handles synchronization)
 *
 * Use case: User data, financial records, transactions
 *
 * @param T Type of cached data (UserResponse, PemanfaatanResponse, etc.)
 */
class DatabaseCacheStrategy<T>(
    private val cacheGetter: suspend () -> T?
) : CacheStrategy<T> {

    override suspend fun get(key: String?): T? {
        return try {
            cacheGetter()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun put(key: String?, value: T) {
    }

    override suspend fun isValid(cachedValue: T?, forceRefresh: Boolean): Boolean {
        if (forceRefresh) {
            return false
        }

        val cached = cachedValue ?: return false

        try {
            val latestUpdate = CacheManager.getUserDao().getLatestUpdatedAt()
            if (latestUpdate != null) {
                return CacheManager.isCacheFresh(latestUpdate.time)
            }
        } catch (e: Exception) {
        }

        return false
    }

    override suspend fun clear() {
        try {
            CacheManager.getUserDao().deleteAll()
            CacheManager.getFinancialRecordDao().deleteAll()
        } catch (e: Exception) {
        }
    }
}
