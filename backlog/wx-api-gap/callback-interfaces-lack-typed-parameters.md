# Callbacks — audio errors discard their result

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
AudioCallback has no parameters, including when used by onError, so native error details are discarded. Other generic JSObject callbacks retain their payload and can already be consumed.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/AudioCallback.java` (lines 6–8).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WXAudioContext.java` (lines 60–70).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameShare.java` (lines 207–214).

**What happens at runtime:**
Audio handlers cannot inspect errCode/errMsg through the current signature. Untyped touch, keyboard and lifecycle objects are a maintainability concern rather than demonstrated data loss.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`InnerAudioContext.onError`.

**Doc path:**
- `references/wx-api-docs/media/audio/InnerAudioContext.onError.md`.
- `references/wx-api-docs/media/audio/InnerAudioContext.onTimeUpdate.md`.

**API signature / key details:**
onError supplies a result with errCode/errMsg. The onTimeUpdate snapshot documents no payload; read currentTime/duration properties when needed.

## Proposed Solution

**How to fix:**
Introduce an AudioErrorCallback receiving one typed error object. Retain parameterless callbacks for events without result data. Add other typed overlays where they improve real consumers.

**Binding changes needed (WX.java):**
Change or overload onError and matching offError consistently, preserving listener identity. Do not change every AudioCallback into a mandatory result callback.

**Implementation changes needed:**
Update pooled Voice and music error handling to accept/log useful details without changing cleanup ownership. Keep lifecycle query consumers working.

## Notes

Sharing already extracts lifecycle query data. This is not a blocker for every callback consumer; coordinate with [audio binding coverage](inner-audio-context-missing-properties.md).
