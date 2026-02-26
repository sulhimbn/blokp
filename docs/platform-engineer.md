# Platform Engineer Documentation

This document serves as the long-term memory for the platform-engineer agent, tracking improvements made to enhance platform infrastructure, build systems, CI/CD, and developer tooling in this repository.

## Repository Context

- **Project Type**: Android Application (Kotlin/Java)
- **Build System**: Gradle 8.1.0 with Android Gradle Plugin
- **Architecture**: MVVM with Hilt dependency injection
- **Language**: Mixed Kotlin (new code) and Java (legacy)

## Platform Improvements Made

### 2026-02-25: opencode.json Configuration Fix

**Issue:** CI workflow was failing with error:
```
Error: Config file at /home/runner/work/blokp/blokp/opencode.json is invalid
↳ Unrecognized key: "compaction"
```

**Root Cause:** The opencode.json config file contained an invalid key "compaction" which is not recognized by the OpenCode configuration schema.

**Changes:**
- Removed invalid `compaction` block from opencode.json (lines 7-10)
- Validated JSON syntax after fix

**Impact:**
- Fixed CI workflow failures
- OpenCode agent can now execute properly

**Files Changed:**
- `opencode.json`

---

### 2026-02-25: app/build.gradle Syntax Fix

**Issue:** Gradle build was failing with syntax error:
```
build file '/home/runner/work/blokp/blokp/app/build.gradle': 51: Unexpected input: '{\n        jvmTarget = \'1.8\'\n    }\n}' @ line 51, column 1.
```

**Root Cause:** Duplicate closing brace in android block - there was an extra `}` after the `defaultConfig` block closing brace.

**Changes:**
- Removed duplicate closing brace in app/build.gradle (line 28)

**Impact:**
- Fixed Gradle configuration parsing
- Build configuration is now valid

**Files Changed:**
- `app/build.gradle`

---

## Platform Engineering Checklist

For future platform-engineer work:

- [ ] Validate opencode.json JSON syntax before committing
- [ ] Verify Gradle configuration with `./gradlew help` before CI runs
- [ ] Check for duplicate braces/brackets in build files
NB|- [ ] Monitor workflow runs for platform-related failures

---

## Key Learnings

### Merge Conflict Resolution
When fixing old branches with merge conflicts:
1. Check if commits on the branch are still relevant to main
2. Use `git cherry-pick` instead of rebase when only specific commits are needed
3. Resolve content conflicts carefully - check both sides of conflict markers

### CI/Workflow Debugging
- Scheduled CI runs may fail even after fix is merged (due to timing)
- Always verify the CI run timestamp vs merge timestamp
- Check workflow logs for specific error messages

### Git Branch Management
- PRs with `platform-engineer` label should be kept up to date with main
- Force push with `--force-with-lease` to update PR branches safely
- Verify mergeability status after pushing changes
