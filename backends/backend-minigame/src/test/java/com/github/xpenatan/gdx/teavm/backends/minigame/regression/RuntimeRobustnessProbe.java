package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameApplication;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameAudio;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameAudio.MiniGameMusic;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameAudio.MiniGameSound;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameGraphics;
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
                        record("pointerX", x);
                        record("pointerY", y);
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
            public void resize(int width, int height) {
                record("resizeWidth", width);
                record("resizeHeight", height);
            }

            @Override
            public void pause() {
                record("pause", 0);
            }

            @Override
            public void resume() {
                record("resume", 0);
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
        MiniGameSound otherSound = audio.newSound(new FileHandle("other.mp3"));
        MiniGameMusic[] music = new MiniGameMusic[3];
        for (int i = 0; i < music.length; i++) music[i] = audio.newMusic(new FileHandle("music" + i + ".mp3"));

        register((command, arg) -> {
            switch (command) {
                case "playSound": return (int) sound.play();
                case "loopSound": return (int) sound.loop();
                case "playOtherSound": return (int) otherSound.play();
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
                case "graphicsHeight": return Gdx.graphics.getHeight();
                case "readWindowMetrics": {
                    int sum = 0;
                    for (int i = 0; i < arg; i++) {
                        sum += Gdx.graphics.getSafeInsetTop() + Gdx.graphics.getSafeInsetLeft()
                            + Gdx.graphics.getSafeInsetBottom() + Gdx.graphics.getSafeInsetRight()
                            + (int) Gdx.graphics.getDensity();
                    }
                    return sum;
                }
                case "safeInset": {
                    switch (arg) {
                        case 0: return Gdx.graphics.getSafeInsetTop();
                        case 1: return Gdx.graphics.getSafeInsetLeft();
                        case 2: return Gdx.graphics.getSafeInsetBottom();
                        default: return Gdx.graphics.getSafeInsetRight();
                    }
                }
                case "uiutils": {
                    switch (arg) {
                        case 0: return com.badlogic.gdx.scenes.scene2d.utils.UIUtils.isAndroid ? 1 : 0;
                        case 1: return com.badlogic.gdx.scenes.scene2d.utils.UIUtils.isIos ? 1 : 0;
                        default: return "OS X".equals(System.getProperty("os.name")) ? 1 : 0;
                    }
                }
                case "vibrate": Gdx.input.vibrate(arg); return 0;
                case "vibrateType": Gdx.input.vibrate(Input.VibrationType.values()[arg]); return 0;
                case "textInput": {
                    Input.TextInputListener listener = new Input.TextInputListener() {
                        @Override
                        public void input(String text) {
                            record("textInput", 0);
                        }

                        @Override
                        public void canceled() {
                            record("textCanceled", 0);
                        }
                    };
                    if (arg == 0) Gdx.input.getTextInput(listener, "title", "text", "hint");
                    else Gdx.input.getTextInput(listener, "title", "text", "hint", Input.OnscreenKeyboardType.Default);
                    return 0;
                }
                case "exit": Gdx.app.exit(); return 0;
                case "graphicsDensityTimes100": return Math.round(Gdx.graphics.getDensity() * 100);
                case "ppiX": return Math.round(Gdx.graphics.getPpiX());
                case "ppiY": return Math.round(Gdx.graphics.getPpiY());
                // Reaches WebGL20.glTexImage2D, so the release-logging test sees its code.
                case "texImage2D":
                    Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, 1, 1, 0, GL20.GL_RGBA,
                        GL20.GL_UNSIGNED_BYTE, BufferUtils.newByteBuffer(4));
                    return 0;
                case "densityTimes100": return (int) Math.round(((MiniGameGraphics) Gdx.graphics).getNativeScreenDensity() * 100);
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
