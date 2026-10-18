package com.github.xpenatan.gdx.teavm.backends.minigame;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/** Optional packaged files. Manifest existence is separate from successful package loading. */
public final class MiniGameAssetPackages {
    private MiniGameAssetPackages() { }

    @JSFunctor
    public interface Completion extends JSObject { void complete(boolean ready); }

    @JSBody(params = {"path"}, script = "var packages = globalThis.__miniGameAssetPackages; return !!packages && packages.isMissing(path);")
    public static native boolean isMissing(String path);

    @JSBody(params = {"path"}, script = "var packages = globalThis.__miniGameAssetPackages; return !!packages && packages.isLoading(path);")
    public static native boolean isLoading(String path);

    @JSBody(params = {"path"}, script = "var packages = globalThis.__miniGameAssetPackages; return packages ? packages.root(path) : 'assets/';")
    public static native String root(String path);

    @JSBody(script = "var packages = globalThis.__miniGameAssetPackages; return packages ? packages.downloadingCount() : 0;")
    public static native int downloadingCount();

    /** Newline-separated logical paths. Completion runs between frames; UI callers must post it. */
    @JSBody(params = {"paths", "done"}, script =
        "var packages = globalThis.__miniGameAssetPackages;" +
        "if (!packages) { done(true); return; }" +
        "packages.load(paths.split('\\n')).then(function() { done(true); }, function(error) {" +
        "console.error('[AssetPackages]', error); done(false); });")
    public static native void load(String paths, Completion done);
}
