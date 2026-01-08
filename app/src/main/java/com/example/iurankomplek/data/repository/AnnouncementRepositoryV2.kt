package com.example.iurankomplek.data.repository

import com.example.iurankomplek.data.repository.BaseRepositoryV2
import com.example.iurankomplek.data.repository.cache.CacheStrategy
import com.example.iurankomplek.data.repository.cache.InMemoryCacheStrategy
import com.example.iurankomplek.model.Announcement
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

/**
 * Refactored AnnouncementRepository using unified repository pattern.
 * Extends BaseRepositoryV2 for consistent error handling and caching.
 *
 * BEFORE (37 lines):
 * - Extended BaseRepository (old version)
 * - Manual ConcurrentHashMap for caching
 * - Duplicate circuit breaker logic
 *
 * AFTER (this implementation):
 * - Extends BaseRepositoryV2 (enhanced)
 * - Uses InMemoryCacheStrategy
 * - No circuit breaker duplication
 * - Unified error handling
 * - Clearer code structure
 */
class AnnouncementRepositoryV2(
    private val apiService: com.example.iurankomplek.network.ApiService
) : AnnouncementRepository(), BaseRepositoryV2<List<Announcement>>() {

    override val cacheStrategy: CacheStrategy<List<Announcement>> =
        InMemoryCacheStrategy()

    override suspend fun getAnnouncements(forceRefresh: Boolean): Result<List<Announcement>> {
        return fetchWithCache(
            cacheKey = "announcements",
            forceRefresh = forceRefresh,
            fromNetwork = { apiService.getAnnouncements() }
        )
    }

    override suspend fun getCachedAnnouncements(): Result<List<Announcement>> {
        return try {
            val cached = cacheStrategy.get("announcements")
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(Exception("No cached announcements"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            this@AnnouncementRepositoryV2.clearCache()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
