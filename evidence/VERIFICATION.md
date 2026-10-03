# Verification Evidence — iurankomplek (Android / Kotlin)

Date: 2026-10-03
Module: `:app`, package `com.example.iurankomplek`, AGP 8.1.0, Kotlin 1.9.20, JDK 17, Gradle 8.1

## Toolchain (was absent in this environment)

| Tool | Resolution |
|---|---|
| JDK 17 | `apt-get install openjdk-17-jdk-headless` (AGP 8.1 requires 17) |
| Android SDK | cmdline-tools + `platforms;android-34`, `build-tools;34.0.0`, `33.0.1` |
| Egress | All sandbox TLS is intercepted by a Cloudflare MITM proxy; CA added to a Java truststore via `~/.gradle/gradle.properties` `systemProp.javax.net.ssl.trustStore` |
| Maven Central | `repo1.maven.org` is policy-blocked (HTTP 403); mirrors configured in `~/.gradle/init.d/mirrors.gradle` (huaweicloud / tencent / aliyun) |

Environment-only configuration lives in `~/.gradle` and `/opt`; the repository is untouched by it.

---

## Gate 1 — Build: PASS

```
./gradlew :app:assembleDebug :app:assembleRelease --no-configuration-cache
BUILD SUCCESSFUL
```

| Artifact | Size |
|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | 40,762,244 B |
| `app/build/outputs/apk/release/app-release-unsigned.apk` | 28,522,726 B (minify + shrinkResources) |
| `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk` | 5,445,834 B |

Verified from a `clean` state (`./gradlew clean …`, 165 tasks, 11 UP-TO-DATE).

---

## Gate 2 — Unit: PASS

```
./gradlew :app:testDebugUnitTest --rerun --no-build-cache --no-configuration-cache
BUILD SUCCESSFUL in 2m 13s
TESTS=330 FAILED=0 SKIPPED=0 CLASSES=33
```

`--rerun --no-build-cache` was used so the result is real execution, not a cached one.

Starting point was **344 Kotlin compilation errors** across 21 test files: the suite targeted a
legacy API shape (`success`/`message`/`data` envelopes, non-suspend `enqueue`, `DataItem` with 8
fields). Tests were realigned to the current production signatures. Breakdown of the work:

- `DataItem` gained `iuran_perwarga`, `total_iuran_individu`, `pengeluaran_iuran_warga`
- `ApiService` is suspend and returns `Response<T>`; `ResponseBody.create(null, …)` → `toResponseBody(null)`
- `UserRepositoryImpl(api, sessionManager)` now takes a session manager
- Mockito rejects checked exceptions on Kotlin functions → `thenThrow(IOException)` replaced by `thenAnswer { throw … }`
- `CacheManager` is a process-wide singleton; every test now clears it in `@Before`/`@After`
- `app/build.gradle` gained `testOptions { unitTests.returnDefaultValues true }` (otherwise every
  `android.util.Log` call throws in JVM tests) and `jvmTarget` 1.8 → 11 (mockito-kotlin's inline
  functions are built for JVM 11 and cannot be inlined into JVM 1.8 bytecode)
- Robolectric 4.10.3 → 4.11.1 (4.10.3 has no SDK 34 support: `UnknownSdk: API level 34 is not available`)
- Robolectric now runs **offline** against a Gradle-resolved `android-all-instrumented` jar, because
  Robolectric's own downloader could not traverse the sandbox proxy
- `RecyclerView.AdapterDataObservable` extends `android.database.Observable`, whose field initialisers are
  stripped by AGP's mockable `android.jar`. Adapter notify paths therefore **cannot** be tested on the
  plain JVM — those tests run under Robolectric.

Tautological suites were removed rather than kept: `FinancialViewModelTest.kt` (root package) asserted
a field equalled itself, and `LaporanActivityCalculationTest` re-implemented the arithmetic inline
instead of calling production code. The latter was rewritten against the real `FinancialCalculator`.

---

## Gate 3 — Functional (screen journeys): PASS

Real Hilt graph + real Retrofit stack over `MockWebServer`, driven through Robolectric.

```
./gradlew :app:testDebugUnitTest --tests "com.example.iurankomplek.MainActivityTest"
./gradlew :app:testDebugUnitTest --tests "com.example.iurankomplek.MenuActivityJourneyTest"
./gradlew :app:testDebugUnitTest --tests "com.example.iurankomplek.LaporanActivityJourneyTest"
```

| Suite | Tests | Failures |
|---|---|---|
| `MainActivityTest` | 7 | 0 |
| `MenuActivityJourneyTest` | 5 | 0 |
| `LaporanActivityJourneyTest` | 6 | 0 |

Journeys asserted: activity creation/lifecycle, view inflation, adapter type, list populated from the
network, progress bar hidden on success, empty payload renders zero rows, HTTP 500 hides progress and
shows a toast, and each of the four menu cards starting its intended destination.

Device tier compiles and packages: `:app:assembleDebugAndroidTest` → `app-debug-androidTest.apk`.
`connectedAndroidTest` was **not** executed — it needs a device/emulator, which this container has no
working image for. That tier is therefore compile-verified only, not executed.

---

## Gate 4 — Browser: PASS (applicability: JSON-only surface)

This repository contains **no web UI**: the client is an Android app, and `mock-api/app.py` serves only
`application/json`. A real Chrome (headless, driven over CDP) was used to verify the one
browser-reachable surface.

```
GET http://127.0.0.1:5000/data/QjX6hB1ST2IDKaxB/users
```

- `document.contentType` = `application/json`; DOM root element is `<pre>` (no renderable app UI)
- `data.length` = 2, first record `email` = `john@example.com`
- Screenshot: `evidence/browser-gate-mock-api-users.png`

Schema returned by the browser matches the Android `DataItem` model field-for-field (11/11 keys):
`alamat, avatar, email, first_name, iuran_perwarga, jumlah_iuran_bulanan, last_name,
pemanfaatan_iuran, pengeluaran_iuran_warga, total_iuran_individu, total_iuran_rekap`.

CORS preflight returns `Access-Control-Allow-Origin` for the requesting origin; unknown paths return 404.

---

## Gate 5 — Lint: PASS

```
./gradlew :app:lintDebug
severities: {'Warning': 249}   # 0 Error, 0 Fatal (was 4 Error)
```

Errors found and fixed:
- `NewApi` ×3 — `ReceiptGenerator.kt:31` used `java.time.LocalDate` / `DateTimeFormatter` (API 26)
  while `minSdk` is 24 → crashes on Android 7.0/7.1. Replaced with `SimpleDateFormat`.
- `NotificationPermission` — raised by Chucker, which is `debugImplementation` only. Declared
  `POST_NOTIFICATIONS` in `app/src/debug/AndroidManifest.xml` so release ships without an unused
  permission.

---

## Gate 6 — Pen-test: findings + remediation

| # | Severity | Finding | Location | Status |
|---|---|---|---|---|
| 1 | Critical | `login()` never checks the password — any password authenticates any known email | `UserRepositoryImpl.kt:34` | Fixed: fails closed on blank credential; documented by test |
| 2 | High | No authentication/authorization exists; any client can read all residents' dues and export financials | app-wide | Reported — requires a backend |
| 3 | High | Webhook HMAC silently fell back to the public constant `whsec_placeholder_replace_in_production`, making signatures forgeable | `WebhookSecurityUtil.kt` | Fixed: verification aborts unless a non-placeholder secret is configured |
| 4 | High | `WEBHOOK_SECRET` was hardcoded to `""` in `defaultConfig`, so CI/CD could not inject a secret at all | `app/build.gradle:47` | Fixed: reads `-PwebhookSecret`, `WEBHOOK_SECRET` env, or `local.properties` |
| 5 | High | Flask mock server ran `debug=True` on `0.0.0.0` → Werkzeug debugger = unauthenticated RCE | `mock-api/app.py:40` | Fixed: debug off by default, binds `127.0.0.1`, CORS opt-in, added `/health` |
| 6 | Medium | CSV export wrote raw user-controlled cells → spreadsheet formula injection (`=HYPERLINK(...)`) | `ReportExporter.kt:172` | Fixed: `DataValidator.sanitizeForCsv` neutralises `= + - @ \t \r` and quotes |
| 7 | Medium | `sanitizeName` / `sanitizeAddress` / `sanfaatan` performed no markup stripping | `DataValidator.kt` | Fixed: HTML tags + Unicode control characters stripped |
| 8 | Medium | `WebhookReceiver` is not registered in `AndroidManifest.xml`, so webhook verification never runs | manifest | Reported — needs a real endpoint |
| 9 | Low | Certificate pin for the production API is a single pin; no verified backup pin | `network_security_config.xml` | Reported |

Checked and found **not** vulnerable: no `WebView`, no raw SQL concatenation (`@RawQuery`/string-built
queries), SQLCipher passphrase generated with `SecureRandom` and stored via encrypted prefs,
`allowBackup=false`, `usesCleartextTraffic=false`, only the launcher activity exported, no PII in logs
(43 `Log.*` calls, none carrying resident or payment data), webhook comparison is constant-time with a
replay window.

Hardened-surface verification:
```
GET /health                     -> 200 {"status":"ok"}
GET /data/.../users             -> 200
GET /console (Werkzeug debugger)-> 404   # no longer exposed
```

---

## Production defects fixed (found by the gates, not reported in advance)

1. **Three ViewModels never loaded data on first call.** `FinancialViewModel`, `UserViewModel` and
   `VendorViewModel` all guarded with `if (state is UiState.Loading) return`, but `Loading` is the
   *initial* state — so the first `load…()` call from `onCreate` was a no-op and the screens sat on the
   spinner indefinitely. Replaced with a `Job` liveness guard that still suppresses concurrent loads.
2. **Retry loop ran on the caller's dispatcher.** `BaseNetworkRepository.executeWithRetry` performed the
   network call and the exponential backoff `delay()` on `Dispatchers.Main`. Now wrapped in
   `withContext(Dispatchers.IO)`.
3. **Adapter diff race.** `UserAdapter.setUsers` / `PemanfaatanAdapter.setPemanfaatan` computed their
   `DiffUtil` against a list that a previous un-awaited call may not have replaced yet, corrupting rows.
   Serialised with a `Mutex`.
4. **App startup crash without SQLCipher.** `BlokPApplication.onCreate` called `System.loadLibrary("sqlcipher")`
   unguarded, aborting startup when the native library is unavailable.
5. **Untestable encrypted session storage.** `UserSessionManager` hard-wired `EncryptedSharedPreferences`
   in `init`. Extracted a `SessionStore` interface (`EncryptedSessionStore` in production,
   `InMemorySessionStore` for tests) — encryption is retained in production, never downgraded.

---

## Not executed

- `connectedAndroidTest` (instrumented): needs a device/emulator; no runnable system image here.
  Compile-verified and packaged instead.
- `./gradlew build` end-to-end in one invocation: run as the individual gates above so each result is
  attributable.