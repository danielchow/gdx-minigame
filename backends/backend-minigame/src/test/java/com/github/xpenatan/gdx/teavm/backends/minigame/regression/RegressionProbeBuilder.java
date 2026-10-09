package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.github.xpenatan.gdx.teavm.backends.minigame.config.backend.MiniGameBackend;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import java.io.File;
import org.teavm.vm.TeaVMOptimizationLevel;

/**
 * Compiles each regression probe into a Mini Game package with the real backend, using the
 * release optimization path. src/test/js runs the packages against the bundled native WASM.
 */
public final class RegressionProbeBuilder {
    public static void main(String[] args) {
        File outputRoot = new File(args[0]);
        build(PixmapBufferProbe.class, new File(outputRoot, "pixmap-buffer"));
        build(AsyncCompletionProbe.class, new File(outputRoot, "async-completion"));
        build(GLUploadRangeProbe.class, new File(outputRoot, "gl-upload-range"));
        build(RuntimeRobustnessProbe.class, new File(outputRoot, "runtime-robustness"));
        build(StorageAndAssetsProbe.class, new File(outputRoot, "storage-and-assets"));
    }

    private static void build(Class<?> probe, File output) {
        new TeaCompiler(new MiniGameBackend().setAppId(probe.getSimpleName()))
            .setMainClass(probe.getName())
            .setOptimizationLevel(TeaVMOptimizationLevel.ADVANCED)
            .setObfuscated(true)
            .build(output);
    }
}
