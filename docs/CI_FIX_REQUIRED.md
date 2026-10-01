# CI Status

> Rewritten after `main` recovered a compiling, CI-gated build in #557. The previous
> version of this file described a Java 11 failure that no longer exists and recommended
> a workflow change that is now known to break the runner. Do not apply it.

## Current state

`.github/workflows/build.yml` is the single gate. On `push` to `main`, on every
`pull_request`, and on manual dispatch it:

1. checks out with `actions/checkout@v5`,
2. sets up JDK 17 (Temurin) with the Gradle cache,
3. locates the SDK that ships on the `ubuntu-24.04` image and installs
   `platform-tools`, `platforms;android-34`, `build-tools;34.0.0`,
4. runs `./gradlew --no-daemon :app:assembleDebug`.

`on-pull.yml` and `oc-pr-handler.yml` no longer invoke Gradle at all, so they need no
JDK or SDK setup. Do not add it back.

## Do not reintroduce `android-actions/setup-android@v3`

That action shells out to `sdkmanager tools`, and Google removed that package from
the command-line tools repository. It exits 1 on current runners before any build
starts. `build.yml` locates the SDK's own `sdkmanager` instead; that is deliberate.

## Known gap: the unit test suite is not compiled

`:app:testDebugUnitTest` is intentionally absent from the gate. `app/src/test` has
not compiled for some time and still does not — for example `PemanfaatanItem` is
referenced by two test files and does not exist anywhere in `app/src/main`:

```
$ grep -rl '\bPemanfaatanItem\b' app/src/main --include=*.kt | wc -l   # 0
$ grep -rl '\bPemanfaatanItem\b' app/src/test --include=*.kt | wc -l   # 2
```

Until that is repaired, enabling the step would swap a green gate for a red one.
Restoring it is tracked separately (referenced as #558 by the comment in
`build.yml`) and must land as its own change: fix the tests, confirm
`./gradlew :app:testDebugUnitTest` is green locally, then add the step to
`build.yml` in a following commit.

Until then, treat "CI is green" as "the debug variant assembles", not "the suite
passes". Do not merge on the strength of the build gate alone when a change is
logic-only.

## Verifying a workflow change locally

This needs JDK 17 and the Android SDK on `PATH`; neither is present in every
sandbox. If you cannot run the build, say so in the PR rather than reporting the
gate as verified.