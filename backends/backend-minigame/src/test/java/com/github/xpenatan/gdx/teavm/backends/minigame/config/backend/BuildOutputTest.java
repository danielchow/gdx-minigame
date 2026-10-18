package com.github.xpenatan.gdx.teavm.backends.minigame.config.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.xpenatan.gdx.teavm.backends.minigame.filesystem.types.InternalStorage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Checks the release directory that MiniGameBackend produces. The probe packages under
 * build/regression-probes are real backend builds; build.gradle.kts seeds the
 * pixmap-buffer one with a previous build's leftovers before rebuilding it.
 */
public class BuildOutputTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private static File probe(String name) {
        String root = System.getProperty("regressionProbes");
        if (root == null) throw new IllegalStateException("Run through Gradle: regressionProbes is not set");
        return new File(root, name + "/minigame");
    }

    private static String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    @Test
    public void optionalPackagesRenderAlongsideMandatoryRootsAndShipTheirRuntime() throws IOException {
        String json = TemplateUtil.resolve("game.json", Map.of("orientation", "portrait",
            "desktopOrientation", "portrait", "displayMode", "",
            "assetPackages", AssetPackages.declarations(Map.of("regal", List.of("theme/regal/")))));
        JsonValue packages = new JsonReader().parse(json).get("subpackages");
        assertEquals(4, packages.size);
        assertEquals("assets/", packages.get(2).getString("root"));
        assertEquals("regal", packages.get(3).getString("name"));
        assertEquals("optional/regal/", packages.get(3).getString("root"));
        assertEquals(TemplateUtil.resolve("asset-packages.js", Map.of()),
            read(new File(probe("storage-and-assets"), "asset-packages.js")));
    }

    @Test
    public void rebuildDeletesPreviousOutputButKeepsPrivateConfig() throws IOException {
        File release = probe("pixmap-buffer");
        for (String stale : new String[] {"app.js.map", "app.js.teavmdbg", "src", "assets/removed-asset.png"}) {
            assertFalse(stale + " survived the rebuild", new File(release, stale).exists());
        }
        assertFalse(read(new File(release, "game.json")).contains("stale"));
        JsonValue renameMap = new JsonReader().parse(read(new File(release, "assets/rename-map.json")));
        assertFalse("stale rename-map entry survived", renameMap.has("/old.bin"));
        assertEquals("{\"planted\": true}", read(new File(release, "project.private.config.json")));
        assertTrue(new File(release, "subpackages/game/app.js").exists());
        assertTrue(new File(release, "subpackages/engine/gdx.wasm").exists());
    }

    @Test
    public void desktopOrientationDefaultsToTheMobileOne() throws IOException {
        JsonValue fallback = new JsonReader().parse(read(new File(probe("share"), "game.json")));
        assertEquals("portrait", fallback.getString("deviceOrientation"));
        assertEquals("portrait", fallback.getString("desktopDeviceOrientation"));
        JsonValue landscape = new JsonReader().parse(read(new File(probe("runtime-robustness"), "game.json")));
        assertEquals("portrait", landscape.getString("deviceOrientation"));
        assertEquals("landscape", landscape.getString("desktopDeviceOrientation"));
    }

    @Test
    public void displayModeIsWrittenOnlyWhenSet() throws IOException {
        assertFalse(new JsonReader().parse(read(new File(probe("share"), "game.json"))).has("displayMode"));
        JsonValue desktop = new JsonReader().parse(read(new File(probe("runtime-robustness"), "game.json")));
        assertEquals("desktop", desktop.getString("displayMode"));
    }

    @Test
    public void projectConfigIgnoresDebugOutputs() throws IOException {
        JsonValue config = new JsonReader().parse(read(new File(probe("async-completion"), "project.config.json")));
        Set<String> ignored = new HashSet<>();
        for (JsonValue rule : config.get("packOptions").get("ignore")) {
            ignored.add(rule.getString("type") + ":" + rule.getString("value"));
        }
        assertTrue(ignored.toString(), ignored.contains("folder:src"));
        assertTrue(ignored.toString(), ignored.contains("suffix:.teavmdbg"));
        assertTrue(ignored.toString(), ignored.contains("suffix:.js.map"));
    }

    @Test
    public void renameMapIsAlwaysWritten() throws IOException {
        File assets = temp.newFolder("assets");
        Files.write(new File(assets, "card.png").toPath(), new byte[] {1});
        File renameMap = new File(assets, "rename-map.json");
        Files.write(renameMap.toPath(), "{\"/old.bin\": \"/old.bin.txt\"}".getBytes(StandardCharsets.UTF_8));
        assertEquals(0, MiniGameBackend.renameBlockedExtensions(new FileHandle(assets)));
        assertEquals("{}\n", read(renameMap));

        Files.write(new File(assets, "shader.glsl").toPath(), new byte[] {1});
        assertEquals(1, MiniGameBackend.renameBlockedExtensions(new FileHandle(assets)));
        assertTrue(new File(assets, "shader.glsl.txt").exists());
        assertEquals("{\n  \"/shader.glsl\": \"/shader.glsl.txt\"\n}\n", read(renameMap));
    }

    @Test
    public void renameMapEscapesJsonAndRoundTrips() {
        Map<String, String> renames = new TreeMap<>();
        renames.put("/a \"quoted\" name.bin", "/a \"quoted\" name.bin.txt");
        renames.put("/back\\slash.dat2", "/back\\slash.dat2.txt");
        renames.put("/tab\there.x", "/tab\there.x.txt");
        renames.put("/字体/中文.otf", "/字体/中文.otf.txt");
        String json = MiniGameBackend.toJsonObject(renames);

        Map<String, String> standard = new TreeMap<>();
        for (JsonValue entry : new JsonReader().parse(json)) standard.put(entry.name, entry.asString());
        assertEquals(renames, standard);

        Map<String, String> runtime = new LinkedHashMap<>();
        InternalStorage.parseRenameMap(json, runtime);
        Map<String, String> expected = new TreeMap<>();
        renames.forEach((from, to) -> expected.put(from.substring(1), to.substring(1)));
        assertEquals(expected, new TreeMap<>(runtime));

        assertEquals("{}\n", MiniGameBackend.toJsonObject(new TreeMap<>()));
        Map<String, String> empty = new LinkedHashMap<>();
        InternalStorage.parseRenameMap("{}\n", empty);
        assertTrue(empty.isEmpty());
    }

    @Test
    public void missingDataUriFailsTheBuild() throws IOException {
        File wasmJs = temp.newFile("gdx.wasm.js");
        Files.write(wasmJs.toPath(), "var O=\"data:application/json,{}\";".getBytes(StandardCharsets.UTF_8));
        assertFailsNaming(wasmJs, () -> MiniGameBackend.extractWasm(new FileHandle(wasmJs)));
        assertFailsNaming(wasmJs, () -> MiniGameBackend.replaceWasmDataUriWithDummy(new FileHandle(wasmJs)));
    }

    @Test
    public void missingEngineScriptFailsTheBuild() {
        File wasmJs = new File(temp.getRoot(), "subpackages/engine/gdx.wasm.js");
        assertFailsNaming(wasmJs, () -> MiniGameBackend.extractWasm(new FileHandle(wasmJs)));
    }

    @Test
    public void dataUriIsExtractedAndReplaced() throws IOException {
        File wasmJs = temp.newFile("gdx.wasm.js");
        Files.write(wasmJs.toPath(), "a\r\nO=\"data:application/octet-stream;base64,AGFzbQEAAAA=\";b".getBytes(StandardCharsets.UTF_8));
        assertEquals(8, MiniGameBackend.extractWasm(new FileHandle(wasmJs)).length);
        MiniGameBackend.replaceWasmDataUriWithDummy(new FileHandle(wasmJs));
        assertEquals("a\r\nO=\"data:application/octet-stream;base64,AA==\";b", read(wasmJs));
    }

    @Test
    public void cleaningRefusesADirectoryThatIsNotAPreviousBuild() throws IOException {
        File dir = temp.newFolder("not-a-build");
        File userFile = new File(dir, "notes.txt");
        Files.write(userFile.toPath(), new byte[] {1});
        try {
            MiniGameBackend.cleanReleaseDirectory(dir);
            fail("cleaning a foreign directory must fail");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains(dir.getAbsolutePath()));
        }
        assertTrue(userFile.exists());
    }

    private static void assertFailsNaming(File file, Runnable step) {
        try {
            step.run();
            fail("expected the build step to fail");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains(file.getPath().replace('\\', '/')));
        }
    }
}
