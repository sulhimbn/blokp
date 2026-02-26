# Backend Engineer Agent - Long-term Memory

## Overview
This document serves as the long-term memory for the autonomous backend-engineer agent working on the blokp Android project.

## Domain
- Backend data layer (Room database, repositories)
- Network layer (Retrofit, API services)
- Data integrity and transaction management

## Key Patterns

### Repository Pattern
- All data repositories should follow the repository pattern
- Use `BaseRepository` interface for common operations
- Use `BaseNetworkRepository` for network operations with retry logic

### Transaction Management
- **CRITICAL**: When a method performs multiple database operations that need atomicity, use `@androidx.room.Transaction` annotation
- Methods that combine insert + update, or get + update should be transactional
- Room DAO methods are already transactional individually - the annotation is needed at the repository level for multi-operation methods

### Example - When to Use @Transaction
```kotlin
// ✅ NEEDS @Transaction - multiple DB operations
@Transaction
suspend fun processPayment(request: PaymentRequest): Result<Transaction> {
    val transaction = Transaction.create(request)
    transactionDao.insert(transaction)  // Op 1
    
    val paymentResult = paymentGateway.processPayment(request)
    paymentResult.onSuccess { response ->
        val updatedTransaction = transaction.copy(status = PaymentStatus.COMPLETED)
        transactionDao.update(updatedTransaction)  // Op 2
    }
    return paymentResult.map { transaction }
}

// ❌ DOES NOT NEED @Transaction - single DB operation
suspend fun getTransactionById(id: String): Transaction? {
    return transactionDao.getTransactionById(id)
}
```

## Issue Categories

### Data Layer Issues
- Missing @Transaction annotations
- Missing data validation
- No caching in repositories
- Incorrect error handling
- Duplicate function definitions

### Network Layer Issues
- Missing retry logic
- Incorrect error handling
- No timeout configuration
- Hardcoded secrets (security issue)

## Known Issues Fixed

- PR #386: Duplicate Function Definitions in TransactionRepository
  - Fixed by removing duplicate `processPayment` and `refundPayment` function definitions that caused compilation errors

- Issue #357: Missing @Transaction Annotations in TransactionRepository
  - Fixed by adding @Transaction to `processPayment()` and `refundPayment()` methods

## Notes
- This is an Android/Kotlin project with Room database
- Network layer uses Retrofit 2 with OkHttp3
- No Android SDK available in CI - build verification limited to syntax checks

## Known Issues Fixed (Continued)

- PR #404: Security and Performance Fixes
  - Issue #399: Removed insecure `createInsecureTrustManager()` from SecurityManager.kt
    - This method created an all-trusting X509TrustManager that bypassed SSL verification
    - Removed method and 7 unused SSL-related imports
    - Fixes MITM attack vulnerability
  - Issue #401: Fixed unmanaged CoroutineScope in PaymentService.kt
    - Added `externalScope: CoroutineScope?` parameter for lifecycle-aware scope management
    - Callers can now pass viewModelScope or lifecycleScope for proper cancellation
    - Falls back to default scope for backward compatibility

- Issue #421: Additional CoroutineScope leaks in TransactionHistoryAdapter, WebhookReceiver, and TransactionHistoryActivity
  - Fixed in PR #492 by making externalScope parameter REQUIRED (non-null) in TransactionHistoryAdapter and WebhookReceiver
  - Removed unsafe CoroutineScope(Dispatchers.IO) fallback that caused memory leaks
  - TransactionHistoryActivity already uses lifecycleScope - no changes needed
  - Follows the same pattern as Issue #401/PR #404



## Known Issues Fixed (2026-02-25)

- Issue #451: Duplicate TransactionDatabase Implementations with Conflicting Encryption
  - Fixed by removing duplicate database class implementation in TransactionDatabase.kt
  - File originally had two complete implementations (185 lines instead of ~95)
  - First implementation used Base64 encoding (correct), second used UTF-8 (incorrect)
  - UTF-8 approach would cause data corruption when encrypting/decrypting database
  - Kept Base64 implementation, removed duplicate code
  - Also fixed pre-existing build.gradle syntax error (extra closing brace)
