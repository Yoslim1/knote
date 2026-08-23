# Knote — Architecture

This document describes the **actual, current** architecture of the repository.
It is written from inspection of the source in `app/src/`, the Gradle
configuration, the exported Room schemas, and the test suite. Where something
cannot be verified from the repository it is explicitly labelled
**Not verified**; where something is intended but not implemented it is labelled
**Planned**.

Implementation is the source of truth. If this document and the code disagree,
the code wins and this document is wrong.

---

## System Overview

Knote is a single-module Android application (`:app`) — a private, fully
offline productivity app (calendar, reminders, tasks, habits, notes, journal,
mood, meditation, finance). It declares **no `INTERNET` permission** and
`android:allowBackup="false"` (`app/src/main/AndroidManifest.xml`); local
backup/restore is implemented as an encrypted, password-protected file
container. All sensitive state is stored in a single SQLCipher-encrypted
Room database (`knote.db`) plus an Android `SharedPreferences` file that holds
only *wrapped* key material.

### Layers

```
UI (Jetpack Compose, single activity)
  └─ ViewModels (viewmodel/)
       └─ AppRepository (repository/) + per-feature business logic
            └─ Room DAOs (data/)
                 └─ AppDatabase (Room over SQLCipher, version 5)
Security: KeyManager/KeyStorage/Argon2Kdf/SecurityGate (security/)
          provide the database encryption key (DEK) and gate the app start.
```

- **UI:** single `MainActivity` (Compose, Material 3, edge-to-edge). The app is
  gated behind `AppLockGate` → `LockScreen` until the database is unlocked
  (`MainActivity.kt`). Screens live in `ui/screens/`, shared widgets in
  `ui/components/`, dialogs in `ui/dialogs/`.
- **State:** per-feature `ViewModel`s (`viewmodel/`) expose state to Compose;
  there is no navigation library — navigation is state-driven within the
  scaffold (`KnoteApp.kt`, `AppScaffold.kt`).
- **Data:** `AppRepository` (`repository/AppRepository.kt`) is the single
  facade over the Room DAOs in `data/`. Feature-specific pure logic (finance
  projection, caffeine/sleep model, holidays, note markdown) lives in
  `finance/`, `science/`, `holidays/`, `ui/screens/` helper objects.
- **Persistence:** Room (`androidx.room:room-runtime/ktx`, compiler via KSP)
  over `net.zetetic:sqlcipher-android`. The DB is opened only with the
  correct DEK; there is no plaintext copy (see Data Layer).
- **Runtime boundaries:** a foreground service
  (`service/MeditationService.kt`, media playback type), two receivers
  (`notification/ReminderReceiver`, `notification/BootReceiver`), and the
  launcher activity-alias set for icon switching. `BootReceiver`
  reschedules exact alarms after `BOOT_COMPLETED` / `MY_PACKAGE_REPLACED` /
  `TIME_SET` / `TIMEZONE_CHANGED`.
- **Export:** PDF/HTML/ZIP exporters in `export/`; encrypted backup in
  `data/backup/`.

### Dependency direction

`ui` and `viewmodel` depend on `repository` and `data`; `data` depends on
`security` only for key helpers (`SqlCipherKey`, `wipe()`). `security` does not
depend on `ui`. `data/backup` depends on `security` for `Argon2Kdf`/`wipe()`.

---

## Application Structure

Module layout (`settings.gradle.kts`, `app/build.gradle.kts`):

- `:app` — the application (`namespace`/`applicationId` `com.dalelalmuslim.knote`).
- `:baselineprofile` — macro-benchmark baseline profile module
  (`targetProjectPath = ":app"`, `minSdk = 36`). Its generated profiles are
  consumed via `baselineProfile(project(":baselineprofile"))`.

Package map under `app/src/main/java/com/dalelalmuslim/knote/`:

| Package | Responsibility |
|---|---|
| `data/` | Room entities, DAOs, `AppDatabase`, `DatabaseProvider`, `PlaintextDbMigration`, `SqlCipherKey`, `backup/` (encrypted backup) |
| `repository/` | `AppRepository` — single data facade used by ViewModels |
| `security/` | `KeyManager`, `KeyStorage`, `KeyMode`, `Argon2Kdf`, `RecoveryCode`, `SecurityGate`, `SecurityController`, `SecureBytes` |
| `viewmodel/` | Per-feature ViewModels (notes, tasks, habits, finance, settings, backup, export, meditation, sleep/caffeine) |
| `ui/` | Compose UI: `screens/`, `components/`, `dialogs/`, `theme/`, `strings/` (localized app strings), `brand/`, `AppCurrency.kt` |
| `finance/`, `science/`, `holidays/` | Pure feature logic (expense projection, caffeine/sleep model, holiday calendars) |
| `notification/`, `service/` | Reminder scheduling/receivers and the meditation foreground service |
| `export/` | PDF / HTML / ZIP / recovery-code exporters |
| `icon/` | Launcher icon switching (`IconSwitcher`, activity aliases) |

Localization: `res/values*` contains the English resource contract and a complete Arabic resource set. The shipped set is intentionally limited to `en` and `ar`, enforced by `androidResources.localeFilters` and `res/xml/locales_config.xml`. The selected language is mirrored in a non-sensitive preference so the lock screen and Android 26–32 can apply it before the encrypted database is opened; the unlocked Compose tree also provides an explicit `LocalLayoutDirection`.


---

## Data Layer

- **Technology:** Room 2.8.4 with KSP; the driver is SQLCipher
  (`SupportOpenHelperFactory` + a raw-key byte array — `AppDatabase.kt:88-98`,
  `SqlCipherKey.kt`). Database file name is `knote.db`.
- **Entities (12):** `Task`, `Note`, `Expense`, `Category`, `AppSettings`,
  `Habit`, `HabitLog`, `GratitudeEntry`, `MoodEntry`, `CaffeineDose`,
  `RecurringCostHistory`, `AdditionalIncome` (`AppDatabase.kt:15-16`). Type
  converters in `data/Converters.kt`.
- **DAOs:** one per entity in `data/*Dao.kt`, plus `BackupDao` for
  whole-database export/import. Exposed through `AppRepository`; ViewModels
  never touch DAOs directly.
- **Persistence flow:** `SecurityGate.prepare()` unwraps the DEK (or
  initializes a new one), runs the one-time plaintext→encrypted migration if a
  legacy plaintext DB exists, then `DatabaseProvider.open()` builds and caches
  the single `AppDatabase` instance (`DatabaseProvider.kt`). All reads/writes go
  through Room; sensitive data never exists outside the encrypted DB.
- **Migration mechanism:** four explicit `Migration` objects
  (`MIGRATION_1_2 … MIGRATION_4_5`) registered via `addMigrations` using the
  Room `androidx.sqlite.SQLiteConnection` API. `exportSchema = true`; exported
  schemas are committed under
  `app/schemas/com.dalelalmuslim.knote.data.AppDatabase/{1..5}.json`.
  There is **no** destructive fallback. See `MIGRATION_POLICY.md`.
- **Schema management:** KSP arg `room.schemaLocation = "$projectDir/schemas"`
  (`app/build.gradle.kts:84-86`); schema files are part of the repository and
  must be updated with any schema change.
- **Plaintext→encrypted upgrade:** `PlaintextDbMigration` detects a legacy
  plaintext `knote.db` and re-exports it into an encrypted DB via
  `sqlcipher_export`, preserving `PRAGMA user_version` so Room migrations
  continue from the same version (`data/PlaintextDbMigration.kt`). Covered by
  `androidTest/.../data/PlaintextMigrationInstrumentedTest.kt`.

---

## Security Architecture

Only what actually exists is documented here; no credentials or test secrets
are repeated. The DEK (32 random bytes) encrypts the database via SQLCipher
(AES-256). The DEK is wrapped ("KEK") and the wrapper is stored in
`SharedPreferences` `knote_keys` (`security/KeyStorage.kt`) — key material is
never stored in plaintext.

- **Key modes (`security/KeyMode.kt`):**
  - `KEYSTORE_NO_LOCK` — DEK wrapped with a Keystore AES-256-GCM key; no user
    interaction.
  - `KEYSTORE_LOCK` — DEK wrapped with a Keystore RSA-2048 key
    (OAEP/SHA-256), `setUserAuthenticationRequired(true)` with
    `BIOMETRIC_STRONG | DEVICE_CREDENTIAL` (API 30+) / validity -1 (API <30).
  - `PASSPHRASE` — DEK wrapped with a KEK derived from the passphrase.
- **Key derivation (`security/Argon2Kdf.kt`):** Argon2id, 64 MiB, 3 iterations,
  parallelism 1, 32-byte hash, 16-byte salt.
- **Recovery code (`security/RecoveryCode.kt`):** device-generated 25-symbol
  Crockford Base32 code (125 bits entropy); wraps the same DEK a second,
  independent time (Argon2id + AES-256-GCM). Provisioned automatically whenever
  a lock is enabled. Input is normalized (case, `I/L→1`, `O→0`, grouping
  separators) before derivation.
- **AndroidKeyStore / StrongBox:** no-lock and lock keys are generated in the
  AndroidKeyStore; `setIsStrongBoxBacked(true)` is attempted on API 28+, with a
  graceful fallback on `StrongBoxUnavailableException`
  (`security/KeyManager.kt:257-325`). MGF1 digest differs by API level
  (SHA-256 on API 35+, SHA-1 below) to match what the generated key authorizes.
- **Unlock flows:** biometric unlock passes a Keystore-initialized RSA cipher to
  `KeyManager.unlockWithKeystore()`; passphrase unlock derives the KEK and
  unwraps with AES-GCM. Wrong credentials surface as
  `WrongPassphraseException` / `WrongRecoveryCodeException`.
  `KeyPermanentlyInvalidatedException` (new biometrics enrolled) maps to
  `KeyInvalidatedException` — the user is routed to the recovery code.
- **App start / relock (`security/SecurityGate.kt`):** `prepare()` returns a
  `StartGate` (`UNLOCKED` / `NEEDS_BIOMETRIC` / `NEEDS_PASSPHRASE`); the
  `LockScreen` then authenticates. A configurable inactivity timeout relocks the
  app on background, with a 60 s grace window for in-flight system activities
  (BiometricPrompt device-credential, document pickers). `resetAndReinitialize`
  wipes keys and the database, then recreates from scratch (destructive reset
  is a deliberate, user-confirmed action).
- **Memory hygiene:** `SecureBytes.kt` provides `wipe()` for `ByteArray` /
  `CharArray`; passphrases, KEKs, and raw DEKs are wiped after use throughout
  the security and backup paths.
- **At-rest extras:** encrypted backups (`data/backup/BackupCodec.kt`):
  `APPBK` container, AES-256-GCM (128-bit tag) over gzip, Argon2id-derived key;
  format version 1 with bounded parameter validation on decrypt.
  `FLAG_SECURE` screenshot blocking is a user setting (`MainActivity.kt:154-160`).
- **Platform config:** `res/xml/data_extraction_rules.xml` /
  `backup_rules.xml` exclude `knote.db` and `knote_keys.xml` from Android
  auto-backup; `allowBackup="false"` is the hard guarantee.

---

## Testing Architecture

Two clearly separated layers:

- **`app/src/test/` (JVM unit tests).** Pure-JVM logic that does not need the
  Android framework or native libraries: `CaffeineModelTest`, finance
  `ExpenseProjectionTest`, `holidays/*`, `security/KeyModeTest`,
  `RecoveryCodeTest`, `security/Argon2KdfUnitTest` and `KeyManagerUnitTest`
  (the latter two assert **only** the JVM-safe contracts; derivation and
  key-wrap behavior is gated/skipped or left to instrumented tests, since the
  argon2kt native lib and the AndroidKeyStore do not exist on the desktop JVM),
  `data/backup/BackupHeaderTest` and `BackupSerializationTest`, `ui/AppCurrencyTest`,
  and the note markdown/checklist/tag/timestamp tests under `ui/screens/`.
- **`app/src/androidTest/` (instrumented tests).** Android framework /
  native-dependent behavior: `data/DatabaseEncryptionTest`,
  `data/PlaintextMigrationInstrumentedTest`, `data/backup/BackupCodecInstrumentedTest`,
  `export/HtmlToPdfRendererInstrumentedTest`, `security/KeyManagerInstrumentedTest`
  (real AndroidKeyStore + Argon2 + SharedPreferences), and
  `screenshot/SeedNotesForScreenshot` (screenshot seeding).
- **Rule:** JVM-testable behavior goes in `src/test`; anything touching the
  Android runtime (Keystore, SQLCipher/Argon2 native libs, real `Context`,
  Room) goes in `src/androidTest`. There are **no** Room `MigrationTestHelper`
  migration tests today (see `MIGRATION_POLICY.md`).

---

## Build & CI

- **Modules/tasks:** standard Gradle Android tasks from `com.android.application`
  (AGP 9.2.1) apply to `:app`. Build commands documented in the README are
  `./gradlew :app:assembleRelease` and `./gradlew :app:assembleDebug`.
  Signing is read from `keystore.properties` when present
  (`app/build.gradle.kts:17-20,38-47,57-60`); `keystore.properties.example` is
  committed, real secrets are not.
- **Config:** Java/Kotlin 11 toolchain, JVM args `-Xmx2048m`,
  configuration-cache enabled, Kotlin `official` code style
  (`gradle.properties`). Versions centralized in `gradle/libs.versions.toml`.
- **Legal/license data:** custom tasks `collectLegalNotices`,
  `collectCopyrightYears`, `regenerateLegalData` regenerate
  `app/src/main/res/raw/legal_notices.json` and `copyright_years.json` from the
  resolved classpath.
- **CI:** `.github/workflows/ci.yml` runs JVM unit tests, Android lint, and a debug build. `release.yml` builds and signs release APKs from version tags, verifies the signature, and publishes checksums. `update-lint-baseline.yml` is a manual maintenance workflow.


---

## Architectural Invariants

Rules that must remain true for any change:

1. The database must remain SQLCipher-encrypted; encrypted data must not be
   persisted as plaintext (the only plaintext→encrypted path is the legacy
   one-time `PlaintextDbMigration`).
2. Key material (DEK, KEKs, recovery codes, passphrases) must never be logged
   or committed, and must be wiped from memory after use.
3. Every database schema change requires an explicit, reviewed Room migration;
   a missing migration must fail loudly rather than fall back to data deletion.
4. Security-sensitive operations must fail closed (wrong passphrase/recovery
   code → denied; invalidated keystore key → recovery flow, never silent
   unlock).
5. The `:app` module must never obtain network permission or telemetry/analytics
   dependencies.
6. Sensitive files (`knote.db`, `knote_keys`) must stay excluded from Android
   auto-backup.

---

## Known Constraints

- `minSdk = 26` forces API-level guards in security code (e.g. biometric
  auth-parameter handling below API 30, StrongBox/`setIsStrongBoxBacked` below
  API 28, MGF1 digest selection) — see `KeyManager.kt` and commit history.
- Argon2 (`argon2kt`) and SQLCipher native libraries are Android-only; JVM unit
  tests cannot exercise them (they are skipped via `Assume` or covered by
  instrumented tests).
- The database is versioned and schemas are exported; changing entities without
  bumping the version and adding a migration will break the Room validation.
- Recovery codes are single-usage in the sense that a new code invalidates the
  previous one when regenerated.
- `baselineprofile` runs only on a connected device/emulator at API 36.
