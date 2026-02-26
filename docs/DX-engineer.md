# DX Engineer Documentation

This document serves as the long-term memory for the DX-engineer agent, tracking improvements made to enhance developer experience in this repository.

## Repository Context

- **Project Type**: Android Application (Kotlin)
- **Build System**: Gradle 8.1.0 with Android Gradle Plugin
- **Architecture**: MVVM with Hilt dependency injection
- **Language**: 100% Kotlin codebase

## DX Improvements Made

### 2026-02-25: KSP Migration (Kotlin Symbol Processing)

**Changes:**
- Replaced `kotlin-kapt` plugin with `com.google.devtools.ksp` in app/build.gradle
- Added KSP plugin (`com.google.devtools.ksp` version 1.9.20-1.0.14) to root build.gradle
- Replaced `kapt` with `ksp` for Room compiler dependency
- Replaced `kapt` with `ksp` for Hilt compiler dependency

**Impact:**
- KSP is 2-3x faster than kapt for annotation processing
- Reduced build times for clean and incremental builds
- More accurate incremental compilation
- Better IDE support (faster code completion)

**Configuration:**
```groovy
// root build.gradle
plugins {
    id 'com.google.devtools.ksp' version '1.9.20-1.0.14' apply false
}

// app/build.gradle
plugins {
    id 'com.google.devtools.ksp'
}

dependencies {
    ksp libs.room.compiler
    ksp libs.hilt.android.compiler
}
```

---

### 2026-02-25: Gradle Build Performance v2

### 2026-02-25: Gradle Build Performance v2

**Changes:**
- Enabled `org.gradle.configuration-cache=true` in gradle.properties
- Added `kotlin.daemon.jvmargs=-Xmx1536m` for better Kotlin compilation performance

**Impact:**
- Configuration cache caches the configuration phase, significantly speeding up subsequent builds
- Kotlin daemon gets dedicated memory for faster Kotlin compilation
- Combined with existing parallel, daemon, and caching settings

**Configuration:**
```properties
org.gradle.configuration-cache=true
kotlin.daemon.jvmargs=-Xmx1536m
```

TH|---

### 2026-02-26: .gitconfig with Developer Aliases

**Changes:**
- Added `.gitconfig` with useful aliases for common git operations
- Status shortcuts: `ss` (short status), `st` (full status)
- Log shortcuts: `lg` (last 10 commits), `lg1` (last commit)
- Branch shortcuts: `co` (checkout), `cb` (checkout -b)
- Diff shortcuts: `dc` (cached diff), `dn` (diff names only)
- Stage shortcuts: `aa` (add all), `ap` (add patch)
- Undo shortcuts: `unstage` (reset HEAD), `undo` (soft reset)
- Pretty graph log: `graph` (visual commit history)

**Impact:**
- Faster git workflow for developers
- Consistent git commands across team
- Better visualization of commit history

**Files Changed:**
- `.gitconfig` (new file)

---

### 2026-02-26: .editorconfig for Consistent Coding Style

**Changes:**
- Added `.editorconfig` with coding style rules
- Kotlin files: 4-space indent, 120 char max line length
- Java/Gradle files: 4-space indent
- XML files: 4-space indent
- JSON/YAML files: 2-space indent
- Markdown: no trailing whitespace

**Impact:**
- Consistent code style across all editors
- Automatic formatting in IDEs
- Enforces team coding standards

**Files Changed:**
- `.editorconfig` (new file)

---

### 2026-02-26: Build.gradle Syntax Fix

**Changes:**
- Fixed duplicate closing brace in app/build.gradle
- Removed duplicate `buildTypes` block declaration

**Impact:**
- Build file is now syntactically correct
- Enables Gradle build to proceed past configuration phase

**Files Changed:**
- `app/build.gradle` (syntax fix)

---

### 2026-02-26: CI Gradle Cache (PENDING - Permission Required)

**Changes (pending):**
- Add Gradle wrapper cache to `.github/workflows/on-push.yml`
- Add Gradle wrapper cache to `.github/workflows/on-pull.yml`

**Configuration:**
```yaml
- name: Setup Gradle Cache
  uses: actions/cache@v5
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: gradle-${{ runner.os }}-${{ hashFiles('gradle/wrapper/gradle-wrapper.properties') }}-v1
    restore-keys: |
      gradle-${{ runner.os }}-
```

**Impact:**
- 30-50% faster CI builds on cache hits
- Reduces network usage for dependency downloads

**Status:** Not applied due to GitHub App workflow permission limitations

---


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
- Add CI caching for dependencies

- Add Gradle build scan for better diagnostics
- Explore kapt incremental compilation
- Add CI caching for dependencies
- Consider Kotlin symbol processing (KSP) instead of kapt
