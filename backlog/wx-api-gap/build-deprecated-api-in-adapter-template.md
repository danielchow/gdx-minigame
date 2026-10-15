# Adapter metrics — deprecated system-info calls

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
The external adapter template uses up to three getSystemInfoSync calls when filling missing devicePixelRatio/innerWidth/innerHeight. It uses screen dimensions for window dimensions.

**Where in the codebase:**
- `backends/backend-minigame/src/main/resources/templates/minigame/adapter.js` (lines 30–37).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java` (lines 213–216).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameGraphics.java` (lines 241–261).

**What happens at runtime:**
The legacy fallback remains in generated adapter.js. Deprecation warnings, performance impact and future removal failures were not reproduced.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.getWindowInfo` and `wx.onWindowResize`.

**Doc path:**
- `references/wx-api-docs/base/system/wx.getWindowInfo.md`.
- `references/wx-api-docs/ui/window/wx.onWindowResize.md`.

**API signature / key details:**
Use pixelRatio plus windowWidth/windowHeight, consistent with the window-following graphics code. Screen size need not equal a PC window size.

## Proposed Solution

**How to fix:**
Edit the external adapter.js template. Cache one startup window snapshot and refresh owned fallback metrics on the resize event; preserve appropriate capability fallback.

**Binding changes needed (WX.java):**
Existing Java window-info/resize bindings can remain. The adapter needs its own startup/resize logic.

**Implementation changes needed:**
Keep logical window dimensions consistent with MiniGameGraphics and avoid synchronous native calls in per-frame property getters. Never reintroduce inline generated JS strings in MiniGameBackend.

## Notes

Template loading is already externalized. The resize reference is under ui/window/.
