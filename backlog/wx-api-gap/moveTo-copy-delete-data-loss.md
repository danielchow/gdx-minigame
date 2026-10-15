# moveTo — quiet close can hide a failed persistent write

> **Status**: 📋 Researched
>
> **Severity**: 🔴 High (inferred data-loss risk; fix first)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
The copy/delete move path can delete its source after a failed destination write. FileDB buffers writes and commits only on close, but MiniGameFileHandle.write(InputStream, ...) closes the output quietly. A successful return from copyTo therefore does not prove persistent storage accepted the bytes.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameFileHandle.java` (lines 251–264).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameFileHandle.java` (lines 554–575).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameFileHandle.java` (lines 610–629).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/FileDB.java` (lines 27–52).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/PersistentFileStorage.java` (lines 67–90).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/filesystem/PersistentFileStorage.java` (lines 183–195).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WX.java` (lines 204–205).

**What happens at runtime:**
Inferred failure sequence: a move falls through to copyTo; the destination buffers the source; close calls fsWriteFileSync and throws; closeQuietly suppresses that failure; copyTo returns; moveTo calls delete on the source. Storage-full, permission or I/O failure could expose this path. It has not been reproduced.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`FileSystemManager.renameSync` for eligible native moves; copy APIs or a checked stream copy for fallback.

**Doc path:**
- `references/wx-api-docs/file/FileSystemManager.renameSync.md`.
- `references/wx-api-docs/file/FileSystemManager.copyFileSync.md`.

**API signature / key details:**
PersistentFileStorage already implements native rename with failure reporting. moveTo currently uses it only for a missing destination with the same parent. The reference does not establish universal atomicity or overwrite guarantees.

## Proposed Solution

**How to fix:**
Use native rename for compatible paths in the same persistent storage where its semantics are validated. For fallback, copy to a unique temporary destination, explicitly close and propagate commit failures, verify complete persisted content, then publish the destination safely. Never delete the source or an existing destination unless the copy verifiably succeeded; a failed copy must preserve both originals.

**Binding changes needed (WX.java):**
Reuse fsRenameSync where applicable. Any new copy/write binding must surface native failure instead of suppressing it. Native copy alone is not proof of safe replacement semantics.

**Implementation changes needed:**
Fix the underlying stream-copy success contract so close is part of the checked operation. Preserve the original error if cleanup also fails. Resolve destination-directory semantics before moving, handle self-moves, and require every file in a directory copy to succeed before deleting any source tree. Preserve or reject an existing destination if safe publication cannot be guaranteed; do not pre-delete it or delete it as rollback.

## Notes

High priority reflects the possible irreversible loss, not a reproduced incident. Read-only javap of cached LibGDX 1.14.0 confirms StreamUtils.closeQuietly catches Throwable. A future focused regression should inject a close-time persistent-write failure and assert the source and any pre-existing destination survive; also cover successful cross-directory moves and partial directory copies.
