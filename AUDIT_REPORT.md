# Knote Full Project Audit — 2026-08-22

## Summary
- Files reviewed: 186 Kotlin files (161 main + 25 test, ~23,300 lines) plus AndroidManifest, Gradle configs, workflows, all `res/values*` resource files, Room schemas, and CI/release metadata
- Historical commits/releases reviewed: 14 commits (entire history), 2 tags (v1.4.0, v1.4.1), 2 GitHub releases, 11 CI workflow runs
- Features/additions reviewed: 16 feature areas (encryption/security layer, SQLCipher DB + plaintext migration, encrypted backup/restore, reminders/alarms/boot rescheduling, meditation service/timer, finance module, habits, gratitude/mood tracking, caffeine/sleep lab, holidays, notes (markdown/checklists/tags/trash/pin), themes, localization (ar + 8 further locales), icon switcher, PDF/ZIP export, licenses screen)
- Issues found: 9
- Issues fixed automatically: 5 (9 file changes, +20/−78 lines)
- Issues flagged for human review: 4
- Previous fixes re-verified: 10
- Regressions found: 0 (no previously applied fix was broken by later commits; two pre-existing crash bugs of the same class as earlier fixes were found baselined instead of fixed)
- Verification status: Partially verified — all fixes statically verified and re-read; local Gradle execution is forbidden by audit rules, so compile/lint/test verification of the changed tree requires the next GitHub Actions run

## Historical / Regression Review
- Previous fixes checked: 10 (`aab81bc`, `a1876e5`, `883f2de`, `383c123`, `d414e2c`, `e31af0f`, `7a00e68`, `2159fd4`, `cd5a430`, `8091de0`, plus baseline snapshot `b408fc8`)
- Previous fixes still valid: 9 of 10 fully intact; `8091de0` ("include Arabic locale in build") was only partially effective — see findings below
- Regressions found: 0 — each fix was located in current source and confirmed still present and correct:
  - Destructive-migration removal intact: no `fallbackToDestructiveMigration` anywhere; migrations 1→2→3→4→5 chained in `app/src/main/java/com/dalelalmuslim/knote/data/AppDatabase.kt:96`
  - Exact-alarm guard intact: `notification/ReminderScheduler.kt:82-93` checks `canScheduleExactAlarms()` behind SDK_INT ≥ S with inexact fallback; app holds always-granted `USE_EXACT_ALARM`
  - StrongBox guards intact: `security/KeyManager.kt:270-277` and `KeyManager.kt:317-324` generate StrongBox keys only on API ≥ P with `StrongBoxUnavailableException` fallback (lint-baseline entries for these are false positives caused by the lambda indirection — manually verified safe)
  - Biometric auth-parameter guard intact: `KeyManager.kt:297-312` uses `setUserAuthenticationParameters` on API ≥ 30, `-1` fallback below
  - LocaleManager guard intact: `AppScaffold.kt:135` gates `applicationLocales` behind SDK_INT ≥ 33
  - Theme default intact: `data/AppSettings.kt:16` defaults `themeMode = "SYSTEM"`; `ui/theme/Theme.kt:28-32` maps unknown persisted values to system dark mode (no crash path)
  - Currency default intact: `repository/AppRepository.kt:99` derives default currency via `defaultCurrencyCodeForLocale(Locale.getDefault().country)` (`ui/AppCurrency.kt:119-137`)
  - Test migration to Mockito intact (`test/.../KeyManagerUnitTest.kt`)
  - CI keystore-path fix intact; release builds succeed (see GitHub verification)

### Concrete historical findings
1. The lint-debt snapshot `b408fc8` (657 issues) contained 10 `NewApi` entries; manual analysis showed **6 were real crash-class defects** (two files, see "Fixed automatically") and 4 were false positives from guarded StrongBox code. This is the same bug class fixed in `383c123`/`2159fd4`, but these occurrences were baselined instead of fixed.
2. Commit `883f2de` added Arabic for only `values/strings.xml`; `TASK_3_arabic_localization.md` scoped it that way, but the repo also contains `values/strings_ui.xml` (602 strings + 16 plurals + 4 arrays — every tab, button, dialog, setting label, and the notification plurals). Arabic has none of these, so under Arabic locale most of the UI falls back to English. Pre-existing since the feature landed; not a later regression.

## Fixed automatically

### Android API / minSdk compatibility (crash-class)
- `app/src/main/java/com/dalelalmuslim/knote/util/Haptics.kt:12-15` — `performCheckHaptic()` used `VibratorManager` (API 31+) and `VibrationEffect.createPredefined` (API 29+) with no SDK_INT guard while minSdk is 26. On Android 8.x–10 devices this threw `NoClassDefFoundError`/`NoSuchMethodError` on every task completion, habit check-off, and note-check interaction (5 call sites: `AppScaffold.kt:325`, `AppScaffold.kt:348`, `NoteEditor.kt:185,192,248`). Root cause: same pattern as the previously fixed LocaleManager crash, but hidden in the lint baseline instead of fixed. Fix: return early below API 31 (behavior-preserving no-op; identical feedback on API 31+).
- `app/src/main/java/com/dalelalmuslim/knote/service/MeditationService.kt:96-104` — three-argument `startForeground(id, notification, foregroundServiceType)` exists only from API 29; unguarded call crashed meditation start on API 26–28 with `NoSuchMethodError`. Fix: SDK_INT ≥ Q guard; below Q uses the plain two-argument form (the manifest's `foregroundServiceType="mediaPlayback"` applies at platform level on 29+).

### Localization / resource consistency
- `app/src/main/res/xml/locales_config.xml:13` — Arabic was missing from the per-app locale config although commit `8091de0` claims to "include Arabic locale in build" (only `localeFilters` was updated). Without the declaration, Android 13+ cannot offer/show Arabic in system per-app language settings, and the baselined `UnusedTranslation` warning ("language ar present but not declared in localeConfig") confirmed the inconsistency. Fix: added `<locale android:name="ar" />`.
- `app/src/main/res/values/strings_ui.xml:55` + `ui/screens/SettingsDarstellung.kt:114` + `ui/strings/AppStrings.kt:69` — the in-app language picker offered AUTO/de/en/es/fr/it/pt-PT/pt-BR/nl/pl but **no Arabic option**, making the shipped `values-ar` translation unreachable through the app's own settings (only via device-wide locale). Fix: added non-translatable endonym `lang_arabic` = "العربية" (standard language-picker convention; avoids new MissingTranslation debt in the other 8 locales), `langArabic` accessor, and the `"ar"` picker entry. `localeListForSetting("ar")` already resolves correctly via `MainActivity.kt:555`.

### Hard-coded user-facing string
- `app/src/main/java/com/dalelalmuslim/knote/service/MeditationService.kt:208` — notification channel name was hardcoded `"Meditation"` (visible in system Settings → App → Notifications); the appointments channel correctly used a resource. Fix: added `meditation_channel_name` to `values/strings.xml` ("Meditation") and `values-ar/strings.xml` ("تأمل") and referenced it via `getString(...)`.

### Lint baseline hygiene
- `app/lint-baseline.xml` — removed exactly the 7 entries made obsolete by the fixes above (6× NewApi/InlinedApi for Haptics.kt and MeditationService.kt, 1× UnusedTranslation for the missing `ar` locale declaration). Formatting preserved; remaining 651 entries untouched. Note: the 4 remaining KeyManager NewApi baseline entries correspond to code that IS runtime-guarded (verified manually) — they stay documented as known false positives.

## Flagged for human review (NOT fixed)

### Localization completeness
- `app/src/main/res/values-ar/` — no `strings_ui.xml`: all 602 strings, 16 `<plurals>` (including `reminder_in_hours`/`reminder_in_minutes` used by notifications), and 4 `<string-array>`s fall back to English under Arabic locale (618 MissingTranslation entries in the lint baseline). Why human approval: translating ~620 strings requires native-speaker quality review (per the project's own TASK_3 standard); bulk machine translation would violate the established translation policy.

### Release / version integrity
- `app/build.gradle.kts:36-37` — `versionCode = 5` and `versionName = "1.4"` were not bumped between tags: both published releases (v1.4.0 built from `cd5a430`, v1.4.1 from `8091de0`) ship APKs with identical versionCode/versionName. Consequence: Android treats them as the same version — sideloading v1.4.1 over v1.4.0 is refused/confusing, installed-version cannot be distinguished, and future store-style updates need strictly increasing codes. Why human approval: choosing the next versionCode/versionName is a release-management decision; changing it now does not retroactively fix the published artifacts.

### Error-message localization (pre-existing design)
- `app/src/main/java/com/dalelalmuslim/knote/MainActivity.kt:244,263,351,368,388,460,479` and `security/SecurityController.kt:69,83,112,182,208,239` — raw German exception messages (`e.message`, e.g. "Kein gewrappter DEK vorhanden" from `KeyStorage`/`KeyManager`) can reach the lock-screen error slot in rare failure paths on EN/AR devices; deterministic messages (wrong passphrase/recovery code) are properly localized. Why human approval: mapping every security exception type to localized resources changes user-visible error behavior across the auth flow and needs a deliberate message-design pass, not an automatic edit.

### Language application scope (architectural, pre-existing upstream)
- `viewmodel/SettingsViewModel.kt:47` + `AppScaffold.kt:133-142` — the persisted language preference is only applied via `LocaleManager` (API 33+); on API 26–32 selecting any language (not just Arabic) persists the choice but never applies it (no `attachBaseContext`/appcompat backport). Pre-existing since the fork; affects all languages equally. Why human approval: fixing requires adding appcompat per-app-locale backporting or a configuration-context layer — an architectural change out of scope for a safe-fix pass.

## Release / GitHub Verification
- Repository/version checked: `Yoslim1/knote` (public), branch `main` at `8091de03aa7a433d155ee5b2cd7441fe8ba19732` (= tag `v1.4.1`); `versionCode=5`, `versionName="1.4"`, minSdk 26, targetSdk 37, namespace/applicationId `com.dalelalmuslim.knote`
- Git tag checked: `v1.4.0` → `cd5a430`, `v1.4.1` → `8091de0` (both present locally and on origin; HEAD == v1.4.1)
- GitHub release checked: v1.4.1 (Latest, published 2026-08-21T13:04:25Z by github-actions[bot], target main) and v1.4.0 (published 2026-08-21T09:36:06Z)
- APK artifact checked: `app-release.apk` attached to both releases (v1.4.1: 32,942,019 bytes, sha256 digest `904a6c97…befec351` recorded by GitHub; v1.4.0: 32,930,099 bytes, digest `52baf6bf…bc49f421`), each accompanied by `app-release.apk.sha256` — consistent with `.github/workflows/release.yml` upload step. Caveat: the released APK contains the pre-audit code; the fixes from this audit are NOT in any published artifact yet.
- Relevant CI run checked: Release run #3 (tag v1.4.1, commit `8091de0`) — success; CI run #7 (main, `8091de0`: unit tests + lint + debug assemble) — success
- Verification limitations: the audit's own changes have no CI run yet (local Gradle execution prohibited by task rules); compile/test verification of the modified tree must come from the next CI run on a pushed commit. APK binary contents were verified only via release/workflow metadata, not byte-level inspection.

## Clean categories
The following audit categories produced no concrete issues:
- Crash risk / null safety: only 7 `!!` uses exist; all proven guarded (e.g., `NumberedPrefix` regex `\d{1,9}` bounds `toIntOrNull()` so `NoteMarkdownFormatting.kt:191` cannot NPE; filtered-null patterns before `associateBy`)
- Room / data integrity: schema v5 matches exported JSON (`schemas/.../5.json`, 12 entities = 12 `@Entity` tables); full migration chain 1→5; multi-table restore wrapped in `db.withTransaction` (`BackupRepository.kt:46-64`); finance/habit resets transactional (`AppRepository.kt:55-60,86-94,108`); no destructive fallback
- Security / encryption: DEK-wrap architecture sound (Keystore AES-GCM/RSA-OAEP + Argon2id 64 MiB/t=3); recovery codes 125-bit Crockford Base32 with canonical normalization; all key/passphrase buffers wiped (`SecureBytes`, finally blocks throughout `KeyManager`, `SecurityGate`, `BackupCodec`, `BackupViewModel`); backup container bounds Argon2 parameters on decrypt (DoS-safe); zero secrets in logs (only 3 `Log.w` calls, library IDs only); DB + key prefs excluded from cloud backup/device transfer (`backup_rules.xml`, `data_extraction_rules.xml`); plaintext→encrypted migration wipes key material and temp DB on failure
- Authentication / component exposure: MainActivity unexported; launcher aliases exported as required; receivers/services/FileProvider unexported; PendingIntents FLAG_IMMUTABLE; reminder tap intent carries no sensitive extras; boot receiver only acts in no-lock mode (locked DBs stay encrypted at rest)
- Memory leaks / lifecycle: no GlobalScope/runBlocking; ViewModels use viewModelScope + WhileSubscribed(5000); receivers use goAsync with finish() in finally; MeditationService releases wakelock/media players in onDestroy; Compose listeners bound via DisposableEffect/node lifecycle; SecurityGate relock logic handles system-activity detours
- Deprecated APIs: only intentional `setUserAuthenticationValidityDurationSeconds(-1)` pre-API-30 fallback with suppress + rationale; themes.xml splash attrs (API 31) are silently ignored below 31 — benign
- GPLv3 provenance: all 161 main-source Kotlin files carry the attribution header "Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026."; LICENSE is stock GPLv3. Limitation: upstream Mushotoku history is not locally available (fork arrived as one squashed commit `7c7dd9a`), so file-by-file comparison against upstream headers could not be performed beyond what the current checkout shows
- Theme behavior: SYSTEM default respected; LIGHT/DARK/SYSTEM all map correctly; invalid persisted values degrade to system mode; status-bar appearance synced via SideEffect

## Remaining Risks / Follow-up
1. Translate `values/strings_ui.xml` to Arabic (~602 strings + 16 plurals + 4 arrays) with native-speaker review — until then, Arabic locale shows English for most of the UI including reminder notifications.
2. Bump `versionCode`/`versionName` before the next release (and adopt a policy ensuring every tag bumps versionCode) — v1.4.0 and v1.4.1 APKs are currently indistinguishable to the OS.
3. Push this audit's changes and let GitHub CI verify build/tests/lint; then cut the next tagged release so the API-crash fixes (Android 8.x–10 haptics crash, meditation start crash) reach users — currently every published APK still contains both crash paths.
4. Decide whether to localize the German technical exception messages shown in rare lock/security error paths.
5. Decide whether per-app language selection should work on API 26–32 (requires appcompat backport or equivalent).
