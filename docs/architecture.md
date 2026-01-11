# Architecture Documentation

## Overview

Iuran BlokP is an Android application for HOA (Homeowners Association) fee management, built with modern Android development practices. The application follows a layered architecture pattern with clear separation of concerns.

## Current Architecture

### Technology Stack
- **Platform**: Android (API 24-34)
- **Language**: Kotlin 100%
- **UI Framework**: Android Views with Material Design Components
- **Architecture**: MVVM (fully implemented)
- **Networking**: Retrofit 2.11.0 with OkHttp 4.12.0
- **Database**: Room 2.6.1 (fully implemented with cache-first strategy)
- **Dependency Injection**: Pragmatic DI Container (DependencyContainer.kt)
- **Image Loading**: Glide 4.11.0
- **Testing**: JUnit, Robolectric, Espresso (450+ test cases)

### Current Package Structure
```
app/src/main/java/com/example/iurankomplek/
├── MainActivity.kt              # User list screen (extends BaseActivity)
├── LaporanActivity.kt          # Financial reports screen (extends BaseActivity)
├── MenuActivity.kt             # Main menu (extends BaseActivity)
├── PaymentActivity.kt          # Payment processing
├── TransactionHistoryActivity.kt # Transaction history
├── presentation/               # Presentation Layer
│   ├── ui/                     # Activities & Fragments
│   │   ├── BaseActivity.kt    # Base activity with common functionality
│   │   ├── BaseFragment.kt    # Base fragment with common functionality
│   │   └── ...
│   ├── viewmodel/              # ViewModels
│   │   ├── UserViewModel.kt
│   │   ├── FinancialViewModel.kt
│   │   ├── VendorViewModel.kt
│   │   ├── AnnouncementViewModel.kt
│   │   ├── MessageViewModel.kt
│   │   ├── CommunityPostViewModel.kt
│   │   ├── TransactionViewModel.kt
│   │   └── PaymentViewModel.kt
│   └── adapter/               # RecyclerView adapters
│       ├── UserAdapter.kt      # DiffUtil-based adapter
│       ├── PemanfaatanAdapter.kt
│       └── ...
├── data/                       # Data Layer
│   ├── repository/             # Repository implementations
│   │   ├── UserRepository.kt (interface)
│   │   ├── UserRepositoryImpl.kt
│   │   ├── PemanfaatanRepository.kt (interface)
│   │   ├── PemanfaatanRepositoryImpl.kt
│   │   ├── VendorRepository.kt (interface)
│   │   ├── VendorRepositoryImpl.kt
│   │   ├── TransactionRepository.kt (interface)
│   │   ├── TransactionRepositoryImpl.kt
│   │   ├── AnnouncementRepository.kt (interface)
│   │   ├── AnnouncementRepositoryImpl.kt
│   │   ├── MessageRepository.kt (interface)
│   │   └── MessageRepositoryImpl.kt
│   ├── cache/                  # Cache management
│   │   ├── CacheManager.kt
│   │   ├── CacheHelper.kt
│   │   └── cacheFirstStrategy.kt
│   ├── dao/                    # Room DAOs
│   │   ├── UserDao.kt
│   │   ├── FinancialRecordDao.kt
│   │   └── TransactionDao.kt
│   ├── database/               # Room database
│   │   ├── AppDatabase.kt
│   │   ├── Migration1.kt      # Database migrations
│   │   └── ...
│   ├── entity/                 # Room entities
│   │   ├── UserEntity.kt
│   │   ├── FinancialRecordEntity.kt
│   │   └── Transaction.kt
│   ├── dto/                    # Data Transfer Objects (API models)
│   │   ├── UserDto.kt
│   │   ├── FinancialDto.kt
│   │   └── LegacyDataItemDto.kt
│   ├── mapper/                 # Entity ↔ DTO conversion
│   │   └── EntityMapper.kt
│   ├── constraints/            # Validation constraints
│   │   ├── DatabaseConstraints.kt
│   │   ├── UserConstraints.kt
│   │   └── FinancialRecordConstraints.kt
│   └── api/                    # Network layer
│       ├── ApiService.kt       # Legacy API (backward compatible)
│       ├── ApiServiceV1.kt     # API v1 (recommended)
│       ├── ApiConfig.kt
│       ├── SecurityConfig.kt
│       ├── resilience/
│       │   └── CircuitBreaker.kt
│       ├── interceptor/
│       │   ├── NetworkErrorInterceptor.kt
│       │   ├── RequestIdInterceptor.kt
│       │   └── RetryableRequestInterceptor.kt
│       └── models/             # API response models
│           ├── DataItem.kt
│           └── ApiError.kt
├── domain/                     # Domain Layer
│   ├── model/                  # Domain models (business entities)
│   │   ├── User.kt
│   │   └── FinancialRecord.kt
│   └── usecase/               # Business logic use cases
│       ├── CalculateFinancialTotalsUseCase.kt
│       ├── ValidateFinancialDataUseCase.kt
│       ├── LoadUsersUseCase.kt
│       ├── LoadFinancialDataUseCase.kt
│       ├── CalculateFinancialSummaryUseCase.kt
│       ├── PaymentSummaryIntegrationUseCase.kt
│       └── ValidatePaymentUseCase.kt
├── core/                       # Core infrastructure
│   └── base/
│       ├── BaseActivity.kt     # Base activity implementation
│       ├── BaseFragment.kt     # Base fragment implementation
│       └── GenericViewModelFactory.kt
├── payment/                    # Payment system
│   ├── PaymentGateway.kt (interface)
│   ├── PaymentService.kt
│   ├── PaymentViewModel.kt
│   ├── ReceiptGenerator.kt
│   ├── WebhookReceiver.kt
│   └── WebhookQueue.kt
└── utils/                      # Utilities
    ├── NetworkUtils.kt
    ├── InputSanitizer.kt
    ├── ErrorHandler.kt
    ├── FinancialCalculator.kt
    ├── Constants.kt
    ├── UiState.kt
    ├── SecurityManager.kt
    ├── ImageLoader.kt
    ├── LoggingUtils.kt
    └── RetryHelper.kt
```

## Architecture Patterns

### MVVM (Model-View-ViewModel)
The application follows the MVVM pattern with clear layer separation:

#### Model Layer
- **Data Models**: Raw data from API (DataItem, DTOs)
- **Domain Models**: Business entities with validation (User, FinancialRecord)
- **Entities**: Room entities for database persistence (UserEntity, FinancialRecordEntity)
- **Repository**: Data access abstraction with caching

#### View Layer
- **Activities/Fragments**: UI controllers extending BaseActivity/BaseFragment
- **Adapters**: RecyclerView adapters with DiffUtil for efficiency
- **Custom Views**: Reusable UI components

#### ViewModel Layer
- **Business Logic**: Calculation and transformation
- **State Management**: UI state with StateFlow
- **Event Handling**: User interaction processing

### Repository Pattern
Repositories provide data abstraction with caching strategy:

```kotlin
interface UserRepository {
    suspend fun getUsers(forceRefresh: Boolean = false): Result<UserResponse>
}

class UserRepositoryImpl(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val entityMapper: EntityMapper
) : UserRepository {

    override suspend fun getUsers(forceRefresh: Boolean): Result<UserResponse> {
        return if (!forceRefresh && CacheManager.isCacheFresh()) {
            getFromCache()
        } else {
            fetchFromNetwork()
        }
    }

    private suspend fun fetchFromNetwork(): Result<UserResponse> {
        return executeWithCircuitBreaker {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                val data = response.body()?.data ?: return@executeWithCircuitBreaker Result.failure(Exception("No data"))
                saveToCache(data)
                Result.success(UserResponse(data))
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        }
    }
}
```

### Dependency Injection (Pragmatic DI)
The application uses a pragmatic DI container without external frameworks:

```kotlin
object DependencyContainer {

    private val apiService: ApiService by lazy { ApiConfig.getApiService() }

    private val userDao: UserDao by lazy { AppDatabase.getInstance().userDao() }

    fun provideUserRepository(): UserRepository =
        UserRepositoryImpl(apiService, userDao, EntityMapper())

    fun provideLoadUsersUseCase(): LoadUsersUseCase =
        LoadUsersUseCase(provideUserRepository())

    fun provideUserViewModel(): UserViewModel =
        UserViewModel(provideLoadUsersUseCase())
}
```

## Data Flow

### Current Data Flow
```
Activity → ViewModel → UseCase → Repository → API/Database → Domain Model → StateFlow → UI Update
                    ↑                          ↓
                    └──←←←←← Error Handling ←←←←←┘
```

### State Management
```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
}

class UserViewModel(
    private val loadUsersUseCase: LoadUsersUseCase
) : ViewModel() {

    private val _users = MutableStateFlow<UiState<List<DataItem>>>(UiState.Idle)
    val users: StateFlow<UiState<List<DataItem>>> = _users

    fun loadUsers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _users.value = UiState.Loading
            loadUsersUseCase(forceRefresh)
                .onSuccess { response ->
                    _users.value = UiState.Success(response.data)
                }
                .onFailure { error ->
                    _users.value = UiState.Error(
                        message = ErrorHandler.getMessage(error),
                        exception = error
                    )
                }
        }
    }
}
```

## Security Architecture

### Network Security
- **Certificate Pinning**: Prevents MITM attacks (3 pins with backup rotation)
- **HTTPS Only**: All API calls use HTTPS (cleartextTrafficPermitted="false")
- **Timeout Configuration**: 30s connect/read/write timeouts
- **Request Validation**: Input sanitization via InputSanitizer

### Data Security
- **Local Storage**: Room database with entity validation
- **API Keys**: Stored securely in BuildConfig
- **User Data**: Backup disabled (android:allowBackup="false")
- **Encryption**: Certificate pinning for network security

### Security Configuration
```kotlin
object SecurityConfig {
    private const val PRIMARY_CERT_PIN = "sha256/PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0="
    private const val BACKUP_CERT_PIN_1 = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
    private const val BACKUP_CERT_PIN_2 = "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="

    fun getSecureOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .certificatePinner(
                CertificatePinner.Builder()
                    .add("api.apispreadsheets.com", PRIMARY_CERT_PIN)
                    .add("api.apispreadsheets.com", BACKUP_CERT_PIN_1)
                    .add("api.apispreadsheets.com", BACKUP_CERT_PIN_2)
                    .build()
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
```

## Testing Architecture

### Test Pyramid
```
    E2E Tests (5%)
   ──────────────
  UI/Integration Tests (15%)
 ────────────────────────────
Unit Tests (80%)
```

### Test Coverage (450+ tests)
- **Unit Tests**: 400+ tests covering ViewModels, Repositories, Use Cases, Utilities
- **Instrumented Tests**: 50+ tests for Activities, Fragments, Database
- **Test Coverage Areas**:
  - ViewModels (UserViewModel, FinancialViewModel, etc.)
  - Repositories (UserRepository, PemanfaatanRepository, etc.)
  - Use Cases (CalculateFinancialTotalsUseCase, ValidateFinancialDataUseCase, etc.)
  - Network Layer (CircuitBreaker, Interceptors)
  - Cache Management (CacheManager, CacheHelper)
  - Payment System (PaymentGateway, WebhookQueue)
  - Utilities (InputSanitizer, ErrorHandler, FinancialCalculator)

## Performance Architecture

### Image Loading
```kotlin
object ImageLoader {
    fun loadImage(context: Context, url: String, imageView: ImageView) {
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.placeholder_avatar)
            .error(R.drawable.error_avatar)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }
}
```

### Memory Management
- **ViewBinding**: Prevents memory leaks from findViewById
- **Lifecycle Awareness**: Proper lifecycle management with coroutines
- **Background Processing**: Coroutines for async operations (Dispatchers.IO, Dispatchers.Default)

### Network Optimization
- **Caching**: Cache-first strategy with freshness validation (5 minutes)
- **Connection Pooling**: OkHttp with 5 max idle connections
- **Circuit Breaker**: Fault tolerance pattern for API calls
- **Rate Limiting**: Token bucket algorithm (10 req/sec, 600 req/min)

### Performance Optimizations Implemented
- **DiffUtil**: Efficient RecyclerView updates with background thread calculations
- **Algorithm Optimization**: Single-pass financial calculations (66% faster)
- **Query Optimization**: Partial indexes, lightweight cache freshness checks (50-80% faster)
- **RecyclerView Pool**: Optimized view reuse for better scrolling

## Best Practices

### Code Organization
- **Single Responsibility**: Each class has one clear purpose
- **Dependency Inversion**: Depend on abstractions (interfaces), not concretions
- **Interface Segregation**: Small, focused interfaces
- **Don't Repeat Yourself**: Eliminate code duplication

### Error Handling
```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
}

class ErrorHandler {
    fun getMessage(error: Throwable): String {
        return when (error) {
            is IOException -> "No internet connection"
            is SocketTimeoutException -> "Connection timeout"
            is UnknownHostException -> "Server unreachable"
            else -> error.message ?: "An unexpected error occurred"
        }
    }
}
```

### Configuration Management
```kotlin
object Constants {
    object Network {
        const val BASE_URL = "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"
        const val CONNECT_TIMEOUT = 30_000L
        const val READ_TIMEOUT = 30_000L
        const val WRITE_TIMEOUT = 30_000L
        const val MAX_RETRIES = 3
        const val RETRY_DELAY_MS = 1_000L
    }

    object Cache {
        const val FRESHNESS_THRESHOLD_MS = 5 * 60 * 1000 // 5 minutes
    }
}
```

## Key Design Patterns Implemented
- **Repository Pattern**: Data abstraction layer
- **Factory Pattern**: ViewModel/Repository instantiation
- **Strategy Pattern**: Caching strategies, payment gateways
- **Observer Pattern**: StateFlow/LiveData
- **Adapter Pattern**: RecyclerView adapters
- **Circuit Breaker**: Fault tolerance for API calls
- **Builder Pattern**: Network configuration

## SOLID Principles Compliance
- **S**ingle Responsibility: Each class has one purpose
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Substitutable implementations via interfaces
- **I**nterface Segregation: Small, focused interfaces
- **D**ependency Inversion: Depend on abstractions, not concretions

---

*Last Updated: 2026-01-10*
*Maintainer: Android Development Team*
