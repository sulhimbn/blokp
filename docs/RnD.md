# RnD (Research & Development) Documentation

## Overview
This document serves as the long-term memory for the RnD specialist working on this Android project.

## Active Work Items

### Current Cycle
- **Issue #397**: Duplicate UserRepositoryImpl Class Definitions - Compilation Blocker
- **Status**: Fix applied, PR created
- **Date**: 2026-02-25

## Completed Work

### Issue #397: Duplicate UserRepositoryImpl Class Definitions
- **Description**: UserRepositoryImpl.kt had TWO separate class definitions causing compilation failures
- **Risk Level**: Critical (compilation blocker)
- **Files Changed**: 
  - `app/src/main/java/com/example/iurankomplek/data/repository/UserRepositoryImpl.kt`
  - `app/src/test/java/com/example/iurankomplek/data/repository/UserRepositoryImplTest.kt`
- **Changes**:
  1. Merged two duplicate class definitions into single clean implementation
  2. Preserved caching logic from first implementation (5-min TTL)
  3. Preserved login/logout/getCurrentUserFlow from second implementation
  4. Removed duplicate imports and code blocks
  5. Updated test file to match new constructor signature (added UserSessionManager mock)
- **PR**: #411

### Issue #357: Missing @Transaction Annotations
- **Description**: TransactionRepository performed multi-operation transactions without @Transaction annotations
- **Risk Level**: Low (data integrity improvement)
- **Files Changed**: `app/src/main/java/com/example/iurankomplek/transaction/TransactionRepository.kt`
- **Changes**:
  1. Added `import androidx.room.Transaction`
  2. Added `@Transaction` annotation to `processPayment()` method
  3. Added `@Transaction` annotation to `refundPayment()` method

## Repository Health Indicators

### Build Status
- Android SDK required for full build verification
- CI workflow available in `.github/workflows/`

### Open Issues (Priority Order)
1. Issue #398: Hardcoded Secrets in Source Code (CRITICAL - security)
2. Issue #399: Insecure All-Trusting Trust Manager (HIGH - security)
3. Issue #400: Duplicate Room Dependencies (HIGH - build)
4. Issue #401: Unmanaged CoroutineScope Memory Leak (MEDIUM - performance)
5. Issue #354: No Data Caching in Repositories
6. Issue #352: Fragments Using Deprecated this

## RnD Principles
1. Small, safe, measurable improvements
2. Focus on data integrity and stability
3. Minimal code changes with maximum impact
4. Always verify with build/tests when possible
5. Document all changes for future reference

## Notes
- Android SDK not available in local environment - CI verifies builds
- All changes should be atomic and self-contained
- PRs must have "RnD" label and be linked to issues
