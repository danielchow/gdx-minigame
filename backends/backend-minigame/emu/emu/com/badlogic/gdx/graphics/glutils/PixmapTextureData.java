package emu.com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.xpenatan.gdx.teavm.backends.minigame.WebGL20;
import emu.com.badlogic.gdx.graphics.g2d.Gdx2DPixmapNative;
import emu.com.badlogic.gdx.graphics.g2d.PixmapNativeInterface;

/**
 * Emulated PixmapTextureData that uploads directly from Gdx.HEAPU8 when native pixmap memory is
 * authoritative (e.g., FreeType PixmapPacker font pages), avoiding 1 MiB+ ByteBuffer allocations
 * and heap-to-JS copies on every font page upload.
 */
public class PixmapTextureData implements TextureData {
    final Pixmap pixmap;
    final Format format;
    final boolean useMipMaps;
    final boolean disposePixmap;
    final boolean managed;

    public PixmapTextureData(Pixmap pixmap, Format format, boolean useMipMaps, boolean disposePixmap) {
        this(pixmap, format, useMipMaps, disposePixmap, false);
    }

    public PixmapTextureData(Pixmap pixmap, Format format, boolean useMipMaps, boolean disposePixmap, boolean managed) {
        this.pixmap = pixmap;
        this.format = format == null ? pixmap.getFormat() : format;
        this.useMipMaps = useMipMaps;
        this.disposePixmap = disposePixmap;
        this.managed = managed;
    }

    @Override
    public boolean disposePixmap() {
        return disposePixmap;
    }

    @Override
    public Pixmap consumePixmap() {
        return pixmap;
    }

    @Override
    public int getWidth() {
        return pixmap.getWidth();
    }

    @Override
    public int getHeight() {
        return pixmap.getHeight();
    }

    @Override
    public Format getFormat() {
        return format;
    }

    @Override
    public boolean useMipMaps() {
        return useMipMaps;
    }

    @Override
    public boolean isManaged() {
        return managed;
    }

    private boolean canUploadDirectly() {
        if (useMipMaps || pixmap == null || format != pixmap.getFormat()) return false;
        if (!(Gdx.gl20 instanceof WebGL20) || pixmap.getGLType() != GL20.GL_UNSIGNED_BYTE) return false;
        Gdx2DPixmapNative nativePixmap = ((PixmapNativeInterface) (Object) pixmap).getNative();
        return nativePixmap != null && nativePixmap.canUploadHeapDirectly();
    }

    @Override
    public TextureDataType getType() {
        return canUploadDirectly() ? TextureDataType.Custom : TextureDataType.Pixmap;
    }

    @Override
    public void consumeCustomData(int target) {
        if (!canUploadDirectly()) {
            throw new GdxRuntimeException("This TextureData implementation does not upload data itself");
        }
        try {
            Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
            ((PixmapNativeInterface) (Object) pixmap).getNative().uploadToGL(
                ((WebGL20) Gdx.gl20).gl, target, pixmap.getGLInternalFormat(), pixmap.getGLFormat(), pixmap.getGLType());
        } finally {
            if (disposePixmap) pixmap.dispose();
        }
    }

    @Override
    public boolean isPrepared() {
        return true;
    }

    @Override
    public void prepare() {
        throw new GdxRuntimeException("prepare() must not be called on a PixmapTextureData instance as it is already prepared.");
    }
}
