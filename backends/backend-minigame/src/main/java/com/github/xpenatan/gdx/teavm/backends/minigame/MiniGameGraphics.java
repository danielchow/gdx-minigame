package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import com.github.xpenatan.gdx.teavm.backends.minigame.gl.WebGL2RenderingContextExt;
import com.github.xpenatan.gdx.teavm.backends.minigame.gl.WebGLContextAttributesExt;
import com.github.xpenatan.gdx.teavm.backends.minigame.gl.WebGLRenderingContextExt;
import org.teavm.jso.JSBody;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.webgl.WebGLContextAttributes;
import org.teavm.jso.webgl.WebGLRenderingContext;

/**
 * Graphics implementation for WeChat Mini Game.
 * Forked from WebGraphics + WebGLGraphics, with DOM dependencies removed:
 * - No document.getElementById (canvas from config)
 * - No fullscreen API (always fullscreen on WeChat)
 * - No setTitle (no title bar on WeChat)
 * - No CSS cursor manipulation
 * - Screen size via wx.getSystemInfoSync() instead of screen.*
 * - Density via wx.getSystemInfoSync().pixelRatio instead of devicePixelRatio
 */
public class MiniGameGraphics implements Graphics {

    protected HTMLCanvasElement canvas;
    protected MiniGameApplicationConfiguration config;
    private WebGLRenderingContext context;
    protected GL20 gl20;
    protected GL30 gl30;
    protected GLVersion glVersion;

    float fps = 0;
    long lastTimeStamp = System.currentTimeMillis();
    long frameId = -1;
    float deltaTime = 0;
    float time = 0;
    int frames;

    public MiniGameGraphics(MiniGameApplicationConfiguration config) {
        this.config = config;
        this.canvas = (HTMLCanvasElement) config.canvas;

        WebGLContextAttributesExt attr = (WebGLContextAttributesExt) WebGLContextAttributes.create();
        attr.setAlpha(config.alpha);
        attr.setAntialias(config.antialiasing);
        attr.setStencil(config.stencil);
        attr.setPremultipliedAlpha(config.premultipliedAlpha);
        attr.setPreserveDrawingBuffer(config.preserveDrawingBuffer);
        attr.setPowerPreference(config.powerPreference);

        if (config.useGL30) {
            context = (WebGLRenderingContext) canvas.getContext("webgl2", attr);
        }

        if (config.useGL30 && context != null) {
            this.gl30 = config.useDebugGL ? new WebGL30Debug((WebGL2RenderingContextExt) context)
                    : new WebGL30((WebGL2RenderingContextExt) context);
            this.gl20 = gl30;
        } else {
            context = (WebGLRenderingContext) canvas.getContext("webgl", attr);
            this.gl20 = config.useDebugGL ? new WebGL20Debug((WebGLRenderingContextExt) context)
                    : new WebGL20((WebGLRenderingContextExt) context);
        }

        String versionString = gl20.glGetString(GL20.GL_VERSION);
        String vendorString = gl20.glGetString(GL20.GL_VENDOR);
        String rendererString = gl20.glGetString(GL20.GL_RENDERER);
        glVersion = new GLVersion(Application.ApplicationType.WebGL, versionString, vendorString, rendererString);

        context.viewport(0, 0, getWidth(), getHeight());
    }

    public void render(ApplicationListener listener) {
        listener.render();
    }

    public void resize(ApplicationListener appListener, int width, int height) {
        Gdx.gl.glViewport(0, 0, width, height);
        appListener.resize(width, height);
    }

    public void begin() {
        long currTimeStamp = System.currentTimeMillis();
        deltaTime = (currTimeStamp - lastTimeStamp) / 1000.0f;
        lastTimeStamp = currTimeStamp;
        time += deltaTime;
        frames++;
        if (time > 1) {
            this.fps = frames;
            time = 0;
            frames = 0;
        }
    }

    public void end() {
    }

    // === GL Accessors ===

    @Override
    public boolean isGL30Available() { return gl30 != null; }

    @Override
    public boolean isGL31Available() { return false; }

    @Override
    public boolean isGL32Available() { return false; }

    @Override
    public GL20 getGL20() { return gl20; }

    @Override
    public GL30 getGL30() { return gl30; }

    @Override
    public com.badlogic.gdx.graphics.GL31 getGL31() { return null; }

    @Override
    public com.badlogic.gdx.graphics.GL32 getGL32() { return null; }

    @Override
    public void setGL20(GL20 gl20) {
        this.gl20 = gl20;
        Gdx.gl = gl20;
        Gdx.gl20 = gl20;
    }

    @Override
    public void setGL30(GL30 gl30) {
        this.gl30 = gl30;
        if (gl30 != null) {
            this.gl20 = gl30;
            Gdx.gl = gl20;
            Gdx.gl20 = gl20;
            Gdx.gl30 = gl30;
        }
    }

    @Override
    public void setGL31(com.badlogic.gdx.graphics.GL31 gl31) {}

    @Override
    public void setGL32(com.badlogic.gdx.graphics.GL32 gl32) {}

    @Override
    public GLVersion getGLVersion() { return glVersion; }

    // === Size ===

    @Override
    public int getWidth() { return canvas.getWidth(); }

    @Override
    public int getHeight() { return canvas.getHeight(); }

    @Override
    public int getBackBufferWidth() { return canvas.getWidth(); }

    @Override
    public int getBackBufferHeight() { return canvas.getHeight(); }

    // === FPS & Timing ===

    @Override
    public long getFrameId() { return frameId; }

    @Override
    public float getDeltaTime() { return deltaTime; }

    @Override
    public float getRawDeltaTime() { return deltaTime; }

    @Override
    public int getFramesPerSecond() { return (int) fps; }

    @Override
    public GraphicsType getType() { return GraphicsType.WebGL; }

    // === Density & PPI ===

    @Override
    public float getPpiX() { return 96f * (float) getDensity(); }

    @Override
    public float getPpiY() { return 96f * (float) getDensity(); }

    @Override
    public float getPpcX() { return getPpiX() / 2.54f; }

    @Override
    public float getPpcY() { return getPpiY() / 2.54f; }

    @Override
    public float getDensity() {
        float ppiX = getPpiX();
        return (ppiX > 0 && ppiX <= Float.MAX_VALUE) ? ppiX / 160f : 1f;
    }

    public double getNativeScreenDensity() {
        return WX.getPixelRatio();
    }

    // === Display (WeChat is always fullscreen) ===

    @Override
    public boolean supportsDisplayModeChange() { return true; }

    @Override
    public Monitor getPrimaryMonitor() {
        return new TeaMonitor(0, 0, "Primary Monitor");
    }

    @Override
    public Monitor getMonitor() { return getPrimaryMonitor(); }

    @Override
    public Monitor[] getMonitors() { return new Monitor[]{getPrimaryMonitor()}; }

    @Override
    public DisplayMode[] getDisplayModes() { return new DisplayMode[]{getDisplayMode()}; }

    @Override
    public DisplayMode[] getDisplayModes(Monitor monitor) { return getDisplayModes(); }

    @Override
    public DisplayMode getDisplayMode() {
        double density = config.usePhysicalPixels ? getNativeScreenDensity() : 1;
        return new DisplayMode(
                (int) (WX.getScreenWidth() * density),
                (int) (WX.getScreenHeight() * density), 60, 8) {};
    }

    @Override
    public DisplayMode getDisplayMode(Monitor monitor) { return getDisplayMode(); }

    @Override
    public boolean setFullscreenMode(DisplayMode displayMode) {
        return true; // Already fullscreen on WeChat
    }

    @Override
    public boolean setWindowedMode(int width, int height) {
        canvas.setWidth(width);
        canvas.setHeight(height);
        return true;
    }

    // === Not applicable on WeChat ===

    @Override
    public void setTitle(String title) { /* No title bar on WeChat */ }

    @Override
    public void setUndecorated(boolean undecorated) {}

    @Override
    public void setResizable(boolean resizable) {}

    @Override
    public void setVSync(boolean vsync) {}

    @Override
    public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
        return null; // No cursor on mobile
    }

    @Override
    public void setCursor(Cursor cursor) {}

    @Override
    public void setSystemCursor(SystemCursor systemCursor) {}

    @Override
    public BufferFormat getBufferFormat() {
        return new BufferFormat(8, 8, 8, 0, 16, config.stencil ? 8 : 0, 0, false);
    }

    @Override
    public void setContinuousRendering(boolean isContinuous) {}

    @Override
    public boolean isContinuousRendering() { return true; }

    @Override
    public void requestRendering() {}

    @Override
    public boolean supportsExtension(String extensionName) {
        if (context != null) {
            return context.getExtension(extensionName) != null;
        }
        return false;
    }

    @Override
    public float getBackBufferScale() {
        return getBackBufferWidth() / (float) getWidth();
    }

    @Override
    public void setForegroundFPS(int fps) {}

    @Override
    public boolean isFullscreen() {
        return true; // Always fullscreen on WeChat
    }

    @Override
    public int getSafeInsetLeft() { return 0; }
    @Override
    public int getSafeInsetTop() { return 0; }
    @Override
    public int getSafeInsetBottom() { return 0; }
    @Override
    public int getSafeInsetRight() { return 0; }

    static class TeaMonitor extends Monitor {
        protected TeaMonitor(int virtualX, int virtualY, String name) {
            super(virtualX, virtualY, name);
        }
    }
}
