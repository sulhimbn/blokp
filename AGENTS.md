# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Test Commands
- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Install debug: `./gradlew installDebug`
- Single test execution: `./gradlew test --tests "com.example.iurankomplek.ExampleUnitTest"`

## Project-Specific Patterns
- Mixed Kotlin/Java codebase: MainActivity.kt and LaporanActivity.kt use Kotlin, MenuActivity.java uses Java
- API endpoints both use `@GET(".")` path in ApiService.kt - same base URL for different data
- Data model has specific calculation logic: `total_iuran_individu * 3` in LaporanActivity.kt line 56
- Network debugging uses Chucker (debugImplementation only)
- Glide image loading with CircleCrop transform for avatars
- Both UserAdapter and PemanfaatanAdapter use notifyDataSetChanged() instead of DiffUtil
- API base URL contains newline characters that must be preserved: "https://api.apispreadsheets.com/data/QjX6hB1ST2IDKaxB/\n\n"

## Code Style
- Kotlin uses "official" code style (kotlin.code.style=official)
- Mixed language project: prefer Kotlin for new features but maintain Java compatibility
- RecyclerView adapters follow standard pattern but use notifyDataSetChanged()
- Retrofit API calls use enqueue with Callback objects
- Error handling shows Toast messages and prints stack traces