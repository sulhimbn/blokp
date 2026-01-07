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

// Constants - UPPER_SNAKE_CASE
const val BASE_URL = "https://api.example.com"
const val MAX_RETRY_COUNT = 3

// Private properties - camelCase with underscore prefix
private val _users = mutableListOf<DataItem>()
```

#### File Structure
```kotlin
package com.example.iurankomplek

// Imports (standard library first, then third-party, then project)
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.iurankomplek.model.DataItem
import retrofit2.Call

class MainActivity : AppCompatActivity() {
    // 1. Companion object
    companion object {
        private const val TAG = "MainActivity"
    }
    
    // 2. Private properties
    private lateinit var adapter: UserAdapter
    private lateinit var rv_users: RecyclerView
    
    // 3. Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
        getUser()
    }
    
    // 4. Private setup methods
    private fun setupViews() {
        rv_users = findViewById(R.id.rv_users)
        adapter = UserAdapter(mutableListOf())
        rv_users.layoutManager = LinearLayoutManager(this)
        rv_users.adapter = adapter
    }
    
    // 5. Private business logic methods
    private fun getUser() {
        // Implementation
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

### Java Standards (for MenuActivity.java)

#### Naming Conventions
```java
// Classes - PascalCase
public class MenuActivity extends AppCompatActivity

// Methods & Variables - camelCase
private LinearLayout tombolSatu;
private void setupClickListeners()

// Constants - UPPER_SNAKE_CASE
private static final String TAG = "MenuActivity";
```

#### Code Style
```java
public class MenuActivity extends AppCompatActivity {
    LinearLayout tombolSatu;
    LinearLayout tombolDua;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        
        setupFullscreenMode();
        setupClickListeners();
    }
    
    private void setupFullscreenMode() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
    
    private void setupClickListeners() {
        tombolSatu = findViewById(R.id.cdMenu1);
        tombolSatu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        
        // Similar implementation for tombolDua
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
object ApiConfig {
    private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
    
    fun getApiService(): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(getOkHttpClient())
            .build()
        return retrofit.create(ApiService::class.java)
    }
    
    private fun getBaseUrl(): String = if (USE_MOCK_API) {
        "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/\n\n"
    } else {
        "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/\n\n"
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
class LaporanActivityCalculationTest {
    
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
private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null

// Environment-specific configurations
object Config {
    const val API_TIMEOUT = 30_000L
    const val RETRY_COUNT = 3
    const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB
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