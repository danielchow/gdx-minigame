# Build configuration — standalone fields are not consumed

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
MiniGameBuildConfiguration duplicates backend settings but has no repository consumer. MiniGameBackend only imports it and uses its own fields/setters.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/MiniGameBuildConfiguration.java` (lines 7–12).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java` (lines 4).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java` (lines 39–81).

**What happens at runtime:**
Setting fields on a standalone configuration instance does not configure generated output. Builders using backend setters already work.

## WeChat Mini Game API Reference

**WX API that can fix this:**
None.

**Doc path:**
Not applicable; this is a build/backend issue.

**API signature / key details:**
This concerns the public build configuration surface, not a runtime WX binding.

## Proposed Solution

**How to fix:**
Choose one supported configuration path: wire an explicit configuration object into the backend, or deprecate/remove the unused class after checking external users.

**Binding changes needed (WX.java):**
None.

**Implementation changes needed:**
Keep backend setters and any new configuration adapter consistent, including desktopOrientation and precedence rules. Preserve the appId diagnostic.

## Notes

AGENTS.md now describes the actual setter-based path. No external consumer compatibility review has been performed.
