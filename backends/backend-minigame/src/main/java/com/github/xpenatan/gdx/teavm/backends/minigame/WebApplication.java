package com.github.xpenatan.gdx.teavm.backends.minigame;

import java.util.ArrayList;
import org.teavm.jso.JSObject;

/**
 * Stub for compilation - will be replaced by MiniGameApplication in Phase 3.
 * Referenced by emu files.
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
