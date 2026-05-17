package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.AnimationFrameCallback;
import org.teavm.jso.typedarrays.ArrayBuffer;

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

    @JSBody(script = "return wx.getWindowInfo().pixelRatio;")
    public static native double getPixelRatio();

    @JSBody(script = "return wx.getWindowInfo().screenWidth;")
    public static native double getScreenWidth();

    @JSBody(script = "return wx.getWindowInfo().screenHeight;")
    public static native double getScreenHeight();

    @JSBody(script = "return wx.getDeviceInfo().platform;")
    public static native String getPlatform();

    // === Safe Area ===

    @JSBody(script = "var sa = wx.getWindowInfo().safeArea; return sa ? sa.top : 0;")
    public static native double getSafeAreaTop();

    @JSBody(script = "var sa = wx.getWindowInfo().safeArea; var wi = wx.getWindowInfo(); return sa ? (wi.screenHeight - sa.bottom) : 0;")
    public static native double getSafeAreaBottom();

    @JSBody(script = "var sa = wx.getWindowInfo().safeArea; return sa ? sa.left : 0;")
    public static native double getSafeAreaLeft();

    @JSBody(script = "var sa = wx.getWindowInfo().safeArea; var wi = wx.getWindowInfo(); return sa ? (wi.screenWidth - sa.right) : 0;")
    public static native double getSafeAreaRight();

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
    public static native void onHide(OnHideCallback callback);

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

    // === File System Sync Operations ===

    @JSBody(params = {"fsm", "path"}, script = "try { return fsm.readFileSync(path); } catch(e) { return null; }")
    public static native ArrayBuffer fsReadFileSync(JSObject fsm, String path);

    @JSBody(params = {"fsm", "path"}, script = "try { return fsm.readFileSync(path, 'utf-8'); } catch(e) { return null; }")
    public static native String fsReadFileSyncText(JSObject fsm, String path);

    @JSBody(params = {"fsm", "path", "data"}, script = "fsm.writeFileSync(path, data);")
    public static native void fsWriteFileSync(JSObject fsm, String path, JSObject data);

    @JSBody(params = {"fsm", "path"}, script = "try { fsm.unlinkSync(path); return true; } catch(e) { return false; }")
    public static native boolean fsUnlinkSync(JSObject fsm, String path);

    @JSBody(params = {"fsm", "path"}, script = "try { fsm.mkdirSync(path, true); } catch(e) { }")
    public static native void fsMkdirSync(JSObject fsm, String path);

    @JSBody(params = {"fsm", "path"}, script = "try { fsm.rmdirSync(path, true); return true; } catch(e) { return false; }")
    public static native boolean fsRmdirSync(JSObject fsm, String path);

    @JSBody(params = {"fsm", "path"}, script = "try { return fsm.readdirSync(path); } catch(e) { return []; }")
    public static native String[] fsReaddirSync(JSObject fsm, String path);

    @JSBody(params = {"fsm", "path"}, script = "try { var s = fsm.statSync(path); return s.isDirectory(); } catch(e) { return false; }")
    public static native boolean fsIsDirectory(JSObject fsm, String path);

    @JSBody(params = {"fsm", "path"}, script = "try { fsm.accessSync(path); return true; } catch(e) { return false; }")
    public static native boolean fsExists(JSObject fsm, String path);

    @JSBody(params = {"fsm", "path"}, script = "try { var s = fsm.statSync(path); return s.size; } catch(e) { return 0; }")
    public static native double fsFileSize(JSObject fsm, String path);

    @JSBody(params = {"fsm", "oldPath", "newPath"}, script = "try { fsm.renameSync(oldPath, newPath); return true; } catch(e) { return false; }")
    public static native boolean fsRenameSync(JSObject fsm, String oldPath, String newPath);

    // === Ads ===

    @JSBody(params = "adUnitId", script = "return wx.createRewardedVideoAd({adUnitId: adUnitId});")
    public static native WXRewardedVideoAd createRewardedVideoAd(String adUnitId);

    @JSBody(params = "adUnitId", script = "return wx.createInterstitialAd({adUnitId: adUnitId});")
    public static native WXInterstitialAd createInterstitialAd(String adUnitId);

    @JSBody(params = "options", script = "return wx.createCustomAd(options);")
    public static native WXCustomAd createCustomAd(JSObject options);

    /**
     * Build options for wx.createCustomAd.
     * @param adIntervals Auto-refresh interval in seconds (≥30). 0 = pass 30 (minimum).
     */
    @JSBody(params = {"adUnitId", "left", "top", "width", "adIntervals"},
        script = "return {adUnitId: adUnitId, adIntervals: adIntervals > 0 ? adIntervals : 30, style: {left: left, top: top, width: width}};")
    public static native JSObject createCustomAdOptions(String adUnitId, double left, double top, double width, double adIntervals);
}
