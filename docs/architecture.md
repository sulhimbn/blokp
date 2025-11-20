# Architecture Documentation

## Overview

Iuran BlokP is an Android application for HOA (Homeowners Association) fee management, built with modern Android development practices. The application follows a layered architecture pattern with clear separation of concerns.

## Current Architecture

### Technology Stack
- **Platform**: Android (API 24-34)
- **Language**: Kotlin (primary), Java (legacy - being migrated)
- **UI Framework**: Android Views with Material Design Components
- **Architecture**: MVVM (being implemented)
- **Networking**: Retrofit 2 with OkHttp 3
- **Database**: Room (planned for payment features)
- **Dependency Injection**: Hilt (planned)
- **Image Loading**: Glide 4.16.0
- **Testing**: JUnit, Mockito, Espresso

### Package Structure (Current)
```
com.example.iurankomplek/
├── MainActivity.kt              # User list screen
├── LaporanActivity.kt          # Financial reports screen  
├── MenuActivity.java           # Main menu (legacy Java)
├── model/                      # Data models
│   ├── DataItem.kt
│   ├── UserResponse.kt
│   └── PemanfaatanResponse.kt
├── network/                    # API layer
│   ├── ApiConfig.kt
│   └── ApiService.kt
├── utils/                      # Utility classes
│   └── NetworkUtils.kt
└── adapters/                   # RecyclerView adapters
    ├── UserAdapter.kt
    └── PemanfaatanAdapter.kt
```

### Target Package Structure (Post-Refactoring)
```
com.example.iurankomplek/
├── di/                         # Dependency injection modules
├── data/                       # Data layer
│   ├── model/                  # Data models
│   │   ├── user/
│   │   ├── financial/
│   │   └── payment/
│   ├── repository/             # Repository implementations
│   └── remote/                 # Remote data sources
├── domain/                     # Business logic layer
│   ├── model/                  # Domain models
│   ├── repository/             # Repository interfaces
│   └── usecase/                # Use cases
├── presentation/               # Presentation layer
│   ├── ui/                     # UI components
│   │   ├── main/
│   │   ├── laporan/
│   │   ├── menu/
│   │   └── payment/
│   ├── viewmodel/              # ViewModels
│   └── common/                 # Common UI components
└── core/                       # Core utilities
    ├── network/
    ├── security/
    ├── validation/
    └── extensions/
```

## Architecture Patterns

### MVVM (Model-View-ViewModel)
The application is transitioning to MVVM pattern:

#### Model Layer
- **Data Models**: Raw data from API or database
- **Domain Models**: Business entities with validation
- **Repository**: Data access abstraction

#### View Layer  
- **Activities/Fragments**: UI controllers
- **Adapters**: RecyclerView adapters
- **Custom Views**: Reusable UI components

#### ViewModel Layer
- **Business Logic**: Calculation and transformation
- **State Management**: UI state with LiveData/StateFlow
- **Event Handling**: User interaction processing

### Repository Pattern
```kotlin
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: String): Result<User>
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(id: String): Result<Unit>
}

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
) : UserRepository {
    override suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                val users = response.body()?.data?.map { it.toUser() }
                if (users != null) {
                    userDao.insertAll(users)
                    Result.success(users)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                // Fallback to cache
                val cachedUsers = userDao.getAll()
                if (cachedUsers.isNotEmpty()) {
                    Result.success(cachedUsers)
                } else {
                    Result.failure(Exception("Network error and no cache"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Dependency Injection with Hilt
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

## Data Flow

### Current Data Flow
```
Activity → Retrofit → API Spreadsheet → JSON Response → DataItem → UI Update
```

### Target Data Flow
```
Activity → ViewModel → Repository → API/Database → Domain Model → UI State → UI Update
```

### State Management
```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _users = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val users = _users.asStateFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _users.value = UiState.Loading
            userRepository.getUsers()
                .onSuccess { users ->
                    _users.value = if (users.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(users)
                    }
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
- **Certificate Pinning**: Prevents MITM attacks
- **HTTPS Only**: All API calls use HTTPS
- **Timeout Configuration**: Prevents hanging connections
- **Request Validation**: Input sanitization and validation

### Data Security
- **Local Storage**: Sensitive data encrypted
- **API Keys**: Stored securely in build config
- **User Data**: Minimal data collection and storage

### Authentication & Authorization
```kotlin
object SecurityConfig {
    private const val CERTIFICATE_PINNER = "sha256/PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0="
    
    fun getSecureOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .certificatePinner(
                CertificatePinner.Builder()
                    .add("api.apispreadsheets.com", CERTIFICATE_PINNER)
                    .build()
            )
            .addInterceptor(AuthInterceptor())
            .addInterceptor(ValidationInterceptor())
            .build()
    }
}
```

## Testing Architecture

### Test Pyramid
```
    E2E Tests (10%)
   ─────────────────
  UI/Integration Tests (20%)
 ─────────────────────────
Unit Tests (70%)
```

### Unit Tests
- **ViewModels**: Business logic testing
- **Repositories**: Data layer testing  
- **Use Cases**: Domain logic testing
- **Utilities**: Helper function testing

### Integration Tests
- **API Layer**: Network communication testing
- **Database**: Room database testing
- **Repository**: Data flow testing

### UI Tests
- **Activities**: User interaction testing
- **Fragments**: Component behavior testing
- **Navigation**: Flow testing

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
- **Lifecycle Awareness**: Proper lifecycle management
- **Background Processing**: Coroutines for async operations

### Network Optimization
- **Caching**: Response caching for offline support
- **Compression**: GZIP compression for API calls
- **Batching**: Multiple requests combined when possible

## Migration Strategy

### Phase 1: Foundation (Week 1-2)
1. Convert MenuActivity.java to Kotlin
2. Implement BaseActivity for common functionality
3. Setup dependency injection with Hilt
4. Create base repository pattern

### Phase 2: Architecture (Week 2-4)
1. Implement MVVM for MainActivity
2. Implement MVVM for LaporanActivity
3. Create comprehensive unit tests
4. Setup CI/CD pipeline improvements

### Phase 3: Enhancement (Week 4-8)
1. Add payment processing architecture
2. Implement security enhancements
3. Add comprehensive error handling
4. Performance optimizations

## Best Practices

### Code Organization
- **Single Responsibility**: Each class has one clear purpose
- **Dependency Inversion**: Depend on abstractions, not concretions
- **Interface Segregation**: Small, focused interfaces
- **Don't Repeat Yourself**: Eliminate code duplication

### Error Handling
```kotlin
sealed class AppError : Exception() {
    object NetworkError : AppError()
    object ValidationError : AppError()
    data class ApiError(val code: Int, override val message: String) : AppError()
    data class UnknownError(override val message: String) : AppError()
}

class ErrorHandler {
    fun handleError(error: Throwable): String {
        return when (error) {
            is AppError.NetworkError -> "No internet connection"
            is AppError.ValidationError -> "Invalid input data"
            is AppError.ApiError -> "API Error: ${error.message}"
            else -> "An unexpected error occurred"
        }
    }
}
```

### Configuration Management
```kotlin
object AppConfig {
    const val API_BASE_URL = "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"
    const val CONNECT_TIMEOUT = 30_000L
    const val READ_TIMEOUT = 30_000L
    const val MAX_RETRY_COUNT = 3
    const val RETRY_DELAY_MS = 1_000L
}
```

---

*Last Updated: November 2025*
*Next Review: After architecture migration completion*
*Maintainer: Android Development Team*