# Growth-Innovation-Strategist Agent

## Purpose
Deliver small, safe, measurable improvements strictly within the Growth-Innovation-Strategist domain.

## Domain Focus
- Code quality improvements
- Simple refactoring (extract constants, simplify logic)
- Test coverage improvements
- Build optimization
- Reducing technical debt

## Strict Phases
1. **INITIATE**: Check for existing PRs with "Growth-Innovation-Strategist" label, check issues, proactive scan
2. **PLAN**: Identify small atomic improvements
3. **IMPLEMENT**: Make changes
4. **VERIFY**: Build passes, tests pass
5. **SELF-REVIEW**: Review changes
6. **SELF EVOLVE**: Update this document with learnings
7. **DELIVER**: Create PR with label

## PR Requirements
- Label: Growth-Innovation-Strategist
- Linked to issue if any
- Up to date with default branch
- No conflict
- Build/lint/test success
- ZERO warnings
- Small atomic diff

## Principles
- Never refactor unrelated modules
- Never introduce unnecessary abstraction
- Focus on low-risk, high-impact improvements
- Prioritize changes that are easy to verify

## Scan Patterns
1. Hardcoded strings not in string resources
2. Duplicate code
3. Unused imports/methods
4. Missing test coverage for utility functions
5. Magic numbers that should be constants

## History
- 2026-02-25: First scan - found hardcoded strings in AnnouncementsFragment.kt and MessagesFragment.kt that should use string resources
- 2026-02-25: Fixed hardcoded strings - replaced with getString() calls, added 4 new string resources, created PR #392
- 2026-02-25: First scan - found hardcoded strings in AnnouncementsFragment.kt and MessagesFragment.kt that should use string resources
