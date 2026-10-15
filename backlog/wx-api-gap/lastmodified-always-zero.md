# Local file timestamps — lastModified always zero

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
lastModified returns zero even for persisted Local files; the binding exposes size/directory state but no timestamp.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameFileHandle.java` (lines 588–594).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/PersistentFileStorage.java` (lines 15–39).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WX.java` (lines 219–226).

**What happens at runtime:**
Local timestamp-based invalidation cannot observe changes through this method. Classpath/Internal zero values are allowed by the documented FileHandle behavior.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`FileSystemManager.statSync` and `Stats.lastModifiedTime`.

**Doc path:**
- `references/wx-api-docs/file/FileSystemManager.statSync.md`.
- `references/wx-api-docs/file/Stats.md`.

**API signature / key details:**
lastModifiedTime is documented as a UNIX timestamp; establish its unit on supported runtimes and convert to the milliseconds required by FileHandle.

## Proposed Solution

**How to fix:**
Read timestamps for Local files through the existing persistent path mapping, retaining zero for missing/unavailable data.

**Binding changes needed (WX.java):**
Add a checked timestamp/stat accessor; verify the actual synchronous return shape rather than copying the asynchronous result wrapper.

**Implementation changes needed:**
Implement Local storage timestamps and route lastModified to that storage. Do not invent internal asset creation timestamps.

## Notes

Native stat is a synchronous round trip. Avoid calling it repeatedly during rendering; timestamp units and runtime behavior require validation.
