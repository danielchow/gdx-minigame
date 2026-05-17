# FreeType WASM Build

This directory contains the build source for recompiling FreeType from asm.js to WASM.

## Files

| File | Purpose |
|------|---------|
| `freetype_bridge.c` | C bridge — 75 `_c_*` functions adapted from libgdx JNI (`libgdx/extensions/gdx-freetype/src/.../FreeType.java` inline JNI comments) |
| `build.sh` | Emscripten build script — compiles FreeType 2.14.3 + bridge into WASM module |

## Build Artifacts (generated, not tracked)

| Output | Size | Purpose |
|--------|------|---------|
| `output/freetype.js` | ~21KB | Emscripten WASM glue code |
| `output/freetype.wasm` | ~593KB | FreeType WASM binary |

These are copied to `src/main/resources/` and shipped in the JAR.

The `freetype-loader.js` (in `src/main/resources/`) is hand-written, not generated.

## Prerequisites

1. **Emscripten SDK** — https://emscripten.org/docs/getting_started/downloads.html
2. **FreeType 2.14.3 source** — `git clone https://gitlab.freedesktop.org/freetype/freetype.git`

## Rebuilding

```bash
source ~/Develop/GitHub/emsdk/emsdk_env.sh   # activate emsdk
bash build.sh                                  # builds output/freetype.js + output/freetype.wasm
cp output/freetype.js ../src/main/resources/freetype.js
cp output/freetype.wasm ../src/main/resources/freetype.wasm
```

Then publish and rebuild:

```bash
cd /Users/daniel/workspace/gdx-minigame
./gradlew :extensions:gdx-freetype-teavm:publishToMavenLocal :backends:backend-minigame:publishToMavenLocal
```

## Architecture

```
FreeType 2.14.3 (C library)
    ↓ emcmake cmake + emcc
freetype.wasm — native WASM binary with c_* exports
    ↓ Emscripten glue
freetype.js — JS wrapper exporting Module._c_* via createFreeTypeModule()
    ↓ freetype-loader.js
globalThis.Module._c_* — bridged to shared Module for TeaVM Java @JSBody calls
```

The C bridge functions (`c_*` in C, `_c_*` in JS) are thin wrappers around FreeType API calls.
They were derived from libgdx's `FreeType.java` JNI inline C code, adapted for WASM32 (int for pointers).
