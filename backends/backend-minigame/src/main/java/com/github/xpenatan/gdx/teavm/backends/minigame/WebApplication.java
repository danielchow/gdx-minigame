package com.github.xpenatan.gdx.teavm.backends.minigame;

import org.teavm.jso.JSObject;
import java.util.ArrayList;

/**
 * Compatibility stub for emu files that reference WebApplication.
 * Delegates to MiniGameApplication at runtime.
 */
public class WebApplication {

    public static WebApplication get() {
        return (WebApplication)com.badlogic.gdx.Gdx.app;
    }

    public WebApplicationConfiguration getConfig() {
        return null;
    }

    public static void print(String text) {}

    public static void printErrorStack(Object obj) {
        Throwable error = (Throwable)obj;
        error.printStackTrace();
    }
}
