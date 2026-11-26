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
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.LifecycleCallback;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WXWindowInfo;
import com.github.xpenatan.gdx.teavm.backends.minigame.dom.MiniGameNavigator;
import com.github.xpenatan.gdx.teavm.backends.minigame.gl.WebGL2RenderingContextExt;
import com.github.xpenatan.gdx.teavm.backends.minigame.gl.WebGLContextAttributesExt;
import com.github.xpenatan.gdx.teavm.backends.minigame.gl.WebGLRenderingContextExt;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.TimerHandler;
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
 * - Density from wx.getWindowInfo().pixelRatio, cached, instead of devicePixelRatio
 * - Canvas sized from wx.getWindowInfo() window size, following wx.onWindowResize
 */
public class MiniGameGraphics implements Graphics {

    protected HTMLCanvasElement canvas;
    protected MiniGameApplicationConfiguration config;
    private WebGLRenderingContext context;
    protected GL20 gl20;
    protected GL30 gl30;
    protected GLVersion glVersion;

    private boolean continuousRendering = true;
    private Runnable frameRequester;

    void setFrameRequester(Runnable frameRequester) {
        this.frameRequester = frameRequester;
    }

    float fps = 0;
    long lastTimeStamp = System.currentTimeMillis();
    long frameId = -1;
    float deltaTime = 0;
    float time = 0;
    int frames;

    // wx.getWindowInfo() is a synchronous native call. Layout code reads density and safe insets
    // per widget, so they are cached here and refreshed on wx.onWindowResize.
    private static final double KEYBOARD_RESIZE_SETTLE_MS = 600.0;
    private static final int RECONCILE_DELAY_MARGIN_MS = 50;
    private static final int METRICS_IGNORED = 0;
    private static final int METRICS_UNCHANGED = 1;
    private static final int METRICS_CHANGED = 2;
    private final boolean mobilePlatform;
    private double pixelRatio = 1;
    private int safeInsetTop;
    private int safeInsetLeft;
    private int safeInsetBottom;
    private int safeInsetRight;
    // Logical size of the window the canvas fills; touches arrive in these units.
    private double windowWidth;
    private double windowHeight;
    private double screenHeight;
    private boolean keyboardActive;
    private boolean keyboardHeightOpen;
    private boolean keyboardListenersRegistered;
    private double lastKeyboardEventMs = Double.NEGATIVE_INFINITY;
    private boolean deferredResizePending;
    private boolean appHidden;
    private JSObject reconcileTimer;
    private int reconcileGeneration;

    public MiniGameGraphics(MiniGameApplicationConfiguration config) {
        this.config = config;
        this.canvas = (HTMLCanvasElement) config.canvas;
        this.mobilePlatform = isMobilePlatform(MiniGameNavigator.getPlatform());

        // The canvas is sized in physical pixels, like the web backend provides, from the window
        // as it is now: wx.createCanvas() ran in game.js before the subpackages loaded, and
        // desktop WeChat can still be opening its window then, which left a squashed canvas
        // that WeChat stretched over the final window.
        applyWindowMetrics(null);
        WX.onWindowResize((LifecycleCallback) resize -> {
            if (applyWindowMetrics(resize) != METRICS_IGNORED) {
                requestRendering();
            }
        });

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
    // WeChat reports no physical ppi. As on Android, where density is about the device pixel
    // ratio and 1.0 means 160 dpi, density is the pixel ratio and ppi is 160 times it.

    @Override
    public float getPpiX() { return 160f * (float) getNativeScreenDensity(); }

    @Override
    public float getPpiY() { return 160f * (float) getNativeScreenDensity(); }

    @Override
    public float getPpcX() { return getPpiX() / 2.54f; }

    @Override
    public float getPpcY() { return getPpiY() / 2.54f; }

    @Override
    public float getDensity() {
        return (float) getNativeScreenDensity();
    }

    public double getNativeScreenDensity() {
        return pixelRatio;
    }

    private static boolean isMobilePlatform(String platform) {
        return "android".equals(platform) || "ios".equals(platform) || "ohos".equals(platform);
    }

    /**
     * Marks whether a soft keyboard session is active. On mobile WeChat (notably Android),
     * showing or hiding the soft keyboard can transiently change {@code windowHeight} via
     * {@code wx.onWindowResize}; tracking keyboard state lets graphics ignore those transient
     * height-only resizes so the game surface does not jump while the keyboard opens or closes.
     */
    public void setKeyboardActive(boolean active) {
        if (active && mobilePlatform && !keyboardListenersRegistered) {
            keyboardListenersRegistered = true;
            registerKeyboardListeners(
                height -> {
                    boolean wasOpen = keyboardHeightOpen;
                    keyboardHeightOpen = height > 0;
                    if (wasOpen && !keyboardHeightOpen) keyboardActive = false;
                    lastKeyboardEventMs = nowMs();
                    if (keyboardHeightOpen) {
                        cancelReconcileTimer();
                    } else if (deferredResizePending && !appHidden && !keyboardActive) {
                        scheduleReconcile();
                    }
                },
                () -> {
                    keyboardHeightOpen = false;
                    keyboardActive = false;
                    lastKeyboardEventMs = nowMs();
                    if (deferredResizePending && !appHidden) {
                        scheduleReconcile();
                    }
                }
            );
        }
        boolean hadKeyboard = keyboardActive || keyboardHeightOpen;
        if (active || hadKeyboard) {
            lastKeyboardEventMs = nowMs();
        }
        keyboardActive = active;
        if (active) {
            cancelReconcileTimer();
        } else {
            keyboardHeightOpen = false;
            if (deferredResizePending && !appHidden && (hadKeyboard || reconcileTimer == null)) {
                scheduleReconcile();
            }
        }
    }

    void onAppHide() {
        appHidden = true;
        setKeyboardActive(false);
        cancelReconcileTimer();
    }

    void onAppShow() {
        appHidden = false;
        if (deferredResizePending && !keyboardActive && !keyboardHeightOpen) {
            if (!isKeyboardResizeWindow()) {
                reconcileDeferredResize();
            } else if (reconcileTimer == null) {
                scheduleReconcile();
            }
        }
    }

    private boolean isKeyboardResizeWindow() {
        if (keyboardActive || keyboardHeightOpen) return true;
        double elapsed = nowMs() - lastKeyboardEventMs;
        return elapsed >= 0.0 && elapsed < KEYBOARD_RESIZE_SETTLE_MS;
    }

    private void cancelReconcileTimer() {
        reconcileGeneration++;
        JSObject handle = reconcileTimer;
        reconcileTimer = null;
        if (handle != null) {
            clearTimeoutHandle(handle);
        }
    }

    private void scheduleReconcile() {
        cancelReconcileTimer();
        double elapsed = nowMs() - lastKeyboardEventMs;
        double remaining = (elapsed >= 0.0 && elapsed < KEYBOARD_RESIZE_SETTLE_MS)
            ? (KEYBOARD_RESIZE_SETTLE_MS - elapsed) : 0.0;
        int delayMs = (remaining > 0.0 ? (int) Math.ceil(remaining) : 0) + RECONCILE_DELAY_MARGIN_MS;
        final int generation = reconcileGeneration;
        reconcileTimer = scheduleTimeout(() -> {
            if (generation != reconcileGeneration) return;
            reconcileTimer = null;
            reconcileDeferredResize();
        }, delayMs);
    }

    private void reconcileDeferredResize() {
        if (!deferredResizePending || appHidden) return;
        if (keyboardActive || keyboardHeightOpen) return;
        if (isKeyboardResizeWindow()) {
            scheduleReconcile();
            return;
        }
        deferredResizePending = false;
        if (applyWindowMetrics(null) == METRICS_CHANGED) {
            requestRendering();
        }
    }

    private boolean shouldIgnoreMobileKeyboardResize(
            double nextWindowWidth, double nextPixelRatio, double nextScreenHeight) {
        return mobilePlatform
            && nextWindowWidth == windowWidth
            && nextPixelRatio == pixelRatio
            && nextScreenHeight == screenHeight
            && isKeyboardResizeWindow();
    }

    @JSFunctor
    private interface KeyboardHeightCallback extends JSObject {
        void onHeightChange(double height);
    }

    @JSFunctor
    private interface KeyboardCompleteCallback extends JSObject {
        void onComplete();
    }

    @JSBody(script = "return Date.now();")
    private static native double nowMs();

    @JSBody(
        params = {"timerCallback", "delayMillis"},
        script = "if (typeof setTimeout !== 'function') return null;"
            + " var timerHandle = setTimeout(timerCallback, delayMillis);"
            + " return timerHandle === undefined ? null : timerHandle;"
    )
    private static native JSObject scheduleTimeout(TimerHandler timerCallback, int delayMillis);

    @JSBody(
        params = "timerHandle",
        script = "if (timerHandle !== null && timerHandle !== undefined && typeof clearTimeout === 'function') {"
            + " clearTimeout(timerHandle); }"
    )
    private static native void clearTimeoutHandle(JSObject timerHandle);

    @JSBody(
        params = {"onHeight", "onComplete"},
        script = "if (typeof wx.onKeyboardHeightChange === 'function') {"
            + " wx.onKeyboardHeightChange(function(keyboardEvent) {"
            + " var nextHeight = keyboardEvent && typeof keyboardEvent.height === 'number' ? keyboardEvent.height : 0;"
            + " onHeight(nextHeight);"
            + " }); }"
            + " if (typeof wx.onKeyboardComplete === 'function') {"
            + " wx.onKeyboardComplete(function() { onComplete(); }); }"
    )
    private static native void registerKeyboardListeners(
        KeyboardHeightCallback onHeight, KeyboardCompleteCallback onComplete);

    @JSBody(params = "resize", script = "return resize.windowWidth || 0;")
    private static native double windowWidthOf(JSObject resize);

    @JSBody(params = "resize", script = "return resize.windowHeight || 0;")
    private static native double windowHeightOf(JSObject resize);

    /** Canvas pixels per logical window pixel, for mapping touch coordinates. */
    public float getTouchScaleX() { return (float) (canvas.getWidth() / windowWidth); }

    public float getTouchScaleY() { return (float) (canvas.getHeight() / windowHeight); }

    /**
     * Re-reads pixel ratio, window size and safe area from one wx.getWindowInfo() call, preferring
     * the size a wx.onWindowResize event carries, and sizes the canvas to the window in physical
     * pixels; insets in physical pixels.
     */
    protected void updateWindowMetrics(JSObject resize) {
        applyWindowMetrics(resize);
    }

    private int applyWindowMetrics(JSObject resize) {
        WXWindowInfo info = WX.getWindowInfo();
        double nextPixelRatio = info.getPixelRatio();
        // The event's size is the new one; desktop WeChat's getWindowInfo() can still lag behind it.
        double eventWidth = resize != null ? windowWidthOf(resize) : 0;
        double eventHeight = resize != null ? windowHeightOf(resize) : 0;
        double nextWindowWidth = eventWidth > 0 ? eventWidth : info.getWindowWidth();
        double nextWindowHeight = eventHeight > 0 ? eventHeight : info.getWindowHeight();
        double nextScreenHeight = info.getScreenHeight();
        if (resize != null && shouldIgnoreMobileKeyboardResize(nextWindowWidth, nextPixelRatio, nextScreenHeight)) {
            deferredResizePending = true;
            if (!appHidden && !keyboardActive && !keyboardHeightOpen && reconcileTimer == null) {
                scheduleReconcile();
            }
            return METRICS_IGNORED;
        }
        deferredResizePending = false;
        cancelReconcileTimer();
        int width = (int) (nextWindowWidth * nextPixelRatio);
        int height = (int) (nextWindowHeight * nextPixelRatio);
        int prevCanvasWidth = canvas.getWidth();
        int prevCanvasHeight = canvas.getHeight();
        // Assigning a size clears the drawing buffer even when it is unchanged.
        if (prevCanvasWidth != width) canvas.setWidth(width);
        if (prevCanvasHeight != height) canvas.setHeight(height);
        WXWindowInfo.SafeArea safeArea = info.getSafeArea();
        int nextSafeInsetTop;
        int nextSafeInsetLeft;
        int nextSafeInsetBottom;
        int nextSafeInsetRight;
        if (safeArea == null) {
            nextSafeInsetTop = nextSafeInsetLeft = nextSafeInsetBottom = nextSafeInsetRight = 0;
        } else {
            nextSafeInsetTop = (int) (safeArea.getTop() * nextPixelRatio);
            nextSafeInsetLeft = (int) (safeArea.getLeft() * nextPixelRatio);
            nextSafeInsetBottom = (int) ((nextScreenHeight - safeArea.getBottom()) * nextPixelRatio);
            nextSafeInsetRight = (int) ((info.getScreenWidth() - safeArea.getRight()) * nextPixelRatio);
        }
        boolean changed = prevCanvasWidth != width
            || prevCanvasHeight != height
            || pixelRatio != nextPixelRatio
            || windowWidth != nextWindowWidth
            || windowHeight != nextWindowHeight
            || safeInsetTop != nextSafeInsetTop
            || safeInsetLeft != nextSafeInsetLeft
            || safeInsetBottom != nextSafeInsetBottom
            || safeInsetRight != nextSafeInsetRight;
        pixelRatio = nextPixelRatio;
        windowWidth = nextWindowWidth;
        windowHeight = nextWindowHeight;
        screenHeight = nextScreenHeight;
        safeInsetTop = nextSafeInsetTop;
        safeInsetLeft = nextSafeInsetLeft;
        safeInsetBottom = nextSafeInsetBottom;
        safeInsetRight = nextSafeInsetRight;
        return changed ? METRICS_CHANGED : METRICS_UNCHANGED;
    }

    // === Display (WeChat is always fullscreen) ===

    @Override
    public boolean supportsDisplayModeChange() { return false; }

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
        return new DisplayMode(
                canvas.getWidth(),
                canvas.getHeight(), 60, 8) {};
    }

    @Override
    public DisplayMode getDisplayMode(Monitor monitor) { return getDisplayMode(); }

    @Override
    public boolean setFullscreenMode(DisplayMode displayMode) {
        return true; // Already fullscreen on WeChat
    }

    @Override
    public boolean setWindowedMode(int width, int height) {
        // The canvas is WeChat's full-screen surface: resizing it only lowers the resolution while
        // WeChat still stretches it over the screen. libGT's DesktopDisplay.setOrientation() calls
        // this (with a desktop HDPI factor of 2) when a win animation locks the orientation, which
        // halved the canvas and drew the game twice as large.
        return false;
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
    public void setContinuousRendering(boolean isContinuous) {
        continuousRendering = isContinuous;
        if (isContinuous) requestRendering();
    }

    @Override
    public boolean isContinuousRendering() { return continuousRendering; }

    @Override
    public void requestRendering() {
        if (frameRequester != null) frameRequester.run();
    }

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
    public int getSafeInsetLeft() {
        return safeInsetLeft;
    }

    @Override
    public int getSafeInsetTop() {
        return safeInsetTop;
    }

    @Override
    public int getSafeInsetBottom() {
        return safeInsetBottom;
    }

    @Override
    public int getSafeInsetRight() {
        return safeInsetRight;
    }

    static class TeaMonitor extends Monitor {
        protected TeaMonitor(int virtualX, int virtualY, String name) {
            super(virtualX, virtualY, name);
        }
    }
}
