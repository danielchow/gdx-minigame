# Integer texture setters — call texParameterf

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
glTexParameteri and glTexParameteriv dispatch through texParameterf.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/WebGL20.java` (lines 860–866).

**What happens at runtime:**
The Java-to-WebGL API choice does not match the integer setter. Ordinary enum values survive numeric conversion; no valid call producing different rendering was identified.

## WeChat Mini Game API Reference

**WX API that can fix this:**
WebGL texParameteri.

**Doc path:**
- `references/wx-api-docs/render/canvas/RenderingContext.md`.

**API signature / key details:**
Integer and float entry points should match the requested API. A method mismatch alone is not evidence of visible rendering failure.

## Proposed Solution

**How to fix:**
Route integer setters to the integer WebGL method while preserving existing buffer consumption semantics.

**Binding changes needed (WX.java):**
No WX.java changes; use or add the appropriate minigame GL integer-method overlay.

**Implementation changes needed:**
Update both setters and compare resulting texture state for valid pnames/values. Preserve position behavior unless a separate contract issue is established.

## Notes

Treat as API-fidelity cleanup until a concrete behavioral difference is reproduced; do not retain an unproven rendering-breakage severity.
