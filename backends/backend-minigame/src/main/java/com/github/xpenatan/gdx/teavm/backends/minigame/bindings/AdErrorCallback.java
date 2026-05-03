package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface AdErrorCallback extends JSObject {
    void invoke(JSObject error);  // { errCode: number, errMsg: string }
}
