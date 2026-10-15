# Desktop input — native mouse and wheel events are missing

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Only touch handlers are registered; no native mouse or wheel bindings were found.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 51–55).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 73–119).

**What happens at runtime:**
The backend synthesizes left-button events from touches. Native hover, other mouse buttons and wheel events have no implementation; whether desktop touch emulation covers ordinary clicks is unverified.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.onMouseDown`, `wx.onMouseUp`, `wx.onMouseMove`, `wx.onWheel` and removal APIs.

**Doc path:**
- `references/wx-api-docs/device/mouse-event/wx.onMouseDown.md`.
- `references/wx-api-docs/device/mouse-event/wx.onMouseMove.md`.
- `references/wx-api-docs/device/wheel-event/wx.onWheel.md`.

**API signature / key details:**
These are event APIs; payload coordinates, button values and wheel deltas need mapping to InputProcessor.

## Proposed Solution

**How to fix:**
Add supported desktop events with consistent window-to-canvas scaling, hover and scroll delivery, and per-button state.

**Binding changes needed (WX.java):**
Add typed result overlays and on/off mouse/wheel bindings with capability checks.

**Implementation changes needed:**
Extend MiniGameInput without double-delivering clicks received through touch emulation. Verify right/middle buttons, wheel signs and pointer coordinates on PC.

## Notes

PC orientation/minimize support and window-following canvas/touch scaling already exist. This note does not establish that desktop input is wholly unavailable.
