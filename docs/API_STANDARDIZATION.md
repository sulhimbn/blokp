# API Standardization Guide

## Overview

This document establishes standardization patterns for the IuranKomplek API to ensure consistency, maintainability, and backward compatibility across all endpoints.

## Table of Contents

1. [API Versioning](#api-versioning)
2. [Naming Conventions](#naming-conventions)
3. [Request/Response Patterns](#requestresponse-patterns)
4. [Error Handling](#error-handling)
5. [HTTP Methods](#http-methods)
6. [Status Codes](#status-codes)
7. [Pagination](#pagination)
8. [Migration Plan](#migration-plan)

---

## API Versioning

### Versioning Strategy

**Current Version**: v1
**Version Prefix**: `/api/v1`

### Versioning Rules

1. **URL Path Versioning**: Use path-based versioning for clear separation
   ```
   GET /api/v1/users
   POST /api/v1/payments/initiate
   ```

2. **Backward Compatibility**: Maintain previous major versions for at least 6 months

3. **Version Deprecation**: Add deprecation headers to deprecated versions
   ```http
   X-API-Deprecated: true
   X-API-Sunset: 2026-07-01
   X-API-Recommended-Version: v2
   ```

4. **Breaking Changes**: Always increment major version for breaking changes
   - v1.x → v2.0 for breaking changes
   - v1.x → v1.y for non-breaking additions

### Implementation Plan

#### Phase 1: Add Version Prefix (Current)
- Add `/api/v1` prefix to all new endpoints
- Maintain backward compatibility with existing endpoints
- Document deprecation timeline for non-versioned endpoints

#### Phase 2: Migrate Existing Endpoints (Future)
- Migrate all endpoints to versioned paths
- Add HTTP redirects from old paths to new versioned paths
- Update all client implementations

---

## Naming Conventions

### Endpoint Naming

**RESTful Resource Naming**:
```
GET    /api/v1/users          - List all users
GET    /api/v1/users/{id}     - Get specific user
POST   /api/v1/users          - Create new user
PUT    /api/v1/users/{id}     - Update user
DELETE /api/v1/users/{id}     - Delete user
```

**Nested Resources**:
```
GET /api/v1/users/{userId}/payments           - User's payments
POST /api/v1/users/{userId}/payments           - Create payment
GET /api/v1/work-orders/{id}/assign          - Assign work order
PUT /api/v1/work-orders/{id}/status         - Update status
```

### Field Naming

**JSON (API Response)**: snake_case
```json
{
  "first_name": "John",
  "last_name": "Doe",
  "contact_person": "Jane Smith",
  "phone_number": "+1234567890",
  "contract_start": "2024-01-01",
  "contract_end": "2024-12-31"
}
```

**Kotlin (Data Models)**: camelCase
```kotlin
data class User(
    val firstName: String,
    val lastName: String,
    val contactPerson: String,
    val phoneNumber: String,
    val contractStart: String,
    val contractEnd: String
)
```

**Gson Serialization**: Use @SerializedName annotation for mapping
```kotlin
data class User(
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String,
    
    @SerializedName("contact_person")
    val contactPerson: String
)
```

### Enum Naming

**Values**: UPPERCASE_SNAKE_CASE
```kotlin
enum class PaymentMethod {
    CREDIT_CARD,
    BANK_TRANSFER,
    E_WALLET,
    VIRTUAL_ACCOUNT
}

enum class WorkOrderPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
```

---

## Request/Response Patterns

### Request Structure

#### Single Resource Request (GET)
```http
GET /api/v1/users/{id}
Authorization: Bearer {token}
X-Request-ID: {uuid}
X-Client-Version: 1.0.0
```

#### Collection Request (GET)
```http
GET /api/v1/users?page=1&pageSize=20&sort=name&order=asc
Authorization: Bearer {token}
X-Request-ID: {uuid}
```

#### Create Request (POST) with Body
```http
POST /api/v1/users
Content-Type: application/json
Authorization: Bearer {token}
X-Request-ID: {uuid}

{
  "first_name": "John",
  "last_name": "Doe",
  "email": "john.doe@example.com",
  "phone_number": "+1234567890"
}
```

#### Update Request (PUT/PATCH) with Body
```http
PUT /api/v1/users/{id}
Content-Type: application/json
Authorization: Bearer {token}
X-Request-ID: {uuid}

{
  "first_name": "John",
  "last_name": "Smith",
  "phone_number": "+9876543210"
}
```

#### Delete Request (DELETE)
```http
DELETE /api/v1/users/{id}
Authorization: Bearer {token}
X-Request-ID: {uuid}
```

### Response Structure

#### Standard Response Wrapper
```json
{
  "data": { ... },
  "request_id": "uuid",
  "timestamp": 1234567890000
}
```

#### List Response with Pagination
```json
{
  "data": [ ... ],
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 100,
    "total_pages": 5,
    "has_next": true,
    "has_previous": false
  },
  "request_id": "uuid",
  "timestamp": 1234567890000
}
```

#### Error Response
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": "Email format is invalid",
    "field": "email"
  },
  "request_id": "uuid",
  "timestamp": 1234567890000
}
```

### Request vs Query Parameters

**Use Query Parameters For**:
- Filtering: `?status=active&priority=high`
- Sorting: `?sort=name&order=asc`
- Pagination: `?page=1&pageSize=20`
- Simple lookups: `?userId=123`

**Use Request Body For**:
- Create operations (POST)
- Update operations (PUT/PATCH)
- Complex filtering with multiple criteria
- Bulk operations

**Examples**:

✅ **Good (Query for filtering)**:
```
GET /api/v1/work-orders?status=pending&priority=high
```

✅ **Good (Body for create)**:
```
POST /api/v1/work-orders
{
  "title": "Fix plumbing",
  "description": "Kitchen sink leak",
  "category": "plumbing",
  "priority": "high",
  "estimated_cost": 500.00
}
```

❌ **Bad (Too many query params)**:
```
POST /api/v1/vendors?name=ACME&contactPerson=John&phone=123&email=john@acme.com&specialty=plumbing&address=123%20Main%20St&license=XYZ&insurance=ABC&contractStart=2024-01-01&contractEnd=2024-12-31
```

✅ **Good (Request body instead)**:
```
POST /api/v1/vendors
{
  "name": "ACME",
  "contact_person": "John Smith",
  "phone_number": "+1234567890",
  "email": "john@acme.com",
  "specialty": "plumbing",
  "address": "123 Main St",
  "license_number": "XYZ",
  "insurance_info": "ABC",
  "contract_start": "2024-01-01",
  "contract_end": "2024-12-31"
}
```

---

## Error Handling

### Standard Error Response Format

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": "Email format is invalid",
    "field": "email"
  },
  "request_id": "uuid",
  "timestamp": 1234567890000
}
```

### Error Codes

| Code | HTTP Status | Description | User Message |
|------|-------------|-------------|---------------|
| `VALIDATION_ERROR` | 400 | Request validation failed | Invalid input data |
| `UNAUTHORIZED` | 401 | Authentication required | Please log in |
| `FORBIDDEN` | 403 | Access denied | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found | Resource not found |
| `CONFLICT` | 409 | Resource conflict | Resource already exists |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests | Please slow down |
| `INTERNAL_SERVER_ERROR` | 500 | Server error | Server error occurred |
| `SERVICE_UNAVAILABLE` | 503 | Service down | Service temporarily unavailable |
| `TIMEOUT` | 504 | Request timeout | Request timed out |

### Error Handling Best Practices

1. **Always Include Request ID**: For debugging and tracing
2. **Provide Actionable Messages**: User should know what to do
3. **Field-Level Errors**: Specify which field caused validation errors
4. **Logging**: Log all errors with request ID for debugging

---

## HTTP Methods

### GET
- Retrieve resources
- Should not modify server state
- Idempotent
- Cacheable

### POST
- Create new resources
- Non-idempotent (multiple calls create multiple resources)
- Not cacheable

### PUT
- Update/replace entire resource
- Idempotent
- Not cacheable

### PATCH
- Partial resource update
- May not be idempotent
- Not cacheable

### DELETE
- Remove resources
- Idempotent
- Not cacheable

---

## Status Codes

### 2xx Success

| Code | Usage | Example |
|------|-------|---------|
| 200 OK | Request successful | GET /api/v1/users/{id} |
| 201 Created | Resource created | POST /api/v1/users |
| 202 Accepted | Request accepted for processing | POST /api/v1/payments/initiate |
| 204 No Content | Successful with no response body | DELETE /api/v1/users/{id} |

### 4xx Client Errors

| Code | Usage | Example |
|------|-------|---------|
| 400 Bad Request | Invalid request | Missing required field |
| 401 Unauthorized | Not authenticated | Invalid/expired token |
| 403 Forbidden | Not authorized | Insufficient permissions |
| 404 Not Found | Resource not found | GET /api/v1/users/{id} |
| 409 Conflict | Resource conflict | Duplicate email |
| 422 Unprocessable Entity | Validation failed | Invalid email format |
| 429 Too Many Requests | Rate limit exceeded | Exceed request limit |

### 5xx Server Errors

| Code | Usage | Example |
|------|-------|---------|
| 500 Internal Server Error | Unexpected server error | Unhandled exception |
| 502 Bad Gateway | Invalid response from upstream | Downstream service error |
| 503 Service Unavailable | Service down temporarily | Maintenance mode |
| 504 Gateway Timeout | Upstream timeout | External service timeout |

---

## Pagination

### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Integer | 1 | Page number (1-indexed) |
| `pageSize` | Integer | 20 | Items per page (max 100) |
| `sort` | String | id | Sort field |
| `order` | String | asc | Sort order (asc/desc) |

### Pagination Metadata

```json
{
  "page": 1,
  "page_size": 20,
  "total_items": 100,
  "total_pages": 5,
  "has_next": true,
  "has_previous": false
}
```

### Pagination Best Practices

1. **Default Page Size**: 20 items (reasonable for most use cases)
2. **Maximum Page Size**: 100 items (prevent excessive data transfer)
3. **Default Sort**: Most logical field for resource (e.g., created_at)
4. **Include Navigation Flags**: `has_next`, `has_previous` for UI

### Example Request

```
GET /api/v1/users?page=1&pageSize=20&sort=name&order=asc
```

### Example Response

```json
{
  "data": [
    { "id": "1", "name": "Alice" },
    { "id": "2", "name": "Bob" }
  ],
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 100,
    "total_pages": 5,
    "has_next": true,
    "has_previous": false
  },
  "request_id": "uuid",
  "timestamp": 1234567890000
}
```

---

## Migration Plan

### Current State

- ✅ Endpoints use RESTful naming conventions
- ⚠️  Mixed query parameter and request body usage
- ❌ No API versioning
- ❌ Inconsistent response wrapper structure
- ✅ Standard error codes (NetworkError, ApiErrorCode)

### Phase 1: API Versioning (Immediate)

**Tasks**:
1. Add `/api/v1` prefix to all new endpoints
2. Document versioning strategy
3. Create compatibility layer for existing endpoints
4. Update API documentation

**Timeline**: Week 1

### Phase 2: Standardize Request Patterns (Week 2-3)

**Tasks**:
1. Replace multi-query parameter requests with request bodies
2. Create standardized request models
3. Update ApiService interface
4. Update repositories to use new request models

**Affected Endpoints**:
- `POST /vendors` - 10 query params → request body
- `PUT /vendors/{id}` - 11 query params → request body
- `POST /work-orders` - 7 query params → request body
- `POST /community-posts` - 4 query params → request body
- `POST /messages` - 3 query params → request body
- `POST /payments/initiate` - 4 query params → request body

### Phase 3: Standardize Response Wrappers (Week 4)

**Tasks**:
1. Implement ApiResponse<T> wrapper for single resources
2. Implement ApiListResponse<T> wrapper for collections
3. Add pagination metadata to list responses
4. Add request_id and timestamp to all responses
5. Update all endpoint responses to use new wrappers

**Affected Endpoints**:
- All endpoints returning single resources (Vendor, WorkOrder, etc.)
- All endpoints returning collections (Users, Messages, etc.)

### Phase 4: Client Migration (Week 5-6)

**Tasks**:
1. Update Android client to use versioned endpoints
2. Update client to use request bodies instead of query params
3. Update client to parse new response wrappers
4. Add backward compatibility layer for old client versions
5. Test migration with existing production data

### Phase 5: Deprecate Old Patterns (Week 7-8)

**Tasks**:
1. Add deprecation headers to old endpoints
2. Monitor usage of old endpoints
3. Set deprecation timeline (6 months)
4. Document sunset date
5. Communicate changes to stakeholders

### Phase 6: Remove Old Patterns (Month 6+)

**Tasks**:
1. Remove old endpoints after deprecation period
2. Remove backward compatibility layer
3. Cleanup unused code
4. Finalize migration documentation

---

## Success Criteria

- [x] API versioning strategy defined
- [x] API version constants added to client configuration
- [x] Standardized response wrapper models created (ApiResponse<T>, ApiListResponse<T>)
- [x] Enhanced error logging with request ID tracing
- [x] Naming conventions documented
- [x] Request/response patterns standardized
- [x] Error handling consistent across all endpoints
- [x] All endpoints use `/api/v1` prefix (ApiServiceV1 interface created)
- [x] All create/update endpoints use request bodies (legacy ApiService updated, V1 ready)
- [x] All responses use standardized wrappers (ApiServiceV1 fully standardized)
- [x] Pagination implemented for all list endpoints (model ready)
- [ ] Client migration to ApiServiceV1 complete (migration plan documented)
- [ ] Old patterns deprecated with clear timeline

---

## Client-Side Integration Improvements (2026-01-08)

### API Versioning Support
- Added `Constants.Api.API_VERSION = "v1"`
- Added `Constants.Api.API_VERSION_PREFIX = "api/v1/"`
- Client prepared for versioned endpoint migration
- Documentation of deprecation timeline strategy (6 months)

### Standardized Response Models
**New Models in `data/api/models/ApiResponse.kt`**:
- `ApiResponse<T>`: Wrapper for single resource responses
  - `data`: Resource payload
  - `request_id`: Request tracking identifier
  - `timestamp`: Response timestamp
  
- `ApiListResponse<T>`: Wrapper for collection responses
  - `data`: List of resources
  - `pagination`: Pagination metadata (page, page_size, total_items, etc.)
  - `request_id`: Request tracking identifier
  - `timestamp`: Response timestamp
  
- `PaginationMetadata`: Pagination information
  - `page`: Current page number
  - `page_size`: Items per page
  - `total_items`: Total number of items
  - `total_pages`: Total number of pages
  - `has_next`: Next page available flag
  - `has_previous`: Previous page available flag
  - `isFirstPage`: Helper for first page detection
  - `isLastPage`: Helper for last page detection
  
- `ApiErrorResponse`: Standardized error response
  - `error`: ApiErrorDetail with code, message, details, field
  - `request_id`: Request tracking identifier
  - `timestamp`: Error timestamp
  
- `ApiErrorDetail`: Detailed error information
  - `code`: Error code string
  - `message`: User-friendly error message
  - `details`: Additional error details
  - `field`: Field name for validation errors
  - `toDisplayMessage()`: Helper for formatted error messages

### Enhanced Error Logging
**Improved `ErrorHandler` with**:
- `ErrorContext` data class for structured error context
  - `requestId`: Request identifier from X-Request-ID header
  - `endpoint`: API endpoint being called
  - `httpCode`: HTTP status code
  - `timestamp`: Error timestamp
  
- Enhanced error categorization:
  - Circuit breaker errors (SERVICE_UNAVAILABLE)
  - HTTP 408 (Request Timeout)
  - HTTP 429 (Too Many Requests)
  - HTTP 502 (Bad Gateway)
  - HTTP 503 (Service Unavailable)
  - HTTP 504 (Gateway Timeout)
  
- Structured error logging:
  - Request ID tracing for debugging
  - Endpoint information for context
  - HTTP code for correlation
  - Error body extraction for details
  - Log level differentiation (WARN for 4xx, ERROR for 5xx)
  
- New utility method:
  - `toNetworkError(throwable)`: Converts any Throwable to NetworkError

### New Test Coverage
**ApiResponseTest.kt**: 5 test cases
- ApiResponse success and successWithMetadata
- ApiListResponse success and successWithMetadata

**PaginationMetadataTest.kt**: 4 test cases
- isFirstPage detection
- isLastPage detection

**ApiErrorDetailTest.kt**: 3 test cases
- toDisplayMessage() with different error detail scenarios

**ErrorHandlerEnhancedTest.kt**: 17 test cases
- All HTTP error codes (400, 401, 403, 404, 408, 429, 500, 503)
- Network exceptions (UnknownHostException, SocketTimeoutException, IOException)
- Circuit breaker exceptions
- Generic exceptions
- Error context logging
- toNetworkError() conversions

### New Logging Tags
Added to `Constants.Tags`:
- `ERROR_HANDLER`: Enhanced error handler logs
- `API_CLIENT`: API client operations
- `CIRCUIT_BREAKER`: Circuit breaker state changes
- `RATE_LIMITER`: Rate limiter statistics

### Benefits
1. **API Versioning Ready**: Client prepared for migration to `/api/v1` endpoints
2. **Consistent Error Handling**: User-friendly messages for all error types
3. **Request Tracing**: Every error logged with request ID for debugging
4. **Type-Safe Responses**: Standardized wrappers with compile-time safety
5. **Pagination Support**: Ready for paginated list responses
6. **Test Coverage**: 29 new test cases for response models and error handling
7. **Backward Compatible**: No breaking changes to existing code

---

*Last Updated: 2026-01-08*
*Version: 1.1.0*
*Maintained by: Integration Engineer*
