package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Callback for wx.onKeyboardInput.
 */
@JSFunctor
public interface KeyboardCallback extends JSObject {
    void invoke(JSObject result);
}
