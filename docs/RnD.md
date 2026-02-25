# RnD (Research & Development) Documentation

## Overview
This document serves as the long-term memory for the RnD specialist working on this Android project.

## Active Work Items

### Current Cycle
- **Issue #357**: Missing @Transaction annotations in TransactionRepository
- **Status**: Fix applied, PR in progress
- **Date**: 2026-02-25

## Completed Work

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
1. Issue #357: Missing @Transaction annotations (FIXED)
2. Issue #354: No Data Caching in Repositories
3. Issue #352: Fragments Using Deprecated this
4. Issue #351: Handler Memory Leak in BaseActivity (HIGH)
5. Issue #349: Room Database Not Encrypted (HIGH)
6. Issue #348: Webhook Receiver Has No Signature Verification (CRITICAL)

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
