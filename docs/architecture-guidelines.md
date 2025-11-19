# Architecture Guidelines for BlokP Application

## Overview

BlokP is an Android application for residential complex payment management, built with a hybrid Kotlin/Java architecture. This document provides comprehensive architectural guidelines, design patterns, and best practices for maintaining and extending the application.

## Current Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
├─────────────────────────────────────────────────────────────┤
│  MainActivity.kt    │  LaporanActivity.kt  │ MenuActivity.java │
│  (User List)        │  (Financial Reports) │  (Navigation)     │
├─────────────────────────────────────────────────────────────┤
│                    UI Components                             │
├─────────────────────────────────────────────────────────────┤
│  UserAdapter.kt     │ PemanfaatanAdapter.kt │ Layouts XML     │
│  (RecyclerView)     │ (RecyclerView)        │ (Views)          │
├─────────────────────────────────────────────────────────────┤
│                    Business Logic                           │
├─────────────────────────────────────────────────────────────┤
│  NetworkUtils.kt    │ Financial Calculations  │ Data Models    │
│  (Connectivity)     │ (LaporanActivity)       │ (DataItem.kt)  │
├─────────────────────────────────────────────────────────────┤
│                    Data Layer                               │
├─────────────────────────────────────────────────────────────┤
│  ApiConfig.kt       │ ApiService.kt          │ Retrofit        │
│  (Configuration)    │ (Endpoints)            │ (HTTP Client)   │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack
- **Platform**: Android SDK API 34
- **Languages**: Kotlin (primary), Java (legacy compatibility)
- **Build System**: Gradle with Kotlin DSL
- **Architecture Pattern**: MVVM-Light (Activities as Views, minimal ViewModels)
- **Networking**: Retrofit 2 + OkHttp 3
- **Image Loading**: Glide 4.16.0
- **JSON Parsing**: Gson 2.10.1
- **UI Framework**: Android Jetpack (AppCompat, ConstraintLayout, RecyclerView)
- **Testing**: JUnit 4, Espresso

## Design Patterns

### 1. Repository Pattern (Light Implementation)
```kotlin
// Current implementation in ApiConfig.kt
object ApiConfig {
    fun getApiService(): ApiService {
        // Configuration and setup
        return retrofit.create(ApiService::class.java)
    }
}

// Future enhancement: Proper Repository
class UserRepository(private val apiService: ApiService) {
    suspend fun getUsers(): Result<List<DataItem>> {
        return try {
            val response = apiService.getUsers()
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 2. Adapter Pattern for RecyclerView
```kotlin
// UserAdapter.kt - Efficient RecyclerView implementation
class UserAdapter(private var users: MutableList<DataItem>) :
    RecyclerView.Adapter<UserAdapter.ListViewHolder>() {
    
    fun setUsers(newUsers: List<DataItem>) {
        val diffCallback = UserDiffCallback(this.users, newUsers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        this.users.clear()
        this.users.addAll(newUsers)
        diffResult.dispatchUpdatesTo(this)
    }
}
```

### 3. Builder Pattern for API Configuration
```kotlin
// ApiConfig.kt - Retrofit configuration
val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClientBuilder.build())
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

## Component Guidelines

### Activities

#### MainActivity.kt (User Management)
**Responsibilities:**
- Display list of users/warga
- Handle user data fetching from API
- Manage user interactions and navigation
- Implement error handling and retry logic

**Key Methods:**
```kotlin
private fun getUser(currentRetryCount: Int = 0) // API data fetching
private fun showLoading() // Show loading state (future enhancement)
private fun hideLoading() // Hide loading state (future enhancement)
```

#### LaporanActivity.kt (Financial Reports)
**Responsibilities:**
- Display financial calculations and reports
- Implement business logic for financial calculations
- Handle pemanfaatan (utilization) data
- Manage financial data visualization

**Key Business Logic:**
```kotlin
// Financial calculation with special multiplier
for (dataItem in dataArray) {
    totalIuranBulanan += dataItem.iuran_perwarga
    totalPengeluaran += dataItem.pengeluaran_iuran_warga
    totalIuranIndividu += dataItem.total_iuran_individu * 3 // Business rule
}
var rekapIuran = totalIuranIndividu - totalPengeluaran
```

#### MenuActivity.java (Navigation Hub)
**Responsibilities:**
- Provide navigation to main features
- Implement fullscreen mode for immersive experience
- Handle user flow and app entry point

**Note:** This component is written in Java for legacy compatibility. Consider migrating to Kotlin in future iterations.

### Data Models

#### DataItem.kt (Core Data Model)
```kotlin
data class DataItem(
    val first_name: String,           // User information
    val last_name: String,
    val email: String,
    val alamat: String,
    val iuran_perwarga: Int,          // Financial data
    val total_iuran_rekap: Int,
    val jumlah_iuran_bulanan: Int,
    val total_iuran_individu: Int,
    val pengeluaran_iuran_warga: Int,
    val pemanfaatan_iuran: String,    // Utilization description
    val avatar: String                // Profile image
)
```

**Design Principles:**
- Immutable data classes for safety
- Primitive types for performance (Int for currency)
- Clear naming conventions
- Comprehensive field documentation

### Network Layer

#### ApiService.kt (API Interface)
```kotlin
interface ApiService {
    @GET("users")
    fun getUsers(): Call<UserResponse>
    
    @GET("pemanfaatan")
    fun getPemanfaatan(): Call<PemanfaatanResponse>
}
```

#### ApiConfig.kt (Configuration Management)
**Key Features:**
- Environment switching (debug vs production)
- Certificate pinning for security
- Mock API integration for development
- HTTP client configuration

**Environment Handling:**
```kotlin
private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
private const val BASE_URL = if (USE_MOCK_API) {
    "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/"
} else {
    "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"
}
```

## Best Practices

### Code Organization

#### Package Structure
```
com.example.iurankomplek/
├── MainActivity.kt              # Main user list activity
├── LaporanActivity.kt           # Financial reports activity
├── MenuActivity.java            # Navigation activity (Java)
├── UserAdapter.kt               # User list adapter
├── PemanfaatanAdapter.kt        # Financial data adapter
├── model/                       # Data models
│   ├── DataItem.kt
│   ├── UserResponse.kt
│   └── PemanfaatanResponse.kt
├── network/                     # Network layer
│   ├── ApiConfig.kt
│   └── ApiService.kt
└── utils/                       # Utility classes
    └── NetworkUtils.kt
```

#### Naming Conventions
- **Activities**: `XxxActivity.kt`
- **Adapters**: `XxxAdapter.kt`
- **Models**: `Xxx.kt` (data classes)
- **Network**: `ApiConfig.kt`, `ApiService.kt`
- **Utils**: `XxxUtils.kt`

### Error Handling Guidelines

#### Network Error Handling
```kotlin
// Implemented retry logic with exponential backoff
override fun onFailure(call: Call<UserResponse>, t: Throwable) {
    if (currentRetryCount < maxRetries) {
        Handler(Looper.getMainLooper()).postDelayed({
            getUser(currentRetryCount + 1)
        }, 1000L * (currentRetryCount + 1))
    } else {
        Toast.makeText(this@MainActivity, 
            "Network error: ${t.message}. Failed after ${maxRetries + 1} attempts", 
            Toast.LENGTH_LONG).show()
    }
}
```

#### Best Practices:
1. **Always check network connectivity** before API calls
2. **Implement retry logic** with exponential backoff
3. **Provide user-friendly error messages**
4. **Log errors for debugging** (but not sensitive data)
5. **Handle null responses** gracefully

### Performance Optimization

#### RecyclerView Optimization
```kotlin
// Use DiffUtil for efficient updates
class UserDiffCallback(
    private val oldList: List<DataItem>,
    private val newList: List<DataItem>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].email == newList[newItemPosition].email
    }
}
```

#### Image Loading Optimization
```kotlin
// Glide configuration for efficient image loading
Glide.with(holder.itemView.context)
    .load(user.avatar)
    .apply(RequestOptions().override(80, 80)
        .placeholder(R.drawable.icon_avatar))
    .transform(CircleCrop())
    .into(holder.tvAvatar)
```

### Security Guidelines

#### Certificate Pinning
```kotlin
private fun getCertificatePinner(): CertificatePinner {
    return CertificatePinner.Builder()
        .add("api.apispreadsheets.com", "sha256/PIdO5FV9mQyEclv5rMC4oGNTya7Q9S5/Sn1KTWpQov0=")
        .build()
}
```

#### Security Best Practices:
1. **Use certificate pinning** for production APIs
2. **Never log sensitive data** (tokens, passwords)
3. **Implement proper timeout** configurations
4. **Validate and sanitize** API responses
5. **Keep dependencies updated** for security patches

## Testing Architecture

### Unit Testing
```kotlin
// Example: Financial calculation testing
class LaporanActivityCalculationTest {
    @Test
    fun testTotalIuranIndividuCalculation_accumulatesCorrectly() {
        val testItems = listOf(
            DataItem(iuran_perwarga = 100, total_iuran_individu = 50, pengeluaran_iuran_warga = 25)
        )
        
        var totalIuranIndividu = 0
        for (dataItem in testItems) {
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }
        
        assertEquals(150, totalIuranIndividu) // 50 * 3
    }
}
```

### Testing Guidelines
1. **Test business logic** separately from UI
2. **Mock external dependencies** (API calls, network)
3. **Test edge cases** (empty data, null values)
4. **Achieve 80%+ code coverage** for critical components
5. **Use parameterized tests** for multiple scenarios

## Future Architecture Evolution

### Planned Improvements

#### 1. Full MVVM Implementation
```kotlin
// Future: ViewModel implementation
class UserViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _users = MutableLiveData<List<DataItem>>()
    val users: LiveData<List<DataItem>> = _users
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = userRepository.getUsers()
            _users.value = result.getOrDefault(emptyList())
            _isLoading.value = false
        }
    }
}
```

#### 2. Repository Pattern with Caching
```kotlin
// Future: Enhanced repository
class UserRepository(
    private val apiService: ApiService,
    private val userCache: UserCache
) {
    suspend fun getUsers(): Result<List<DataItem>> {
        return try {
            val cachedUsers = userCache.getUsers()
            if (cachedUsers.isNotEmpty()) {
                Result.success(cachedUsers)
            } else {
                val response = apiService.getUsers()
                userCache.saveUsers(response.data)
                Result.success(response.data)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 3. Dependency Injection with Hilt
```kotlin
// Future: Hilt integration
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return ApiConfig.getApiService()
    }
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var userViewModel: UserViewModel
}
```

### Migration Strategy

#### Phase 1: Stabilization (Current)
- Fix critical bugs and security issues
- Improve error handling and user experience
- Add comprehensive testing

#### Phase 2: Architecture Enhancement (Q2 2025)
- Implement proper ViewModels
- Add Repository pattern
- Introduce dependency injection

#### Phase 3: Modernization (Q3-Q4 2025)
- Migrate to Kotlin fully
- Implement coroutines for async operations
- Add modern Android architecture components

## Code Review Guidelines

### Review Checklist
1. **Architecture Compliance**
   - [ ] Follows established patterns
   - [ ] Proper separation of concerns
   - [ ] Consistent with existing codebase

2. **Code Quality**
   - [ ] No hardcoded strings or magic numbers
   - [ ] Proper error handling
   - [ ] Comprehensive testing
   - [ ] Clear documentation

3. **Security**
   - [ ] No sensitive data in logs
   - [ ] Proper certificate pinning
   - [ ] Input validation
   - [ ] Secure data handling

4. **Performance**
   - [ ] Efficient RecyclerView usage
   - [ ] Proper memory management
   - [ ] Optimized image loading
   - [ ] Minimal API calls

### Review Process
1. **Self-review** before creating PR
2. **Peer review** by at least one team member
3. **Senior review** for architectural changes
4. **Security review** for authentication/payment features
5. **Testing review** to ensure coverage

## Documentation Standards

### Code Documentation
```kotlin
/**
 * Calculates total financial recap for all users.
 * 
 * @param dataArray List of user financial data
 * @return Pair of (totalIuranIndividu, rekapIuran)
 * 
 * Note: Uses business logic multiplier of 3 for individual totals
 * as per requirement specification.
 */
private fun calculateFinancialRecap(dataArray: List<DataItem>): Pair<Int, Int> {
    // Implementation
}
```

### API Documentation
- Document all endpoints in `docs/api-documentation.md`
- Include request/response examples
- Document error scenarios
- Keep authentication requirements clear

### Architecture Documentation
- Maintain this architecture guide
- Document major architectural decisions
- Include rationale for pattern choices
- Update with each major version

---

## Conclusion

This architecture guide provides the foundation for maintaining and extending the BlokP application. The current hybrid architecture serves the immediate needs while providing a clear path for modernization. Following these guidelines ensures consistency, maintainability, and scalability as the application evolves.

*Last Updated: November 2025*
*Architecture Version: 1.0*
*Next Review: December 2025*