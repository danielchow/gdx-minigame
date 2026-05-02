package com.github.xpenatan.gdx.teavm.backends.minigame.dom;

import org.teavm.jso.JSBody;

public class MiniGameNavigator {
    @JSBody(script = "return wx.getSystemInfoSync().platform;")
    public static native String getPlatform();
}
