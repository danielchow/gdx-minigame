package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WXAudioContext;

/**
 * Audio implementation using wx.createInnerAudioContext().
 * No Howler.js dependency — WeChat has native MP3 support.
 */
public class MiniGameAudio implements Audio {

    @Override
    public MiniGameSound newSound(FileHandle fileHandle) {
        return new MiniGameSound(fileHandle);
    }

    @Override
    public MiniGameMusic newMusic(FileHandle fileHandle) {
        return new MiniGameMusic(fileHandle);
    }

    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
        throw new GdxRuntimeException("AudioDevice not supported by WeChat backend");
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
        throw new GdxRuntimeException("AudioRecorder not supported by WeChat backend");
    }

    @Override
    public String[] getAvailableOutputDevices() {
        return new String[0];
    }

    @Override
    public boolean switchOutputDevice(String deviceIdentifier) {
        return false;
    }

    /**
     * Sound implementation — short audio effects using wx.createInnerAudioContext().
     */
    public static class MiniGameSound implements com.badlogic.gdx.audio.Sound {
        private final WXAudioContext audioContext;
        private float volume = 1.0f;
        private float pitch = 1.0f;
        private float pan = 0.0f;

        public MiniGameSound(FileHandle fileHandle) {
            audioContext = WX.createInnerAudioContext();
            audioContext.setSrc(fileHandle.path());
        }

        @Override
        public long play() {
            audioContext.setVolume(volume);
            audioContext.setCurrentTime(0);
            audioContext.play();
            return 0;
        }

        @Override
        public long play(float volume) {
            audioContext.setVolume(volume);
            audioContext.setCurrentTime(0);
            audioContext.play();
            return 0;
        }

        @Override
        public long play(float volume, float pitch, float pan) {
            audioContext.setVolume(volume);
            audioContext.setCurrentTime(0);
            audioContext.play();
            return 0;
        }

        @Override
        public long loop() {
            audioContext.setLoop(true);
            audioContext.setVolume(volume);
            audioContext.setCurrentTime(0);
            audioContext.play();
            return 0;
        }

        @Override
        public long loop(float volume) {
            audioContext.setLoop(true);
            audioContext.setVolume(volume);
            audioContext.setCurrentTime(0);
            audioContext.play();
            return 0;
        }

        @Override
        public long loop(float volume, float pitch, float pan) {
            audioContext.setLoop(true);
            audioContext.setVolume(volume);
            audioContext.setCurrentTime(0);
            audioContext.play();
            return 0;
        }

        @Override
        public void stop() {
            audioContext.stop();
        }

        @Override
        public void pause() {
            audioContext.pause();
        }

        @Override
        public void resume() {
            audioContext.play();
        }

        @Override
        public void dispose() {
            audioContext.destroy();
        }

        @Override
        public void stop(long soundId) {
            audioContext.stop();
        }

        @Override
        public void pause(long soundId) {
            audioContext.pause();
        }

        @Override
        public void resume(long soundId) {
            audioContext.play();
        }

        @Override
        public void setVolume(long soundId, float volume) {
            audioContext.setVolume(volume);
        }

        @Override
        public void setPitch(long soundId, float pitch) {
            // WeChat InnerAudioContext doesn't support pitch
        }

        @Override
        public void setPan(long soundId, float pan, float volume) {
            audioContext.setVolume(volume);
        }

        @Override
        public void setLooping(long soundId, boolean looping) {
            audioContext.setLoop(looping);
        }
    }

    /**
     * Music implementation — streaming audio using wx.createInnerAudioContext().
     */
    public static class MiniGameMusic implements com.badlogic.gdx.audio.Music {
        private final WXAudioContext audioContext;
        private OnCompletionListener completionListener;
        private boolean isPlaying = false;
        private float volume = 1.0f;

        public MiniGameMusic(FileHandle fileHandle) {
            audioContext = WX.createInnerAudioContext();
            audioContext.setSrc(fileHandle.path());
            audioContext.setVolume(volume);

            audioContext.onEnded(() -> {
                isPlaying = false;
                if(completionListener != null) {
                    completionListener.onCompletion(MiniGameMusic.this);
                }
            });

            audioContext.onError(() -> {
                isPlaying = false;
            });
        }

        @Override
        public void play() {
            audioContext.play();
            isPlaying = true;
        }

        @Override
        public void pause() {
            audioContext.pause();
            isPlaying = false;
        }

        @Override
        public void stop() {
            audioContext.stop();
            isPlaying = false;
        }

        @Override
        public boolean isPlaying() {
            return isPlaying;
        }

        @Override
        public void setLooping(boolean isLooping) {
            audioContext.setLoop(isLooping);
        }

        @Override
        public boolean isLooping() {
            return false; // No getter available
        }

        @Override
        public void setVolume(float volume) {
            this.volume = volume;
            audioContext.setVolume(volume);
        }

        @Override
        public float getVolume() {
            return volume;
        }

        @Override
        public void setPan(float pan, float volume) {
            audioContext.setVolume(volume);
        }

        @Override
        public void setPosition(float position) {
            audioContext.setCurrentTime(position);
        }

        @Override
        public float getPosition() {
            return (float) audioContext.getCurrentTime();
        }

        @Override
        public void dispose() {
            audioContext.destroy();
        }

        @Override
        public void setOnCompletionListener(OnCompletionListener listener) {
            this.completionListener = listener;
        }
    }
}
