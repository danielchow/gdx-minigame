# Build output — incomplete final package validation

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Missing engine/data URI and invalid WASM base64 already fail the build. Remaining checks include the final game script and entry/layout consistency: build() skips a missing target script, and appId only warns.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java` (lines 109–127).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java` (lines 163–165).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java` (lines 233–283).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java` (lines 367–390).

**What happens at runtime:**
An incomplete game script/layout can escape those checks and fail at startup. No malformed-output runtime failure was reproduced.

## WeChat Mini Game API Reference

**WX API that can fix this:**
No runtime binding is needed.

**Doc path:**
Not applicable; this is a build/backend issue.

**API signature / key details:**
Current entries are subpackages/engine/game.js, subpackages/game/game.js and assets/game.js. The scripts/ layout is obsolete.

## Proposed Solution

**How to fix:**
Add a final output validator after compilation, relocation, extraction and extension renaming. Fail for missing required scripts/entries/assets and mismatched references; define separate development/production appId policy.

**Binding changes needed (WX.java):**
None.

**Implementation changes needed:**
Validate configured targetFileName against actual output, game.json subpackage roots, extracted WASM path and rename-map targets. Derive extension checks from an authoritative policy rather than claiming the current whitelist is proven incomplete.

## Notes

Current release-directory cleanup protects against stale output; it is not a complete output validator. Keep existing extraction failures and private-config preservation. Platform package-size/policy limits need separate verification.
