# Iuran BlokP

> 🏠 Modern Android application for managing residential/apartment complex dues payments.

[![Android CI](https://github.com/sulhimbn/blokp/workflows/Android%20CI/badge.svg)](https://github.com/sulhimbn/blokp/actions)
[![License](https://img.shields.io/badge/license-Proprietary-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)](https://kotlinlang.org)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [API Configuration](#api-configuration)
- [Testing](#testing)
- [Contributing](#contributing)
- [Documentation](#documentation)
- [License](#license)

## 🎯 Overview

Iuran BlokP is a comprehensive Android application built with **Kotlin 100%** that enables complex/apartment managers to efficiently manage resident dues payments. The application follows modern MVVM architecture with clean code principles, providing a robust and scalable solution for financial management.

### Key Highlights

- ✨ **100% Kotlin** - Modern, concise, and type-safe codebase
- 🏗️ **MVVM Architecture** - Clean separation of concerns
- 🔄 **Offline-First** - Cache-first strategy with automatic synchronization
- 🛡️ **Security Hardened** - Certificate pinning, encrypted storage
- 🧪 **Comprehensive Testing** - 450+ test cases
- 📦 **Production Ready** - CI/CD pipeline, monitoring, and observability

## ✨ Features

### 🏘️ User Management

- **User Directory**: Complete resident information including names, emails, addresses, and avatars
- **Individual Dues Tracking**: Monitor monthly dues for each resident
- **Profile Management**: Display user profiles with circular avatar images
- **Data Validation**: Ensure data integrity before storage

### 💰 Financial Reporting

- **Monthly Dues Calculation**: Automatic calculation of total monthly dues
- **Expense Tracking**: Record and track all expenses from dues funds
- **Balance Summary**: Calculate final balance after deducting expenses
- **Usage Reports**: Detailed breakdown of fund utilization

### 🔄 Data Synchronization

- **Real-time Sync**: Automatic synchronization with external API
- **Offline Support**: Full functionality available without internet connection
- **Cache Management**: Intelligent caching with freshness validation
- **Error Recovery**: Automatic retry with exponential backoff

### 🏗️ System Architecture

- **Menu Navigation**: Intuitive navigation with four main sections
- **Responsive UI**: Optimized for various screen sizes and orientations
- **State Management**: Reactive UI with StateFlow
- **Performance Optimized**: Efficient RecyclerView updates with DiffUtil

## 🛠️ Technology Stack

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Android SDK | API 34 | Platform |
| Kotlin | 100% | Programming Language |
| Gradle | 8.1.0 | Build System |
| Minimum SDK | API 24 (Android 7.0) | Compatibility |
| Target SDK | API 34 (Android 14) | Latest Features |

### Key Dependencies

```gradle
// Core Android
androidx.core:core-ktx:1.13.1
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.12.0
androidx.constraintlayout:constraintlayout:2.1.4

// UI Components
androidx.recyclerview:recyclerview:1.3.2
androidx.lifecycle:lifecycle-viewmodel:2.7.0
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0

// Networking
com.squareup.retrofit2:retrofit:2.11.0
com.squareup.retrofit2:converter-gson:2.11.0
com.squareup.okhttp3:logging-interceptor:4.12.0

// Image Loading
com.github.bumptech.glide:glide:4.11.0

// JSON Processing
com.google.code.gson:gson:2.8.9

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3

// Debugging (debug only)
com.github.chuckerteam.chucker:library:3.3.0

// Security
org.owasp:dependency-check-gradle:12.1.0
```

### Architecture Patterns

- **MVVM (Model-View-ViewModel)**: Clean architecture pattern
- **Repository Pattern**: Data abstraction layer
- **Factory Pattern**: Consistent dependency injection
- **Circuit Breaker**: Fault tolerance for API calls
- **StateFlow**: Reactive state management
- **DiffUtil**: Efficient list updates

## 🚀 Quick Start

### 3-Minute Developer Quick Start

**Get up and running immediately:**

```bash
# Clone and build
git clone https://github.com/sulhimbn/blokp.git && cd blokp
./gradlew build

# Run tests
./gradlew test

# Install on device/emulator
./gradlew installDebug
```

> **For detailed setup options**, see the Setup sections below. **For app usage guides**, see [User Guides](docs/USER_GUIDES.md).

---

### Prerequisites

Before you begin, ensure you have:

- [Android Studio Flamingo](https://developer.android.com/studio) or later
- [JDK 8](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html) or higher
- Android SDK API level 34
- Git installed
- (Optional) [Docker](https://www.docker.com/) for consistent development environment

### Option 1: Manual Setup with Android Studio

#### Step 1: Clone the Repository

```bash
git clone https://github.com/sulhimbn/blokp.git
cd blokp
```

#### Step 2: Open in Android Studio

```bash
# Navigate to project directory
cd blokp

# Open project in Android Studio
# File → Open → Select blokp directory
```

#### Step 3: Build the Project

```bash
# Using Gradle wrapper
./gradlew build

# Or in Android Studio: Build → Make Project
```

#### Step 4: Run the Application

```bash
# Install debug APK
./gradlew installDebug

# Or in Android Studio: Click Run button (▶)
```

### Option 2: Docker Setup (Recommended)

For a consistent development environment, use Docker:

#### Step 1: Start Development Environment

```bash
# Clone the repository
git clone https://github.com/sulhimbn/blokp.git
cd blokp

# Run setup script
./scripts/setup-dev-env.sh
```

#### Step 2: Access Development Tools

```bash
# Access VS Code at
http://localhost:8081

# Access Mock API at
http://localhost:8080
```

#### Step 3: Build and Test

```bash
# Build application
./scripts/build.sh

# Run tests
./scripts/test.sh
```

> **📖 Detailed Docker Setup**: See [`docs/docker-setup.md`](docs/docker-setup.md) for comprehensive Docker configuration instructions.

### Verification

After setup, verify your installation:

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator)
./gradlew connectedAndroidTest

# Build debug APK
./gradlew assembleDebug
```

The application automatically switches between environments:
- **Production**: Uses production API v1 endpoint
- **Development (Mock)**: Uses mock API server in Docker

Auto-switching is based on `BuildConfig.DEBUG` and `DOCKER_ENV` environment variable.

## 📁 Project Structure

```
BlokP/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/iurankomplek/     # Kotlin source code
│   │   │   │   ├── MainActivity.kt                 # User list screen
│   │   │   │   ├── LaporanActivity.kt              # Financial reports
│   │   │   │   ├── MenuActivity.kt                 # Main navigation
│   │   │   │   ├── presentation/                   # UI layer
│   │   │   │   │   ├── ui/                        # Activities & Fragments
│   │   │   │   │   ├── viewmodel/                 # ViewModels
│   │   │   │   │   └── adapter/                   # RecyclerView adapters
│   │   │   │   ├── data/                          # Data layer
│   │   │   │   │   ├── repository/                # Repository implementations
│   │   │   │   │   ├── dao/                       # Room DAOs
│   │   │   │   │   ├── database/                  # Room database
│   │   │   │   │   ├── entity/                    # Room entities
│   │   │   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   │   │   ├── api/                       # Network layer
│   │   │   │   │   ├── cache/                     # Cache management
│   │   │   │   │   └── constraints/                # Validation constraints
│   │   │   │   ├── domain/                        # Domain layer
│   │   │   │   │   ├── model/                     # Domain models
│   │   │   │   │   └── usecase/                   # Business logic
│   │   │   │   ├── payment/                       # Payment system
│   │   │   │   └── utils/                        # Utilities
│   │   │   ├── res/                                # Resources
│   │   │   │   ├── layout/                         # XML layouts
│   │   │   │   ├── values/                        # Strings, colors, etc.
│   │   │   │   ├── drawable/                      # Images, icons
│   │   │   │   └── xml/                           # Configurations
│   │   │   └── AndroidManifest.xml                 # App configuration
│   │   ├── test/                                   # Unit tests
│   │   └── androidTest/                            # Instrumented tests
│   └── build.gradle                                # App module build config
├── docs/                                            # Documentation
├── scripts/                                         # Utility scripts
├── build.gradle                                     # Root build config
├── settings.gradle                                  # Gradle settings
├── gradle.properties                               # Gradle properties
└── README.md                                        # This file
```

## 🔌 API Configuration

### API Versioning

The application supports two API versions:

- **Version 1 (Recommended)**: `/api/v1/*` - Standardized wrappers, error handling, request tracking
- **Legacy API**: `/data/{SPREADSHEET_ID}/` - Maintained for backward compatibility

See [API Documentation](docs/API.md) for complete details.

### Endpoints (v1 API - Recommended)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/users` | GET | Retrieve user/resident data |
| `/api/v1/pemanfaatan` | GET | Retrieve financial usage data |
| `/api/v1/vendors` | GET | Retrieve vendor information |
| `/api/v1/announcements` | GET | Retrieve community announcements |
| `/api/v1/messages` | GET/POST | Send/receive messages |
| `/api/v1/payments/*` | POST/GET | Payment processing |
| `/api/v1/health` | POST | Health check endpoint |

### Base URLs

```kotlin
// Production (v1 API)
https://api.apispreadsheets.com/api/v1/

// Development (Mock API)
http://api-mock:5000/api/v1/
```

### Resilience Patterns

The application implements fault tolerance with:

**Circuit Breaker** - Automatic failure protection
- Failure Threshold: 3 failures before opening circuit
- Success Threshold: 2 successes before closing circuit
- Timeout: 60 seconds before attempting recovery
- Handled automatically in BaseRepository (no manual setup needed)

**Retry Logic** - Exponential backoff with jitter
- Automatic retries on transient failures
- Configurable max retry attempts
- Jitter prevents thundering herd problem

**Rate Limiting** - Request rate protection
- Per-endpoint rate limits
- Automatic retry-after header handling
- Circuit breaker integration

> **See [API Integration Patterns](docs/API_INTEGRATION_PATTERNS.md)** for detailed implementation.

### Data Models (v1 API)

#### v1 API Response Format

All v1 API responses use standardized wrappers:

```kotlin
// Success response wrapper
ApiResponse<T>(
    data: T,           // Response data
    success: Boolean,  // Success flag
    message: String,   // Status message
    timestamp: Long    // Response timestamp
)

// List response wrapper
ApiListResponse<T>(
    data: List<T>,
    pagination: PaginationMetadata,
    success: Boolean,
    message: String,
    timestamp: Long
)
```

#### User Data Model

```kotlin
data class DataItem(
    val first_name: String,           // First name
    val last_name: String,            // Last name
    val email: String,                // Email address
    val alamat: String,               // Address
    val iuran_perwarga: Int,          // Monthly dues amount
    val total_iuran_rekap: Int,       // Annual total
    val jumlah_iuran_bulanan: Int,    // Monthly collection
    val total_iuran_individu: Int,    // Individual total
    val pengeluaran_iuran_warga: Int, // Expenses
    val pemanfaatan_iuran: String,    // Usage description
    val avatar: String                // Profile image URL
)
```

## 🧪 Testing

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.example.iurankomplek.ExampleUnitTest"

# Run instrumented tests (requires emulator)
./gradlew connectedAndroidTest

# Run tests with Docker
./scripts/test.sh

# Generate code coverage report
./gradlew test jacocoTestReport

# Build and test
./gradlew build
```

### Test Coverage

- **Total Tests**: 450+ test cases
- **Unit Tests**: 400+ tests
- **Instrumented Tests**: 50+ tests
- **Coverage Areas**: Repositories, ViewModels, Utilities, Network, Cache, Payment, Webhooks

> **📖 Detailed Testing Guide**: See [`AGENTS.md`](AGENTS.md) for build and test commands.

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Workflow

1. **Fork the repository**
   ```bash
   # Fork on GitHub and clone your fork
   git clone https://github.com/YOUR_USERNAME/blokp.git
   cd blokp
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Follow [Development Guidelines](docs/DEVELOPMENT.md)
   - Write tests for new functionality
   - Update documentation as needed

4. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

5. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Create a Pull Request**
   - Go to your fork on GitHub
   - Click "New Pull Request"
   - Fill in the PR template
   - Submit for review

### Code Style

- **Kotlin**: Follow [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html)
- **Testing**: Write tests for all new functionality
- **Documentation**: Update relevant docs for API changes
- **Commit Messages**: Use [conventional commits](https://www.conventionalcommits.org/)

### Pull Request Checklist

- [ ] Code follows project style guidelines
- [ ] All tests pass (unit + instrumented)
- [ ] Documentation updated
- [ ] No TODO comments left
- [ ] No hardcoded values
- [ ] Error handling implemented
- [ ] Self-review completed

## 📚 Documentation

### For Users

- [**User Guides**](docs/USER_GUIDES.md) - Step-by-step guides for common workflows
- [**Features Overview**](docs/feature.md) - Detailed feature descriptions
- [**Troubleshooting**](docs/TROUBLESHOOTING.md) - Common issues and solutions

### For Developers

#### API Documentation
- [**API Documentation Hub**](docs/API_DOCS_HUB.md) - Unified entry point for all API docs
- [**API Documentation**](docs/API.md) - Complete API reference with endpoints
- [**API Integration Patterns**](docs/API_INTEGRATION_PATTERNS.md) - Circuit breaker, retry logic
- [**API Versioning**](docs/API_VERSIONING.md) - API versioning strategy and migration guide
- [**API Endpoint Catalog**](docs/API_ENDPOINT_CATALOG.md) - Complete endpoint reference with schemas
- [**API Error Codes**](docs/API_ERROR_CODES.md) - Comprehensive error reference
- [**API Headers and Errors**](docs/API_HEADERS_AND_ERRORS.md) - HTTP headers, error responses

#### Architecture & Development
- [**Architecture Blueprint**](docs/blueprint.md) - Detailed architecture blueprint
- [**Architecture Documentation**](docs/ARCHITECTURE.md) - System architecture and component relationships
- [**Development Guidelines**](docs/DEVELOPMENT.md) - Coding standards and development workflow
- [**Caching Strategy**](docs/CACHING_STRATEGY.md) - Offline support and data synchronization
- [**Database Schema**](docs/DATABASE_SCHEMA.md) - Database structure

#### Testing & Performance
- [**Testing Summary**](docs/TESTING_SUMMARY.md) - Test coverage and strategies
- [**Performance Optimization**](docs/PERFORMANCE_OPTIMIZATION.md) - Performance improvements

#### Security
- [**Security Audit Report**](docs/SECURITY_AUDIT_REPORT.md) - Security architecture and compliance
- [**Security Assessment**](docs/SECURITY_ASSESSMENT_2026-01-10_REPORT.md) - Latest security assessment

#### Roadmap & Tasks
- [**Roadmap**](docs/ROADMAP.md) - Project roadmap and milestones
- [**Actionable Tasks**](docs/actionable-tasks.md) - Available development tasks

## 📄 License

This project is proprietary software. Use according to your internal development policies.

## 🏢 Application Screens

### Menu Activity
Main navigation hub with four menu cards:
- **Menu 1**: User Management (MainActivity)
- **Menu 2**: Financial Reports (LaporanActivity)
- **Menu 3**: Communication (CommunicationActivity)
- **Menu 4**: Payments (PaymentActivity)

### User List Screen
- Display all residents with complete information
- Show avatars with circular transformation
- Swipe-to-refresh functionality
- Efficient list rendering with DiffUtil

### Financial Reports Screen
- Real-time financial calculations
- Expense tracking and visualization
- Summary dashboard with key metrics
- Integration with payment transactions

## 🔒 Security

### Security Features

- ✅ **HTTPS Enforcement**: Production API uses secure connections
- ✅ **Certificate Pinning**: Prevents man-in-the-middle attacks (with backup pins)
- ✅ **Encrypted Storage**: SecureStorage with AES-256-GCM encryption
- ✅ **Root Detection**: Comprehensive rooted device detection (8 methods)
- ✅ **Emulator Detection**: Comprehensive emulator detection (7 methods)
- ✅ **Environment Validation**: `isSecureEnvironment()` verifies real device
- ✅ **Certificate Monitoring**: Automatic expiration monitoring with 90-day warnings
- ✅ **Input Validation**: Sanitized user inputs
- ✅ **Secure Logging**: No sensitive data in logs (reduced information leakage)
- ✅ **Up-to-date Dependencies**: Regular security updates
- ✅ **Network Security Config**: Proper SSL/TLS configuration
- ✅ **Dependency Scanning**: OWASP dependency-check with CVSS threshold 7.0

### Security Audits

Latest security audit completed: **2026-01-11**
- OWASP Mobile Security compliance (9/10 score)
- CWE Top 25 mitigations
- Encrypted storage with AES-256-GCM
- Root and emulator detection
- Certificate expiration monitoring
- Dependency vulnerability scanning (OWASP dependency-check)

## ♿ Accessibility

### Accessibility Features

- ✅ **Screen Reader Support**: Proper contentDescription on all interactive elements
- ✅ **Non-Redundant Announcements**: Single announcements for menu items (no double-speak)
- ✅ **Consistent Navigation**: Proper focus ordering and accessibility hints
- ✅ **Touch Target Size**: Minimum 48dp for all interactive elements
- ✅ **Color Contrast**: WCAG AA compliant text contrast
- ✅ **Accessibility Labeling**: Descriptive labels for all controls

### Accessibility Improvements (2026-01-11)

- **A11Y-001**: Eliminated redundant screen reader announcements in menu layouts
  - Parent LinearLayouts provide complete context
  - Child TextViews set to `importantForAccessibility="no"`
  - Single announcement per menu item
  - Consistent across portrait and tablet layouts

## 📊 Performance

### Optimization Highlights

- **DiffUtil**: Efficient RecyclerView updates
- **Image Caching**: Glide with smart caching strategies
- **Memory Management**: Proper lifecycle awareness
- **Network Optimization**: Connection pooling, request caching
- **Query Optimization**: Partial indexes, 50-80% faster queries
- **Algorithm Efficiency**: Single-pass calculations

## 🌐 CI/CD

### GitHub Actions Workflow

- **Automated Testing**: Lint, unit tests, instrumented tests
- **Matrix Testing**: Multiple API levels
- **Artifact Management**: APKs, test reports, lint reports
- **Path Filtering**: CI only on relevant changes
- **Gradle Caching**: Faster builds

### Build Status

[![Android CI](https://github.com/sulhimbn/blokp/workflows/Android%20CI/badge.svg)](https://github.com/sulhimbn/blokp/actions)

## 💡 Tips

### Development Tips

1. **Use Android Studio's Live Templates** - Speed up coding with templates
2. **Enable Layout Inspector** - Debug UI issues visually
3. **Use Database Inspector** - View Room database contents in real-time
4. **Profile with Memory Profiler** - Catch memory leaks early
5. **Network Profiler** - Monitor API call performance

### Common Commands

```bash
# Clean build
./gradlew clean build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Run specific test
./gradlew test --tests "com.example.iurankomplek.ExampleUnitTest"

# Compile Kotlin only
./gradlew :app:compileDebugKotlin

# View dependency tree
./gradlew app:dependencies

# Analyze build
./gradlew build --scan
```

## 🆘 Support

### Getting Help

- **Documentation**: Check [`docs/`](docs/) folder for detailed guides
- **Issues**: Search [GitHub Issues](https://github.com/sulhimbn/blokp/issues)
- **Community**: Contact development team
- **API Documentation**: See [`docs/API.md`](docs/API.md)

### Reporting Bugs

When reporting issues, please include:

1. Device/Emulator specifications
2. Android version and API level
3. App version and build number
4. Detailed steps to reproduce
5. Expected vs actual behavior
6. Relevant logcat output
7. Screenshots if applicable

## 🎯 Roadmap

### Current Focus

- ✅ MVVM Architecture
- ✅ Repository Pattern
- ✅ Offline Support
- ✅ Payment Integration
- ✅ Webhook Reliability
- ✅ Security Hardening

### Future Enhancements

- 🔄 Dependency Injection with Hilt
- 🔄 Jetpack Compose Migration
- 🔄 Firebase Integration
- 🔄 Advanced Analytics
- 🔄 Enhanced Error Recovery

---

<div align="center">

**Built with ❤️ using Kotlin and Modern Android Architecture**

[⬆ Back to Top](#-table-of-contents)

</div>
