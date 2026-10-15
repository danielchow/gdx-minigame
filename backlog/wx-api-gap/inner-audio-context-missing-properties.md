# InnerAudioContext — incomplete property and event coverage

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
WXAudioContext lacks playbackRate, startTime, autoplay, paused/buffered accessors and canplay/time-update events. Error callbacks discard data. offEnded and offError already exist.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/WXAudioContext.java` (lines 10–82).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/bindings/AudioCallback.java` (lines 6–8).

**What happens at runtime:**
Applications cannot access those optional capabilities through the overlay; audible effects depend on usage. Pitch and error-data gaps have dedicated notes.

## WeChat Mini Game API Reference

**WX API that can fix this:**
InnerAudioContext properties/events and `wx.setInnerAudioOption`.

**Doc path:**
- `references/wx-api-docs/media/audio/InnerAudioContext.md`.
- `references/wx-api-docs/media/audio/InnerAudioContext.onTimeUpdate.md`.
- `references/wx-api-docs/media/audio/wx.setInnerAudioOption.md`.

**API signature / key details:**
obeyMuteSwitch on the context is documented as ineffective since 2.3.0; use global audio options for that policy. onTimeUpdate has no documented result payload.

## Proposed Solution

**How to fix:**
Add capabilities needed by actual callers, using correct read-only/read-write properties and paired listener removal.

**Binding changes needed (WX.java):**
Extend WXAudioContext, with a separate typed error callback; add global audio options to WX only if required.

**Implementation changes needed:**
Preserve pooled listener ownership and avoid per-play listener accumulation. Read time properties in the time-update callback instead of expecting them in a result object.

## Notes

Coordinate [pitch support](audio-setpitch-noop.md) and [typed errors](callback-interfaces-lack-typed-parameters.md). Binding coverage alone is not evidence of a runtime defect.
