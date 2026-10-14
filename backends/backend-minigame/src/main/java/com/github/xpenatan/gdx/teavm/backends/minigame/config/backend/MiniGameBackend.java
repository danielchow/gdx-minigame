package com.github.xpenatan.gdx.teavm.backends.minigame.config.backend;

import com.badlogic.gdx.files.FileHandle;
import com.github.xpenatan.gdx.teavm.backends.minigame.config.MiniGameBuildConfiguration;
import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFilter;
import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetsCopy;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaBackend;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompilerData;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.teavm.tooling.TeaVMTargetType;

/**
 * Build backend for WeChat Mini Game.
 * Generates game.js, game.json, project.config.json, adapter.js, and first-screen.js
 * with a subpackages/ directory layout for parallel loading.
 */
public class MiniGameBackend extends TeaBackend {

    /** WeChat DevTools keeps the developer's local settings here; cleaning must keep it. */
    static final String PRIVATE_CONFIG_FILE = "project.private.config.json";

    private static final Pattern WASM_DATA_URI =
        Pattern.compile("(\"data:application/octet-stream;base64,)([A-Za-z0-9+/=]+)(\")");

    public String appId = "";
    public String orientation = "portrait";
    /** Orientation in Windows and Mac WeChat; null uses {@link #orientation} there too. */
    public String desktopOrientation;
    public String description = "WeChat Mini Game";
    public String targetFileName = "app";
    public String mainClassArgs = "";
    public AssetFilter scriptFilter;

    public MiniGameBackend setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public MiniGameBackend setOrientation(String orientation) {
        this.orientation = orientation;
        return this;
    }

    /** game.json desktopDeviceOrientation: e.g. a landscape window for a portrait phone game. */
    public MiniGameBackend setDesktopOrientation(String desktopOrientation) {
        this.desktopOrientation = desktopOrientation;
        return this;
    }

    public MiniGameBackend setDescription(String description) {
        this.description = description;
        return this;
    }

    public MiniGameBackend setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
        return this;
    }

    public MiniGameBackend setMainClassArgs(String mainClassArgs) {
        this.mainClassArgs = mainClassArgs;
        return this;
    }

    public MiniGameBackend setScriptFilter(AssetFilter scriptFilter) {
        this.scriptFilter = scriptFilter;
        return this;
    }

    @Override
    protected void setup(TeaCompilerData data) {
        targetType = TeaVMTargetType.JAVASCRIPT;
        tool.setTargetFileName(data.outputName + ".js");

        if(data.releasePath != null) {
            releasePath = new FileHandle(data.releasePath.getAbsolutePath().replace("\\", "/"));
        } else {
            releasePath = new FileHandle(new File(data.output, "minigame").getAbsolutePath().replace("\\", "/"));
        }
        // setup() runs before TeaVM compiles and before copyAssets(), so nothing of this
        // build exists yet. Without this, debug output (app.js.map, *.teavmdbg, src/),
        // removed assets and stale rename-map entries from earlier builds would ship.
        cleanReleaseDirectory(releasePath.file());
        tool.setTargetDirectory(releasePath.file());
        setupMinigame(data);
    }

    @Override
    protected void build(TeaCompilerData data) {
        super.build(data);
        // Move TeaVM output (app.js) to game subpackage — can't do this in copyAssets()
        // because TeaVM hasn't produced the file yet at that point
        FileHandle gameFolder = releasePath.child("subpackages/game");
        gameFolder.mkdirs();
        FileHandle appJs = releasePath.child(targetFileName + ".js");
        if (appJs.exists()) {
            appJs.moveTo(gameFolder.child(targetFileName + ".js"));
            System.out.println("[MiniGameBackend] Moved " + targetFileName + ".js to subpackages/game/");

            // Insert Module declaration after "use strict" so TeaVM's strict-mode code can access
            // the freetype Module object set by freetype.js via globalThis.Module
            FileHandle movedAppJs = gameFolder.child(targetFileName + ".js");
            String appContent = movedAppJs.readString();
            String moduleDecl = "var Module=globalThis.Module;\n";
            // Insert after the "use strict"; directive (first line)
            if (appContent.startsWith("\"use strict\";")) {
                appContent = "\"use strict\";\n" + moduleDecl + appContent.substring("\"use strict\";\n".length());
            } else {
                appContent = moduleDecl + appContent;
            }
            movedAppJs.writeString(appContent, false);
            System.out.println("[MiniGameBackend] Inserted Module declaration into " + targetFileName + ".js");
        }
    }

    /**
     * Delete everything in the release directory except {@link #PRIVATE_CONFIG_FILE}.
     * Refuses a non-empty directory that holds no game.json or project.config.json, since
     * that is not a previous Mini Game build and its files may not be ours to delete.
     */
    static void cleanReleaseDirectory(File dir) {
        File[] children = dir.listFiles();
        if (children == null) return;
        boolean hasOtherFiles = Arrays.stream(children)
            .anyMatch(child -> !child.getName().startsWith(".") && !child.getName().equals(PRIVATE_CONFIG_FILE));
        boolean previousBuild = new File(dir, "game.json").exists() || new File(dir, "project.config.json").exists();
        if (hasOtherFiles && !previousBuild) {
            throw new IllegalStateException("Refusing to clean release directory " + dir.getAbsolutePath()
                + ": it is not empty and has no game.json or project.config.json from a previous Mini Game build");
        }
        for (File child : children) {
            if (child.getName().equals(PRIVATE_CONFIG_FILE)) continue;
            deleteRecursively(child.toPath());
        }
    }

    /** Deletes without following symbolic links, so a linked folder's contents survive. */
    private static void deleteRecursively(Path path) {
        try (Stream<Path> walk = Files.walk(path)) {
            for (Path p : (Iterable<Path>) walk.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(p);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't clean " + path + " from the release directory", e);
        }
    }

    private void setupMinigame(TeaCompilerData data) {
        if (appId == null || appId.isEmpty()) {
            System.out.println("[MiniGameBackend] WARNING: appId is empty. Set via setAppId() for production builds.");
        }
        generateGameJson();
        generateProjectConfigJson();
        generateGameJs(data);
        generateAdapter();
        generateFirstScreen();
    }

    /**
     * Generate game.json — WeChat Mini Game configuration.
     */
    private void generateGameJson() {
        String json = TemplateUtil.resolve("game.json",
            Map.of("orientation", orientation,
                "desktopOrientation", desktopOrientation != null ? desktopOrientation : orientation));
        FileHandle gameJson = releasePath.child("game.json");
        gameJson.writeString(json, false);
    }

    /**
     * Generate project.config.json — WeChat DevTools project config.
     */
    private void generateProjectConfigJson() {
        String json = TemplateUtil.resolve("project.config.json",
            Map.of(
                "description", description,
                "appId", appId
            ));
        FileHandle projectConfig = releasePath.child("project.config.json");
        projectConfig.writeString(json, false);
    }

    /**
     * Generate game.js — Main package entry point.
     * Uses parallel subpackage loading with WebGL splash screen.
     */
    private void generateGameJs(TeaCompilerData data) {
        String js = TemplateUtil.resolve("game.js",
            Map.of("mainClassArgs", mainClassArgs));
        FileHandle gameJs = releasePath.child("game.js");
        gameJs.writeString(js, false);
    }

    /**
     * Generate adapter.js — Browser polyfills for WeChat environment.
     * Flat file at root level (was adapter/index.js).
     * Includes real-device polyfills for window, document, performance.
     */
    private void generateAdapter() {
        String js = TemplateUtil.resolve("adapter.js");
        FileHandle adapterJs = releasePath.child("adapter.js");
        adapterJs.writeString(js, false);
    }

    /**
     * Generate first-screen.js — WebGL splash screen for loading.
     * Shows a colored rectangle via scissor+clear to avoid black screen.
     */
    private void generateFirstScreen() {
        String js = TemplateUtil.resolve("first-screen.js");
        FileHandle firstScreenJs = releasePath.child("first-screen.js");
        firstScreenJs.writeString(js, false);
    }

    /**
     * Generate subpackage entry files — bridge entries for each subpackage.
     * WeChat requires <root>/game.js as the subpackage entry point.
     */
    private void generateSubpackageEntryFiles() {
        String engineEntry = TemplateUtil.resolve("subpackage-engine.js");
        String gameEntry = TemplateUtil.resolve("subpackage-game.js",
            Map.of("targetFileName", targetFileName));
        String assetsEntry = TemplateUtil.resolve("subpackage-assets.js");

        releasePath.child("subpackages/engine").mkdirs();
        releasePath.child("subpackages/game").mkdirs();
        releasePath.child("subpackages/engine/game.js").writeString(engineEntry, false);
        releasePath.child("subpackages/game/game.js").writeString(gameEntry, false);
        releasePath.child("assets/game.js").writeString(assetsEntry, false);
    }

    /**
     * Extract embedded WASM binary from gdx.wasm.js and write it as subpackages/engine/gdx.wasm.
     * The JS file contains base64 WASM data in a data URL like:
     *   O="data:application/octet-stream;base64,AGFzbQ..."
     * We parse this out, decode the base64, and write the raw bytes as a .wasm file
     * so WXWebAssembly.instantiate('subpackages/engine/gdx.wasm', imports) can find it.
     */
    private void extractWasmFromJs() {
        FileHandle engineFolder = releasePath.child("subpackages/engine");
        byte[] wasmBytes = extractWasm(engineFolder.child("gdx.wasm.js"));
        // Write the .wasm binary to subpackages/engine/ (WXWebAssembly reads from filesystem)
        engineFolder.child("gdx.wasm").writeBytes(wasmBytes, false);
        System.out.println("[MiniGameBackend] Extracted gdx.wasm (" + wasmBytes.length + " bytes) from gdx.wasm.js");
    }

    /** Decodes the WASM embedded in gdx.wasm.js; without it the package cannot start, so this fails the build. */
    static byte[] extractWasm(FileHandle wasmJs) {
        Matcher matcher = findWasmDataUri(wasmJs, readWasmJs(wasmJs));
        try {
            return Base64.getDecoder().decode(matcher.group(2));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid base64 WASM data URI in " + wasmJs.path(), e);
        }
    }

    private static String readWasmJs(FileHandle wasmJs) {
        if (!wasmJs.exists()) {
            throw new IllegalStateException("Engine script not found: " + wasmJs.path()
                + " (the backend JAR must provide gdx.wasm.js)");
        }
        return wasmJs.readString("UTF-8");
    }

    private static Matcher findWasmDataUri(FileHandle wasmJs, String content) {
        Matcher matcher = WASM_DATA_URI.matcher(content);
        if (!matcher.find()) {
            throw new IllegalStateException("No base64 WASM data URI found in " + wasmJs.path());
        }
        return matcher;
    }

    @Override
    protected void copyAssets(TeaCompilerData data) {
        super.copyAssets(data);

        // Filter out template resource files that leak into scripts/propertiesResources
        // via classpath scanning (TeaVMResourceProperties.getResources)
        scripts.removeIf(path -> path.contains("templates/minigame/"));

        // Filter out howler.js — WeChat uses wx.createInnerAudioContext() for audio,
        // not Howler.js. Saves ~35KB of I/O during engine subpackage loading.
        scripts.removeIf(path -> path.endsWith("howler.js"));

        // Filter out backup files from freetype-teavm resources
        scripts.removeIf(path -> path.endsWith(".asm.bak"));

        // Clean up template files that leaked into assets/ via propertiesResources
        FileHandle leakedTemplates = releasePath.child("assets/templates");
        if (leakedTemplates.exists()) {
            leakedTemplates.deleteDirectory();
            System.out.println("[MiniGameBackend] Cleaned up leaked template files from assets/");
        }

        // Remove template references from preload.txt (generated by super.copyAssets)
        FileHandle preloadTxt = releasePath.child("assets/preload.txt");
        if (preloadTxt.exists()) {
            String preload = preloadTxt.readString();
            String cleaned = preload.lines()
                .filter(line -> !line.contains("templates/minigame/"))
                .collect(java.util.stream.Collectors.joining("\n", "", "\n"));
            preloadTxt.writeString(cleaned, false);
        }

        // Engine subpackage: scripts → subpackages/engine/
        FileHandle engineFolder = releasePath.child("subpackages/engine");
        engineFolder.mkdirs();
        AssetsCopy.copyResources(classLoader, scripts, scriptFilter, engineFolder);
        // gdx.wasm.js stays in engine folder — NO move to root

        // Generate subpackage entry files required by WeChat
        generateSubpackageEntryFiles();

        // Extract WASM binary from embedded base64 in gdx.wasm.js
        extractWasmFromJs();

        // Replace the large base64 data URI with a 1-byte dummy to save ~129K
        replaceWasmDataUriWithDummy();

        // Note: app.js move happens in build() post-build step (TeaVM output doesn't exist yet here)

        // Rename files with blocked extensions for WeChat readFileSync compatibility
        renameBlockedExtensions();
    }

    /**
     * Replace the large base64 WASM data URI in gdx.wasm.js with a 1-byte dummy (AA==).
     * The standalone gdx.wasm has already been extracted by extractWasmFromJs(),
     * so the embedded copy is dead weight. The adapter's patched WebAssembly.instantiate
     * loads from the standalone file, ignoring the dummy bytes.
     *
     * Saves ~129KB in the build output.
     */
    private void replaceWasmDataUriWithDummy() {
        FileHandle wasmJs = releasePath.child("subpackages/engine/gdx.wasm.js");
        int saved = replaceWasmDataUriWithDummy(wasmJs);
        System.out.println("[MiniGameBackend] Replaced WASM data URI with 1-byte dummy (saved ~" + (saved / 1024) + "KB)");
    }

    /** Returns the number of characters saved. */
    static int replaceWasmDataUriWithDummy(FileHandle wasmJs) {
        String content = readWasmJs(wasmJs);
        Matcher matcher = findWasmDataUri(wasmJs, content);
        String modified = content.substring(0, matcher.start(2)) + "AA==" + content.substring(matcher.end(2));
        wasmJs.writeString(modified, false, "UTF-8");
        return content.length() - modified.length();
    }

    /**
     * Rename files with extensions that WeChat's readFileSync blocks (permission denied).
     * Appends .txt to blocked extensions and generates a rename-map.json for runtime lookup.
     */
    private void renameBlockedExtensions() {
        int count = renameBlockedExtensions(releasePath.child("assets"));
        System.out.println("[MiniGameBackend] Generated rename-map.json with " + count + " entries");
    }

    /** Returns the number of renamed files. */
    static int renameBlockedExtensions(FileHandle assetsDir) {
        Set<String> allowedExts = new HashSet<>(Arrays.asList(
            // WeChat Mini Game official whitelist (69 extensions)
            // Source: https://developers.weixin.qq.com/minigame/dev/guide/base-ability/code-package.html
            "png", "jpg", "jpeg", "gif", "svg", "js", "json", "cer", "obj", "dae",
            "fbx", "mtl", "stl", "3ds", "mp3", "pvr", "wav", "plist", "ttf", "fnt",
            "gz", "ccz", "m4a", "mp4", "bmp", "atlas", "swf", "ani", "part", "proto",
            "bin", "sk", "mipmaps", "txt", "zip", "tt", "map", "ogg", "silk", "dbbin",
            "dbmv", "etc", "lmat", "lm", "ls", "lh", "lani", "lav", "lsani", "ltc",
            "aac", "astc", "br", "csv", "cur", "dat", "dds", "glb", "gltf", "ico",
            "ktx", "lmani", "lml", "pkm", "prefab", "scene", "skel", "wasm", "xml"
        ));
        assetsDir.mkdirs();

        // Always written, even empty, so the runtime never sees a map from an older build
        Map<String, String> renames = new TreeMap<>();
        renameFilesRecursive(assetsDir, assetsDir, allowedExts, renames);
        assetsDir.child("rename-map.json").writeString(toJsonObject(renames), false, "UTF-8");
        return renames.size();
    }

    /** Writes a flat JSON object of strings; InternalStorage.parseRenameMap reads it back. */
    static String toJsonObject(Map<String, String> map) {
        if (map.isEmpty()) return "{}\n";
        StringBuilder json = new StringBuilder("{\n");
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            json.append("  ");
            appendJsonString(json, entry.getKey());
            json.append(": ");
            appendJsonString(json, entry.getValue());
            json.append(++i < map.size() ? ",\n" : "\n");
        }
        return json.append("}\n").toString();
    }

    private static void appendJsonString(StringBuilder json, String value) {
        json.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                json.append('\\').append(c);
            } else if (c < 0x20) {
                json.append(String.format("\\u%04x", (int) c));
            } else {
                json.append(c);
            }
        }
        json.append('"');
    }

    private static void renameFilesRecursive(FileHandle dir, FileHandle root, Set<String> allowedExts, Map<String, String> renames) {
        FileHandle[] children = dir.list();
        if (children == null) return;
        for (FileHandle child : children) {
            if (child.isDirectory()) {
                renameFilesRecursive(child, root, allowedExts, renames);
            } else {
                String name = child.name();
                // Skip metadata files and dotfiles
                if (name.equals("rename-map.json") || name.equals("preload.txt") || name.startsWith(".")) continue;
                String ext = child.extension().toLowerCase();
                if (!allowedExts.contains(ext)) {
                    String relativePath = child.path().substring(root.path().length() + 1).replace('\\', '/');
                    String newName = name + ".txt";
                    FileHandle renamed = child.sibling(newName);
                    // Delete target if exists (incremental builds)
                    if (renamed.exists()) renamed.delete();
                    child.moveTo(renamed);
                    String renamedRelative = renamed.path().substring(root.path().length() + 1).replace('\\', '/');
                    renames.put("/" + relativePath, "/" + renamedRelative);
                }
            }
        }
    }
}
