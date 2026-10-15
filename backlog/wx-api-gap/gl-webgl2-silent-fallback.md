# GL30 request — silent WebGL1 fallback after splash context

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
When useGL30 is requested and webgl2 is unavailable, graphics silently creates WebGL1. The startup splash first obtains webgl on the same canvas, which may constrain the later context request.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameGraphics.java` (lines 81–98).
- `backends/backend-minigame/src/main/resources/templates/minigame/game.js` (lines 4–10).
- `backends/backend-minigame/src/main/resources/templates/minigame/first-screen.js` (lines 11–14).

**What happens at runtime:**
The application can run without GL30 despite requesting it. Whether the early splash context forces fallback on each WeChat client is unverified.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`Canvas.getContext` supports webgl and, on supported base libraries, webgl2.

**Doc path:**
- `references/wx-api-docs/render/canvas/Canvas.getContext.md`.

**API signature / key details:**
Graphics already constructs GLVersion from the actual context string. LibGDX 1.14.0 ApplicationType has WebGL, not a separate WebGL2 value.

## Proposed Solution

**How to fix:**
Define whether useGL30 means preferred or required. Log fallback or fail clearly when required, and align splash context selection with the application requirement before the first getContext.

**Binding changes needed (WX.java):**
No new WX.java binding is required.

**Implementation changes needed:**
Coordinate generated startup/splash templates and graphics context initialization; retain GLVersion(ApplicationType.WebGL, actualVersion, ...) and capability checks. Do not invent ApplicationType.WebGL2.

## Notes

Validate startup and fallback on device and DevTools, including clients without WebGL2. This note does not claim the splash constraint has been reproduced.
