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
  PR|  - Also fixed pre-existing build.gradle syntax error (extra closing brace)

## Known Issues Fixed (2026-02-26)

MX|- Issue #455: Missing Input Validation in TransactionRepository
XZ|  - Added validation in `processPayment()` method before creating transaction
WH|  - Validates: amount > 0, description not blank, customerId not blank
BS|  - Returns IllegalArgumentException with descriptive message on validation failure

VZ|- Issue: Duplicate Certificate Pins in Constants.kt
XD|  - Fixed ALL_CERTIFICATE_PINS array to not include duplicate backup pin
QW|  - Added note that backup pin needs to be updated in production
YM|  - Backup pin is currently same as primary - needs real backup from certificate provider

HP|- Issue: Missing Write Timeout in Network Configuration
QX|  - Added writeTimeout configuration to ApiConfig.kt (mock/debug client)
SY|  - Added writeTimeout configuration to SecurityConfig.kt (production client)
WR|  - Now using Constants.Network.WRITE_TIMEOUT (30 seconds) for all timeout types

## Proactive Scan Findings (2026-02-26)

### Network Layer - Good Practices Found
VB|- Certificate pinning implemented with OkHttp3 CertificatePinner
VR|- Retry logic with exponential backoff implemented in BaseNetworkRepository
VW|- Retry wrapper available in BaseActivity for Activities
VV|- Timeout configurations centralized in Constants.kt
VZ|- Security headers (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection)

### Data Layer - Good Practices Found
VF|- @Transaction annotations properly placed for multi-operation methods
VH|- DataValidator utility with sanitization methods (sanitizeName, sanitizeEmail, etc.)
VJ|- ValidatedDataItem model uses DataValidator for all incoming API data

### Issues Identified but Not Fixed (Requires External Action)

JB|- Hardcoded API Spreadsheet ID in build.gradle
KD|  - API_SPREADSHEET_ID is exposed in source code
KD|  - Recommendation: Move to local.properties or CI/CD secrets

KD|- Empty/Missing Webhook Secret in build.gradle
KD|  - WEBHOOK_SECRET is empty string in build.gradle
KD|  - Falls back to placeholder in Constants.kt
KD|  - Recommendation: Set via CI/CD environment variables

KD|- Activities Not Extending BaseActivity
KD|  - Multiple activities extend AppCompatActivity directly
KD|  - Missing retry logic for network calls
KD|  - Activities: VendorManagementActivity, CommunicationActivity, MenuActivity, PaymentActivity, etc.
KD|  - Recommendation: Either extend BaseActivity or add retry wrapper to repositories

KD|- CommunityFragment Missing Retry Logic
PV|KD|  - Fragment uses direct .enqueue() without retry
KV|KD|  - FIXED: Created BaseFragment.kt with retry support
RD|KD|  - Updated CommunityFragment to extend BaseFragment and use executeWithRetry()
KV|KD|  - Other fragments (VendorDatabaseFragment, WorkOrderManagementFragment, etc.) can be updated similarly
KD|  - Fragment uses direct .enqueue() without retry
KD|  - Recommendation: Create BaseFragment with retry support
