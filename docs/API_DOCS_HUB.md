# API Documentation Hub

Welcome to the IuranKomplek API Documentation Hub. This hub provides comprehensive documentation for all API interfaces, integration patterns, and versioning strategies.

## Quick Start

- **New Integration?** Start with [API Standardization Guide](API_STANDARDIZATION.md)
- **Need Reference?** Check [OpenAPI Specification](openapi.yaml)
- **Migrating to V1?** See [Migration Guide](API_MIGRATION_GUIDE.md)
- **Troubleshooting?** Review [Troubleshooting Guide](TROUBLESHOOTING.md)

## Core Documentation

| Document | Description | Status |
|----------|-------------|--------|
| **[OpenAPI Specification](openapi.yaml)** | Complete API contract with all endpoints, schemas, and examples | ✅ Complete (21/21 endpoints documented - INT-005 - 2026-01-11) |
| **[API Documentation](API.md)** | Comprehensive API documentation with implementation examples | ✅ Complete |
| **[API Standardization Guide](API_STANDARDIZATION.md)** | API design patterns, naming conventions, and best practices | ✅ Version 1.1.0 |
| **[API Migration Guide](API_MIGRATION_GUIDE.md)** | Migration plan from legacy API to API v1 | 🔄 In Progress |

## API Versions

### Legacy API (Deprecated)
- **Base URL**: `https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/`
- **Status**: Production use (deprecated)
- **Deprecation Date**: TBD
- **Interface**: `ApiService` (Kotlin)
- **Response Format**: Direct object responses
- **Documentation**: [API.md](API.md#legacy-api)
- **Migration**: See [Migration Guide](API_MIGRATION_GUIDE.md)

### API v1 (Recommended)
- **Base URL**: `https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/api/v1/`
- **Status**: Ready for migration
- **Interface**: `ApiServiceV1` (Kotlin)
- **Response Format**: Standardized wrappers (`ApiResponse<T>`, `ApiListResponse<T>`)
- **Features**: Pagination, request ID tracing, error details
- **Documentation**: [API Standardization Guide](API_STANDARDIZATION.md)
- **OpenAPI**: [openapi.yaml](openapi.yaml) (v1 endpoints)

**Recommendation**: Use API v1 for all new integrations. Migrate existing integrations using the [Migration Guide](API_MIGRATION_GUIDE.md).

## API Endpoints Overview

### Users
| Endpoint | Method | Description | Version |
|----------|--------|-------------|---------|
| `/users` | GET | Get all users | Legacy |
| `/api/v1/users` | GET | Get all users (paginated) | v1 |
| `/users/{id}` | GET | Get user by ID | Planned |

### Financial
| Endpoint | Method | Description | Version |
|----------|--------|-------------|---------|
| `/pemanfaatan` | GET | Get financial records | Legacy |
| `/api/v1/pemanfaatan` | GET | Get financial records (paginated) | v1 |

### Vendors
| Endpoint | Method | Description | Version |
|----------|--------|-------------|---------|
| `/vendors` | GET | Get all vendors | Legacy |
| `/vendors` | POST | Create vendor | Legacy |
| `/vendors/{id}` | GET | Get vendor by ID | Legacy |
| `/vendors/{id}` | PUT | Update vendor | Legacy |
| `/api/v1/vendors` | GET | Get all vendors (paginated) | v1 |
| `/api/v1/vendors` | POST | Create vendor (v1) | v1 |
| `/api/v1/vendors/{id}` | GET | Get vendor by ID (v1) | v1 |
| `/api/v1/vendors/{id}` | PUT | Update vendor (v1) | v1 |

### Work Orders
| Endpoint | Method | Description | Version |
|----------|--------|-------------|---------|
| `/work-orders` | GET | Get all work orders | Legacy |
| `/work-orders` | POST | Create work order | Legacy |
| `/work-orders/{id}` | GET | Get work order by ID | Legacy |
| `/work-orders/{id}/assign` | PUT | Assign vendor to work order | Legacy |
| `/work-orders/{id}/status` | PUT | Update work order status | Legacy |
| `/api/v1/work-orders` | GET | Get all work orders (paginated) | v1 |
| `/api/v1/work-orders` | POST | Create work order (v1) | v1 |
| `/api/v1/work-orders/{id}` | GET | Get work order by ID (v1) | v1 |
| `/api/v1/work-orders/{id}/assign` | PUT | Assign vendor (v1) | v1 |
| `/api/v1/work-orders/{id}/status` | PUT | Update status (v1) | v1 |

### Payments
| Endpoint | Method | Description | Version |
|----------|--------|-------------|---------|
| `/payments/initiate` | POST | Initiate payment | Legacy |
| `/payments/{id}/status` | GET | Get payment status | Legacy |
| `/payments/{id}/confirm` | POST | Confirm payment | Legacy |
| `/api/v1/payments/initiate` | POST | Initiate payment (v1) | v1 |
| `/api/v1/payments/{id}/status` | GET | Get payment status (v1) | v1 |
| `/api/v1/payments/{id}/confirm` | POST | Confirm payment (v1) | v1 |

### Communication
| Endpoint | Method | Description | Version |
|----------|--------|-------------|---------|
| `/announcements` | GET | Get announcements | Legacy |
| `/messages` | GET | Get messages | Legacy |
| `/messages` | POST | Send message | Legacy |
| `/messages/{receiverId}` | GET | Get conversation | Legacy |
| `/community-posts` | GET | Get community posts | Legacy |
| `/community-posts` | POST | Create community post | Legacy |
| `/api/v1/announcements` | GET | Get announcements (paginated) | v1 |
| `/api/v1/messages` | GET | Get messages (paginated) | v1 |
| `/api/v1/messages` | POST | Send message (v1) | v1 |
| `/api/v1/messages/{receiverId}` | GET | Get conversation (paginated) | v1 |
| `/api/v1/community-posts` | GET | Get posts (paginated) | v1 |
| `/api/v1/community-posts` | POST | Create post (v1) | v1 |

## Response Format Comparison

### Legacy Response Format
```json
{
  "data": [
    {
      "id": "1",
      "name": "John Doe",
      "email": "john@example.com"
    }
  ]
}
```

### API v1 Response Format (Single Resource)
```json
{
  "data": {
    "id": "1",
    "name": "John Doe",
    "email": "john@example.com"
  },
  "request_id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000
}
```

### API v1 Response Format (Collection with Pagination)
```json
{
  "data": [
    {
      "id": "1",
      "name": "John Doe",
      "email": "john@example.com"
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

## Error Responses

All APIs return standardized error responses:

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

For detailed error codes, see [Error Code Reference](#error-code-reference).

## Authentication

### API Key Authentication
```http
GET /api/v1/users
X-API-Key: your-api-key
```

### JWT Bearer Token (Future)
```http
GET /api/v1/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Integration Patterns

### Circuit Breaker
- **Implementation**: `CircuitBreaker` class in `network/resilience/`
- **Configuration**: 3 failures trigger open, 2 successes reset to closed
- **Documentation**: [API Integration Patterns](API_INTEGRATION_PATTERNS.md)

### Rate Limiting
- **Limits**: 10 requests/second, 60 requests/minute
- **Implementation**: `RateLimiterInterceptor` in `network/interceptor/`
- **Response**: HTTP 429 with `Retry-After` header

### Retry Logic
- **Strategy**: Exponential backoff with jitter
- **Max Retries**: 3 attempts
- **Initial Delay**: 1 second
- **Max Delay**: 30 seconds

## Testing

### Unit Tests
- Location: `app/src/test/java/com/example/iurankomplek/`
- Run: `./gradlew test`
- Coverage: JaCoCo reports in `build/reports/jacoco/`

### Integration Tests
- Location: `app/src/androidTest/java/com/example/iurankomplek/`
- Run: `./gradlew connectedAndroidTest`

### Mock Server Testing
- Location: `docker-compose.yml`
- Run: `docker-compose up api-mock`
- Base URL: `http://api-mock:5000/data/QjX6hB1ST2IDKaxB/`

## Support

### Troubleshooting
Common issues and solutions:
- [Troubleshooting Guide](TROUBLESHOOTING.md)
- [Error Codes](#error-code-reference)
- [Network Issues](TROUBLESHOOTING.md#common-network-issues)

### Getting Help
1. Check existing documentation in this hub
2. Review [API Standardization Guide](API_STANDARDIZATION.md)
3. Consult [OpenAPI Specification](openapi.yaml)
4. Check [Migration Guide](API_MIGRATION_GUIDE.md) for version issues
5. Report issues via GitHub Issues

## Error Code Reference

| Code | HTTP | Description | User Message |
|------|------|-------------|---------------|
| `VALIDATION_ERROR` | 400 | Request validation failed | Invalid input data |
| `UNAUTHORIZED` | 401 | Authentication required | Please log in |
| `FORBIDDEN` | 403 | Access denied | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found | Resource not found |
| `CONFLICT` | 409 | Resource conflict | Resource already exists |
| `RATE_LIMIT_EXCEEDED` | 429 | Rate limit exceeded | Please slow down |
| `INTERNAL_SERVER_ERROR` | 500 | Server error | Server error occurred |
| `SERVICE_UNAVAILABLE` | 503 | Service unavailable | Service temporarily unavailable |
| `TIMEOUT` | 504 | Request timeout | Request timed out |
| `NETWORK_ERROR` | N/A | Network connection error | No internet connection |
| `CIRCUIT_BREAKER_ERROR` | N/A | Circuit breaker open | Service temporarily unavailable |
| `UNKNOWN_ERROR` | N/A | Unexpected error | An unknown error occurred |

## Best Practices

1. **Use API v1**: Always prefer API v1 for new implementations
2. **Handle Errors**: Always implement error handling for all error codes
3. **Use Pagination**: For list endpoints, use pagination for large datasets
4. **Request ID**: Log request IDs for debugging and tracing
5. **Retry Logic**: Implement exponential backoff for retryable errors
6. **Rate Limiting**: Respect rate limits and implement client-side throttling
7. **Caching**: Cache responses appropriately to reduce API calls
8. **Versioning**: Track API version in your client for future compatibility

## Changelog

### 2026-01-08
- ✅ Enhanced OpenAPI specification with API v1 endpoints
- ✅ Added standardized response wrappers (`ApiResponse<T>`, `ApiListResponse<T>`)
- ✅ Added pagination metadata schema
- ✅ Created API Documentation Hub for unified access
- ✅ Documented API versioning strategy

### 2025-11-xx
- Initial API documentation
- Legacy API endpoints documented

---

*Last Updated: 2026-01-08*
*Maintained by: Integration Engineer*
*API Version: 1.0.0 (Legacy), 1.1.0 (v1)*
