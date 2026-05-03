package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

/**
 * Utility accessors for WX ad callback parameters.
 */
public final class WXAdUtils {
    private WXAdUtils() {}

    @JSBody(params = "error", script = "return error.errCode;")
    public static native int getAdErrorCode(JSObject error);

    @JSBody(params = "error", script = "return error.errMsg;")
    public static native String getAdErrorMessage(JSObject error);

    @JSBody(params = "result", script = "return result.isEnded;")
    public static native boolean isVideoEnded(JSObject result);

    @JSBody(params = "size", script = "return size.width;")
    public static native double getResizeWidth(JSObject size);

    @JSBody(params = "size", script = "return size.height;")
    public static native double getResizeHeight(JSObject size);
}
