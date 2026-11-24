package com.github.xpenatan.gdx.teavm.backends.minigame.filesystem.types;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameAssetPackages;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameFileHandle;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import com.github.xpenatan.gdx.teavm.backends.minigame.filesystem.FileDB;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Int8Array;

/**
 * On-demand file storage for Internal/Classpath assets on WeChat Mini Game.
 *
 * Reads the preload.txt manifest at construction to populate metadata only
 * (file paths, directory paths), then reads file contents on-demand via
 * wx.getFileSystemManager().readFileSync().
 *
 * Nothing is copied into memory up front, which keeps startup independent of asset size.
 */
public class InternalStorage extends FileDB {

    private final JSObject fsm;

    /** Canonical file paths from manifest (no leading /). */
    private final Set<String> filePaths = new HashSet<>();

    /** Canonical directory paths inferred from file paths (no leading /). */
    private final Set<String> dirPaths = new HashSet<>();

    /** Rename map: logical path → renamed path on filesystem (both no leading /). */
    private final Map<String, String> renameMap = new HashMap<>();

    /** Native JS rename-map object when pre-parsed by game.js via JSON.parse (avoids Java char-by-char parsing). */
    private final JSObject jsRenameMap;

    public InternalStorage() {
        this.fsm = WX.getFileSystemManager();
        loadManifest();
        this.jsRenameMap = consumePreloadedRenameMap();
        if (this.jsRenameMap == null) {
            loadRenameMap();
        }
    }

    @JSBody(script = "var m = globalThis.__preloadedRenameMap; if (m && typeof m === 'object') { delete globalThis.__preloadedRenameMap; return m; } return null;")
    private static native JSObject consumePreloadedRenameMap();

    @JSBody(params = {"map", "key"}, script = "var v = map[key] || map['/' + key]; return typeof v === 'string' ? v : null;")
    private static native String lookupJsRename(JSObject map, String key);

    // ── Path normalization ──────────────────────────────────────────

    /** Strip leading slash and normalize to canonical form (no leading /). */
    private static String normalize(String path) {
        if (path == null) return "";
        path = path.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    private static boolean isRoot(String path) {
        path = normalize(path);
        return path.isEmpty() || path.equals(".") || path.equals("./");
    }

    /**
     * Convert a logical file path to the WeChat filesystem path.
     * Uses the logical path to choose its package root, then applies blocked-extension renaming.
     */
    private String toWXPath(String relativePath) {
        String normalized = normalize(relativePath);
        String root = MiniGameAssetPackages.root(normalized);
        // Check rename map
        String renamed = jsRenameMap != null ? lookupJsRename(jsRenameMap, normalized) : renameMap.get(normalized);
        if (renamed != null) {
            normalized = normalize(renamed);
        }
        return root + normalized;
    }

    // ── Manifest loading ────────────────────────────────────────────

    private void loadManifest() {
        String manifest = WX.fsReadFileSyncText(fsm, "assets/preload.txt");
        if (manifest == null) {
            System.err.println("[InternalStorage] No assets/preload.txt found, internal files will be empty");
            return;
        }
        int length = manifest.length();
        int lineStart = 0;
        while (lineStart < length) {
            int lineEnd = manifest.indexOf('\n', lineStart);
            if (lineEnd < 0) lineEnd = length;
            int start = lineStart;
            int end = lineEnd;
            while (start < end && manifest.charAt(start) <= ' ') start++;
            while (end > start && manifest.charAt(end - 1) <= ' ') end--;
            lineStart = lineEnd + 1;
            if (start >= end) continue;
            // Format: {fileType}:{assetType}:{path}:{size}:{overwrite}
            int firstColon = manifest.indexOf(':', start);
            if (firstColon < 0 || firstColon >= end) continue;
            int secondColon = manifest.indexOf(':', firstColon + 1);
            if (secondColon < 0 || secondColon >= end) continue;
            if (firstColon - start != 1) continue;
            char fileType = manifest.charAt(start);
            if (fileType != 'i' && fileType != 'c') continue;
            boolean isDir = (secondColon - firstColon == 2) && manifest.charAt(firstColon + 1) == 'd';
            int thirdColon = manifest.indexOf(':', secondColon + 1);
            int pathEnd = (thirdColon >= 0 && thirdColon < end) ? thirdColon : end;
            String normalized = normalize(manifest.substring(secondColon + 1, pathEnd));
            if (normalized.isEmpty()) continue;
            if (isDir) {
                dirPaths.add(normalized);
            } else {
                filePaths.add(normalized);
                inferDirectories(normalized);
            }
        }
    }

    private void inferDirectories(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        while (lastSlash > 0) {
            String dir = filePath.substring(0, lastSlash);
            if (!dirPaths.add(dir)) return;
            lastSlash = dir.lastIndexOf('/');
        }
    }

    private void loadRenameMap() {
        String json = WX.fsReadFileSyncText(fsm, "assets/rename-map.json");
        if (json == null) {
            System.err.println("[InternalStorage] No assets/rename-map.json found, renamed files will not be found");
            return;
        }
        // Simple JSON parse: {"key1":"val1","key2":"val2"}
        // We can't use a JSON library in TeaVM, so parse manually.
        parseRenameMap(json);
    }

    private void parseRenameMap(String json) {
        parseRenameMap(json, renameMap);
    }

    /**
     * Minimal parser for the flat {"key": "value", ...} object MiniGameBackend writes,
     * including its string escapes. Keys and values are normalized (no leading /).
     */
    public static void parseRenameMap(String json, Map<String, String> out) {
        int[] pos = {0};
        if (!skipTo(json, pos, '{')) return;
        while (true) {
            String key = readJsonString(json, pos);
            if (key == null || !skipTo(json, pos, ':')) return;
            String value = readJsonString(json, pos);
            if (value == null) return;
            out.put(normalize(key), normalize(value));
            if (!skipTo(json, pos, ',')) return;
        }
    }

    /** Skips whitespace; consumes {@code expected} and returns true if it comes next. */
    private static boolean skipTo(String json, int[] pos, char expected) {
        int i = pos[0];
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        if (i >= json.length() || json.charAt(i) != expected) return false;
        pos[0] = i + 1;
        return true;
    }

    private static String readJsonString(String json, int[] pos) {
        if (!skipTo(json, pos, '"')) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = pos[0]; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                pos[0] = i + 1;
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (++i >= json.length()) return null;
            char e = json.charAt(i);
            switch (e) {
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (i + 4 >= json.length()) return null;
                    sb.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                    i += 4;
                    break;
                default: sb.append(e); // \" \\ \/
            }
        }
        return null;
    }

    // ── FileDB implementation ────────────────────────────────────────

    @Override
    public InputStream read(MiniGameFileHandle file) {
        byte[] bytes = readBytes(file);
        if (bytes == null) return null;
        return new ByteArrayInputStream(bytes);
    }

    /** Reads the raw ArrayBuffer directly from WX without copying into a Java byte[]. */
    public ArrayBuffer readArrayBuffer(MiniGameFileHandle file) {
        String normalized = normalize(file.path());
        if (!filePaths.contains(normalized)) return null;
        if (MiniGameAssetPackages.isMissing(normalized))
            throw new GdxRuntimeException("Asset package is not loaded: " + normalized);
        String wxPath = toWXPath(file.path());
        return WX.fsReadFileSync(fsm, wxPath);
    }

    @Override
    public byte[] readBytes(MiniGameFileHandle file) {
        ArrayBuffer ab = readArrayBuffer(file);
        if (ab == null) return null;
        Int8Array int8 = new Int8Array(ab);
        return int8.copyToJavaArray();
    }

    @Override
    protected void writeInternal(MiniGameFileHandle file, byte[] data, boolean append, int expectedLength) {
        throw new GdxRuntimeException("Cannot write to internal storage: " + file.path());
    }

    @Override
    protected String[] paths(MiniGameFileHandle file) {
        String dir = normalize(file.path());
        boolean isRoot = isRoot(file.path());

        List<String> result = new ArrayList<>();
        for (String filePath : filePaths) {
            int lastSlash = filePath.lastIndexOf('/');
            String parent = lastSlash > 0 ? filePath.substring(0, lastSlash) : "";

            if (isRoot) {
                // Root: direct children whose parent is "" (root)
                if (parent.isEmpty()) {
                    result.add(filePath);
                }
            } else {
                // Direct children only: parent must equal dir exactly
                if (parent.equals(dir)) {
                    result.add(filePath);
                }
            }
        }
        // Also add subdirectories as direct children
        for (String dirPath : dirPaths) {
            int lastSlash = dirPath.lastIndexOf('/');
            String parent = lastSlash > 0 ? dirPath.substring(0, lastSlash) : "";

            if (isRoot) {
                if (parent.isEmpty()) {
                    result.add(dirPath);
                }
            } else {
                if (parent.equals(dir)) {
                    result.add(dirPath);
                }
            }
        }
        return result.toArray(new String[0]);
    }

    @Override
    public boolean isDirectory(MiniGameFileHandle file) {
        if (isRoot(file.path())) return true;
        String normalized = normalize(file.path());
        return dirPaths.contains(normalized);
    }

    @Override
    public void mkdirs(MiniGameFileHandle file) {
        throw new GdxRuntimeException("Cannot mkdirs in internal storage: " + file.path());
    }

    @Override
    public boolean exists(MiniGameFileHandle file) {
        if (isRoot(file.path())) return true;
        String normalized = normalize(file.path());
        return (filePaths.contains(normalized) && !MiniGameAssetPackages.isMissing(normalized)) || dirPaths.contains(normalized);
    }

    @Override
    public boolean delete(MiniGameFileHandle file) {
        throw new GdxRuntimeException("Cannot delete from internal storage: " + file.path());
    }

    @Override
    public boolean deleteDirectory(MiniGameFileHandle file) {
        throw new GdxRuntimeException("Cannot delete directory from internal storage: " + file.path());
    }

    @Override
    public long length(MiniGameFileHandle file) {
        String normalized = normalize(file.path());
        if (dirPaths.contains(normalized)) return 0;
        if (!filePaths.contains(normalized) || MiniGameAssetPackages.isMissing(normalized)) return 0;
        String wxPath = toWXPath(file.path());
        return (long) WX.fsFileSize(fsm, wxPath);
    }

    @Override
    public void rename(MiniGameFileHandle source, MiniGameFileHandle target) {
        throw new GdxRuntimeException("Cannot rename in internal storage: " + source.path());
    }
}
