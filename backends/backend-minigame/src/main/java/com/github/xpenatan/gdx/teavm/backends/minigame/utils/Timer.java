package com.github.xpenatan.gdx.teavm.backends.minigame.utils;

import org.teavm.jso.JSBody;
import org.teavm.jso.browser.TimerHandler;

/**
 * Timer utility using bare setTimeout/setInterval (no DOM Window dependency).
 * Ported from backend-web Timer, replacing Window calls with @JSBody.
 */
public abstract class Timer {

    private boolean isRepeating;
    private Integer timerId = null;
    private int cancelCounter = 0;

    public final boolean isRunning() {
        return timerId != null;
    }

    public void cancel() {
        if (!isRunning()) {
            return;
        }
        cancelCounter++;
        if (isRepeating) {
            clearInterval(timerId);
        } else {
            clearTimeout(timerId);
        }
        timerId = null;
    }

    public abstract void run();

    public void schedule(int delayMillis) {
        if (delayMillis < 0) {
            throw new IllegalArgumentException("must be non-negative");
        }
        if (isRunning()) {
            cancel();
        }
        isRepeating = false;
        timerId = setTimeout(createCallback(this, cancelCounter), delayMillis);
    }

    public void scheduleRepeating(int periodMillis) {
        if (periodMillis <= 0) {
            throw new IllegalArgumentException("must be positive");
        }
        if (isRunning()) {
            cancel();
        }
        isRepeating = true;
        timerId = setInterval(createCallback(this, cancelCounter), periodMillis);
    }

    final void fire(int scheduleCancelCounter) {
        if (scheduleCancelCounter != cancelCounter) {
            return;
        }
        if (!isRepeating) {
            timerId = null;
        }
        run();
    }

    private static TimerHandler createCallback(Timer timer, int cancelCounter) {
        return new TimerHandler() {
            @Override
            public void onTimer() {
                timer.fire(cancelCounter);
            }
        };
    }

    @JSBody(params = {"func", "time"}, script = "return setTimeout(func, time);")
    private static native int setTimeout(TimerHandler func, int time);

    @JSBody(params = {"func", "time"}, script = "return setInterval(func, time);")
    private static native int setInterval(TimerHandler func, int time);

    @JSBody(params = "timerId", script = "clearInterval(timerId);")
    private static native void clearInterval(int timerId);

    @JSBody(params = "timerId", script = "clearTimeout(timerId);")
    private static native void clearTimeout(int timerId);
}
