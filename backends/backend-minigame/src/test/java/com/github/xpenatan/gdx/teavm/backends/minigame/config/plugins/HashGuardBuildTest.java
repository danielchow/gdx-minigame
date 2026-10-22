package com.github.xpenatan.gdx.teavm.backends.minigame.config.plugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Test;

/**
 * A FibonacciHashTransformer diagnostics error must fail the real backend build, not just log.
 * RegressionProbeBuilder builds BrokenHashProbe expecting that failure and keeps the log.
 */
public class HashGuardBuildTest {
    @Test
    public void aLongHashingPlaceFailsTheBuild() throws IOException {
        String root = System.getProperty("regressionProbes");
        if(root == null) throw new IllegalStateException("Run through Gradle: regressionProbes is not set");
        File output = new File(new File(root).getParentFile(), "regression-probe-failures/broken-hash");
        String log = Files.readString(new File(output, "build-failure.log").toPath(), StandardCharsets.UTF_8);
        assertTrue(log, log.startsWith("exception: Build Failed\n"));
        assertTrue(log, log.contains("FibonacciHashTransformer: com.badlogic.gdx.utils.BrokenHashMap.place(Ljava/lang/Object;)I"
            + " still computes with long after the rewrite"));
        assertTrue(log, log.contains("-4417276706812531889L")); // 0xC2B2AE3D27D4EB4FL in the listing
        assertFalse("no game was written", new File(output, "minigame/subpackages/game/app.js").exists());
    }
}
