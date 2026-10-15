# Readback — only RGBA/UNSIGNED_BYTE is implemented

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
glReadPixels rejects every other format/type, casts the destination to ByteBuffer and allocates conversion buffers per call.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/WebGL20.java` (lines 732–745).

**What happens at runtime:**
Other requested pairs throw before reaching the context, including any pair a particular framebuffer might support. No broader device support or allocation cost has been measured.

## WeChat Mini Game API Reference

**WX API that can fix this:**
WebGL readPixels on the selected context/framebuffer.

**Doc path:**
- `references/wx-api-docs/render/canvas/RenderingContext.md`.
- `references/wx-api-docs/render/canvas/Canvas.getContext.md`.

**API signature / key details:**
Legal format/type pairs depend on context version, framebuffer attachments and extensions. RGB, ALPHA, depth and floating readback cannot be assumed available.

## Proposed Solution

**How to fix:**
Support only validated pairs. Determine the required destination typed view, byte count and pack alignment for each pair; keep clear errors for unsupported cases.

**Binding changes needed (WX.java):**
No WX.java changes. Add minigame GL typed-view overloads if required.

**Implementation changes needed:**
Validate destination type/capacity and byte offset, preserve the intended buffer range contract, and handle pack state. Consider buffer reuse only after correctness and measured allocation needs are established.

## Notes

Simply removing the guard and choosing a typed array is insufficient. Verify legal pairs and negative cases on each supported context/framebuffer configuration.
