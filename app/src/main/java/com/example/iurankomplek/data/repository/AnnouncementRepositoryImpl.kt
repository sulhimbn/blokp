package com.example.iurankomplek.data.repository

import com.example.iurankomplek.model.Announcement
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class AnnouncementRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiService
) : BaseRepositoryLegacy(), AnnouncementRepository {
    private val cache = ConcurrentHashMap<String, Announcement>()

    override suspend fun getAnnouncements(forceRefresh: Boolean): Result<List<Announcement>> {
        if (!forceRefresh && cache.isNotEmpty()) {
            return Result.success(cache.values.toList())
        }

        return executeWithCircuitBreaker { apiService.getAnnouncements() }
    }

    override suspend fun getCachedAnnouncements(): Result<List<Announcement>> {
        return try {
            Result.success(cache.values.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            cache.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
