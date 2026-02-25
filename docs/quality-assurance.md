# Quality Assurance Agent - Long-term Memory

## Overview
This document serves as the long-term memory for the autonomous quality-assurance agent. It tracks patterns, lessons learned, and best practices for this codebase.

## Domain Focus
- Security best practices for Android financial applications
- Code quality improvements
- Small, safe, measurable fixes
- Documentation accuracy

## QA Issues Fixed

### 1. allowBackup Security Fix (2026-02-25)
- **Issue**: `android:allowBackup="true"` in AndroidManifest.xml
- **Problem**: Financial app data could be backed up and potentially exposed
- **Fix**: Changed to `android:allowBackup="false"`
- **File**: `app/src/main/AndroidManifest.xml`
- **Risk**: Low - no functional impact, only security hardening
- **Verification**: Build should pass, no behavioral changes

## Patterns to Check

### Security Checklist
- [ ] allowBackup should be false for financial apps
- [ ] No hardcoded secrets in source code
- [ ] Webhook signature verification implemented
- [ ] Room database encryption for sensitive data

### Code Quality
- [ ] Empty catch blocks should log errors
- [ ] DiffUtil instead of notifyDataSetChanged
- [ ] Proper error handling in repositories

### Documentation
- [ ] docs/blueprint.md matches actual code state
- [ ] No Java files remaining (except auto-generated BuildConfig)

## Lessons Learned

1. **Issue Validation**: Always verify that mentioned files actually exist before attempting to fix
2. **Proactive Scanning**: When no actionable issues exist, scan for related security/code quality issues
3. **Small Changes**: Prefer single-file, low-risk changes that are easy to verify

## Workflow
1. INITIATE: Check for open QA PRs → Check for QA issues → Proactive scan
2. PLAN: Identify fix, create TODO
3. IMPLEMENT: Make the change
4. VERIFY: Check the change is correct
5. SELF-REVIEW: Document what worked/didn't
6. SELF-EVOLVE: Update this document
7. DELIVER: Create PR with label "quality-assurance"
