package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.utils.Clipboard;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;

/**
 * Clipboard implementation using wx.setClipboardData.
 * No permission checks needed (unlike browser navigator.clipboard).
 */
public class MiniGameClipboard implements Clipboard {

    private String content = "";

    @Override
    public boolean hasContents() {
        String contents = getContents();
        return contents != null && !contents.isEmpty();
    }

    @Override
    public String getContents() {
        return content;
    }

    @Override
    public void setContents(String content) {
        this.content = content;
        WX.setClipboardData(content);
    }
}
