package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameAssetDownloader;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameAssetLoader;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameFiles;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGamePreferences;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetInstance;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetType;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.WebBlob;
import org.teavm.jso.JSBody;

/**
 * Runs preferences, file handles and asset loading against the mocked wx storage and file
 * system that storage-and-assets.test.mjs provides. Each case records its results, and a
 * failing case records its exception so the others still run.
 */
public final class StorageAndAssetsProbe {
    public static void main(String[] args) {
        Gdx.files = new MiniGameFiles(new MiniGameApplicationConfiguration(), null);
        AssetInstance.setInstance(new MiniGameAssetLoader());
        AssetInstance.setInstance(new MiniGameAssetDownloader());
        run("loader", StorageAndAssetsProbe::loaderListeners);
        run("downloader", StorageAndAssetsProbe::downloaderListeners);
        run("manager", StorageAndAssetsProbe::assetManager);
        run("preferences", StorageAndAssetsProbe::preferences);
        run("encoded-preferences", StorageAndAssetsProbe::encodedPreferences);
        run("file-handle", StorageAndAssetsProbe::fileHandles);
        result("main", "complete");
    }

    private static void run(String name, Runnable section) {
        try {
            section.run();
        } catch (Throwable t) {
            result("error:" + name, t.toString());
        }
    }

    private static void loaderListeners() {
        MiniGameAssetLoader loader = new MiniGameAssetLoader();
        loader.loadAsset("present.txt", AssetType.Binary, FileType.Internal, listener("loader-present"));
        loader.loadAsset("missing.png", AssetType.Binary, FileType.Internal, listener("loader-missing"));
        loader.loadAsset("present.txt", AssetType.Binary, FileType.Internal, listener("loader-overwrite"), true);
    }

    private static void downloaderListeners() {
        AssetInstance.getDownloaderInstance().load(true, "https://example.com/a.png", AssetType.Binary, listener("download"));
        Pixmap.downloadFromUrl("https://example.com/b.png", new Pixmap.DownloadPixmapResponseListener() {
            @Override
            public void downloadComplete(Pixmap pixmap) {
                result("pixmap-download", "complete");
            }

            @Override
            public void downloadFailed(Throwable t) {
                result("pixmap-download", "failed");
            }
        });
    }

    private static AssetLoaderListener<WebBlob> listener(String name) {
        return new AssetLoaderListener<WebBlob>() {
            @Override
            public void onFailure(String url) {
                result(name, "failure:" + url);
            }

            @Override
            public void onSuccess(String url, WebBlob result) {
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < result.length(); i++) text.append((char) result.get(i));
                result(name, "success:" + url + ":" + text);
            }
        };
    }

    private static void assetManager() {
        result("manager-present", loadThroughManager("present.txt"));
        result("manager-missing", loadThroughManager("missing.bin"));
    }

    /** Polls a bounded number of times so a manager that never finishes cannot hang the test. */
    private static String loadThroughManager(String fileName) {
        AssetManager manager = new AssetManager();
        manager.setLogger(new Logger("StorageAndAssetsProbe", Logger.NONE));
        manager.setLoader(TextAsset.class, new TextLoader());
        manager.load(fileName, TextAsset.class);
        try {
            for (int i = 0; i < 20; i++) {
                if (manager.update()) return "loaded:" + manager.get(fileName, TextAsset.class).text;
            }
            return "still-loading";
        } catch (RuntimeException e) {
            StringBuilder messages = new StringBuilder("threw");
            for (Throwable t = e; t != null; t = t.getCause()) messages.append('|').append(t.getMessage());
            return messages.toString();
        }
    }

    private static void preferences() {
        mark("load");
        MiniGamePreferences prefs = new MiniGamePreferences("p", false);
        result("dup-string", prefs.getString("dup"));
        result("dup-int", String.valueOf(prefs.getInteger("dup", -1)));
        mark("flush-after-load");
        prefs.flush();
        mark("flush-unchanged");
        prefs.flush();
        mark("flush-two-changes");
        prefs.putInteger("score", 5);
        prefs.putString("name", "a");
        prefs.flush();
        mark("type-change");
        prefs.putString("score", "five");
        prefs.flush();
        mark("remove");
        prefs.remove("name");
        prefs.flush();
        mark("done");

        result("keep-as-string", prefs.getString("keep"));
        result("keep-as-long", String.valueOf(prefs.getLong("keep", -1)));
        result("keep-as-float", String.valueOf(prefs.getFloat("keep", -1)));
        result("keep-as-boolean", String.valueOf(prefs.getBoolean("keep", true)));
        result("ratio-as-int", String.valueOf(prefs.getInteger("ratio", 42)));
        result("flag-as-int", String.valueOf(prefs.getInteger("flag", 3)));
        result("flag-as-string", prefs.getString("flag"));
        result("text-as-boolean", String.valueOf(prefs.getBoolean("text", true)));
        result("score-as-int", String.valueOf(prefs.getInteger("score", 9)));

        MiniGamePreferences reloaded = new MiniGamePreferences("p", false);
        result("reload-score", reloaded.getString("score"));
        result("reload-has-name", String.valueOf(reloaded.contains("name")));
    }

    private static void encodedPreferences() {
        MiniGamePreferences prefs = new MiniGamePreferences("e", true);
        prefs.putInteger("x", 1);
        prefs.flush();
        prefs.putString("x", "one");
        prefs.flush();
        result("encoded-reload", new MiniGamePreferences("e", true).getString("x"));
    }

    private static void fileHandles() {
        FileHandle internal = Gdx.files.internal("data/a.png");
        FileHandle sameInternal = Gdx.files.internal("data/a.png");
        FileHandle local = Gdx.files.local("data/a.png");
        result("fh-type-field", String.valueOf(internal.type()));
        result("fh-equal-same", String.valueOf(internal.equals(sameInternal)));
        result("fh-equal-cross-type", String.valueOf(internal.equals(local)));
        result("fh-hash-same", String.valueOf(internal.hashCode() == sameInternal.hashCode()));
        ObjectMap<FileHandle, String> map = new ObjectMap<>();
        map.put(internal, "internal");
        map.put(local, "local");
        result("fh-map", map.get(sameInternal) + "," + map.size);
    }

    public static final class TextAsset {
        final String text;
        TextAsset(String text) { this.text = text; }
    }

    private static final class TextLoader extends SynchronousAssetLoader<TextAsset, AssetLoaderParameters<TextAsset>> {
        TextLoader() {
            super(new InternalFileHandleResolver());
        }

        @Override
        public TextAsset load(AssetManager assetManager, String fileName, FileHandle file, AssetLoaderParameters<TextAsset> parameter) {
            return new TextAsset(file.readString());
        }

        @Override
        public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AssetLoaderParameters<TextAsset> parameter) {
            return null;
        }
    }

    @JSBody(params = "name", script = "globalThis.__storageProbe.log.push(['mark', name]);")
    private static native void mark(String name);

    @JSBody(params = {"name", "value"}, script = "globalThis.__storageProbe.results[name] = value;")
    private static native void result(String name, String value);
}
