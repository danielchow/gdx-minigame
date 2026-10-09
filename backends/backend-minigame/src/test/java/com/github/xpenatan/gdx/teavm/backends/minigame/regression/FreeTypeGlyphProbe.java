package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import org.teavm.jso.JSBody;
import org.teavm.jso.typedarrays.Int8Array;

/**
 * Renders a glyph through the bundled FreeType WASM (see freetype-glyph.test.mjs). Bitmap.getPixmap()'s fast path
 * (white, grayscale, gamma 1) must reach the native pixmap that font packing draws from, not only the Java buffer.
 */
public final class FreeTypeGlyphProbe {
    public static void main(String[] args) {
        byte[] font = fontBytes().copyToJavaArray();
        FreeType.Library library = FreeType.initFreeType();
        FreeType.Face face = library.newMemoryFace(font, font.length, 0);
        face.setPixelSizes(0, 32);
        face.loadChar('A', FreeType.FT_LOAD_DEFAULT);
        FreeType.GlyphSlot slot = face.getGlyph();
        slot.renderGlyph(FreeType.FT_RENDER_MODE_NORMAL);
        FreeType.Bitmap bitmap = slot.getBitmap();
        record("pixelModeGray", bitmap.getPixelMode() == FreeType.FT_PIXEL_MODE_GRAY ? 1 : 0);
        record("rowBytesIsWidth", Math.abs(bitmap.getPitch()) == bitmap.getWidth() ? 1 : 0);

        Pixmap pixmap = bitmap.getPixmap(Pixmap.Format.RGBA8888, Color.WHITE, 1f);
        int covered = 0;
        for (int y = 0; y < pixmap.getHeight(); y++) {
            for (int x = 0; x < pixmap.getWidth(); x++) {
                if ((pixmap.getPixel(x, y) & 0xff) != 0) covered++;
            }
        }
        record("width", pixmap.getWidth());
        record("height", pixmap.getHeight());
        record("covered", covered);
        pixmap.dispose();
        face.dispose();
        library.dispose();
        record("complete", 1);
    }

    @JSBody(script = "return globalThis.__fontBytes;")
    private static native Int8Array fontBytes();

    @JSBody(params = {"name", "value"}, script = "globalThis.__glyphProbe[name] = value;")
    private static native void record(String name, int value);
}
