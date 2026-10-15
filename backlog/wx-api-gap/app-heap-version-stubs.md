# Application diagnostics — zero heap and version values

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
getJavaHeap, getNativeHeap and getVersion return zero. Heap measurement and optional SDK-version reporting are different capabilities.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameApplication.java` (lines 417–428).

**What happens at runtime:**
Callers receive no heap measurement or backend version value. Application-specific effects have not been measured.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`wx.onMemoryWarning` provides warnings; `wx.getAppBaseInfo` exposes SDKVersion. Neither is a direct replacement for all three methods.

**Doc path:**
- `references/wx-api-docs/device/memory/wx.onMemoryWarning.md`.
- `references/wx-api-docs/base/system/wx.getAppBaseInfo.md`.

**API signature / key details:**
Memory warnings are events, not byte counts. SDKVersion is a dotted string, not an established LibGDX Application.getVersion integer.

## Proposed Solution

**How to fix:**
Document unavailable heap measurements and optionally expose warnings plus the SDK string through a backend extension. Establish the LibGDX version contract before changing getVersion.

**Binding changes needed (WX.java):**
Optional memory-warning on/off and app-base-info bindings, with capability and failure handling.

**Implementation changes needed:**
Keep unavailable measurements explicit. Do not invent a packed SDK integer or report nonstandard performance.memory values as Java/native heap sizes.

## Notes

The web backend also returns zero for these methods. See [platform limitations](../../docs/limitations.md); no memory-query API was identified in the checked-in references.
