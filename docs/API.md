# API Documentation

## Overview

IuranKomplek API menyediakan endpoints untuk mengambil data pengguna dan informasi pemanfaatan iuran komplek. API menggunakan layanan pihak ketiga (API Spreadsheets) untuk penyimpanan data.

## Payment Processing System

### Overview
The application implements a flexible payment processing system with the following components:

#### Payment Methods
- CREDIT_CARD
- BANK_TRANSFER
- E_WALLET
- VIRTUAL_ACCOUNT

#### Core Components
- `PaymentGateway` - Interface for payment processing operations
- `MockPaymentGateway` - Development/testing implementation
- `Transaction` - Room entity for storing payment transactions
- `TransactionDao` - Data access object for transaction operations
- `TransactionRepository` - Business logic layer for transaction management
- `Receipt` - Receipt data model
- `ReceiptGenerator` - Receipt generation utility
- `PaymentViewModel` - UI state management for payment operations
- `PaymentActivity` - Payment processing UI

#### Payment Flow
1. User enters payment amount and selects payment method
2. Payment request is sent to the payment gateway
3. Transaction is created with PENDING status and stored in database
4. Payment gateway processes the payment
5. Transaction status is updated to COMPLETED or FAILED based on result
6. Receipt is generated for successful payments

## Base URLs

### Production
```
https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/
```

### Development (Docker)
```
http://api-mock:5000/data/QjX6hB1ST2IDKaxB/
```

### Environment Switching
Aplikasi secara otomatis beralih antara production dan development API berdasarkan:
- `BuildConfig.DEBUG` flag
- `DOCKER_ENV` environment variable

## Endpoints

### User & Financial Data Endpoints

#### GET /data/QjX6hB1ST2IDKaxB/users

Mengambil data pengguna/warga.

#### GET /data/QjX6hB1ST2IDKaxB/pemanfaatan

Mengambil data pemanfaatan iuran.

### Communication Endpoints

#### GET /data/QjX6hB1ST2IDKaxB/announcements

Mengambil pengumuman komunitas.

#### GET /data/QjX6hB1ST2IDKaxB/messages?userId={userId}

Mengambil pesan untuk pengguna tertentu.

#### GET /data/QjX6hB1ST2IDKaxB/messages/{receiverId}?senderId={senderId}

Mengambil percakapan dengan pengguna tertentu.

#### POST /data/QjX6hB1ST2IDKaxB/messages

Mengirim pesan baru.

### Payment Processing Endpoints

#### POST /data/QjX6hB1ST2IDKaxB/payments/initiate

Memulai proses pembayaran.

#### GET /data/QjX6hB1ST2IDKaxB/payments/{id}/status

Mengambil status pembayaran.

#### POST /data/QjX6hB1ST2IDKaxB/payments/{id}/confirm

Mengonfirmasi pembayaran.

### Vendor Management Endpoints

#### GET /data/QjX6hB1ST2IDKaxB/vendors

Mengambil daftar vendor.

#### POST /data/QjX6hB1ST2IDKaxB/vendors

Membuat vendor baru.

### Work Order Endpoints

#### GET /data/QjX6hB1ST2IDKaxB/work-orders

Mengambil daftar work order.

#### POST /data/QjX6hB1ST2IDKaxB/work-orders

Membuat work order baru.

#### Request (Users Endpoint)
```http
GET /data/QjX6hB1ST2IDKaxB/users HTTP/1.1
Host: api.apispreadsheets.com
Accept: application/json
```

#### Request (Pemanfaatan Endpoint)
```http
GET /data/QjX6hB1ST2IDKaxB/pemanfaatan HTTP/1.1
Host: api.apispreadsheets.com
Accept: application/json
```

#### Response Format
```json
{
  "data": [
    {
      "first_name": "John",
      "last_name": "Doe",
      "email": "john.doe@example.com",
      "alamat": "Jl. Contoh No. 123, Jakarta",
      "iuran_perwarga": 150000,
      "total_iuran_rekap": 1800000,
      "jumlah_iuran_bulanan": 150000,
      "total_iuran_individu": 150000,
      "pengeluaran_iuran_warga": 50000,
      "pemanfaatan_iuran": "Perbaikan jalan komplek",
      "avatar": "https://example.com/avatar.jpg"
    }
  ]
}
```

#### Data Fields

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `first_name` | String | Nama depan pengguna | "John" |
| `last_name` | String | Nama belakang pengguna | "Doe" |
| `email` | String | Email pengguna | "john@example.com" |
| `alamat` | String | Alamat lengkap | "Jl. Contoh No. 123" |
| `iuran_perwarga` | Integer | Jumlah iuran per warga per bulan | 150000 |
| `total_iuran_rekap` | Integer | Total rekap iuran (setahun) | 1800000 |
| `jumlah_iuran_bulanan` | Integer | Jumlah iuran bulanan | 150000 |
| `total_iuran_individu` | Integer | Total iuran individu | 150000 |
| `pengeluaran_iuran_warga` | Integer | Jumlah pengeluaran | 50000 |
| `pemanfaatan_iuran` | String | Deskripsi pemanfaatan | "Perbaikan jalan" |
| `avatar` | String | URL foto profil | "https://..." |

## Client Implementation

### Android (Kotlin)

#### Service Interface
```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): Response<UserResponse>
    
    @GET("pemanfaatan")
    suspend fun getPemanfaatan(): Response<PemanfaatanResponse>
    
    // Payment endpoints
    @POST("payments/initiate")
    suspend fun initiatePayment(
        @Query("amount") amount: String,
        @Query("description") description: String,
        @Query("customerId") customerId: String,
        @Query("paymentMethod") paymentMethod: String
    ): Response<PaymentResponse>
    
    // Additional endpoints for communication, vendors, work orders...
}
```

#### Configuration
```kotlin
object ApiConfig {
    private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
    private const val BASE_URL = if (USE_MOCK_API) {
        "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/\n\n"
    } else {
        "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/\n\n"
    }
    
    fun getApiService(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}
```

#### Usage Example
```kotlin
class MainActivity : AppCompatActivity() {
    private val scope = lifecycleScope
    
    private fun getUser() {
        scope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val response = apiService.getUsers()
                
                if (response.isSuccessful && response.body() != null) {
                    val dataArray = response.body()?.data
                    if (dataArray != null) {
                        adapter.setUsers(dataArray)
                    } else {
                        showEmptyState()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Network error", e)
            }
        }
    }
}
```

## Error Handling

### HTTP Status Codes

| Status | Description | Client Action |
|--------|-------------|---------------|
| 200 | Success | Process response data |
| 400 | Bad Request | Check request parameters |
| 404 | Not Found | Verify endpoint URL |
| 500 | Server Error | Retry with backoff |
| 503 | Service Unavailable | Display offline message |

### Error Response Format
```json
{
  "error": "Data not found",
  "message": "Spreadsheet not found or inaccessible"
}
```

### Client Error Handling
```kotlin
scope.launch {
    try {
        val response = apiService.getUsers()
        
        when (response.code()) {
            200 -> {
                val data = response.body()?.data
                if (data != null) {
                    adapter.setUsers(data)
                } else {
                    showEmptyState()
                }
            }
            404 -> {
                showErrorMessage("Data tidak ditemukan")
            }
            500 -> {
                showErrorMessage("Server error, coba lagi nanti")
            }
            else -> {
                showErrorMessage("Terjadi kesalahan: ${response.code()}")
            }
        }
    } catch (e: Exception) {
        showErrorMessage("Network error: ${e.message}")
    }
}
```

## Data Models

### ResponseUser
```kotlin
data class ResponseUser(
    val data: List<DataItem>
)
```

### DataItem
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

## Mock API (Development)

### Setup with Docker
```bash
# Start mock API
docker-compose up api-mock

# Access mock API
curl http://localhost:8080/data/QjX6hB1ST2IDKaxB/
```

### Mock Data Structure
Mock API harus mengembalikan data dengan struktur yang sama seperti production API:

```json
{
  "data": [
    {
      "first_name": "Test",
      "last_name": "User",
      "email": "test@example.com",
      "alamat": "Test Address",
      "iuran_perwarga": 100000,
      "total_iuran_rekap": 1200000,
      "jumlah_iuran_bulanan": 100000,
      "total_iuran_individu": 100000,
      "pengeluaran_iuran_warga": 25000,
      "pemanfaatan_iuran": "Test pemanfaatan",
      "avatar": "https://via.placeholder.com/80"
    }
  ]
}
```

## Security Considerations

### Current Security Measures
- HTTPS for production endpoints
- Basic error message sanitization
- Debug-only network inspection

### Security Recommendations
1. **Certificate Pinning**: Implement SSL certificate pinning
2. **API Key Authentication**: Add API key if supported by provider
3. **Request Validation**: Validate all incoming data
4. **Rate Limiting**: Implement client-side rate limiting
5. **Data Encryption**: Encrypt sensitive data at rest

### Network Security Configuration
```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.apispreadsheets.com</domain>
        <pin-set expiration="2024-12-31">
            <pin algorithm="sha256">CERTIFICATE_PIN_HERE</pin>
        </pin-set>
    </domain-config>
</network-security_config>
```

## Performance Optimization

### Caching Strategy
```kotlin
// Add OkHttp caching
val cacheSize = 10 * 1024 * 1024 // 10 MB
val cache = Cache(context.cacheDir, cacheSize)

val okHttpClient = OkHttpClient.Builder()
    .cache(cache)
    .build()
```

### Request Optimization
- Use conditional requests (ETag, Last-Modified)
- Implement request deduplication
- Add request timeouts
- Use connection pooling

### Response Optimization
- Implement response compression
- Use efficient JSON parsing
- Cache parsed responses
- Lazy load large datasets

## Testing

### Unit Tests
```kotlin
@Test
fun `test API response parsing`() = runTest {
    val json = """{"data":[{"first_name":"Test","last_name":"User"}]}"""
    val response = Gson().fromJson(json, UserResponse::class.java)
    assertEquals("Test", response.data[0].first_name)
}

@Test
fun `test getUsers with coroutines`() = runTest {
    val mockService = mockApiService()
    val expectedResponse = UserResponse(listOf(
        DataItem(first_name = "Test", last_name = "User", ...)
    ))
    
    coEvery { mockService.getUsers() } returns Response.success(expectedResponse)
    
    val result = mockService.getUsers()
    assertTrue(result.isSuccessful)
    assertEquals("Test", result.body()?.data?.get(0)?.first_name)
}
```

### Integration Tests
```kotlin
@Test
fun `test API connectivity`() = runTest {
    val apiService = ApiConfig.getApiService()
    val response = apiService.getUsers()
    
    assertTrue(response.isSuccessful)
    assertNotNull(response.body())
}
```

### Mock Server Testing
```kotlin
// Use MockWebServer for local testing
val mockServer = MockWebServer()
mockServer.enqueue(MockResponse().setBody(mockJsonResponse))

// Update base URL for testing
val retrofit = Retrofit.Builder()
    .baseUrl(mockServer.url("/"))
    .build()
```

## Troubleshooting

### Common Issues

#### 1. Connection Timeout
**Symptoms**: Request hangs or times out
**Solutions**:
- Check network connectivity
- Verify base URL correctness
- Increase timeout values
- Check DNS resolution

#### 2. JSON Parsing Error
**Symptoms**: Crashes on response parsing
**Solutions**:
- Validate JSON structure
- Check field names and types
- Add null safety checks
- Use JSON schema validation

#### 3. Mock API Not Working
**Symptoms**: Development environment fails
**Solutions**:
- Verify Docker container is running
- Check mock data structure
- Validate endpoint URLs
- Review network configuration

#### 4. SSL Certificate Issues
**Symptoms**: HTTPS requests fail
**Solutions**:
- Update certificate pinning
- Check system time
- Verify certificate validity
- Use proper security configuration

### Debug Tools

#### Network Inspection
```kotlin
// Chucker for debug builds
debugImplementation("com.github.chuckerteam.chucker:library:3.3.0")
```

#### Logging
```kotlin
// Add OkHttp logging interceptor
val logging = HttpLoggingInterceptor()
logging.setLevel(HttpLoggingInterceptor.Level.BODY)

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()
```

## Version History

### v1.0.0 (Current)
- Initial API implementation
- Single endpoint for user data
- Basic error handling
- Mock API support

### Planned Updates
- v1.1.0: Add pagination support
- v1.2.0: Implement data filtering
- v1.3.0: Add real-time updates
- v2.0.0: Multiple endpoint support

## Support

For API-related issues:
1. Check the troubleshooting section
2. Review GitHub issues
3. Contact development team
4. Check API provider documentation

---

*Last Updated: November 2025*
*API Version: 1.0.0*