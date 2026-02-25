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
