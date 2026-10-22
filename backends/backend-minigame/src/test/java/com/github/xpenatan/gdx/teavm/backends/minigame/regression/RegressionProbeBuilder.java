package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.github.xpenatan.gdx.teavm.backends.minigame.config.backend.MiniGameBackend;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
        // A portrait phone game in a landscape PC window, for BuildOutputTest.
        build(RuntimeRobustnessProbe.class, new File(outputRoot, "runtime-robustness"), "landscape");
        build(StorageAndAssetsProbe.class, new File(outputRoot, "storage-and-assets"));
        build(FreeTypeGlyphProbe.class, new File(outputRoot, "freetype-glyph"));
        build(ShareProbe.class, new File(outputRoot, "share"));
        build(HashPlaceProbe.class, new File(outputRoot, "hash-place"));
        // Outside the probe root: the Node tests sweep every probe there for a built game.
        buildExpectingHashGuardFailure(BrokenHashProbe.class, new File(outputRoot.getParentFile(), "regression-probe-failures/broken-hash"));
    }

    /**
     * Builds a probe whose libGDX-package collection hashes with an unknown long constant. The build
     * must fail on FibonacciHashTransformer's diagnostics; the captured log goes to build-failure.log
     * for HashGuardBuildTest, and an unexpected success is an error.
     */
    private static void buildExpectingHashGuardFailure(Class<?> probe, File output) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                captured.write(b);
                originalErr.write(b);
            }
        }, true));
        String failure;
        try {
            build(probe, output);
            failure = null;
        } catch(RuntimeException expected) {
            failure = expected.getMessage();
        } finally {
            System.setErr(originalErr);
        }
        if(failure == null) {
            throw new IllegalStateException(probe.getSimpleName() + " built although its place() hashes with long");
        }
        output.mkdirs();
        try {
            Files.writeString(new File(output, "build-failure.log").toPath(),
                "exception: " + failure + "\n" + captured.toString(StandardCharsets.UTF_8));
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void build(Class<?> probe, File output) {
        build(probe, output, null);
    }

    private static void build(Class<?> probe, File output, String desktopOrientation) {
        // A desktop orientation marks the probe that stands for a PC-ready game.
        new TeaCompiler(new MiniGameBackend().setAppId(probe.getSimpleName())
                .setDesktopOrientation(desktopOrientation)
                .setDisplayMode(desktopOrientation != null ? "desktop" : null))
            .setMainClass(probe.getName())
            .setOptimizationLevel(TeaVMOptimizationLevel.ADVANCED)
            .setObfuscated(true)
            .build(output);
    }
}
