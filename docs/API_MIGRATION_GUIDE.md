# API Standardization Migration Guide

## Overview

This document provides guidance for migrating from legacy API patterns to standardized V1 API patterns in the IuranKomplek application.

## Migration Status (2026-01-08)

### Completed Work

✅ **Request Body Standardization**
- Legacy `ApiService` now uses request bodies instead of query parameters
- All POST endpoints use `@Body` annotations with DTO objects
- All PUT endpoints use `@Body` annotations for complex payloads
- Non-breaking change: wire format remains the same

✅ **API Versioning Infrastructure**
- Created `ApiServiceV1` interface with `/api/v1` prefix
- All V1 endpoints use standardized response wrappers (`ApiResponse<T>`, `ApiListResponse<T>`)
- Legacy `ApiService` maintained for backward compatibility

✅ **Standardized Request Models**
- All request DTOs defined in `network/model/ApiRequest.kt`:
  - `CreateVendorRequest`
  - `UpdateVendorRequest`
  - `CreateWorkOrderRequest`
  - `AssignVendorRequest`
  - `UpdateWorkOrderRequest`
  - `SendMessageRequest`
  - `CreateCommunityPostRequest`
  - `InitiatePaymentRequest`

✅ **Standardized Response Models**
- `ApiResponse<T>`: Wrapper for single resource responses
- `ApiListResponse<T>`: Wrapper for collection responses with pagination
- `ApiErrorResponse`: Standardized error response with request ID
- `PaginationMetadata`: Pagination information for list responses

### API Interfaces Comparison

#### Legacy ApiService (Current)
```kotlin
interface ApiService {
    @POST("vendors")
    suspend fun createVendor(@Body request: CreateVendorRequest): Response<SingleVendorResponse>

    @GET("vendors")
    suspend fun getVendors(): Response<VendorResponse>
}
```

**Characteristics:**
- Uses request bodies for create/update operations
- No API version prefix
- Legacy response format (no wrapper)
- **Status**: Production use, backward compatible

#### Standardized ApiServiceV1 (Ready for Migration)
```kotlin
interface ApiServiceV1 {
    @POST("api/v1/vendors")
    suspend fun createVendor(@Body request: CreateVendorRequest): Response<ApiResponse<SingleVendorResponse>>

    @GET("api/v1/vendors")
    suspend fun getVendors(): Response<ApiResponse<VendorResponse>>
}
```

**Characteristics:**
- Uses request bodies for create/update operations
- API version prefix (`/api/v1`)
- Standardized response wrapper (`ApiResponse<T>`)
- Pagination support (`ApiListResponse<T>`)
- Request ID tracking
- **Status**: Ready for migration, not yet in production

## Migration Plan

### Phase 1: Backend Preparation (Completed)
- [x] Define API versioning strategy (path-based: `/api/v1`)
- [x] Create standardized response wrapper models
- [x] Create standardized request DTO models
- [x] Implement ApiServiceV1 with fully standardized patterns
- [x] Document migration timeline and deprecation strategy

### Phase 2: Client-Side Preparation (Current Status)
- [ ] Add ApiServiceV1 to ApiConfig (parallel instance)
- [ ] Update all repositories to use ApiServiceV1
- [ ] Update repository responses to unwrap ApiResponse/ApiListResponse
- [ ] Add feature flag for gradual rollout
- [ ] Unit test repository changes

**Affected Repositories:**
- `UserRepositoryImpl`
- `PemanfaatanRepositoryImpl`
- `VendorRepositoryImpl`
- `AnnouncementRepositoryImpl`
- `MessageRepositoryImpl`
- `CommunityPostRepositoryImpl`
- `TransactionRepositoryImpl`
- `PaymentService`

### Phase 3: Backend Migration (Future)
- [ ] Implement `/api/v1` endpoints on backend
- [ ] Add deprecation headers to legacy endpoints:
  ```http
  X-API-Deprecated: true
  X-API-Sunset: 2026-07-01
  X-API-Recommended-Version: v1
  ```
- [ ] Test new endpoints with existing production data
- [ ] Deploy v1 endpoints alongside legacy endpoints

### Phase 4: Client Rollout (Future)
- [ ] Enable ApiServiceV1 via feature flag for beta testers
- [ ] Monitor for errors and issues
- [ ] Gradually increase rollout percentage (10% → 50% → 100%)
- [ ] Collect metrics and performance data
- [ ] Roll back if critical issues found

### Phase 5: Deprecation (Future)
- [ ] Announce deprecation of legacy endpoints
- [ ] Set sunset date (6 months after v1 launch)
- [ ] Monitor legacy endpoint usage
- [ ] Communicate migration deadline to all stakeholders
- [ ] Update all external integrations

### Phase 6: Cleanup (Future)
- [ ] Remove legacy `ApiService` interface after sunset date
- [ ] Remove legacy endpoints from backend
- [ ] Remove feature flags
- [ ] Update documentation to reflect v1-only API
- [ ] Archive legacy code

## Migration Example: UserRepositoryImpl

### Before (Legacy ApiService)
```kotlin
class UserRepositoryImpl(private val apiService: ApiService) : UserRepository {
    override suspend fun getUsers(): Result<UserResponse> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### After (ApiServiceV1)
```kotlin
class UserRepositoryImpl(private val apiServiceV1: ApiServiceV1) : UserRepository {
    override suspend fun getUsers(): Result<UserResponse> {
        return try {
            val response = apiServiceV1.getUsers()
            if (response.isSuccessful) {
                val apiResponse = response.body()!!
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception("API error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Key Changes:
1. **Dependency Injection**: `ApiService` → `ApiServiceV1`
2. **Response Unwrapping**: Extract `.data` from `ApiResponse<T>` wrapper
3. **Error Handling**: Same pattern, just different response structure

## Backward Compatibility Strategy

### Dual API Service Approach
Maintain both `ApiService` and `ApiServiceV1` simultaneously during migration period:

```kotlin
object ApiConfig {
    private val USE_V1_API = BuildConfig.FEATURE_V1_API_ENABLED

    fun getApiService(): ApiService {
        return if (USE_V1_API) {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build()
                .create(ApiServiceV1::class.java)
        } else {
            getLegacyApiService()
        }
    }

    private fun getLegacyApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
            .create(ApiService::class.java)
    }
}
```

### Benefits:
- Gradual migration with feature flags
- Instant rollback capability
- A/B testing possible
- Zero downtime deployment

## Testing Strategy

### Unit Tests
- Test repository methods with both ApiService and ApiServiceV1
- Verify response unwrapping logic
- Mock ApiServiceV1 for isolated testing

### Integration Tests
- Test all ApiServiceV1 endpoints against staging environment
- Verify response structure matches ApiResponse<T> format
- Test error responses with ApiErrorResponse format

### Migration Tests
- Test feature flag toggle
- Verify both services return same data
- Performance comparison between v1 and legacy

## Rollback Procedure

If critical issues are discovered during migration:

1. **Immediate Rollback**: Toggle feature flag to disable V1 API
2. **Investigation**: Analyze logs and metrics
3. **Fix**: Address issues in code
4. **Testing**: Thoroughly test fixes in staging
5. **Re-migration**: Gradual rollout again from 10%

## Success Metrics

### Pre-Migration
- Baseline API error rate
- Average response time
- Repository unit test coverage

### Post-Migration
- API error rate ≤ baseline
- Response time within 10% of baseline
- Unit test coverage maintained
- Zero breaking changes for existing features

## Documentation Updates Required

- [ ] Update `docs/API.md` with v1 endpoint documentation
- [ ] Update `docs/ARCHITECTURE.md` with migration status
- [ ] Update `docs/blueprint.md` with ApiServiceV1
- [ ] Update `AGENTS.md` with new build commands
- [ ] Create API changelog for v1 release

## Timeline

| Phase | Duration | Start Date | End Date | Status |
|--------|-----------|-------------|------------|--------|
| Phase 1: Backend Preparation | 1 week | 2026-01-08 | 2026-01-15 | ✅ Completed |
| Phase 2: Client Preparation | 2-3 weeks | 2026-01-15 | 2026-02-05 | 🔄 Ready to start |
| Phase 3: Backend Migration | 1-2 weeks | 2026-02-05 | 2026-02-19 | ⏳ Pending |
| Phase 4: Client Rollout | 2-3 weeks | 2026-02-19 | 2026-03-12 | ⏳ Pending |
| Phase 5: Deprecation | 6 months | 2026-02-19 | 2026-08-19 | ⏳ Pending |
| Phase 6: Cleanup | 1 week | 2026-08-19 | 2026-08-26 | ⏳ Pending |

**Estimated Total Migration Time**: 3-4 weeks for rollout + 6 months deprecation period

---

*Created: 2026-01-08*
*Version: 1.0.0*
*Maintained by: Integration Engineer*
