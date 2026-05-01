package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface OnHideCallback extends JSObject {
    void invoke();
}
