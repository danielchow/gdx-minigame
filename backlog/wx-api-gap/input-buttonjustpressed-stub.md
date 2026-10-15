# isButtonJustPressed — always false

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
The per-button just-pressed getter always returns false, although touch-down already records justTouched and left-button state.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 73–89).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 159).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameInput.java` (lines 231–233).

**What happens at runtime:**
Polling isButtonJustPressed(LEFT) misses touch presses. Existing touch callbacks and justTouched remain available.

## WeChat Mini Game API Reference

**WX API that can fix this:**
No new WX API is needed.

**Doc path:**
Not applicable; this is a backend implementation issue.

**API signature / key details:**
The touch event and per-frame reset already provide the required timing.

## Proposed Solution

**How to fix:**
Track just-pressed state per button for the current frame, including a press released before polling.

**Binding changes needed (WX.java):**
None.

**Implementation changes needed:**
Mark the left button on its press transition, return the recorded flag, and clear it in reset with justTouched. Extend the same mechanism if native mouse buttons are added.

## Notes

Use the same frame boundary as existing input reset; verify down/up in one frame and multi-touch transitions.
