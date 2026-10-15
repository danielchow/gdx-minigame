# Touch pressure — binary fallback only

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Pressure is always 1 for a touched pointer and 0 otherwise; touch force is never read.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 73–119).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 153–154).

**What happens at runtime:**
Pressure-sensitive input cannot distinguish light and firm touches.

## WeChat Mini Game API Reference

**WX API that can fix this:**
The existing touch events can supply `Touch.force` on supporting hardware.

**Doc path:**
- `references/wx-api-docs/device/touch-event/Touch.md`.

**API signature / key details:**
Force ranges from 0 to 1 and is only returned on hardware that supports it. A value of zero is valid.

## Proposed Solution

**How to fix:**
Cache force per mapped pointer when present, use 1 for a touch without force support, and reset to 0 on up/cancel.

**Binding changes needed (WX.java):**
Add a touch-force accessor that distinguishes an absent/non-numeric value from zero: `typeof touch.force === "number" ? touch.force : 1`. Do not use `touch.force || 1`.

**Implementation changes needed:**
Update touch-down/move processing and pressure getters; preserve pointer bounds and clear state when a touch ends.

## Notes

Hardware coverage and behavior in desktop touch emulation remain unverified.
