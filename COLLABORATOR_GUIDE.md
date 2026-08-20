# Knote — Collaborator & Agent Guide

Rules for human contributors and AI coding agents (OpenCode, other agents)
working on this repository. **Read `ARCHITECTURE.md` and `MIGRATION_POLICY.md`
first.** When documentation and code disagree, the code is the source of truth.

---

## Before Making Changes

Before editing anything, an agent MUST:

1. Inspect the repository layout (`README.md`, `ARCHITECTURE.md`,
   `MIGRATION_POLICY.md`, `PROJECT_CONTEXT.md`, `ROADMAP.md`).
2. Inspect the relevant production code in `app/src/main/`.
3. Inspect the relevant tests in `app/src/test/` and `app/src/androidTest/`.
4. Inspect the current Git state (`git status --short`, `git log --oneline -5`).
5. Identify the **smallest** change that satisfies the task, and its allowed
   files.

---

## Never Guess

Do not invent APIs, classes, methods, dependencies, database schemas,
migration versions, architecture, or configuration values.

- If something is unclear: **Inspect → verify → implement**. Never fabricate a
  fact to make the work complete.
- Verify version numbers against `gradle/libs.versions.toml`, SDK values against
  `app/build.gradle.kts`, and database versions against `AppDatabase.kt`.
- If a claim cannot be verified, label it `Not verified`; if something is
  intended but absent, label it `Planned`. Never state a guess as a current
  fact.

---

## Scope Control

Every task must define:

- allowed files,
- forbidden files,
- expected behavior,
- validation method.

Do **not** refactor unrelated code. Do **not** perform opportunistic cleanup of
code you are not asked to touch. A task that touches `app/src/main/**` needs an
explicit reason in its brief; documentation tasks typically do not.

---

## Security Rules

Knote is a security-sensitive app (SQLCipher database, AndroidKeyStore DEKs,
Argon2, biometrics). For any change:

- **No secrets in source:** never commit real passwords, keystore material,
  recovery codes, or test secrets. `keystore.properties` is git-ignored; only
  the `.example` is committed.
- **No secret logging:** never log key material, wrapped DEKs, passphrases, or
  recovery codes.
- **No plaintext sensitive data:** never introduce a plaintext persistence path
  for encrypted data.
- **Validate security boundaries:** unlocking/relocking, keystore invalidation
  (`KeyInvalidatedException`), and recovery flows must keep their
  fail-closed behavior.
- **Preserve authentication/authorization assumptions:** do not weaken
  `setUserAuthenticationRequired`, biometric authenticator flags, or passphrase
  checks.
- **Treat cryptographic code as high-risk:** any change to `security/`,
  `data/SqlCipherKey.kt`, `data/backup/`, or key-wrapping logic must be
  reviewed and tested at the correct layer.
- **Do not weaken encryption "for testing":** never add shortcuts that skip
  key derivation or encryption.

---

## Database Rules

Full policy: `MIGRATION_POLICY.md`. Summary:

- Never modify a Room entity/schema without an explicit, reviewed migration.
- Never bump `AppDatabase` version without adding and registering the matching
  `Migration` and updating the exported schema files under
  `app/schemas/com.dalelalmuslim.knote.data.AppDatabase/`.
- Destructive migration fallback is **not** part of the supported strategy; do
  not introduce it as a shortcut.
- Migrations must preserve existing user data; if you cannot prove that, you
  have not finished the migration.
- Remember the legacy plaintext→encrypted path (`PlaintextDbMigration`): schema
  history starts from the legacy `PRAGMA user_version`, which the migration
  preserves.

---

## Testing Rules

- `app/src/test` → JVM / unit-testable behavior (no Android framework or
  native libs).
- `app/src/androidTest` → Android framework / integration behavior (Keystore,
  SQLCipher, Argon2 native, Room, real `Context`).
- Place a test in the layer that actually exercises the code; do not create
  unrealistic mocks merely to move coverage numbers.
- When a JVM test cannot run because a native/Android dependency is absent,
  gate it with a JUnit `Assume` (as `Argon2KdfUnitTest` does) rather than
  stubbing the dependency.
- For database/migration changes, write migration tests that create the old
  schema, seed data, run the migration, and verify data + schema.

---

## Git Rules

Before committing:

```sh
git status --short
git diff --check
git diff
```

Review the complete final diff. Then:

- Stage **only** the files you intentionally changed.
- Never revert unrelated user changes; never stage them.
- Never commit secrets or generated artifacts unless explicitly required.
- If unrelated working-tree changes exist, leave them alone and report them.

## Commit Rules

- One commit per logical change; conventional messages, matching the repo
  style:
  - `docs: update architecture documentation`
  - `test: add KeyManager coverage`
  - `fix: correct database migration`
- Do not create a commit containing unrelated changes.
- Do not amend commits you did not create for this change; do not use
  `--no-verify`.

---

## Agent Completion Report

After every significant task, report:

- files changed,
- behavior changed (or "none"),
- tests added/updated,
- validation performed,
- known limitations / unverified claims.

---

## Non-Negotiable Repository Facts

- No `INTERNET` permission; no analytics/ads/telemetry dependencies.
- `allowBackup = false`; `knote.db` and `knote_keys` excluded from auto-backup.
- `minSdk 26` / `targetSdk 37` / `compileSdk 37`; Kotlin 2.2.10; Room 2.8.4.
- Database name `knote.db`, Room version `5`.
- Single activity + Compose; ViewModels over `AppRepository` over DAOs.
