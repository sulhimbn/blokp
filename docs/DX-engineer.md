# DX Engineer Documentation

This document serves as the long-term memory for the DX-engineer agent, tracking improvements made to enhance developer experience in this repository.

## Repository Context

- **Project Type**: Android Application (Kotlin)
- **Build System**: Gradle 8.1.0 with Android Gradle Plugin
- **Architecture**: MVVM with Hilt dependency injection
- **Language**: 100% Kotlin codebase

## DX Improvements Made

### 2026-02-26: Android Lint Configuration

**Changes:**
- Created comprehensive `app/src/main/res/xml/lint.xml` with project-specific rules
- Added lint configuration block to `app/build.gradle`
- Configured lint to generate HTML and XML reports
- Enabled 'StopShip' check for critical issues
- Disabled noisy rules: InvalidPackage, PrivateResource, GradleOverrides

**Impact:**
- Early detection of code quality issues during build
- Standardized lint rules across team members
- HTML/XML reports for detailed analysis
- Prevents shipping with critical issues via StopShip

**Configuration:**
```xml
<!-- app/src/main/res/xml/lint.xml -->
<lint>
    <!-- Custom rules for project-specific checks -->
    <issue id="AdapterViewChildren" severity="warning" />
    <issue id="AllowBackup" severity="warning" />
    <!-- ... comprehensive rules -->
</lint>


```groovy
// app/build.gradle
lint {
    abortOnError false
    checkReleaseBuilds true
    htmlReport true
    xmlReport true
    warningsAsErrors false
    enable 'StopShip'
    disable 'InvalidPackage', 'PrivateResource', 'GradleOverrides'
    lintConfig file('src/main/res/xml/lint.xml')
}
```

---

### 2026-02-25: KSP Migration (Kotlin Symbol Processing)

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
