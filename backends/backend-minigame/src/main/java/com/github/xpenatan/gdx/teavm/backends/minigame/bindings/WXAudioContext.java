package com.github.xpenatan.gdx.teavm.backends.minigame.bindings;

import org.teavm.jso.JSMethod;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Overlay type for the audio context returned by wx.createInnerAudioContext().
 */
public interface WXAudioContext extends JSObject {

    @JSProperty
    void setSrc(String src);

    @JSProperty
    String getSrc();

    @JSProperty
    void setLoop(boolean loop);

    @JSProperty
    void setVolume(double volume);

    @JSProperty
    double getDuration();

    @JSProperty
    double getCurrentTime();

    @JSProperty
    void setCurrentTime(double time);

    @JSMethod
    void play();

    @JSMethod
    void pause();

    @JSMethod
    void stop();

    @JSMethod
    void seek(double position);

    @JSMethod
    void onEnded(Runnable callback);

    @JSMethod
    void onError(Runnable callback);

    @JSMethod
    void onStop(Runnable callback);

    @JSMethod
    void onPlay(Runnable callback);

    @JSMethod
    void onPause(Runnable callback);

    @JSMethod
    void destroy();
}
