package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Callback for wx.onWindowStateChange: "minimize", "normalize" or "maximize".
 */
@JSFunctor
public interface WindowStateCallback extends JSObject {
    void invoke(String state);
}
