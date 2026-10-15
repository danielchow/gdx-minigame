# Gyroscope — getters return zero

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
All three gyroscope getters return zero; no gyroscope binding was found.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 135–137).

**What happens at runtime:**
Polling games receive no angular velocity.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.startGyroscope`, `wx.onGyroscopeChange`, `wx.stopGyroscope` and `wx.offGyroscopeChange`.

**Doc path:**
- `references/wx-api-docs/device/gyroscope/wx.startGyroscope.md`.
- `references/wx-api-docs/device/gyroscope/wx.onGyroscopeChange.md`.
- `references/wx-api-docs/device/gyroscope/wx.offGyroscopeChange.md`.

**API signature / key details:**
The change listener receives one object containing x/y/z angular velocities; it does not receive three positional numbers.

## Proposed Solution

**How to fix:**
Cache event values after verifying units, signs and orientation against the LibGDX contract.

**Binding changes needed (WX.java):**
Add lifecycle and one-result-object callback bindings, with numeric x/y/z properties on the result overlay.

**Implementation changes needed:**
Handle unsupported devices and startup failure, stop/unregister on disposal, suspend on hide and restore only if enabled. Keep getters free of synchronous native calls.

## Notes

Availability must reflect successful initialization. Coordinate conversion and device behavior remain unverified.
