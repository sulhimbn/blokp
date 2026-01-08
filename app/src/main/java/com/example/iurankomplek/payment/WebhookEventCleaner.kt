package com.example.iurankomplek.payment

import android.util.Log
import com.example.iurankomplek.utils.Constants

class WebhookEventCleaner(
    private val webhookEventDao: WebhookEventDao,
    private val eventChannel: kotlinx.coroutines.Channel<Long>
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
        
        Log.d(TAG, "Retrying $retriedCount failed events")
        return retriedCount
    }

    suspend fun cleanupOldEvents(): Int {
        val cutoffTime = System.currentTimeMillis() - 
            (Constants.Webhook.MAX_EVENT_RETENTION_DAYS * 24L * 60L * 60L * 1000L)
        
        val deletedCount = webhookEventDao.deleteEventsOlderThan(cutoffTime)
        Log.d(TAG, "Cleaned up $deletedCount old webhook events")
        return deletedCount
    }
}
