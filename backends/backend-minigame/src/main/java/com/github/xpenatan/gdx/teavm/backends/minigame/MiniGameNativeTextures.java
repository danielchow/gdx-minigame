package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import java.util.LinkedHashMap;
import java.util.Map;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Startup-only native PNG decoding. Explicit opt-in promises that a file's textures will not
 * expose Pixmaps. Ordinary Pixmap/FileTextureData callers and unlisted files are unchanged.
 * No asynchronous method is reachable from texture construction, upload or managed reload.
 */
public final class MiniGameNativeTextures {
    private final MiniGameFiles files;
    private final Map<String, Entry> ready = new LinkedHashMap<>();
    private int expiry = -1;

    MiniGameNativeTextures(MiniGameFiles files) {
        this.files = files;
    }

    /** Called only during application initialization, before any application frame may run. */
    void prepare(String[] paths, int timeoutMillis, Runnable complete) {
        if (timeoutMillis <= 0) throw new IllegalArgumentException("nativeTextureTimeoutMillis must be positive");
        Map<String, Entry> candidates = new LinkedHashMap<>();
        for (String path : paths) {
            if (path == null || candidates.containsKey(path)) continue;
            MiniGameFileHandle file = (MiniGameFileHandle) files.internal(path);
            try {
                String physical = file.nativeImagePath(files);
                if (physical == null) continue;
                int[] size = rgbaPngSize(file.readBytes());
                if (size != null) candidates.put(file.path(), new Entry(file, physical, size[0], size[1]));
            } catch (RuntimeException error) {
                // The real loader will still report a missing/corrupt file in its usual way.
                fallback(path, "file unavailable");
            }
        }
        if (candidates.isEmpty()) {
            complete.run();
            return;
        }
        int[] remaining = {candidates.size()};
        for (Entry entry : candidates.values()) {
            decode(entry.physicalPath, entry.width, entry.height, timeoutMillis, (image, failure) -> {
                if (image != null) {
                    entry.image = image;
                    ready.put(entry.file.path(), entry);
                } else {
                    fallback(entry.file.path(), failure);
                }
                if (--remaining[0] == 0) {
                    // A configured file might not be used (e.g. the player chose another deck).
                    // This is a bounded optimization, not a permanent decoded-image cache.
                    if (!ready.isEmpty()) expiry = expire(this::clear);
                    complete.run();
                }
            });
        }
    }

    /** Releases unused startup images. A claimed TextureData owns its image until first upload. */
    void clear() {
        ready.clear();
        if (expiry != -1) cancelExpiry(expiry);
        expiry = -1;
    }

    /** Both TextureData.Factory and TextureLoader call this BEFORE constructing a Pixmap. */
    public static TextureData takePrepared(FileHandle file, Pixmap.Format format, boolean useMipMaps) {
        if (!(Gdx.app instanceof MiniGameApplication) || !(Gdx.gl instanceof WebGL20)
            || useMipMaps || (format != null && format != Pixmap.Format.RGBA8888)
            || Gdx.gl20 == null // Preserve any FileTextureData.copyToPOT/ensurePot behavior.
            || file.getClass() != MiniGameFileHandle.class || file.type() != FileType.Internal) return null;
        MiniGameNativeTextures textures = ((MiniGameApplication) Gdx.app).nativeTextures;
        if (textures == null || ((MiniGameFileHandle) file).nativeImagePath(textures.files) == null) return null;
        Entry entry = textures.ready.remove(file.path());
        if (entry == null) return null;
        if (textures.ready.isEmpty()) textures.clear();
        return new NativeTextureData(entry);
    }

    private static void fallback(String path, String reason) {
        System.err.println("[NativeTextures] " + path + ": " + reason + "; using stb_image");
    }

    /** Only formats whose stb_image result is known to be RGBA8888; no pixel decoding here. */
    static int[] rgbaPngSize(byte[] png) {
        if (png.length < 45 || readInt(png, 0) != 0x89504e47 || readInt(png, 4) != 0x0d0a1a0a
            || readInt(png, 8) != 13 || readInt(png, 12) != 0x49484452) return null;
        int width = readInt(png, 16), height = readInt(png, 20);
        int color = png[25] & 255;
        if (width <= 0 || height <= 0 || png[24] != 8 || (color != 6 && color != 3)
            || png[26] != 0 || png[27] != 0 || png[28] != 0) return null;
        int paletteEntries = 0;
        boolean alpha = color == 6, data = false;
        for (int offset = 33; offset <= png.length - 12;) {
            int length = readInt(png, offset), type = readInt(png, offset + 4);
            if (length < 0 || length > png.length - offset - 12) return null;
            if (type == 0x504c5445) { // PLTE
                if (data || length == 0 || length > 768 || length % 3 != 0) return null;
                paletteEntries = length / 3;
            } else if (type == 0x74524e53) { // tRNS gives palette images four stb channels
                if (data || color != 3 || length == 0 || length > paletteEntries) return null;
                alpha = true;
            } else if (type == 0x49444154) { // IDAT
                if (color == 3 && paletteEntries == 0) return null;
                data = true;
            } else if (type == 0x49454e44) { // IEND
                return length == 0 && data && alpha ? new int[] {width, height} : null;
            } else if (type == 0x69434350 || type == 0x67414d41 || type == 0x6348524d || type == 0x73524742) {
                // Colour-managed inputs need a separate parity contract; keep their old decoder.
                return null;
            }
            offset += length + 12;
        }
        return null;
    }

    private static int readInt(byte[] bytes, int offset) {
        return (bytes[offset] & 255) << 24 | (bytes[offset + 1] & 255) << 16
            | (bytes[offset + 2] & 255) << 8 | bytes[offset + 3] & 255;
    }

    private static final class Entry {
        final FileHandle file;
        final String physicalPath;
        final int width, height;
        JSObject image;
        Entry(FileHandle file, String physicalPath, int width, int height) {
            this.file = file;
            this.physicalPath = physicalPath;
            this.width = width;
            this.height = height;
        }
    }

    private static final class NativeTextureData implements TextureData {
        final FileHandle file;
        int width, height;
        JSObject image;
        FileTextureData reload;
        boolean prepared;

        NativeTextureData(Entry entry) {
            file = entry.file;
            width = entry.width;
            height = entry.height;
            image = entry.image;
            entry.image = null;
        }

        public TextureDataType getType() { return TextureDataType.Custom; }
        public boolean isManaged() { return true; }
        public boolean isPrepared() { return prepared; }
        public boolean useMipMaps() { return false; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public Pixmap.Format getFormat() { return Pixmap.Format.RGBA8888; }
        public boolean disposePixmap() { return false; }

        public void prepare() {
            if (prepared) throw new GdxRuntimeException("Already prepared");
            if (image == null) prepareReload();
            prepared = true;
        }

        private void prepareReload() {
            // Direct constructor: never select the native route again. Managed recovery stays
            // synchronous and does not keep either an Image or a pixel copy between uploads.
            reload = new FileTextureData(file, null, Pixmap.Format.RGBA8888, false);
            reload.prepare();
            width = reload.getWidth();
            height = reload.getHeight();
        }

        public Pixmap consumePixmap() {
            throw new GdxRuntimeException("Native texture-only file: " + file
                + ". Use new Pixmap(file) or direct FileTextureData when pixels are needed.");
        }

        public void consumeCustomData(int target) {
            if (!prepared) throw new GdxRuntimeException("Call prepare() before uploading texture data");
            try {
                if (image != null) {
                    String failure = Gdx.gl instanceof WebGL20
                        ? upload(((WebGL20) Gdx.gl).gl, image, target) : "unsupported GL wrapper";
                    image = null;
                    if (failure == null) return;
                    fallback(file.path(), failure);
                    prepareReload();
                }
                GLTexture.uploadImageData(target, reload, 0);
            } finally {
                image = null;
                // If upload throws before consuming/disposal, release its native Pixmap too.
                if (reload != null && reload.isPrepared()) reload.consumePixmap().dispose();
                reload = null;
                prepared = false;
            }
        }
    }

    @JSFunctor
    private interface Completion extends JSObject { void complete(JSObject image, String failure); }

    @JSFunctor
    private interface Expiry extends JSObject { void run(); }

    @JSBody(params = "callback", script = "return setTimeout(callback, 5000);")
    private static native int expire(Expiry callback);

    @JSBody(params = "id", script = "clearTimeout(id);")
    private static native void cancelExpiry(int id);

    @JSBody(params = {"path", "width", "height", "timeout", "complete"}, script =
        "var nativeImage = null, settled = false;" +
        "var timeoutId = setTimeout(function() { finish(null, 'native decode timed out'); }, timeout);" +
        "function finish(result, failure) {" +
        "  if (settled) return; settled = true; clearTimeout(timeoutId);" +
        "  if (nativeImage) { nativeImage.onload = null; nativeImage.onerror = null; }" +
        "  complete(result, failure);" +
        "}" +
        "try {" +
        "  nativeImage = wx.createImage();" +
        // WebGL's unpack flag alone cannot undo WeChat's lossy default decode premultiplication.
        "  nativeImage.premultiplyAlpha = false;" +
        "  nativeImage.onload = function() {" +
        "    if (nativeImage.width !== width || nativeImage.height !== height) finish(null, 'native size mismatch');" +
        "    else finish(nativeImage, null);" +
        "  };" +
        "  nativeImage.onerror = function() { finish(null, 'native decode failed'); };" +
        "  nativeImage.src = path;" +
        "} catch (nativeError) { finish(null, 'native decode threw'); }")
    private static native void decode(String path, int width, int height, int timeout, Completion complete);

    @JSBody(params = {"gl", "image", "target"}, script =
        "var oldPremultiply = gl.getParameter(37441), oldColorSpace = gl.getParameter(37443)," +
        "    oldFlip = gl.getParameter(37440), oldAlignment = gl.getParameter(3317);" +
        "try {" +
        "  gl.pixelStorei(37441, false); gl.pixelStorei(37443, 0); gl.pixelStorei(37440, false); gl.pixelStorei(3317, 1);" +
        "  gl.texImage2D(target, 0, 6408, 6408, 5121, image);" +
        "  var uploadError = gl.getError();" +
        "  return uploadError ? 'native upload GL error ' + uploadError : null;" +
        "} catch (uploadFailure) { return 'native upload threw'; }" +
        "finally {" +
        "  gl.pixelStorei(37441, oldPremultiply); gl.pixelStorei(37443, oldColorSpace);" +
        "  gl.pixelStorei(37440, oldFlip); gl.pixelStorei(3317, oldAlignment);" +
        "}")
    private static native String upload(JSObject gl, JSObject image, int target);
}
