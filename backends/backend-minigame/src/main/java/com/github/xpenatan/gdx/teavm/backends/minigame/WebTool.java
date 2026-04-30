package com.github.xpenatan.gdx.teavm.backends.minigame;

/**
 * Simple utility - whether we're in production mode.
 */
public class WebTool {
    private static boolean isProd = true;
    public static boolean isProdMode() {
        return isProd;
    }

    public static void setIsProd(boolean flag) {
        isProd = flag;
    }
}
