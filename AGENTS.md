# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Known Issues & Solutions

### CI-001: Result Type Name Conflict (RESOLVED 2026-01-10)
**Problem**: Custom `Result` class in `UiState.kt` shadowed Kotlin's built-in `Result<T>` type, causing compilation errors in CI/CD pipeline.

**Root Cause**:
- `UiState.kt` defined `sealed class Result<out T>` 
- Multiple files imported both `kotlin.Result` and used `Result<...>` in return signatures
- Kotlin compiler was confused about which `Result` type to use

**Solution Implemented**:
1. Renamed custom `Result` class to `OperationResult` in `UiState.kt`
2. Added extension methods to `OperationResult`:
   - `onSuccess(action: (T) -> Unit)`
   - `onError(action: (OperationResult.Error) -> Unit)`
   - `map(transform: (T) -> R)`
3. Updated all affected files:
   - `PaymentGateway.kt`, `MockPaymentGateway.kt`, `RealPaymentGateway.kt`
   - `TransactionRepository.kt`, `TransactionRepositoryImpl.kt`
   - All repository and usecase files
   - Fixed `EntityMapper.kt` to use explicit `kotlin.Result` import

**Files Modified**: 24 files (135 insertions, 76 deletions)

**Impact**: Fixes critical CI build failure, eliminates type safety issues

## Build/Test Commands
- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Install debug: `./gradlew installDebug`
- Single test execution: `./gradlew test --tests "com.example.iurankomplek.ExampleUnitTest"`
- Compile Kotlin: `./gradlew :app:compileDebugKotlin`

## Project-Specific Patterns
- Kotlin (100%): MainActivity.kt, LaporanActivity.kt, MenuActivity.kt, adapters, dan network layer menggunakan Kotlin
- API endpoints now use distinct paths: `@GET("users")` and `@GET("pemanfaatan")` in ApiService.kt for better clarity and maintainability
- API responses use specific models: UserResponse for user endpoint and PemanfaatanResponse for financial data endpoint to improve type safety
- Data model memiliki logika perhitungan khusus: `total_iuran_individu * 3` di LaporanActivity.kt line 56 untuk menghitung rekap iuran
- Network debugging menggunakan Chucker (hanya di debugImplementation) untuk inspeksi traffic API
- Glide image loading dengan CircleCrop transform untuk menampilkan avatar pengguna berbentuk bulat
- RecyclerView adapters now use DiffUtil for efficient updates instead of notifyDataSetChanged() untuk better performance
