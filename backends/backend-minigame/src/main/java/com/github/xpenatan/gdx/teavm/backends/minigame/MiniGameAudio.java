package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WXAudioContext;

/**
 * Audio implementation using wx.createInnerAudioContext().
 * No Howler.js dependency — WeChat has native MP3 support.
 *
 * Uses a pool of audio contexts to stay within WeChat's ~10 concurrent
 * InnerAudioContext limit. Short sound effects share contexts from the pool,
 * while music tracks keep dedicated (unpooled) contexts.
 */
public class MiniGameAudio implements Audio {

    private static final int MAX_POOL_SIZE = 10;

    /** Idle contexts ready for reuse. */
    private final Array<WXAudioContext> pool = new Array<>();

    /** Contexts currently playing (acquired but not yet released), in FIFO order for stealing. */
    private final Array<WXAudioContext> active = new Array<>();

    /**
     * Maps each active context to a steal-cleanup callback.
     * When a context is stolen (pool exhausted), the callback removes the
     * stale entry from the previous owner's activePlaybacks map.
     */
    private final ObjectMap<WXAudioContext, Runnable> stealCallbacks = new ObjectMap<>();

    /**
     * Acquire a context from the pool (or create a new one if under the limit).
     * If the pool is empty and we've hit the limit, steal the oldest active context.
     */
    private WXAudioContext acquire() {
        if (pool.size > 0) {
            return pool.pop();
        }
        if (active.size < MAX_POOL_SIZE) {
            return WX.createInnerAudioContext();
        }
        // Steal the oldest active context — notify its previous owner
        WXAudioContext stolen = active.removeIndex(0);
        Runnable cleanup = stealCallbacks.remove(stolen);
        if (cleanup != null) {
            cleanup.run();
        }
        stolen.stop();
        stolen.setCurrentTime(0);
        return stolen;
    }

    /**
     * Return a context to the pool after playback finishes.
     */
    private void release(WXAudioContext ctx) {
        // Clear the onEnded callback to prevent stale fires after manual stop
        ctx.onEnded(() -> {});
        ctx.stop();
        ctx.setCurrentTime(0);
        active.removeValue(ctx, true);
        stealCallbacks.remove(ctx);
        // Guard against double-release: don't re-add if already in pool
        if (pool.contains(ctx, true)) {
            return;
        }
        if (pool.size < MAX_POOL_SIZE) {
            pool.add(ctx);
        } else {
            ctx.destroy();
        }
    }

    /**
     * Track an active context so it can be stolen if the pool is exhausted.
     * @param ctx the context to track
     * @param onSteal cleanup callback to invoke if this context is stolen from its owner
     */
    private void trackActive(WXAudioContext ctx, Runnable onSteal) {
        if (!active.contains(ctx, true)) {
            active.add(ctx);
        }
        stealCallbacks.put(ctx, onSteal);
    }

    /**
     * Remove a context from the active tracking list.
     */
    private void untrackActive(WXAudioContext ctx) {
        active.removeValue(ctx, true);
        stealCallbacks.remove(ctx);
    }

    @Override
    public MiniGameSound newSound(FileHandle fileHandle) {
        return new MiniGameSound(this, fileHandle.path());
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
     * Sound implementation — short audio effects using a pooled audio context approach.
     *
     * Instead of one context per Sound (which exhausts WeChat's ~10 context limit),
     * each {@code play()} call acquires a context from the shared pool, sets its src,
     * plays it, and returns the context when done via the onEnded callback.
     */
    public static class MiniGameSound implements com.badlogic.gdx.audio.Sound {
        private static long nextId = 1;

        private final MiniGameAudio audio;
        private final String src;
        private float volume = 1.0f;
        private float pitch = 1.0f;
        private float pan = 0.0f;
        private boolean disposed = false;

        /** Maps soundId → active context for per-playback control. */
        private final IntMap<WXAudioContext> activePlaybacks = new IntMap<>();

        public MiniGameSound(MiniGameAudio audio, String src) {
            this.audio = audio;
            this.src = src;
        }

        @Override
        public long play() {
            return play(volume, pitch, pan);
        }

        @Override
        public long play(float volume) {
            return play(volume, pitch, pan);
        }

        @Override
        public long play(float volume, float pitch, float pan) {
            return startPlayback(volume, false);
        }

        @Override
        public long loop() {
            return loop(volume, pitch, pan);
        }

        @Override
        public long loop(float volume) {
            return loop(volume, pitch, pan);
        }

        @Override
        public long loop(float volume, float pitch, float pan) {
            return startPlayback(volume, true);
        }

        private long startPlayback(float vol, boolean looping) {
            if (disposed) return -1;

            WXAudioContext ctx = audio.acquire();
            ctx.setSrc(src);
            ctx.setVolume(vol);
            ctx.setLoop(looping);
            ctx.setCurrentTime(0);

            long id = nextId++;
            final int soundId = (int) id;
            activePlaybacks.put(soundId, ctx);

            // Register a steal callback so the stale entry is removed if
            // this context is reclaimed for a new playback
            audio.trackActive(ctx, () -> activePlaybacks.remove(soundId));

            ctx.onEnded(() -> {
                activePlaybacks.remove(soundId);
                audio.untrackActive(ctx);
                audio.release(ctx);
            });

            ctx.play();
            return id;
        }

        @Override
        public void stop() {
            // Stop all active playbacks for this sound
            for (IntMap.Entry<WXAudioContext> entry : activePlaybacks.entries()) {
                WXAudioContext ctx = entry.value;
                audio.untrackActive(ctx);
                audio.release(ctx);
            }
            activePlaybacks.clear();
        }

        @Override
        public void stop(long soundId) {
            WXAudioContext ctx = activePlaybacks.remove((int) soundId);
            if (ctx != null) {
                audio.untrackActive(ctx);
                audio.release(ctx);
            }
        }

        @Override
        public void pause() {
            for (IntMap.Entry<WXAudioContext> entry : activePlaybacks.entries()) {
                entry.value.pause();
            }
        }

        @Override
        public void resume() {
            for (IntMap.Entry<WXAudioContext> entry : activePlaybacks.entries()) {
                entry.value.play();
            }
        }

        @Override
        public void pause(long soundId) {
            WXAudioContext ctx = activePlaybacks.get((int) soundId);
            if (ctx != null) {
                ctx.pause();
            }
        }

        @Override
        public void resume(long soundId) {
            WXAudioContext ctx = activePlaybacks.get((int) soundId);
            if (ctx != null) {
                ctx.play();
            }
        }

        @Override
        public void setVolume(long soundId, float volume) {
            WXAudioContext ctx = activePlaybacks.get((int) soundId);
            if (ctx != null) {
                ctx.setVolume(volume);
            }
        }

        @Override
        public void setPitch(long soundId, float pitch) {
            // WeChat InnerAudioContext doesn't support pitch
        }

        @Override
        public void setPan(long soundId, float pan, float volume) {
            WXAudioContext ctx = activePlaybacks.get((int) soundId);
            if (ctx != null) {
                ctx.setVolume(volume);
            }
        }

        @Override
        public void setLooping(long soundId, boolean looping) {
            WXAudioContext ctx = activePlaybacks.get((int) soundId);
            if (ctx != null) {
                ctx.setLoop(looping);
            }
        }

        @Override
        public void dispose() {
            disposed = true;
            stop();
        }
    }

    /**
     * Music implementation — streaming audio using wx.createInnerAudioContext().
     * Music uses a dedicated (unpooled) context since there's typically only
     * 1 Music instance playing at a time.
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
                if (completionListener != null) {
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
            return audioContext.getLoop();
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
