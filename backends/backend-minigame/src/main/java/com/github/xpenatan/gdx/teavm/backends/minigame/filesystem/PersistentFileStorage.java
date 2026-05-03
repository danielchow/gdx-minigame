package com.github.xpenatan.gdx.teavm.backends.minigame.filesystem;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.xpenatan.gdx.teavm.backends.minigame.MiniGameFileHandle;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Int8Array;

public class PersistentFileStorage extends FileDB {

    private final JSObject fsm;
    private final String basePath;

    public PersistentFileStorage(String prefix) {
        this.fsm = WX.getFileSystemManager();
        String userDataPath = WX.getUserDataPath();
        if (userDataPath.endsWith("/")) {
            userDataPath = userDataPath.substring(0, userDataPath.length() - 1);
        }
        this.basePath = userDataPath + "/" + prefix;
        // Ensure base directory exists
        WX.fsMkdirSync(fsm, basePath);
    }

    private String toWXPath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return basePath;
        }
        // Strip leading / to prevent double slashes
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return basePath + "/" + relativePath;
    }

    private boolean isRootPath(String path) {
        return path == null || path.isEmpty() || path.equals(".") || path.equals("/");
    }

    @Override
    public InputStream read(MiniGameFileHandle file) {
        byte[] bytes = readBytes(file);
        if (bytes == null) {
            return null;
        }
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public byte[] readBytes(MiniGameFileHandle file) {
        String wxPath = toWXPath(file.path());
        ArrayBuffer ab = WX.fsReadFileSync(fsm, wxPath);
        if (ab == null) {
            return null;
        }
        Int8Array int8 = new Int8Array(ab);
        return int8.copyToJavaArray();
    }

    @Override
    protected void writeInternal(MiniGameFileHandle file, byte[] data, boolean append, int expectedLength) {
        String relativePath = file.path();
        String wxPath = toWXPath(relativePath);

        byte[] writeData = data;
        if (append && WX.fsExists(fsm, wxPath)) {
            ArrayBuffer existingAb = WX.fsReadFileSync(fsm, wxPath);
            if (existingAb != null) {
                byte[] existing = new Int8Array(existingAb).copyToJavaArray();
                byte[] combined = new byte[existing.length + data.length];
                System.arraycopy(existing, 0, combined, 0, existing.length);
                System.arraycopy(data, 0, combined, existing.length, data.length);
                writeData = combined;
            }
        }

        // Ensure parent directories exist
        FileHandle parent = file.parent();
        if (parent != null && !isRootPath(parent.path())) {
            WX.fsMkdirSync(fsm, toWXPath(parent.path()));
        }

        Int8Array int8 = Int8Array.copyFromJavaArray(writeData);
        WX.fsWriteFileSync(fsm, wxPath, int8.getBuffer());
    }

    @Override
    protected String[] paths(MiniGameFileHandle file) {
        String relativePath = file.path();
        String wxPath = toWXPath(relativePath);

        String[] childNames = WX.fsReaddirSync(fsm, wxPath);
        if (childNames == null || childNames.length == 0) {
            return new String[0];
        }

        List<String> result = new ArrayList<>();
        for (String childName : childNames) {
            String childRelativePath;
            if (isRootPath(relativePath)) {
                childRelativePath = childName;
            } else {
                childRelativePath = relativePath + "/" + childName;
            }
            result.add(childRelativePath);
        }
        return result.toArray(new String[0]);
    }

    @Override
    public boolean isDirectory(MiniGameFileHandle file) {
        String relativePath = file.path();
        if (isRootPath(relativePath)) {
            return true;
        }
        return WX.fsIsDirectory(fsm, toWXPath(relativePath));
    }

    @Override
    public void mkdirs(MiniGameFileHandle file) {
        String relativePath = file.path();
        if (isRootPath(relativePath)) {
            return;
        }
        WX.fsMkdirSync(fsm, toWXPath(relativePath));
    }

    @Override
    public boolean exists(MiniGameFileHandle file) {
        String relativePath = file.path();
        if (isRootPath(relativePath)) {
            return true;
        }
        return WX.fsExists(fsm, toWXPath(relativePath));
    }

    @Override
    public boolean delete(MiniGameFileHandle file) {
        String relativePath = file.path();
        if (isRootPath(relativePath)) {
            return false;
        }
        String wxPath = toWXPath(relativePath);
        if (WX.fsIsDirectory(fsm, wxPath)) {
            return false;
        }
        return WX.fsUnlinkSync(fsm, wxPath);
    }

    @Override
    public boolean deleteDirectory(MiniGameFileHandle file) {
        String relativePath = file.path();
        if (isRootPath(relativePath)) {
            return false;
        }
        String wxPath = toWXPath(relativePath);
        if (!WX.fsIsDirectory(fsm, wxPath)) {
            return false;
        }
        return WX.fsRmdirSync(fsm, wxPath);
    }

    @Override
    public long length(MiniGameFileHandle file) {
        String relativePath = file.path();
        if (isRootPath(relativePath)) {
            return 0;
        }
        String wxPath = toWXPath(relativePath);
        if (WX.fsIsDirectory(fsm, wxPath)) {
            return 0;
        }
        return (long) WX.fsFileSize(fsm, wxPath);
    }

    @Override
    public void rename(MiniGameFileHandle source, MiniGameFileHandle target) {
        String sourceWxPath = toWXPath(source.path());
        String targetWxPath = toWXPath(target.path());

        FileHandle targetParent = target.parent();
        if (targetParent != null && !isRootPath(targetParent.path())) {
            WX.fsMkdirSync(fsm, toWXPath(targetParent.path()));
        }

        boolean ok = WX.fsRenameSync(fsm, sourceWxPath, targetWxPath);
        if (!ok) {
            throw new GdxRuntimeException("Failed to rename " + source.path() + " to " + target.path());
        }
    }
}
