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
