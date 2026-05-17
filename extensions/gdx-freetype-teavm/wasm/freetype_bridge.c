/*
 * freetype_bridge.c — C bridge from FreeType to WASM32 for gdx-minigame.
 *
 * Adapted from libgdx JNI code in FreeType.java.
 * Adaptation: jlong → int (WASM32 pointers are 32-bit),
 *             JNI boolean return → int (0=false, 1=true),
 *             env->NewDirectByteBuffer → return raw pointer as int.
 */

#include <ft2build.h>
#include FT_FREETYPE_H
#include FT_STROKER_H
#include <stdlib.h>
#include <stdint.h>

static int g_lastErrorCode = 0;

/* ===== FreeType (2) ===== */

int c_FreeType_initFreeTypeJni() {
    FT_Library library = 0;
    FT_Error error = FT_Init_FreeType(&library);
    if (error) {
        g_lastErrorCode = error;
        return 0;
    }
    return (int)(uintptr_t)library;
}

int c_FreeType_getLastErrorCode() {
    return g_lastErrorCode;
}

/* ===== Library (3) ===== */

void c_Library_doneFreeType(int library) {
    FT_Done_FreeType((FT_Library)(uintptr_t)library);
}

int c_Library_newMemoryFace(int library, int data, int dataSize, int faceIndex) {
    FT_Face face = 0;
    FT_Error error = FT_New_Memory_Face(
        (FT_Library)(uintptr_t)library,
        (const FT_Byte*)(uintptr_t)data,
        dataSize,
        faceIndex,
        &face
    );
    if (error) {
        g_lastErrorCode = error;
        return 0;
    }
    return (int)(uintptr_t)face;
}

int c_Library_strokerNew(int library) {
    FT_Stroker stroker;
    FT_Error error = FT_Stroker_New((FT_Library)(uintptr_t)library, &stroker);
    if (error) {
        g_lastErrorCode = error;
        return 0;
    }
    return (int)(uintptr_t)stroker;
}

/* ===== Face (20) ===== */

void c_Face_doneFace(int face) {
    FT_Done_Face((FT_Face)(uintptr_t)face);
}

int c_Face_getFaceFlags(int face) {
    return ((FT_Face)(uintptr_t)face)->face_flags;
}

int c_Face_getStyleFlags(int face) {
    return ((FT_Face)(uintptr_t)face)->style_flags;
}

int c_Face_getNumGlyphs(int face) {
    return ((FT_Face)(uintptr_t)face)->num_glyphs;
}

int c_Face_getAscender(int face) {
    return ((FT_Face)(uintptr_t)face)->ascender;
}

int c_Face_getDescender(int face) {
    return ((FT_Face)(uintptr_t)face)->descender;
}

int c_Face_getHeight(int face) {
    return ((FT_Face)(uintptr_t)face)->height;
}

int c_Face_getMaxAdvanceWidth(int face) {
    return ((FT_Face)(uintptr_t)face)->max_advance_width;
}

int c_Face_getMaxAdvanceHeight(int face) {
    return ((FT_Face)(uintptr_t)face)->max_advance_height;
}

int c_Face_getUnderlinePosition(int face) {
    return ((FT_Face)(uintptr_t)face)->underline_position;
}

int c_Face_getUnderlineThickness(int face) {
    return ((FT_Face)(uintptr_t)face)->underline_thickness;
}

int c_Face_selectSize(int face, int strike_index) {
    return !FT_Select_Size((FT_Face)(uintptr_t)face, strike_index);
}

int c_Face_setCharSize(int face, int charWidth, int charHeight, int horzResolution, int vertResolution) {
    return !FT_Set_Char_Size((FT_Face)(uintptr_t)face, charWidth, charHeight, horzResolution, vertResolution);
}

int c_Face_setPixelSizes(int face, int pixelWidth, int pixelHeight) {
    return !FT_Set_Pixel_Sizes((FT_Face)(uintptr_t)face, pixelWidth, pixelHeight);
}

int c_Face_loadGlyph(int face, int glyphIndex, int loadFlags) {
    return !FT_Load_Glyph((FT_Face)(uintptr_t)face, glyphIndex, loadFlags);
}

int c_Face_loadChar(int face, int charCode, int loadFlags) {
    return !FT_Load_Char((FT_Face)(uintptr_t)face, charCode, loadFlags);
}

int c_Face_getGlyph(int face) {
    return (int)(uintptr_t)((FT_Face)(uintptr_t)face)->glyph;
}

int c_Face_getSize(int face) {
    return (int)(uintptr_t)((FT_Face)(uintptr_t)face)->size;
}

int c_Face_hasKerning(int face) {
    return FT_HAS_KERNING(((FT_Face)(uintptr_t)face));
}

int c_Face_getKerning(int face, int leftGlyph, int rightGlyph, int kernMode) {
    FT_Vector kerning;
    FT_Error error = FT_Get_Kerning((FT_Face)(uintptr_t)face, leftGlyph, rightGlyph, kernMode, &kerning);
    if (error) return 0;
    return kerning.x;
}

int c_Face_getCharIndex(int face, int charCode) {
    return FT_Get_Char_Index((FT_Face)(uintptr_t)face, charCode);
}

/* ===== Size (1) ===== */

int c_Size_getMetrics(int address) {
    return (int)(uintptr_t)&((FT_Size)(uintptr_t)address)->metrics;
}

/* ===== SizeMetrics (8) ===== */

int c_SizeMetrics_getXppem(int metrics) {
    return ((FT_Size_Metrics*)(uintptr_t)metrics)->x_ppem;
}

int c_SizeMetrics_getYppem(int metrics) {
    return ((FT_Size_Metrics*)(uintptr_t)metrics)->y_ppem;
}

int c_SizeMetrics_getXscale(int metrics) {
    return ((FT_Size_Metrics*)(uintptr_t)metrics)->x_scale;
}

int c_SizeMetrics_getYscale(int metrics) {
    return ((FT_Size_Metrics*)(uintptr_t)metrics)->y_scale;
}

int c_SizeMetrics_getAscender(int metrics) {
    return ((FT_Size_Metrics*)(uintptr_t)metrics)->ascender;
}

int c_SizeMetrics_getDescender(int metrics) {
    return ((FT_Size_Metrics*)(uintptr_t)metrics)->descender;
}

int c_SizeMetrics_getHeight(int metrics) {
    return ((FT_Size_Metrics*)(uintptr_t)metrics)->height;
}

int c_SizeMetrics_getMaxAdvance(int metrics) {
    return ((FT_Size_Metrics*)(uintptr_t)metrics)->max_advance;
}

/* ===== GlyphSlot (12) ===== */

int c_GlyphSlot_getMetrics(int slot) {
    return (int)(uintptr_t)&((FT_GlyphSlot)(uintptr_t)slot)->metrics;
}

int c_GlyphSlot_getLinearHoriAdvance(int slot) {
    return ((FT_GlyphSlot)(uintptr_t)slot)->linearHoriAdvance;
}

int c_GlyphSlot_getLinearVertAdvance(int slot) {
    return ((FT_GlyphSlot)(uintptr_t)slot)->linearVertAdvance;
}

int c_GlyphSlot_getAdvanceX(int slot) {
    return ((FT_GlyphSlot)(uintptr_t)slot)->advance.x;
}

int c_GlyphSlot_getAdvanceY(int slot) {
    return ((FT_GlyphSlot)(uintptr_t)slot)->advance.y;
}

int c_GlyphSlot_getFormat(int slot) {
    return ((FT_GlyphSlot)(uintptr_t)slot)->format;
}

int c_GlyphSlot_getBitmap(int slot) {
    FT_GlyphSlot glyph = ((FT_GlyphSlot)(uintptr_t)slot);
    return (int)(uintptr_t)&(glyph->bitmap);
}

int c_GlyphSlot_getBitmapLeft(int slot) {
    return ((FT_GlyphSlot)(uintptr_t)slot)->bitmap_left;
}

int c_GlyphSlot_getBitmapTop(int slot) {
    return ((FT_GlyphSlot)(uintptr_t)slot)->bitmap_top;
}

int c_GlyphSlot_renderGlyph(int slot, int renderMode) {
    return !FT_Render_Glyph((FT_GlyphSlot)(uintptr_t)slot, (FT_Render_Mode)renderMode);
}

int c_GlyphSlot_getGlyph(int glyphSlot) {
    FT_Glyph glyph;
    FT_Error error = FT_Get_Glyph((FT_GlyphSlot)(uintptr_t)glyphSlot, &glyph);
    if (error) {
        g_lastErrorCode = error;
        return 0;
    }
    return (int)(uintptr_t)glyph;
}

/* ===== Glyph (6) ===== */

void c_Glyph_done(int glyph) {
    FT_Done_Glyph((FT_Glyph)(uintptr_t)glyph);
}

int c_Glyph_strokeBorder(int glyph, int stroker, int inside) {
    FT_Glyph border_glyph = (FT_Glyph)(uintptr_t)glyph;
    FT_Glyph_StrokeBorder(&border_glyph, (FT_Stroker)(uintptr_t)stroker, inside, 1);
    return (int)(uintptr_t)border_glyph;
}

int c_Glyph_toBitmap(int glyph, int renderMode) {
    FT_Glyph bitmap = (FT_Glyph)(uintptr_t)glyph;
    FT_Error error = FT_Glyph_To_Bitmap(&bitmap, (FT_Render_Mode)renderMode, NULL, 1);
    if (error) {
        g_lastErrorCode = error;
        return 0;
    }
    return (int)(uintptr_t)bitmap;
}

int c_Glyph_getBitmap(int glyph) {
    FT_BitmapGlyph glyph_bitmap = ((FT_BitmapGlyph)(uintptr_t)glyph);
    return (int)(uintptr_t)&(glyph_bitmap->bitmap);
}

int c_Glyph_getLeft(int glyph) {
    FT_BitmapGlyph glyph_bitmap = ((FT_BitmapGlyph)(uintptr_t)glyph);
    return glyph_bitmap->left;
}

int c_Glyph_getTop(int glyph) {
    FT_BitmapGlyph glyph_bitmap = ((FT_BitmapGlyph)(uintptr_t)glyph);
    return glyph_bitmap->top;
}

/* ===== Bitmap (8) ===== */

int c_Bitmap_getRows(int bitmap) {
    return ((FT_Bitmap*)(uintptr_t)bitmap)->rows;
}

int c_Bitmap_getWidth(int bitmap) {
    return ((FT_Bitmap*)(uintptr_t)bitmap)->width;
}

int c_Bitmap_getPitch(int bitmap) {
    return ((FT_Bitmap*)(uintptr_t)bitmap)->pitch;
}

int c_Bitmap_getBufferAddress(int bitmap) {
    FT_Bitmap* bmp = (FT_Bitmap*)(uintptr_t)bitmap;
    return (int)(uintptr_t)bmp->buffer;
}

int c_Bitmap_getBufferSize(int bitmap) {
    FT_Bitmap* bmp = (FT_Bitmap*)(uintptr_t)bitmap;
    return bmp->rows * abs(bmp->pitch);
}

int c_Bitmap_getNumGray(int bitmap) {
    return ((FT_Bitmap*)(uintptr_t)bitmap)->num_grays;
}

int c_Bitmap_getPixelMode(int bitmap) {
    return ((FT_Bitmap*)(uintptr_t)bitmap)->pixel_mode;
}

/* ===== GlyphMetrics (8) ===== */

int c_GlyphMetrics_getWidth(int metrics) {
    return ((FT_Glyph_Metrics*)(uintptr_t)metrics)->width;
}

int c_GlyphMetrics_getHeight(int metrics) {
    return ((FT_Glyph_Metrics*)(uintptr_t)metrics)->height;
}

int c_GlyphMetrics_getHoriBearingX(int metrics) {
    return ((FT_Glyph_Metrics*)(uintptr_t)metrics)->horiBearingX;
}

int c_GlyphMetrics_getHoriBearingY(int metrics) {
    return ((FT_Glyph_Metrics*)(uintptr_t)metrics)->horiBearingY;
}

int c_GlyphMetrics_getHoriAdvance(int metrics) {
    return ((FT_Glyph_Metrics*)(uintptr_t)metrics)->horiAdvance;
}

int c_GlyphMetrics_getVertBearingX(int metrics) {
    return ((FT_Glyph_Metrics*)(uintptr_t)metrics)->vertBearingX;
}

int c_GlyphMetrics_getVertBearingY(int metrics) {
    return ((FT_Glyph_Metrics*)(uintptr_t)metrics)->vertBearingY;
}

int c_GlyphMetrics_getVertAdvance(int metrics) {
    return ((FT_Glyph_Metrics*)(uintptr_t)metrics)->vertAdvance;
}

/* ===== Stroker (2) ===== */

void c_Stroker_set(int stroker, int radius, int lineCap, int lineJoin, int miterLimit) {
    FT_Stroker_Set(
        (FT_Stroker)(uintptr_t)stroker,
        radius,
        (FT_Stroker_LineCap)lineCap,
        (FT_Stroker_LineJoin)lineJoin,
        miterLimit
    );
}

void c_Stroker_done(int stroker) {
    FT_Stroker_Done((FT_Stroker)(uintptr_t)stroker);
}
