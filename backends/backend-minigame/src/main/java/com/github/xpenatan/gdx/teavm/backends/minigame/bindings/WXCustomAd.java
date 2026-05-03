package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSMethod;
import org.teavm.jso.JSObject;

/**
 * Overlay type for wx.createCustomAd().
 */
public interface WXCustomAd extends JSObject {

    @JSMethod JSObject show();   // Returns Promise
    @JSMethod JSObject hide();   // Returns Promise
    @JSMethod void destroy();
    @JSMethod boolean isShow();

    @JSMethod void onLoad(AdCallback callback);
    @JSMethod void offLoad(AdCallback callback);
    @JSMethod void onError(AdErrorCallback callback);
    @JSMethod void offError(AdErrorCallback callback);
    @JSMethod void onClose(AdCallback callback);
    @JSMethod void offClose(AdCallback callback);
    @JSMethod void onHide(AdCallback callback);
    @JSMethod void offHide(AdCallback callback);
    @JSMethod void onResize(AdResizeCallback callback);
    @JSMethod void offResize(AdResizeCallback callback);
}
