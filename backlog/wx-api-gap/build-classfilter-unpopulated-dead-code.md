# Class filter — registered with no configured exclusions

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
The SPI registers MiniGameClassFilter. Its exclusion lists start empty and no repository callers of the add methods were found.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/plugins/MiniGameClassFilter.java` (lines 13–29).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/plugins/MiniGameClassFilter.java` (lines 69–100).
- `backends/backend-minigame/src/main/resources/META-INF/services/org.teavm.vm.spi.ElementFilter` (line 1).

**What happens at runtime:**
By default all elements pass. acceptClass records class names in a static diagnostic list; method/field checks do not append to that list. Build overhead is unmeasured.

## WeChat Mini Game API Reference

**WX API that can fix this:**
None.

**Doc path:**
Not applicable; this is a build/backend issue.

**API signature / key details:**
This is TeaVM build infrastructure. Empty exclusions do not demonstrate incorrect generated code.

## Proposed Solution

**How to fix:**
Confirm whether external build entry points intentionally populate these public static lists before removing infrastructure. Then remove unused registration/tracking or document and support the intended configuration lifecycle.

**Binding changes needed (WX.java):**
None.

**Implementation changes needed:**
Limit any change to the minigame filter and its SPI resource. Do not add speculative exclusions such as broad desktop packages without proving they are safe.

## Notes

Repository absence is not proof that downstream builders never use the public methods. Do not claim measured slowdown or a runtime crash.
