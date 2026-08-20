# Knote — Database Migration Policy

This policy reflects the **actual** Room/SQLCipher implementation in this
repository (verified by inspection of `app/src/main/java/com/dalelalmuslim/knote/data/`,
the exported schemas, and the test suite).

---

## Current Database Architecture (verified facts)

| Item | Value |
|---|---|
| Database class | `com.dalelalmuslim.knote.data.AppDatabase` (`AppDatabase.kt`) |
| Database file | `knote.db` (SQLCipher-encrypted) |
| Room version | **5** (`@Database(version = 5)`) |
| Schema export | `exportSchema = true`, KSP `room.schemaLocation = "$projectDir/schemas"` |
| Exported schemas | `app/schemas/com.dalelalmuslim.knote.data.AppDatabase/{1,2,3,4,5}.json` |
| Entities | 12: `Task`, `Note`, `Expense`, `Category`, `AppSettings`, `Habit`, `HabitLog`, `GratitudeEntry`, `MoodEntry`, `CaffeineDose`, `RecurringCostHistory`, `AdditionalIncome` |
| Migration objects | `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`, `MIGRATION_4_5` (private, in `AppDatabase.Companion`) |
| Registration | `.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)` (`AppDatabase.build`) |
| Destructive fallback | **Not present.** `fallbackToDestructiveMigration` / `OnDowngrade` are not called anywhere in production code |
| Migration tests | **None** with Room `MigrationTestHelper` today. Instrumented tests cover DB encryption and the plaintext→encrypted upgrade only |
| Legacy upgrade | `PlaintextDbMigration` re-exports a legacy plaintext `knote.db` into the encrypted DB via `sqlcipher_export`, preserving `PRAGMA user_version` |

Migration SQL uses the Room `androidx.sqlite.SQLiteConnection` API
(`connection.execSQL` / `connection.prepare`) — `AppDatabase.kt:41-86`.

---

## Core Principle

Every production database schema change MUST have an explicit, reviewed
`Migration` object that preserves existing user data.

A missing migration must **fail loudly**, not silently delete user data.
Destructive fallback is intentionally NOT configured and MUST NOT be introduced
as a shortcut (see commit history: *"fix: remove silent destructive migration
fallback"*).

---

## Required Process

For any schema/model change:

1. Modify the entity/model (e.g. `data/*.kt`).
2. Increment `@Database(version = ...)` in `AppDatabase.kt`.
3. Create an explicit `Migration(oldVersion, newVersion)` implementing the
   schema change (prefer `ALTER TABLE`; add/drop columns exactly, never
   re-create tables and copy rows unless unavoidable).
4. Register it in `.addMigrations(...)` in `AppDatabase.build`.
5. Update the exported schema: rebuild so KSP regenerates the new
   `app/schemas/.../{version}.json`, and commit it.
6. Add/update migration tests (see Migration Testing below).
7. Verify existing user data is preserved across the migration.
8. Review the migration SQL carefully — it runs against real user data in
   production.

---

## Migration Safety

Migrations must:

- preserve existing user data and valid rows,
- handle rows that already exist (e.g. add columns with `DEFAULT` values that
  are valid for existing rows, never `NOT NULL` without a default),
- maintain constraints and foreign-key behavior the schema declares,
- preserve indexes where the schema requires them,
- avoid accidental data loss or truncation,
- be deterministic (same input → same schema, independent of device state),
- be reviewed before release.

Guard unexpected legacy states defensively: e.g. `MIGRATION_2_3` drops a column
only if it actually exists (`pragma_table_info` check) because the column only
exists on databases that passed through an unreleased intermediate version
(`AppDatabase.kt:55-64`).

---

## Destructive Migration

- `fallbackToDestructiveMigration` is **not configured** in this project; it is
  not part of the supported migration strategy.
- The only destructive operation in the codebase is `SecurityGate.resetAndReinitialize`
  (`SecurityGate.kt:148-157`), a **deliberate, user-confirmed full data reset**
  (wipe keys + delete DB + reinitialize), not a migration path.
- Do not add a destructive fallback to fix a missing migration. The correct fix
  is writing the missing `Migration`.

---

## Migration Testing

**Current state:** there are no Room `MigrationTestHelper`-based migration tests
in `app/src/androidTest/` today. `DatabaseEncryptionTest` covers opening the
encrypted DB with the correct/incorrect DEK; `PlaintextMigrationInstrumentedTest`
covers the legacy plaintext→encrypted upgrade.

**Required strategy** (for every future schema bump):

1. Create the **old** schema — ideally from the committed export
   (`app/schemas/.../{previousVersion}.json`) using
   `androidx.room.testing.MigrationTestHelper`.
2. Insert representative data into the old schema.
3. Run the migration (open the DB with `AppDatabase` at the new version).
4. Verify the migrated database opens and data integrity is preserved.
5. Verify schema expectations against the new exported schema.

Do not claim a migration test exists unless it actually exists.

---

## Legacy Plaintext→Encrypted Upgrade

`PlaintextDbMigration` (`data/PlaintextDbMigration.kt`) is a one-time, pre-Room
upgrade that runs on first initialization (`SecurityGate.prepare`). It detects a
plaintext SQLite `knote.db`, re-exports it with SQLCipher
(`sqlcipher_export`), preserves `PRAGMA user_version`, and moves sidecar files
(`-wal/-shm/-journal`) over. It is a migration of *storage format*, not a Room
schema migration — Room versioning continues from the preserved `user_version`.
Covered by `PlaintextMigrationInstrumentedTest`.

---

## Version History

| From | To | Change |
|---|---|---|
| 1 | 2 | `notes.color` (INTEGER NOT NULL DEFAULT 0); `app_settings.newNoteStartsWithTitle` |
| 2 | 3 | drop `app_settings.noteTypeFilterEnabled` (only if present) |
| 3 | 4 | `app_settings.mindfulnessHintSeen` (INTEGER NOT NULL DEFAULT 1) |
| 4 | 5 | `app_settings.mindfulnessEnabled` (INTEGER NOT NULL DEFAULT 1) |

Any new version must follow the Required Process above.
