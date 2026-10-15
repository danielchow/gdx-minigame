# Accelerometer — getters return zero

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
All three acceleration getters return zero; no accelerometer binding was found.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 132–134).

**What happens at runtime:**
Tilt input polled through these getters receives no sensor data.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.startAccelerometer`, `wx.onAccelerometerChange`, `wx.stopAccelerometer` and `wx.offAccelerometerChange`.

**Doc path:**
- `references/wx-api-docs/device/accelerometer/wx.startAccelerometer.md`.
- `references/wx-api-docs/device/accelerometer/wx.onAccelerometerChange.md`.
- `references/wx-api-docs/device/accelerometer/wx.offAccelerometerChange.md`.

**API signature / key details:**
The listener receives one result object with `x`, `y` and `z`. The reference snapshot does not establish acceleration units.

## Proposed Solution

**How to fix:**
Cache sensor readings and return them from the getters. Verify units, axis signs and orientation conversion against LibGDX before assigning values.

**Binding changes needed (WX.java):**
Add start/stop and on/off bindings with a one-argument callback such as `void invoke(AccelerationResult result)`; the result overlay exposes numeric x/y/z properties. A three-number callback passed directly to WX is invalid.

**Implementation changes needed:**
Initialize only when requested/supported, handle startup failure, stop on hide and dispose, and restore on show when enabled. Report availability from actual startup state.

## Notes

Intervals are requested frequencies, not guarantees. Device checks must establish conversion and lifecycle behavior; no device impact was measured.
