package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSMethod;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Overlay type for WeChat canvas object returned by wx.createCanvas().
 * TeaVM overlay types are duck-typed — WeChat canvas has the same interface.
 */
public interface WXCanvas extends JSObject {

    @JSProperty
    int getWidth();

    @JSProperty
    void setWidth(int w);

    @JSProperty
    int getHeight();

    @JSProperty
    void setHeight(int h);

    @JSMethod
    JSObject getContext(String type);
}
