package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.badlogic.gdx.utils.BrokenHashMap;

/**
 * Uses a libGDX-package collection that hashes with an unknown long constant. RegressionProbeBuilder
 * expects this build to fail on the FibonacciHashTransformer diagnostics; HashGuardBuildTest checks.
 */
public final class BrokenHashProbe {
    public static void main(String[] args) {
        BrokenHashMap<String, Integer> map = new BrokenHashMap<>();
        map.put("a_position", 0);
        if(map.get("a_position") != 0) {
            throw new IllegalStateException();
        }
    }
}
