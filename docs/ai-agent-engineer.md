# AI Agent Engineer Domain

## Overview

This document serves as the long-term memory for the ai-agent-engineer domain. It documents the autonomous agent ecosystem, workflows, best practices, and operational guidelines for this repository.

## Autonomous Agent Ecosystem

### Agent Workflows

This repository employs multiple autonomous agents that work together to maintain and improve the codebase:

#### Primary Agent Workflows (OpenCode-powered)

| Workflow File | Name | Purpose | Trigger | Model | Runner | Timeout |
|---------------|------|---------|---------|-------|--------|---------|
| `oc-issue-solver.yml` | Issue Solver | Handles open issues end-to-end: analyze, plan, implement, verify, create PR | Schedule (30 min), manual | iflowcn/qwen3-coder-plus | ubuntu-slim | 40 min |
| `oc-pr-handler.yml` | PR Handler | Maintains PRs, resolves feedback, fixes checks, merges when ready | Schedule (9,15,21 UTC), PR events, manual | iflowcn/qwen3-coder-plus | ubuntu-24.04-arm | 40 min |
| `oc-maintainer.yml` | Maintainer | Repository health scans, proactive maintenance, security, CI improvements | Schedule (daily 3 UTC), manual | iflowcn/glm-4.6 | ubuntu-slim | 40 min |
| `oc-repo-manager.yml` | Repo Manager | Issue/PR coordination, label management, stale tracking, dependency monitoring | Schedule (6 hours), PR events, push to main, manual | iflowcn/glm-4.6 | ubuntu-slim | 30 min |
| `oc-release-manager.yml` | Release Manager | Analyzes commits, creates changelogs, manages GitHub releases | Manual (workflow_dispatch) | iflowcn/glm-4.6 | ubuntu-slim | 40 min |
| `oc-code-quality-analyzer.yml` | Code Quality Analyzer | Deep code analysis, identifies bugs, performance issues, creates improvement issues | Schedule (daily 2 UTC), manual | iflowcn/glm-4.6 | ubuntu-slim | 60 min |

#### Event-Driven Workflows

| Workflow File | Name | Purpose | Trigger |
|---------------|------|---------|---------|
| `on-push.yml` | On Push | Multi-phase autonomous agent: handles PRs→issues→analysis→product thinking→docs | Push to main, manual |
| `on-pull.yml` | On Pull | PR review, merge handling, issue management with quality scoring | PR events, schedule (hourly), manual |
| `parallel.yml` | Parallel | Multi-stage pipeline: Architect → Implement → Verify → Review → Release | Push to main, schedule (4 hours), manual |

#### Additional Specialized Workflows

| Workflow File | Name | Purpose | Trigger |
|---------------|------|---------|---------|
| `oc-researcher.yml` | Researcher | Research and investigation tasks | Manual |
| `oc-problem-finder.yml` | Problem Finder | Proactive issue discovery | Manual |

#### Global Concurrency Control

Most agent workflows use a global concurrency group to prevent parallel execution:
```yaml
concurrency:
  group: ${{ github.repository }}-global-workflow
  cancel-in-progress: false
```

#### Common Patterns

All OpenCode-powered workflows:
- Use `softprops/turnstyle` for queue management (where applicable)
- Cache OpenCode CLI between runs
- Configure Git identity for commits
- Support both scheduled and manual (`workflow_dispatch`) triggers
- Set appropriate timeout limits to prevent runaway jobs

### Agent Identity

Agents operate using the following Git identity:
- Email: `maskom_team@ma-malnukananga.sch.id` (issue solver, maintainer, code quality analyzer)
- Email: `pr_handler@jasaweb.co.id` (PR handler, release manager)
- Email: `repo-manager@ai-agent.local` (repo manager)

#### Common Permissions

All agent workflows request these permissions:
- `contents: write` - For creating branches, commits, PRs
- `pull-requests: write` - For creating and updating PRs
- `issues: write` - For creating and updating issues
- `actions: write` - For managing workflow runs (some workflows)
- `id-token: write` - For OIDC authentication

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

### Repo Manager Agent

**Capabilities:**
- Issue management: identify duplicates, add labels, suggest milestones
- PR review: code review best practices, bug detection, reviewer suggestions
- Code & quality: metrics analysis, technical debt identification, dependency monitoring
- Documentation: ensure documentation stays current
- Security: vulnerability scanning, credential detection

**Triggers:**
- Schedule (every 6 hours)
- PR events (opened, reopened, synchronize, ready_for_review)
- Push to main/master
- Issue comments with `/repo-manager` command
- Manual workflow dispatch

### Release Manager Agent

**Capabilities:**
- Analyze commit history for changes since last release
- Categorize changes (features, bug fixes, breaking changes, performance)
- Generate structured changelogs (Keep a Changelog format)
- Create GitHub releases with semantic versioning
- Handle version conflicts with fallback to patch increment

**Usage:**
- Manual trigger only via workflow_dispatch
- Accepts version input (optional, auto-determines if not provided)
- Supports draft releases

### Code Quality Analyzer Agent

**Capabilities:**
- Deep code analysis for bugs and errors
- Performance pattern analysis
- Code consistency evaluation
- Dependency integration analysis
- Identify code consolidation opportunities

**Constraints:**
- Maximum 10 issues per execution to avoid spam
- Only create issues for significant findings
- Avoid speculative changes without concrete evidence
- Focus on improvements with real value

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
- 100% Kotlin codebase
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
