# IuranKomplek Architecture Documentation

## Overview

IuranKomplek adalah aplikasi Android untuk mengelola pembayaran iuran komplek perumahan/apartemen. Aplikasi ini dibangun dengan Kotlin 100% dan mengikuti pola MVVM modern.

## Technology Stack

### Core Technologies
- **Platform**: Android SDK API level 34
- **Languages**: Kotlin 100%
- **Build System**: Gradle 8.1.0
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)

### Key Dependencies
```gradle
// Core Android
androidx.core:core-ktx:1.13.1
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.12.0
androidx.constraintlayout:constraintlayout:2.1.4

// UI Components
androidx.recyclerview:recyclerview:1.3.2

// Networking
com.squareup.retrofit2:retrofit:2.6.4
com.squareup.retrofit2:converter-gson:2.6.4
com.squareup.okhttp3:logging-interceptor:3.8.0

// Image Loading
com.github.bumptech.glide:glide:4.11.0

// JSON Processing
com.google.code.gson:gson:2.8.9

// Debugging (debug only)
com.github.chuckerteam.chucker:library:3.3.0
```

## Architecture Patterns

### 1. MVVM Light Pattern
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Activities    │───▶│   ViewModels     │───▶│   Repositories  │
│   (Views)       │    │   (Business)     │    │   (Data)        │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Components │    │   LiveData       │    │   API Services  │
│   (Adapters)    │    │   (State)        │    │   (Network)     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### 2. Repository Pattern
```kotlin
// ApiConfig.kt - Single source of truth for API configuration
object ApiConfig {
    fun getApiService(): ApiService {
        // Retrofit configuration
        // Base URL switching (debug/production)
        // Network interceptors
    }
}
```

### 3. Adapter Pattern
```kotlin
// RecyclerView adapters for different data types
UserAdapter        // User list display
PemanfaatanAdapter // Financial usage display
LaporanAdapter     // Reports (currently unused)
```

## Component Architecture

### Activities Layer
```
MenuActivity (Kotlin)
├── Navigation hub
├── Fullscreen mode
└── Intent routing

MainActivity (Kotlin)
├── User list display
├── RecyclerView with UserAdapter
└── API data fetching

LaporanActivity (Kotlin)
├── Financial reports
├── PemanfaatanAdapter
├── Calculation logic
└── Data aggregation
```

### Network Layer
```
ApiService (Interface)
├── @GET(".") getUsers()
└── @GET(".") getPemanfaatan()

ApiConfig (Object)
├── Retrofit builder
├── Environment switching
├── Interceptor configuration
└── Service creation

Models
├── DataItem (data class)
└── ResponseUser (data class)
```

### Data Flow
```
1. User Interaction
   ↓
2. Activity/Fragment
   ↓
3. ViewModel (if implemented)
   ↓
4. Repository/ApiConfig
   ↓
5. API Service (Retrofit)
   ↓
6. Network Response
   ↓
7. Model Parsing (Gson)
   ↓
8. UI Update (Adapter)
   ↓
9. Screen Refresh
```

## Data Models

### DataItem Structure
```kotlin
data class DataItem(
    val first_name: String,           // User's first name
    val last_name: String,            // User's last name
    val email: String,                // Contact email
    val alamat: String,               // Residential address
    val iuran_perwarga: Int,          // Monthly dues amount
    val total_iuran_rekap: Int,       // Annual total
    val jumlah_iuran_bulanan: Int,    // Monthly collection
    val total_iuran_individu: Int,    // Individual total
    val pengeluaran_iuran_warga: Int, // Expenses amount
    val pemanfaatan_iuran: String,    // Usage description
    val avatar: String                // Profile image URL
)
```

### Response Structure
```kotlin
data class ResponseUser(
    val data: List<DataItem>
)
```

## API Architecture

### Endpoints
```
Base URL: https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/

User & Financial Data:
- GET users - Returns user data
- GET pemanfaatan - Returns financial usage data

Communication:
- GET announcements - Community announcements
- GET messages - Message retrieval
- POST messages - Send messages
- GET community-posts - Community posts
- POST community-posts - Create community posts

Payment:
- POST payments/initiate - Initiate payment
- GET payments/{id}/status - Get payment status
- POST payments/{id}/confirm - Confirm payment

Vendor Management:
- GET vendors - List vendors
- POST vendors - Create vendor
- PUT vendors/{id} - Update vendor

Work Orders:
- GET work-orders - List work orders
- POST work-orders - Create work order
- PUT work-orders/{id}/assign - Assign vendor to work order
- PUT work-orders/{id}/status - Update work order status

Development:
├── Mock API: http://api-mock:5000
├── Docker container
└── Same endpoint structure
```

### Environment Configuration
```kotlin
private const val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
private const val BASE_URL = if (USE_MOCK_API) {
    "http://api-mock:5000/data/QjX6hB1ST2IDKaxB/\n\n"
} else {
    "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/\n\n"
}
```

## UI Architecture

### Layout Hierarchy
```
activity_menu.xml
├── LinearLayout (vertical)
├── cdMenu1 (CardView) → MainActivity
└── cdMenu2 (CardView) → LaporanActivity

activity_main.xml
├── RecyclerView
└── UserAdapter items

activity_laporan.xml
├── TextView components (4)
├── RecyclerView
└── PemanfaatanAdapter items
```

### Adapter Patterns
```kotlin
// UserAdapter - Using DiffUtil for efficient updates
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

## Business Logic

### Financial Calculations
```kotlin
// LaporanActivity.kt - Core calculation logic
for (dataItem in dataArray) {
    totalIuranBulanan += dataItem.iuran_perwarga
    totalPengeluaran += dataItem.pengeluaran_iuran_warga
    totalIuranIndividu += dataItem.total_iuran_individu * 3  // Special multiplier
}

var rekapIuran = totalIuranIndividu - totalPengeluaran
```

### Data Validation
```kotlin
// Current validation (minimal)
if (response.isSuccessful) {
    val dataArray = response.body()?.data
    if (dataArray != null) {
        // Process data
    } else {
        Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show()
    }
}
```

## Caching Architecture

### Cache Strategy Components
- **CacheManager**: Singleton untuk database access dan management
- **CacheStrategies**: cache-first dan network-first patterns
- **DatabasePreloader**: Index validation dan integrity checks
- **CacheConstants**: Cache configuration management

### Cache-First Flow
1. Repository menerima data request
2. Check cache untuk existing data
3. Jika data exists dan fresh (dalam 5 min threshold), return cached data
4. Jika data stale atau missing, fetch dari network API
5. Save API response ke cache (upsert logic untuk updates)
6. Return network data
7. Jika network fails, fallback ke cached data

### Offline Support
- Network unavailable → automatically fallback ke cached data
- UI displays cached data dengan clear indication
- Data synchronization dengan API saat network tersedia

## Webhook Reliability Architecture

### Webhook Components
- **WebhookEvent**: Room entity dengan idempotency index
- **WebhookEventDao**: Database operations untuk webhooks
- **WebhookQueue**: Processing engine dengan retry logic
- **WebhookReceiver**: Reception dan enqueue webhooks

### Reliability Features
- **Persistent Storage**: Semua webhooks disimpan sebelum processing
- **Idempotency**: Duplicate webhook prevention dengan unique keys
- **Exponential Backoff**: Retry logic dengan jitter (thundering herd prevention)
- **Graceful Degradation**: Fallback ke immediate processing jika queue unavailable
- **Audit Trail**: Full webhook lifecycle tracking

## Security Architecture

### Current Security Measures
- ✅ HTTPS for production API
- ✅ Debug-only network inspection (Chucker)
- ✅ Basic error handling
- ✅ Certificate pinning dengan backup pin (prevents single point of failure)
- ✅ Network security configuration dengan proper SSL enforcement
- ✅ Lifecycle-aware coroutines (prevents memory leaks)
- ✅ Sanitized logging (no sensitive data exposure)
- ✅ Up-to-date dependencies (androidx.core-ktx 1.13.1)

### Security Gaps
- ❌ Data encryption at rest (Room database encryption)

## Testing Architecture

### Current Test Coverage
```
test/
├── Repository Tests (UserRepositoryImpl, PemanfaatanRepository, VendorRepository) - 60+ tests
├── ViewModel Tests (UserViewModel, FinancialViewModel, VendorViewModel) - 15+ tests
├── Utility Tests (DataValidator, ErrorHandler, FinancialCalculator) - 60+ tests
├── Network Tests (CircuitBreaker, NetworkError models, Interceptors) - 45+ tests
├── Cache Tests (CacheManager, CacheStrategies) - 31 tests
├── Payment Tests (PaymentViewModel, RealPaymentGateway, WebhookReceiver, PaymentService) - 65+ tests
├── Adapter Tests (UserAdapter, PemanfaatanAdapter, LaporanSummaryAdapter, etc.) - 74+ tests
└── Webhook Tests (WebhookQueue, WebhookEventDao, Migration2) - 34 tests

androidTest/
├── Database Tests (UserDao, FinancialRecordDao, Migration1, Migration2) - 51 tests
└── Integration Tests (ApiIntegration, PaymentApi, etc.)
```

### Test Coverage Summary
- **Total Test Cases**: 400+ unit tests
- **Total Instrumented Tests**: 50+ database/integration tests
- **Coverage Areas**: Repositories, ViewModels, Utilities, Network, Cache, Payment, Webhooks, Adapters
- **Critical Path Coverage**: 100% untuk business logic
- **Edge Case Testing**: Boundary conditions, null values, special characters

### Test Strategy
- **Unit Tests**: Business logic validation, edge cases, error handling
- **Integration Tests**: API communication, database operations
- **UI Tests**: User interaction flows (Espresso)
- **Mock Tests**: Development environment validation (MockWebServer)

## Development Environment

### Docker Setup
```yaml
services:
  android-builder:  # Build environment
  api-mock:        # Mock API server
  dev-tools:       # VS Code development
```

### Build Configuration
```gradle
android {
    compileSdk 34
    defaultConfig {
        applicationId "com.example.iurankomplek"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }
}
```

## Performance Considerations

### Current Performance Issues
1. **RecyclerView Optimization**: Using notifyDataSetChanged()
2. **Image Loading**: No caching strategy for avatars
3. **Memory Usage**: Potential memory leaks in adapters
4. **Network**: No request caching mechanism

### Optimization Opportunities
1. **DiffUtil Implementation**: Smarter RecyclerView updates
2. **Glide Configuration**: Memory-efficient image loading
3. **Paging Library**: Large dataset handling
4. **Network Caching**: Offline data support

## Scalability Architecture

### Current Limitations
- Single API endpoint dependency
- Limited error recovery mechanisms
- Monolithic activity structure

### Future Scalability Plans
1. **Multiple API Sources**: Flexible data providers
2. **Microservices**: Modular backend architecture
3. **Cloud Integration**: Firebase/AWS backend

## Deployment Architecture

### CI/CD Pipeline (GitHub Actions)
```
.github/workflows/android-ci.yml
├── Build Job
│   ├── Lint checks
│   ├── Debug build
│   ├── Release build
│   ├── Unit tests
│   └── Artifacts (APKs, lint reports, test reports)
└── Instrumented Tests Job
    ├── Matrix testing (API levels 29 dan 34)
    ├── Android emulator
    └── Connected Android tests

.github/workflows/opencode-*.yml
├── OpenCode flows untuk code analysis
├── Autonomous agent system
└── PR handling workflows
```

### CI/CD Features
- **Automated Testing**: Lint, unit tests, instrumented tests
- **Matrix Testing**: Multiple API levels untuk compatibility
- **Artifact Management**: Debug APKs, test reports, lint reports
- **Path Filtering**: CI only runs pada relevant changes
- **Gradle Caching**: Faster CI builds dengan dependency caching
- **Green Builds**: Semua checks harus pass sebelum merge

### Release Strategy
- **Semantic Versioning**: major.minor.patch
- **Draft Releases**: Pre-release validation
- **GitHub Releases**: Distribution platform

## Monitoring & Analytics

### Current Monitoring
- GitHub Actions workflow status
- Build success/failure rates
- Issue tracking and resolution

### Future Monitoring Needs
- Crash reporting (Firebase Crashlytics)
- Performance monitoring (Firebase Performance)
- User analytics (Firebase Analytics)
- API performance monitoring

## Conclusion

Arsitektur IuranKomplek saat ini memberikan fondasi yang solid untuk aplikasi manajemen iuran komplek. Seluruh kodebase telah bermigrasi ke Kotlin 100% dan mengikuti arsitektur MVVM modern dengan pola desain terbaik.

**Key Strengths:**
- Clean separation of concerns (MVVM pattern)
- Modular network layer dengan Repository pattern
- Comprehensive testing setup (400+ unit tests, 50+ instrumented tests)
- Modern Android development practices (StateFlow, Coroutines, ViewBinding)
- Production-ready caching strategy (cache-first dengan offline support)
- Webhook reliability system (idempotency, retry logic, persistent storage)
- Security hardening (certificate pinning, network security, sanitized logging)
- CI/CD pipeline (automated build, test, dan artifact generation)
- SOLID principles compliance (dependency inversion, single responsibility)

**Completed Modules:**
- ✅ MVVM Architecture dengan Repository pattern
- ✅ State management dengan StateFlow
- ✅ Room Database dengan proper relationships
- ✅ Cache Strategy (offline-first architecture)
- ✅ Webhook Reliability (queue-based processing)
- ✅ Security Hardening (certificate pinning, updated dependencies)
- ✅ Integration Hardening (circuit breaker, error handling)
- ✅ CI/CD Pipeline (GitHub Actions)
- ✅ Comprehensive Test Coverage (450+ total tests)

**Areas for Future Enhancement:**
- Dependency Injection dengan Hilt
- Data encryption at rest (Room database encryption)
- Jetpack Compose migration (optional)
- Advanced error recovery mechanisms
- Firebase integration (analytics, crash reporting)
- Performance optimization
- Enhanced error handling
- Better state management

Arsitektur ini dirancang untuk evolusi bertahap dengan mempertimbangkan kebutuhan bisnis, keterbatasan teknis, dan sumber daya pengembangan yang tersedia.