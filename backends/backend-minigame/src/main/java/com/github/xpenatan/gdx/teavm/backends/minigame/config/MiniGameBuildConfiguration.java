package com.github.xpenatan.gdx.teavm.backends.minigame.config;

/**
 * Build configuration for WeChat Mini Game.
 * Standalone class (doesn't extend TeaBuildConfiguration from backend-web).
 */
public class MiniGameBuildConfiguration {
    public String appId = "";
    public String orientation = "portrait";
    public String description = "WeChat Mini Game";
    public String targetFileName = "app";
    public String mainClassArgs = "";
}
