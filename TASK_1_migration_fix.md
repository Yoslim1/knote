# Task: Remove silent destructive-migration fallback

## Context
File: app/src/main/java/com/dalelalmuslim/knote/data/AppDatabase.kt

The build() function currently configures Room with:

.fallbackToDestructiveMigration(dropAllTables = true)
.fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)

This means: if a future schema change ships without a matching Migration
object, or if a user somehow ends up on a downgrade path, Room will
silently drop every table and wipe all user data with no warning, no
confirmation, and no error surfaced to the user. This is unacceptable for
an app whose entire value proposition is that user data never leaves the
device and is never lost.

## What to change

1. Remove both fallbackToDestructiveMigration(dropAllTables = true) and
   fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true) from
   the Room.databaseBuilder(...) chain in AppDatabase.build().
2. Do NOT replace them with anything that also silently deletes data.
   The correct behavior is: if Room cannot find a migration path, it
   should throw (IllegalStateException is Room's default behavior once
   the destructive fallback is removed). This is intentional - a crash
   during development is preferable to silent data loss in production,
   because it forces whoever ships the next schema change to write an
   explicit, tested Migration object (following the existing pattern of
   MIGRATION_1_2 through MIGRATION_4_5 in the same file - each one has
   a short comment explaining why the change is safe).
3. Add a comment directly above the .addMigrations(...) line explaining
   this policy, e.g.: "No destructive fallback, intentionally - a missing
   migration must fail loudly during development, not silently delete
   user data in production. Every schema bump requires a hand-written,
   tested Migration object."
4. Do not touch the existing MIGRATION_1_2 ... MIGRATION_4_5 objects
   themselves. Do not change the schema version number. This task is only
   about removing the two destructive-fallback lines and documenting why.

## Constraints
- Do not run ./gradlew anything - this device cannot run Android
  Gradle builds. Verify your change by re-reading the file after editing
  and confirming the two fallback lines are gone and nothing else in the
  file changed except the added comment.
- Do not modify any other file.
- Show a diff of exactly what changed before finishing.

## Done when
- grep -n "fallbackToDestructiveMigration" app/src/main/java/com/dalelalmuslim/knote/data/AppDatabase.kt
  returns no results.
- The file still compiles logically (all braces/parens balanced - you
  cannot build to verify, so read the full function back carefully after
  editing).
- A one-paragraph summary of the change is printed at the end.
