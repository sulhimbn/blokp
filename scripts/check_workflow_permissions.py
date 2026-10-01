#!/usr/bin/env python3
"""Reject workflow `permissions:` grants that nobody justified.

Ten of this repository's workflows granted `id-token: write` and nothing minted
an OIDC token. The grant arrived by copy-paste from `oc - template.md` and
survived review because a permission block carries no evidence of intent (#560).
This makes the evidence explicit: every scope a workflow grants must appear in
ALLOWED below, next to the instruction that needs it. A grant with no entry
fails, and so does a stale entry, so the two cannot drift apart.

The parser is deliberately hand-rolled and fails closed. `permissions:` written
as a flow mapping, as `read-all`/`write-all`, with an unknown scope name, or at
a level other than read/write/none is reported as an error rather than skipped.
A guard that passes what it cannot read is worse than no guard.

Scope of the guarantee: this checks that each grant is *deliberate and recorded*.
It cannot tell you a recorded grant is still needed - only a run log can. It
also only binds the implicit `GITHUB_TOKEN`. Jobs that export
`GH_TOKEN: ${{ secrets.GH_TOKEN }}` authenticate the agent with a PAT, whose
scopes are set on the token and not here.
"""

from __future__ import annotations

import argparse
import re
import shutil
import sys
import tempfile
from pathlib import Path

DEFAULT_WORKFLOWS_DIR = Path(".github/workflows")

KNOWN_PERMISSIONS = frozenset(
    {
        "actions",
        "attestations",
        "checks",
        "contents",
        "deployments",
        "discussions",
        "id-token",
        "issues",
        "models",
        "packages",
        "pages",
        "pull-requests",
        "repository-projects",
        "security-events",
        "statuses",
    }
)

LEVELS = frozenset({"read", "write", "none"})
BULK_LEVELS = frozenset({"read-all", "write-all"})
EMPTY_BLOCKS = frozenset({"{}", "{ }"})

# Scope -> the instruction that consumes it. Quotes are verbatim from the
# workflow so the citation survives line-number drift; a reviewer can grep for
# the phrase instead of trusting a line reference that moved.
ALLOWED: dict[str, dict[str, str]] = {
    "build.yml": {
        "contents": "read - checkout plus gradle; the job makes no GitHub API call",
    },
    "oc - code quality analyzer.yml": {
        "contents": "read - checkout and reads the tree",
        "pull-requests": "read - reads PRs while triaging what already exists",
        "issues": "write - opens and labels issues ('Label yang sesuai untuk kategori')",
    },
    "oc - release manager.yml": {
        "contents": "write - 'gh release create' uploads a tag and needs contents: write",
    },
    "oc- researcher.yml": {
        "contents": "read - repository research is read-only",
        "pull-requests": "write - 'add a clear comment to the existing issue or PR'",
        "issues": "write - 'Create non-duplicated, well-structured GitHub issues'",
    },
    "oc-issue-solver.yml": {
        "contents": "write - pushes the fix branch",
        "pull-requests": "write - opens the PR and closes the issue from its body",
        "issues": "write - 'Public plan comment on the issue' and the completion comment",
    },
    "oc-maintainer.yml": {
        "contents": "write - maintenance edits are pushed as branches",
        "pull-requests": "write - comments and labels on existing issues/PRs",
        "issues": "write - opens and comments on maintenance issues",
    },
    "oc-pr-handler.yml": {
        "contents": "write - pushes to the PR branch",
        "pull-requests": "write - reviews, resolves threads and merges the PR",
        "issues": "write - PR conversations are the issues API",
        "actions": "read - 'monitor the status of the branch's checks'",
    },
    "oc-problem-finder.yml": {
        "contents": "write - 'Commit perubahan, push lalu buat pr'",
        "pull-requests": "write - creates the PR",
        "issues": "write - creates labeled issues",
    },
    "oc-repo-manager.yml": {
        "contents": "write - keeps README and docs current",
        "pull-requests": "write - 'Menentukan apakah PR memenuhi syarat untuk merge otomatis'",
        "issues": "write - 'Mengidentifikasi dan menutup issue yang duplikat atau tidak relevan'",
    },
    "on-pull.yml": {
        "contents": "write - pushes the branch and merges it",
        "pull-requests": "write - 'gh pr merge --admin'",
        "actions": "read - 'Set to auto merge if check takes too long'",
    },
    "on-push.yml": {
        "contents": "write - pushes the branch and merges it",
        "pull-requests": "write - 'Merge PR if all checks are green'",
    },
    "parallel.yml": {
        "contents": "write - stages push fixes",
        "issues": "write - stages create and label issues",
        "pull-requests": "write - 'Merge Qualified PRs'",
    },
    "oc - template.md": {
        "contents": "read - read-only starting point for a new agent workflow",
        "pull-requests": "read - read-only starting point for a new agent workflow",
        "issues": "read - read-only starting point for a new agent workflow",
    },
    "workflow-permissions.yml": {
        "contents": "read - this guard only reads the repository",
    },
}

PERMISSIONS_LINE = re.compile(r"^(?P<indent>[ \t]*)permissions:[ \t]*(?P<value>.*?)[ \t]*$")
ENTRY_LINE = re.compile(r"^(?P<indent>[ \t]*)(?P<name>[A-Za-z][A-Za-z0-9_-]*):[ \t]*(?P<value>\S+)[ \t]*$")


def read_grants(path: Path) -> tuple[dict[str, str], list[str]]:
    """Return the scopes the file grants, plus any syntax this parser distrusts."""
    granted: dict[str, str] = {}
    errors: list[str] = []
    block_indent: int | None = None

    for lineno, line in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
        if not line.strip() or line.lstrip().startswith("#"):
            continue

        header = PERMISSIONS_LINE.match(line)
        if header:
            block_indent = len(header.group("indent"))
            inline = header.group("value")
            if inline in EMPTY_BLOCKS:
                block_indent = None
            elif inline:
                errors.append(
                    f"{path.name}:{lineno}: `permissions: {inline}` is shorthand this "
                    "guard cannot audit; list each scope on its own line"
                )
                block_indent = None
            continue

        if block_indent is None:
            continue

        entry = ENTRY_LINE.match(line)
        indent = len(line) - len(line.lstrip())
        if not entry or indent <= block_indent:
            block_indent = None
            continue

        name = entry.group("name")
        value = entry.group("value")
        if name not in KNOWN_PERMISSIONS:
            errors.append(f"{path.name}:{lineno}: `{name}` is not a GitHub permission name")
        elif value in BULK_LEVELS:
            errors.append(f"{path.name}:{lineno}: `{name}: {value}` grants everything; name each scope")
        elif value not in LEVELS:
            errors.append(f"{path.name}:{lineno}: `{name}: {value}` is not one of read/write/none")
        elif name in granted and granted[name] != value:
            errors.append(
                f"{path.name}:{lineno}: `{name}` is granted both `{granted[name]}` and `{value}`"
            )
        else:
            granted[name] = value

    return granted, errors


def cited_level(citation: str) -> str:
    return citation.split(" ", 1)[0]


def check(workflows_dir: Path) -> list[str]:
    problems: list[str] = []
    seen: set[str] = set()

    for path in sorted(workflows_dir.iterdir()):
        if not path.is_file() or path.suffix not in {".yml", ".md"}:
            continue
        seen.add(path.name)
        granted, errors = read_grants(path)
        problems.extend(errors)

        allowed = ALLOWED.get(path.name)
        if allowed is None:
            problems.append(
                f"{path.name}: no ALLOWED entry, so this guard cannot judge it; add "
                "the scopes it needs with the instruction that needs each one"
            )
            continue

        for name in sorted(set(allowed)):
            if cited_level(allowed[name]) not in LEVELS:
                problems.append(
                    f"ALLOWED: {path.name} `{name}` cites level "
                    f"`{cited_level(allowed[name])}`, which is not one of read/write/none"
                )

        for name in sorted(set(granted) - set(allowed)):
            problems.append(f"{path.name}: grants `{name}: {granted[name]}` with no recorded justification")
        for name in sorted(set(allowed) - set(granted)):
            problems.append(
                f"{path.name}: ALLOWED justifies `{name}` but the workflow does not grant it"
            )
        for name in sorted(set(granted) & set(allowed)):
            if granted[name] != cited_level(allowed[name]):
                problems.append(
                    f"{path.name}: `{name}` is `{granted[name]}` but ALLOWED expects "
                    f"`{cited_level(allowed[name])}`"
                )

    for name in sorted(set(ALLOWED) - seen):
        problems.append(f"ALLOWED justifies `{name}`, which is not in {workflows_dir}")

    return problems


def self_test(workflows_dir: Path) -> list[str]:
    """Prove the guard still fails, by injecting the grant it exists to catch."""
    failures: list[str] = []

    with tempfile.TemporaryDirectory() as tmp:
        copy = Path(tmp) / "workflows"
        shutil.copytree(workflows_dir, copy)
        if check(copy):
            failures.append("the unmutated workflows already fail the guard")

        for name in sorted(ALLOWED):
            source = copy / name
            if not source.exists():
                continue
            lines = source.read_text(encoding="utf-8").splitlines(keepends=True)
            mutated: list[str] = []
            injected = False
            for line in lines:
                mutated.append(line)
                header = PERMISSIONS_LINE.match(line)
                if not injected and header and not header.group("value"):
                    mutated.append(f"{header.group('indent')}  id-token: write\n")
                    injected = True
            if not injected:
                failures.append(f"self-test could not inject a grant into {name}")
                continue
            source.write_text("".join(mutated), encoding="utf-8")
            if not any("id-token" in problem for problem in check(copy)):
                failures.append(f"injecting `id-token: write` into {name} did not fail the guard")
            source.write_text("".join(lines), encoding="utf-8")

    return failures


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--workflows-dir", type=Path, default=DEFAULT_WORKFLOWS_DIR)
    parser.add_argument(
        "--self-test",
        action="store_true",
        help="inject an unjustified grant into every workflow and require the guard to reject each",
    )
    args = parser.parse_args(argv)

    if not args.workflows_dir.is_dir():
        print(f"{args.workflows_dir} is not a directory", file=sys.stderr)
        return 2

    problems = self_test(args.workflows_dir) if args.self_test else check(args.workflows_dir)
    for problem in problems:
        print(problem)

    if problems:
        print(f"\n{len(problems)} permission problem(s)", file=sys.stderr)
        return 1

    scope = "self-test" if args.self_test else "check"
    print(f"workflow-permissions {scope} OK: {len(ALLOWED)} files, every grant justified")
    return 0


if __name__ == "__main__":
    sys.exit(main())
