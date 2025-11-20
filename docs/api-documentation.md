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