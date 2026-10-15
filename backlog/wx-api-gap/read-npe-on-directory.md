# FileHandle.read — primary storage can return null

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
The primary storage read result is returned without a null check, despite read() promising a stream or GdxRuntimeException. Storage implementations now check null before constructing ByteArrayInputStream; the old immediate constructor NPE diagnosis is obsolete.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameFileHandle.java` (lines 92–105).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/types/InternalStorage.java` (lines 197–211).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/PersistentFileStorage.java` (lines 47–63).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WX.java` (lines 195–196).

**What happens at runtime:**
An existing directory or an existing file whose native read fails can yield null. Downstream callers may then fail away from the actual read; this path has not been reproduced.

## WeChat Mini Game API Reference

**WX API that can fix this:**
No new WX API is required. Preserve failure information from existing readFileSync if practical.

**Doc path:**
- `references/wx-api-docs/file/FileSystemManager.readFileSync.md`.

**API signature / key details:**
fsReadFileSync currently catches native errors and returns null. Both storage read methods propagate that null.

## Proposed Solution

**How to fix:**
Check the primary result just as the classpath fallback is checked, and throw a descriptive GdxRuntimeException for unreadable paths. Distinguish directories when feasible.

**Binding changes needed (WX.java):**
Optionally return or wrap native read errors instead of discarding their cause; do not add synchronous checks to unrelated hot getters.

**Implementation changes needed:**
Centralize the stream-or-exception contract in MiniGameFileHandle.read. Preserve intended fallback behavior and cover directory, missing file and native read failure.

## Notes

The filename is retained for continuity; a ByteArrayInputStream(null) failure is no longer the mechanism.
