package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import emu.com.badlogic.gdx.graphics.g2d.Gdx2DPixmapNative;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.teavm.jso.JSBody;

/** Exercises pixmap buffer synchronization and ownership against the bundled native WASM. */
public final class PixmapBufferProbe {
    public static void main(String[] args) {
        Pixmap atlas = new Pixmap(1024, 1024, Pixmap.Format.RGBA8888);
        Pixmap glyph = new Pixmap(8, 8, Pixmap.Format.RGBA8888);
        atlas.setBlending(Pixmap.Blending.None);
        glyph.setBlending(Pixmap.Blending.None);
        glyph.setColor(0x11223344);
        glyph.fill();
        for (int i = 0; i < 100; i++) atlas.drawPixmap(glyph, i % 10 * 8, i / 10 * 8);
        check(atlas.getPixel(79, 79) == 0x11223344, "native atlas contents before exposure");
        check(atlasCopies() == 0, "packing must not copy the unexposed 4 MiB atlas");

        ByteBuffer retained = atlas.getPixels();
        check(atlasCopies() == 1, "first pixel read must copy the atlas exactly once");
        check(retained.getInt(0) == 0x11223344, "first buffer contains native pixels");
        check(atlas.getPixels() == retained && atlasCopies() == 1, "an unchanged atlas is not copied again");

        // An uploaded font page keeps its buffer; packing more glyphs must not copy the page each time.
        atlas.setColor(0x99aabbcc);
        atlas.drawPixel(0, 0);
        atlas.fillRectangle(0, 1, 2, 1);
        atlas.drawPixmap(glyph, 0, 2);
        for (int i = 0; i < 50; i++) atlas.drawPixmap(glyph, 512 + i % 10 * 8, 512 + i / 10 * 8);
        check(atlasCopies() == 1, "drawing on an exposed atlas must not copy it");
        check(atlas.getPixels() == retained, "buffer identity survives drawing");
        check(atlasCopies() == 2, "the next pixel read copies the atlas once");
        check(retained.getInt(0) == 0x99aabbcc, "pixel read sees drawPixel");
        check(retained.getInt(1024 * 4) == 0x99aabbcc, "pixel read sees fillRectangle");
        check(retained.getInt(1024 * 8) == 0x11223344, "pixel read sees drawPixmap");
        check(retained.getInt((520 * 1024 + 520) * 4) == 0x11223344, "pixel read sees later glyphs");
        atlas.setBlending(Pixmap.Blending.SourceOver);
        atlas.getPixels();
        check(atlasCopies() == 2, "changing blending does not invalidate the buffer");

        // setPixels must reach the native pixmap that drawing, getPixel and packing read.
        ByteBuffer replacement = BufferUtils.newByteBuffer(1024 * 1024 * 4);
        replacement.order(ByteOrder.BIG_ENDIAN).putInt(0, 0x0a0b0c0d);
        atlas.setPixels(replacement);
        check(atlas.getPixel(0, 0) == 0x0a0b0c0d, "setPixels reaches the native pixmap");

        int beforeFree = frees();
        atlas.dispose();
        check(frees() == beforeFree + 1, "exposed allocation freed only once");
        check(atlas.getPixels() == null, "disposed buffer cannot be recreated");
        glyph.dispose();

        // Exercise the native wrapper directly for its backend-only synchronization API.
        Gdx2DPixmapNative exposed = new Gdx2DPixmapNative(2, 2, 4);
        ByteBuffer direct = exposed.getBuffer();
        direct.putInt(4, 0x10203040);
        exposed.copyToHeap();
        check(exposed.getPixel(1, 0) == 0x10203040, "explicit buffer edits reach the native heap");
        beforeFree = frees();
        exposed.dispose();
        exposed.dispose();
        check(frees() == beforeFree + 1 && exposed.getBuffer() == null, "exposed native disposal is idempotent");

        Gdx2DPixmapNative unexposed = new Gdx2DPixmapNative(2, 2, 4);
        unexposed.clear(0x50607080);
        unexposed.copyToHeap();
        check(unexposed.getPixel(0, 0) == 0x50607080, "copyToHeap before exposure preserves native pixels");
        beforeFree = frees();
        unexposed.dispose();
        unexposed.dispose();
        check(frees() == beforeFree + 1, "unexposed allocation freed only once");
        check(unexposed.getBuffer() == null, "unexposed disposed buffer stays absent");

        // Original fixture: two opaque RGBA pixels, red then green; no external image asset.
        String hex = "89504e470d0a1a0a0000000d4948445200000002000000010806000000f4227f8a"
            + "0000000e49444154789c63f8cfc0f01f040110f803fd4e95c16f0000000049454e44ae426082";
        byte[] png = new byte[hex.length() / 2];
        for (int i = 0; i < png.length; i++) png[i] = (byte)Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        Pixmap decoded = new Pixmap(png, 0, png.length);
        check(decoded.getWidth() == 2 && decoded.getHeight() == 1, "encoded constructor dimensions");
        check(decoded.getPixels().getInt(0) == 0xff0000ff, "encoded constructor pixel buffer");
        decoded.dispose();
        beforeFree = frees();
        Gdx2DPixmapNative converted = new Gdx2DPixmapNative(png, 0, png.length, 3);
        check(frees() == beforeFree + 1, "conversion releases the original native allocation");
        check(converted.getFormat() == 3, "requested RGB888 conversion");
        ByteBuffer rgb = converted.getBuffer();
        check(rgb != null && rgb.capacity() == 6, "converted buffer uses the new format size");
        check((rgb.get(0) & 255) == 255 && rgb.get(1) == 0 && rgb.get(2) == 0
            && rgb.get(3) == 0 && (rgb.get(4) & 255) == 255 && rgb.get(5) == 0,
            "converted buffer preserves both source pixels");
        check(converted.getPixel(1, 0) == 0x00ff00ff, "converted native allocation remains owned");
        beforeFree = frees();
        converted.dispose();
        converted.dispose();
        check(frees() == beforeFree + 1 && converted.getBuffer() == null, "converted allocation disposed once");
        // Undecodable data and impossible allocations are catchable errors, not a native null dereference.
        checkThrows(() -> new Pixmap(new byte[] {1, 2, 3, 4, 5, 6, 7, 8}, 0, 8), "Couldn't load pixmap");
        // 24000^2 RGBA (2.3 GB) exceeds the engine's 2 GiB heap cap, so malloc fails without growing the heap.
        checkThrows(() -> new Gdx2DPixmapNative(24000, 24000, 4), "Couldn't allocate pixmap");
        complete();
    }

    private static void checkThrows(Runnable action, String message) {
        try {
            action.run();
        } catch (GdxRuntimeException expected) {
            check(expected.getMessage().startsWith(message), "unexpected error: " + expected.getMessage());
            return;
        }
        throw new AssertionError("expected GdxRuntimeException: " + message);
    }

    private static void check(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }

    @JSBody(script = "return globalThis.__pixmapProbe.atlasCopies;")
    private static native int atlasCopies();

    @JSBody(script = "return globalThis.__pixmapProbe.frees;")
    private static native int frees();

    @JSBody(script = "globalThis.__pixmapProbe.complete = true;")
    private static native void complete();
}
