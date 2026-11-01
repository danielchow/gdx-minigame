package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.MipMapGenerator;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameApplication;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameFileHandle;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameFiles;
import java.util.HashMap;
import java.util.Map;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/** Real application, Texture/AssetManager and WASM; the Node host drives wx image completions. */
public final class NativeTextureProbe {
    private static final Map<String, Texture> textures = new HashMap<>();
    private static AssetManager manager;

    public static void main(String[] args) {
        MiniGameApplicationConfiguration config = new MiniGameApplicationConfiguration();
        String paths = paths();
        config.nativeTextureFiles = paths.isEmpty() ? new String[0] : paths.split("\n");
        config.nativeTextureTimeoutMillis = 100;
        new MiniGameApplication(new ApplicationAdapter() {
            public void create() { record("create"); }
            public void resize(int width, int height) { record("resize"); }
            public void render() { record("render"); delta(Gdx.graphics.getDeltaTime()); }
            public void pause() { record("pause"); }
            public void resume() { record("resume"); }
        }, config);
        register((operation, path, argument) -> {
            FileHandle file = Gdx.files.internal(path);
            switch (operation) {
                case "factory": {
                    Pixmap.Format format = argument == 1 ? Pixmap.Format.RGBA8888
                        : argument == 2 ? Pixmap.Format.RGB888 : null;
                    TextureData data = TextureData.Factory.loadFromFile(file, format, argument == 3);
                    Texture texture = new Texture(data);
                    textures.put(path, texture);
                    return data.getType().ordinal();
                }
                case "customHandle": {
                    FileHandle custom = new MiniGameFileHandle((MiniGameFiles) Gdx.files, path,
                        com.badlogic.gdx.Files.FileType.Internal) { };
                    Texture texture = new Texture(custom);
                    textures.put(path, texture);
                    return texture.getTextureData().getType().ordinal();
                }
                case "pixelFile": {
                    Pixmap pixmap = new Pixmap(file);
                    int pixel = pixmap.getPixel(0, 0);
                    pixmap.dispose();
                    return pixel;
                }
                case "directData": {
                    FileTextureData data = new FileTextureData(file, null, null, false);
                    data.prepare();
                    Pixmap pixmap = data.consumePixmap();
                    int pixel = pixmap.getPixel(0, 0);
                    pixmap.dispose();
                    return pixel;
                }
                case "suppliedPixmap": {
                    Pixmap pixmap = new Pixmap(file);
                    Texture texture = new Texture(pixmap);
                    pixmap.dispose();
                    textures.put(path, texture);
                    return texture.getTextureData().getType().ordinal();
                }
                case "loader": {
                    manager();
                    manager.load(path, Texture.class);
                    manager.finishLoading();
                    Texture texture = manager.get(path, Texture.class);
                    textures.put(path, texture);
                    return texture.getTextureData().getType().ordinal();
                }
                case "atlas": {
                    manager();
                    manager.load(path, TextureAtlas.class);
                    manager.finishLoading();
                    Texture texture = manager.get(path, TextureAtlas.class).getTextures().first();
                    textures.put(path, texture);
                    return texture.getTextureData().getType().ordinal();
                }
                case "pixelException": {
                    try { textures.get(path).getTextureData().consumePixmap(); }
                    catch (GdxRuntimeException expected) {
                        if (expected.getMessage().contains("Native texture-only file")
                            && expected.getMessage().contains("FileTextureData")) return 1;
                        throw expected;
                    }
                    return 0;
                }
                case "reload":
                    Texture.setAssetManager(argument == 0 ? null : manager);
                    Texture.invalidateAllTextures(Gdx.app);
                    if (manager != null) manager.finishLoading();
                    return textures.get(path).getTextureData().getType().ordinal();
                case "format": return textures.get(path).getTextureData().getFormat().ordinal();
                case "width": return textures.get(path).getWidth();
                case "height": return textures.get(path).getHeight();
                case "managed": return textures.get(path).isManaged() ? 1 : 0;
                case "references": return manager.getReferenceCount(path);
                case "filter":
                    textures.get(path).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest);
                    textures.get(path).setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
                    return 0;
                case "filterPreserved": {
                    Texture texture = textures.get(path);
                    return texture.getMinFilter() == Texture.TextureFilter.Linear
                        && texture.getMagFilter() == Texture.TextureFilter.Nearest
                        && texture.getUWrap() == Texture.TextureWrap.Repeat
                        && texture.getVWrap() == Texture.TextureWrap.ClampToEdge ? 1 : 0;
                }
                case "drawManaged": {
                    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                    try { textures.get(path).draw(pixmap, 0, 0); }
                    catch (GdxRuntimeException expected) { return 1; }
                    finally { pixmap.dispose(); }
                    return 0;
                }
                case "doublePrepare": {
                    TextureData data = textures.get(path).getTextureData();
                    data.prepare();
                    try { data.prepare(); }
                    catch (GdxRuntimeException expected) {
                        textures.get(path).bind();
                        data.consumeCustomData(GL20.GL_TEXTURE_2D);
                        return 1;
                    }
                    return 0;
                }
                case "cpuMipmaps": MipMapGenerator.setUseHardwareMipMap(false); return 0;
                case "potFallback":
                    Gdx.gl20 = argument != 0 ? null : Gdx.gl;
                    return 0;
                case "potData": {
                    // copyToPOT is a backend-only field, not in the JVM libGDX API. Exercise
                    // that explicit backend class too, with its own field and constructor.
                    emu.com.badlogic.gdx.graphics.glutils.FileTextureData.copyToPOT = true;
                    Texture texture = new Texture(new emu.com.badlogic.gdx.graphics.glutils.FileTextureData(
                        file, null, null, false));
                    textures.put(path, texture);
                    return texture.getWidth();
                }
                case "dispose":
                    if (manager != null) manager.dispose();
                    for (Texture texture : textures.values()) { texture.dispose(); texture.dispose(); }
                    textures.clear();
                    Texture.setAssetManager(null);
                    return 0;
                case "continuous": Gdx.graphics.setContinuousRendering(argument != 0); return 0;
                case "requestFrame": Gdx.graphics.requestRendering(); return 0;
                case "postRunnable": Gdx.app.postRunnable(() -> record("runnable")); return 0;
                default: throw new IllegalArgumentException(operation);
            }
        });
    }

    private static void manager() {
        if (manager == null) manager = new AssetManager();
    }

    @JSFunctor
    interface Command extends JSObject { int run(String operation, String path, int argument); }
    @JSBody(script = "return globalThis.__nativeProbe.paths;")
    private static native String paths();
    @JSBody(params = "command", script = "globalThis.__nativeProbe.command = command;")
    private static native void register(Command command);
    @JSBody(params = "event", script = "globalThis.__nativeProbe.events.push(event);")
    private static native void record(String event);
    @JSBody(params = "value", script = "globalThis.__nativeProbe.delta = value;")
    private static native void delta(float value);
}
