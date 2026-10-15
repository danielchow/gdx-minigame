# Context powerPreference — undocumented behavior

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Graphics passes powerPreference and the startup splash requests high-performance. The checked-in Canvas.getContext attributes do not document this option.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameGraphics.java` (lines 73–90).
- `backends/backend-minigame/src/main/resources/templates/minigame/first-screen.js` (lines 11–14).

**What happens at runtime:**
Whether clients accept, ignore or act on the option is unverified. No GPU selection, frame-rate or battery regression has been measured.

## WeChat Mini Game API Reference

**WX API that can fix this:**
No documented equivalent was identified in the checked-in canvas API.

**Doc path:**
- `references/wx-api-docs/render/canvas/Canvas.getContext.md`.

**API signature / key details:**
The snapshot documents antialias, preserveDrawingBuffer, antialiasSamples and alpha; omission alone does not prove powerPreference is rejected.

## Proposed Solution

**How to fix:**
Check supported clients and context attributes before deciding whether to omit or retain this hint. Document the result and capability policy.

**Binding changes needed (WX.java):**
None unless verification identifies a supported alternative.

**Implementation changes needed:**
Keep the splash and application context requests consistent, because the splash obtains the first WebGL context. Make any generated-JS change in first-screen.js.

## Notes

This is an investigation with unverified runtime behavior; do not promise that removing the option changes GPU selection or performance.
