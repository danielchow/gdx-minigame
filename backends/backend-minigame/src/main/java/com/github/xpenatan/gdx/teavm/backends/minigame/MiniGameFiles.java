package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.xpenatan.gdx.teavm.backends.minigame.filesystem.FileDB;
import com.github.xpenatan.gdx.teavm.backends.minigame.filesystem.MemoryFileStorage;
import com.github.xpenatan.gdx.teavm.backends.minigame.filesystem.types.ClasspathStorage;
import com.github.xpenatan.gdx.teavm.backends.minigame.filesystem.types.InternalStorage;

/**
 * Files implementation for WeChat Mini Game.
 * Forked from WebFiles — uses MemoryFileStorage for internal/classpath,
 * and MemoryFileStorage for local (no IndexedDB on WeChat).
 */
public class MiniGameFiles implements Files {

    public MemoryFileStorage internalStorage;
    public MemoryFileStorage classpathStorage;
    public MemoryFileStorage localStorage;
    public String localStoragePrefix;

    public MiniGameFiles(MiniGameApplicationConfiguration config, MiniGameApplication application) {
        this.internalStorage = new InternalStorage();
        this.classpathStorage = this.internalStorage;  // Share same storage — all assets preloaded into internal
        this.localStorage = new MemoryFileStorage();
        localStoragePrefix = config.localStoragePrefix;
    }

    public FileDB getFileDB(FileType type) {
        if(type == FileType.Internal) {
            return internalStorage;
        }
        else if(type == FileType.Classpath) {
            return classpathStorage;
        }
        else if(type == FileType.Local) {
            return localStorage;
        }
        return null;
    }

    @Override
    public FileHandle getFileHandle(String path, FileType type) {
        if(type == FileType.Internal) {
            return internal(path);
        }
        else if(type == FileType.Classpath) {
            return classpath(path);
        }
        else if(type == FileType.Local) {
            return local(path);
        }
        throw new GdxRuntimeException("Type " + type + " is not supported");
    }

    @Override
    public FileHandle classpath(String path) {
        return new MiniGameFileHandle(this, path, FileType.Classpath);
    }

    @Override
    public FileHandle internal(String path) {
        return new MiniGameFileHandle(this, path, FileType.Internal);
    }

    @Override
    public FileHandle local(String path) {
        return new MiniGameFileHandle(this, path, FileType.Local);
    }

    @Override
    public FileHandle external(String path) {
        throw new GdxRuntimeException("Type external is not supported");
    }

    @Override
    public FileHandle absolute(String path) {
        throw new GdxRuntimeException("Type absolute is not supported");
    }

    @Override
    public String getExternalStoragePath() {
        return null;
    }

    @Override
    public boolean isExternalStorageAvailable() {
        return false;
    }

    @Override
    public String getLocalStoragePath() {
        return localStoragePrefix;
    }

    @Override
    public boolean isLocalStorageAvailable() {
        return true;
    }
}
