package com.github.xpenatan.gdx.teavm.backends.minigame;

/**
 * Configuration for MiniGameApplication.
 * Stripped of all browser/DOM-specific fields from WebApplicationConfiguration.
 */
public class MiniGameApplicationConfiguration {

    /** Whether to enable OpenGL ES 30 (WebGL2) if supported. */
    public boolean useGL30 = false;

    /** Whether to use a stencil buffer. */
    public boolean stencil = false;

    /** Whether to enable antialiasing. */
    public boolean antialiasing = false;

    /** Whether to include an alpha channel. */
    public boolean alpha = false;

    /** Whether to use premultiplied alpha. */
    public boolean premultipliedAlpha = false;

    /** Whether to preserve the back buffer. */
    public boolean preserveDrawingBuffer = false;

    /** Whether to use debugging mode for OpenGL calls. */
    public boolean useDebugGL = false;

    /** Whether to use physical device pixels. */
    public boolean usePhysicalPixels = false;

    /** Power preference: "default", "low-power", or "high-performance". */
    public String powerPreference = "high-performance";

    /**
     * Canvas object created by game.js adapter via wx.createCanvas().
     * Set before Java code runs.
     */
    public Object canvas;

    /** Prefix for browser/WX storage. */
    public String storagePrefix = "app";

    /** Prefix for local storage database. */
    public String localStoragePrefix = "db/assets";

    /** Whether to encode preference keys/values. */
    public boolean shouldEncodePreference = false;

    public boolean isFixedSizeApplication() {
        return false; // Always fullscreen on WeChat
    }

    public boolean isAutoSizeApplication() {
        return true; // Always fullscreen on WeChat
    }
}
