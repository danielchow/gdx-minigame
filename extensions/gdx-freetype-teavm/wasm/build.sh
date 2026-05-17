#!/bin/bash
# build.sh — Compile FreeType C library + JNI bridge to WASM for gdx-minigame
#
# Prerequisites:
#   1. Emscripten SDK installed and activated
#      (https://emscripten.org/docs/getting_started/downloads.html)
#      source ~/Develop/GitHub/emsdk/emsdk_env.sh
#
#   2. FreeType 2.14.3 source at FREETYPE_SRC (default: ~/Develop/GitHub/freetype)
#
# Output:
#   output/freetype.js   — WASM glue code (~21KB)
#   output/freetype.wasm — WASM binary (~593KB)
#
# After building, copy outputs to src/main/resources/:
#   cp output/freetype.js ../src/main/resources/freetype.js
#   cp output/freetype.wasm ../src/main/resources/freetype.wasm
#
# The freetype-loader.js is hand-maintained in src/main/resources/ (not generated).

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FREETYPE_SRC="${FREETYPE_SRC:-$HOME/Develop/GitHub/freetype}"
BUILD_DIR="$SCRIPT_DIR/build"
OUTPUT_DIR="$SCRIPT_DIR/output"

# Verify emscripten
if ! command -v emcc &>/dev/null; then
    echo "ERROR: emcc not found. Install and activate Emscripten SDK first:"
    echo "  git clone https://github.com/emscripten-core/emsdk.git ~/Develop/GitHub/emsdk"
    echo "  cd ~/Develop/GitHub/emsdk && ./emsdk install latest && ./emsdk activate latest"
    echo "  source ~/Develop/GitHub/emsdk/emsdk_env.sh"
    exit 1
fi

# Verify FreeType source
if [ ! -f "$FREETYPE_SRC/include/freetype/freetype.h" ]; then
    echo "ERROR: FreeType source not found at $FREETYPE_SRC"
    echo "Set FREETYPE_SRC to the FreeType source directory."
    echo "  git clone https://gitlab.freedesktop.org/freetype/freetype.git ~/Develop/GitHub/freetype"
    exit 1
fi

rm -rf "$BUILD_DIR" "$OUTPUT_DIR"
mkdir -p "$BUILD_DIR" "$OUTPUT_DIR"

echo "=== Building FreeType static library (emcmake) ==="
cd "$BUILD_DIR"
emcmake cmake "$FREETYPE_SRC" \
    -DCMAKE_BUILD_TYPE=Release \
    -DFT_DISABLE_BROTLI=ON \
    -DFT_DISABLE_HARFBUZZ=ON \
    -DFT_DISABLE_PNG=ON \
    -DFT_DISABLE_BZIP2=ON \
    -DFT_DISABLE_ZLIB=ON \
    -DBUILD_SHARED_LIBS=OFF
emmake make -j"$(sysctl -n hw.ncpu 2>/dev/null || nproc)"

echo ""
echo "=== Linking bridge + FreeType into WASM module ==="

# All _c_* bridge functions + Emscripten builtins.
# C functions are named c_* (no leading underscore); Emscripten adds a _
# prefix in JS, so EXPORTED_FUNCTIONS uses _c_* (which becomes Module._c_*)
# matching what the Java @JSBody annotations call.
EXPORTED_FUNCTIONS="[
    '_malloc','_free',
    '_c_FreeType_initFreeTypeJni','_c_FreeType_getLastErrorCode',
    '_c_Library_doneFreeType','_c_Library_newMemoryFace','_c_Library_strokerNew',
    '_c_Face_doneFace','_c_Face_getFaceFlags','_c_Face_getStyleFlags',
    '_c_Face_getNumGlyphs','_c_Face_getAscender','_c_Face_getDescender',
    '_c_Face_getHeight','_c_Face_getMaxAdvanceWidth','_c_Face_getMaxAdvanceHeight',
    '_c_Face_getUnderlinePosition','_c_Face_getUnderlineThickness',
    '_c_Face_selectSize','_c_Face_setCharSize','_c_Face_setPixelSizes',
    '_c_Face_loadGlyph','_c_Face_loadChar','_c_Face_getGlyph','_c_Face_getSize',
    '_c_Face_hasKerning','_c_Face_getKerning','_c_Face_getCharIndex',
    '_c_Size_getMetrics',
    '_c_SizeMetrics_getXppem','_c_SizeMetrics_getYppem',
    '_c_SizeMetrics_getXscale','_c_SizeMetrics_getYscale',
    '_c_SizeMetrics_getAscender','_c_SizeMetrics_getDescender',
    '_c_SizeMetrics_getHeight','_c_SizeMetrics_getMaxAdvance',
    '_c_GlyphSlot_getMetrics','_c_GlyphSlot_getLinearHoriAdvance',
    '_c_GlyphSlot_getLinearVertAdvance','_c_GlyphSlot_getAdvanceX',
    '_c_GlyphSlot_getAdvanceY','_c_GlyphSlot_getFormat',
    '_c_GlyphSlot_getBitmap','_c_GlyphSlot_getBitmapLeft',
    '_c_GlyphSlot_getBitmapTop','_c_GlyphSlot_renderGlyph',
    '_c_GlyphSlot_getGlyph',
    '_c_Glyph_done','_c_Glyph_strokeBorder','_c_Glyph_toBitmap',
    '_c_Glyph_getBitmap','_c_Glyph_getLeft','_c_Glyph_getTop',
    '_c_Bitmap_getRows','_c_Bitmap_getWidth','_c_Bitmap_getPitch',
    '_c_Bitmap_getBufferAddress','_c_Bitmap_getBufferSize',
    '_c_Bitmap_getNumGray','_c_Bitmap_getPixelMode',
    '_c_GlyphMetrics_getWidth','_c_GlyphMetrics_getHeight',
    '_c_GlyphMetrics_getHoriBearingX','_c_GlyphMetrics_getHoriBearingY',
    '_c_GlyphMetrics_getHoriAdvance',
    '_c_GlyphMetrics_getVertBearingX','_c_GlyphMetrics_getVertBearingY',
    '_c_GlyphMetrics_getVertAdvance',
    '_c_Stroker_set','_c_Stroker_done'
]"

emcc "$SCRIPT_DIR/freetype_bridge.c" \
    "$BUILD_DIR/libfreetype.a" \
    -I"$FREETYPE_SRC/include" \
    -o "$OUTPUT_DIR/freetype.js" \
    -s WASM=1 \
    -s MODULARIZE=1 \
    -s EXPORT_NAME="createFreeTypeModule" \
    -s EXPORTED_FUNCTIONS="$EXPORTED_FUNCTIONS" \
    -s EXPORTED_RUNTIME_METHODS="['writeArrayToMemory','ccall','cwrap','HEAP8','HEAPU8','setValue','getValue']" \
    -s INITIAL_MEMORY=4194304 \
    -s ALLOW_MEMORY_GROWTH=1 \
    -s MAXIMUM_MEMORY=16777216 \
    -s ENVIRONMENT='web' \
    -s FILESYSTEM=0 \
    -O3 \
    -flto

echo ""
echo "=== Build complete ==="
ls -lh "$OUTPUT_DIR/"
echo ""
echo "To update the extension resources, run:"
echo "  cp $OUTPUT_DIR/freetype.js  $SCRIPT_DIR/../src/main/resources/freetype.js"
echo "  cp $OUTPUT_DIR/freetype.wasm $SCRIPT_DIR/../src/main/resources/freetype.wasm"
