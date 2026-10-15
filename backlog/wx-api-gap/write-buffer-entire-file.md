# Writes — entire-file buffering and append copies

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
FileDB.write always buffers the complete write and persists it on close. Its bufferSize parameter only controls initial allocation. Persistent append rereads the old file and combines old/new bytes.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/FileDB.java` (lines 27–52).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameFileHandle.java` (lines 251–264).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/PersistentFileStorage.java` (lines 67–90).

**What happens at runtime:**
Memory use grows with the data size, with additional toByteArray and typed-array copies. Append also allocates old and combined content. Peak memory and timing have not been measured.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`FileSystemManager.openSync`, `writeSync`, `closeSync` and `appendFileSync`.

**Doc path:**
- `references/wx-api-docs/file/FileSystemManager.openSync.md`.
- `references/wx-api-docs/file/FileSystemManager.writeSync.md`.
- `references/wx-api-docs/file/FileSystemManager.closeSync.md`.
- `references/wx-api-docs/file/FileSystemManager.appendFileSync.md`.

**API signature / key details:**
Native descriptor writes can avoid whole-file buffering; partial writes and close failures need handling. InputStream.available() is not the total length.

## Proposed Solution

**How to fix:**
Refactor the final FileDB.write API to support storage-specific bounded output streams and native append, with explicit commit/error semantics.

**Binding changes needed (WX.java):**
Add checked descriptor/open/write/close or append bindings; respect the input byte range and report actual bytes written.

**Implementation changes needed:**
Keep a bounded fallback where necessary, avoid sizing correctness from available(), and propagate close-time persistence failures. Preserve append semantics under failed reads/writes.

## Notes

Local persistence already exists; MemoryFileStorage proposals no longer apply. Coordinate the close contract with [move safety](moveTo-copy-delete-data-loss.md).
