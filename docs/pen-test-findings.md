# Pen-test / security audit — BlokP Android client

Scope: `app/src/main` (Kotlin), `AndroidManifest.xml`, `res/xml/network_security_config.xml`,
release APK artifact, and `mock-api/`. Date: 2026-10-02.

## Method

- Static review of every network, storage, crypto, auth and export path.
- `AndroidManifest` component/permission/exported surface.
- Certificate-pinning + cleartext policy in `network_security_config.xml`.
- Secret extraction attempt against the **release** DEX (`strings`-equivalent byte scan).
- `android.util.Log` audit for sensitive-data leakage.
- Endpoint probing of the mock backend for authz/CORS/error handling.

## Confirmed strengths (evidence)

| Area | Evidence |
|---|---|
| Exported surface | Only `.MainActivity` is exported, guarded by the launcher intent-filter. Everything else `exported="false"`. No custom `BroadcastReceiver`/`Service` registered — the webhook receiver is an ordinary class, not a manifest component. |
| App-data protection | `android:allowBackup="false"`, `usesCleartextTraffic="false"`. |
| Transport | `network_security_config.xml` pins `api.apispreadsheets.com` with `sha256/PIdO5FV9...`, `cleartextTrafficPermitted="false"`, pin expiry 2028-12-31. |
| At-rest DB | `TransactionDatabase.kt:33` uses SQLCipher `SupportOpenHelperFactory` with a 256-bit `SecureRandom` passphrase; the passphrase itself lives in `EncryptedSharedPreferences` (AES256-GCM / AES256-SIV) backed by `MasterKey`. |
| Session storage | `UserSessionManager.kt:28-39` uses `EncryptedSharedPreferences` + AES256-GCM values; keys AES256-SIV. |
| Webhook verification | `WebhookSecurityUtil` verifies HMAC-SHA256 signature and a ±5-minute timestamp window (replay defence) before processing payload. |
| Dep secrets | Release DEX byte scan found **no** `whsec_*` literal — R8 eliminated the placeholder webhook secret because the webhook path is unreachable from any manifest entry point. |

## Findings

### P1 — `ACCESS_NETWORK_STATE` was never declared (fixed)
`NetworkUtils.isNetworkAvailable()` (`utils/NetworkUtils.kt:11-12`) calls
`ConnectivityManager.activeNetwork` / `getNetworkCapabilities`, both of which require
`android.permission.ACCESS_NETWORK_STATE`. The manifest declared only `INTERNET`, so
`activeNetwork` always returned `null` and the helper always returned `false`.
`BaseActivity.executeWithRetry` (`BaseActivity.kt:30`) gates *every* retry on that helper,
so the legacy retry path reported "no internet connection" unconditionally.
Found via lint `MissingPermission` (5 occurrences). Fixed by declaring the permission;
lint `MissingPermission` count went 5 → 0.

### P1 — `java.time` on `minSdk 24` without desugaring (fixed)
`receipt/ReceiptGenerator.kt:31` uses `java.time.LocalDate.now()` and
`DateTimeFormatter.BASIC_ISO_DATE`. `java.time` is API 26+, `minSdk` is 24, and
`coreLibraryDesugaringEnabled` was not set, so receipt generation throws
`NoClassDefFoundError` on Android 7.0–7.1. Surfaced as 3 lint **error**-severity `NewApi`
issues. Fixed by enabling core library desugaring and adding
`com.android.tools:desugar_jdk_libs:2.0.4`. Lint errors: 4 → 1.

### P2 — First load of users, financial data and vendors is a permanent no-op (fixed)
`UserViewModel.loadUsers()`, `FinancialViewModel.loadFinancialData()`,
`VendorViewModel.loadVendors()` and `loadWorkOrders()` all began with
`if (_state.value is UiState.Loading) return`. The states are *initialised* to `Loading`, so
the guard was always true on the first call and the repository was never hit. Data only
appeared after some other code path moved the state off `Loading`. Replaced the UI-state
guard with an explicit in-flight flag (`isLoading`) plus `finally { isLoading = false }`,
which keeps the intended de-duplication and lets the first call run.

### P2 — `sanitizeName` did not sanitise (fixed)
`utils/DataValidator.sanitizeName()` only trimmed and length-checked;
`<script>alert('XSS')</script>John` passed through unchanged. Values originate from a
third-party spreadsheet API and are rendered in the UI and written to exports.
`sanitizeName`/`sanitizeAddress`/`sanitizePemanfaatan` now strip markup tags and control
characters via a shared helper.

### P3 — Certificate pin has no working backup (open, pre-existing)
`Constants.CERTIFICATE_PINNER` and `BACKUP_CERTIFICATE_PINNER` are the same value and
`ALL_CERTIFICATE_PINS` contains a single entry; the XML `pin-set` likewise declares one pin.
A routine certificate rotation on `api.apispreadsheets.com` will hard-fail the app for all
users until a new release ships. Documented in-code as a TODO. **Not fixed** — it needs a
real backup pin from the certificate provider.

### P3 — Lint cannot fail CI (open, configuration)
`app/build.gradle:61` sets `abortOnError false`, so lint **errors** never fail the build.
This is exactly how the two P1 defects above survived. The single remaining lint error is
`NotificationPermission`: no notification code exists in `app/src`, and the merged manifest
contains no notification component, so it is not app-caused — but nothing in CI would catch
a genuine error either.

### P3 — Debug build can only reach its backend inside docker-compose
`ApiConfig.BASE_URL` targeted the hardcoded docker hostname `api-mock:5000`, which does not
resolve for a plain `./gradlew installDebug` on an emulator or device; every screen then
errored. `network_security_config.xml` additionally declares `api-mock` in a
`cleartextTrafficPermitted="false"` domain-config, which contradicts its own "local
development server" comment (debug builds only work via `<debug-overrides>`).
Partially mitigated: the mock host is now a build-configurable property
(`-PmockApiHost=…` / `MOCK_API_HOST`, default unchanged at `api-mock:5000`). The misleading
`domain-config` entries were left as-is to avoid changing the production policy.

### P4 — Mock backend has no authentication (dev-only)
`mock-api/app.py` enables `CORS(app)` (echoes any origin) and serves every record without
auth. It is a local development fixture bound to `0.0.0.0`. Acceptable for local dev;
must not be deployed publicly. Informational.

## Not exploitable / triaged out

- **`TrustAllX509TrustManager` (lint warning)** — located inside
  `bcpkix-jdk18on-1.75.jar` (BouncyCastle, pulled in transitively), not in app code. False
  positive for this project; noted only as a dependency-supply-chain watch item.
- **Placeholder webhook secret** — present in source but absent from the release DEX (R8
  dead-code elimination, since no manifest component reaches `WebhookReceiver`). Latent
  hardening item rather than a live vulnerability.
- **`SharedFlow.replay`** — not a security issue; the production `EventBus` exposes
  `SharedFlow`, which has no `replay` property (that belongs to `MutableSharedFlow`). The
  replay semantics are now tested behaviourally instead.