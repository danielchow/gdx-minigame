package com.github.xpenatan.gdx.teavm.backends.minigame.config.backend;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class AssetPackagesTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private FileHandle fixture() throws Exception {
        FileHandle release = new FileHandle(temp.newFolder());
        FileHandle assets = release.child("assets"); assets.mkdirs();
        assets.child("preload.txt").writeString("i:b:/theme/regal/cards.atlas:1:0\ni:b:/theme/regal/cards.png:1:0\ni:b:/theme/regal/data.custom:1:0\ni:b:/default.png:1:0\n", false);
        for (String path : List.of("theme/regal/cards.atlas", "theme/regal/cards.png", "theme/regal/data.custom", "default.png")) {
            FileHandle file = assets.child(path); file.parent().mkdirs(); file.writeString(path, false);
        }
        MiniGameBackend.renameBlockedExtensions(assets);
        return release;
    }
    @Test public void movesWholeGroupAndKeepsLogicalManifestAndRenames() throws Exception {
        FileHandle release = fixture();
        String manifest = release.child("assets/preload.txt").readString();
        AssetPackages.split(release, Map.of("regal", List.of("theme/regal/")));
        assertEquals(manifest, release.child("assets/preload.txt").readString());
        assertTrue(release.child("assets/default.png").exists());
        assertFalse(release.child("assets/theme/regal/cards.png").exists());
        assertEquals("theme/regal/data.custom", release.child("optional/regal/theme/regal/data.custom.txt").readString());
        assertTrue(release.child("optional/regal/game.js").exists());
        assertEquals("regal", new JsonReader().parse(release.child("assets/asset-packages.json")).getString("theme/regal/data.custom"));
        assertEquals("/theme/regal/data.custom.txt", new JsonReader().parse(release.child("assets/rename-map.json")).getString("/theme/regal/data.custom"));
    }
    @Test public void defaultConfigurationMovesNothingAndWritesEmptyIndex() throws Exception {
        FileHandle release = fixture(); AssetPackages.split(release, Map.of());
        assertTrue(release.child("assets/theme/regal/cards.png").exists());
        assertEquals(0, new JsonReader().parse(release.child("assets/asset-packages.json")).size);
        assertEquals("", AssetPackages.declarations(Map.of()));
    }
    @Test public void rejectsOverlapAndUnknownSelectorsBeforeMovingAnything() throws Exception {
        FileHandle release = fixture();
        assertThrows(IllegalArgumentException.class, () -> AssetPackages.split(release,
            Map.of("one", List.of("theme/regal/"), "two", List.of("theme/regal/cards.png"))));
        assertTrue(release.child("assets/theme/regal/cards.png").exists());
        assertThrows(IllegalArgumentException.class, () -> AssetPackages.split(release, Map.of("one", List.of("missing/"))));
        assertTrue(release.child("assets/theme/regal/cards.png").exists());
    }
    @Test public void rejectsReservedNamesAndEscapingSelectors() {
        assertThrows(IllegalArgumentException.class, () -> AssetPackages.validate(Map.of("assets", List.of("theme/"))));
        assertThrows(IllegalArgumentException.class, () -> AssetPackages.validate(Map.of("deck", List.of("../secret"))));
        assertThrows(IllegalArgumentException.class, () -> AssetPackages.validate(Map.of("deck", List.of("/absolute"))));
    }
}
