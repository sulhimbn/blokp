package com.example.iurankomplek.event

/**
 * Sealed class representing all application events for cross-ViewModel communication.
 * Used with EventBus to enable reactive updates across features.
 */
sealed class AppEvent {
    
    // Payment Events
    data class PaymentCompleted(
        val paymentId: String,
        val amount: java.math.BigDecimal
    ) : AppEvent()
    
    data class PaymentFailed(
        val error: String
    ) : AppEvent()
    
    // Transaction Events
    data class TransactionCreated(
        val transactionId: String
    ) : AppEvent()
    
    data class TransactionUpdated(
        val transactionId: String
    ) : AppEvent()
    
    // User Events
    data class UserLoggedIn(
        val userId: String
    ) : AppEvent()
    
    data object UserLoggedOut : AppEvent()
    
    data class UserProfileUpdated(
        val userId: String
    ) : AppEvent()
    
    // Financial Events
    data object FinancialDataUpdated : AppEvent()
    
    // Network Events
    data class NetworkStatusChanged(
        val isConnected: Boolean
    ) : AppEvent()
    
    // Announcement Events
    data class NewAnnouncement(
        val announcementId: String
    ) : AppEvent()
    
    data class AnnouncementRead(
        val announcementId: String
    ) : AppEvent()
    
    // Message Events
    data class NewMessage(
        val messageId: String,
        val senderId: String
    ) : AppEvent()
    
    data class MessageRead(
        val messageId: String
    ) : AppEvent()
    
    // Work Order Events
    data class WorkOrderCreated(
        val workOrderId: String
    ) : AppEvent()
    
    data class WorkOrderUpdated(
        val workOrderId: String,
        val status: String
    ) : AppEvent()
    
    // Vendor Events
    data class VendorDataUpdated : AppEvent()
    
    // Cache Events
    data class CacheCleared : AppEvent()
    
    // Refresh Events
    data object RefreshAllData : AppEvent()
}
