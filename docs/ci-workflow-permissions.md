# Workflow permissions

Every scope a GitHub Actions workflow grants is deliberate and recorded, and a
check enforces it. This document explains how to change a grant correctly, and
what the check does *not* protect you from.

## The check

```bash
python3 scripts/check_workflow_permissions.py            # audit the current tree
python3 scripts/check_workflow_permissions.py --self-test  # also prove the audit still bites
```

`workflow-permissions` runs both on every pull request and on pushes to `main`,
in about ten seconds. `build` is a separate check and stays the merge gate.

The audit fails when:

- a workflow grants a scope that has no entry in `ALLOWED`
- `ALLOWED` cites a scope or a level the workflow no longer grants
- `ALLOWED` cites a file that no longer exists
- a `permissions:` block is written in a form the parser cannot read (flow
  mapping, `read-all`/`write-all`, an unknown scope name, a level other than
  `read`/`write`/`none`)

The last case is deliberate. The parser is hand-rolled rather than a YAML
library so it has no dependencies, and it fails closed: syntax it cannot audit
is an error, never a pass. A guard that shrugs at what it cannot read is worse
than no guard.

`--self-test` copies the workflows to a temporary directory, injects
`id-token: write` into each one, and requires the audit to reject every
injection. It runs in CI on the same step, so a future rewrite of the checker
cannot quietly stop catching anything.

## Adding a permission

A permission block carries no evidence of intent, which is why ten workflows
once granted `id-token: write` that nothing consumed: the grant came from
copy-pasting `oc - template.md` (#560). Adding one therefore takes two edits:

1. the `permissions:` block in the workflow
2. an `ALLOWED` entry in `scripts/check_workflow_permissions.py`

```python
"on-push.yml": {
    "contents": "write - pushes the branch and merges it",
    "pull-requests": "write - 'Merge PR if all checks are green'",
},
```

Cite the instruction that needs the scope, quoted verbatim from the workflow, so
a reviewer can grep for the phrase. Line numbers are not used: they move. If the
scope is not consumed by anything the agent is instructed to do, do not add it.

Dropping a permission needs the same two edits. The audit is exact in both
directions, so a stale citation cannot sit in the script waiting to justify
re-adding a grant nobody needs.

## What this does not cover

**A `permissions:` block only binds the implicit `GITHUB_TOKEN`.** Most of the
agent workflows export a different token:

| Authenticates the agent with | Workflows |
|---|---|
| the workflow's `GITHUB_TOKEN`, so `permissions:` binds it | `build.yml`, `on-pull.yml`, `on-push.yml`, `parallel.yml`, `workflow-permissions.yml` |
| `secrets.GH_TOKEN`, a PAT whose scopes live on the token | `oc - code quality analyzer.yml`, `oc - release manager.yml`, `oc- researcher.yml`, `oc-issue-solver.yml`, `oc-maintainer.yml`, `oc-pr-handler.yml`, `oc-problem-finder.yml`, `oc-repo-manager.yml` |

For the second group, narrowing `permissions:` does not reduce what the agent
can do, because `gh` authenticates with the PAT. Moving those workflows to
`secrets.GITHUB_TOKEN` is the change that would actually make their grants
load-bearing. It is tracked separately because it cannot be validated by reading
YAML, and it is not bundled into the removal of `id-token: write`.

**A recorded grant is not a grant that is still needed.** The audit proves each
scope is deliberate. Only a run log proves it is required. When you narrow a
workflow, read what the agent step failed with before adding the scope back.

## Ownership

Adding or widening a permission is a security review, not a mechanical fix. It
needs someone who can say what consumes the scope.
