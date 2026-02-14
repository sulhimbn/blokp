# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Test Commands
- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Install debug: `./gradlew installDebug`
- Single test execution: `./gradlew test --tests "com.example.iurankomplek.ExampleUnitTest"`
- Compile Kotlin: `./gradlew :app:compileDebugKotlin`

## Project-Specific Patterns
- Mixed Kotlin/Java codebase: MainActivity.kt, LaporanActivity.kt, MenuActivity.kt, adapters, dan network layer menggunakan Kotlin
- **Architecture Pattern**: MVVM with ViewModels in `presentation/viewmodel/` package (e.g., PaymentViewModel.kt)
- API endpoints now use distinct paths: `@GET("users")` and `@GET("pemanfaatan")` in ApiService.kt for better clarity and maintainability
- API responses use specific models: UserResponse for user endpoint and PemanfaatanResponse for financial data endpoint to improve type safety
- Data model memiliki logika perhitungan khusus: `total_iuran_individu * 3` di LaporanActivity.kt line 56 untuk menghitung rekap iuran
- Network debugging menggunakan Chucker (hanya di debugImplementation) untuk inspeksi traffic API
- Glide image loading dengan CircleCrop transform untuk menampilkan avatar pengguna berbentuk bulat
- RecyclerView adapters now use DiffUtil for efficient updates instead of notifyDataSetChanged() for better performance

## Documentation
- Complete API documentation available in docs/API.md
- Architecture documentation in docs/ARCHITECTURE.md
- Development guidelines in docs/DEVELOPMENT.md
- Troubleshooting guide in docs/TROUBLESHOOTING.md

## Code Style
- Kotlin menggunakan "official" code style (kotlin.code.style=official)
- Proyek mixed language: prefer Kotlin untuk fitur baru tapi maintain kompatibilitas Java
- RecyclerView adapters mengikuti pola standar dengan DiffUtil untuk efisiensi update
- Retrofit API calls menggunakan enqueue dengan Callback objects
- Error handling menampilkan Toast messages dan print stack traces