# API Versioning Strategy

## Overview

IuranKomplek API follows a **path-based versioning strategy** to maintain backward compatibility while enabling evolution of the API. This document explains the versioning approach, current versions, and migration roadmap.

## Versioning Strategy

### Path-Based Versioning

All API versions are exposed via URL path prefixes:

```
https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/{version}/{endpoint}
```

Where `{version}` is:
- Empty string `""` or omitted → Legacy API
- `api/v1/` → API Version 1 (current recommended)

### Example URLs

| Version | Base URL | Example Endpoint |
|---------|-----------|-----------------|
| Legacy | `https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/` | `/users` |
| v1 | `https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/api/v1/` | `/api/v1/users` |

## API Versions

### Legacy API (v0 - Unofficial)

**Status**: 🟡 Deprecated (Production Use)
**URL Pattern**: `{base_url}/`
**Interface**: `ApiService` (Kotlin)
**Deprecation Date**: TBD (planned for 2026-07-01)
**Sunset Date**: TBD (planned for 2027-01-01)

**Characteristics**:
- No version prefix in URL
- Direct object responses (no wrapper)
- Mixed query parameter and request body usage
- No pagination support
- Basic error responses

**Response Format**:
```json
{
  "data": [
    { "id": "1", "name": "John Doe" }
  ]
}
```

**When to Use**:
- Existing integrations that haven't migrated yet
- Temporary during migration period
- Legacy client compatibility

**When to Migrate**:
- Immediately for all new integrations
- As soon as possible for existing integrations
- Before deprecation deadline

### API v1 (Current Recommended)

**Status**: 🟢 Production Ready
**URL Pattern**: `{base_url}/api/v1/`
**Interface**: `ApiServiceV1` (Kotlin)
**Release Date**: 2026-01-08
**Documentation**: [OpenAPI Specification](openapi.yaml) | [API Standardization Guide](API_STANDARDIZATION.md)

**Characteristics**:
- Explicit version prefix (`/api/v1/`)
- Standardized response wrappers
- Request bodies for all create/update operations
- Pagination support for list endpoints
- Request ID tracing
- Enhanced error responses with field-level details
- Consistent naming conventions

**Response Format (Single Resource)**:
```json
{
  "data": {
    "id": "1",
    "name": "John Doe",
    "email": "john.doe@example.com"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Response Format (Collection with Pagination)**:
```json
{
  "data": [
    {
      "id": "1",
      "name": "John Doe",
      "email": "john.doe@example.com"
    }
  ],
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 100,
    "total_pages": 5,
    "has_next": true,
    "has_previous": false
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**Error Response Format**:
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": "Email format is invalid",
    "field": "email"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

**When to Use**:
- All new integrations
- Migrations from legacy API
- Any integration requiring pagination
- Any integration needing better error handling
- Any integration requiring request tracing

## Comparison: Legacy vs v1

### Endpoint Comparison

| Feature | Legacy API | API v1 |
|---------|-------------|---------|
| URL Prefix | None (implicit) | `/api/v1/` |
| Response Wrapper | None | `ApiResponse<T>`, `ApiListResponse<T>` |
| Request Bodies | Mixed (query + body) | Consistent (all body) |
| Pagination | ❌ No | ✅ Yes (optional) |
| Request ID | ❌ No | ✅ Yes |
| Field-Level Errors | ❌ No | ✅ Yes |
| Response Timestamp | ❌ No | ✅ Yes |

### Request/Response Comparison

#### Legacy API
```http
GET /users
```

```json
{
  "data": [
    { "id": "1", "name": "John Doe", "email": "john@example.com" }
  ]
}
```

#### API v1
```http
GET /api/v1/users?page=1&pageSize=20
X-Request-ID: 550e8400-e29b-41d4-a716-446655440000
```

```json
{
  "data": [
    { "id": "1", "name": "John Doe", "email": "john@example.com" }
  ],
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 100,
    "total_pages": 5,
    "has_next": true,
    "has_previous": false
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

## Migration Strategy

### Migration Phases

#### Phase 1: Infrastructure (✅ Completed - 2026-01-08)
- [x] Define API versioning strategy
- [x] Create API v1 interface (`ApiServiceV1`)
- [x] Implement standardized response wrappers
- [x] Add pagination support
- [x] Update OpenAPI specification

#### Phase 2: Client-Side Preparation (Current)
- [ ] Add `ApiServiceV1` to `ApiConfig`
- [ ] Update all repositories to use `ApiServiceV1`
- [ ] Update repository response parsing
- [ ] Add feature flag for gradual rollout
- [ ] Write unit tests for v1 integration

#### Phase 3: Backend Deployment
- [ ] Deploy `/api/v1/` endpoints to production
- [ ] Add deprecation headers to legacy endpoints
- [ ] Monitor v1 endpoint performance
- [ ] Load test v1 endpoints

**Deprecation Headers for Legacy**:
```http
X-API-Deprecated: true
X-API-Sunset: 2027-01-01
X-API-Recommended-Version: v1
```

#### Phase 4: Client Rollout
- [ ] Enable v1 API for beta testers (10%)
- [ ] Monitor for errors and issues
- [ ] Gradually increase rollout (50% → 100%)
- [ ] Collect performance metrics
- [ ] Roll back if critical issues found

#### Phase 5: Deprecation (6-month window)
- [ ] Announce deprecation of legacy API
- [ ] Communicate migration deadline
- [ ] Monitor legacy API usage
- [ ] Provide migration support
- [ ] Update all external integrations

#### Phase 6: Removal
- [ ] Remove legacy `ApiService` interface
- [ ] Remove legacy endpoints from backend
- [ ] Remove feature flags
- [ ] Archive legacy code
- [ ] Finalize migration documentation

### Migration Timeline

| Phase | Duration | Start Date | End Date | Status |
|-------|-----------|-------------|-----------|--------|
| Phase 1: Infrastructure | 1 week | 2026-01-08 | 2026-01-15 | ✅ Completed |
| Phase 2: Client Preparation | 2-3 weeks | 2026-01-15 | 2026-02-05 | 🔄 In Progress |
| Phase 3: Backend Deployment | 1-2 weeks | 2026-02-05 | 2026-02-19 | ⏳ Pending |
| Phase 4: Client Rollout | 2-3 weeks | 2026-02-19 | 2026-03-12 | ⏳ Pending |
| Phase 5: Deprecation | 6 months | 2026-02-19 | 2026-08-19 | ⏳ Pending |
| Phase 6: Removal | 1 week | 2026-08-19 | 2026-08-26 | ⏳ Pending |

**Total Migration Time**: 3-4 weeks for rollout + 6 months deprecation period

## Client-Side Migration Guide

### Step 1: Update Dependencies

No new dependencies required. The `ApiServiceV1` interface is already available.

### Step 2: Update Repository Implementation

**Before (Legacy)**:
```kotlin
class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {
    
    override suspend fun getUsers(): Result<UserResponse> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
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

**After (API v1)**:
```kotlin
class UserRepositoryImpl(
    private val apiServiceV1: ApiServiceV1
) : UserRepository {
    
    override suspend fun getUsers(): Result<UserResponse> {
        return try {
            val response = apiServiceV1.getUsers()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Result.success(apiResponse.data)  // Unwrap data from ApiResponse<T>
            } else {
                Result.failure(Exception("API error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Step 3: Update ApiConfig

**Add v1 API Service**:
```kotlin
object ApiConfig {
    private val USE_V1_API = BuildConfig.FEATURE_V1_API_ENABLED
    
    fun getApiService(): ApiService {
        return if (USE_V1_API) {
            getApiServiceV1()  // Use v1
        } else {
            getLegacyApiService()  // Use legacy
        }
    }
    
    private fun getApiServiceV1(): ApiServiceV1 {
        return Retrofit.Builder()
            .baseUrl(BASE_URL + "api/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceV1::class.java)
    }
    
    private fun getLegacyApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
```

### Step 4: Testing

**Unit Tests**:
```kotlin
@Test
fun `getUsers with v1 returns data correctly`() = runTest {
    // Given
    val expectedUsers = listOf(
        DataItem(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            alamat = "123 Main St",
            iuran_perwarga = 500000,
            total_iuran_rekap = 1500000,
            jumlah_iuran_bulanan = 500000,
            total_iuran_individu = 1500000,
            pengeluaran_iuran_warga = 200000,
            pemanfaatan_iuran = "Maintenance",
            avatar = "https://example.com/avatar.jpg"
        )
    )
    val apiResponse = ApiResponse(expectedUsers)
    coEvery { apiServiceV1.getUsers() } returns Response.success(apiResponse)
    
    // When
    val result = repository.getUsers()
    
    // Then
    assertTrue(result.isSuccess)
    assertEquals(expectedUsers, result.getOrNull()?.data)
}
```

**Integration Tests**:
```kotlin
@Test
fun `test v1 API connectivity`() = runTest {
    // When
    val response = apiServiceV1.getUsers()
    
    // Then
    assertTrue(response.isSuccessful)
    assertNotNull(response.body())
    assertNotNull(response.body()?.data)
    assertNotNull(response.body()?.request_id)  // v1 specific
    assertNotNull(response.body()?.timestamp)  // v1 specific
}
```

## Breaking Changes

### Changes from Legacy to v1

1. **URL Prefix**
   - Legacy: `/users`
   - v1: `/api/v1/users`

2. **Response Structure**
   - Legacy: Direct `data` array
   - v1: Wrapped in `ApiResponse<T>` with metadata

3. **Response Unwrapping**
   - Legacy: `response.body()?.data`
   - v1: `response.body()?.data` (unwrap from `ApiResponse<T>`)

4. **Pagination**
   - Legacy: No pagination
   - v1: Optional pagination with `page` and `pageSize` parameters

## Backward Compatibility

### Migration Period
- **Duration**: 6 months (2026-02-19 to 2026-08-19)
- **Status**: Both legacy and v1 APIs operational
- **Support**: Full support for both versions
- **Communication**: Regular updates on migration progress

### After Migration
- Legacy API will be sunset on 2026-08-19
- Only v1 API will be supported
- Legacy endpoints will return HTTP 410 Gone

## Rollback Plan

If critical issues are discovered during v1 rollout:

1. **Immediate Rollback**:
   - Disable `FEATURE_V1_API_ENABLED` feature flag
   - Legacy API continues to work without interruption

2. **Investigation**:
   - Analyze logs and error reports
   - Review metrics and performance data
   - Identify root cause of issues

3. **Fix**:
   - Address issues in v1 implementation
   - Thoroughly test fixes in staging
   - Update unit and integration tests

4. **Re-migration**:
   - Gradual rollout starting from 10%
   - Monitor closely for issues
   - Incrementally increase to 50% → 100%

## Success Metrics

### Pre-Migration Baseline
- Legacy API error rate: < 1%
- Average response time: < 500ms
- Repository test coverage: > 80%

### Post-Migration Targets
- v1 API error rate: ≤ legacy baseline (≤ 1%)
- v1 API response time: ≤ 110% of baseline (≤ 550ms)
- Repository test coverage: ≥ baseline (≥ 80%)
- Zero breaking changes for existing features
- 100% client migration before deprecation deadline

## Versioning Best Practices

### For API Consumers

1. **Always Specify Version**: Never rely on implicit version
   - ❌ Bad: `/users` (implicit legacy)
   - ✅ Good: `/api/v1/users` (explicit v1)

2. **Check Deprecation Headers**: Monitor for deprecation warnings
   ```http
   X-API-Deprecated: true
   X-API-Sunset: 2027-01-01
   ```

3. **Version in Code**: Store API version in configuration
   ```kotlin
   object ApiConfig {
       const val API_VERSION = "v1"
       const val API_VERSION_PREFIX = "api/v1/"
   }
   ```

4. **Plan for Migration**: Allocate time for version upgrades

### For API Providers

1. **Backward Compatibility**: Maintain previous versions for 6 months
2. **Deprecation Headers**: Always include deprecation information
3. **Clear Communication**: Notify consumers early and often
4. **Migration Support**: Provide guides and tools for migration
5. **Gradual Rollout**: Use feature flags for safe deployment

## Documentation

- **[OpenAPI Specification](openapi.yaml)** - Complete API contract
- **[API Standardization Guide](API_STANDARDIZATION.md)** - Design patterns and best practices
- **[API Migration Guide](API_MIGRATION_GUIDE.md)** - Step-by-step migration instructions
- **[API Documentation Hub](API_DOCS_HUB.md)** - Unified documentation index

## Support

### Migration Assistance
- **Email**: dev@iurankomplek.com
- **GitHub Issues**: Report issues with "API Migration" label
- **Documentation**: All guides linked above

### Getting Help
1. Check this versioning strategy document
2. Review API Standardization Guide
3. Consult Migration Guide
4. Open GitHub issue with detailed error information

---

*Last Updated: 2026-01-08*
*Version: 1.0.0*
*Maintained by: Integration Engineer*
