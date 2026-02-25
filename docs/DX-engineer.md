# DX Engineer Documentation

This document serves as the long-term memory for the DX-engineer agent, tracking improvements made to enhance developer experience in this repository.

## Repository Context

- **Project Type**: Android Application (Kotlin/Java)
- **Build System**: Gradle 8.1.0 with Android Gradle Plugin
- **Architecture**: MVVM with Hilt dependency injection
- **Language**: Mixed Kotlin (new code) and Java (legacy)

## DX Improvements Made

### 2026-02-25: Comprehensive .gitignore

**Changes:**
- Replaced minimal .gitignore with comprehensive Android/Gradle entries
- Added build artifacts (`*.apk`, `*.aab`, `*.dex`, `*.class`)
- Added generated files (`bin/`, `gen/`, `out/`)
- Added IDE files (IntelliJ `.idea/`, `*.iml`)
- Added local configuration (`local.properties`)
- Added secrets protection (`secrets.properties`)
- Added build tools (lint, Kotlin, fastlane, app modules)

**Impact:**
- Prevents committing build artifacts to version control
- Keeps repository clean from IDE-specific files
- Protects secrets like API keys and local paths
- Improves CI performance by reducing unnecessary file tracking

**Files Changed:**
- `.gitignore` (72 additions, 2 deletions)

---

### 2025-02-25: Gradle Build Performance
## DX Improvements Made

### 2025-02-25: Gradle Build Performance

**Changes:**
- Enabled `org.gradle.parallel=true` in gradle.properties
- Added `org.gradle.daemon=true` for faster subsequent builds
- Added `org.gradle.caching=true` for build cache

**Impact:**
- Parallel builds allow Gradle to execute independent tasks concurrently
- Daemon keeps Gradle process warm between builds
- Build cache reuses outputs from previous builds

**Configuration:**
```properties
org.gradle.parallel=true
org.gradle.daemon=true
org.gradle.caching=true
```

### 2026-02-25: Duplicate Room Dependencies Fixed

**Changes:**
- Removed duplicate Room dependencies in `app/build.gradle`
- Previously: Room runtime, ktx, and compiler declared twice (6 entries)
- Now: Single declaration (3 entries)

**Impact:**
- Eliminates redundant dependency resolution
- Reduces Gradle sync and build time slightly
- Cleaner dependency graph

**Files Changed:**
- `app/build.gradle` (removed 3 duplicate lines)
### 2026-02-25: Duplicate Room Dependencies Fixed

**Issue Found:**
- PR #408 initially removed ALL Room dependencies instead of just duplicates
- This would have broken the build completely

**Fix Applied:**
- Corrected to remove only the duplicate Room dependency set (3 lines)
- Main had 6 entries (2 sets) → PR correctly removes to 3 entries (1 set)

**Changes:**
- Removed duplicate Room dependencies in `app/build.gradle`
- Previously: Room runtime, ktx, and compiler declared twice (6 entries)
- Now: Single declaration (3 entries)

**Impact:**
- Eliminates redundant dependency resolution
- Reduces Gradle sync and build time slightly
- Cleaner dependency graph

**Files Changed:**
- `app/build.gradle` (removed 3 duplicate lines)

---

## DX Principles

1. **Small, safe, measurable improvements** - Focus on incremental changes
2. **Never break the build** - All changes must preserve functionality
3. **Measure when possible** - Track impact of changes
4. **Document for future reference** - Keep this file updated

## Common DX Areas to Explore

- Build performance (Gradle configuration)
- CI/CD optimization
- Code quality tooling
- Developer onboarding (documentation)
- IDE integration
- Testing infrastructure
- Static analysis

## Future Improvements Ideas

- Add Gradle build scan for better diagnostics
- Explore kapt incremental compilation
- Add CI caching for dependencies
- Consider Kotlin symbol processing (KSP) instead of kapt
