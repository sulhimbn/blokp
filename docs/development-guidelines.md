# Development Guidelines

## Overview

This document provides comprehensive guidelines for contributing to the Iuran BlokP Android application. Following these standards ensures code quality, maintainability, and consistency across the project.

## Code Standards

### Kotlin Coding Standards

#### Naming Conventions
```kotlin
// Classes and Objects - PascalCase
class UserManager
object ApiConfig

// Functions and Variables - camelCase
fun getUserById()
val userName: String

// Constants - UPPER_SNAKE_CASE
const val MAX_RETRY_COUNT = 3
const val API_BASE_URL = "https://api.example.com"

// Private properties - camelCase with underscore prefix if needed
private val _users = MutableStateFlow<List<User>>(emptyList())
val users = _users.asStateFlow()
```

#### File Organization
```kotlin
// File header
package com.example.iurankomplek.presentation.ui.main

// Imports (grouped)
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.iurankomplek.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

// Class definition
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    // Properties
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    // Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        observeViewModel()
        setupListeners()
    }
    
    // Private methods
    private fun setupBinding() { /* ... */ }
    private fun observeViewModel() { /* ... */ }
    private fun setupListeners() { /* ... */ }
}
```

#### Function Guidelines
```kotlin
// Single responsibility functions
private fun validateUserInput(email: String, amount: Int): ValidationResult {
    return when {
        !isValidEmail(email) -> ValidationResult.Invalid("Invalid email format")
        amount <= 0 -> ValidationResult.Invalid("Amount must be positive")
        else -> ValidationResult.Valid
    }
}

// Extension functions for utilities
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}
```

### Java Coding Standards (Legacy)

#### Naming Conventions
```java
// Classes - PascalCase
public class MenuActivity extends AppCompatActivity {
    
    // Constants - UPPER_SNAKE_CASE
    private static final String TAG = "MenuActivity";
    
    // Variables - camelCase
    private LinearLayout tombolSatu;
    private LinearLayout tombolDua;
    
    // Methods - camelCase
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setupViews();
        setupListeners();
    }
    
    private void setupViews() { /* ... */ }
    private void setupListeners() { /* ... */ }
}
```

## Architecture Guidelines

### MVVM Implementation

#### ViewModel Pattern
```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val errorHandler: ErrorHandler
) : ViewModel() {
    
    // StateFlow for UI state
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    // Events for one-time actions
    private val _events = MutableSharedFlow<UserEvent>()
    val events = _events.asSharedFlow()
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading
            
            userRepository.getUsers()
                .onSuccess { users ->
                    _uiState.value = if (users.isEmpty()) {
                        UserUiState.Empty
                    } else {
                        UserUiState.Success(users)
                    }
                }
                .onFailure { error ->
                    _uiState.value = UserUiState.Error(
                        message = errorHandler.getMessage(error)
                    )
                    _events.emit(UserEvent.ShowError(errorHandler.getMessage(error)))
                }
        }
    }
}

// UI state sealed class
sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val users: List<User>) : UserUiState()
    object Empty : UserUiState()
    data class Error(val message: String) : UserUiState()
}

// Events sealed class
sealed class UserEvent {
    data class ShowError(val message: String) : UserEvent()
    data class NavigateToDetail(val userId: String) : UserEvent()
    object ShowSuccessMessage : UserEvent()
}
```

#### Repository Pattern
```kotlin
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: String): Result<User>
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(id: String): Result<Unit>
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val cacheManager: CacheManager
) : UserRepository {
    
    override suspend fun getUsers(): Result<List<User>> {
        return try {
            // Try network first
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                val users = response.body()?.data?.map { it.toUser() }
                if (users != null) {
                    // Cache for offline
                    userDao.insertAll(users)
                    cacheManager.cacheUsers(users)
                    Result.success(users)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                // Fallback to cache
                getCachedUsers()
            }
        } catch (e: Exception) {
            // Network error, try cache
            getCachedUsers().getOrElse {
                Result.failure(e)
            }
        }
    }
    
    private suspend fun getCachedUsers(): Result<List<User>> {
        val cachedUsers = userDao.getAll()
        return if (cachedUsers.isNotEmpty()) {
            Result.success(cachedUsers)
        } else {
            Result.failure(Exception("No cached data available"))
        }
    }
}
```

### Dependency Injection with Hilt

#### Module Configuration
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
            .certificatePinner(CertificatePinningConfig.getCertificatePinner())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
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

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
```

## UI Development Guidelines

### ViewBinding Implementation
```kotlin
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        setupObservers()
    }
    
    private fun setupViews() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = userAdapter
        }
        
        binding.refreshButton.setOnClickListener {
            viewModel.refreshUsers()
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UserUiState.Loading -> showLoading()
                    is UserUiState.Success -> showUsers(state.users)
                    is UserUiState.Empty -> showEmptyState()
                    is UserUiState.Error -> showError(state.message)
                }
            }
        }
    }
}
```

### RecyclerView Adapter Pattern
```kotlin
class UserAdapter(
    private val onUserClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onUserClick)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onUserClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            binding.apply {
                nameTextView.text = user.fullName
                emailTextView.text = user.email
                addressTextView.text = user.address
                
                // Load image with Glide
                Glide.with(root.context)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.placeholder_avatar)
                    .error(R.drawable.error_avatar)
                    .circleCrop()
                    .into(avatarImageView)
                
                root.setOnClickListener { onUserClick(user) }
            }
        }
    }
    
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
```

## Testing Guidelines

### Unit Tests
```kotlin
@ExtendWith(MockitoExtension::class)
class UserViewModelTest {
    
    @Mock
    private lateinit var userRepository: UserRepository
    
    @Mock
    private lateinit var errorHandler: ErrorHandler
    
    private lateinit var viewModel: UserViewModel
    
    @BeforeEach
    fun setup() {
        viewModel = UserViewModel(userRepository, errorHandler)
    }
    
    @Test
    fun `loadUsers should emit success when repository returns users`() = runTest {
        // Given
        val expectedUsers = listOf(
            User(id = "1", firstName = "John", lastName = "Doe", email = "john@example.com", address = "Address", avatarUrl = null)
        )
        whenever(userRepository.getUsers()).thenReturn(Result.success(expectedUsers))
        
        // When
        viewModel.loadUsers()
        
        // Then
        assertEquals(UserUiState.Success(expectedUsers), viewModel.uiState.value)
    }
    
    @Test
    fun `loadUsers should emit error when repository fails`() = runTest {
        // Given
        val error = IOException("Network error")
        whenever(userRepository.getUsers()).thenReturn(Result.failure(error))
        whenever(errorHandler.getMessage(error)).thenReturn("No internet connection")
        
        // When
        viewModel.loadUsers()
        
        // Then
        val state = viewModel.uiState.value as UserUiState.Error
        assertEquals("No internet connection", state.message)
    }
}
```

### Integration Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class UserRepositoryIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var userRepository: UserRepository
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
            
        userRepository = UserRepositoryImpl(apiService, mockUserDao, mockCacheManager)
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun getUsers_shouldParseResponseCorrectly() = runTest {
        // Given
        val mockResponse = """
        {
            "data": [
                {
                    "first_name": "John",
                    "last_name": "Doe",
                    "email": "john@example.com",
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
        }
        """
        
        mockWebServer.enqueue(
            MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        // When
        val result = userRepository.getUsers()
        
        // Then
        assertTrue(result.isSuccess)
        val users = result.getOrNull()
        assertEquals(1, users?.size)
        assertEquals("John Doe", users?.first()?.fullName)
    }
}
```

### UI Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun recyclerView_shouldDisplayUsers_whenDataLoaded() {
        // Given
        val mockUsers = listOf(
            User(id = "1", firstName = "John", lastName = "Doe", email = "john@example.com", address = "Address", avatarUrl = null)
        )
        
        // When
        activityRule.scenario.onActivity { activity ->
            // Setup mock data
            (activity as MainActivity).updateUsers(mockUsers)
        }
        
        // Then
        onView(withId(R.id.recyclerView))
            .check(matches(isDisplayed()))
            .check(matches(hasItemCount(1)))
            
        onView(withText("John Doe"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun errorView_shouldDisplay_whenNetworkError() {
        // When
        activityRule.scenario.onActivity { activity ->
            (activity as MainActivity).showError("Network error")
        }
        
        // Then
        onView(withText("Network error"))
            .check(matches(isDisplayed()))
    }
}
```

## Git Workflow

### Branch Naming Convention
```bash
# Feature branches
feature/user-authentication
feature/payment-gateway

# Bugfix branches
fix/financial-calculation-bug
fix/memory-leak-in-main-activity

# Release branches
release/v1.2.0

# Hotfix branches
hotfix/critical-security-patch
```

### Commit Message Format
```bash
# Format: <type>(<scope>): <description>

# Features
feat(auth): add user login functionality
feat(payment): implement payment gateway integration

# Bug fixes
fix(calculation): correct financial formula in LaporanActivity
fix(ui): resolve memory leak in RecyclerView

# Documentation
docs(api): update API documentation for new endpoints
docs(readme): add setup instructions for Docker

# Style changes
style(kotlin): format code according to standards

# Refactoring
refactor(network): extract common retry logic to BaseActivity

# Tests
test(viewmodel): add unit tests for UserViewModel
test(integration): add API integration tests

# Chore
chore(deps): update Android Gradle Plugin to 8.1.0
chore(build): add JaCoCo test coverage reporting
```

### Pull Request Guidelines
```markdown
## Description
Brief description of changes made in this PR.

## Type of Change
- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] UI tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] Build is green
- [ ] No breaking changes without migration plan

## Issue(s)
Closes #123, #456
```

## Performance Guidelines

### Memory Management
```kotlin
// Use ViewBinding instead of findViewById to prevent memory leaks
private lateinit var binding: ActivityMainBinding

// Clear resources in onDestroy
override fun onDestroy() {
    super.onDestroy()
    // Clear any references to prevent memory leaks
    binding.recyclerView.adapter = null
}

// Use WeakReference for long-running operations
class ImageLoader {
    private val imageCache = LruCache<String, Bitmap>(4 * 1024 * 1024) // 4MB
    
    fun loadImage(url: String, target: ImageView) {
        val bitmap = imageCache.get(url)
        if (bitmap != null) {
            target.setImageBitmap(bitmap)
        } else {
            // Load from network
        }
    }
}
```

### Network Optimization
```kotlin
// Implement proper caching
object ApiCache {
    private val cache = Cache(File(context.cacheDir, "api_cache"), 10 * 1024 * 1024) // 10MB
    
    fun get(url: String): Response? {
        return try {
            cache.get(url)?.use { snapshot ->
                Response.Builder()
                    .request(Request.Builder().url(url).build())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .body(snapshot.body)
                    .build()
            }
        } catch (e: Exception) {
            null
        }
    }
}

// Use coroutines for async operations
suspend fun loadData(): Result<List<User>> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUsers()
            Result.success(response.body()?.data ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Security Guidelines

### Input Validation
```kotlin
object ValidationUtils {
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid("Email cannot be empty")
            !email.isValidEmail() -> ValidationResult.Invalid("Invalid email format")
            email.length > 254 -> ValidationResult.Invalid("Email too long")
            else -> ValidationResult.Valid
        }
    }
    
    fun validateAmount(amount: String): ValidationResult {
        return try {
            val value = amount.toDoubleOrNull()
            when {
                value == null -> ValidationResult.Invalid("Invalid number format")
                value < 0 -> ValidationResult.Invalid("Amount cannot be negative")
                value > 10_000_000 -> ValidationResult.Invalid("Amount too large")
                else -> ValidationResult.Valid
            }
        } catch (e: Exception) {
            ValidationResult.Invalid("Invalid amount")
        }
    }
}
```

### Data Protection
```kotlin
// Use encrypted preferences for sensitive data
class SecurePreferences @Inject constructor(
    context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveToken(token: String) {
        encryptedPrefs.edit().putString("auth_token", token).apply()
    }
    
    fun getToken(): String? {
        return encryptedPrefs.getString("auth_token", null)
    }
}
```

---

*Last Updated: November 2025*
*Next Review: Monthly or after major feature releases*
*Maintainer: Development Team Lead*