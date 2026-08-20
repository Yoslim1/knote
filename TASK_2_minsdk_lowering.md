# Task: Lower minSdk from Android 13 (API 33) to Android 8.0 (API 26)

## Context
File: app/build.gradle.kts currently has:

minSdk = 33

The goal is to support devices back to Android 8.0 (API 26). This is not a
one-line change - one specific security-critical call in the codebase
requires API 30 and must be made backward-compatible first, or the app
will crash on Android 8/9/10 devices the moment a user tries to enable
biometric/device-credential lock.

## Step 1 - Change the minSdk value

In app/build.gradle.kts, change:
minSdk = 33
to:
minSdk = 26
Do not change targetSdk or compileSdk. Do not change anything else in
this file.

## Step 2 - Fix the API 30 gate in KeyManager.kt

File: app/src/main/java/com/dalelalmuslim/knote/security/KeyManager.kt

Inside the private getOrCreateLockKey() function, this call is
unconditional:

.setUserAuthenticationRequired(true)
.setUserAuthenticationParameters(
    0,
    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL,
)

setUserAuthenticationParameters(timeout, type) requires API 30. On API
26-29 it does not exist and will throw NoSuchMethodError at runtime the
first time getOrCreateLockKey() runs (i.e. the first time a user picks
the biometric/device-credential lock mode).

Wrap it in a version check, using the pre-API-30 equivalent
(setUserAuthenticationValidityDurationSeconds) as the fallback, matching
the existing style already used a few lines below for the MGF1 digest
(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)):

.setUserAuthenticationRequired(true)
.apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        setUserAuthenticationParameters(
            0,
            KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL,
        )
    } else {
        // Pre-API-30: no combined biometric+credential auth type constant
        // exists yet. -1 requires authentication for every single use,
        // which is the closest equivalent to "auth required, no grace
        // period" on these older API levels.
        @Suppress("DEPRECATION")
        setUserAuthenticationValidityDurationSeconds(-1)
    }
}

(Build.VERSION_CODES.R is API 30 - use that named constant, not the
literal 30, to match the existing code style in this file.)

## Step 3 - Search for any other API 30+ calls you missed

Run a search across the whole security/, data/, and notification/
packages for any other Keystore, biometric, or notification API that
might have a minSdk floor above 26. The only two version-gated call sites
that existed before this task were both already guarded for API 35
(VANILLA_ICE_CREAM) - leave those exactly as they are, they don't need
changes. Report anything else you find that looks like it needs a similar
guard, but do not change it without listing it in your final summary
first - this task's required scope is only the one call site in Step 2.

## Constraints
- Do not run ./gradlew anything - this device cannot run Android Gradle
  builds locally. The actual build verification happens in GitHub
  Actions after this is pushed.
- Do not touch StrongBox-related code (StrongBoxUnavailableException
  handling) - it's already correctly guarded with try/catch and works
  fine down to API 26 (StrongBox itself just won't be available below
  API 28, which the existing catch block already handles).
- Do not modify any file other than app/build.gradle.kts and
  KeyManager.kt unless Step 3 turns up something and you flag it first.

## Done when
- grep -n "minSdk" app/build.gradle.kts shows minSdk = 26.
- setUserAuthenticationParameters in KeyManager.kt is only called
  inside an if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) branch.
- A diff of both changed files is shown.
- A short list of anything else found in Step 3 (even if empty) is
  reported at the end.
