package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSMethod;
import org.teavm.jso.JSObject;

/**
 * Overlay type for wx.createRewardedVideoAd().
 */
public interface WXRewardedVideoAd extends JSObject {

    @JSMethod JSObject load();   // Returns Promise
    @JSMethod JSObject show();   // Returns Promise
    @JSMethod void destroy();

    @JSMethod void onLoad(AdCallback callback);
    @JSMethod void offLoad(AdCallback callback);
    @JSMethod void onError(AdErrorCallback callback);
    @JSMethod void offError(AdErrorCallback callback);
    @JSMethod void onClose(AdCloseCallback callback);
    @JSMethod void offClose(AdCloseCallback callback);
}
