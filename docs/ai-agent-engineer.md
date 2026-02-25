# AI Agent Engineer Domain

## Overview

This document serves as the long-term memory for the ai-agent-engineer domain. It documents the autonomous agent ecosystem, workflows, best practices, and operational guidelines for this repository.

## Autonomous Agent Ecosystem

### Agent Workflows

This repository employs multiple autonomous agents that work together to maintain and improve the codebase:

| Workflow | Purpose | Trigger |
|----------|---------|---------|
| `oc-issue-solver` | Handles open issues end-to-end | Schedule (30 min), manual |
| `oc-pr-handler` | Maintains PRs, resolves feedback, merges when ready | Schedule, PR events |
| `oc-maintainer` | Repository maintenance, health scans | Schedule (daily) |
| `oc-repo-manager` | General repo management, issue/PR coordination | Schedule (6 hours), events |
| `oc-researcher` | Research and investigation | Manual |
| `oc-problem-finder` | Proactive issue discovery | Manual |
| `oc-code-quality-analyzer` | Code quality analysis | Manual |
| `oc-release-manager` | Release management | Manual |

### Agent Identity

Agents operate using the following Git identity:
- Email: `maskom_team@ma-malnukananga.sch.id` (issue solver)
- Email: `pr_handler@jasaweb.co.id` (PR handler)

## Operating Contract

### Issue Solver Agent

**Hard Constraints:**
- Do not work on more than one issue in a single run
- Do not make unrelated changes, refactors, or cleanups
- Do not rewrite project structure, tooling, or CI configuration unless the issue explicitly requires it
- Always keep existing coding style, patterns, and conventions
- If something is unclear, ask for clarification in an issue comment before implementing

**Priority Order:**
1. Issues with highest priority labels (P0, P1, critical)
2. Issues assigned to the agent
3. Issues with highest user impact
4. Oldest open issue

**Workflow:**
1. Scan and select issue
2. Post plan comment on issue
3. Implement solution
4. Verify with tests/checks
5. Create PR with proper linking
6. Comment on issue with PR reference

### PR Handler Agent

**Hard Constraints:**
- Do not work on more than one PR/branch per run
- Do not create new PRs; only maintain existing ones
- Do not merge unless all conditions are met:
  - All required checks/builds passing
  - No unresolved change requests
  - Branch up to date with target
  - Repository policies allow merges
- Never disable tests or quality gates to "make CI green"

**Priority Order:**
1. PRs labeled for bot/automation handling
2. PRs with requested changes/unresolved comments
3. PRs with failing required checks
4. Oldest PR
5. PRs closest to merge-ready

**Workflow:**
1. Select PR or active branch
2. Analyze PR, commits, diff, review comments
3. Post plan comment on PR
4. Address review comments and suggestions
5. Fix failing checks
6. Resolve conversations
7. Merge when ready or post final status

### Maintainer Agent

**Hard Constraints:**
- Prefer small, focused PRs
- Do not change licensing or metadata
- Do not disable security features or tests
- Do not push directly to protected branches

**Focus Areas:**
1. Security and correctness
2. CI stability and quality
3. Developer experience and maintainability
4. Performance and efficiency
5. Documentation and governance

## Issue/PR Labels

### Priority Labels
- `P0` - Critical, blocks development
- `P1` - High priority
- `P2` - Medium priority
- `P3` - Low priority

### Category Labels
- `bug` - Bug reports
- `enhancement` - Feature improvements
- `refactor` - Code refactoring
- `chore` - Maintenance tasks
- `security` - Security issues
- `ci` - CI/CD related
- `docs` - Documentation

### Special Labels
- `ai-agent-engineer` - Issues/PRs related to agent improvement

## Codebase Patterns

### Architecture
- MVVM with ViewModels in `presentation/viewmodel/`
- Mixed Kotlin/Java codebase
- Retrofit for API communication

### Key Files
- `AGENTS.md` - Agent instructions and build commands
- `app/src/main/java/com/example/iurankomplek/` - Main source code

### Build Commands
```bash
./gradlew build           # Full build
./gradlew test           # Run tests
./gradlew connectedAndroidTest  # Instrumented tests
```

## Best Practices

### For Issue Solving
1. Always confirm understanding by posting a plan before implementing
2. Keep PRs focused and small
3. Verify changes with actual test runs
4. Link PR to issue using "Fixes #123" syntax

### For PR Handling
1. Address all review comments appropriately
2. Never bypass tests to make CI green
3. Merge only when fully ready
4. Communicate status clearly in comments

### For Repository Maintenance
1. Scan for security issues first
2. Keep changes minimal and focused
3. Document any configuration changes
4. Create issues for deferred work

## Common Pitfalls

### Avoid
- Large, unrelated changes in single PR
- Disabling tests or quality gates
- Pushing directly to protected branches
- Making assumptions about unclear requirements
- Skipping verification steps

### Required
- Running tests before submitting PRs
- Following existing code patterns
- Proper issue/PR linking
- Clear communication in comments

## GitHub Actions Workflows

### Main CI/CD Workflows
| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `on-push.yml` | Push to main | Sequential 00-11 agent pipeline |
| `on-pull.yml` | PR opened/synced | PR handler with state machine |
| `parallel.yml` | Push to main | 4-stage parallel specialist execution |

### Autonomous Agent Workflows
| Workflow | Schedule | Purpose |
|----------|----------|---------|
| `oc-issue-solver.yml` | Every 30 min | Handles open issues end-to-end |
| `oc-pr-handler.yml` | 9am, 3pm, 9pm | Maintains PRs, resolves feedback, merges |
| `oc-maintainer.yml` | Daily 3 AM | Repository health scans, maintenance |
| `oc-repo-manager.yml` | Every 6 hours | Issue/PR coordination, project management |
| `oc-problem-finder.yml` | Daily midnight | Proactive issue discovery |
| `oc-code-quality-analyzer.yml` | On-demand | Code quality analysis |
| `oc-researcher.yml` | On-demand | Research and investigation |
| `oc-release-manager.yml` | On-demand | Release management |

### Execution Patterns
- **Sequential** (`on-push.yml`): Runs 12 specialized agents one-by-one (~6 hours)
- **Parallel** (`parallel.yml`): Runs 4 specialist stages concurrently (~30 min)
- **Scheduled** (oc-*): Time-based autonomous maintenance

## Self-Evolution

This document should be updated when:
1. New agent workflows are added
2. Operating procedures change
3. New patterns or conventions are established
4. Agent-related issues are discovered and resolved

---

*Last Updated: 2026-02-25*
*Domain: ai-agent-engineer*
