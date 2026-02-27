---
name: android-build
description: Build and test Android APK using Gradle
license: MIT
compatibility: opencode
metadata:
  audience: developers
  workflow: android
---

## What I do

- Build Android APK using `./gradlew build`
- Run unit tests with `./gradlew test`
- Compile Kotlin code with `./gradlew :app:compileDebugKotlin`
- Install debug APK with `./gradlew installDebug`

## When to use me

Use this when you need to:
- Verify code compiles correctly
- Run tests to verify functionality
- Build a debug APK for testing
- Check for compilation errors

## Examples

```
Build the project to verify it compiles
Run the unit tests
Compile just the Kotlin code
```

## Notes

- Requires Android SDK and Java to be installed
- Uses Gradle wrapper (./gradlew)
- Build outputs go to app/build/outputs/apk/
