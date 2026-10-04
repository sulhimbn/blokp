# Security review — BlokP (com.example.iurankomplek)

Scope: static audit of `app/src/main` plus runtime verification against a real
release build. Findings are ordered by severity. Each states the evidence and
whether it was **fixed in this change** or **left open**.

## Method and honest caveats

- Everything below was read from source, not inferred.
- Two candidate findings were **discarded after verification** rather than
  reported (see "Disproven").
- The certificate pin **could not be validated**: this environment intercepts
  all outbound TLS with a `Cloudflare TLS proxy-everything Intercept CA`, so the
  certificate observed for `api.apispreadsheets.com` is the sandbox's, not the
  real one. No mismatch is claimed.

---

## CRITICAL

### 1. Release builds silently lose all API data (R8 keep rule points at a package that does not exist)

`app/proguard-rules.pro:53` kept `com.example.iurankomplek.data.model.**`. No
such package exists — the models live in `com.example.iurankomplek.model` and
`com.example.iurankomplek.data.api.models`. None of them carry `@SerializedName`
(0 occurrences in the whole app), so once R8 renamed the fields, Gson had no
key to bind.

Measured on a real `assembleRelease` APK:

| | model classes in DEX | `first_name` | `total_iuran_individu` |
|---|---|---|---|
| before fix | 0 | absent | absent |
| after fix | 641 | present | present |

Impact: in a minified release build the app's only data source deserialises to
nulls and zeros. Debug builds are unaffected, which is why it was never seen.
**Fixed** — keep rules corrected to the real packages.

### 2. No authentication: `login()` ignores the password and is never called

`data/repository/UserRepositoryImpl.kt:34`

```kotlin
override suspend fun login(email: String, password: String): Result<User> =
    // password is never read; a match on email alone succeeds
```

- `login()` has **zero callers** in `app/src/main`.
- There is no `LoginActivity` and no `login` layout.
- `UserSessionManager.isLoggedIn` is referenced only inside its own file — no
  Activity or Fragment checks it.
- `DashboardActivity` is the launcher and opens with no gate.

So the app has no authentication at all, and the one auth primitive that exists
is a no-op that would authenticate anyone who knew an email address.

**Not fixed — out of scope for a test-restoration change.** This needs a product
decision: either wire real authentication in front of the launcher, or delete
`login()` so it stops reading as an auth control.

### 3. No role check before privileged actions

Payment processing, vendor management and work-order assignment have no
admin/resident distinction. Any user reaching the app can drive
`TransactionRepository.processPayment` and `VendorRepository` mutations.

**Not fixed** — same reason as #2.

---

## HIGH

### 4. Hardcoded webhook HMAC secret ships in every build, and verification fails open

- `utils/Constants.kt:37` — `WEBHOOK_SECRET_KEY = "whsec_placeholder_replace_in_production"`
- `app/build.gradle` defines `BuildConfig.WEBHOOK_SECRET` as `""` in
  `defaultConfig` with **no `release` blockType override**, so release builds
  fall through to the placeholder too (the comment at `build.gradle:25`
  implies otherwise).
- `utils/WebhookSecurityUtil.kt:71` — `verifySignature` logs
  "Using default webhook secret" and **continues** instead of failing closed.
- `utils/WebhookSecurityUtil.kt:193` — `generateTestSignature` signs arbitrary
  payloads with the live secret and is public API inside the `object`.

**Exploitability is currently low**: `WebhookReceiver` is a plain class, not a
`BroadcastReceiver`, is absent from `AndroidManifest.xml`, and has no callers
outside its own file. It is unreachable dead code today.

**Risk is that it is a loaded gun**: the moment someone registers a receiver or
calls it, the secret is a published constant, verification fails open, and a
test-signature helper is available in the release APK.

**Not fixed** — the fix is to delete the dead webhook code or make it
fail-closed with a real secret injected at build time. Both are design calls.

### 5. Stored XSS / injection: `DataValidator` never stripped markup

`utils/DataValidator.kt` — `sanitizeName`, `sanitizeAddress` and
`sanitizePemanfaatan` only trimmed and length-checked. Input such as
`<script>alert('XSS')</script>John` passed through untouched, while
`sanitizeEmail` and `isValidUrl` were strict. These values arrive from the
remote spreadsheet API and flow into `ValidatedDataItem`, the adapters, PDF and
CSV export.

The restored test suite caught this: `FoundationInfrastructureTest` asserted
`assertNotEquals("<script>alert('XSS')</script>John", sanitized1)` and failed.

**Fixed** — added `stripMarkup` (tag removal + stray angle-bracket removal +
whitespace collapse) applied by all three text sanitizers. Production behaviour
change, justified by the pre-existing failing regression test.

---

## MEDIUM

### 6. `java.time` on `minSdk 24` crashes receipt generation on Android 7

`receipt/ReceiptGenerator.kt:31` used `LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)`.
`java.time` requires API 26 and core library desugaring is **not** enabled
(`app/build.gradle` has no `coreLibraryDesugaring`), so on API 24/25 devices
`generateReceipt()` throws `NoClassDefFoundError`.

Found by lint (`NewApi`, severity Error). **Fixed** — replaced with
`SimpleDateFormat("yyyyMMdd", Locale.US)`, which yields the same `yyyyMMdd` form
on all API levels and matches the `Calendar`/`java.util.Date` style used
elsewhere. Lint `NewApi` count went 3 → 0.

### 7. Certificate pinning has zero redundancy

- `res/xml/network_security_config.xml` declares a `pin-set` with exactly one
  `<pin>` for `api.apispreadsheets.com`.
- `utils/Constants.kt:28` defines `BACKUP_CERTIFICATE_PINNER` as a **copy of the
  primary**, so `network/SecurityConfig.kt:18-20` adds the same value twice.
  The string `PLACEHOLDER` it checks for is not present in that constant, so the
  branch always runs.

A single cert rotation bricks the app in production with a pinning failure, and
there is no second pin to fall back to.

**Not fixed** — needs a genuine second pin from the certificate provider.

### 8. `SecurityManager` is pure theatre

`utils/SecurityManager.kt` — every method returns `true` or logs
"should be implemented":

```kotlin
fun isSecureEnvironment(): Boolean { Log.w(TAG, "..."); return true }
fun validateSecurityConfiguration(): Boolean { Log.w(TAG, "..."); return true }
fun checkSecurityThreats(vararg types: SecurityCheckType): Boolean { ...; return true }
```

A caller would receive false assurance. Mitigated only by the fact that it has
**no callers** in `app/src/main` — dead code.

**Not fixed** — recommend deleting it; a stub that claims success is worse than
no stub.

### 9. Process-wide mutable cache made tests lie

`utils/CacheManager.kt` is a singleton reached via `getInstance()`, and
`data/repository/BaseRepository.kt:getCachedOrNetwork` caches `user_list` for
5 minutes. Gradle runs all unit tests in one JVM, so the first passing test
populated the cache and every later test got a cache hit — the API mock was
never called, and 19 tests per repository failed as "WantedButNotInvoked".

**Fixed in tests** by clearing via `CacheManager.getInstance().clearSync()` in
`@Before`. The underlying design issue (global cache with no injection point) is
still worth addressing.

### 10. Debug HTTP client selected by an environment variable

`network/ApiConfig.kt:13`

```kotlin
val USE_MOCK_API = BuildConfig.DEBUG || System.getenv("DOCKER_ENV") != null
```

`DOCKER_ENV` forces cleartext `http://api-mock:5000/...` even when
`BuildConfig.DEBUG` is false, i.e. it can downgrade a release build to
cleartext. `usesCleartextTraffic="false"` in the manifest limits the blast
radius, so this is defence-in-depth rather than a direct exploit.

**Not fixed** — low practical risk; worth narrowing to debug builds.

---

## LOW / INFORMATIONAL

### 11. Only `MainActivity` is exported

`AndroidManifest.xml` — one `android:exported="true"` (the launcher). All other
activities, the `FileProvider`, and the application are `false`. `FileProvider`
uses `grantUriPermissions` rather than export, which is correct.

### 12. `cache-path path="."` in `res/xml/file_provider_paths.xml`

Exposes the entire cache directory to any component holding a URI grant. Not
directly exploitable (not exported), but broader than the receipts/photos the app
needs.

### 13. BODY-level HTTP logging in debug

`network/ApiConfig.kt:55` and `network/SecurityConfig.kt:32` log full
request/response bodies at `Level.BODY`, which includes resident names, emails
and addresses. Debug-only, so acceptable, but it does write PII to logcat on any
debug build.

### 14. No raw SQL

`transaction/TransactionDao.kt` is a Room `@Dao` with only `@Query`/`@Insert`/
`@Update`/`@Delete`; no `rawQuery`/`execSQL`/string-built SQL anywhere. No
SQL-injection surface found.

### 15. Database encryption is done correctly

`session/UserSessionManager.kt` and the Room factory use `SupportOpenHelperFactory`
with a `SecureRandom`-generated passphrase stored in `EncryptedSharedPreferences`
keyed by an Android Keystore `MasterKey` — not a hardcoded key. This is the one
crypto area that is right.

### 16. Constant-time comparison is acceptable

`utils/WebhookSecurityUtil.kt:174` returns early on a length mismatch, which
leaks only the hash length. For fixed-length HMAC hex digests that reveals
nothing useful. The per-character XOR accumulation is otherwise constant-time.
Recorded for completeness, not as a defect.

---

## Disproven during this review

Reported here so these are not re-investigated:

1. **HMAC hex encoding bug** — `WebhookSecurityUtil.kt:164` uses
   `"%02x".format(it)` on a `Byte`. Suspected sign-extension producing 8-digit
   output for bytes ≥ 0x80. **Tested on the JVM: Java's `Formatter` correctly
   converts `Byte` to unsigned**, output is the expected 64 hex chars. Not a bug.
2. **Certificate pin mismatch** — the live pin did not match the committed
   value, but the observed certificate was issued by this sandbox's intercepting
   proxy. The check is invalid inside this environment; **no mismatch is claimed**
   and the pin remains unverified.
3. **CORS `Access-Control-Allow-Origin` on the mock API** — the preflight
   returns `http://localhost:3000` rather than `*`. That is `flask-cors`
   reflecting the request Origin, which is spec-compliant. The test assertion
   was wrong, not the server.

---

## Summary

| Severity | Count | Fixed here | Open |
|---|---|---|---|
| Critical | 3 | 1 | 2 |
| High | 2 | 1 | 1 |
| Medium | 5 | 2 | 3 |
| Low / info | 6 | 0 | 6 |

Highest-value follow-ups, in order:

1. **Decide on authentication** (#2, #3). Everything else is secondary while the
   app has none and pretends to have some.
2. **Delete or fail-close the webhook path** (#4). It is currently unreachable,
   but it is a forgeable-secret-plus-fail-open combination waiting to be wired.
3. **Get a second real certificate pin** (#7) before the current one rotates.
4. **Delete `SecurityManager`** (#8) so it stops returning `true`.
5. **Remove the `DOCKER_ENV` cleartext override** (#10).