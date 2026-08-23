# Knote engineering audit notes

## Repository baseline

- Android application written in Kotlin with Jetpack Compose and Material 3.
- Single `:app` module plus a `:baselineprofile` module.
- `minSdk = 26`, `targetSdk = 37`, Room 2.8.4 over SQLCipher 4.16.0.
- Current checkout includes CI workflows under `.github/workflows/` for unit tests, lint, debug build, release, and lint-baseline generation.
- Current latest tag in local history is `v1.4.2`; working tree was clean at clone time.

## Strong invariants to preserve

- No `INTERNET` permission, no telemetry/analytics, and local-only data.
- SQLCipher-encrypted Room database and explicit migrations with no destructive fallback.
- Keystore/Argon2/recovery-code security paths and encrypted backup/restore.
- GPLv3 license and attribution headers.
- Android 26 compatibility, including API guards.

## Confirmed localization state

- Supported packaged locales are `ar` and `en` in Gradle and `res/xml/locales_config.xml`.
- The in-app language picker offers AUTO, English, and Arabic.
- The Arabic resource directory has only 177 string keys while the default resources have 780 string keys.
- Arabic is missing 603 string keys, all 16 plurals, and all 4 string arrays from the default resource set.
- The current locale application uses `LocaleManager.applicationLocales` only on Android 13+; API 26-32 persist the choice but do not apply it.
- Manifest declares `supportsRtl=true`, but the Compose tree does not explicitly provide a layout direction from the selected app locale.

## UI/architecture observations

- `AppScaffold.kt` is a very large orchestration file (~35 KB) owning navigation state, overlays, permissions, reminders, and composition locals.
- `MainActivity.kt` is also large (~24 KB), mixing activity lifecycle, security gate, locale bootstrap, orientation, and UI host responsibilities.
- Top/bottom bars use custom hand-rolled layouts, fixed colors, and a simple 64dp bottom row rather than adaptive Material navigation patterns.
- `TaskScreen.kt` uses a vertically scrolling column with bespoke drag handling and platform date/time dialogs.
- `NotesScreen.kt` uses a fixed two-column grid and custom filter chips; the UI is functional but not adaptive to larger windows.
- Finance/notes/task colors and some category presentation are hard-coded in feature files.
- There is an existing Haze/glass overlay layer, but the visual language is not centralized in the theme and uses repeated literal accent values.

## Data layer observations

- The schema has 12 entities and version 5 with four explicit migrations.
- `tasks.date`, `expenses(date, category)`, `additional_incomes.date`, and `caffeine_doses.timeMillis` are indexed; `notes` has no index despite queries filtering `isDeleted` and sorting by `isPinned, updatedAt`.
- No schema change is justified until query/index performance is measured; adding a composite notes index may be worthwhile but requires a new Room migration and migration tests.
- Repository already wraps destructive finance/category/habit operations in transactions.

## Existing known findings from AUDIT_REPORT.md

- Previous audit fixed API 26-30 haptics and meditation foreground-service crash paths.
- Previous audit fixed Arabic locale declaration and language-picker reachability.
- Previous audit flagged Arabic UI incompleteness, API 26-32 language application, localized security error messages, and release/version integrity.

## Testing constraints

- `PROJECT_CONTEXT.md` states that local Gradle builds are not allowed on this device and GitHub Actions is authoritative.
- We can still run deterministic static audits, resource consistency checks, XML parsing, Kotlin source checks, and repository tests that do not invoke Gradle.
- Final build/test verification should be performed by GitHub Actions after changes are pushed to a branch.

## CI verification evidence

- First CI run on commit `695eff4` failed at Android resource merge because the generated Arabic `meditation_minutes` and `notes_selected` plurals contained duplicate `quantity="one"` items. The failure was reported by GitHub Actions at https://github.com/Yoslim1/knote/actions/runs/32629648449.
- The failure was diagnosed from the AAPT log, corrected by changing the second plural item in each resource to `quantity="other"`, and committed as `389278e`.
- A second CI run was dispatched for commit `389278e` at https://github.com/Yoslim1/knote/actions/runs/32630000977. Its resource parity step completed successfully; at the latest poll, the JVM unit-test step was still in progress and lint/build steps were pending.

أُجري التحقق المحلي عبر Gradle، لكنه توقف قبل تنفيذ المهام لأن بيئة sandbox لا تحتوي على Android SDK ولا `ANDROID_HOME` صالحًا. لذلك اعتمدت فحوصات Python المحلية وGitHub Actions للبناء الفعلي؛ فـ CI أثبت نجاح compile والاختبارات حتى مرحلة lint في التشغيل السابق، وسيعاد تشغيله على آخر commit.
