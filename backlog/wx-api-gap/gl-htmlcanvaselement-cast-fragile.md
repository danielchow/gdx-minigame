# Canvas overlay — browser type used for WX canvas

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
MiniGameGraphics stores config.canvas as HTMLCanvasElement. WXCanvas exposes width/height and only the one-argument getContext, so graphics relies on a broader browser overlay for context attributes.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameGraphics.java` (lines 37–64).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameGraphics.java` (lines 73–92).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WXCanvas.java` (lines 11–26).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 26–27).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 45–48).

**What happens at runtime:**
TeaVM overlays are structural; the cast itself does not prove a runtime failure. No failing current canvas call was identified.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`Canvas.getContext(contextType, contextAttributes)`.

**Doc path:**
- `references/wx-api-docs/render/canvas/Canvas.getContext.md`.

**API signature / key details:**
The WX canvas documents the context-attributes overload used by graphics. It is not a full browser DOM element.

## Proposed Solution

**How to fix:**
Add the attributes overload to WXCanvas and use the narrow WX overlay for operations it supports.

**Binding changes needed (WX.java):**
Extend WXCanvas rather than WX.java; add only methods actually needed by backend callers.

**Implementation changes needed:**
Replace the graphics field/cast after checking existing width/height, context and accessor consumers. MiniGameInput already receives graphics rather than holding a canvas; do not reintroduce a canvas input dependency.

## Notes

This is structural-type cleanup, not demonstrated runtime breakage. Unsupported future DOM methods are not evidence of a present failure.
