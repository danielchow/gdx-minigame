# copyTo — whole-file buffering and extra copies

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
File copies read native data into a Java byte array and buffer the complete destination before persistence. Local storage is already native/persistent, not a map of permanent in-memory file copies.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameFileHandle.java` (lines 610–629).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/FileDB.java` (lines 27–52).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/types/InternalStorage.java` (lines 204–211).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/PersistentFileStorage.java` (lines 56–90).

**What happens at runtime:**
Copying can retain several full-file buffers: native read ArrayBuffer, Java read bytes, growing output buffer, toByteArray result and write-side Int8Array. Peak overlap and out-of-memory thresholds have not been measured; a fixed 3× estimate is unsupported.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`FileSystemManager.copyFileSync` for eligible files; descriptor-based chunked read/write for other cases.

**Doc path:**
- `references/wx-api-docs/file/FileSystemManager.copyFileSync.md`.
- `references/wx-api-docs/file/FileSystemManager.readSync.md`.
- `references/wx-api-docs/file/FileSystemManager.writeSync.md`.

**API signature / key details:**
Native path permissions and package-path restrictions must be checked for each source/destination pair. File copying is separate from recursive directory traversal.

## Proposed Solution

**How to fix:**
Use native copy only where supported and compatible with the FileHandle contract. Otherwise implement bounded streaming rather than repeated whole-file conversion.

**Binding changes needed (WX.java):**
Add checked copy or descriptor bindings with explicit native failure propagation.

**Implementation changes needed:**
Refactor FileDB.write, which is final, before introducing storage-specific output streams. Keep directory handling recursive and preserve errors. Measure allocation overlap on representative files.

## Notes

Native copy is not an established atomic replacement guarantee. Address [move failure safety](moveTo-copy-delete-data-loss.md) independently; the historical filename does not assert a measured multiplier.
