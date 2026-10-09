package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import com.github.xpenatan.gdx.teavm.backends.minigame.filesystem.HEXCoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Preferences implementation using wx.setStorageSync/getStorageSync.
 * Forked from WebPreferences, replacing localStorage with WX storage.
 *
 * <p>Each value is stored under {@code "pref:" + name + ":" + key + suffix}, where the suffix is its
 * type: b(oolean), i(nteger), l(ong), f(loat) or s(tring). Every storage call is a synchronous disk
 * write, so {@link #flush()} only writes keys changed since the last flush and only removes keys
 * that were removed or whose type changed.
 *
 * <p>Storage written by older versions can hold one key under several suffixes. On load the first
 * suffix in {@link #LOAD_PRIORITY} wins; the others are deleted at the next flush.
 *
 * <p>Like libGDX's desktop preferences, which keep every value as a string, a getter whose type
 * differs from the stored value's parses that value's string form, and returns the default when
 * it can't be parsed.
 */
public class MiniGamePreferences implements Preferences {

    private static final String ID_FOR_PREF = "pref:";
    /** Type suffixes in the order that wins when one key is stored under several. */
    private static final String LOAD_PRIORITY = "sflib";

    final String prefix;
    ObjectMap<String, Object> values = new ObjectMap<String, Object>();
    /** Storage key (before encoding) each loaded or flushed key is stored under. */
    private final ObjectMap<String, String> storedKeys = new ObjectMap<>();
    private final ObjectSet<String> changedKeys = new ObjectSet<>();
    private final ObjectSet<String> removedKeys = new ObjectSet<>();
    /** Storage keys (before encoding) that lost to another type of the same key on load. */
    private final Array<String> staleStorageKeys = new Array<>();
    private boolean shouldEncode;

    public MiniGamePreferences(String prefix, boolean shouldEncode) {
        this.prefix = ID_FOR_PREF + prefix + ":";
        this.shouldEncode = shouldEncode;
        int prefixLength = this.prefix.length();
        try {
            String[] allKeys = WX.getStorageInfoKeys();
            if(allKeys != null) {
                for(String keyEncoded : allKeys) {
                    String key = keyEncoded;
                    if(shouldEncode) {
                        key = new String(HEXCoder.decode(keyEncoded));
                    }
                    if(!key.startsWith(this.prefix) || key.length() == prefixLength) continue;
                    char suffix = key.charAt(key.length() - 1);
                    if(LOAD_PRIORITY.indexOf(suffix) < 0) continue;
                    String keyStr = key.substring(prefixLength, key.length() - 1);
                    String loaded = storedKeys.get(keyStr);
                    if(loaded != null && priority(loaded) < priority(key)) {
                        staleStorageKeys.add(key);
                        continue;
                    }
                    if(loaded != null) staleStorageKeys.add(loaded);
                    String value = WX.getStorageSync(keyEncoded);
                    if(shouldEncode) {
                        value = new String(HEXCoder.decode(value));
                    }
                    values.put(keyStr, toObject(suffix, value));
                    storedKeys.put(keyStr, key);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            values.clear();
            storedKeys.clear();
            staleStorageKeys.clear();
        }
    }

    private static int priority(String storageKey) {
        return LOAD_PRIORITY.indexOf(storageKey.charAt(storageKey.length() - 1));
    }

    private static Object toObject(char suffix, String value) {
        try {
            switch(suffix) {
                case 'b': return Boolean.valueOf(value);
                case 'i': return Integer.valueOf(value);
                case 'l': return Long.valueOf(value);
                case 'f': return Float.valueOf(value);
            }
        }
        catch(NumberFormatException e) {
            // Keep the raw text; the getters parse or fall back to their defaults
        }
        return value;
    }

    private String toStorageKey(String key, Object value) {
        if(value instanceof Boolean) return prefix + key + "b";
        if(value instanceof Integer) return prefix + key + "i";
        if(value instanceof Long) return prefix + key + "l";
        if(value instanceof Float) return prefix + key + "f";
        return prefix + key + "s";
    }

    private String encode(String text) {
        return shouldEncode ? HEXCoder.encode(text.getBytes()) : text;
    }

    @Override
    public void flush() {
        try {
            for(String key : removedKeys) {
                String storageKey = storedKeys.remove(key);
                if(storageKey != null) WX.removeStorageSync(encode(storageKey));
            }
            removedKeys.clear();
            for(String storageKey : staleStorageKeys) {
                WX.removeStorageSync(encode(storageKey));
            }
            staleStorageKeys.clear();
            for(String key : changedKeys) {
                Object val = values.get(key);
                String storageKey = toStorageKey(key, val);
                String previous = storedKeys.put(key, storageKey);
                if(previous != null && !previous.equals(storageKey)) {
                    // The type changed; the old typed key would otherwise come back on load
                    WX.removeStorageSync(encode(previous));
                }
                WX.setStorageSync(encode(storageKey), encode(val.toString()));
            }
            changedKeys.clear();
        } catch(Exception e) {
            throw new GdxRuntimeException("Couldn't flush preferences", e);
        }
    }

    private Preferences putValue(String key, Object val) {
        values.put(key, val);
        changedKeys.add(key);
        removedKeys.remove(key);
        return this;
    }

    @Override
    public Preferences putBoolean(String key, boolean val) {
        return putValue(key, val);
    }

    @Override
    public Preferences putInteger(String key, int val) {
        return putValue(key, val);
    }

    @Override
    public Preferences putLong(String key, long val) {
        return putValue(key, val);
    }

    @Override
    public Preferences putFloat(String key, float val) {
        return putValue(key, val);
    }

    @Override
    public Preferences putString(String key, String val) {
        return putValue(key, val);
    }

    @Override
    public Preferences put(Map<String, ?> vals) {
        for(String key : vals.keySet()) {
            putValue(key, vals.get(key));
        }
        return this;
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Override
    public int getInteger(String key) {
        return getInteger(key, 0);
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Override
    public float getFloat(String key) {
        return getFloat(key, 0);
    }

    @Override
    public String getString(String key) {
        return getString(key, "");
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Object res = values.get(key);
        if(res instanceof Boolean) return (Boolean) res;
        if(res == null) return defValue;
        String text = res.toString();
        if(text.equalsIgnoreCase("true")) return true;
        if(text.equalsIgnoreCase("false")) return false;
        return defValue;
    }

    @Override
    public int getInteger(String key, int defValue) {
        Object res = values.get(key);
        if(res instanceof Integer) return (Integer) res;
        if(res == null) return defValue;
        try {
            return Integer.parseInt(res.toString());
        }
        catch(NumberFormatException e) {
            return defValue;
        }
    }

    @Override
    public long getLong(String key, long defValue) {
        Object res = values.get(key);
        if(res instanceof Long) return (Long) res;
        if(res == null) return defValue;
        try {
            return Long.parseLong(res.toString());
        }
        catch(NumberFormatException e) {
            return defValue;
        }
    }

    @Override
    public float getFloat(String key, float defValue) {
        Object res = values.get(key);
        if(res instanceof Float) return (Float) res;
        if(res == null) return defValue;
        try {
            return Float.parseFloat(res.toString());
        }
        catch(NumberFormatException e) {
            return defValue;
        }
    }

    @Override
    public String getString(String key, String defValue) {
        Object res = values.get(key);
        return res == null ? defValue : res.toString();
    }

    @Override
    public Map<String, ?> get() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for(String key : values.keys()) {
            map.put(key, values.get(key));
        }
        return map;
    }

    @Override
    public boolean contains(String key) {
        return values.containsKey(key);
    }

    @Override
    public void clear() {
        for(String key : storedKeys.keys()) {
            removedKeys.add(key);
        }
        values.clear();
        changedKeys.clear();
    }

    @Override
    public void remove(String key) {
        values.remove(key);
        changedKeys.remove(key);
        removedKeys.add(key);
    }
}
