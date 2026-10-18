package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoader;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetType;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.WebBlob;
import org.teavm.jso.typedarrays.Int8Array;

/**
 * AssetLoader for WeChat Mini Game. Every asset ships in the package: InternalStorage lists
 * them from assets/preload.txt at startup. Optional packages are loaded before reading;
 * InternalStorage routes logical paths to their current package roots.
 */
public class MiniGameAssetLoader implements AssetLoader {

    @Override
    public void preload(String assetFileUrl, AssetLoaderListener<Void> preloadListener) {
        // Packaged assets need no preloading, so it is complete at once
        if (preloadListener != null) preloadListener.onSuccess(assetFileUrl, null);
    }

    @Override
    public String getAssetUrl() {
        return "assets/";
    }

    @Override
    public String getScriptUrl() {
        return "subpackages/engine/";
    }

    @Override
    public boolean isAssetInQueueOrDownloading(String path) {
        return MiniGameAssetPackages.isLoading(path);
    }

    @Override
    public boolean isAssetLoaded(FileType fileType, String path) {
        return !isDeferredMissing(fileType, path) && Gdx.files.getFileHandle(path, fileType).exists();
    }

    @Override
    public void loadAsset(String path, AssetType assetType, FileType fileType) {
        if (isDeferredMissing(fileType, path)) MiniGameAssetPackages.load(path, ready -> { });
    }

    @Override
    public void loadAsset(String path, AssetType assetType, FileType fileType, AssetLoaderListener<WebBlob> listener) {
        loadAsset(path, assetType, fileType, listener, false);
    }

    @Override
    public void loadAsset(String path, AssetType assetType, FileType fileType, AssetLoaderListener<WebBlob> listener, boolean overwrite) {
        if (isDeferredMissing(fileType, path)) {
            MiniGameAssetPackages.load(path, ready -> {
                if (ready) loadAsset(path, assetType, fileType, listener, overwrite);
                else if (listener != null) listener.onFailure(path);
            });
            return;
        }
        // Read only after the containing package has loaded.
        if (listener == null) return;
        FileHandle file = Gdx.files.getFileHandle(path, fileType);
        if (!file.exists() || file.isDirectory()) {
            listener.onFailure(path);
            return;
        }
        Int8Array data = Int8Array.copyFromJavaArray(file.readBytes());
        listener.onSuccess(path, new WebBlob(data.getBuffer(), data));
    }

    private static boolean isDeferredMissing(FileType type, String path) {
        return (type == FileType.Internal || type == FileType.Classpath) && MiniGameAssetPackages.isMissing(path);
    }

    @Override
    public void loadScript(String path) {
        // No-op: scripts loaded via require() in game.js
    }

    @Override
    public void loadScript(String path, AssetLoaderListener<String> listener) {
        // game.js has already loaded every script through the engine subpackage
        if (listener != null) listener.onSuccess(path, "");
    }

    @Override
    public int getQueue() {
        return 0;
    }

    @Override
    public int getDownloadingCount() {
        return MiniGameAssetPackages.downloadingCount();
    }

    @Override
    public boolean isDownloading() {
        return getDownloadingCount() > 0;
    }
}
