# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Build/Test Commands
- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Install debug: `./gradlew installDebug`
- Single test execution: `./gradlew test --tests "com.example.iurankomplek.ExampleUnitTest"`

## Project-Specific Patterns
- Mixed Kotlin/Java codebase: MainActivity.kt, LaporanActivity.kt, adapters, dan network layer menggunakan Kotlin; MenuActivity.java menggunakan Java untuk kompatibilitas
- API endpoints menggunakan `@GET(".")` path yang sama di ApiService.kt karena data berbeda diambil dari base URL yang identik
- Data model memiliki logika perhitungan khusus: `total_iuran_individu * 3` di LaporanActivity.kt line 56 untuk menghitung rekap iuran
- Network debugging menggunakan Chucker (hanya di debugImplementation) untuk inspeksi traffic API
- Glide image loading dengan CircleCrop transform untuk menampilkan avatar pengguna berbentuk bulat
- UserAdapter dan PemanfaatanAdapter menggunakan notifyDataSetChanged() sebagai pengganti DiffUtil untuk kemudahan implementasi

## Code Style
- Kotlin menggunakan "official" code style (kotlin.code.style=official)
- Proyek mixed language: prefer Kotlin untuk fitur baru tapi maintain kompatibilitas Java
- RecyclerView adapters mengikuti pola standar tapi menggunakan notifyDataSetChanged()
- Retrofit API calls menggunakan enqueue dengan Callback objects
- Error handling menampilkan Toast messages dan print stack traces