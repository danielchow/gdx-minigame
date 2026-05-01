package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.AnimationFrameCallback;

/**
 * Static bindings for the WeChat Mini Game wx.* API.
 * All methods are @JSBody static calls — no DOM references.
 */
public final class WX {

    private WX() {} // Prevent instantiation

    // === Canvas ===

    @JSBody(script = "return wx.createCanvas();")
    public static native JSObject createCanvas();

    // === System Info ===

    @JSBody(script = "return wx.getSystemInfoSync().pixelRatio;")
    public static native double getPixelRatio();

    @JSBody(script = "return wx.getSystemInfoSync().screenWidth;")
    public static native double getScreenWidth();

    @JSBody(script = "return wx.getSystemInfoSync().screenHeight;")
    public static native double getScreenHeight();

    @JSBody(script = "return wx.getSystemInfoSync().platform;")
    public static native String getPlatform();

    // === Animation Frame ===

    @JSBody(params = "callback", script = "return requestAnimationFrame(callback);")
    public static native int requestAnimationFrame(AnimationFrameCallback callback);

    @JSBody(params = "id", script = "cancelAnimationFrame(id);")
    public static native void cancelAnimationFrame(int id);

    // === Frame Rate ===

    @JSBody(params = "fps", script = "wx.setPreferredFramesPerSecond(fps);")
    public static native void setPreferredFramesPerSecond(int fps);

    // === Touch Events ===

    @JSBody(params = "callback", script = "wx.onTouchStart(callback);")
    public static native void onTouchStart(TouchCallback callback);

    @JSBody(params = "callback", script = "wx.onTouchMove(callback);")
    public static native void onTouchMove(TouchCallback callback);

    @JSBody(params = "callback", script = "wx.onTouchEnd(callback);")
    public static native void onTouchEnd(TouchCallback callback);

    @JSBody(params = "callback", script = "wx.onTouchCancel(callback);")
    public static native void onTouchCancel(TouchCallback callback);

    // === Audio ===

    @JSBody(script = "return wx.createInnerAudioContext();")
    public static native WXAudioContext createInnerAudioContext();

    // === Storage ===

    @JSBody(params = {"key", "value"}, script = "wx.setStorageSync(key, value);")
    public static native void setStorageSync(String key, String value);

    @JSBody(params = "key", script = "return wx.getStorageSync(key);")
    public static native String getStorageSync(String key);

    @JSBody(params = "key", script = "wx.removeStorageSync(key);")
    public static native void removeStorageSync(String key);

    @JSBody(script = "return wx.getStorageInfoSync().keys;")
    public static native String[] getStorageInfoKeys();

    // === File System ===

    @JSBody(script = "return wx.getFileSystemManager();")
    public static native JSObject getFileSystemManager();

    @JSBody(script = "return wx.env.USER_DATA_PATH;")
    public static native String getUserDataPath();

    // === HTTP ===

    @JSBody(params = "options", script = "wx.request(options);")
    public static native void request(JSObject options);

    // === Lifecycle ===

    @JSBody(params = "callback", script = "wx.onShow(callback);")
    public static native void onShow(LifecycleCallback callback);

    @JSBody(params = "callback", script = "wx.onHide(callback);")
    public static native void onHide(Runnable callback);

    // === Clipboard ===

    @JSBody(params = "data", script = "wx.setClipboardData({data: data});")
    public static native void setClipboardData(String data);

    // === Keyboard ===

    @JSBody(params = "options", script = "wx.showKeyboard(options);")
    public static native void showKeyboard(JSObject options);

    @JSBody(script = "wx.hideKeyboard();")
    public static native void hideKeyboard();

    @JSBody(params = "callback", script = "wx.onKeyboardInput(callback);")
    public static native void onKeyboardInput(KeyboardCallback callback);

    // === Vibration ===

    @JSBody(script = "wx.vibrateLong();")
    public static native void vibrateLong();

    @JSBody(script = "wx.vibrateShort({type:'heavy'});")
    public static native void vibrateShort();

    // === Script Loading ===

    @JSBody(params = "path", script = "return require(path);")
    public static native JSObject requireModule(String path);

    // === Window Resize ===

    @JSBody(params = "callback", script = "wx.onWindowResize(callback);")
    public static native void onWindowResize(LifecycleCallback callback);
}
