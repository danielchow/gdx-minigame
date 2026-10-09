package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameApplication;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameAudio;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameAudio.MiniGameMusic;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameAudio.MiniGameSound;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Runs a real MiniGameApplication against the mocked wx object of runtime-robustness.test.mjs.
 * Node drives frames, wx events and audio callbacks, and calls back in through {@link Command}.
 */
public final class RuntimeRobustnessProbe {
    private static boolean throwOnRender;

    public static void main(String[] args) {
        MiniGameApplication app = new MiniGameApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                Gdx.input.setInputProcessor(new InputAdapter() {
                    @Override
                    public boolean touchDown(int x, int y, int pointer, int button) {
                        record("touchDown", pointer);
                        return true;
                    }

                    @Override
                    public boolean touchUp(int x, int y, int pointer, int button) {
                        record("touchUp", pointer);
                        return true;
                    }

                    @Override
                    public boolean touchCancelled(int x, int y, int pointer, int button) {
                        record("touchCancelled", pointer);
                        return true;
                    }
                });
                record("create", 0);
            }

            @Override
            public void render() {
                record("render", 0);
                if (throwOnRender) throw new IllegalStateException("expected-probe-failure");
            }
        }, new MiniGameApplicationConfiguration()) {
            @Override
            protected void onError(Throwable error) {
                record("onError", 0);
                super.onError(error);
            }

            @Override
            protected void onFatalError(Throwable error) {
                record("onFatalError", 0);
                super.onFatalError(error);
            }
        };

        MiniGameAudio audio = (MiniGameAudio) app.getAudio();
        MiniGameSound sound = audio.newSound(new FileHandle("effect.mp3"));
        MiniGameMusic[] music = new MiniGameMusic[3];
        for (int i = 0; i < music.length; i++) music[i] = audio.newMusic(new FileHandle("music" + i + ".mp3"));

        register((command, arg) -> {
            switch (command) {
                case "playSound": return (int) sound.play();
                case "loopSound": return (int) sound.loop();
                case "pauseSound": sound.pause(arg); return 0;
                case "resumeSound": sound.resume(arg); return 0;
                case "stopSound": sound.stop(arg); return 0;
                case "stopAllSounds": sound.stop(); return 0;
                case "playMusic": music[arg].play(); return 0;
                case "pauseMusic": music[arg].pause(); return 0;
                case "musicPlaying": return music[arg].isPlaying() ? 1 : 0;
                case "isTouched": return Gdx.input.isTouched() ? 1 : 0;
                case "throwOnRender": throwOnRender = true; return 0;
                // DesktopDisplay.setOrientation (libGT) resizes the "window" this way during win animations.
                case "setWindowedMode": return Gdx.graphics.setWindowedMode(arg, arg * 2) ? 1 : 0;
                case "supportsDisplayModeChange": return Gdx.graphics.supportsDisplayModeChange() ? 1 : 0;
                case "graphicsWidth": return Gdx.graphics.getWidth();
                default: throw new IllegalArgumentException(command);
            }
        });
    }

    @JSFunctor
    interface Command extends JSObject {
        int run(String command, int arg);
    }

    @JSBody(params = "command", script = "globalThis.__robustnessProbe.command = command;")
    private static native void register(Command command);

    @JSBody(params = {"event", "value"}, script = "globalThis.__robustnessProbe.events.push(event + ':' + value);")
    private static native void record(String event, int value);
}
