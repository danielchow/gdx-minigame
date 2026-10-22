package com.badlogic.gdx.utils;

/**
 * A libGDX-package collection whose place() hashes with a long constant FibonacciHashTransformer
 * does not know, as a future libGDX might. The transformer must reject it: every place() in
 * com.badlogic.gdx.utils must be free of long arithmetic, or lookups allocate BigInts again.
 */
public class BrokenHashMap<K, V> extends ObjectMap<K, V> {
    @Override
    protected int place(K item) {
        return (int)(item.hashCode() * 0xC2B2AE3D27D4EB4FL >>> shift);
    }
}
