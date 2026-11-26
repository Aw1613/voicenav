# AGENTS.md: Collaboration Rules for AntiGravity Agents

These rules apply to **every** AntiGravity agent working in this repository. Read them before making any change. If a rule conflicts with a task instruction, the rule wins. Stop and ask a human maintainer instead of working around it.

---

## 1. Non-Negotiable Rules

1. **Never push directly to `main`** (or `master`, `develop`, `release/*`, or any protected branch). All changes go through a branch and a Pull Request.
2. **Never force-push** (`--force`, `--force-with-lease`) to a shared or protected branch.
3. **Never rewrite shared history** (no rebasing or amending commits that are already pushed and used by others).
4. **Never bypass checks.** Do not use `--no-verify`, disable CI, skip tests, or weaken linters to make a change pass.
5. **Never commit secrets**: API keys, tokens, passwords, `.env` files, private keys, or credentials of any kind.
6. **Never merge your own PR.** A human maintainer (or the designated reviewer) merges.
7. **Never run destructive commands** (`rm -rf`, `git reset --hard` on shared work, dropping databases, deleting branches you didn't create) without explicit human approval.
8. **Do not violate any rule in this file.** If you are unsure whether an action is allowed, treat it as not allowed and ask.

---

## 2. Branching Workflow

Always start from an up-to-date `main`:

```bash
git checkout main
git pull origin main
git checkout -b <branch-name>
```

### Branch naming convention

Format: `<type>/<issue-id>-<short-kebab-description>`

| Type        | Use for                                   | Example                              |
|-------------|-------------------------------------------|--------------------------------------|
| `fix/`      | Bug fixes                                 | `fix/142-null-pointer-on-login`      |
| `feat/`     | New features                              | `feat/87-add-export-to-csv`          |
| `hotfix/`   | Urgent production fixes                   | `hotfix/203-payment-timeout`         |
| `refactor/` | Code restructuring, no behavior change    | `refactor/115-split-auth-module`     |
| `docs/`     | Documentation only                        | `docs/60-update-readme`              |
| `test/`     | Adding or fixing tests                    | `test/99-cover-parser-edge-cases`    |
| `chore/`    | Tooling, dependencies, config             | `chore/71-bump-eslint`               |

Rules:
- **Bug fixes must always use a `fix/` branch.** No exceptions.
- Use lowercase, hyphens only, no spaces or special characters.
- Include the issue/ticket number when one exists. If none exists, omit it (`fix/null-pointer-on-login`).
- Keep the description under ~5 words.
- **One branch = one purpose.** Do not mix a bug fix with unrelated refactors or features.

---

## 3. Bug Fix Procedure

1. Confirm the bug: reproduce it or write a failing test first.
2. Create a `fix/...` branch from the latest `main`.
3. Make the **smallest change** that fixes the root cause (not just the symptom).
4. Add or update a test that fails before the fix and passes after.
5. Run the full test suite and linters locally.
6. Commit using the format below, push the branch, and open a PR.
7. Link the issue in the PR (`Fixes #142`).

---

## 4. Commit Guidelines

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short summary in imperative mood>

<optional body: what and why, not how>

<optional footer: Fixes #123>
```

Examples:
```
fix(auth): handle null user object on login
feat(export): add CSV download for reports
docs(readme): clarify setup steps
```

Rules:
- Keep commits small, focused, and atomic.
- Summary line max 72 characters, imperative mood ("add", not "added").
- No "WIP", "misc", "fix stuff", or empty messages.
- Do not commit generated files, build artifacts, or unrelated formatting changes.

---

## 5. Pull Request Requirements

Every PR must:
- Target `main` (or the branch specified by a maintainer).
- Have a clear title following the commit convention.
- Include: **what** changed, **why**, **how it was tested**, and linked issue(s).
- Pass all CI checks (build, tests, lint, type checks).
- Be small and reviewable. If it grows large, split it.
- Be marked **Draft** until it is ready for review.
- Be reviewed and approved by at least one human before merge.

Do not request merge, auto-merge, or bypass review.

---

## 6. Code Quality Standards

- Follow the existing code style, naming, and project structure. Match what is already there.
- Run the formatter and linter before every commit.
- Write or update tests for every behavior change.
- Do not reduce test coverage.
- Do not leave dead code, debug prints, commented-out blocks, or unresolved `TODO`s without an issue reference.
- Do not add new dependencies without justification in the PR description.
- Prefer clarity over cleverness. Comment the *why*, not the *what*.

---

## 7. Scope and Safety Boundaries

- Only modify files relevant to the assigned task.
- Do **not** edit these without explicit human approval:
  - CI/CD configs (`.github/workflows/`, pipelines)
  - Branch protection or repository settings
  - Lockfiles, unless the task is a dependency change
  - Security, auth, or payment-critical code paths (flag for extra review)
  - This `AGENTS.md` file
- Do not access, copy, or exfiltrate data outside the repository.
- Do not make network calls or install packages not required by the task.

---

## 8. Multi-Agent Coordination

- **Check before you start:** look for existing branches/PRs touching the same files or issue. Do not duplicate work.
- **One agent per branch.** Do not push to a branch another agent or person owns. Open a PR against their branch or ask first.
- **Claim work** by commenting on the issue or opening a Draft PR early.
- **Avoid overlapping edits.** If two tasks need the same file, coordinate sequentially and rebase onto `main` before opening the PR.
- **Resolve conflicts carefully.** Never discard another contributor's changes to make a conflict go away. If the resolution is non-trivial, ask a human.
- Leave clear notes in the PR description so other agents and humans can continue the work.

---

## 9. When to Stop and Ask a Human

Stop and request guidance if:
- The task requires pushing to a protected branch or bypassing any rule above.
- Requirements are ambiguous, contradictory, or the root cause is unclear.
- Tests fail and the fix would mean weakening or deleting them.
- A change touches security, auth, payments, data migrations, or infrastructure.
- You discover leaked secrets or a vulnerability (report it; do not publish details in a public PR).
- Another contributor's work conflicts with yours.

Never guess on destructive or irreversible actions.

---

## 10. Pre-Push Checklist

- [ ] I am on a correctly named branch, **not** `main`
- [ ] Bug fixes are on a `fix/...` branch
- [ ] Branch is based on the latest `main`
- [ ] Changes are scoped to a single purpose
- [ ] Tests added or updated, and the full suite passes
- [ ] Linter and formatter pass
- [ ] No secrets, debug code, or unrelated changes included
- [ ] Commit messages follow Conventional Commits
- [ ] PR description is complete and the issue is linked
