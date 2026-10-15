# WASM layout — duplicated extracted path

> **Status**: 📋 Researched
>
> **Severity**: 🔵 Low (cleanup or optional capability)

Verified: 2026-10-15 against 0961fbf2601e9de24f14f879000565ea5d91a716 (source inspection; no runtime reproduction).

## Problem

**What is broken or missing:**
The adapter and Java extractor separately specify subpackages/engine/gdx.wasm. The path agrees today; the issue is maintenance coupling.

**Where in the codebase:**
- `backends/backend-minigame/src/main/resources/templates/minigame/adapter.js` (lines 82–86).
- `backends/backend-minigame/src/main/java/com/github/xpenatan/gdx/teavm/backends/minigame/config/backend/MiniGameBackend.java` (lines 253–283).

**What happens at runtime:**
A future one-sided layout change could make runtime instantiation miss the extracted file. No current path mismatch was found; extraction already fails loudly.

## WeChat Mini Game API Reference

**WX API that can fix this:**
`WXWebAssembly.instantiate` consumes the packaged path.

**Doc path:**
See the checked-in adapter and extractor above; no new WX binding is required.

**API signature / key details:**
The extracted engine WASM is outside assets/; asset extension renaming does not establish another fallback path for it.

## Proposed Solution

**How to fix:**
Use one backend layout constant passed through an adapter template placeholder, or enforce the relationship with a build-output assertion.

**Binding changes needed (WX.java):**
None.

**Implementation changes needed:**
Keep extractor output, adapter substitution and subpackage entries consistent. Validate the expected path in final output; do not add arbitrary path fallbacks.

## Notes

All generated JS remains in external templates. This is a maintenance issue, not a current startup failure.
