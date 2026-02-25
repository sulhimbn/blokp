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
- Look for missing model documentation when new endpoints are added
