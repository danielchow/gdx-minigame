package emu.com.badlogic.gdx.utils.async;

import emu.com.badlogic.gdx.assets.AssetLoadingTask;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.TimerHandler;

public class AsyncResult<T> implements TimerHandler {
    private final AssetLoadingTask loadingTask;
    private boolean started;
    private boolean isDone;
    private Throwable failure;
    private int count;

    AsyncResult(AsyncTask<T> task) {
        loadingTask = (AssetLoadingTask)task;
        setTimeout(this, 0);
    }

    @JSBody(params = {"callback", "ms"}, script = "setTimeout(callback, ms);")
    private static native void setTimeout(TimerHandler callback, int ms);

    public boolean isDone() {
        // finishLoading() polls synchronously and cannot yield to the timer, so run the task
        // inline on the third poll. Never run it again once it has started.
        if(!isDone && ++count > 2) {
            tick();
        }
        return isDone;
    }

    public T get() {
        // AssetLoadingTask catches this and reports the failure through AssetManager.
        if(failure != null) {
            throw new RuntimeException(failure);
        }
        return null;
    }

    @Override
    public void onTimer() {
        tick();
    }

    private void tick() {
        // The timer stays queued when polling has already run the task inline.
        if(started) {
            return;
        }
        started = true;
        try {
            loadingTask.call();
        } catch(Throwable e) {
            failure = e;
        } finally {
            isDone = true;
        }
    }
}
