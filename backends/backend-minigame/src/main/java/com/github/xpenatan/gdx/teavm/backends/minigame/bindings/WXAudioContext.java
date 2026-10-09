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

    /** Adds a listener; WeChat keeps every listener added until it is removed with offEnded. */
    @JSMethod
    void onEnded(AudioCallback callback);

    /** Removes a listener added with onEnded; it must be the same function object. */
    @JSMethod
    void offEnded(AudioCallback callback);

    /** Removes every onEnded listener. */
    @JSMethod
    void offEnded();

    /** Adds a listener; WeChat keeps every listener added until it is removed with offError. */
    @JSMethod
    void onError(AudioCallback callback);

    /** Removes a listener added with onError; it must be the same function object. */
    @JSMethod
    void offError(AudioCallback callback);

    /** Removes every onError listener. */
    @JSMethod
    void offError();

    @JSMethod
    void onStop(AudioCallback callback);

    @JSMethod
    void onPlay(AudioCallback callback);

    @JSMethod
    void onPause(AudioCallback callback);

    @JSMethod
    void destroy();
}
