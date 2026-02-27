---
description: OpenCode Configuration for Iuran BlokP
mode: subagent
model: opencode/big-pickle
tools:
  bash: true
  write: true
  edit: true
  read: true
  grep: true
  glob: true
  patch: true
  todowrite: true
  todoread: true
  webfetch: true
  websearch: true
  codesearch: true
  skill: true
---

# OpenCode Configuration for Iuran BlokP

## Quick Start

1. **Install OpenCode**: `curl -fsSL https://opencode.ai/install | bash`
2. **Authenticate**: Run `opencode auth login` and select your provider
3. **Start coding**: Run `opencode` in this directory

## Models (FREE)

This project uses **OpenCode Zen free models** - completely free to use:

| Model | Type | Cost |
|-------|------|------|
| `opencode/big-pickle` | Main Model | FREE |
| `opencode/minimax-m2.5-free` | Small Model | FREE |

> **Note**: Big Pickle and MiniMax M2.5 Free are available at no cost through OpenCode Zen.

## OpenCode Zen - Free Models

OpenCode Zen provides curated models that are tested and verified for coding agents:

- **Big Pickle**: Stealth model, free on OpenCode for a limited time
- **MiniMax M2.5 Free**: Available on OpenCode for a limited time

For more information about OpenCode Zen and other available models, see:
- [OpenCode Zen Documentation](https://opencode.ai/docs/zen/)

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

### oh-my-opencode Agents

This project uses oh-my-opencode plugin for enhanced agent capabilities:

- **Sisyphus**: Main orchestration agent (uses `opencode/big-pickle`)
- **Oracle**: Debugging and architecture consultation
- **Explore**: Codebase exploration and pattern discovery
- **Librarian**: External documentation and reference lookup
- **android-developer**: Project-specific Android development specialist

For more information about agents, see:
- [Agents Documentation](https://opencode.ai/docs/agents/)

## Available Skills

This project defines custom skills for specialized tasks:

- **android-build**: Build and test Android APK using Gradle

For more information about skills, see:
- [Skills Documentation](https://opencode.ai/docs/skills/)

## LSP Support

This project has LSP (Language Server Protocol) support for:
- Kotlin development (auto-installed for Kotlin projects)
- Android SDK

For more information about LSP configuration, see:
- [LSP Documentation](https://opencode.ai/docs/lsp/)

## Custom Tools

OpenCode supports custom tools for enhanced functionality. This project uses:
- Android build tools via Gradle
- Git operations
- Web search and fetch

For more information about custom tools, see:
- [Custom Tools Documentation](https://opencode.ai/docs/custom-tools/)

## Plugins

This project uses the following OpenCode plugins:

- **oh-my-opencode**: Enhanced agent orchestration and autonomous workflows

For more information about plugins, see:
- [Plugins Documentation](https://opencode.ai/docs/plugins/)

## SDK & Development

For information about OpenCode SDK and development tools:
- [OpenCode SDK Documentation](https://opencode.ai/docs/sdk/)

## Installation Reference

For detailed installation instructions and oh-my-opencode setup:
- [oh-my-opencode Installation Guide](https://raw.githubusercontent.com/code-yeongyu/oh-my-opencode/refs/heads/master/docs/guide/installation.md)

## General OpenCode Documentation

- [OpenCode Official Docs](https://opencode.ai/docs)
- [OpenCode Zen - Free Models](https://opencode.ai/docs/zen/)
- [Agents](https://opencode.ai/docs/agents/)
- [Skills](https://opencode.ai/docs/skills/)
- [LSP Servers](https://opencode.ai/docs/lsp/)
- [Custom Tools](https://opencode.ai/docs/custom-tools/)
- [Plugins](https://opencode.ai/docs/plugins/)
- [SDK](https://opencode.ai/docs/sdk/)

## Documentation

- [API Documentation](docs/api-documentation.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Development Guidelines](docs/development-guidelines.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)

## Support

For issues or questions, refer to the troubleshooting guide or open an issue on GitHub.
