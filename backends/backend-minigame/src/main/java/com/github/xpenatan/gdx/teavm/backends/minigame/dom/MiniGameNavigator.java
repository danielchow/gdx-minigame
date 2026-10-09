package com.github.xpenatan.gdx.teavm.backends.minigame.dom;

import org.teavm.jso.JSBody;

public class MiniGameNavigator {
    private static String platform;

    /**
     * WeChat's platform name, lowercase: "ios", "android", "ohos", "windows", "mac" or "devtools".
     * Read once: each wx device-info call is a synchronous native call.
     */
    public static String getPlatform() {
        if (platform == null) {
            String value = getDevicePlatform();
            platform = value == null ? "" : value.toLowerCase();
        }
        return platform;
    }

    @JSBody(script = "return wx.getDeviceInfo().platform;")
    private static native String getDevicePlatform();
}
