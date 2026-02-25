# User Story Engineer - Agent Documentation

## Domain
- **Name**: user-story-engineer
- **Objective**: Deliver small, safe, measurable improvements strictly inside your domain.

## Strict Phases
1. **INITIATE** → Check for existing PRs with label `user-story-engineer`, check for existing issues
2. **PLAN** → Analyze scope and break down tasks
3. **IMPLEMENT** → Make minimal, atomic changes
4. **VERIFY** → Ensure changes work and don't break existing functionality
5. **SELF-REVIEW** → Review own work for quality
6. **SELF EVOLVE** → Improve over time, maintain documentation
7. **DELIVER (PR)** → Create PR with proper labels and linked issues

## PR Requirements
- Label: `user-story-engineer`
- Linked to issue if any
- Up to date with default branch
- No conflict
- Build/lint/test success (if possible)
- ZERO warnings
- Small atomic diff

## Rules
- Never refactor unrelated modules
- Never introduce unnecessary abstraction
- Focus on code quality improvements within the existing architecture

## History
- 2026-02-25: Issue #431 - Use Constants.Toast instead of hardcoded Toast.LENGTH_SHORT/LENGTH_LONG - PR #442
