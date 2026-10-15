# Sound pitch — initial and per-playback values are ignored

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
play/loop discard their pitch argument and setPitch has no implementation. activePlaybacks maps IDs to pooled Voice objects, not WXAudioContext.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameAudio.java` (lines 233–268).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameAudio.java` (lines 356–359).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WXAudioContext.java` (lines 10–82).

**What happens at runtime:**
Requested pitch changes do not reach the audio context.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`InnerAudioContext.playbackRate`.

**Doc path:**
- `references/wx-api-docs/media/audio/InnerAudioContext.md`.

**API signature / key details:**
The snapshot documents a playback rate of 0.5–2.0, default 1, from base library 2.11.0. Rate changes need audible validation for the intended pitch behavior.

## Proposed Solution

**How to fix:**
Bind playbackRate, define out-of-range behavior, and pass initial pitch into startPlayback. Apply per-ID updates through voice.ctx.

**Binding changes needed (WX.java):**
Add playbackRate property accessors to WXAudioContext; use a capability fallback where needed.

**Implementation changes needed:**
Apply the chosen rate every time a Voice is acquired, including the default 1, so reused voices cannot inherit a previous playback rate. Preserve IDs, pause/resume and pool ownership.

## Notes

Validate release-compiled bindings and pooled reuse on devices. The source confirms ignored values; audible rate/pitch equivalence is unverified.
