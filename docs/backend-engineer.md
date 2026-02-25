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

### Network Layer Issues
- Missing retry logic
- Incorrect error handling
- No timeout configuration
- Hardcoded secrets (security issue)

## Known Issues Fixed
- Issue #357: Missing @Transaction Annotations in TransactionRepository
  - Fixed by adding @Transaction to `processPayment()` and `refundPayment()` methods

## Notes
- This is an Android/Kotlin project with Room database
- Network layer uses Retrofit 2 with OkHttp3
- No Android SDK available in CI - build verification limited to syntax checks
