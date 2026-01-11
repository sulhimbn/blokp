package com.example.iurankomplek.payment

class WebhookEventMonitor(
    private val webhookEventDao: WebhookEventDao
) {
    suspend fun getPendingEventCount(): Int {
        return webhookEventDao.countByStatus(WebhookDeliveryStatus.PENDING)
    }

    suspend fun getFailedEventCount(): Int {
        return webhookEventDao.countByStatus(WebhookDeliveryStatus.FAILED)
    }
}
