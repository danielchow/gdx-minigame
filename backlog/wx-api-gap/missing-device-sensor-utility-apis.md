# Device APIs — contract gaps and optional extensions

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Sensor/orientation getters are stubbed. Bindings for keep-screen-on, battery, network type, memory warnings and global audio options were not found; these optional utilities are separate from LibGDX sensor contracts.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 132–137).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 205–216).

**What happens at runtime:**
Sensor getters provide fixed values. The effect of missing optional utilities depends on the application; sleep during normal play has not been demonstrated.

## WeChat Mini Game API Reference

**WX API that can fix this:**
Sensor listeners; `wx.setKeepScreenOn`, `wx.getBatteryInfo`, `wx.getNetworkType`, `wx.onMemoryWarning`, `wx.getPerformance` and `wx.setInnerAudioOption`.

**Doc path:**
- `references/wx-api-docs/device/screen/wx.setKeepScreenOn.md`.
- `references/wx-api-docs/device/battery/wx.getBatteryInfo.md`.
- `references/wx-api-docs/device/network/wx.getNetworkType.md`.
- `references/wx-api-docs/device/memory/wx.onMemoryWarning.md`.
- `references/wx-api-docs/base/performance/wx.getPerformance.md`.
- `references/wx-api-docs/media/audio/wx.setInnerAudioOption.md`.

**API signature / key details:**
Warnings do not measure heap usage. Global audio options affect audio policy; they are not per-context properties.

## Proposed Solution

**How to fix:**
Address [accelerometer](accelerometer-stub.md), [gyroscope](gyroscope-stub.md) and [orientation](orientation-compass-stub.md) contract work independently. Add optional utility bindings only for a defined application use case.

**Binding changes needed (WX.java):**
Use result-object callbacks, capability/failure handling and on/off lifecycle pairs where available. Avoid synchronous native queries in per-frame getters.

**Implementation changes needed:**
Expose optional APIs through an explicit backend extension rather than inventing LibGDX methods. Cache any queried state and refresh it through events.

## Notes

This is an umbrella for optional work, not a claim that every WX API must be wrapped. Performance needs and sleep behavior are unverified.
