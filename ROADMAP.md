# Knote — Development Roadmap

## Phase 0 — Fork & Legal Baseline (closing out)
- [x] Clone upstream, strip git history, create private repo
- [x] Package rename: com.mushotoku.app -> com.dalelalmuslim.knote
- [x] Rebrand: app name, README, remove fastlane/ and .idea/
- [x] Minimal GPLv3 attribution applied (headers + NOTICE.md)
- [x] Security baseline re-verified (no INTERNET, no analytics, allowBackup=false)
- [ ] Final automated audit pass (nemotron-3-ultra-free)
- [ ] First commit + push to GitHub

## Phase 1 — Full Codebase Audit (current priority, no code changes)
Goal: fully understand the inherited architecture before touching any logic.
- [ ] Data layer: Room entities, DAOs, migrations, AppDatabase — schema map
- [ ] Security layer: KeyManager, Argon2Kdf, SqlCipherKey, KeyStorage,
      SecurityGate — encryption flow map
- [ ] Backup/Export layer: BackupRepository, BackupCodec, exporters (PDF/ZIP/HTML)
- [ ] Business logic: ViewModels per feature (Notes, Tasks, Habits, Finance, Meditation)
- [ ] UI layer: screen inventory, navigation graph, Compose structure
- [ ] Dependency audit: every library in libs.versions.toml — purpose, license, necessity
- [ ] CI/CD: confirm GitHub Actions workflow produces a working signed APK end-to-end
- Deliverable: ARCHITECTURE.md, written after the audit completes

## Phase 2 — Stabilization
- [ ] First real signed release validated on-device
- [ ] Any build warnings/deprecations found during audit resolved
- [ ] Baseline test suite run and results recorded

## Phase 3 — Personal Feature Backlog
- [ ] Backlog defined from audit findings + personal needs
- [ ] Prioritized and executed incrementally, one feature per commit/PR

## Phase 4 — Ongoing Maintenance
- [ ] Dependency updates
- [ ] Periodic security re-audit
