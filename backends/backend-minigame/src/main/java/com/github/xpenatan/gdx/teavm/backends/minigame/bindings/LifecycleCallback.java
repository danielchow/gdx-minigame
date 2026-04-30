package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Callback for wx.onShow and wx.onWindowResize.
 */
@JSFunctor
public interface LifecycleCallback extends JSObject {
    void invoke(JSObject result);
}
