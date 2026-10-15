# Indexed drawing — Buffer contents are never uploaded

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
The Buffer overloads of glDrawElements and glDrawRangeElements pass indices.position() as a bound element-buffer byte offset without uploading indices.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/WebGL20.java` (lines 340–341).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/WebGL30.java` (lines 236–237).

**What happens at runtime:**
The supplied client-side index data is ignored. The call instead reads the currently bound element buffer at an unrelated offset or produces a GL error; exact rendering impact has not been reproduced.

## WeChat Mini Game API Reference

**WX API that can fix this:**
WebGL indexed draws consume a bound element buffer plus a byte offset.

**Doc path:**
- `references/wx-api-docs/render/canvas/RenderingContext.md`.

**API signature / key details:**
Buffer position is expressed in elements for ShortBuffer/IntBuffer, whereas the draw offset is bytes. Multiplying position alone still does not upload the supplied data.

## Proposed Solution

**How to fix:**
Either fail fast with a clear unsupported-client-buffer message or implement a temporary element-buffer upload with validated type/count and correct byte ranges.

**Binding changes needed (WX.java):**
No WX.java change is required; use the existing GL buffer operations and query support or a reliable binding-state cache.

**Implementation changes needed:**
If emulating, preserve the existing element-buffer binding and WebGL2 VAO state, upload only the count/type range starting at the buffer position, draw at the matching offset, and restore/delete temporary state even after failure.

## Notes

The integer-offset overload remains a separate supported path. Verify nonzero positions, index widths and previously bound buffer state; no temporary-buffer sample should silently replace that state.
