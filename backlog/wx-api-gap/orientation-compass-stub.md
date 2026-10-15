# Orientation — angles and rotation matrix are unimplemented

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Azimuth, pitch and roll return zero; getRotationMatrix leaves its argument untouched. No motion or compass implementation was found.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 205–208).

**What happens at runtime:**
Callers receive fixed angles and an unchanged matrix, rather than orientation data.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.startDeviceMotionListening`, `wx.onDeviceMotionChange` and optional compass APIs.

**Doc path:**
- `references/wx-api-docs/device/motion/wx.startDeviceMotionListening.md`.
- `references/wx-api-docs/device/motion/wx.onDeviceMotionChange.md`.
- `references/wx-api-docs/device/motion/wx.offDeviceMotionChange.md`.

**API signature / key details:**
The listener receives one object with alpha/beta/gamma. The snapshot gives ranges in radians, including alpha in [0, 2π); LibGDX angle getters use degrees. Axis conventions must also be reconciled.

## Proposed Solution

**How to fix:**
Define the coordinate transform first, including screen orientation, handedness and any compass fusion needed for azimuth. Derive angles and the matrix from that common representation.

**Binding changes needed (WX.java):**
Use a one-argument JSFunctor receiving a motion-result overlay, rather than three doubles. Add start/stop/on/off bindings and failure handling.

**Implementation changes needed:**
Cache converted orientation in MiniGameInput and fill the supported matrix sizes according to the LibGDX contract. Handle hide/show/disposal and expose availability accurately.

## Notes

Do not assign alpha/beta/gamma directly to azimuth/pitch/roll. Units and coordinate conventions need device validation.
