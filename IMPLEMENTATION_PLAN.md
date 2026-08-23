# Knote implementation plan

## Product direction

Knote remains a quiet, private, offline-first productivity app. The redesign will improve hierarchy, spacing, accessibility, responsiveness, and navigation clarity without turning the product into a gamified dashboard. Arabic and English will be first-class locales, with English fallback for any future key that is not yet translated.

## Keep unchanged

The encrypted Room/SQLCipher database, key wrapping, Argon2 derivation, recovery-code flow, encrypted backup container, explicit Room migrations, no-network invariant, notification scheduling, meditation service, local exports, GPLv3 license, and attribution headers remain protected. Existing user data must never be deleted by a UI or migration change.

## Fix and add

| Area | Change | Reason | Data impact |
|---|---|---|---|
| Locale bootstrap | Add a non-sensitive display-locale mirror in app preferences and apply it through a localized base context on API 26–32; keep platform LocaleManager on API 33+ | Current selection is persisted but not applied below Android 13 | No user data impact; only language code is duplicated |
| Compose direction | Provide `LocalLayoutDirection` from the active locale and use `start/end` plus auto-mirrored icons | Manifest RTL support alone does not guarantee custom Compose layouts mirror correctly | None |
| Arabic resources | Add a complete `values-ar/strings_ui.xml` matching default string/plural/array keys | Current Arabic coverage is 177/780 strings and lacks all plurals/arrays | None |
| Theme | Centralize surfaces, typography, shapes, spacing, and semantic accent colors in Material 3; remove repeated literal accent colors from shell components | Current visual tokens are repeated in feature files and make consistency difficult | None |
| Navigation shell | Replace the custom bottom row with modern Material 3 navigation buttons and adaptive sizing while preserving Today/Finance/Notes behavior | Current navigation is visually weak and fixed-size | None |
| Primary screens | Improve Today, Notes, Finance empty states, cards, section hierarchy, and accessibility semantics without changing domain behavior | Highest daily-use visual surface | None |
| Data performance | Add only measured indexes; likely notes composite index is deferred unless query evidence justifies it | Avoid unnecessary schema version bumps and migration risk | If added, requires explicit migration and schema update |
| Error UX | Map security/storage exceptions to stable localized messages and avoid exposing raw technical/German messages | Raw exception text can leak into user-facing auth errors | None |
| Release integrity | Add version policy/checks so every release tag produces a strictly increasing version code | Prevent indistinguishable APK releases | None |
| Verification | Add locale resource parity tests, locale mapping tests, layout-direction tests where feasible, and static checks | Prevent regressions in the areas being changed | None |

## Explicitly not deleting

No feature, entity, migration, security control, export format, or existing settings field will be removed in this pass. Deprecated code will only be removed after proving it is unused and after documenting the reason. The existing `AUDIT_REPORT.md` and `AUDIT_NOTES.md` remain as historical evidence; the final report will supersede neither.

## Branch and delivery

Work proceeds on a branch named `modernize/ar-en-ui-and-rtl`. Commits will be separated by concern where practical. The branch will be pushed to the existing GitHub repository; `main` will not be rewritten. Because the repository's project context declares GitHub Actions authoritative and forbids local Gradle execution, build/test verification will use static checks locally and the CI workflow remotely.

## Acceptance criteria

The change is accepted only if the repository has no untracked generated secrets, default and Arabic resource key sets are equal, selecting Arabic or English updates text and layout direction, the stored choice survives process restart, the app remains offline and encrypted, all existing unit/instrumented test sources remain compatible, the CI build/lint/unit-test workflow passes, and the final report explains every retained, added, or removed item.
