# API Endpoint Catalog

Complete catalog of all IuranKomplek API endpoints with request/response schemas, examples, and implementation details.

## Table of Contents

- [Users](#users-endpoints)
- [Financial](#financial-endpoints)
- [Vendors](#vendor-endpoints)
- [Work Orders](#work-order-endpoints)
- [Payments](#payment-endpoints)
- [Communication](#communication-endpoints)

---

## Users Endpoints

### Get All Users

#### Legacy API

**Endpoint**: `GET /users`

**Description**: Retrieves list of all users/residents in the HOA system.

**Headers**:
| Header | Type | Required | Description |
|--------|------|----------|-------------|
| X-Request-ID | string (UUID) | No | Request identifier for tracing |
| X-Client-Version | string | No | Client application version |

**Query Parameters**: None

**Request Example**:
```http
GET /users HTTP/1.1
Host: api.apispreadsheets.com
Accept: application/json
```

**Response Schema**:
```json
{
  "data": [
    {
      "first_name": "string",
      "last_name": "string",
      "email": "string",
      "alamat": "string",
      "iuran_perwarga": 0,
      "total_iuran_rekap": 0,
      "jumlah_iuran_bulanan": 0,
      "total_iuran_individu": 0,
      "pengeluaran_iuran_warga": 0,
      "pemanfaatan_iuran": "string",
      "avatar": "string"
    }
  ]
}
```

**Response Example**:
```json
{
  "data": [
    {
      "first_name": "John",
      "last_name": "Doe",
      "email": "john.doe@example.com",
      "alamat": "Jl. Merdeka No. 123, Jakarta",
      "iuran_perwarga": 500000,
      "total_iuran_rekap": 1500000,
      "jumlah_iuran_bulanan": 500000,
      "total_iuran_individu": 1500000,
      "pengeluaran_iuran_warga": 200000,
      "pemanfaatan_iuran": "Maintenance fasilitas umum",
      "avatar": "https://example.com/avatar.jpg"
    }
  ]
}
```

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Success |
| 404 | Endpoint not available |
| 500 | Internal server error |

#### API v1

**Endpoint**: `GET /api/v1/users`

**Description**: Retrieves list of all users with standardized response wrapper and optional pagination.

**Headers**:
| Header | Type | Required | Description |
|--------|------|----------|-------------|
| X-Request-ID | string (UUID) | No | Request identifier for tracing |
| X-Client-Version | string | No | Client application version |

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | integer | No | 1 | Page number (1-indexed) |
| pageSize | integer | No | 20 | Items per page (max 100) |

**Request Example**:
```http
GET /api/v1/users?page=1&pageSize=20 HTTP/1.1
Host: api.apispreadsheets.com
Accept: application/json
X-Request-ID: 550e8400-e29b-41d4-a716-446655440000
```

**Response Schema**:
```json
{
  "data": [
    {
      "first_name": "string",
      "last_name": "string",
      "email": "string",
      "alamat": "string",
      "iuran_perwarga": 0,
      "total_iuran_rekap": 0,
      "jumlah_iuran_bulanan": 0,
      "total_iuran_individu": 0,
      "pengeluaran_iuran_warga": 0,
      "pemanfaatan_iuran": "string",
      "avatar": "string"
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
  "request_id": "uuid",
  "timestamp": 1704067200000
}
```

**Response Example**:
```json
{
  "data": [
    {
      "first_name": "John",
      "last_name": "Doe",
      "email": "john.doe@example.com",
      "alamat": "Jl. Merdeka No. 123, Jakarta",
      "iuran_perwarga": 500000,
      "total_iuran_rekap": 1500000,
      "jumlah_iuran_bulanan": 500000,
      "total_iuran_individu": 1500000,
      "pengeluaran_iuran_warga": 200000,
      "pemanfaatan_iuran": "Maintenance fasilitas umum",
      "avatar": "https://example.com/avatar.jpg"
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

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad Request |
| 404 | Not Found |
| 500 | Internal Server Error |

---

## Financial Endpoints

### Get Financial Records

#### Legacy API

**Endpoint**: `GET /pemanfaatan`

**Description**: Retrieves financial data and fund utilization information.

**Headers**:
| Header | Type | Required | Description |
|--------|------|----------|-------------|
| X-Request-ID | string (UUID) | No | Request identifier for tracing |

**Request Example**:
```http
GET /pemanfaatan HTTP/1.1
Host: api.apispreadsheets.com
Accept: application/json
```

**Response Schema**: Same as Users response (DataItem array)

**Response Example**: Same as Users response

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Success |
| 404 | Financial data not available |
| 500 | Internal server error |

#### API v1

**Endpoint**: `GET /api/v1/pemanfaatan`

**Description**: Retrieves financial data with standardized response wrapper and optional pagination.

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | integer | No | 1 | Page number (1-indexed) |
| pageSize | integer | No | 20 | Items per page (max 100) |

**Response Schema**: Same as API v1 Users response with pagination

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad Request |
| 404 | Not Found |
| 500 | Internal Server Error |

---

## Vendor Endpoints

### Get All Vendors

**Endpoint**: `GET /vendors` (Legacy) or `GET /api/v1/vendors` (v1)

**Description**: Retrieves list of all vendors/service providers.

**Request Example**:
```http
GET /vendors HTTP/1.1
```

**Response Schema (Legacy)**:
```json
{
  "data": [
    {
      "id": "string",
      "name": "string",
      "contactPerson": "string",
      "phoneNumber": "string",
      "email": "string",
      "specialty": "string",
      "address": "string",
      "licenseNumber": "string",
      "insuranceInfo": "string",
      "contractStart": "YYYY-MM-DD",
      "contractEnd": "YYYY-MM-DD",
      "isActive": true,
      "createdAt": "YYYY-MM-DDTHH:mm:ssZ",
      "updatedAt": "YYYY-MM-DDTHH:mm:ssZ"
    }
  ]
}
```

**Response Schema (API v1)**: Same as API v1 Users response with pagination

### Create Vendor

**Endpoint**: `POST /vendors` (Legacy) or `POST /api/v1/vendors` (v1)

**Description**: Creates a new vendor/service provider.

**Request Schema**:
```json
{
  "name": "string",
  "contactPerson": "string",
  "phoneNumber": "string",
  "email": "string",
  "specialty": "string",
  "address": "string",
  "licenseNumber": "string",
  "insuranceInfo": "string",
  "contractStart": "YYYY-MM-DD",
  "contractEnd": "YYYY-MM-DD"
}
```

**Request Example**:
```json
{
  "name": "ACME Plumbing Services",
  "contactPerson": "John Smith",
  "phoneNumber": "+6281234567890",
  "email": "acme@plumbing.com",
  "specialty": "Plumbing",
  "address": "123 Main Street",
  "licenseNumber": "LIC-2024-001",
  "insuranceInfo": "INS-2024-ABC",
  "contractStart": "2024-01-01",
  "contractEnd": "2024-12-31"
}
```

**Response Schema**: SingleVendorResponse with Vendor object

**Status Codes**:
| Code | Description |
|------|-------------|
| 201 (Legacy) / 200 (v1) | Vendor created successfully |
| 400 | Bad Request - Invalid parameters |
| 409 | Conflict - Vendor already exists |
| 422 | Validation Error |
| 500 | Internal Server Error |

### Get Vendor by ID

**Endpoint**: `GET /vendors/{id}` (Legacy) or `GET /api/v1/vendors/{id}` (v1)

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | Vendor ID |

**Request Example**:
```http
GET /vendors/123e4567-e89b-12d3-a456-426614174000 HTTP/1.1
```

**Response Schema**: SingleVendorResponse with Vendor object

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Success |
| 404 | Vendor not found |

### Update Vendor

**Endpoint**: `PUT /vendors/{id}` (Legacy) or `PUT /api/v1/vendors/{id}` (v1)

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | Vendor ID |

**Request Schema**:
```json
{
  "name": "string",
  "contactPerson": "string",
  "phoneNumber": "string",
  "email": "string",
  "specialty": "string",
  "address": "string",
  "licenseNumber": "string",
  "insuranceInfo": "string",
  "contractStart": "YYYY-MM-DD",
  "contractEnd": "YYYY-MM-DD",
  "isActive": true
}
```

**Request Example**:
```json
{
  "name": "ACME Plumbing Services",
  "contactPerson": "John Smith",
  "phoneNumber": "+6281234567890",
  "email": "acme@plumbing.com",
  "specialty": "Plumbing",
  "address": "123 Main Street",
  "licenseNumber": "LIC-2024-001",
  "insuranceInfo": "INS-2024-ABC",
  "contractStart": "2024-01-01",
  "contractEnd": "2024-12-31",
  "isActive": true
}
```

**Response Schema**: SingleVendorResponse with Vendor object

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Vendor updated successfully |
| 400 | Bad Request |
| 404 | Vendor not found |
| 500 | Internal Server Error |

---

## Work Order Endpoints

### Get All Work Orders

**Endpoint**: `GET /work-orders` (Legacy) or `GET /api/v1/work-orders` (v1)

**Description**: Retrieves list of all work orders.

**Response Schema**: WorkOrderResponse with array of WorkOrder objects

**WorkOrder Schema**:
```json
{
  "id": "string",
  "title": "string",
  "description": "string",
  "category": "ELEKTRIK|PLUMBING|STRUKTUR|AC|KEAMANAN|KEBERSIHAN|LAINNYA",
  "priority": "LOW|MEDIUM|HIGH|CRITICAL",
  "propertyId": "string",
  "reporterId": "string",
  "assignedVendorId": "string",
  "status": "PENDING|ASSIGNED|IN_PROGRESS|COMPLETED|CANCELLED",
  "estimatedCost": 0.0,
  "attachments": ["string"],
  "scheduledDate": "YYYY-MM-DDTHH:mm:ssZ",
  "completedAt": "YYYY-MM-DDTHH:mm:ssZ",
  "createdAt": "YYYY-MM-DDTHH:mm:ssZ",
  "updatedAt": "YYYY-MM-DDTHH:mm:ssZ",
  "notes": "string"
}
```

### Create Work Order

**Endpoint**: `POST /work-orders` (Legacy) or `POST /api/v1/work-orders` (v1)

**Request Schema**:
```json
{
  "title": "string",
  "description": "string",
  "category": "ELEKTRIK|PLUMBING|STRUKTUR|AC|KEAMANAN|KEBERSIHAN|LAINNYA",
  "priority": "LOW|MEDIUM|HIGH|CRITICAL",
  "propertyId": "string",
  "reporterId": "string",
  "estimatedCost": 0.0,
  "attachments": ["string"]
}
```

**Request Example**:
```json
{
  "title": "Fix plumbing leak",
  "description": "Kitchen sink is leaking",
  "category": "PLUMBING",
  "priority": "HIGH",
  "propertyId": "property-123",
  "reporterId": "user-456",
  "estimatedCost": 500.00,
  "attachments": ["https://example.com/photo1.jpg"]
}
```

**Response Schema**: SingleWorkOrderResponse with WorkOrder object

**Status Codes**:
| Code | Description |
|------|-------------|
| 201 (Legacy) / 200 (v1) | Work order created successfully |
| 400 | Bad Request |
| 422 | Validation Error |
| 500 | Internal Server Error |

### Get Work Order by ID

**Endpoint**: `GET /work-orders/{id}` (Legacy) or `GET /api/v1/work-orders/{id}` (v1)

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | Work order ID |

**Response Schema**: SingleWorkOrderResponse with WorkOrder object

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Success |
| 404 | Work order not found |

### Assign Vendor to Work Order

**Endpoint**: `PUT /work-orders/{id}/assign` (Legacy) or `PUT /api/v1/work-orders/{id}/assign` (v1)

**Request Schema**:
```json
{
  "vendorId": "string",
  "scheduledDate": "YYYY-MM-DDTHH:mm:ssZ"
}
```

**Request Example**:
```json
{
  "vendorId": "vendor-789",
  "scheduledDate": "2024-01-20T10:00:00Z"
}
```

**Response Schema**: SingleWorkOrderResponse with WorkOrder object

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Vendor assigned successfully |
| 400 | Bad Request |
| 404 | Work order not found |
| 500 | Internal Server Error |

### Update Work Order Status

**Endpoint**: `PUT /work-orders/{id}/status` (Legacy) or `PUT /api/v1/work-orders/{id}/status` (v1)

**Request Schema**:
```json
{
  "status": "PENDING|ASSIGNED|IN_PROGRESS|COMPLETED|CANCELLED",
  "notes": "string"
}
```

**Request Example**:
```json
{
  "status": "COMPLETED",
  "notes": "Work completed successfully"
}
```

**Response Schema**: SingleWorkOrderResponse with WorkOrder object

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Status updated successfully |
| 400 | Bad Request |
| 404 | Work order not found |
| 500 | Internal Server Error |

---

## Payment Endpoints

### Initiate Payment

**Endpoint**: `POST /payments/initiate` (Legacy) or `POST /api/v1/payments/initiate` (v1)

**Request Schema**:
```json
{
  "amount": "string",
  "description": "string",
  "customerId": "string",
  "paymentMethod": "CREDIT_CARD|BANK_TRANSFER|E_WALLET|VIRTUAL_ACCOUNT"
}
```

**Request Example**:
```json
{
  "amount": "500000",
  "description": "January iuran payment",
  "customerId": "user-123",
  "paymentMethod": "BANK_TRANSFER"
}
```

**Response Schema**:
```json
{
  "transactionId": "string",
  "status": "PENDING|COMPLETED|FAILED|REFUNDED",
  "paymentMethod": "CREDIT_CARD|BANK_TRANSFER|E_WALLET|VIRTUAL_ACCOUNT",
  "amount": "string",
  "currency": "string",
  "transactionTime": 1704067200000,
  "referenceNumber": "string"
}
```

**Response Example**:
```json
{
  "transactionId": "txn_123e4567-e89b",
  "status": "PENDING",
  "paymentMethod": "BANK_TRANSFER",
  "amount": "500000",
  "currency": "IDR",
  "transactionTime": 1704067200000,
  "referenceNumber": "REF-2024-001"
}
```

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Payment initiated successfully |
| 400 | Bad Request |
| 422 | Validation Error |
| 500 | Internal Server Error |

### Get Payment Status

**Endpoint**: `GET /payments/{id}/status` (Legacy) or `GET /api/v1/payments/{id}/status` (v1)

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | Payment transaction ID |

**Response Schema**:
```json
{
  "transactionId": "string",
  "status": "PENDING|COMPLETED|FAILED|REFUNDED",
  "amount": "string",
  "currency": "string",
  "updatedAt": 1704067200000
}
```

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Success |
| 404 | Payment not found |

### Confirm Payment

**Endpoint**: `POST /payments/{id}/confirm` (Legacy) or `POST /api/v1/payments/{id}/confirm` (v1)

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | string | Yes | Payment transaction ID |

**Response Schema**:
```json
{
  "transactionId": "string",
  "status": "COMPLETED|FAILED",
  "confirmationTime": 1704067200000
}
```

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Payment confirmed successfully |
| 404 | Payment not found |
| 422 | Validation Error |
| 500 | Internal Server Error |

---

## Communication Endpoints

### Get Announcements

**Endpoint**: `GET /announcements` (Legacy) or `GET /api/v1/announcements` (v1)

**Description**: Retrieves list of community announcements.

**Response Schema**: Array of Announcement objects

**Announcement Schema**:
```json
{
  "id": "string",
  "title": "string",
  "content": "string",
  "priority": "LOW|MEDIUM|HIGH|URGENT",
  "category": "string",
  "createdAt": "YYYY-MM-DDTHH:mm:ssZ",
  "createdBy": "string",
  "readBy": ["string"],
  "attachments": ["string"]
}
```

### Get Messages

**Endpoint**: `GET /messages?userId={userId}` (Legacy) or `GET /api/v1/messages?userId={userId}` (v1)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | string | Yes | User ID to filter messages |

**Response Schema**: Array of Message objects

**Message Schema**:
```json
{
  "id": "string",
  "senderId": "string",
  "receiverId": "string",
  "content": "string",
  "timestamp": "YYYY-MM-DDTHH:mm:ssZ",
  "isRead": true,
  "attachments": ["string"]
}
```

### Send Message

**Endpoint**: `POST /messages` (Legacy) or `POST /api/v1/messages` (v1)

**Request Schema**:
```json
{
  "senderId": "string",
  "receiverId": "string",
  "content": "string"
}
```

**Request Example**:
```json
{
  "senderId": "user-123",
  "receiverId": "user-456",
  "content": "Hello, how are you?"
}
```

**Response Schema**: Message object

**Status Codes**:
| Code | Description |
|------|-------------|
| 200 | Message sent successfully |
| 400 | Bad Request |
| 422 | Validation Error |

### Get Messages with User

**Endpoint**: `GET /messages/{receiverId}?senderId={senderId}` (Legacy) or `GET /api/v1/messages/{receiverId}?senderId={senderId}` (v1)

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| receiverId | string | Yes | Receiver user ID |

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| senderId | string | Yes | Sender user ID |

**Response Schema**: Array of Message objects (conversation)

### Get Community Posts

**Endpoint**: `GET /community-posts` (Legacy) or `GET /api/v1/community-posts` (v1)

**Description**: Retrieves list of community posts.

**Response Schema**: Array of CommunityPost objects

**CommunityPost Schema**:
```json
{
  "id": "string",
  "authorId": "string",
  "title": "string",
  "content": "string",
  "category": "SARAN|KELUHAN|INFO|DISKUSI|LAINNYA",
  "createdAt": "YYYY-MM-DDTHH:mm:ssZ",
  "updatedAt": "YYYY-MM-DDTHH:mm:ssZ",
  "likes": 0,
  "likedBy": ["string"],
  "comments": [{}]
}
```

### Create Community Post

**Endpoint**: `POST /community-posts` (Legacy) or `POST /api/v1/community-posts` (v1)

**Request Schema**:
```json
{
  "authorId": "string",
  "title": "string",
  "content": "string",
  "category": "SARAN|KELUHAN|INFO|DISKUSI|LAINNYA"
}
```

**Request Example**:
```json
{
  "authorId": "user-123",
  "title": "Community Garden Cleanup",
  "content": "Let's clean up the community garden this Saturday!",
  "category": "INFO"
}
```

**Response Schema**: CommunityPost object

**Status Codes**:
| Code | Description |
|------|-------------|
| 201 (Legacy) / 200 (v1) | Post created successfully |
| 400 | Bad Request |
| 422 | Validation Error |

---

## Common Error Responses

All endpoints return standardized error responses:

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

For detailed error codes, see [API_DOCS_HUB.md](API_DOCS_HUB.md#error-code-reference).

---

## Rate Limiting

All endpoints are subject to rate limiting:
- **Maximum**: 10 requests per second
- **Burst**: 60 requests per minute
- **Response**: HTTP 429 with `Retry-After` header

## Authentication

### API Key (Current)
```http
X-API-Key: your-api-key-here
```

### Bearer Token (Future)
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Testing

### Mock Server
```bash
docker-compose up api-mock
```

Base URL: `http://api-mock:5000/data/QjX6hB1ST2IDKaxB/`

### Example cURL Commands

```bash
# Get users (Legacy)
curl -X GET "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/users"

# Get users (API v1)
curl -X GET "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/api/v1/users?page=1&pageSize=20"

# Create vendor
curl -X POST "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/vendors" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ACME Plumbing",
    "contactPerson": "John Smith",
    "phoneNumber": "+6281234567890",
    "email": "acme@plumbing.com",
    "specialty": "Plumbing",
    "address": "123 Main St",
    "licenseNumber": "LIC-2024-001",
    "insuranceInfo": "INS-2024-ABC",
    "contractStart": "2024-01-01",
    "contractEnd": "2024-12-31"
  }'
```

---

*Last Updated: 2026-01-08*
*Maintained by: Integration Engineer*
