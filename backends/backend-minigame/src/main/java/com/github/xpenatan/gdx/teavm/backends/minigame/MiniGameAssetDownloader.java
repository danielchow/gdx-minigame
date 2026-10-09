package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetDownloader;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetType;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.WebBlob;

/**
 * AssetDownloader for WeChat Mini Game, which has no runtime downloads: every asset ships
 * in the package and is read from it on demand. Each request reports failure to its
 * listener, so a caller such as Pixmap.downloadFromUrl() gets an answer instead of waiting.
 */
public class MiniGameAssetDownloader implements AssetDownloader {

    @Override
    public void load(boolean async, String url, AssetType type, AssetLoaderListener<WebBlob> listener) {
        if (listener != null) listener.onFailure(url);
    }

    @Override
    public void loadScript(boolean async, String url, AssetLoaderListener<String> listener) {
        if (listener != null) listener.onFailure(url);
    }
}
