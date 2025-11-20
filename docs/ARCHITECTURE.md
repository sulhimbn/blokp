# IuranKomplek Architecture Documentation

## Overview

IuranKomplek adalah aplikasi Android untuk mengelola pembayaran iuran komplek perumahan/apartemen. Aplikasi ini dibangun dengan arsitektur hybrid Kotlin-Java dan mengikuti pola MVVM Light.

## Technology Stack

### Core Technologies
- **Platform**: Android SDK API level 34
- **Languages**: Kotlin (primary), Java (legacy compatibility)
- **Build System**: Gradle 7.3.0
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)

### Key Dependencies
```gradle
// Core Android
androidx.core:core-ktx:1.7.0
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
MenuActivity (Java)
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

GET /data/QjX6hB1ST2IDKaxB/
├── Returns: ResponseUser
├── Data: List<DataItem>
└── Format: JSON

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
// UserAdapter - Standard RecyclerView pattern
class UserAdapter(private val users: MutableList<DataItem>) :
    RecyclerView.Adapter<UserAdapter.ListViewHolder>() {
    
    override fun onCreateViewHolder(...)
    override fun onBindViewHolder(...)
    fun setUsers(users: List<DataItem>) {
        this.users.clear()
        this.users.addAll(users)
        notifyDataSetChanged() // TODO: Replace with DiffUtil
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

## Security Architecture

### Current Security Measures
- ✅ HTTPS for production API
- ✅ Debug-only network inspection (Chucker)
- ✅ Basic error handling

### Security Gaps
- ❌ No certificate pinning
- ❌ No network security configuration
- ❌ HTTP allowed in debug mode
- ❌ No data encryption at rest

## Testing Architecture

### Current Test Coverage
```
test/
├── ExampleUnitTest.kt (basic)
└── LaporanActivityCalculationTest.kt (financial logic)

androidTest/
└── ExampleInstrumentedTest.kt (basic)
```

### Test Strategy
- **Unit Tests**: Business logic validation
- **Integration Tests**: API communication
- **UI Tests**: User interaction flows
- **Mock Tests**: Development environment validation

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
- No offline data persistence
- Limited error recovery mechanisms
- Monolithic activity structure

### Future Scalability Plans
1. **Database Integration**: Room persistence
2. **Multiple API Sources**: Flexible data providers
3. **Microservices**: Modular backend architecture
4. **Cloud Integration**: Firebase/AWS backend

## Deployment Architecture

### Build Pipeline
```
GitHub Actions
├── Code Quality Analyzer (daily)
├── Release Manager (manual)
├── Researcher (automated)
├── Issue Solver (automated)
├── Maintainer (automated)
├── PR Handler (automated)
└── Problem Finder (automated)
```

### Release Strategy
- **Semantic Versioning**: major.minor.patch
- **Automated Changelog**: Git commit analysis
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

Arsitektur IuranKomplek saat ini memberikan fondasi yang solid untuk aplikasi manajemen iuran komplek. Arsitektur hybrid Kotlin-Java memungkinkan transisi bertahap ke Kotlin sepenuhnya sambil mempertahankan kompatibilitas.

**Key Strengths:**
- Clean separation of concerns
- Modular network layer
- Comprehensive testing setup
- Modern Android development practices

**Areas for Improvement:**
- Security hardening
- Performance optimization
- Enhanced error handling
- Better state management

Arsitektur ini dirancang untuk evolusi bertahap dengan mempertimbangkan kebutuhan bisnis, keterbatasan teknis, dan sumber daya pengembangan yang tersedia.