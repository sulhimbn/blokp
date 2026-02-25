# API Documentation

## Overview

Iuran BlokP uses RESTful API for data communication between the Android application and backend services. The API follows JSON format for request/response payloads and implements standard HTTP status codes.

## Base Configuration

### Production API
- **Base URL**: `https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/`
- **Protocol**: HTTPS
- **Data Format**: JSON
- **Authentication**: API Key (if required)

### Development API (Mock)
- **Base URL**: `http://api-mock:5000/data/QjX6hB1ST2IDKaxB/`
- **Protocol**: HTTP
- **Data Format**: JSON
- **Environment**: Docker development environment

## API Endpoints

### 1. Users Endpoint

#### GET /users
Retrieves list of all users/warga in the HOA system.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/users
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "first_name": "John",
      "last_name": "Doe",
      "email": "john.doe@example.com",
      "alamat": "Jl. Merdeka No. 123",
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

**Error Responses:**
- `404 Not Found`: Endpoint not available
- `500 Internal Server Error`: Server processing error

#### Response Schema
```typescript
interface UserResponse {
  data: DataItem[];
}

interface DataItem {
  first_name: string;
  last_name: string;
  email: string;
  alamat: string;
  iuran_perwarga: number;
  total_iuran_rekap: number;
  jumlah_iuran_bulanan: number;
  total_iuran_individu: number;
  pengeluaran_iuran_warga: number;
  pemanfaatan_iuran: string;
  avatar: string;
}
```

### 2. Financial Data Endpoint

#### GET /pemanfaatan
Retrieves financial data and fund utilization information.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/pemanfaatan
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "first_name": "John",
      "last_name": "Doe",
      "email": "john.doe@example.com",
      "alamat": "Jl. Merdeka No. 123",
      "iuran_perwarga": 500000,
      "total_iuran_rekap": 1500000,
      "jumlah_iuran_bulanan": 500000,
      "total_iuran_individu": 1500000,
      "pengeluaran_iuran_warga": 200000,
      "pemanfaatan_iuran": "Perbaikan taman dan keamanan",
      "avatar": "https://example.com/avatar.jpg"
    }
  ]
}
```

**Error Responses:**
- `404 Not Found`: Financial data not available
- `500 Internal Server Error`: Server processing error

### 3. Legacy Endpoint (Deprecated)

#### GET /
Legacy endpoint for backward compatibility. Redirects to users endpoint.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/
Content-Type: application/json
```

**Note:** This endpoint is deprecated and will be removed in future versions. Use `/users` endpoint instead.
### 4. Announcements Endpoint

#### GET /announcements
Retrieves list of all announcements in the HOA system.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/announcements
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": "ann_001",
      "title": "Monthly Meeting",
      "content": "There will be a monthly community meeting this Saturday...",
      "category": "meeting",
      "priority": "high",
      "createdAt": "2026-02-20T10:00:00Z",
      "readBy": ["user_1", "user_2"]
    }
  ]
}
```

**Response Schema:**
```typescript
interface Announcement {
  id: string;
  title: string;
  content: string;
  category: string;
  priority: string;
  createdAt: string;
  readBy: string[];
}
```

### 5. Messages Endpoints

#### GET /messages
Retrieves messages for a specific user.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/messages?userId=user_001
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": "msg_001",
      "senderId": "user_001",
      "receiverId": "user_002",
      "content": "Hello, regarding the maintenance request...",
      "timestamp": "2026-02-20T14:30:00Z",
      "readStatus": false,
      "attachments": []
    }
  ]
}
```

#### GET /messages/{receiverId}
Retrieves conversation between two users.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/messages/user_002?senderId=user_001
Content-Type: application/json
```

#### POST /messages
Send a message to another user.

**Request:**
```http
POST /data/QjX6hB1ST2IDKaxB/messages?senderId=user_001&receiverId=user_002&content=Hello
Content-Type: application/json
```

**Response Schema:**
```typescript
interface Message {
  id: string;
  senderId: string;
  receiverId: string;
  content: string;
  timestamp: string;
  readStatus: boolean;
  attachments: string[];
}
```

### 6. Community Posts Endpoint

#### GET /community-posts
Retrieves community discussion posts.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/community-posts
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": "post_001",
      "authorId": "user_001",
      "title": "Pool Maintenance Schedule",
      "content": "The pool will be closed for maintenance next week...",
      "category": "maintenance",
      "likes": 15,
      "comments": [
        {
          "id": "comment_001",
          "authorId": "user_002",
          "content": "Thank you for the update!",
          "timestamp": "2026-02-20T15:00:00Z"
        }
      ],
      "createdAt": "2026-02-20T12:00:00Z"
    }
  ]
}
```

#### POST /community-posts
Create a new community post.

**Request:**
```http
POST /data/QjX6hB1ST2IDKaxB/community-posts?authorId=user_001&title=New+Post&content=Content&category=general
Content-Type: application/json
```

**Response Schema:**
```typescript
interface Comment {
  id: string;
  authorId: string;
  content: string;
  timestamp: string;
}

interface CommunityPost {
  id: string;
  authorId: string;
  title: string;
  content: string;
  category: string;
  likes: number;
  comments: Comment[];
  createdAt: string;
}
```

### 7. Payment Endpoints

#### POST /payments/initiate
Initiate a new payment transaction.

**Request:**
```http
POST /data/QjX6hB1ST2IDKaxB/payments/initiate?amount=500000&description=Monthly+Iuran&customerId=user_001&paymentMethod=bank_transfer
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "transactionId": "txn_001",
  "status": "pending",
  "paymentMethod": "bank_transfer",
  "amount": "500000",
  "currency": "IDR",
  "transactionTime": 1708444800000,
  "referenceNumber": "REF-20260220-001"
}
```

#### GET /payments/{id}/status
Get payment transaction status.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/payments/txn_001/status
Content-Type: application/json
```

#### POST /payments/{id}/confirm
Confirm a pending payment.

**Request:**
```http
POST /data/QjX6hB1ST2IDKaxB/payments/txn_001/confirm
Content-Type: application/json
```

**Response Schema:**
```typescript
interface PaymentResponse {
  transactionId: string;
  status: string;
  paymentMethod: string;
  amount: string;
  currency: string;
  transactionTime: number;
  referenceNumber: string;
}

interface PaymentStatusResponse {
  transactionId: string;
  status: string;
  amount: string;
  currency: string;
  updatedAt: number;
}

interface PaymentConfirmationResponse {
  transactionId: string;
  status: string;
  confirmationTime: number;
}
```

### 8. Vendor Management Endpoints

#### GET /vendors
Retrieves list of all vendors.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/vendors
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": "vendor_001",
      "name": "PT Jaya Makmur",
      "contactPerson": "John Doe",
      "phoneNumber": "+62812345678",
      "email": "john@jayamakmur.com",
      "specialty": "plumbing",
      "address": "Jl. Merdeka No. 45",
      "licenseNumber": "LIC-2024-001",
      "insuranceInfo": "insured",
      "certifications": ["ISO 9001"],
      "rating": 4.5,
      "totalReviews": 28,
      "contractStart": "2024-01-01",
      "contractEnd": "2025-12-31",
      "isActive": true
    }
  ]
}
```

#### GET /vendors/{id}
Retrieves a specific vendor by ID.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/vendors/vendor_001
Content-Type: application/json
```

#### POST /vendors
Create a new vendor.

**Request:**
```http
POST /data/QjX6hB1ST2IDKaxB/vendors?name=PT+Jaya+Makmur&contactPerson=John+Doe&phoneNumber=+62812345678&email=john@jayamakmur.com&specialty=plumbing&address=Jl.+Merdeka+No.+45&licenseNumber=LIC-2024-001&insuranceInfo=insured&contractStart=2024-01-01&contractEnd=2025-12-31
Content-Type: application/json
```

#### PUT /vendors/{id}
Update an existing vendor.

**Request:**
```http
PUT /data/QjX6hB1ST2IDKaxB/vendors/vendor_001?name=PT+Jaya+Makmur&contactPerson=John+Doe&phoneNumber=+62812345678&email=john@jayamakmur.com&specialty=plumbing&address=Jl.+Merdeka+No.+45&licenseNumber=LIC-2024-001&insuranceInfo=insured&contractStart=2024-01-01&contractEnd=2025-12-31&isActive=true
Content-Type: application/json
```

**Response Schema:**
```typescript
interface Vendor {
  id: string;
  name: string;
  contactPerson: string;
  phoneNumber: string;
  email: string;
  specialty: string;
  address: string;
  licenseNumber: string;
  insuranceInfo: string;
  certifications: string[];
  rating: number;
  totalReviews: number;
  contractStart: string;
  contractEnd: string;
  isActive: boolean;
}
```

### 9. Work Order Endpoints

#### GET /work-orders
Retrieves all work orders.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/work-orders
Content-Type: application/json
```

**Response (200 OK):**
```json
{
  "data": [
    {
      "id": "wo_001",
      "title": "Fix Leaking Faucet",
      "description": "The faucet in block A is leaking",
      "category": "plumbing",
      "priority": "medium",
      "status": "pending",
      "vendorId": null,
      "vendorName": null,
      "assignedAt": null,
      "scheduledDate": null,
      "completedAt": null,
      "estimatedCost": 150000,
      "actualCost": 0,
      "propertyId": "prop_001",
      "reporterId": "user_001",
      "createdAt": "2026-02-20T10:00:00Z",
      "updatedAt": "2026-02-20T10:00:00Z",
      "attachments": [],
      "notes": []
    }
  ]
}
```

#### GET /work-orders/{id}
Retrieves a specific work order by ID.

**Request:**
```http
GET /data/QjX6hB1ST2IDKaxB/work-orders/wo_001
Content-Type: application/json
```

#### POST /work-orders
Create a new work order.

**Request:**
```http
POST /data/QjX6hB1ST2IDKaxB/work-orders?title=Fix+Leaking+Faucet&description=The+faucet+in+block+A+is+leaking&category=plumbing&priority=medium&propertyId=prop_001&reporterId=user_001&estimatedCost=150000
Content-Type: application/json
```

#### PUT /work-orders/{id}/assign
Assign a vendor to a work order.

**Request:**
```http
PUT /data/QjX6hB1ST2IDKaxB/work-orders/wo_001/assign?vendorId=vendor_001&scheduledDate=2026-02-25
Content-Type: application/json
```

#### PUT /work-orders/{id}/status
Update work order status.

**Request:**
```http
PUT /data/QjX6hB1ST2IDKaxB/work-orders/wo_001/status?status=in_progress&notes=Work+started
Content-Type: application/json
```

**Response Schema:**
```typescript
interface WorkOrder {
  id: string;
  title: string;
  description: string;
  category: string;
  priority: string;
  status: string;
  vendorId: string | null;
  vendorName: string | null;
  assignedAt: string | null;
  scheduledDate: string | null;
  completedAt: string | null;
  estimatedCost: number;
  actualCost: number;
  propertyId: string;
  reporterId: string;
  createdAt: string;
  updatedAt: string;
  attachments: string[];
  notes: string[];
}
```


## Data Models

### User Model
```kotlin
data class User(
    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val email: String,
    val address: String,
    val avatarUrl: String?
) {
    val fullName: String
        get() = "$firstName $lastName"
}
```

### Financial Record Model
```kotlin
data class FinancialRecord(
    val userId: String,
    val monthlyDue: Int,
    val totalRecap: Int,
    val monthlyTotal: Int,
    val individualTotal: Int,
    val expenses: Int,
    val utilizationDescription: String
) {
    val balance: Int
        get() = individualTotal - expenses
}
```

### Combined Data Item (Current)
```kotlin
data class DataItem(
    val first_name: String,
    val last_name: String,
    val email: String,
    val alamat: String,
    val iuran_perwarga: Int,
    val total_iuran_rekap: Int,
    val jumlah_iuran_bulanan: Int,
    val total_iuran_individu: Int,
    val pengeluaran_iuran_warga: Int,
    val pemanfaatan_iuran: String,
    val avatar: String
)
```

## API Client Implementation

### Service Interface
```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): Response<UserResponse>
    
    @GET("pemanfaatan")
    suspend fun getPemanfaatan(): Response<PemanfaatanResponse>
}
```

### Repository Implementation
```kotlin
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userCache: UserCache
) {
    suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                val users = response.body()?.data?.map { it.toUser() }
                if (users != null) {
                    userCache.cacheUsers(users)
                    Result.success(users)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                // Fallback to cache
                val cachedUsers = userCache.getCachedUsers()
                if (cachedUsers.isNotEmpty()) {
                    Result.success(cachedUsers)
                } else {
                    Result.failure(Exception("Network error and no cache available"))
                }
            }
        } catch (e: Exception) {
            // Try cache on network error
            val cachedUsers = userCache.getCachedUsers()
            if (cachedUsers.isNotEmpty()) {
                Result.success(cachedUsers)
            } else {
                Result.failure(e)
            }
        }
    }
}
```

## Error Handling

### HTTP Status Codes
- `200 OK`: Request successful
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Access denied
- `404 Not Found`: Resource not found
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error
- `502 Bad Gateway`: Gateway error
- `503 Service Unavailable`: Service temporarily unavailable

### Error Response Format
```json
{
  "error": {
    "code": "NETWORK_ERROR",
    "message": "No internet connection",
    "details": "Please check your network settings and try again"
  }
}
```

### Client-Side Error Handling
```kotlin
class ApiErrorHandler {
    fun handleError(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> "No internet connection"
            is SocketTimeoutException -> "Connection timeout"
            is HttpException -> {
                when (throwable.code()) {
                    401 -> "Unauthorized access"
                    403 -> "Access forbidden"
                    404 -> "Data not found"
                    429 -> "Too many requests. Please try again later"
                    500 -> "Server error. Please try again later"
                    else -> "HTTP Error: ${throwable.code()}"
                }
            }
            else -> "An unexpected error occurred"
        }
    }
}
```

## Security

### Certificate Pinning
```kotlin
object CertificatePinningConfig {
    private const val CERTIFICATE_PINNER = "sha256/PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0="
    
    fun getCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("api.apispreadsheets.com", CERTIFICATE_PINNER)
            .build()
    }
}
```

### Request Validation
```kotlin
class RequestValidator {
    fun validateEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun validateAmount(amount: Int): Boolean {
        return amount >= 0 && amount <= 10_000_000
    }
}
```

## Performance Optimization

### Caching Strategy
```kotlin
class ApiCache @Inject constructor(
    private val context: Context
) {
    private val cacheSize = (10 * 1024 * 1024).toLong() // 10MB
    private val cache = Cache(context.cacheDir, cacheSize)
    
    fun getCachedResponse(url: String): String? {
        return try {
            val snapshot = cache.get(url)
            snapshot?.use { it.getString(Charsets.UTF_8) }
        } catch (e: Exception) {
            null
        }
    }
    
    fun cacheResponse(url: String, response: String) {
        try {
            val editor = cache.edit(url)
            editor?.put(response)
            editor?.commit()
        } catch (e: Exception) {
            // Cache write failed
        }
    }
}
```

### Retry Logic
```kotlin
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val retryDelayMs: Long = 1000
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var retryCount = 0
        
        while (retryCount < maxRetries) {
            try {
                response = chain.proceed(request)
                if (response.isSuccessful || response.code == 404) {
                    return response
                }
            } catch (e: Exception) {
                if (retryCount == maxRetries - 1) {
                    throw e
                }
            }
            
            retryCount++
            Thread.sleep(retryDelayMs * retryCount)
        }
        
        return response ?: throw IOException("Max retries exceeded")
    }
}
```

## Testing

### Mock API Server
```python
from flask import Flask, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

@app.route('/data/QjX6hB1ST2IDKaxB/users', methods=['GET'])
def get_users():
    return jsonify({
        "data": [
            {
                "first_name": "Test",
                "last_name": "User",
                "email": "test@example.com",
                "alamat": "Test Address",
                "iuran_perwarga": 500000,
                "total_iuran_rekap": 1500000,
                "jumlah_iuran_bulanan": 500000,
                "total_iuran_individu": 1500000,
                "pengeluaran_iuran_warga": 200000,
                "pemanfaatan_iuran": "Test utilization",
                "avatar": "https://example.com/avatar.jpg"
            }
        ]
    })
```

### Integration Tests
```kotlin
@Test
fun `getUsers should parse response correctly`() {
    // Given
    val mockResponse = MockResponse()
        .setBody(mockUsersJson)
        .addHeader("Content-Type", "application/json")
    
    mockWebServer.enqueue(mockResponse)
    
    // When
    val response = apiService.getUsers().execute()
    
    // Then
    assertTrue(response.isSuccessful)
    assertEquals(200, response.code())
    assertNotNull(response.body())
    assertEquals(1, response.body()?.data?.size)
}
```

## Future API Enhancements

### Planned Endpoints
- `POST /users` - Create new user
- `PUT /users/{id}` - Update user information
- `DELETE /users/{id}` - Delete user
- `POST /payments` - Process payment
- `GET /payments/{id}` - Get payment status
- `GET /reports/financial` - Generate financial reports
- `POST /announcements` - Create announcement
- `GET /announcements` - List announcements

### Authentication & Authorization
- JWT token-based authentication
- Role-based access control (Admin, Resident, Staff)
- API key management for third-party integrations

### Rate Limiting
- 100 requests per minute per user
- 1000 requests per minute per IP
- Burst capacity for batch operations

---

*Last Updated: November 2025*
*Next Review: After payment system implementation*
*Maintainer: Backend Development Team*