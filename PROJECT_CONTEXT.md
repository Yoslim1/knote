# Knote — Project Context

## Origin
Fork of Mushotoku (https://github.com/tomfrischmuth/mushotoku), GPLv3.
Forked and rebranded 2026 by Yosef (GitHub: dalelalmuslim).

## Legal / Licensing (non-negotiable)
- LICENSE file: verbatim GPLv3 text, never edit.
- Every source file retains a single-line header:
  `Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026.`
- NOTICE.md: single attribution notice, required by GPLv3 Section 5(a).
- No mention of "Tom Frischmuth" or "Mushotoku" anywhere else in the
  repo (README, UI strings, comments) beyond the items above.
- Not distributed on F-Droid. No F-Droid references anywhere in docs.
- fastlane/ and .idea/ were intentionally removed. Do not recreate them.

## Identity
- App name: Knote
- applicationId / namespace: com.dalelalmuslim.knote
- GitHub: dalelalmuslim/knote (private repo)

## Verified security baseline (as of fork date)
- No INTERNET permission
- allowBackup = false
- No analytics / ads / crashlytics / telemetry SDKs of any kind
- Distribution: GitHub Releases only, built via GitHub Actions signed APK

## Build / CI rules (shared with the Notely project on this device)
- GitHub Actions is the sole authoritative build pipeline.
  No local `./gradlew` builds on this device (hardware insufficient).
- OpenCode CLI gotchas learned the hard way on this project:
  - `opencode run --model provider/model "message" -f file` — the `-f`
    flag must come AFTER the positional message, not before
    (yargs array-flag greedy-consumption bug).
  - Never mix Arabic (or any RTL) text inside a bash heredoc on this
    device. It causes terminal buffer corruption, and script logic can
    silently fail to match even when the file looks correct afterward.
    Keep all script comments in English.
  - Prefer writing a script to a file first, then running it
    (`cat > script.py << 'EOF' ... ; python3 script.py`), over inline
    `python3 -c "..."` for anything non-trivial.
  - Verify script results by inspecting the actual file state
    (byte-level grep/python check), not by trusting printed
    success/failure flags from the same script if terminal corruption
    is possible.

## Model assignments (OpenCode Zen free gateway, format: opencode/<model>)
- deepseek-v4-flash-free — daily coding
- laguna-s-2.1-free — complex multi-step builds
- nemotron-3-ultra-free — security analysis
- nemotron-3.5-lightning-free — trivial/fast fixes
