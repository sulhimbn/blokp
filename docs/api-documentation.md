# API Documentation

## Overview

IuranKomplek API v1.0 provides a RESTful interface for managing apartment complex membership dues, financial records, communications, vendors, work orders, and payments. The API implements modern resilience patterns including circuit breakers, rate limiting, retry logic, and idempotency guarantees.

## Base Configuration

### Production API
- **Base URL**: `https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/`
- **Protocol**: HTTPS
- **Data Format**: JSON
- **API Version**: v1.0.0

### Development API (Mock)
- **Base URL**: `http://api-mock:5000/data/QjX6hB1ST2IDKaxB/`
- **Protocol**: HTTP
- **Data Format**: JSON
- **Environment**: Docker development environment

### Standard Headers
| Header | Description | Example |
|--------|-------------|---------|
| `Content-Type` | Request content type | `application/json` |
| `Accept` | Response content type | `application/json` |
| `X-Request-ID` | Unique request identifier (auto-generated) | `req_1234567890_abc42` |
| `X-Idempotency-Key` | Idempotency key for safe retries (POST/PUT/DELETE/PATCH) | `idk_1704672000000_12345` |
| `X-Priority` | Request priority level (auto-generated) | `HIGH` |
| `Accept-Encoding` | Response compression preference | `gzip` |

## API Endpoints

### Tags
- **Users** - User management operations
- **Financial** - Financial records operations
- **Communications** - Messages and announcements
- **Community** - Community posts and discussions
- **Payments** - Payment processing and management
- **Vendors** - Vendor management
- **Work Orders** - Work order management
- **Health** - Health monitoring and diagnostics

---

## 1. Users

### GET /api/v1/users
Get all registered users with pagination support.

**Request:**
```http
GET /api/v1/users?page=1&page_size=20
Content-Type: application/json
```

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|----------|-------------|
| `page` | integer | No | 1 | Page number (minimum: 1) |
| `page_size` | integer | No | 20 | Items per page (1-100) |

**Response (200 OK):**
```json
{
  "data": {
    "users": [
      {
        "id": "user_123",
        "first_name": "John",
        "last_name": "Doe",
        "email": "john.doe@example.com",
        "alamat": "Jl. Merdeka No. 123, Jakarta",
        "avatar": "https://example.com/avatar.jpg"
      }
    ]
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

**Error Responses:**
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server processing error
- `503 Service Unavailable`: Circuit breaker open

---

## 2. Financial Data

### GET /api/v1/pemanfaatan
Get financial records (pemanfaatan iuran) with pagination support.

**Request:**
```http
GET /api/v1/pemanfaatan?page=1&page_size=20
Content-Type: application/json
```

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|----------|-------------|
| `page` | integer | No | 1 | Page number (minimum: 1) |
| `page_size` | integer | No | 20 | Items per page (1-100) |

**Response (200 OK):**
```json
{
  "data": {
    "financial_records": [
      {
        "id": "fin_123",
        "user_id": "user_123",
        "iuran_perwarga": 150000,
        "total_iuran_rekap": 1800000,
        "jumlah_iuran_bulanan": 150000,
        "total_iuran_individu": 150000,
        "pengeluaran_iuran_warga": 50000,
        "pemanfaatan_iuran": "Perbaikan jalan komplek"
      }
    ]
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

**Error Responses:**
- `400 Bad Request`: Invalid request parameters
- `500 Internal Server Error`: Server processing error
- `503 Service Unavailable`: Circuit breaker open

---

## 3. Communications

### GET /api/v1/announcements
Get community announcements with pagination support.

**Request:**
```http
GET /api/v1/announcements?page=1&page_size=20
Content-Type: application/json
```

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|----------|-------------|
| `page` | integer | No | 1 | Page number (minimum: 1) |
| `page_size` | integer | No | 20 | Items per page (1-100) |

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": "ann_123",
      "title": "Pemberitahuan Pemeliharaan",
      "content": "Akan ada pemeliharaan fasilitas kolam renang pada tanggal...",
      "created_at": 1704672000000
    }
  ],
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 50,
    "total_pages": 3,
    "has_next": true,
    "has_previous": false
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

### GET /api/v1/messages
Get messages for a specific user.

**Request:**
```http
GET /api/v1/messages?userId=user_123&page=1&page_size=20
Content-Type: application/json
```

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|----------|-------------|
| `userId` | string | Yes | - | User ID to retrieve messages for |
| `page` | integer | No | 1 | Page number |
| `page_size` | integer | No | 20 | Items per page |

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": "msg_123",
      "sender_id": "user_123",
      "receiver_id": "user_456",
      "content": "Halo, apa kabar?",
      "timestamp": 1704672000000
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
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

### POST /api/v1/messages
Send a new message.

**Request:**
```http
POST /api/v1/messages
Content-Type: application/json
X-Idempotency-Key: idk_1704672000000_12345

{
  "sender_id": "user_123",
  "receiver_id": "user_456",
  "content": "Halo, apa kabar?"
}
```

**Request Body:**
```json
{
  "sender_id": "string",
  "receiver_id": "string",
  "content": "string"
}
```

**Response (200 OK):**
```json
{
  "data": {
    "id": "msg_123",
    "sender_id": "user_123",
    "receiver_id": "user_456",
    "content": "Halo, apa kabar?",
    "timestamp": 1704672000000
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

### GET /api/v1/messages/{receiverId}
Get conversation between two users.

**Request:**
```http
GET /api/v1/messages/user_456?senderId=user_123
Content-Type: application/json
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `receiverId` | string | Yes (path) | Receiver user ID |
| `senderId` | string | Yes (query) | Sender user ID |

---

## 4. Community Posts

### GET /api/v1/community-posts
Get community posts with pagination support.

**Request:**
```http
GET /api/v1/community-posts?page=1&page_size=20
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": "post_123",
      "author_id": "user_123",
      "title": "Community Garden Cleanup",
      "content": "Let's clean up community garden this Saturday!",
      "category": "INFO",
      "likes": 15,
      "comments": [],
      "created_at": "2024-01-08T10:30:00Z"
    }
  ],
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 30,
    "total_pages": 2,
    "has_next": true,
    "has_previous": false
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

### POST /api/v1/community-posts
Create a new community post.

**Request:**
```http
POST /api/v1/community-posts
Content-Type: application/json
X-Idempotency-Key: idk_1704672000000_12345

{
  "author_id": "user_123",
  "title": "Community Garden Cleanup",
  "content": "Let's clean up community garden this Saturday!",
  "category": "INFO"
}
```

**Response (200 OK):**
```json
{
  "data": {
    "id": "post_123",
    "author_id": "user_123",
    "title": "Community Garden Cleanup",
    "content": "Let's clean up community garden this Saturday!",
    "category": "INFO",
    "likes": 0,
    "comments": [],
    "created_at": "2024-01-08T10:30:00Z"
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

## 5. Payments

### POST /api/v1/payments/initiate
Initiate a new payment transaction.

**Request:**
```http
POST /api/v1/payments/initiate
Content-Type: application/json
X-Idempotency-Key: idk_1704672000000_12345

{
  "amount": "150000",
  "description": "Pembayaran iuran bulanan",
  "customer_id": "user_123",
  "payment_method": "BANK_TRANSFER"
}
```

**Request Body:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `amount` | string | Yes | Payment amount in IDR |
| `description` | string | Yes | Payment description |
| `customer_id` | string | Yes | Customer ID |
| `payment_method` | enum | Yes | CREDIT_CARD, BANK_TRANSFER, E_WALLET, VIRTUAL_ACCOUNT |

**Response (200 OK):**
```json
{
  "data": {
    "transaction_id": "txn_abc123",
    "status": "PENDING",
    "amount": 150000.0,
    "payment_method": "BANK_TRANSFER",
    "created_at": 1704672000000
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

### GET /api/v1/payments/{id}/status
Get payment transaction status.

**Request:**
```http
GET /api/v1/payments/txn_abc123/status
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": {
    "transaction_id": "txn_abc123",
    "status": "COMPLETED",
    "amount": 150000.0,
    "updated_at": 1704672001000
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

### POST /api/v1/payments/{id}/confirm
Confirm a completed payment transaction.

**Request:**
```http
POST /api/v1/payments/txn_abc123/confirm
Content-Type: application/json
X-Idempotency-Key: idk_1704672000000_12345
```

**Response (200 OK):**
```json
{
  "data": {
    "transaction_id": "txn_abc123",
    "status": "COMPLETED",
    "confirmed_at": 1704672002000
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

## 6. Vendors

### GET /api/v1/vendors
Get all vendors.

**Request:**
```http
GET /api/v1/vendors?page=1&page_size=20
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": {
    "vendors": [
      {
        "id": "vendor_123",
        "name": "Vendor ABC",
        "contact": "08123456789",
        "address": "Jl. Contoh No. 123",
        "services": ["Perbaikan", "Kebersihan"]
      }
    ]
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

### POST /api/v1/vendors
Create a new vendor.

**Request:**
```http
POST /api/v1/vendors
Content-Type: application/json
X-Idempotency-Key: idk_1704672000000_12345

{
  "name": "Vendor ABC",
  "contact": "08123456789",
  "address": "Jl. Contoh No. 123",
  "services": ["Perbaikan", "Kebersihan"]
}
```

---

### GET /api/v1/vendors/{id}
Get vendor details.

**Request:**
```http
GET /api/v1/vendors/vendor_123
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": {
    "vendor": {
      "id": "vendor_123",
      "name": "Vendor ABC",
      "contact": "08123456789",
      "address": "Jl. Contoh No. 123",
      "services": ["Perbaikan", "Kebersihan"]
    }
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

### PUT /api/v1/vendors/{id}
Update an existing vendor.

**Request:**
```http
PUT /api/v1/vendors/vendor_123
Content-Type: application/json
X-Idempotency-Key: idk_1704672000000_12345

{
  "name": "Vendor ABC Updated",
  "contact": "08123456789",
  "address": "Jl. Contoh No. 456",
  "services": ["Perbaikan", "Kebersihan", "Pemeliharaan"]
}
```

---

## 7. Work Orders

### GET /api/v1/work-orders
Get all work orders.

**Request:**
```http
GET /api/v1/work-orders?page=1&page_size=20
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": {
    "work_orders": [
      {
        "id": "wo_123",
        "title": "Perbaikan AC Unit",
        "description": "AC unit di lobi tidak berfungsi dengan baik",
        "status": "PENDING",
        "priority": "HIGH",
        "created_at": 1704672000000,
        "vendor_id": null,
        "vendor_name": null
      }
    ]
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

---

### POST /api/v1/work-orders
Create a new work order.

**Request:**
```http
POST /api/v1/work-orders
Content-Type: application/json
X-Idempotency-Key: idk_1704672000000_12345

{
  "title": "Perbaikan AC Unit",
  "description": "AC unit di lobi tidak berfungsi dengan baik",
  "priority": "HIGH"
}
```

---

### GET /api/v1/work-orders/{id}
Get work order details.

**Request:**
```http
GET /api/v1/work-orders/wo_123
Content-Type: application/json
```

---

### PUT /api/v1/work-orders/{id}/assign
Assign a vendor to a work order.

**Request:**
```http
PUT /api/v1/work-orders/wo_123/assign
Content-Type: application/json
X-Idempotency-Key: idk_1704672000000_12345

{
  "vendor_id": "vendor_456"
}
```

---

### PUT /api/v1/work-orders/{id}/status
Update work order status.

**Request:**
```http
PUT /api/v1/work-orders/wo_123/status
Content-Type: application/json
X-Idempotency-Key: idk_1704672000000_12345

{
  "status": "IN_PROGRESS"
}
```

---

## 8. Health Check

### POST /api/v1/health
Check system health and integration status.

**Request:**
```http
POST /api/v1/health
Content-Type: application/json

{
  "includeDiagnostics": false,
  "includeMetrics": false
}
```

**Request Body:**
| Field | Type | Required | Default | Description |
|-------|------|----------|----------|-------------|
| `includeDiagnostics` | boolean | No | false | Include detailed circuit breaker and rate limiter diagnostics |
| `includeMetrics` | boolean | No | false | Include performance metrics |

**Response (200 OK):**
```json
{
  "data": {
    "status": "HEALTHY",
    "version": "1.0.0",
    "uptimeMs": 86400000,
    "components": {
      "circuit_breaker": {
        "status": "HEALTHY",
        "healthy": true,
        "message": "All integration systems operational"
      },
      "rate_limiter": {
        "status": "HEALTHY",
        "healthy": true,
        "message": "Rate limiter within normal limits"
      },
      "api_service": {
        "status": "HEALTHY",
        "healthy": true,
        "message": "API service operational"
      },
      "network": {
        "status": "HEALTHY",
        "healthy": true,
        "message": "Network connectivity normal"
      }
    },
    "timestamp": 1704672000000,
    "diagnostics": {
      "circuitBreakerState": "CLOSED",
      "circuitBreakerFailures": 0,
      "rateLimitStats": {
        "GET:/api/v1/users": {
          "requestCount": 50,
          "lastRequestTime": 1704671995000
        }
      }
    },
    "metrics": {
      "healthScore": 95.5,
      "totalRequests": 100,
      "successRate": 95.0,
      "averageResponseTimeMs": 150.0,
      "errorRate": 5.0,
      "timeoutCount": 1,
      "rateLimitViolations": 0
    }
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

**Health Status Values:**
| Status | Description | Action |
|--------|-------------|---------|
| `HEALTHY` | All systems operational | Continue normal monitoring |
| `DEGRADED` | Some components degraded | Investigate degraded components |
| `UNHEALTHY` | Critical systems failing | Immediate investigation required |
| `CIRCUIT_OPEN` | Circuit breaker is open | Check service availability |
| `RATE_LIMITED` | Rate limit exceeded | Reduce request rate |

---

## Error Responses

### Standard Error Format

All error responses follow this format:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": "Additional error details",
    "field": "field_name"
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

### HTTP Status Codes

| Code | Error Code | Description | Retryable |
|------|------------|-------------|------------|
| 400 | BAD_REQUEST | Invalid request parameters | No |
| 401 | UNAUTHORIZED | Authentication required | No |
| 403 | FORBIDDEN | Access denied | No |
| 404 | NOT_FOUND | Resource not found | No |
| 409 | CONFLICT | Resource conflict | No |
| 422 | VALIDATION_ERROR | Validation failed | No |
| 429 | RATE_LIMIT_EXCEEDED | Too many requests | Yes (with backoff) |
| 500 | INTERNAL_SERVER_ERROR | Server error | Yes (with backoff) |
| 502 | BAD_GATEWAY | Gateway error | Yes (with backoff) |
| 503 | SERVICE_UNAVAILABLE | Circuit breaker open | Yes (wait for recovery) |
| 504 | TIMEOUT | Request timeout | Yes (with backoff) |

### Error Codes

| Error Code | Description | HTTP Code |
|------------|-------------|------------|
| `BAD_REQUEST` | Invalid request parameters | 400 |
| `UNAUTHORIZED` | Authentication required | 401 |
| `FORBIDDEN` | Access denied | 403 |
| `NOT_FOUND` | Resource not found | 404 |
| `CONFLICT` | Resource conflict (e.g., duplicate) | 409 |
| `VALIDATION_ERROR` | Input validation failed | 422 |
| `RATE_LIMIT_EXCEEDED` | Rate limit exceeded | 429 |
| `INTERNAL_SERVER_ERROR` | Server error | 500 |
| `SERVICE_UNAVAILABLE` | Service unavailable (circuit breaker) | 503 |
| `TIMEOUT` | Request timeout | 504 |
| `NETWORK_ERROR` | Network connectivity issue | N/A (client-side) |
| `UNKNOWN_ERROR` | Unknown error | N/A |

---

## Integration Patterns

### 1. Circuit Breaker

The API implements a circuit breaker pattern to prevent cascading failures:

**States:**
- **CLOSED**: Normal operation, all requests pass through
- **OPEN**: Service is failing, requests blocked immediately
- **HALF_OPEN**: Testing if service has recovered (limited requests allowed)

**Configuration:**
- Failure Threshold: 5 consecutive failures
- Success Threshold: 2 successful requests to reset to CLOSED
- Timeout: 60 seconds before attempting reset from OPEN
- Half-Open Max Calls: 3 calls in HALF_OPEN state

**Behavior:**
- When circuit is OPEN, requests return 503 SERVICE_UNAVAILABLE
- Automatic recovery detection after timeout
- Request ID tracking for debugging

### 2. Rate Limiting

The API implements client-side rate limiting:

**Limits:**
- 10 requests per second
- 60 requests per minute

**Behavior:**
- Requests exceeding limits return 429 RATE_LIMIT_EXCEEDED
- Automatic reset per second/minute
- Per-endpoint statistics tracking

**Headers:**
- Requests are tracked with sliding window algorithm
- All endpoints subject to same limits (except health check)

### 3. Retry Logic

Automatic retry with exponential backoff:

**Retryable Errors:**
- Network errors (IOException, SocketTimeoutException, UnknownHostException)
- 500 Internal Server Error
- 502 Bad Gateway
- 504 Timeout

**Retry Configuration:**
- Max Retries: 3 attempts
- Initial Delay: 1000ms (1 second)
- Max Delay: 30000ms (30 seconds)
- Backoff Multiplier: 2.0 (exponential)
- Jitter: 500ms random variation

**Non-Retryable Operations:**
- POST (create operations) - use idempotency keys instead
- PUT (update operations)
- DELETE (delete operations)
- PATCH (partial updates)

### 4. Idempotency

All non-GET requests include idempotency keys:

**Idempotency Key Format:**
```
idk_{timestamp}_{random}
```

**Example:**
```
X-Idempotency-Key: idk_1704672000000_12345
```

**Behavior:**
- Server caches response for first request with idempotency key
- Subsequent requests with same key return cached response
- Prevents duplicate data creation on retry

**Covered Operations:**
- All POST requests (create operations)
- All PUT requests (update operations)
- All DELETE requests
- All PATCH requests (partial updates)

### 5. Request Prioritization

Requests are assigned priority levels:

**Priority Levels:**
| Priority | Level | Use Cases |
|----------|-------|------------|
| CRITICAL | 1 | Payment confirmations, authentication, health checks |
| HIGH | 2 | User-initiated write operations |
| NORMAL | 3 | Standard data refresh |
| LOW | 4 | Non-critical reads |
| BACKGROUND | 5 | Background operations |

**Behavior:**
- Requests processed in priority order (CRITICAL first)
- FIFO order within each priority level
- Automatic priority assignment based on endpoint and HTTP method

### 6. Request Compression

Gzip compression for request/response bodies:

**Compressible Content Types:**
- `text/*` - Plain text
- `application/json` - JSON data
- `application/xml` - XML data
- `application/javascript` - JavaScript
- `application/x-www-form-urlencoded` - URL-encoded form data

**Configuration:**
- Minimum size to compress: 1024 bytes (1KB)
- Automatic compression for requests >= 1KB
- Automatic decompression of gzip responses

**Performance:**
- ~60-80% bandwidth reduction for text/JSON payloads
- Faster response times with less data transfer

### 7. Timeouts

Per-operation timeout configuration:

**Timeout Profiles:**
| Profile | Timeout | Endpoints |
|---------|----------|-----------|
| FAST | 5 seconds | Health checks, status checks |
| NORMAL | 30 seconds | Users, vendors, announcements, messages, posts |
| SLOW | 60 seconds | Payment initiation |

**Behavior:**
- Per-request timeout based on endpoint path
- Automatic profile mapping via timeout config
- Chain methods override client timeouts

---

## Pagination

Pagination is supported for list endpoints:

**Request Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|----------|-------------|
| `page` | integer | No | 1 | Page number (1-indexed, minimum: 1) |
| `page_size` | integer | No | 20 | Items per page (1-100) |

**Response Metadata:**
```json
{
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 50,
    "total_pages": 3,
    "has_next": true,
    "has_previous": false
  }
}
```

---

## Request Tracking

All requests include a unique identifier:

**Request ID Format:**
```
{timestamp}-{random}
```

**Example:**
```
X-Request-ID: req_1234567890_abc42
```

**Benefits:**
- Traceable request lifecycle
- Debug distributed systems
- Correlate logs across services
- Error tracking and monitoring

---

## Security

### Certificate Pinning
```kotlin
CertificatePinningConfig.getCertificatePinner()
```

### Network Security
- HTTPS enforced in production
- Certificate pinning with backup pins
- Encrypted storage for sensitive data

---

## Best Practices

### For API Consumers

1. **Handle Rate Limits**: Always catch 429 errors and implement exponential backoff
2. **Check Circuit Breaker**: Respect 503 SERVICE_UNAVAILABLE errors
3. **Use Request IDs**: Log X-Request-ID from responses for debugging
4. **Implement Idempotency**: Include X-Idempotency-Key for all write operations
5. **Handle Timeouts**: Set appropriate timeouts for your use case
6. **Use Pagination**: Process large datasets with pagination
7. **Validate Inputs**: Validate request parameters before sending

### For Error Handling

```kotlin
when (error.code) {
    "RATE_LIMIT_EXCEEDED" -> {
        // Wait with exponential backoff
        delay(calculateBackoff(retryCount))
        retry()
    }
    "SERVICE_UNAVAILABLE" -> {
        // Circuit breaker open, wait for recovery
        delay(60000) // 60 seconds
        retry()
    }
    "NETWORK_ERROR" -> {
        // Check network connectivity
        if (!isNetworkAvailable()) {
            showNetworkError()
        }
    }
    else -> {
        // Show error message to user
        showError(error.message)
    }
}
```

---

## Testing

### Mock API Server
See `docker-compose.yml` for mock server configuration.

### Integration Tests
Run with: `./gradlew connectedAndroidTest`

---

## Versioning

**Current Version**: v1.0.0

**Breaking Changes**: None (current version)

**Migration Guide**: See `docs/API_MIGRATION_GUIDE.md`

---

*Last Updated: 2026-01-11*
*Maintained by: Integration Engineer*
