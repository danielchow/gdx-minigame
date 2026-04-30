package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Callback for wx.onTouchStart/Move/End/Cancel.
 */
@JSFunctor
public interface TouchCallback extends JSObject {
    void invoke(JSObject event);
}
