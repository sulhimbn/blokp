# Architecture Documentation

## Overview
IuranKomplek is a residential management application built with Android using a hybrid Kotlin-Java codebase. The application follows a simplified MVVM (Model-View-ViewModel) pattern with a focus on displaying user data and financial reports from a remote API.

## Technology Stack
- **Language**: Kotlin (primary), Java (for compatibility)
- **Framework**: Android SDK
- **Networking**: Retrofit for API calls
- **JSON Parsing**: Gson
- **UI Components**: RecyclerView, AppCompatActivity
- **Build System**: Gradle

## Architecture Pattern
The application follows a simplified MVVM pattern without a dedicated ViewModel layer:

- **Model**: Data classes (DataItem, UserResponse, PemanfaatanResponse) and network layer (ApiService, ApiConfig)
- **View**: Activities (MainActivity, LaporanActivity) and Adapters (UserAdapter, PemanfaatanAdapter)
- **ViewModel**: Logic is handled directly in Activities

## Components

### Activities
- **MainActivity.kt**: Displays user list with UserAdapter
- **LaporanActivity.kt**: Displays financial reports with PemanfaatanAdapter, includes calculation logic
- **MenuActivity.java**: Main menu (Java for compatibility)

### Adapters
- **UserAdapter.kt**: Handles user list display in MainActivity
- **PemanfaatanAdapter.kt**: Handles financial data display in LaporanActivity

### Network Layer
- **ApiConfig.kt**: Configuration for Retrofit client with conditional base URL (mock vs production)
- **ApiService.kt**: API interface with endpoint definitions

### Models
- **DataItem.kt**: Data structure for user/iuran information
- **UserResponse.kt**: Response wrapper for user data
- **PemanfaatanResponse.kt**: Response wrapper for financial data

## Data Flow
1. User interaction → Activity
2. Activity → Network call via ApiConfig.getApiService()
3. API Service → HTTP request to remote API
4. API Response → Model parsing with Gson
5. Model → Adapter → RecyclerView UI Update
6. For LaporanActivity: Additional calculation logic for financial summaries

## Design Decisions

### Hybrid Kotlin-Java Codebase
- Kotlin is preferred for new features
- Java is maintained for existing components (MenuActivity.java) for compatibility

### API Design
- Uses same base URL for different endpoints (users vs pemanfaatan)
- Conditional API URL based on environment (debug vs release)

### Data Handling
- Direct API-to-UI mapping without local persistence
- Calculation logic embedded in LaporanActivity for financial summaries

### Error Handling
- Network error handling with retry logic
- Toast messages for user feedback
- Graceful degradation when offline

## Component Relationships
```
MainActivity ──► UserAdapter ──► RecyclerView
    │
    └─► ApiConfig ──► ApiService ──► Remote API

LaporanActivity ──► PemanfaatanAdapter ──► RecyclerView
    │
    └─► ApiConfig ──► ApiService ──► Remote API
```

## Limitations
- No local data caching
- No offline mode (completely dependent on network)
- No user authentication/authorization
- No background synchronization