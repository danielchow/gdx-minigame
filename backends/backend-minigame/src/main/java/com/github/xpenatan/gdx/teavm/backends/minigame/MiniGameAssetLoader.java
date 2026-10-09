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
 * them from assets/preload.txt at startup and reads their contents on demand through
 * wx.getFileSystemManager(). There are no runtime downloads, so a file that doesn't exist
 * has failed to load.
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
        return false;
    }

    @Override
    public boolean isAssetLoaded(FileType fileType, String path) {
        return Gdx.files.getFileHandle(path, fileType).exists();
    }

    @Override
    public void loadAsset(String path, AssetType assetType, FileType fileType) {
        // No-op: packaged assets are already readable through Gdx.files
    }

    @Override
    public void loadAsset(String path, AssetType assetType, FileType fileType, AssetLoaderListener<WebBlob> listener) {
        loadAsset(path, assetType, fileType, listener, false);
    }

    @Override
    public void loadAsset(String path, AssetType assetType, FileType fileType, AssetLoaderListener<WebBlob> listener, boolean overwrite) {
        // Nothing to download or overwrite: report the packaged file, or failure if there is none
        if (listener == null) return;
        FileHandle file = Gdx.files.getFileHandle(path, fileType);
        if (!file.exists() || file.isDirectory()) {
            listener.onFailure(path);
            return;
        }
        Int8Array data = Int8Array.copyFromJavaArray(file.readBytes());
        listener.onSuccess(path, new WebBlob(data.getBuffer(), data));
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
        return 0;
    }

    @Override
    public boolean isDownloading() {
        return false;
    }
}
