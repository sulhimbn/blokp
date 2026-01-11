package com.example.iurankomplek.data.repository
import com.example.iurankomplek.utils.OperationResult

import com.example.iurankomplek.model.Announcement

interface AnnouncementRepository {
    suspend fun getAnnouncements(forceRefresh: Boolean = false): OperationResult<List<Announcement>>
    suspend fun getCachedAnnouncements(): OperationResult<List<Announcement>>
    suspend fun clearCache(): OperationResult<Unit>
}
