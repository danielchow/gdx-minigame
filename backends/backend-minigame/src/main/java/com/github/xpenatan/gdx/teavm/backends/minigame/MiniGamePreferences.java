package com.github.xpenatan.gdx.teavm.backends.minigame;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.xpenatan.gdx.teavm.backends.minigame.bindings.WX;
import com.github.xpenatan.gdx.teavm.backends.minigame.filesystem.HEXCoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Preferences implementation using wx.setStorageSync/getStorageSync.
 * Forked from WebPreferences, replacing localStorage with WX storage.
 */
public class MiniGamePreferences implements Preferences {

    private static final String ID_FOR_PREF = "pref:";

    final String prefix;
    ObjectMap<String, Object> values = new ObjectMap<String, Object>();
    private final com.badlogic.gdx.utils.Array<String> removedKeys = new com.badlogic.gdx.utils.Array<>();
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
                    boolean flag = key.startsWith(this.prefix);
                    if(flag) {
                        String value = WX.getStorageSync(keyEncoded);
                        String keyStr = key.substring(prefixLength, key.length() - 1);
                        Object object;
                        if(shouldEncode) {
                            object = toObject(key, new String(HEXCoder.decode(value)));
                        }
                        else {
                            object = toObject(key, value);
                        }
                        values.put(keyStr, object);
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            values.clear();
        }
    }

    private Object toObject(String key, String value) {
        if(key.endsWith("b")) return Boolean.valueOf(value);
        if(key.endsWith("i")) return Integer.valueOf(value);
        if(key.endsWith("l")) return Long.valueOf(value);
        if(key.endsWith("f")) return Float.valueOf(value);
        return value;
    }

    private String toStorageKey(String key, Object value) {
        if(value instanceof Boolean) return prefix + key + "b";
        if(value instanceof Integer) return prefix + key + "i";
        if(value instanceof Long) return prefix + key + "l";
        if(value instanceof Float) return prefix + key + "f";
        return prefix + key + "s";
    }

    private String typeSuffix(Object value) {
        if(value instanceof Boolean) return "b";
        if(value instanceof Integer) return "i";
        if(value instanceof Long) return "l";
        if(value instanceof Float) return "f";
        return "s";
    }

    @Override
    public void flush() {
        try {
            // Remove deleted keys from WX storage
            for (int i = 0; i < removedKeys.size; i++) {
                String key = removedKeys.get(i);
                String[] suffixes = {"b", "i", "l", "f", "s"};
                for (String suffix : suffixes) {
                    String storageKey = prefix + key + suffix;
                    if (shouldEncode) {
                        storageKey = HEXCoder.encode(storageKey.getBytes());
                    }
                    WX.removeStorageSync(storageKey);
                }
            }
            removedKeys.clear();

            // Write all values to WX storage
            for(String key : values.keys()) {
                Object val = values.get(key);
                String storageKey = toStorageKey(key, val);
                String storageValue = val.toString();
                if(shouldEncode) {
                    WX.setStorageSync(HEXCoder.encode(storageKey.getBytes()), HEXCoder.encode(storageValue.getBytes()));
                } else {
                    WX.setStorageSync(storageKey, storageValue);
                }
            }
        } catch(Exception e) {
            throw new GdxRuntimeException("Couldn't flush preferences", e);
        }
    }

    @Override
    public Preferences putBoolean(String key, boolean val) {
        values.put(key, val);
        return this;
    }

    @Override
    public Preferences putInteger(String key, int val) {
        values.put(key, val);
        return this;
    }

    @Override
    public Preferences putLong(String key, long val) {
        values.put(key, val);
        return this;
    }

    @Override
    public Preferences putFloat(String key, float val) {
        values.put(key, val);
        return this;
    }

    @Override
    public Preferences putString(String key, String val) {
        values.put(key, val);
        return this;
    }

    @Override
    public Preferences put(Map<String, ?> vals) {
        for(String key : vals.keySet()) {
            values.put(key, vals.get(key));
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
        Boolean res = (Boolean) values.get(key);
        return res == null ? defValue : res;
    }

    @Override
    public int getInteger(String key, int defValue) {
        Integer res = (Integer) values.get(key);
        return res == null ? defValue : res;
    }

    @Override
    public long getLong(String key, long defValue) {
        Long res = (Long) values.get(key);
        return res == null ? defValue : res;
    }

    @Override
    public float getFloat(String key, float defValue) {
        Float res = (Float) values.get(key);
        return res == null ? defValue : res;
    }

    @Override
    public String getString(String key, String defValue) {
        String res = (String) values.get(key);
        return res == null ? defValue : res;
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
        for (String key : values.keys()) {
            removedKeys.add(key);
        }
        values.clear();
    }

    @Override
    public void remove(String key) {
        values.remove(key);
        removedKeys.add(key);
    }
}
