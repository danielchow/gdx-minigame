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
    boolean getLoop();

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
    void onEnded(AudioCallback callback);

    @JSMethod
    void onError(AudioCallback callback);

    @JSMethod
    void onStop(AudioCallback callback);

    @JSMethod
    void onPlay(AudioCallback callback);

    @JSMethod
    void onPause(AudioCallback callback);

    @JSMethod
    void destroy();
}
