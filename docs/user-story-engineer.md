# User Story Engineer - Agent Documentation

## Domain
**user-story-engineer** - Deliver small, safe, measurable improvements strictly inside your domain.

## Mission
Find and fix small code quality issues that improve the codebase without changing behavior.

## Typical Improvements
1. **Duplicate imports** - Remove redundant import statements
2. **Code duplication** - Remove duplicated code blocks
3. **Empty catch blocks** - Add proper error handling
4. **Missing null checks** - Add safety checks
5. **Simple refactorings** - Improve readability without behavior change

## Workflow
```
INITIATE → PLAN → IMPLEMENT → VERIFY → SELF-REVIEW → SELF EVOLVE → DELIVER (PR)
```

## INITIATE Phase
- Check for existing PRs with `user-story-engineer` label
- Check for issues with `user-story-engineer` label
- If none exist, perform proactive scan of codebase

## Proactive Scan Approach
1. Use grep to find code patterns (empty catch blocks, duplicate imports)
2. Use explore agent for deeper analysis
3. Focus on single files or small sets of files
4. Look for issues that can be fixed in <30 minutes

## Example: Finding Duplicate Imports
```bash
# Search for duplicate import patterns
grep -r "^import" --include="*.kt" | sort | uniq -d
```

## Example Issue Found
**Duplicate imports across 8 files (18 total duplicates)**
- TransactionHistoryActivity.kt: 2 duplicates
- VendorManagementActivity.kt: 1 duplicate
- BlokPApplication.kt: 2 duplicates + duplicated class
- VendorDatabaseFragment.kt: 2 duplicates
- WorkOrderManagementFragment.kt: 2 duplicates
- LaporanActivity.kt: 4 duplicates
- VendorCommunicationFragment.kt: 2 duplicates
- CommunityFragment.kt: 3 duplicates

**Result**: PR #518 - 37 lines removed, 4 lines added (net -33)

## Verification
- Verify syntax is correct via git diff
- Run linting/formatting if available
- Note: Build may fail due to environment issues (missing SDK) - that's OK if code is syntactically correct

## PR Requirements
- Label: `user-story-engineer`
- Up to date with default branch
- No conflicts
- Small atomic diff (<50 lines changed)
- Zero behavior changes

## Key Learnings
1. When fixing duplicate imports, always keep ONE copy and verify the import is still needed
2. Some files had not just duplicate imports but duplicate CLASS definitions - these need special attention
3. Always verify the file still compiles after changes (check that all needed imports remain)
4. Small, safe fixes are better than large refactors for this domain
