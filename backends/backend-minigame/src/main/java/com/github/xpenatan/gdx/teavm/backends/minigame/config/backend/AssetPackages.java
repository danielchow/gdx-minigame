package com.github.xpenatan.gdx.teavm.backends.minigame.config.backend;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Build-time layout only. Logical manifest paths remain stable across package moves. */
final class AssetPackages {
    private AssetPackages() { }

    static void validate(Map<String, List<String>> groups) {
        groups.forEach((name, paths) -> {
            if (!name.matches("[a-z][a-z0-9_-]*") || Set.of("engine", "game", "assets").contains(name))
                throw new IllegalArgumentException("Invalid optional asset package name: " + name);
            if (paths.isEmpty()) throw new IllegalArgumentException("Empty asset package: " + name);
            for (String path : paths) {
                if (path.isEmpty() || path.startsWith("/") || path.contains("\\") ||
                    path.contains(":") || List.of(path.split("/")).contains("..") ||
                    List.of(path.split("/")).contains("."))
                    throw new IllegalArgumentException("Expected a relative asset file or directory: " + path);
            }
        });
    }

    static String declarations(Map<String, List<String>> groups) {
        JsonValue packages = new JsonValue(JsonValue.ValueType.array);
        for (String name : new java.util.TreeSet<>(groups.keySet())) {
            JsonValue entry = new JsonValue(JsonValue.ValueType.object);
            entry.addChild("name", new JsonValue(name));
            entry.addChild("root", new JsonValue("optional/" + name + "/"));
            packages.addChild(entry);
        }
        String json = packages.toJson(JsonWriter.OutputType.json);
        return groups.isEmpty() ? "" : ",\n        " + json.substring(1, json.length() - 1);
    }

    /** Called after blocked extensions have been renamed; the complete manifest stays common. */
    static void split(FileHandle release, Map<String, List<String>> groups) {
        validate(groups);
        FileHandle assets = release.child("assets");
        Map<String, String> renames = new TreeMap<>();
        for (JsonValue entry : new JsonReader().parse(assets.child("rename-map.json")))
            renames.put(entry.name.replaceFirst("^/", ""), entry.asString().replaceFirst("^/", ""));
        Set<String> files = new java.util.TreeSet<>();
        for (String line : assets.child("preload.txt").readString("UTF-8").split("\n")) {
            String[] parts = line.trim().split(":");
            if (parts.length >= 3 && (parts[0].equals("i") || parts[0].equals("c")) && !parts[1].equals("d"))
                files.add(parts[2].replaceFirst("^/", ""));
        }
        Map<String, String> owners = new TreeMap<>();
        groups.forEach((name, selectors) -> {
            for (String selector : selectors) {
                boolean found = false;
                for (String path : files) {
                    if (!path.equals(selector) && !((selector.endsWith("/") || selector.endsWith(".")) && path.startsWith(selector))) continue;
                    found = true;
                    String previous = owners.putIfAbsent(path, name);
                    if (previous != null && !previous.equals(name))
                        throw new IllegalArgumentException("Asset belongs to two packages: " + path);
                    if (!assets.child(renames.getOrDefault(path, path)).exists())
                        throw new IllegalArgumentException("Missing packaged asset: " + path);
                }
                if (!found) throw new IllegalArgumentException("No manifest files match: " + selector);
            }
        });
        // Validate the whole plan before moving any payload. No copied duplicate in common assets.
        owners.forEach((path, name) -> {
            String physical = renames.getOrDefault(path, path);
            FileHandle target = release.child("optional/" + name + "/" + physical);
            target.parent().mkdirs();
            FileHandle source = assets.child(physical);
            source.moveTo(target);
            for (FileHandle parent = source.parent(); !parent.equals(assets) && parent.list().length == 0; parent = parent.parent())
                parent.deleteDirectory();
        });
        for (String name : groups.keySet())
            release.child("optional/" + name + "/game.js").writeString(
                TemplateUtil.resolve("subpackage-assets.js", Map.of()), false, "UTF-8");
        assets.child("asset-packages.json").writeString(MiniGameBackend.toJsonObject(owners), false, "UTF-8");
    }
}
