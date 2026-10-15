# Class transformer — broad Reflections roots need review

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
Reflections scans emulate, emu, com, org and emu/net before collecting @Emulate classes. This is broader than a clearly documented set of intended providers.

**Where in the codebase:**
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/plugins/MiniGameClassTransformer.java` (lines 39–69).

**What happens at runtime:**
The scan is confirmed. Wrong-class replacement and a material build slowdown remain unverified.

## WeChat Mini Game API Reference

**WX API that can fix this:**
None.

**Doc path:**
Not applicable; this is a build/backend issue.

**API signature / key details:**
Annotation providers can come from dependencies, including packages outside emu; package names alone do not prove an annotation is unintended.

## Proposed Solution

**How to fix:**
Inventory all @Emulate providers in project/dependency JARs and measure scan cost. Narrow roots or explicitly register providers only if the same required emulations are retained.

**Binding changes needed (WX.java):**
None.

**Implementation changes needed:**
Change only MiniGameClassTransformer if evidence supports it. Compare discovered annotations and generated behavior before and after scope changes.

## Notes

Do not edit backend-web or backend-shared. This is an investigation, not a proven one-line safe fix.
