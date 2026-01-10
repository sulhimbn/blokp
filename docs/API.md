# API Documentation

## Overview

IuranKomplek API menyediakan endpoints untuk mengambil data pengguna dan informasi pemanfaatan iuran komplek. API menggunakan layanan pihak ketiga (API Spreadsheets) untuk penyimpanan data.

## API Specification

**[OpenAPI 3.0 Specification](openapi.yaml)** - Machine-readable API contract (OpenAPI/Swagger)
**[API Integration Patterns](API_INTEGRATION_PATTERNS.md)** - Circuit breaker, rate limiting, retry logic
**[API Headers and Error Responses](API_HEADERS_AND_ERRORS.md)** - HTTP headers, error codes, resilience headers, retry strategies

The OpenAPI specification provides:
- Standardized API contract for all endpoints
- Schema definitions for request/response models
- Error response specifications
- Authentication methods
- Tooling support (Swagger UI, code generation)

## API Versioning

The application supports two API versions:

### Legacy API (ApiService)
- Base path: `/data/{SPREADSHEET_ID}/`
- Response format: Direct data objects
- Use case: Backward compatibility
- Status: Maintained for compatibility

### Version 1 API (ApiServiceV1) - **Recommended**
- Base path: `/api/v1/`
- Response format: Standardized wrappers (`ApiResponse<T>`, `ApiListResponse<T>`)
- Features:
  - Consistent error handling
  - Request tracking (request_id)
  - Timestamps for all responses
  - Pagination support
  - Rate limiting and circuit breaker protection
- Use case: New integrations, improved resilience
- Status: **Active development**

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

### User & Financial Data Endpoints (API v1)

#### GET /api/v1/users

Mengambil data pengguna/warga dengan wrapper standar.

**Response Format:**
```json
{
  "data": {
    "users": [...]
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

#### GET /api/v1/pemanfaatan

Mengambil data pemanfaatan iuran dengan wrapper standar.

**Response Format:**
```json
{
  "data": {
    "financial_records": [...]
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

### Communication Endpoints (API v1)

#### GET /api/v1/announcements

Mengambil pengumuman komunitas dengan wrapper standar dan pagination.

**Response Format:**
```json
{
  "data": [
    {"id": 1, "title": "Pengumuman 1", ...},
    {"id": 2, "title": "Pengumuman 2", ...}
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

#### GET /api/v1/messages?userId={userId}

Mengambil pesan untuk pengguna tertentu.

#### GET /api/v1/messages/{receiverId}?senderId={senderId}

Mengambil percakapan dengan pengguna tertentu.

#### POST /api/v1/messages

Mengirim pesan baru.

**Request Format:**
```json
{
  "sender_id": "user_123",
  "receiver_id": "user_456",
  "content": "Halo, apa kabar?",
  "timestamp": 1704672000000
}
```

**Response Format:**
```json
{
  "data": {
    "id": "msg_789",
    "sender_id": "user_123",
    "receiver_id": "user_456",
    "content": "Halo, apa kabar?",
    "timestamp": 1704672000000
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

### Payment Processing Endpoints (API v1)

#### POST /api/v1/payments/initiate

Memulai proses pembayaran.

**Request Format:**
```json
{
  "amount": "150000",
  "description": "Pembayaran iuran bulanan",
  "customer_id": "user_123",
  "payment_method": "BANK_TRANSFER"
}
```

**Response Format:**
```json
{
  "data": {
    "transaction_id": "txn_abc123",
    "status": "PENDING",
    "amount": 150000,
    "payment_method": "BANK_TRANSFER",
    "created_at": 1704672000000
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

#### GET /api/v1/payments/{id}/status

Mengambil status pembayaran.

**Response Format:**
```json
{
  "data": {
    "transaction_id": "txn_abc123",
    "status": "COMPLETED",
    "amount": 150000,
    "updated_at": 1704672001000
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672001000
}
```

#### POST /api/v1/payments/{id}/confirm

Mengonfirmasi pembayaran.

**Response Format:**
```json
{
  "data": {
    "transaction_id": "txn_abc123",
    "status": "COMPLETED",
    "confirmed_at": 1704672002000
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672002000
}
```

### Vendor Management Endpoints (API v1)

#### GET /api/v1/vendors

Mengambil daftar vendor dengan pagination.

**Response Format:**
```json
{
  "data": {
    "vendors": [...]
  },
  "pagination": {
    "page": 1,
    "page_size": 20,
    "total_items": 15,
    "total_pages": 1,
    "has_next": false,
    "has_previous": false
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

#### POST /api/v1/vendors

Membuat vendor baru.

**Request Format:**
```json
{
  "name": "Vendor ABC",
  "contact": "08123456789",
  "address": "Jl. Contoh No. 123",
  "services": ["Perbaikan", "Kebersihan"]
}
```

#### PUT /api/v1/vendors/{id}

Update vendor yang sudah ada.

#### GET /api/v1/vendors/{id}

Mengambil detail vendor tertentu.

### Work Order Endpoints (API v1)

#### GET /api/v1/work-orders

Mengambil daftar work order dengan pagination.

#### POST /api/v1/work-orders

Membuat work order baru.

#### PUT /api/v1/work-orders/{id}/assign

Menugaskan vendor ke work order.

#### PUT /api/v1/work-orders/{id}/status

Update status work order.

#### GET /api/v1/work-orders/{id}

Mengambil detail work order tertentu.

### Legacy Endpoints (Backward Compatibility)

#### GET /data/QjX6hB1ST2IDKaxB/users

Mengambil data pengguna/warga (legacy format).

#### GET /data/QjX6hB1ST2IDKaxB/pemanfaatan

Mengambil data pemanfaatan iuran (legacy format).

#### GET /data/QjX6hB1ST2IDKaxB/announcements

Mengambil pengumuman komunitas (legacy format).

#### GET /data/QjX6hB1ST2IDKaxB/messages?userId={userId}

Mengambil pesan untuk pengguna tertentu (legacy format).

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

##### API v1 - Standardized Response Wrappers

API v1 uses standardized response wrappers for all endpoints:

**Single Object Response (ApiResponse<T>)**
```json
{
  "data": {
    "user": {
      "id": "user_123",
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
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

**List Response with Pagination (ApiListResponse<T>)**
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
    },
    {
      "first_name": "Jane",
      "last_name": "Smith",
      "email": "jane.smith@example.com",
      "alamat": "Jl. Contoh No. 456, Jakarta",
      "iuran_perwarga": 150000,
      "total_iuran_rekap": 1800000,
      "jumlah_iuran_bulanan": 150000,
      "total_iuran_individu": 150000,
      "pengeluaran_iuran_warga": 60000,
      "pemanfaatan_iuran": "Perbaikan taman komplek",
      "avatar": "https://example.com/avatar2.jpg"
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

**Response Wrapper Fields:**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `data` | T or List<T> | Response data (single object or list) | See above |
| `pagination` | PaginationMetadata | Pagination metadata (for list responses) | See below |
| `request_id` | String | Unique request identifier for tracing | "req_1234567890" |
| `timestamp` | Long | Response timestamp in milliseconds | 1704672000000 |

**Pagination Metadata Fields:**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `page` | Integer | Current page number (1-indexed) | 1 |
| `page_size` | Integer | Number of items per page | 20 |
| `total_items` | Integer | Total number of items across all pages | 50 |
| `total_pages` | Integer | Total number of pages | 3 |
| `has_next` | Boolean | Whether there is a next page | true |
| `has_previous` | Boolean | Whether there is a previous page | false |

##### Legacy API Response Format

Legacy endpoints return direct data without wrapper:

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

**Legacy API (ApiService):**
```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): Response<UserResponse>
    
    @GET("pemanfaatan")
    suspend fun getPemanfaatan(): Response<PemanfaatanResponse>
    
    @GET("vendors")
    suspend fun getVendors(): Response<VendorResponse>
}
```

**API v1 (ApiServiceV1) - Recommended:**
```kotlin
interface ApiServiceV1 {
    @GET("api/v1/users")
    suspend fun getUsers(): Response<ApiResponse<UserResponse>>
    
    @GET("api/v1/pemanfaatan")
    suspend fun getPemanfaatan(): Response<ApiResponse<PemanfaatanResponse>>
    
    @GET("api/v1/announcements")
    suspend fun getAnnouncements(): Response<ApiListResponse<Announcement>>
    
    @GET("api/v1/messages")
    suspend fun getMessages(@Query("userId") userId: String): Response<ApiListResponse<Message>>
    
    @POST("api/v1/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<ApiResponse<Message>>
    
    @POST("api/v1/payments/initiate")
    suspend fun initiatePayment(@Body request: InitiatePaymentRequest): Response<ApiResponse<PaymentResponse>>
    
    @GET("api/v1/payments/{id}/status")
    suspend fun getPaymentStatus(@Path("id") id: String): Response<ApiResponse<PaymentStatusResponse>>
    
    @POST("api/v1/payments/{id}/confirm")
    suspend fun confirmPayment(@Path("id") id: String): Response<ApiResponse<PaymentConfirmationResponse>>
    
    @GET("api/v1/vendors")
    suspend fun getVendors(): Response<ApiResponse<VendorResponse>>
    
    @POST("api/v1/vendors")
    suspend fun createVendor(@Body request: CreateVendorRequest): Response<ApiResponse<SingleVendorResponse>>
    
    @PUT("api/v1/vendors/{id}")
    suspend fun updateVendor(
        @Path("id") id: String,
        @Body request: UpdateVendorRequest
    ): Response<ApiResponse<SingleVendorResponse>>
    
    @GET("api/v1/work-orders")
    suspend fun getWorkOrders(): Response<ApiResponse<WorkOrderResponse>>
    
    @POST("api/v1/work-orders")
    suspend fun createWorkOrder(@Body request: CreateWorkOrderRequest): Response<ApiResponse<SingleWorkOrderResponse>>
    
    @PUT("api/v1/work-orders/{id}/assign")
    suspend fun assignVendorToWorkOrder(
        @Path("id") id: String,
        @Body request: AssignVendorRequest
    ): Response<ApiResponse<SingleWorkOrderResponse>>
    
    @PUT("api/v1/work-orders/{id}/status")
    suspend fun updateWorkOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateWorkOrderRequest
    ): Response<ApiResponse<SingleWorkOrderResponse>>
}
```

#### Configuration with Circuit Breaker and Rate Limiting
```kotlin
 object ApiConfig {
     private val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
     private val BASE_URL = if (USE_MOCK_API) {
         Constants.Api.MOCK_BASE_URL + BuildConfig.API_SPREADSHEET_ID + "/"
     } else {
         Constants.Api.PRODUCTION_BASE_URL + BuildConfig.API_SPREADSHEET_ID + "/"
     }
     
     // Connection pool for efficient HTTP connection reuse
     private val connectionPool = ConnectionPool(
         Constants.Network.MAX_IDLE_CONNECTIONS,
         Constants.Network.KEEP_ALIVE_DURATION_MINUTES,
         TimeUnit.MINUTES
     )
     
     // Circuit breaker for service resilience
     val circuitBreaker: CircuitBreaker = CircuitBreaker(
         failureThreshold = Constants.Network.MAX_RETRIES,
         successThreshold = 2,
         timeout = Constants.Network.MAX_RETRY_DELAY_MS,
         halfOpenMaxCalls = 3
     )
     
     // Rate limiter for preventing API overload
     val rateLimiter: RateLimiterInterceptor = RateLimiterInterceptor(
         maxRequestsPerSecond = Constants.Network.MAX_REQUESTS_PER_SECOND,
         maxRequestsPerMinute = Constants.Network.MAX_REQUESTS_PER_MINUTE,
         enableLogging = BuildConfig.DEBUG
     )
     
     // Singleton pattern with thread-safe initialization
     @Volatile
     private var apiServiceV1Instance: ApiServiceV1? = null
     
     fun getApiServiceV1(): ApiServiceV1 {
         return apiServiceV1Instance ?: synchronized(this) {
             apiServiceV1Instance ?: createApiServiceV1().also { apiServiceV1Instance = it }
         }
     }
     
     private fun createApiServiceV1(): ApiServiceV1 {
         val okHttpClient = if (!USE_MOCK_API) {
             // Use secure client for production
             SecurityConfig.getSecureOkHttpClient()
                 .newBuilder()
                 .connectionPool(connectionPool)
                 .addInterceptor(RequestIdInterceptor())
                 .addInterceptor(rateLimiter)
                 .addInterceptor(RetryableRequestInterceptor())
                 .addInterceptor(NetworkErrorInterceptor(enableLogging = BuildConfig.DEBUG))
                 .build()
         } else {
             // For debug/mock, use basic client but log warning
             val clientBuilder = OkHttpClient.Builder()
                 .connectTimeout(Constants.Network.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                 .readTimeout(Constants.Network.READ_TIMEOUT, TimeUnit.SECONDS)
                 .connectionPool(connectionPool)
                 .addInterceptor(RequestIdInterceptor())
                 .addInterceptor(rateLimiter)
                 .addInterceptor(RetryableRequestInterceptor())
                 .addInterceptor(NetworkErrorInterceptor(enableLogging = true))
             
             // Add logging interceptor only for debug builds
             if (BuildConfig.DEBUG) {
                 val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
                     level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                 }
                 clientBuilder.addInterceptor(loggingInterceptor)
             }
             
             clientBuilder.build()
         }
         
         val retrofit = Retrofit.Builder()
             .baseUrl(BASE_URL)
             .client(okHttpClient)
             .addConverterFactory(GsonConverterFactory.create())
             .build()
         return retrofit.create(ApiServiceV1::class.java)
     }
     
     suspend fun resetCircuitBreaker() {
         circuitBreaker.reset()
     }
     
     fun getCircuitBreakerState(): CircuitBreakerState {
         return circuitBreaker.getState()
     }
     
     fun getRateLimiterStats(): Map<String, RateLimiterInterceptor.EndpointStats> {
         return rateLimiter.getAllStats()
     }
     
     fun resetRateLimiter() {
         rateLimiter.reset()
     }
 }
```

**Interceptor Chain Order:**
1. **RequestIdInterceptor** - Adds unique request ID for tracing
2. **RateLimiterInterceptor** - Enforces rate limits (token bucket algorithm)
3. **RetryableRequestInterceptor** - Marks safe-to-retry requests
4. **NetworkErrorInterceptor** - Parses errors and converts to NetworkError

**Interceptors Purpose:**
- **RequestIdInterceptor**: Request tracking and debugging
- **RateLimiterInterceptor**: API abuse prevention, burst handling
- **RetryableRequestInterceptor**: Retry logic optimization
- **NetworkErrorInterceptor**: Standardized error handling, exception conversion
```

#### Repository Pattern with Circuit Breaker and Retry

**API v1 Repository with Resilience:**
```kotlin
interface UserRepository {
    suspend fun getUsers(forceRefresh: Boolean = false): Result<UserResponse>
    suspend fun getUserById(userId: String): Result<User>
}

class UserRepositoryImpl(
    private val apiService: ApiServiceV1,
    private val circuitBreaker: CircuitBreaker
) : UserRepository {
    
    override suspend fun getUsers(forceRefresh: Boolean): Result<UserResponse> {
        return executeWithCircuitBreaker {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                throw NetworkError.HttpError(
                    code = ApiErrorCode.fromHttpCode(response.code()),
                    userMessage = "Gagal memuat data pengguna",
                    httpCode = response.code(),
                    details = response.body()?.error?.details
                )
            }
        }
    }
    
    private suspend fun <T> executeWithCircuitBreaker(block: suspend () -> T): Result<T> {
        return when (val result = circuitBreaker.execute { block() }) {
            is CircuitBreakerResult.Success -> Result.success(result.value)
            is CircuitBreakerResult.Failure -> Result.failure(result.exception)
            is CircuitBreakerResult.CircuitOpen -> {
                Result.failure(NetworkError.CircuitBreakerError(
                    userMessage = "Layanan sementara tidak tersedia. Silakan coba lagi nanti."
                ))
            }
        }
    }
}

// Dependency Injection for consistent instantiation
class DependencyContainer {
    private val apiService: ApiServiceV1 by lazy {
        ApiConfig.getApiServiceV1()
    }
    
    private val circuitBreaker: CircuitBreaker by lazy {
        ApiConfig.circuitBreaker
    }
    
    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(apiService, circuitBreaker)
    }
    
    fun getUserRepository(): UserRepository = userRepository
}
```

#### ViewModel with StateFlow and Error Handling
```kotlin
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<UserResponse>>(UiState.Loading)
    val uiState: StateFlow<UiState<UserResponse>> = _uiState.asStateFlow()
    
    fun loadUsers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            repository.getUsers(forceRefresh)
                .onSuccess { userResponse ->
                    if (userResponse.users.isEmpty()) {
                        _uiState.value = UiState.Error("Tidak ada data pengguna")
                    } else {
                        _uiState.value = UiState.Success(userResponse)
                    }
                }
                .onFailure { error ->
                    val message = when (error) {
                        is NetworkError.HttpError -> {
                            when (error.code) {
                                ApiErrorCode.RATE_LIMIT_EXCEEDED -> 
                                    "Terlalu banyak permintaan. Silakan tunggu sebentar."
                                ApiErrorCode.SERVICE_UNAVAILABLE ->
                                    "Layanan sementara tidak tersedia. Silakan coba lagi nanti."
                                else -> error.userMessage
                            }
                        }
                        is NetworkError.CircuitBreakerError ->
                            "Layanan sementara tidak tersedia. Silakan coba lagi nanti."
                        is NetworkError.TimeoutError ->
                            "Waktu permintaan habis. Silakan coba lagi."
                        is NetworkError.ConnectionError ->
                            "Tidak ada koneksi internet. Silakan periksa jaringan Anda."
                        else -> "Terjadi kesalahan yang tidak terduga."
                    }
                    _uiState.value = UiState.Error(message)
                }
        }
    }
    
    fun refreshUsers() {
        loadUsers(forceRefresh = true)
    }
}

// Factory for ViewModel instantiation with dependency injection
class UserViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

#### Usage Example in Activity
```kotlin
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory(DependencyContainer().getUserRepository())
    }
    private lateinit var adapter: UserAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        observeViewModel()
        setupSwipeRefresh()
        viewModel.loadUsers()
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvUsers.visibility = View.GONE
                        binding.errorLayout.visibility = View.GONE
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvUsers.visibility = View.VISIBLE
                        binding.errorLayout.visibility = View.GONE
                        adapter.submitList(state.data.users)
                        binding.swipeRefresh.isRefreshing = false
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvUsers.visibility = View.GONE
                        binding.errorLayout.visibility = View.VISIBLE
                        binding.errorMessage.text = state.message
                        binding.swipeRefresh.isRefreshing = false
                        announceForAccessibility(state.message)
                    }
                }
            }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = UserAdapter(DependencyContainer().getUserRepository())
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
            itemAnimator = null // Improve performance
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshUsers()
        }
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
| 422 | Unprocessable Entity | Check validation errors |
| 408 | Request Timeout | Retry with backoff |
| 429 | Too Many Requests | Wait and retry |
| 500 | Internal Server Error | Retry with backoff |
| 503 | Service Unavailable | Display offline message |

### Error Response Format

#### API v1 - Standardized Error Response

API v1 uses standardized error response format with detailed error information:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": "Email field is required",
    "field": "email"
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

**Error Response Fields:**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `error` | ApiErrorDetail | Error detail object | See below |
| `request_id` | String | Unique request identifier | "req_1234567890" |
| `timestamp` | Long | Response timestamp in milliseconds | 1704672000000 |

**ApiErrorDetail Fields:**

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `code` | String | Standard error code | "VALIDATION_ERROR" |
| `message` | String | Human-readable error message | "Invalid request parameters" |
| `details` | String | Additional error details (optional) | "Email field is required" |
| `field` | String | Field with validation error (optional) | "email" |

**Standard Error Codes:**

| Error Code | HTTP Status | Description | User Action |
|-------------|--------------|-------------|--------------|
| `BAD_REQUEST` | 400 | Invalid request parameters | Check request body |
| `UNAUTHORIZED` | 401 | Authentication required | Log in again |
| `FORBIDDEN` | 403 | Access denied | Check permissions |
| `NOT_FOUND` | 404 | Resource not found | Verify resource ID |
| `CONFLICT` | 409 | Resource conflict | Check for duplicates |
| `VALIDATION_ERROR` | 422 | Validation failed | Fix validation errors |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests | Wait and retry |
| `INTERNAL_SERVER_ERROR` | 500 | Server error | Retry with backoff |
| `SERVICE_UNAVAILABLE` | 503 | Service unavailable | Display offline message |
| `TIMEOUT` | 504 | Request timeout | Retry with backoff |
| `NETWORK_ERROR` | N/A | Network connection error | Check internet connection |
| `UNKNOWN_ERROR` | N/A | Unexpected error | Contact support |

**Error Response Examples:**

**Validation Error:**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request parameters",
    "details": "Email is already registered",
    "field": "email"
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

**Rate Limit Error:**
```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests",
    "details": "Rate limit: 10 requests/second. Retry after: 500ms",
    "field": null
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

**Not Found Error:**
```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Resource not found",
    "details": "Vendor with ID 'vendor_123' does not exist",
    "field": "id"
  },
  "request_id": "req_1234567890",
  "timestamp": 1704672000000
}
```

#### Legacy Error Response Format

Legacy endpoints use simple error format:

```json
{
  "error": "Data not found",
  "message": "Spreadsheet not found or inaccessible"
}
```

**Note:** For new integrations, use API v1 for standardized error handling and better resilience.

### Circuit Breaker Pattern

The application implements a Circuit Breaker pattern to prevent cascading failures:

**Circuit Breaker States:**
- **CLOSED**: Normal operation, requests pass through
- **OPEN**: Circuit is open, requests fail fast without hitting the service
- **HALF_OPEN**: Testing if service has recovered (limited requests allowed)

**Circuit Breaker Configuration:**
```kotlin
CircuitBreaker(
    failureThreshold = 3,      // Failures before opening circuit
    successThreshold = 2,      // Successes before closing circuit
    timeout = 60000L,          // Time before attempting recovery (60s)
    halfOpenMaxCalls = 3        // Max requests in half-open state
)
```

**State Transitions:**
1. **CLOSED → OPEN**: After 3 consecutive failures
2. **OPEN → HALF_OPEN**: After 60 seconds (timeout)
3. **HALF_OPEN → CLOSED**: After 2 consecutive successes
4. **HALF_OPEN → OPEN**: On any failure

**Usage in Repositories:**
```kotlin
class UserRepositoryImpl(
    private val apiService: ApiServiceV1
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> {
        return executeWithCircuitBreaker {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data.users)
            } else {
                throw NetworkError.HttpError(
                    code = ApiErrorCode.fromHttpCode(response.code()),
                    userMessage = "Failed to load users",
                    httpCode = response.code()
                )
            }
        }
    }
}
```

### Rate Limiting Pattern

The application implements multi-level rate limiting to prevent API abuse:

**Rate Limiting Configuration:**
```kotlin
RateLimiterInterceptor(
    maxRequestsPerSecond = 10,  // Token bucket refill rate
    maxRequestsPerMinute = 600,  // Per-minute limit
    useTokenBucket = true         // Use token bucket algorithm
)
```

**Token Bucket Algorithm:**
- Bucket starts with 600 tokens (maxRequestsPerMinute)
- Tokens refill at 10 tokens/second (maxRequestsPerSecond)
- Each request consumes 1 token
- Requests are blocked when bucket is empty
- Allows temporary bursts (up to 600 requests instantly)

**Rate Limit Error Handling:**
```kotlin
try {
    val response = apiService.getUsers()
    processResponse(response)
} catch (e: NetworkError.HttpError) {
    if (e.code == ApiErrorCode.RATE_LIMIT_EXCEEDED) {
        val waitTime = ApiConfig.rateLimiter.getTimeToNextToken()
        showRetryDialog(
            message = "Terlalu banyak permintaan. Coba lagi dalam ${waitTime}ms.",
            waitTime = waitTime
        )
    }
}
```

**Rate Limiter Monitoring:**
```kotlin
val stats = ApiConfig.getRateLimiterStats()
stats.forEach { (endpoint, stats) ->
    println("Endpoint: $endpoint")
    println("Requests: ${stats.requestCount}")
    println("Last Request: ${stats.lastRequestTime}")
}
```

**Per-Endpoint Rate Limiting:**
Rate limiting is tracked per endpoint:
- `GET:/api/v1/users` - Separate limit for users endpoint
- `GET:/api/v1/pemanfaatan` - Separate limit for pemanfaatan endpoint
- `POST:/api/v1/messages` - Separate limit for messages endpoint

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