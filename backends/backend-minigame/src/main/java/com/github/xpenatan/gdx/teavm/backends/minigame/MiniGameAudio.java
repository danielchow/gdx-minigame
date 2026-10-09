package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WXAudioContext;

/**
 * Audio implementation using wx.createInnerAudioContext().
 * No Howler.js dependency — WeChat has native MP3 support.
 *
 * Uses a pool of audio contexts to stay within WeChat's ~10 concurrent
 * InnerAudioContext limit. Short sound effects share contexts from the pool, which use the
 * WebAudio implementation, while music tracks keep dedicated (unpooled) default contexts.
 */
public class MiniGameAudio implements Audio {

    private static final int MAX_POOL_SIZE = 10;

    /** Idle voices ready for reuse. */
    private final Array<Voice> pool = new Array<>();

    /** Voices currently owned by a playback (playing or paused), in FIFO order for stealing. */
    private final Array<Voice> active = new Array<>();

    /** Music instances that have not been disposed, for suspend and resume. */
    private final Array<MiniGameMusic> musics = new Array<>();

    private boolean hidden;
    private boolean interrupted;

    /**
     * A pooled context and the sound playback that currently owns it. WeChat's onEnded and
     * onError add a listener on every call, so each context gets exactly one of each when it is
     * created, and they dispatch to the current owner instead of being registered per play.
     */
    private final class Voice {
        final WXAudioContext ctx;
        MiniGameSound owner;
        int soundId;
        /** Paused by the game; such a voice is not resumed when audio suspension ends. */
        boolean paused;
        /** The source last assigned to ctx; assigning src reloads the audio even when unchanged. */
        String src;

        Voice() {
            ctx = WX.createInnerAudioContext(true);
            ctx.onEnded(() -> finished(this));
            ctx.onError(() -> {
                // Assign the source again on the next play, so a failed load is retried
                src = null;
                finished(this);
            });
        }

        void setSrc(String src) {
            if (src.equals(this.src)) return;
            ctx.setSrc(src);
            this.src = src;
        }
    }

    /**
     * Acquire a voice from the pool (or create a new one if under the limit).
     * If the pool is empty and we've hit the limit, steal the oldest active voice.
     */
    private Voice acquire(MiniGameSound owner, int soundId) {
        Voice voice;
        if (pool.size > 0) {
            voice = pool.pop();
        } else if (active.size < MAX_POOL_SIZE) {
            voice = new Voice();
        } else {
            // Steal the oldest active voice and detach it from its previous owner
            voice = active.removeIndex(0);
            voice.owner.activePlaybacks.remove(voice.soundId);
            voice.ctx.stop();
            voice.ctx.setCurrentTime(0);
        }
        voice.owner = owner;
        voice.soundId = soundId;
        voice.paused = false;
        active.add(voice);
        return voice;
    }

    private void finished(Voice voice) {
        // Ignore a late event for a voice that was already stopped and returned to the pool
        if (voice.owner == null) return;
        voice.owner.activePlaybacks.remove(voice.soundId);
        release(voice);
    }

    /**
     * Return a voice to the pool after its playback finished or was stopped.
     */
    private void release(Voice voice) {
        voice.owner = null;
        voice.ctx.stop();
        voice.ctx.setCurrentTime(0);
        active.removeValue(voice, true);
        // Guard against double-release: don't re-add if already in pool
        if (pool.contains(voice, true)) {
            return;
        }
        if (pool.size < MAX_POOL_SIZE) {
            pool.add(voice);
        } else {
            voice.ctx.offEnded();
            voice.ctx.offError();
            voice.ctx.destroy();
        }
    }

    /** True while the mini game is hidden or the system has interrupted audio. */
    public boolean isSuspended() {
        return hidden || interrupted;
    }

    /** Called on wx.onHide: pauses playing sounds and music until {@link #onShow()}. */
    public void onHide() {
        boolean wasSuspended = isSuspended();
        hidden = true;
        if (!wasSuspended) pauseForSuspend();
    }

    /**
     * Called on wx.onShow. A visible game should have sound, so this also ends an interruption
     * whose end event was missed; while it really lasts, WeChat rejects the play() calls anyway.
     */
    public void onShow() {
        boolean wasSuspended = isSuspended();
        hidden = false;
        interrupted = false;
        if (wasSuspended) resumeAfterSuspend();
    }

    /** Called on wx.onAudioInterruptionBegin (phone call, alarm, Siri, ...). */
    public void onInterruptionBegin() {
        boolean wasSuspended = isSuspended();
        interrupted = true;
        if (!wasSuspended) pauseForSuspend();
    }

    /** Called on wx.onAudioInterruptionEnd. */
    public void onInterruptionEnd() {
        boolean wasSuspended = isSuspended();
        interrupted = false;
        if (wasSuspended && !isSuspended()) resumeAfterSuspend();
    }

    // Only what the game considers playing is paused and later resumed, so anything the game
    // paused itself, before or during the suspension, stays paused.
    private void pauseForSuspend() {
        for (int i = 0; i < active.size; i++) {
            Voice voice = active.get(i);
            if (!voice.paused) voice.ctx.pause();
        }
        for (int i = 0; i < musics.size; i++) {
            MiniGameMusic music = musics.get(i);
            if (music.isPlaying) music.audioContext.pause();
        }
    }

    private void resumeAfterSuspend() {
        for (int i = 0; i < active.size; i++) {
            Voice voice = active.get(i);
            if (!voice.paused) voice.ctx.play();
        }
        for (int i = 0; i < musics.size; i++) {
            MiniGameMusic music = musics.get(i);
            if (music.isPlaying) music.audioContext.play();
        }
    }

    private static String toAssetPath(String path) {
        if (path.startsWith("assets/")) return path;
        return "assets/" + path;
    }

    @Override
    public MiniGameSound newSound(FileHandle fileHandle) {
        return new MiniGameSound(this, toAssetPath(fileHandle.path()));
    }

    @Override
    public MiniGameMusic newMusic(FileHandle fileHandle) {
        return new MiniGameMusic(this, fileHandle);
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
     * each {@code play()} call acquires a voice from the shared pool, sets its src
     * if it holds another sound, plays it, and the voice's onEnded listener returns it to the pool when done.
     */
    public static class MiniGameSound implements com.badlogic.gdx.audio.Sound {
        private static long nextId = 1;

        private final MiniGameAudio audio;
        private final String src;
        private float volume = 1.0f;
        private float pitch = 1.0f;
        private float pan = 0.0f;
        private boolean disposed = false;

        /** Maps soundId → active voice for per-playback control. */
        private final IntMap<Voice> activePlaybacks = new IntMap<>();

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
            // A one-shot effect played while suspended would only start, out of context, when
            // the game returns; a loop is kept and starts with the rest of the audio.
            if (!looping && audio.isSuspended()) return -1;

            long id = nextId++;
            final int soundId = (int) id;
            Voice voice = audio.acquire(this, soundId);
            voice.setSrc(src);
            WXAudioContext ctx = voice.ctx;
            ctx.setVolume(vol);
            ctx.setLoop(looping);
            ctx.setCurrentTime(0);
            activePlaybacks.put(soundId, voice);

            if (!audio.isSuspended()) ctx.play();
            return id;
        }

        @Override
        public void stop() {
            // Stop all active playbacks for this sound
            for (IntMap.Entry<Voice> entry : activePlaybacks.entries()) {
                audio.release(entry.value);
            }
            activePlaybacks.clear();
        }

        @Override
        public void stop(long soundId) {
            Voice voice = activePlaybacks.remove((int) soundId);
            if (voice != null) {
                audio.release(voice);
            }
        }

        @Override
        public void pause() {
            for (IntMap.Entry<Voice> entry : activePlaybacks.entries()) {
                pause(entry.value);
            }
        }

        @Override
        public void resume() {
            for (IntMap.Entry<Voice> entry : activePlaybacks.entries()) {
                resume(entry.value);
            }
        }

        @Override
        public void pause(long soundId) {
            Voice voice = activePlaybacks.get((int) soundId);
            if (voice != null) {
                pause(voice);
            }
        }

        @Override
        public void resume(long soundId) {
            Voice voice = activePlaybacks.get((int) soundId);
            if (voice != null) {
                resume(voice);
            }
        }

        private void pause(Voice voice) {
            voice.paused = true;
            voice.ctx.pause();
        }

        private void resume(Voice voice) {
            voice.paused = false;
            if (!audio.isSuspended()) voice.ctx.play();
        }

        @Override
        public void setVolume(long soundId, float volume) {
            Voice voice = activePlaybacks.get((int) soundId);
            if (voice != null) {
                voice.ctx.setVolume(volume);
            }
        }

        @Override
        public void setPitch(long soundId, float pitch) {
            // WeChat InnerAudioContext doesn't support pitch
        }

        @Override
        public void setPan(long soundId, float pan, float volume) {
            Voice voice = activePlaybacks.get((int) soundId);
            if (voice != null) {
                voice.ctx.setVolume(volume);
            }
        }

        @Override
        public void setLooping(long soundId, boolean looping) {
            Voice voice = activePlaybacks.get((int) soundId);
            if (voice != null) {
                voice.ctx.setLoop(looping);
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
        private final MiniGameAudio audio;
        private final WXAudioContext audioContext;
        private OnCompletionListener completionListener;
        /** Whether the game wants this music playing; it stays true while audio is suspended. */
        private boolean isPlaying = false;
        private float volume = 1.0f;

        public MiniGameMusic(MiniGameAudio audio, FileHandle fileHandle) {
            this.audio = audio;
            audioContext = WX.createInnerAudioContext();
            audioContext.setSrc(toAssetPath(fileHandle.path()));
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
            audio.musics.add(this);
        }

        @Override
        public void play() {
            if (!audio.isSuspended()) audioContext.play();
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
            audio.musics.removeValue(this, true);
            isPlaying = false;
            audioContext.destroy();
        }

        @Override
        public void setOnCompletionListener(OnCompletionListener listener) {
            this.completionListener = listener;
        }
    }
}
