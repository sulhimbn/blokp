# Development Guidelines

## Overview

This document provides comprehensive guidelines for contributing to the IuranKomplek Android application. Following these standards ensures code quality, maintainability, and team collaboration efficiency.

## Code Standards

### Kotlin Standards

#### Naming Conventions
```kotlin
// Classes - PascalCase
class UserAdapter
class LaporanActivity

// Functions & Variables - camelCase
private fun getUser()
val totalIuranBulanan = 0

// Constants - UPPER_SNAKE_CASE (centralized in Constants.kt)
import com.example.iurankomplek.utils.Constants
const val MAX_RETRY_COUNT = Constants.Network.MAX_RETRIES

// Private properties - camelCase with underscore prefix
private val _users = mutableListOf<DataItem>()
```

#### File Structure
```kotlin
package com.example.iurankomplek.presentation.ui.activity

// Imports (standard library first, then third-party, then project)
import android.os.Bundle
import android.widget.Toast
import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.presentation.adapter.UserAdapter
import retrofit2.Call

class MainActivity : BaseActivity() {
    // 1. Companion object
    companion object {
        private const val TAG = "MainActivity"
    }

    // 2. Private properties
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: UserAdapter

    // 3. Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        getUser()
    }

    // 4. Private setup methods
    private fun setupViews() {
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(mutableListOf())
        binding.rvUsers.adapter = adapter
    }

    // 5. Private business logic methods
    private fun getUser() {
        // Implementation
    }

    // 6. Cleanup
    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}
```

#### Error Handling with Coroutines
```kotlin
private fun getUser() {
    lifecycleScope.launch {
        try {
            val response = apiService.getUsers()
            
            when (response.code()) {
                200 -> handleSuccessResponse(response)
                404 -> showErrorMessage("Data tidak ditemukan")
                500 -> showErrorMessage("Server error, coba lagi nanti")
                else -> showErrorMessage("Terjadi kesalahan: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error", e)
            showErrorMessage("Error: ${e.message}")
        }
    }
}

private fun handleSuccessResponse(response: Response<UserResponse>) {
    val dataArray = response.body()?.data
    if (dataArray != null) {
        adapter.setUsers(dataArray)
    } else {
        showEmptyState()
    }
}
```

### Kotlin Standards (for MenuActivity.kt)

#### Naming Conventions
```kotlin
// Classes - PascalCase
class MenuActivity : BaseActivity

// Methods & Variables - camelCase
private lateinit var binding: ActivityMenuBinding
private fun setupClickListeners()

// Constants - UPPER_SNAKE_CASE
private companion object {
    private const val TAG = "MenuActivity"
}
```

#### Code Style
```kotlin
class MenuActivity : BaseActivity() {
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cdMenu1.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.cdMenu2.setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }

        binding.cdMenu3.setOnClickListener {
            startActivity(Intent(this, CommunicationActivity::class.java))
        }

        binding.cdMenu4.setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }
    }
}
```

## Architecture Guidelines

### MVVM Pattern
```
Activities/Fragment (View)
    ↓
ViewModels (Business Logic)
    ↓
Repository/ApiConfig (Data Layer)
    ↓
API Services (Network)
```

### Repository Pattern
```kotlin
// Repository interface
interface UserRepository {
    suspend fun getUsers(): Result<List<DataItem>>
}

// Repository implementation with CircuitBreaker
class UserRepositoryImpl(
    private val apiService: ApiService
) : UserRepository {
    
    private val circuitBreaker = ApiConfig.circuitBreaker
    
    override suspend fun getUsers(): Result<List<DataItem>> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(NetworkException(response.code()))
            }
        } catch (e: Exception) {
            circuitBreaker.recordFailure()
            Result.failure(e)
        }
    }
}

// Factory pattern for repository instantiation
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

// Usage in ViewModel
class UserViewModel : ViewModel() {
    private val repository = UserRepositoryFactory.getInstance()
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

// Usage in Activity with Factory pattern
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory()
    }

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

### Adapter Pattern with DiffUtil
```kotlin
class UserAdapter : ListAdapter<DataItem, UserAdapter.ListViewHolder>(DiffCallback) {
    
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DataItem>() {
            override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
                return oldItem.email == newItem.email
            }
            
            override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
```

## Testing Guidelines

### Unit Tests
```kotlin
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
}

class FinancialCalculatorTest {
    
    @Test
    fun testTotalIuranIndividuCalculation_accumulatesCorrectly() {
        // Given
        val testItems = listOf(
            DataItem(iuran_perwarga = 100, total_iuran_individu = 50, pengeluaran_iuran_warga = 25),
            DataItem(iuran_perwarga = 200, total_iuran_individu = 75, pengeluaran_iuran_warga = 30)
        )
        
        // When
        var totalIuranIndividu = 0
        for (dataItem in testItems) {
            totalIuranIndividu += dataItem.total_iuran_individu * 3
        }
        
        // Then
        assertEquals(375, totalIuranIndividu) // (50*3) + (75*3)
    }
}

class UserViewModelTest {
    
    private lateinit var mockRepository: UserRepository
    private lateinit var viewModel: UserViewModel
    
    @Before
    fun setup() {
        mockRepository = mock()
        viewModel = UserViewModel(mockRepository)
    }
    
    @Test
    fun `loadUsers updates state to Success when repository returns data`() = runTest {
        // Given
        val expectedData = listOf(DataItem(
            first_name = "Test", last_name = "User", 
            email = "test@example.com", alamat = "Test Address",
            iuran_perwarga = 100, total_iuran_rekap = 1200,
            jumlah_iuran_bulanan = 100, total_iuran_individu = 100,
            pengeluaran_iuran_warga = 25, pemanfaatan_iuran = "Test",
            avatar = "https://example.com/avatar.jpg"
        ))
        coEvery { mockRepository.getUsers() } returns Result.success(expectedData)
        
        // When
        viewModel.loadUsers()
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(expectedData, (state as UiState.Success).data)
    }
}
```

### Test Coverage Requirements
- **Critical Business Logic**: 100% coverage
- **Network Layer**: 90% coverage
- **UI Components**: 80% coverage
- **Overall Target**: 85% coverage

### Test Organization
```
app/src/test/
├── unit/
│   ├── calculation/
│   │   └── LaporanActivityCalculationTest.kt
│   ├── network/
│   │   └── ApiConfigTest.kt
│   └── utils/
└── integration/
    └── ApiIntegrationTest.kt
```

## Git Workflow

### Branch Strategy
```
main (production)
├── develop (staging)
├── feature/user-management
├── feature/financial-reports
├── bugfix/calculation-error
└── hotfix/security-patch
```

### Commit Message Format
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

#### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

#### Examples
```
feat(api): add certificate pinning for security

Implement SSL certificate pinning to prevent man-in-the-middle attacks.
Added network security configuration and updated ApiConfig.

Closes #49

fix(calculation): correct financial formula in LaporanActivity

Fixed the total_iuran_individu accumulation logic that was only
taking the last item instead of summing all items.

Closes #18

docs(readme): update setup instructions for Docker environment

Added detailed steps for Docker setup and troubleshooting guide.
```

### Branch Naming
- `feature/feature-name`
- `bugfix/issue-description`
- `hotfix/critical-fix`
- `release/version-number`

## Code Review Process

### Pull Request Template
```markdown
## Description
Brief description of changes made.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed
- [ ] Edge cases considered

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No TODO comments left
- [ ] No hardcoded values
- [ ] Error handling implemented

## Related Issues
Closes #issue-number
```

### Review Guidelines
1. **Functionality**: Does the code work as intended?
2. **Architecture**: Does it follow established patterns?
3. **Performance**: Are there performance implications?
4. **Security**: Are there security considerations?
5. **Testing**: Is adequate test coverage provided?
6. **Documentation**: Is the code well-documented?

## Build & Deployment

### Build Commands
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Install debug APK
./gradlew installDebug
```

### Environment Configuration
```kotlin
// BuildConfig.DEBUG automatically set by Gradle
import com.example.iurankomplek.utils.Constants

// Use centralized constants from Constants.kt
object Config {
    const val API_TIMEOUT = Constants.Network.CONNECT_TIMEOUT
    const val RETRY_COUNT = Constants.Network.MAX_RETRIES
    const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB
}

// Constants.kt structure:
object Constants {
    object Api {
        const val PRODUCTION_BASE_URL = "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"
        const val MOCK_BASE_URL = "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/"
        const val DOCKER_ENV_KEY = "DOCKER_ENV"
    }
    
    object Network {
        const val MAX_RETRIES = 3
        const val MAX_RETRY_DELAY_MS = 30000L
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val MAX_IDLE_CONNECTIONS = 5
        const val KEEP_ALIVE_DURATION_MINUTES = 5L
    }
    
    object UI {
        const val ANIMATION_DURATION = 300L
        const val CLICK_DELAY = 500L
    }
}
```

## Security Guidelines

### Network Security
- Always use HTTPS in production
- Implement certificate pinning
- Never log sensitive data
- Validate all API responses

### Data Protection
```kotlin
// Don't log sensitive information
Log.d(TAG, "User: ${user.email}") // ❌ Bad
Log.d(TAG, "User ID: ${user.id}") // ✅ Good

// Secure API configuration
private fun getSecureOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .certificatePinner(getCertificatePinner())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .build()
}
```

## Performance Guidelines

### RecyclerView Optimization
```kotlin
// Use DiffUtil instead of notifyDataSetChanged()
class UserAdapter : ListAdapter<DataItem, UserAdapter.ListViewHolder>(DiffCallback) {
    
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DataItem>() {
            override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
                return oldItem.email == newItem.email
            }
            
            override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
```

### Memory Management
```kotlin
// Use view binding instead of findViewById
private lateinit var binding: ActivityMainBinding

override fun onDestroy() {
    super.onDestroy()
    // Clear references to prevent memory leaks
    adapter = null
    binding.unbind()
}
```

## Troubleshooting

### Common Issues

#### Build Errors
```bash
# Clean and rebuild
./gradlew clean build

# Clear Gradle cache
./gradlew clean build --refresh-dependencies

# Reset Android Studio
File → Invalidate Caches / Restart
```

#### Network Issues
```kotlin
// Check network connectivity
private fun isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) 
        as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}
```

#### API Issues
```bash
# Test API endpoints
curl -X GET "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/"

# Check mock API
curl -X GET "http://localhost:8080/data/QjX6hB1ST2IDKaxB/"
```

## Onboarding Checklist

### Environment Setup
- [ ] Android Studio installed
- [ ] JDK 8+ configured
- [ ] Git configured with SSH keys
- [ ] Docker installed (for development)
- [ ] Repository cloned
- [ ] Dependencies built successfully

### First Tasks
- [ ] Read README.md and architecture documentation
- [ ] Set up development environment
- [ ] Run existing tests
- [ ] Make a small test change
- [ ] Create first pull request

### Learning Resources
1. [Android Developer Documentation](https://developer.android.com/)
2. [Kotlin Documentation](https://kotlinlang.org/docs/)
3. [Retrofit Documentation](https://square.github.io/retrofit/)
4. [RecyclerView Best Practices](https://developer.android.com/topic/performance/vitals/render)

---

*This document should be updated as the project evolves and new patterns are established.*