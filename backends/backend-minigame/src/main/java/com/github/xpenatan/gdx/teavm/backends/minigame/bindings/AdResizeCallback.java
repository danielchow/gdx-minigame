package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface AdResizeCallback extends JSObject {
    void invoke(JSObject size);  // { width: number, height: number }
}
