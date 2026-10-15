# GL queries — implementable state queries still throw

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Several GL20/WebGL2 query overloads throw rather than returning state. This mixes implementable WebGL queries with desktop APIs that WebGL cannot represent.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/WebGL20.java` (lines 466–485).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/WebGL20.java` (lines 585–668).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/WebGL30.java` (lines 370–397).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/WebGL30.java` (lines 591–605).

**What happens at runtime:**
Calling a listed throwing overload raises an exception; whether it reaches an application crash depends on its caller. No affected LibGDX subsystem or frequency ranking was demonstrated.

## WeChat Mini Game API Reference

**WX API that can fix this:**
WebGL state queries through Canvas.getContext, including getBufferParameter, getTexParameter, getUniform and getVertexAttrib.

**Doc path:**
- `references/wx-api-docs/render/canvas/RenderingContext.md`.
- `references/wx-api-docs/render/canvas/Canvas.getContext.md`.

**API signature / key details:**
GL20 candidates include attached shaders, booleans, buffer/renderbuffer parameters, shader precision, texture parameters, uniforms and vertex attributes. WebGL2 candidates include the active-block-name buffer overload, unsigned uniforms and integer attributes.

## Proposed Solution

**How to fix:**
Implement queries with a valid WebGL equivalent, one method at a time. Classify return types by pname, convert object handles through existing ID maps, and copy scalar/vector results with correct buffer bounds and position semantics.

**Binding changes needed (WX.java):**
No WX.java changes. Add typed query helpers to minigame GL overlays where TeaVM lacks the needed return shape.

**Implementation changes needed:**
Preserve explicit unsupported behavior for native pointer/buffer mapping and binary-shader contracts that WebGL cannot satisfy. A buffer binding offset is not a native pointer. Do not silently skip a query or return fabricated state.

## Notes

The source establishes reachable exceptions, not that normal rendering always calls them. Prioritize actual callers and validate outputs against set state; leave backend-web unchanged.
