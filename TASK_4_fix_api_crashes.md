# Task: Fix two API-level crash risks found during minSdk 26 lowering

## Context
A previous task lowered minSdk from 33 to 26 and fixed one API-30 call.
During its Step 3 audit it found two more real, unguarded API-level calls
that will crash the app on the lower API levels we now claim to support.
This task fixes both. Do not touch anything else.

## Fix 1 - AlarmManager.canScheduleExactAlarms() is API 31+

File: app/src/main/java/com/dalelalmuslim/knote/notification/ReminderScheduler.kt
(around line 81, inside sync()/schedule() - read the surrounding
function fully before editing).

canScheduleExactAlarms() does not exist before API 31 (Android 12) and
will throw NoSuchMethodError if called on API 26-30. On those older API
levels, exact-alarm scheduling permission does not exist as a concept -
apps could always schedule exact alarms freely (the runtime permission
system for this was introduced in API 31).

Guard the call and the logic that depends on its result:

val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    alarmManager.canScheduleExactAlarms()
} else {
    // Exact-alarm scheduling permission didn't exist before API 31;
    // it was always implicitly allowed.
    true
}

Then use canScheduleExact wherever the original code used the direct
method call result. Read the full function first to see exactly how the
result is currently branched on (e.g. whether it falls back to
setExactAndAllowWhileIdle vs setAndAllowWhileIdle, or shows a
permission prompt) and preserve that same branching logic, just fed by
the guarded variable instead of the raw unguarded call.

## Fix 2 - setIsStrongBoxBacked() is API 28+

File: app/src/main/java/com/dalelalmuslim/knote/security/KeyManager.kt
(two call sites: the no-lock key spec around line 267, and the lock key
spec around line 310 - both inside .apply { if (strongBox) setIsStrongBoxBacked(true) }).

The existing pattern relies on catching StrongBoxUnavailableException
to fall back to a non-StrongBox key:

try {
    generator.initialize(spec(strongBox = true)); generator.generateKeyPair()
} catch (e: StrongBoxUnavailableException) {
    generator.initialize(spec(strongBox = false)); generator.generateKeyPair()
}

This catch does NOT protect against API 26/27, because on those API
levels setIsStrongBoxBacked() doesn't exist as a method at all -
calling it throws NoSuchMethodError (a LinkageError), which is not a
subtype of StrongBoxUnavailableException and will crash uncaught.

Fix by never attempting the StrongBox path at all below API 28, at both
call sites that build spec(strongBox = true):

try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        generator.initialize(spec(strongBox = true)); generator.generateKeyPair()
    } else {
        generator.initialize(spec(strongBox = false)); generator.generateKeyPair()
    }
} catch (e: StrongBoxUnavailableException) {
    generator.initialize(spec(strongBox = false)); generator.generateKeyPair()
}

Apply the same pattern to both call sites (no-lock key generation and
lock key generation) - find both by searching for spec(strongBox = true)
in this file.

## Constraints
- Do not run ./gradlew anything - cannot build locally on this device.
- Do not modify any file other than ReminderScheduler.kt and
  KeyManager.kt.
- Do not change any other logic in either file beyond what's described
  above.
- Show a diff of exactly what changed before finishing.

## Done when
- canScheduleExactAlarms() in ReminderScheduler.kt only executes
  inside an if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) branch.
- setIsStrongBoxBacked(true) is only ever reached at runtime when
  Build.VERSION.SDK_INT >= Build.VERSION_CODES.P, at both call sites in
  KeyManager.kt.
- A diff of both changed files is shown.
- Confirm no other unguarded API 27+ calls were introduced by this fix.
