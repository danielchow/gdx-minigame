package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Overlay type for the object returned by wx.getWindowInfo(). Sizes are in logical (CSS) pixels.
 */
public interface WXWindowInfo extends JSObject {

    @JSProperty
    double getPixelRatio();

    @JSProperty
    double getScreenWidth();

    @JSProperty
    double getScreenHeight();

    /** Null on platforms that report no safe area. */
    @JSProperty
    SafeArea getSafeArea();

    interface SafeArea extends JSObject {
        @JSProperty
        double getTop();

        @JSProperty
        double getLeft();

        @JSProperty
        double getBottom();

        @JSProperty
        double getRight();
    }
}
