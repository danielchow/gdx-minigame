package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.LifecycleCallback;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.OnHideCallback;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetInstance;
import com.github.xpenatan.gdx.teavm.backends.minigame.dom.typedarray.TypedArrays;
import com.github.xpenatan.jmultiplatform.core.JMultiplatform;
import com.github.xpenatan.jmultiplatform.core.JPlatformMap;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSExceptions;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.AnimationFrameCallback;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Int8Array;

import java.util.ArrayList;

/**
 * Main application class for WeChat Mini Game.
 * Forked from WebApplication with DOM dependencies removed:
 * - No WebWindow — canvas from config
 * - No document.visibilitychange — uses wx.onShow/onHide
 * - No window.addEventListener("resize") — uses wx.onWindowResize
 * - No DOM script injection — uses require()
 * - No Howler.js — uses wx.createInnerAudioContext()
 * - No localStorage — uses wx.setStorageSync/getStorageSync
 */
public class MiniGameApplication implements Application {

    private static final String WEB_SCRIPT_PATH = "WEB_SCRIPT_PATH";
    private static final String WEB_ASSET_PATH = "WEB_ASSET_PATH";

    private boolean firstFrameTimed = false;

    public static MiniGameApplication get() {
        return (MiniGameApplication) Gdx.app;
    }

    private MiniGameGraphics graphics;
    private MiniGameInput input;
    private MiniGameFiles files;
    private MiniGameNet net;
    private MiniGameAudio audio;
    private MiniGameApplicationConfiguration config;
    private ApplicationListener appListener;
    private ApplicationListener curListener;
    private final Array<LifecycleListener> lifecycleListeners = new Array<>(4);

    private AppState initState = AppState.INIT;

    private int lastWidth = -1;
    private int lastHeight = 1;

    private ApplicationLogger logger;
    private int logLevel = LOG_INFO;

    private ObjectMap<String, Preferences> prefs = new ObjectMap<>();
    private MiniGameClipboard clipboard;

    private final Array<Runnable> runnables = new Array<>();
    private final Array<Runnable> runnablesHelper = new Array<>();

    private boolean stepError;
    private boolean isPreloadReady = false;

    public MiniGameApplication(ApplicationListener appListener, MiniGameApplicationConfiguration config) {
        this.config = config;
        this.appListener = appListener;
        curListener = appListener;
        init();
    }

    protected void init() {
        long initStart = System.currentTimeMillis();
        System.out.println("[PERF] phase=java_init start=" + initStart);
        updateErrorStack();

        // Set system properties
        String platform = WX.getPlatform();
        System.setProperty("java.runtime.name", "");
        System.setProperty("userAgent", "WeChat Mini Game");
        if (platform.contains("Windows")) System.setProperty("os.name", "Windows");
        else if (platform.contains("Mac")) System.setProperty("os.name", "OS X");
        else if (platform.contains("Linux")) System.setProperty("os.name", "Linux");
        else if (platform.contains("Android") || platform.contains("android")) System.setProperty("os.name", "Linux");
        else if (platform.contains("iOS") || platform.contains("devtools")) System.setProperty("os.name", "OS X");
        else System.setProperty("os.name", "Unknown");

        JMultiplatform instance = JMultiplatform.getInstance();
        JPlatformMap map = instance.getMap();
        map.put(WEB_SCRIPT_PATH, "subpackages/engine/");
        map.put(WEB_ASSET_PATH, "assets/");

        // Set canvas from globalThis.canvas if not already configured
        if (config.canvas == null) {
            config.canvas = getGlobalCanvas();
        }

        graphics = createGraphics(config);
        input = new MiniGameInput(this, graphics.canvas);
        files = new MiniGameFiles(config, this);
        preloadAssetsIntoStorage();
        long afterPreload = System.currentTimeMillis();
        System.out.println("[PERF] phase=java_init after_preload dur=" + (afterPreload - initStart));
        net = new MiniGameNet();
        logger = new WebApplicationLogger();
        clipboard = new MiniGameClipboard();

        initNativeLibraries();
        System.out.println("[PERF] phase=java_init after_native_libs dur=" + (System.currentTimeMillis() - initStart));

        // Set Gdx singletons
        Gdx.app = this;
        Gdx.graphics = graphics;
        Gdx.gl = graphics.getGL20();
        Gdx.gl20 = graphics.getGL20();
        Gdx.gl30 = graphics.getGL30();
        Gdx.input = input;
        Gdx.files = files;
        AssetInstance.setInstance(new MiniGameAssetDownloader());
        AssetInstance.setInstance(new MiniGameAssetLoader());
        Gdx.net = net;
        this.audio = new MiniGameAudio();
        Gdx.audio = audio;

        System.out.println("[PERF] phase=java_init after_setup dur=" + (System.currentTimeMillis() - initStart));

        // Lifecycle: wx.onHide → pause
        WX.onHide((OnHideCallback) () -> {
            if (initState == AppState.APP_LOOP) {
                synchronized (lifecycleListeners) {
                    for (LifecycleListener listener : lifecycleListeners) {
                        listener.pause();
                    }
                }
                if (curListener != null) curListener.pause();
            }
        });

        // Lifecycle: wx.onShow → resume
        WX.onShow((LifecycleCallback) result -> {
            if (initState == AppState.APP_LOOP) {
                synchronized (lifecycleListeners) {
                    for (LifecycleListener listener : lifecycleListeners) {
                        listener.resume();
                    }
                }
                if (curListener != null) curListener.resume();
            }
        });

        // Game loop
        Runnable appLoop = new Runnable() {
            @Override
            public void run() {
                step(curListener);
                if (!stepError) {
                    WX.requestAnimationFrame(callback -> this.run());
                }
            }
        };

        System.out.println("[PERF] phase=java_init end=" + System.currentTimeMillis() + " dur=" + (System.currentTimeMillis() - initStart));
        // Start
        WX.requestAnimationFrame(callback -> {
            step(curListener);
            if (!stepError) {
                initState = AppState.APP_LOOP;
                WX.requestAnimationFrame(callback1 -> appLoop.run());
            }
        });
    }

    private void step(ApplicationListener appListener) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        boolean resizeBypass = false;

        try {
            graphics.begin();

            if (initState == AppState.INIT) {
                initState = AppState.APP_LOOP;
                input.setInputProcessor(null);
                input.reset();
                runnables.clear();
                graphics.frameId = 0;
                long createStart = System.currentTimeMillis();
                System.out.println("[PERF] phase=app_create start=" + createStart);
                appListener.create();
                System.out.println("[PERF] phase=app_create end=" + System.currentTimeMillis() + " dur=" + (System.currentTimeMillis() - createStart));
                resizeBypass = true;
            }

            if ((width != lastWidth || height != lastHeight) || resizeBypass) {
                lastWidth = width;
                lastHeight = height;
                graphics.resize(appListener, width, height);
            }

            runnablesHelper.addAll(runnables);
            runnables.clear();
            for (int i = 0; i < runnablesHelper.size; i++) {
                runnablesHelper.get(i).run();
            }
            runnablesHelper.clear();
            graphics.frameId++;
            if (graphics.frameId > 5) {
                graphics.render(appListener);
                if (!firstFrameTimed) {
                    firstFrameTimed = true;
                    System.out.println("[PERF] phase=first_frame_render end=" + System.currentTimeMillis());
                    markFirstFrameJS();
                }
            }
            input.reset();

        } catch (Throwable t) {
            stepError = true;
            onError(t);
        } finally {
            graphics.end();
        }
    }

    protected void onError(Throwable error) {
        printErrorStack(error);
    }

    public static void printErrorStack(Object obj) {
        Throwable error = (Throwable) obj;
        ArrayList<JSObject> errors = new ArrayList<>();
        ArrayList<String> throwables = new ArrayList<>();
        Throwable root = error;
        while (root != null) {
            JSObject jsException = JSExceptions.getJSException(root);
            errors.add(0, jsException);
            String msg = root.getMessage();
            if (msg == null) msg = "";
            throwables.add(0, root.getClass().getSimpleName() + " " + msg);
            root = root.getCause();
        }
        JSObject[] errorsJS = new JSObject[errors.size()];
        String[] exceptions = new String[errors.size()];
        errors.toArray(errorsJS);
        throwables.toArray(exceptions);
        groupCollapsed("%cFatal Error" + ": " + exceptions[0], "color: #FF0000");

        for (int i = 0; i < errorsJS.length; i++) {
            int count = i + 1;
            JSObject errorJS = errorsJS[i];
            if (i > 0) {
                consoleLog("%cException " + count + ": " + exceptions[i], "color: #FF0000");
            }
            consoleLogError(errorJS);
        }
        groupEnd();
    }

    @JSBody(params = "error", script = "console.log(error);")
    private static native void consoleLogError(JSObject error);

    @JSBody(script = "if(typeof Error.stackTraceLimit !== 'undefined') { Error.stackTraceLimit = Infinity; }")
    private static native void updateErrorStack();

    @JSBody(params = {"msg", "param"}, script = "console.log(msg, param);")
    private static native void consoleLog(String msg, String param);

    @JSBody(params = {"text", "param"}, script = "console.groupCollapsed(text, param);")
    private static native void groupCollapsed(String text, String param);

    @JSBody(script = "console.groupEnd();")
    private static native void groupEnd();

    @JSBody(params = "text", script = "console.log(text);")
    public static native void print(String text);

    @JSBody(script = "globalThis.__perfFirstFrame = Date.now();")
    private static native void markFirstFrameJS();

    @JSBody(script = "return globalThis.canvas;")
    private static native JSObject getGlobalCanvas();

    // === Asset preloading ===

    @JSBody(script = "return globalThis.__preloadedAssets ? Object.keys(globalThis.__preloadedAssets).length : 0;")
    private static native int getPreloadedAssetCount();

    @JSBody(params = "index", script = "var keys = Object.keys(globalThis.__preloadedAssets); return keys[index] || null;")
    private static native String getPreloadedAssetPath(int index);

    @JSBody(params = "path", script = "var entry = globalThis.__preloadedAssets[path]; return entry ? entry.type : null;")
    private static native String getPreloadedAssetType(String path);

    @JSBody(params = "path", script = "var entry = globalThis.__preloadedAssets[path]; if (!entry || !entry.data) return null; return entry.data;")
    private static native ArrayBuffer getPreloadedAssetArrayBuffer(String path);

    @JSBody(script = "delete globalThis.__preloadedAssets;")
    private static native void clearPreloadedAssets();

    private void preloadAssetsIntoStorage() {
        long transferStart = System.currentTimeMillis();
        System.out.println("[PERF] phase=js_to_java_transfer start=" + transferStart);
        int count = getPreloadedAssetCount();
        if (count == 0) {
            System.out.println("[MiniGameApplication] No preloaded assets found");
            System.out.println("[PERF] phase=js_to_java_transfer end=" + System.currentTimeMillis() + " dur=" + (System.currentTimeMillis() - transferStart) + " files=0 dirs=0 bytes=0");
            System.out.println("[PERF] phase=js_to_java_transfer total_dur=" + (System.currentTimeMillis() - transferStart));
            return;
        }
        System.out.println("[PERF] phase=js_to_java_transfer asset_count=" + count);
        System.out.println("[MiniGameApplication] Preloading " + count + " assets into InternalStorage...");
        long loopStart = System.currentTimeMillis();
        int fileCount = 0;
        int dirCount = 0;
        long totalBytes = 0;
        for (int i = 0; i < count; i++) {
            String path = getPreloadedAssetPath(i);
            if (path == null) continue;
            String type = getPreloadedAssetType(path);
            if ("dir".equals(type)) {
                files.internalStorage.putFolderInternal(path);
                dirCount++;
            } else {
                ArrayBuffer arrayBuffer = getPreloadedAssetArrayBuffer(path);
                if (arrayBuffer != null) {
                    Int8Array int8Array = new Int8Array(arrayBuffer);
                    byte[] bytes = TypedArrays.toByteArray(int8Array);
                    totalBytes += bytes.length;
                    files.internalStorage.putFileInternal(path, bytes);
                    fileCount++;
                }
            }
        }
        long loopEnd = System.currentTimeMillis();
        System.out.println("[PERF] phase=js_to_java_transfer end=" + loopEnd + " dur=" + (loopEnd - loopStart) + " files=" + fileCount + " dirs=" + dirCount + " bytes=" + totalBytes);
        System.out.println("[MiniGameApplication] Asset preloading complete");
        clearPreloadedAssets();
        System.out.println("[PERF] phase=js_to_java_transfer total_dur=" + (System.currentTimeMillis() - transferStart));
    }

    // === Native library loading ===

    private void initNativeLibraries() {
        initGdxLibrary();
    }

    protected void initGdxLibrary() {
        // On WeChat, libraries are loaded by game.js via require()
        // before the Java code starts. Nothing to do here.
    }

    protected MiniGameGraphics createGraphics(MiniGameApplicationConfiguration config) {
        return new MiniGameGraphics(config);
    }

    // === Application interface ===

    public MiniGameApplicationConfiguration getConfig() {
        return config;
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return curListener;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public Files getFiles() {
        return files;
    }

    @Override
    public Net getNet() {
        return net;
    }

    @Override
    public void log(String tag, String message) {
        if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_INFO) getApplicationLogger().log(tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_ERROR) getApplicationLogger().error(tag, message, exception);
    }

    @Override
    public void debug(String tag, String message) {
        if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        if (logLevel >= LOG_DEBUG) getApplicationLogger().debug(tag, message, exception);
    }

    @Override
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public void setApplicationLogger(ApplicationLogger applicationLogger) {
        this.logger = applicationLogger;
    }

    @Override
    public ApplicationLogger getApplicationLogger() {
        return logger;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.WebGL;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public long getJavaHeap() {
        return 0;
    }

    @Override
    public long getNativeHeap() {
        return 0;
    }

    @Override
    public Preferences getPreferences(String name) {
        Preferences pref = prefs.get(name);
        if (pref == null) {
            pref = new MiniGamePreferences(config.storagePrefix + ":" + name, config.shouldEncodePreference);
            prefs.put(name, pref);
        }
        return pref;
    }

    @Override
    public Clipboard getClipboard() {
        return clipboard;
    }

    @Override
    public void postRunnable(Runnable runnable) {
        runnables.add(runnable);
    }

    @Override
    public void exit() {
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.removeValue(listener, true);
        }
    }

    public enum AppState {
        INIT,
        APP_LOOP
    }
}
