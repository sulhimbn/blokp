# User-Story Engineer Agent - Long-term Memory

## Overview
This document serves as the long-term memory for the autonomous user-story-engineer agent working on the blokp Android project.

## Domain
- Code quality improvements
- Small, safe, measurable improvements
- Refactoring for consistency
- Technical debt reduction

## Key Patterns

### Small, Safe Improvements
- Focus on atomic, small changes
- Avoid large refactoring that could introduce bugs
- Prioritize consistency improvements
- Use centralized constants instead of hardcoded values

### Issue Handling
- Check for existing issues with `user-story-engineer` label first
- If no issues, do proactive scan for small improvements
- Create issue before fixing when possible
- Link PR to issue

## Issue Categories

### Code Quality Issues
- Inconsistent use of constants
- Hardcoded values that should be centralized
- Missing imports or unused imports
- @Suppress warnings that can be legitimately removed

### Consistency Improvements
- Ensure new patterns are applied consistently
- Follow existing PR patterns (e.g., PR #389 for Constants.Toast)
- Update multiple files to maintain consistency

## Known Issues Fixed

### PR #433: Use Constants.Toast for toast durations
- Issue #431
- Updated 3 files to use `Constants.Toast.DURATION_SHORT` and `Constants.Toast.DURATION_LONG`:
  - DashboardActivity.kt
  - WorkOrderDetailActivity.kt
  - VendorCommunicationFragment.kt
- This follows the pattern from PR #389 which introduced Constants.Toast

## Findings

### Constants.Toast Usage
- `Constants.Toast.DURATION_SHORT` and `Constants.Toast.DURATION_LONG` are defined in `Constants.kt`
- Originally only MainActivity.kt used these constants (from PR #389)
- Found 38+ occurrences across 13 files still using hardcoded `Toast.LENGTH_SHORT/LENGTH_LONG`
- This is a large inconsistency that can be fixed incrementally

## Notes
- This is an Android/Kotlin project
- No Android SDK available in CI - build verification limited
- PRs should be small and atomic
- Always link to related issues

## Future Work
- Continue updating files to use Constants.Toast (remaining ~35 occurrences)
- Look for other consistency improvements
- Check for other hardcoded values that should be constants
