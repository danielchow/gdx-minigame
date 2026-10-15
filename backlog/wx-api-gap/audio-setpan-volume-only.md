# Audio pan — only volume is applied

> **Status**: 📋 Researched
>
> **Severity**: 🟡 Medium (missing contract behavior)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Sound and music setPan discard pan and apply volume. Pooled sound voices use createInnerAudioContext(true), which selects useWebAudioImplement but still returns InnerAudioContext.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameAudio.java` (lines 42–58).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameAudio.java` (lines 252–268).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameAudio.java` (lines 362–366).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/MiniGameAudio.java` (lines 461–463).

**What happens at runtime:**
Pan requests never reach an audio panning operation; the volume parameter is applied.

## WeChat Mini Game API Reference

**WX API that can fix this:**
InnerAudioContext has no documented pan property. A separate WebAudioContext graph may support an implementation through createPanner.

**Doc path:**
- `references/wx-api-docs/media/audio/InnerAudioContext.md`.
- `references/wx-api-docs/media/audio/WebAudioContext.md`.
- `references/wx-api-docs/media/audio/WebAudioContext.createPanner.md`.

**API signature / key details:**
The checked-in docs list createPanner, not createStereoPanner. StereoPannerNode availability is unverified.

## Proposed Solution

**How to fix:**
Keep the documented fallback until a validated panner graph can preserve LibGDX pan/volume semantics. Prototype supported nodes before choosing an audio architecture.

**Binding changes needed (WX.java):**
If pursued, add separate WebAudioContext/node overlays; useWebAudioImplement alone does not expose a node graph.

**Implementation changes needed:**
Integrate validated panning with sound IDs, pooling, music, volume and lifecycle suspension. Avoid claiming a complete backend rewrite is necessarily required before prototyping.

## Notes

See [platform limitations](../../docs/limitations.md). Device panner support and application impact remain unverified.
