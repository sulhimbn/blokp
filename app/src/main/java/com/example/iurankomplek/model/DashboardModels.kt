package com.example.iurankomplek.model

data class DashboardData(
    val financialSummary: FinancialSummary,
    val announcements: List<Announcement>,
    val messages: List<Message>,
    val communityPosts: List<CommunityPost>,
    val unreadAnnouncements: Int,
    val unreadMessages: Int,
    val lastSyncTime: Long = System.currentTimeMillis()
)

data class FinancialSummary(
    val totalDue: Int,
    val totalCollected: Int,
    val totalExpenses: Int,
    val balance: Int,
    val paymentStatus: PaymentStatus,
    val lastPaymentDate: String?,
    val totalResidents: Int,
    val paidResidents: Int
)

enum class PaymentStatus {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}
