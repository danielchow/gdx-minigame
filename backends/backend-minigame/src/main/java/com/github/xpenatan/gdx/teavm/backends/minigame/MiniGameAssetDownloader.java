package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetDownloader;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetType;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.WebBlob;

/**
 * Minimal AssetDownloader for WeChat Mini Game.
 * All methods are no-ops because assets are preloaded synchronously at startup.
 * This prevents NPE in Pixmap.downloadFromUrl() if it is called.
 */
public class MiniGameAssetDownloader implements AssetDownloader {

    @Override
    public void load(boolean async, String url, AssetType type, AssetLoaderListener<WebBlob> listener) {
        // No-op: no runtime downloading on WeChat Mini Game
    }

    @Override
    public void loadScript(boolean async, String url, AssetLoaderListener<String> listener) {
        // No-op: no runtime script downloading on WeChat Mini Game
    }
}
