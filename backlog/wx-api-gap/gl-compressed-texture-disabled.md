# Compressed textures — upload methods throw

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Compressed image and subimage uploads throw unconditionally, regardless of context capabilities.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/WebGL20.java` (lines 192–200).

**What happens at runtime:**
A caller of either method receives GdxRuntimeException. Device/format support must be established per context; no universal compressed-format support is claimed.

## WeChat Mini Game API Reference

**WX API that can fix this:**
Canvas WebGL contexts expose compressed upload operations when the requested format is supported.

**Doc path:**
- `references/wx-api-docs/render/canvas/RenderingContext.md`.
- `references/wx-api-docs/render/canvas/Canvas.getContext.md`.

**API signature / key details:**
The reference snapshot mentions PVR on iOS and ETC1 on Android. This does not establish every format on every current device.

## Proposed Solution

**How to fix:**
Gate uploads by the actual context/extension format set. Construct an exact byte view beginning at the caller buffer position with imageSize bytes, then call the corresponding compressed upload.

**Binding changes needed (WX.java):**
No WX.java change is required. Extend the minigame GL overlay if typed compressed upload methods are missing.

**Implementation changes needed:**
Handle direct and copied buffers, validate imageSize against remaining bytes, account for the source view byte offset, preserve position/limit, and support both image and subimage operations.

## Notes

Fail clearly for unsupported formats. Do not ignore imageSize or upload the entire buffer; test nonzero positions and device-supported formats.
