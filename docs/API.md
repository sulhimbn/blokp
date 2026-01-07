# API Documentation

## Overview

IuranKomplek API menyediakan endpoints untuk mengambil data pengguna dan informasi pemanfaatan iuran komplek. API menggunakan layanan pihak ketiga (API Spreadsheets) untuk penyimpanan data.

## Integration Patterns

For detailed information about resilience patterns, error handling, and API integration architecture, see:
- **[API Integration Patterns](API_INTEGRATION_PATTERNS.md)** - Circuit breaker, rate limiting, retry logic, error handling
- **[Caching Strategy](CACHING_STRATEGY.md)** - Offline support and data synchronization
- **[Security Architecture](SECURITY_AUDIT_REPORT.md)** - Certificate pinning, input validation, logging

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

### Android (Kotlin) with MVVM Architecture

#### Service Interface
```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): Response<UserResponse>
    
    @GET("pemanfaatan")
    suspend fun getPemanfaatan(): Response<PemanfaatanResponse>
    
    @GET("vendors")
    suspend fun getVendors(): Response<VendorResponse>
    
    // Payment endpoints
    @POST("payments/initiate")
    suspend fun initiatePayment(
        @Query("amount") amount: String,
        @Query("description") description: String,
        @Query("customerId") customerId: String,
        @Query("paymentMethod") paymentMethod: String
    ): Response<PaymentResponse>
    
    // Communication endpoints
    @GET("announcements")
    suspend fun getAnnouncements(): Response<AnnouncementResponse>
    
    @GET("messages")
    suspend fun getMessages(@Query("userId") userId: String): Response<MessageResponse>
    
    // Work order endpoints
    @GET("work-orders")
    suspend fun getWorkOrders(): Response<WorkOrderResponse>
}
```

#### Configuration with Circuit Breaker
```kotlin
object ApiConfig {
    private val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
    private val BASE_URL = if (USE_MOCK_API) {
        "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/"
    } else {
        "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"
    }
    
    // Circuit breaker for service resilience
    val circuitBreaker: CircuitBreaker = CircuitBreaker(
        failureThreshold = 3,
        successThreshold = 2,
        timeout = 60000L,
        halfOpenMaxCalls = 3
    )
    
    // Singleton pattern with thread-safe initialization
    @Volatile
    private var apiServiceInstance: ApiService? = null
    
    fun getApiService(): ApiService {
        return apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: createApiService().also { apiServiceInstance = it }
        }
    }
    
    private fun createApiService(): ApiService {
        val okHttpClient = if (!USE_MOCK_API) {
            SecurityConfig.getSecureOkHttpClient()
                .newBuilder()
                .addInterceptor(RequestIdInterceptor())
                .addInterceptor(RetryableRequestInterceptor())
                .addInterceptor(NetworkErrorInterceptor(enableLogging = BuildConfig.DEBUG))
                .build()
        } else {
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(RequestIdInterceptor())
                .addInterceptor(RetryableRequestInterceptor())
                .addInterceptor(NetworkErrorInterceptor(enableLogging = true))
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                    }
                }
                .build()
        }
        
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}
```

#### Repository Pattern with Circuit Breaker
```kotlin
interface UserRepository {
    suspend fun getUsers(): Result<List<DataItem>>
}

class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {
    
    override suspend fun getUsers(): Result<List<DataItem>> {
        return withCircuitBreaker(ApiConfig.circuitBreaker) {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(NetworkException(response.code()))
            }
        }
    }
}

// Factory pattern for consistent instantiation
object UserRepositoryFactory {
    private var instance: UserRepository? = null
    
    fun getInstance(): UserRepository {
        return instance ?: synchronized(this) {
            instance ?: UserRepositoryImpl(ApiConfig.getApiService()).also { 
                instance = it 
            }
        }
    }
}
```

#### ViewModel with StateFlow
```kotlin
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<DataItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DataItem>>> = _uiState.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getUsers()
                .onSuccess { users ->
                    _uiState.value = UiState.Success(users)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}

// Factory for ViewModel instantiation
class UserViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(UserRepositoryFactory.getInstance()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

#### Usage Example in Activity
```kotlin
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels { UserViewModelFactory() }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        observeViewModel()
        viewModel.loadUsers()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading()
                    is UiState.Success -> showUsers(state.data)
                    is UiState.Error -> showError(state.message)
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvUsers.visibility = View.GONE
    }
    
    private fun showUsers(users: List<DataItem>) {
        binding.progressBar.visibility = View.GONE
        binding.rvUsers.visibility = View.VISIBLE
        adapter.submitList(users)
    }
    
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}
```

## Error Handling

### HTTP Status Codes

| Status | Description | Client Action |
|--------|-------------|---------------|
| 200 | Success | Process response data |
| 400 | Bad Request | Check request parameters |
| 401 | Unauthorized | Check authentication |
| 403 | Forbidden | Check permissions |
| 404 | Not Found | Verify endpoint URL |
| 408 | Request Timeout | Retry with backoff |
| 429 | Too Many Requests | Wait and retry |
| 500 | Server Error | Retry with backoff |
| 503 | Service Unavailable | Display offline message |

### Error Response Format
```json
{
  "error": "Data not found",
  "message": "Spreadsheet not found or inaccessible"
}
```

### Circuit Breaker Pattern
The application implements a Circuit Breaker pattern to prevent cascading failures:

```kotlin
// Circuit Breaker States
enum class CircuitBreakerState {
    CLOSED,    // Normal operation
    OPEN,      // Circuit is open, requests fail fast
    HALF_OPEN  // Testing if service has recovered
}

// Circuit Breaker Configuration
CircuitBreaker(
    failureThreshold = 3,      // Failures before opening circuit
    successThreshold = 2,      // Successes before closing circuit
    timeout = 60000L,          // Time before attempting recovery (60s)
    halfOpenMaxCalls = 3        // Max requests in half-open state
)
```

### Client Error Handling with StateFlow
```kotlin
// UiState wrapper for type-safe state management
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// Error handling in ViewModel
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<DataItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<DataItem>>> = _uiState.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            repository.getUsers()
                .onSuccess { users ->
                    if (users.isEmpty()) {
                        _uiState.value = UiState.Error("No data available")
                    } else {
                        _uiState.value = UiState.Success(users)
                    }
                }
                .onFailure { error ->
                    val message = when (error) {
                        is NetworkException -> "Network error: ${error.message}"
                        is CircuitBreakerException -> "Service temporarily unavailable"
                        is TimeoutException -> "Request timed out"
                        else -> "Unexpected error occurred"
                    }
                    _uiState.value = UiState.Error(message)
                }
        }
    }
}

// Observing state in Activity
class MainActivity : BaseActivity() {
    private val viewModel: UserViewModel by viewModels { UserViewModelFactory() }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading()
                    is UiState.Success -> showUsers(state.data)
                    is UiState.Error -> showError(state.message)
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvUsers.visibility = View.GONE
    }
    
    private fun showUsers(users: List<DataItem>) {
        binding.progressBar.visibility = View.GONE
        binding.rvUsers.visibility = View.VISIBLE
        if (users.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.emptyState.visibility = View.GONE
        }
    }
    
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
```

### Network Error Interceptors
The application uses interceptors for centralized error handling:

1. **NetworkErrorInterceptor**: Parses HTTP errors and converts to typed NetworkError
2. **RequestIdInterceptor**: Adds unique request IDs for tracing
3. **RetryableRequestInterceptor**: Marks safe-to-retry requests (GET, HEAD, OPTIONS)

```kotlin
// Network error types
sealed class NetworkError : Exception() {
    data class HttpError(val code: Int, val message: String) : NetworkError()
    data class TimeoutError(val message: String) : NetworkError()
    data class ConnectionError(val message: String) : NetworkError()
    data class CircuitBreakerError(val message: String) : NetworkError()
    data class ValidationError(val message: String) : NetworkError()
    data class UnknownNetworkError(val message: String) : NetworkError()
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

### Unit Tests for Repository
```kotlin
@RunWith(MockitoJUnitRunner::class)
class UserRepositoryImplTest {
    
    private lateinit var mockApiService: ApiService
    private lateinit var repository: UserRepositoryImpl
    
    @Before
    fun setup() {
        mockApiService = mock()
        repository = UserRepositoryImpl(mockApiService)
    }
    
    @Test
    fun `getUsers returns success with valid data`() = runTest {
        // Given
        val expectedData = listOf(
            DataItem(
                first_name = "Test",
                last_name = "User",
                email = "test@example.com",
                alamat = "Test Address",
                iuran_perwarga = 100,
                total_iuran_rekap = 1200,
                jumlah_iuran_bulanan = 100,
                total_iuran_individu = 100,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test usage",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val response = Response.success(UserResponse(expectedData))
        
        coEvery { mockApiService.getUsers() } returns response
        
        // When
        val result = repository.getUsers()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
        coVerify { mockApiService.getUsers() }
    }
    
    @Test
    fun `getUsers returns failure on network error`() = runTest {
        // Given
        val exception = IOException("Network error")
        coEvery { mockApiService.getUsers() } throws exception
        
        // When
        val result = repository.getUsers()
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }
    
    @Test
    fun `getUsers returns failure on HTTP 500 error`() = runTest {
        // Given
        val response = Response.error<UserResponse>(
            500,
            "Internal Server Error".toResponseBody("application/json".toMediaType())
        )
        coEvery { mockApiService.getUsers() } returns response
        
        // When
        val result = repository.getUsers()
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NetworkException)
    }
}
```

### Unit Tests for ViewModel
```kotlin
@RunWith(MockitoJUnitRunner::class)
class UserViewModelTest {
    
    @Mock
    private lateinit var mockRepository: UserRepository
    
    private lateinit var viewModel: UserViewModel
    
    @Before
    fun setup() {
        viewModel = UserViewModel(mockRepository)
    }
    
    @Test
    fun `loadUsers updates state to Success when repository returns data`() = runTest {
        // Given
        val expectedData = listOf(
            DataItem(
                first_name = "Test",
                last_name = "User",
                email = "test@example.com",
                alamat = "Test Address",
                iuran_perwarga = 100,
                total_iuran_rekap = 1200,
                jumlah_iuran_bulanan = 100,
                total_iuran_individu = 100,
                pengeluaran_iuran_warga = 25,
                pemanfaatan_iuran = "Test",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        coEvery { mockRepository.getUsers() } returns Result.success(expectedData)
        
        // When
        viewModel.loadUsers()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(expectedData, (state as UiState.Success).data)
        coVerify { mockRepository.getUsers() }
    }
    
    @Test
    fun `loadUsers updates state to Error when repository throws exception`() = runTest {
        // Given
        val exception = IOException("Network error")
        coEvery { mockRepository.getUsers() } returns Result.failure(exception)
        
        // When
        viewModel.loadUsers()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("Network error", (state as UiState.Error).message)
    }
}
```

### Integration Tests
```kotlin
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ApiIntegrationTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var apiService: ApiService
    
    @Before
    fun setup() {
        apiService = ApiConfig.getApiService()
    }
    
    @Test
    fun `test API connectivity and data retrieval`() = runTest {
        // When
        val response = apiService.getUsers()
        
        // Then
        assertTrue(response.isSuccessful)
        assertNotNull(response.body())
        assertNotNull(response.body()?.data)
        assertTrue(response.body()!!.data.isNotEmpty())
    }
    
    @Test
    fun `test API returns valid user data structure`() = runTest {
        // When
        val response = apiService.getUsers()
        
        // Then
        assertTrue(response.isSuccessful)
        val data = response.body()?.data
        assertNotNull(data)
        
        data?.let { users ->
            val firstUser = users.first()
            assertNotNull(firstUser.first_name)
            assertNotNull(firstUser.last_name)
            assertNotNull(firstUser.email)
            assertTrue(firstUser.iuran_perwarga > 0)
        }
    }
}
```

### Mock Server Testing with MockWebServer
```kotlin
@RunWith(MockitoJUnitRunner::class)
class ApiConfigTest {
    
    private lateinit var mockServer: MockWebServer
    
    @Before
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()
    }
    
    @After
    fun tearDown() {
        mockServer.shutdown()
    }
    
    @Test
    fun `test with mock server returns expected data`() = runTest {
        // Given
        val mockResponse = """{"data":[
            {
                "first_name":"Test",
                "last_name":"User",
                "email":"test@example.com",
                "alamat":"Test Address",
                "iuran_perwarga":100,
                "total_iuran_rekap":1200,
                "jumlah_iuran_bulanan":100,
                "total_iuran_individu":100,
                "pengeluaran_iuran_warga":25,
                "pemanfaatan_iuran":"Test",
                "avatar":"https://example.com/avatar.jpg"
            }
        ]}"""
        
        mockServer.enqueue(
            MockResponse()
                .setBody(mockResponse)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
        )
        
        // When
        val retrofit = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        val apiService = retrofit.create(ApiService::class.java)
        val response = apiService.getUsers()
        
        // Then
        assertTrue(response.isSuccessful)
        assertEquals("Test", response.body()?.data?.get(0)?.first_name)
    }
}
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