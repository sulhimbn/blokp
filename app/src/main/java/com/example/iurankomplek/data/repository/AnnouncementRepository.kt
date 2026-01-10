package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.utils.Result

interface AnnouncementRepository {
    suspend fun getAnnouncements(forceRefresh: Boolean = false): Result<List<Announcement>>
    suspend fun getCachedAnnouncements(): Result<List<Announcement>>
    suspend fun clearCache(): Result<Unit>
}
