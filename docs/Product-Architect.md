# Product-Architect Agent Documentation

## Overview
This document serves as the long-term memory for the Product-Architect agent, tracking architectural decisions, patterns, and lessons learned.

## Agent Role
- Domain: Product-Architect
- Objective: Deliver small, safe, measurable improvements strictly inside the domain
- Strict Phases: INITIATE → PLAN → IMPLEMENT → VERIFY → SELF-REVIEW → SELF EVOLVE → DELIVER (PR)

## Working Protocol

### Phase 1: INITIATE
- Check for existing PR with "Product-Architect" label
- If exists: ensure up to date with default branch, review, fix if necessary, comment
- If Issue exists: execute
- If none: proactive scan limited to domain
- If nothing valuable: proactive scan repository health and efficiency

### Phase 2: PLAN
- Analyze the issue/requirement
- Identify dependencies
- Create work breakdown

### Phase 3: IMPLEMENT
- Make the required changes
- Follow existing code patterns
- Keep changes atomic and focused

### Phase 4: VERIFY
- Ensure code compiles/builds
- Run tests if available
- Verify no regressions

### Phase 5: SELF-REVIEW
- Review changes for correctness
- Check for edge cases
- Ensure proper error handling
- Verify no security issues

### Phase 6: SELF-EVOLVE
- Check other agents' long time memory (docs/*Agent*.md)
- Update this document with learnings
- Identify process improvements

### Phase 7: DELIVER
- Create PR with "Product-Architect" label
- Link to issue if exists
- Ensure up to date with default branch
- No conflicts
- Add descriptive comments

## Key Lessons Learned

### Bug Fix Pattern: External Scope Management
When fixing CoroutineScope lifecycle issues:
- Always track whether scope is owned (created internally) vs external (passed in)
- Never cancel external scopes - only cancel scopes you created
- Use pattern: `ownsScope: Boolean = externalScope == null`

### Code Review Checklist
- Check for edge cases in dependency injection
- Verify lifecycle methods handle cleanup properly
- Look for resource leaks (coroutines, threads, connections)
- Ensure backward compatibility

### Documentation Maintenance
- Keep history entries clean - remove duplicates
- Update relevant agent docs when patterns are discovered

### Systematic Refactoring Pattern
When refactoring scattered hardcoded values:
- Use grep to identify all occurrences (42 found)
- Verify existing centralized constants (Constants.Toast already existed)
- Update files systematically using edit tool with LINE#ID
- Add required imports (Constants) to each file
- Verify with grep after completion (0 remaining in code)
- Keep history entries clean - remove duplicates
- Update relevant agent docs when patterns are discovered

## History
- 2026-02-25: Fixed CoroutineScope bug in PaymentService.kt - added ownsScope tracking to prevent external scope cancellation
- 2026-02-25: Documented @Suppress UNCHECKED_CAST annotations in 5 files - these are legitimate use cases that cannot be fixed due to Kotlin type erasure and standard Android ViewModelFactory patterns
- 2026-02-25: Fixed CoroutineScope bug in PaymentService.kt - added ownsScope tracking to prevent external scope cancellation
- 2026-02-25: Refactored Toast durations - replaced 42 hardcoded Toast.LENGTH_SHORT/LONG with Constants.Toast.DURATION_SHORT/LONG across 13 files (Issue #431)
- 2026-02-25: Fixed CoroutineScope bug in PaymentService.kt - added ownsScope tracking to prevent external scope cancellation
- 2026-02-25: Documented @Suppress UNCHECKED_CAST annotations in 5 files - these are legitimate use cases that cannot be fixed due to Kotlin type erasure and standard Android ViewModelFactory patterns
- 2026-02-25: Fixed CoroutineScope bug in PaymentService.kt - added ownsScope tracking to prevent external scope cancellation
