package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameAssetLoader;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetInstance;
import emu.com.badlogic.gdx.assets.AssetLoadingTask;
import emu.com.badlogic.gdx.utils.async.AsyncExecutor;
import emu.com.badlogic.gdx.utils.async.AsyncResult;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.TimerHandler;

/** Exercises actual backend futures and loaders; only the host event loop is controlled by Node. */
public final class AsyncCompletionProbe {
    public static void main(String[] args) {
        AssetInstance.setInstance(new MiniGameAssetLoader());
        runManagerCase("inline-success", false, false, false);
        runManagerCase("inline-failure", true, false, false);
        runManagerCase("inline-dependencies", false, false, true);
        runManagerCase("timer-success", false, true, false);
        runManagerCase("timer-failure", true, true, false);
        runManagerCase("timer-error", true, true, false);
        runPollingCase("poll-success", false);
        runPollingCase("poll-failure", true);
        record("main-complete", 1);
    }

    private static AssetManager manager() {
        AssetManager manager = new AssetManager();
        manager.setLogger(new Logger("AsyncCompletionProbe", Logger.NONE));
        return manager;
    }

    private static void runManagerCase(String name, boolean fail, boolean timerFirst, boolean dependencies) {
        AssetManager manager = manager();
        manager.setLoader(ProbeAsset.class, new CountingLoader(name, fail, dependencies));
        // Extensionless names use an in-memory loader and bypass wx file preloading.
        manager.load(name, ProbeAsset.class);
        if (timerFirst) {
            check(!manager.update(), "task must wait for its timer or third poll");
            later(() -> complete(manager, name, fail));
        } else {
            complete(manager, name, fail);
        }
    }

    private static void complete(AssetManager manager, String name, boolean fail) {
        try {
            manager.finishLoading();
            check(!fail, "failed loader must not publish an asset");
            check(manager.get(name, ProbeAsset.class).generation == 1, "first generated asset must be published");
            record(name + "-finish-returned", 1);
        } catch (RuntimeException expected) {
            if (!fail) throw expected;
            checkFailure(expected, name);
            record(name + "-finish-threw", 1);
        }
        // Verify the completed manager stays stable through repeated polling.
        for (int i = 0; i < 5; i++) check(manager.update(), "completed manager must stay finished");
    }

    private static void runPollingCase(String name, boolean fail) {
        AssetManager manager = manager();
        AsyncExecutor executor = new AsyncExecutor(1);
        // The backend only supports AssetLoadingTask; use the real task instead
        // of a synthetic AsyncTask that its constructor would reject.
        AssetLoadingTask task = new AssetLoadingTask(manager,
            new AssetDescriptor<>(name, ProbeAsset.class), new CountingLoader(name, fail, false), executor);
        AsyncResult<Void> future = executor.submit(task);
        check(!future.isDone(), "first poll must remain pending");
        check(!future.isDone(), "second poll must remain pending");
        check(future.isDone(), "third poll must finish inline");
        verifyCompleted(future, name, fail);
        // This callback follows the original pending task timer.
        later(() -> verifyCompleted(future, name, fail));
    }

    private static void verifyCompleted(AsyncResult<Void> future, String name, boolean fail) {
        for (int i = 0; i < 5; i++) check(future.isDone(), "future must remain completed");
        try {
            check(future.get() == null, "asset task result is Void");
            check(!fail, "failed future must retain its exception");
            record(name + "-get-returned", 1);
        } catch (RuntimeException expected) {
            if (!fail) throw expected;
            checkFailure(expected, name);
            record(name + "-get-threw", 1);
        }
    }

    private static void checkFailure(Throwable failure, String name) {
        for (int i = 0; i < 8 && failure != null; i++, failure = failure.getCause()) {
            if ("expected-probe-failure".equals(failure.getMessage())) {
                check("timer-error".equals(name) ? failure instanceof AssertionError :
                    failure instanceof IllegalStateException, "loader failure type must be preserved");
                return;
            }
        }
        throw new AssertionError("loader exception must reach the caller with its cause intact");
    }

    private static final class CountingLoader extends AsynchronousAssetLoader<ProbeAsset, AssetLoaderParameters<ProbeAsset>> {
        private final String name;
        private final boolean fail;
        private final boolean dependencies;
        private int calls;
        private ProbeAsset result;

        CountingLoader(String name, boolean fail, boolean dependencies) {
            super(FileHandle::new);
            this.name = name;
            this.fail = fail;
            this.dependencies = dependencies;
        }

        @Override
        public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AssetLoaderParameters<ProbeAsset> params) {
            return dependencies ? new Array<>() : null;
        }

        @Override
        public void loadAsync(AssetManager assets, String fileName, FileHandle file, AssetLoaderParameters<ProbeAsset> params) {
            record(name + "-loadAsync", ++calls);
            if (fail) {
                if ("timer-error".equals(name)) throw new AssertionError("expected-probe-failure");
                throw new IllegalStateException("expected-probe-failure");
            }
            result = new ProbeAsset(calls);
        }

        @Override
        public ProbeAsset loadSync(AssetManager assets, String fileName, FileHandle file, AssetLoaderParameters<ProbeAsset> params) {
            record(name + "-loadSync", calls);
            ProbeAsset asset = result;
            result = null;
            return asset;
        }
    }

    public static final class ProbeAsset {
        final int generation;
        ProbeAsset(int generation) { this.generation = generation; }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @JSBody(params = {"callback"}, script = "setTimeout(callback, 0);")
    private static native void later(TimerHandler callback);

    @JSBody(params = {"event", "value"}, script = "globalThis.__asyncProbe.events.push({event: event, value: value});")
    private static native void record(String event, int value);
}
