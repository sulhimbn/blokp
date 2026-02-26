# Technical Writer Agent

## Overview

The Technical Writer agent is responsible for maintaining and improving documentation across the blokp repository. This agent ensures documentation is accurate, consistent, and helpful for developers.

## Responsibilities

- Fix broken documentation links
- Maintain documentation consistency across files
- Create and update documentation as needed
- Verify documentation links are valid
- Create technical documentation for features and processes

## Key Documents

| Document | Purpose |
|----------|---------|
| `README.md` | Main project documentation |
| `AGENTS.md` | Agent-specific guidelines and commands |
| `docs/development-guidelines.md` | Coding standards and development workflow |
| `docs/ARCHITECTURE.md` | System architecture documentation |
| `docs/TROUBLESHOOTING.md` | Common issues and solutions |
| `docs/api-documentation.md` | API endpoint specifications |

## Common Issues Fixed

### Broken Links

**Issue**: References to `docs/DEVELOPMENT.md` which doesn't exist
- **Files affected**: README.md, AGENTS.md
- **Solution**: Update to point to `docs/development-guidelines.md`
- **Date**: 2026-02-25

### Documentation Inconsistencies

- Ensure all relative links point to existing files
- Verify file names match references (case-sensitive on Linux)
- Check for duplicate sections that may cause confusion
- **IMPORTANT**: Always grep for ALL occurrences of a term before claiming to have removed it

### PR Review Finding (2026-02-25)

**Issue**: PR #409 claimed to "remove Java compatibility reference" but only fixed ONE occurrence in README.md, leaving another instance on line 7
- **Fix applied**: Removed remaining "Java untuk kompatibilitas" reference
- **Lesson**: Always verify full file with grep before claiming complete fix
- **Verification**: Run `grep -n "Java" README.md` to ensure all instances addressed

- Ensure all relative links point to existing files
- Verify file names match references (case-sensitive on Linux)
- Check for duplicate sections that may cause confusion

## Workflow

1. **INITIATE**: Check for existing technical-writer PRs and issues
2. **SCAN**: Proactively look for documentation issues
3. **FIX**: Make small, atomic documentation improvements
4. **VERIFY**: Ensure build/lint passes
5. **PR**: Create PR with `technical-writer` label

## Quality Standards

- PRs must have `technical-writer` label
- Must be linked to an issue (or create one)
- Must be up to date with default branch
- No merge conflicts
- Zero build/lint warnings
- Small, atomic diffs

## Agent Communication

- Use session_id for follow-up tasks
- Document learnings in this file
- Share insights with other agents via shared documentation
## Agent Communication

XY|- Use session_id for follow-up tasks
KX|- Document learnings in this file
RM|- Share insights with other agents via shared documentation

## Learnings (2026-02-25)

### Documentation Fixes Completed

1. **AGENTS.md - Duplicate Header**
   - Issue: Duplicate `## Code Style` header on lines 29-30
   - Fix: Removed one duplicate header
   - Impact: Cleaner documentation structure

2. **README.md - Outdated Language Reference**
   - Issue: Line 7 mentioned "dilengkapi dengan Java untuk kompatibilitas" (equipped with Java for compatibility)
   - Fix: Updated to reflect 100% Kotlin - removed Java compatibility reference
   - Impact: Accurate representation of project language

3. **api-documentation.md - Missing Endpoints**
   - Issue: Many implemented endpoints were not documented
   - Fix: Added comprehensive documentation for:
     - Announcements (`GET /announcements`)
     - Messages (`GET /messages`, `GET /messages/{receiverId}`, `POST /messages`)
     - Community Posts (`GET /community-posts`, `POST /community-posts`)
     - Payments (`POST /payments/initiate`, `GET /payments/{id}/status`, `POST /payments/{id}/confirm`)
     - Vendors (`GET /vendors`, `GET /vendors/{id}`, `POST /vendors`, `PUT /vendors/{id}`)
     - Work Orders (`GET /work-orders`, `GET /work-orders/{id}`, `POST /work-orders`, `PUT /work-orders/{id}/assign`, `PUT /work-orders/{id}/status`)
   - Impact: Complete API reference for developers

### Proactive Scanning Tips

- Always check AGENTS.md for duplicate headers/sections
- Compare README.md statements with AGENTS.md (they should be consistent)
- Verify api-documentation.md matches actual implementation in ApiService.kt
- Check for outdated language references (Java vs Kotlin)
MX|- Look for missing model documentation when new endpoints are added

NW|4. **ARCHITECTURE.md - Outdated Language Reference**
   - Issue: Line 5 mentioned "hybrid Kotlin-Java" but project is 100% Kotlin
   - Fix: Updated to "100% Kotlin" to reflect current state
   - Impact: Accurate representation of project architecture

NW|5. **ARCHITECTURE.md - MenuActivity Still Listed as Java**
   - Issue: Line 82 listed "MenuActivity (Java)" but it's now Kotlin
   - Fix: Updated to "MenuActivity (Kotlin)"
   - Impact: Matches actual file extension (.kt)

NW|6. **ARCHITECTURE.md - Incomplete Network Layer Documentation**
   - Issue: Only documented 2 endpoints (users, pemanfaatan) but 18 exist
   - Fix: Added all 18 API endpoints to the Network Layer section
   - Impact: Complete API reference matching ApiService.kt

NW|7. **ARCHITECTURE.md - Outdated DiffUtil TODO**
   - Issue: Line 237 had "TODO: Replace with DiffUtil" but DiffUtil is already implemented
   - Fix: Updated comment to "Using DiffUtil via DiffUtil.calculateDiff()"
   - Impact: Accurate representation of adapter implementation

NW|8. **api-documentation.md - Outdated Date**
   - Issue: "Last Updated: November 2025" is outdated
   - Fix: Updated to "February 2026"
   - Impact: Current documentation timestamp

VB|### Proactive Scanning Tips (Extended)
   - Check ARCHITECTURE.md vs AGENTS.md for consistency
   - Verify all listed endpoints in docs match ApiService.kt implementations
   - Look for outdated TODO comments in documentation
   - Check Activity/Class language references against actual file extensions

9. **README.md - Git Stash Conflict Marker**
   - Issue: Lines 8-11 contained unresolved git stash markers (`=======` and `>>>>>>> Stashed changes`)
   - Fix: Removed duplicate content and conflict markers
   - Impact: Clean documentation without merge artifacts

10. **README.md - MenuActivity Language Reference**
    - Issue: Line 208 listed "MenuActivity (Java)" but file is .kt (Kotlin)
    - Fix: Updated to "MenuActivity (Kotlin)"
    - Impact: Consistent with actual file extensions

11. **AGENTS.md - Mixed Language Reference**
    - Issue: Line 14 said "Mixed Kotlin/Java codebase" but project is 100% Kotlin
    - Fix: Updated to "100% Kotlin codebase"
    - Impact: Accurate representation of project language

12. **AGENTS.md - Outdated Mixed Language Section**
    - Issue: Line 30 mentioned "maintain kompatibilitas Java" (maintain Java compatibility)
    - Fix: Updated to "Proyek 100% Kotlin: semua fitur baru dikembangkan dalam Kotlin"
    - Impact: Reflects current project state

### Proactive Scanning Tips (2026-02-25)
- Scan for git conflict markers (`<<<<<<`, `======`, `>>>>>>`) in markdown files
- Verify Activity class language references match actual file extensions (.kt vs .java)
- Check AGENTS.md and README.md for consistency in language descriptions
- Use `grep -n "MenuActivity" README.md` to find all references to verify consistency

13. **Multiple Docs - Outdated Java References**
   - Issue: 5 documentation files still referenced "hybrid Kotlin-Java" or "Mixed Kotlin/Java" language
   - Fix: Updated all references to reflect 100% Kotlin:
     - docs/ARCHITECTURE.md: "Arsitektur hybrid Kotlin-Java" → "Arsitektur 100% Kotlin"
     - docs/DX-engineer.md: "Android Application (Kotlin/Java)" → "Android Application (Kotlin)"
     - docs/DX-engineer.md: "Mixed Kotlin (new code) and Java (legacy)" → "100% Kotlin codebase"
     - docs/ai-agent-engineer.md: "Mixed Kotlin/Java codebase" → "100% Kotlin codebase"
     - docs/ROADMAP.md: "Arsitektur hybrid Kotlin-Java" → "Arsitektur 100% Kotlin"
     - docs/frontend-engineer.md: "Mixed Kotlin/Java Android project" → "100% Kotlin Android project"
   - Impact: All documentation now accurately reflects 100% Kotlin project state
    - Scan for git conflict markers (`<<<<<<`, `======`, `>>>>>>`) in markdown files
    - Verify Activity class language references match actual file extensions (.kt vs .java)
    - Check AGENTS.md and README.md for consistency in language descriptions
    - Use `grep -n "MenuActivity" README.md` to find all references to verify consistency

### PR #478 - Incomplete Fix Discovery (2026-02-26)

**Issue Found**: PR #478 claimed to fix formatting issues in `docs/ui-ux-engineer.md` but was INCOMPLETE:
- It fixed: duplicate Task 1 header, `HJ|→#HJ|`, and removed `#QW|`
- It MISSED: All the garbled hash prefixes in Task 3 section (lines 64-72)

**Root Cause**: The garbled prefixes (`#SQ|`, `#KY|`, `#TB|`, etc.) were NOT addressed by PR #478

**Fix Applied**:
- Created Issue #480 to track the incomplete fix
- Created PR #485 with COMPLETE fix addressing ALL garbled prefixes

**Verification**:
- `grep -n "^#[A-Z]{2,3}|" docs/ui-ux-engineer.md` - no matches = clean

### Proactive Scanning Tips (2026-02-26)
- Always verify what a PR CLAIMS to fix vs what it ACTUALLY fixes by comparing diffs
- Garbled hash prefixes follow pattern: `^#[A-Z]{2,3}|` - grep for this pattern
- When fixing incomplete PRs, always scan for ALL issues in the file, not just what was claimed

## Learnings (2026-02-26)

### Documentation Date Fix

14. **Multiple Docs - Outdated Last Updated Dates**
   - Issue: 3 documentation files still had "Last Updated: November 2025" while current date is February 2026
   - Fix: Updated all 3 files to "Last Updated: February 2026":
     - docs/actionable-tasks.md
     - docs/development-guidelines.md
     - docs/ROADMAP.md
   - Impact: Current documentation timestamps reflecting actual modification date
   - PR: #488
- Always verify what a PR CLAIMS to fix vs what it ACTUALLY fixes by comparing diffs
- Garbled hash prefixes follow pattern: `^#[A-Z]{2,3}|` - grep for this pattern
- When fixing incomplete PRs, always scan for ALL issues in the file, not just what was claimed

### PR #488 - Last Updated Date Fix Review

15. **PR #488 - Last Updated Dates February 2026**
   - Issue: 3 documentation files had outdated "Last Updated: November 2025"
   - Fix Applied: PR #488 correctly updates all 3 files to February 2026
   - Files Updated:
     - docs/actionable-tasks.md
     - docs/development-guidelines.md
     - docs/ROADMAP.md
   - Status: MERGEABLE, no conflicts with main
   - Learning: Always verify PR claims vs actual changes using `git diff`
   - Date: 2026-02-26
