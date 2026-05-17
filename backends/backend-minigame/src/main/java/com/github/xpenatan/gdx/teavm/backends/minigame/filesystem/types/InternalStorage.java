package com.github.xpenatan.gdx.teavm.backends.minigame.filesystem.types;

import com.badlogic.gdx.utils.GdxRuntimeException;
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
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Int8Array;

/**
 * On-demand file storage for Internal/Classpath assets on WeChat Mini Game.
 *
 * Instead of preloading all assets into a HashMap (the old MemoryFileStorage approach),
 * this reads the preload.txt manifest at construction to populate metadata only
 * (file paths, directory paths), then reads file contents on-demand via
 * wx.getFileSystemManager().readFileSync().
 *
 * This eliminates ~519ms of JS-to-JS copy overhead from the startup path.
 */
public class InternalStorage extends FileDB {

    private final JSObject fsm;

    /** Canonical file paths from manifest (no leading /). */
    private final Set<String> filePaths = new HashSet<>();

    /** Canonical directory paths inferred from file paths (no leading /). */
    private final Set<String> dirPaths = new HashSet<>();

    /** Rename map: logical path → renamed path on filesystem (both no leading /). */
    private final Map<String, String> renameMap = new HashMap<>();

    public InternalStorage() {
        this.fsm = WX.getFileSystemManager();
        loadManifest();
        loadRenameMap();
    }

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
     * Applies rename-map lookup first, then prepends "assets/".
     */
    private String toWXPath(String relativePath) {
        String normalized = normalize(relativePath);
        // Check rename map
        String renamed = renameMap.get(normalized);
        if (renamed != null) {
            normalized = renamed;
        }
        return "assets/" + normalized;
    }

    // ── Manifest loading ────────────────────────────────────────────

    private void loadManifest() {
        String manifest = WX.fsReadFileSyncText(fsm, "assets/preload.txt");
        if (manifest == null) {
            System.out.println("[InternalStorage] No preload.txt found, storage will be empty");
            return;
        }
        String[] lines = manifest.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] tokens = line.split(":");
            // Format: {fileType}:{assetType}:{path}:{size}:{overwrite}
            // fileType = 'i' (internal) or 'c' (classpath)
            // assetType = 'b' (binary) or 'd' (directory)
            if (tokens.length < 3) continue;
            String fileType = tokens[0];
            String assetType = tokens[1];
            String path = tokens[2];
            // Accept both 'i:' and 'c:' entries
            if (!"i".equals(fileType) && !"c".equals(fileType)) continue;
            String normalized = normalize(path);
            if (normalized.isEmpty()) continue;
            if ("d".equals(assetType)) {
                dirPaths.add(normalized);
            } else {
                filePaths.add(normalized);
                // Infer parent directories
                inferDirectories(normalized);
            }
        }
        System.out.println("[InternalStorage] Manifest loaded: " + filePaths.size() + " files, " + dirPaths.size() + " dirs");
    }

    private void inferDirectories(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        while (lastSlash > 0) {
            String dir = filePath.substring(0, lastSlash);
            dirPaths.add(dir);
            lastSlash = dir.lastIndexOf('/');
        }
    }

    private void loadRenameMap() {
        String json = WX.fsReadFileSyncText(fsm, "assets/rename-map.json");
        if (json == null) {
            System.out.println("[InternalStorage] No rename-map.json found");
            return;
        }
        // Simple JSON parse: {"key1":"val1","key2":"val2"}
        // We can't use a JSON library in TeaVM, so parse manually.
        parseRenameMap(json);
        System.out.println("[InternalStorage] Rename map loaded: " + renameMap.size() + " entries");
    }

    private void parseRenameMap(String json) {
        // Minimal JSON object parser for {"key":"value", ...}
        // Strip outer braces
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) return;
        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return;

        int i = 0;
        while (i < json.length()) {
            // Find key
            int keyStart = json.indexOf('"', i);
            if (keyStart < 0) break;
            int keyEnd = json.indexOf('"', keyStart + 1);
            if (keyEnd < 0) break;
            String key = json.substring(keyStart + 1, keyEnd);
            // Normalize key (strip leading /)
            key = normalize(key);

            // Find colon
            int colon = json.indexOf(':', keyEnd + 1);
            if (colon < 0) break;

            // Find value
            int valStart = json.indexOf('"', colon + 1);
            if (valStart < 0) break;
            int valEnd = json.indexOf('"', valStart + 1);
            if (valEnd < 0) break;
            String value = json.substring(valStart + 1, valEnd);
            // Normalize value (strip leading /)
            value = normalize(value);

            renameMap.put(key, value);

            // Find comma or end
            int comma = json.indexOf(',', valEnd + 1);
            if (comma < 0) break;
            i = comma + 1;
        }
    }

    // ── FileDB implementation ────────────────────────────────────────

    @Override
    public InputStream read(MiniGameFileHandle file) {
        byte[] bytes = readBytes(file);
        if (bytes == null) return null;
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public byte[] readBytes(MiniGameFileHandle file) {
        String normalized = normalize(file.path());
        if (!filePaths.contains(normalized)) return null;
        String wxPath = toWXPath(file.path());
        ArrayBuffer ab = WX.fsReadFileSync(fsm, wxPath);
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
        return filePaths.contains(normalized) || dirPaths.contains(normalized);
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
        if (!filePaths.contains(normalized)) return 0;
        String wxPath = toWXPath(file.path());
        return (long) WX.fsFileSize(fsm, wxPath);
    }

    @Override
    public void rename(MiniGameFileHandle source, MiniGameFileHandle target) {
        throw new GdxRuntimeException("Cannot rename in internal storage: " + source.path());
    }
}
