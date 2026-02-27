# OpenCode Configuration for Iuran BlokP

This file provides project-specific guidance for AI agents working on this repository.

## Quick Start

1. **Install OpenCode**: `curl -fsSL https://opencode.ai/install | bash`
2. **Authenticate**: Run `opencode auth login` and select your provider
3. **Start coding**: Run `opencode` in this directory

## Models (FREE)

This project uses **OpenCode Zen free models**:
- **Main Model**: `opencode/big-pickle` (FREE - all usage)
- **Small Model**: `opencode/minimax-m2.5-free` (FREE - all usage)

> **Note**: These models are completely free to use. No payment required.

## Project Overview

**Iuran BlokP** is an Android application for managing residential association (iuran) payments in Indonesian housing complexes.

### Technology Stack

| Component | Technology |
|-----------|------------|
| Platform | Android SDK 34 |
| Language | Kotlin (100%) |
| Min SDK | API 24 (Android 7.0) |
| Build | Gradle |
| Network | Retrofit 2 + OkHttp3 |
| Images | Glide |

### Key Features

1. **User Management**: Display resident list with avatars
2. **Financial Reports**: Monthly iuran tracking and utilization reports
3. **Navigation**: Simple menu-based navigation
4. **API Integration**: Real-time sync with external API

## Common Development Tasks

### Building the Project

```bash
# Full build
./gradlew build

# Run tests
./gradlew test

# Install debug APK
./gradlew installDebug

# Compile Kotlin only
./gradlew :app:compileDebugKotlin
```

### Adding New Features

1. Follow **MVVM Light** pattern
2. Use **Kotlin** for all new code
3. Use **DiffUtil** in RecyclerView adapters
4. Add **unit tests** for new functionality

### Network Changes

- API definitions: `app/src/main/java/com/example/iurankomplek/network/ApiService.kt`
- Retrofit config: `app/src/main/java/com/example/iurankomplek/network/ApiConfig.kt`

### Data Models

- Core model: `app/src/main/java/com/example/iurankomplek/model/DataItem.kt`
- User response: `app/src/main/java/com/example/iurankomplek/model/UserResponse.kt`
- Utilization response: `app/src/main/java/com/example/iurankomplek/model/PemanfaatanResponse.kt`

## Important Patterns

### Financial Calculation

The financial report uses a special formula in `LaporanActivity.kt`:
```kotlin
val rekap = total_iuran_individu * 3
```

### Image Loading

Use Glide with CircleCrop for circular avatars:
```kotlin
Glide.with(context)
    .load(avatarUrl)
    .transform(CircleCrop())
    .into(imageView)
```

### Error Handling

- Show Toast messages for user-facing errors
- Print stack traces for debugging
- Use Chucker (debug only) for network inspection

## Available Agents

Custom agents are defined in `.opencode/agents/`:

- **android-developer**: Android/Kotlin development specialist

## Documentation

- [API Documentation](docs/api-documentation.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Development Guidelines](docs/development-guidelines.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)

## Support

For issues or questions, refer to the troubleshooting guide or open an issue on GitHub.
