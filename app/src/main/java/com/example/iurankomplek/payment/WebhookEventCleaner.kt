package com.example.iurankomplek.payment

import android.util.Log
import com.example.iurankomplek.utils.Constants
import kotlinx.coroutines.channels.Channel

class WebhookEventCleaner(
    private val webhookEventDao: WebhookEventDao,
    private val eventChannel: Channel<Long>
) {
    private val TAG = "${Constants.Tags.WEBHOOK_RECEIVER}.EventCleaner"

    suspend fun retryFailedEvents(limit: Int = Constants.Webhook.DEFAULT_RETRY_LIMIT): Int {
        val cutoffTime = System.currentTimeMillis()
        val failedEvents = webhookEventDao.getPendingEventsByStatus(
            status = WebhookDeliveryStatus.FAILED,
            currentTime = cutoffTime,
            limit = limit
        )
        
        var retriedCount = 0
        for (event in failedEvents) {
            webhookEventDao.updateStatus(
                event.id,
                WebhookDeliveryStatus.PENDING
            )
            eventChannel.send(event.id)
            retriedCount++
        }
        
        Log.d(TAG, "Retrying failed events")
        return retriedCount
    }

    suspend fun cleanupOldEvents(): Int {
        val cutoffTime = System.currentTimeMillis() -
            (Constants.Webhook.MAX_EVENT_RETENTION_DAYS * 24L * 60L * 60L * 1000L)

        val deliveredEvents = webhookEventDao.getDeliveredEventsOlderThan(cutoffTime)
        val failedEvents = webhookEventDao.getFailedEventsOlderThan(cutoffTime)

        var softDeletedCount = 0
        for (event in deliveredEvents + failedEvents) {
            webhookEventDao.softDeleteById(event.id)
            softDeletedCount++
        }

        Log.d(TAG, "Soft deleted old webhook events")
        return softDeletedCount
    }

    suspend fun hardDeleteSoftDeletedOldEvents(): Int {
        val cutoffTime = System.currentTimeMillis() -
            (Constants.Webhook.MAX_EVENT_RETENTION_DAYS * 24L * 60L * 60L * 1000L)

        val deletedCount = webhookEventDao.hardDeleteSoftDeletedOlderThan(cutoffTime)
        Log.d(TAG, "Hard deleted old soft-deleted webhook events")
        return deletedCount
    }
}
