package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.network.ApiServiceV1
import com.example.iurankomplek.utils.Result
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

class AnnouncementRepositoryImpl(
    private val apiService: com.example.iurankomplek.network.ApiServiceV1
) : AnnouncementRepository, BaseRepository() {
    private val cache = ConcurrentHashMap<String, Announcement>()

    override suspend fun getAnnouncements(forceRefresh: Boolean): Result<List<Announcement>> {
        if (!forceRefresh && cache.isNotEmpty()) {
            return Result.Success(cache.values.toList())
        }

        return executeWithCircuitBreakerV2 { apiService.getAnnouncements() }
    }

    override suspend fun getCachedAnnouncements(): Result<List<Announcement>> {
        return try {
            Result.Success(cache.values.toList())
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Unknown error")
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            cache.clear()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message ?: "Unknown error")
        }
    }
}
