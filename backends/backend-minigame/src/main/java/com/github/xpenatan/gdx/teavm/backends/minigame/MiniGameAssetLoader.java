package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoader;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetType;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.WebBlob;

/**
 * Minimal AssetLoader implementation for WeChat Mini Game.
 * All assets are preloaded synchronously at startup by the adapter's preloadAssets()
 * and placed into MemoryFileStorage. No runtime downloading is needed.
 */
public class MiniGameAssetLoader implements AssetLoader {

    @Override
    public void preload(String assetFileUrl, AssetLoaderListener<Void> preloadListener) {
        // No-op: preload happens natively in the adapter
    }

    @Override
    public String getAssetUrl() {
        return "assets/";
    }

    @Override
    public String getScriptUrl() {
        return "scripts/";
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
        // No-op: assets already preloaded into MemoryFileStorage
    }

    @Override
    public void loadAsset(String path, AssetType assetType, FileType fileType, AssetLoaderListener<WebBlob> listener) {
        // No-op: assets already preloaded into MemoryFileStorage
    }

    @Override
    public void loadAsset(String path, AssetType assetType, FileType fileType, AssetLoaderListener<WebBlob> listener, boolean overwrite) {
        // No-op: assets already preloaded into MemoryFileStorage
    }

    @Override
    public void loadScript(String path) {
        // No-op: scripts loaded via require() in game.js
    }

    @Override
    public void loadScript(String path, AssetLoaderListener<String> listener) {
        // No-op: scripts loaded via require() in game.js
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
