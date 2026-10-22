package com.github.xpenatan.gdx.teavm.backends.minigame.regression;

import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.LongMap;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectLongMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Exposes the slot every libGDX hash collection computes for a key, and the iteration orders that
 * follow from it, so hash-place.test.mjs can compare the compiled game (where
 * FibonacciHashTransformer rewrote place()) against the JVM. The fixtures are plain Java; JUnit's
 * FibonacciHashTest runs the same ones on the JVM.
 */
public final class HashPlaceProbe {
    /** Int keys, and the hashCode() of the Integer keys: zeros, sign edges, word edges, bit patterns. */
    public static final int[] KEYS = {0, 1, -1, 2, 7, 31, 32, 33, 100, 1000, 65535, 65536, 0x7FFF, 0x8000, 0xFFFF0000,
        0x12345678, 0x9ABCDEF0, 0xDEADBEEF, 0x7FFFFFFF, 0x80000000, 0x80000001, 0x40000000, 0x3FFFFFFF, 0xC0000000,
        0x9E3779B9, 0x7F4A7C15, 0x61C88647, 0x5BD1E995, 0x27D4EB2D, 0xA5A5A5A5, 0x5A5A5A5A, 123456789};
    public static final long[] LONG_KEYS = {0L, 1L, -1L, 2L, 32L, 0xFFFFFFFFL, 0x100000000L, 0x7FFFFFFFL, 0x80000000L,
        0xFFFFFFFF80000000L, 0x7FFFFFFFFFFFFFFFL, 0x8000000000000000L, 0x8000000000000001L, 0x123456789ABCDEF0L,
        0x9E3779B97F4A7C16L, 0xDEADBEEFCAFEBABEL, 0x0000000100000001L, 0x00000001FFFFFFFFL, 0xFFFFFFFF00000001L,
        -1234567890123L, 1234567890123L};
    /** Initial capacities that give the collections shifts 63, 58, 53, 47 and 44. */
    static final int[] CAPACITIES = {1, 51, 1000, 60000, 1 << 19};

    /** order() on the JVM (libGDX 1.14.0); hash-place.test.mjs reads this literal and FibonacciHashTest asserts it. */
    public static final String EXPECTED_ORDER =
        "ObjectMap:0,1000,2135587861,32767,2147483647,1515870810,31,2,-2147483648,-1515870811,1640531527,7,1073741824,-1,33,65536,-559038737,-65536,305419896,1,-1073741824,-1640531527,32768,1073741823,32,123456789,100,668265261,65535,-2147483647,1540483477,-1698898192,\n" +
        "IntMap:0,1000,2135587861,32767,2147483647,1515870810,31,2,-2147483648,-1515870811,1640531527,7,1073741824,-1,33,65536,-559038737,-65536,305419896,1,-1073741824,-1640531527,32768,1073741823,32,123456789,100,668265261,65535,-2147483647,1540483477,-1698898192,\n" +
        "ObjectSet:0,1000,2135587861,32767,2147483647,1515870810,31,2,-2147483648,-1515870811,1640531527,7,1073741824,-1,33,65536,-559038737,-65536,305419896,1,-1073741824,-1640531527,32768,1073741823,32,123456789,100,668265261,65535,-2147483647,1540483477,-1698898192,\n" +
        "IntSet:0,1000,2135587861,32767,2147483647,1515870810,31,2,-2147483648,-1515870811,1640531527,7,1073741824,-1,33,65536,-559038737,-65536,305419896,1,-1073741824,-1640531527,32768,1073741823,32,123456789,100,668265261,65535,-2147483647,1540483477,-1698898192,\n" +
        "LongMap:0,1234567890123,-2401053089206453570,4294967296,2147483647,1311768467463790320,2,-9223372036854775808,4294967297,-1,-7046029254386353130,1,-2147483648,2147483648,32,9223372036854775807,8589934591,-9223372036854775807,4294967295,-4294967295,-1234567890123,\n";

    static final String[] CLASSES = {"ObjectMap", "ObjectSet", "ObjectIntMap", "ObjectFloatMap", "ObjectLongMap",
        "IntMap", "IntSet", "IntIntMap", "IntFloatMap", "IdentityMap", "LongMap"};

    public static void main(String[] args) {
        register((command, arg) -> {
            switch(command) {
                case "slots": return slots();
                case "order": return order();
                case "bench": return String.valueOf(bench(arg));
                default: throw new IllegalArgumentException(command);
            }
        });
    }

    /**
     * One line per collection class and capacity: {@code Class|shift|key=slot,key=slot,...}. The slot is
     * what place() returns; for IdentityMap the key column is the object's identity hash.
     */
    public static String slots() {
        StringBuilder out = new StringBuilder();
        for(String name : CLASSES) {
            for(int capacity : CAPACITIES) {
                Placer placer = create(name, capacity);
                out.append(name).append('|').append(placer.shift()).append('|');
                if(name.equals("LongMap")) {
                    for(int i = 0; i < LONG_KEYS.length; i++) {
                        if(i > 0) out.append(',');
                        out.append(LONG_KEYS[i]).append('=').append(placer.place(LONG_KEYS[i]));
                    }
                }
                else {
                    for(int i = 0; i < KEYS.length; i++) {
                        if(i > 0) out.append(',');
                        out.append(placer.key(KEYS[i])).append('=').append(placer.place(KEYS[i]));
                    }
                }
                out.append('\n');
            }
        }
        return out.toString();
    }

    /** The iteration order of KEYS inserted into tiny collections, which depends on place() and collisions. */
    public static String order() {
        StringBuilder out = new StringBuilder();
        ObjectMap<Integer, Integer> objectMap = new ObjectMap<>(1);
        IntMap<Integer> intMap = new IntMap<>(1);
        ObjectSet<Integer> objectSet = new ObjectSet<>(1);
        IntSet intSet = new IntSet(1);
        LongMap<Integer> longMap = new LongMap<>(1);
        for(int key : KEYS) {
            objectMap.put(key, key);
            intMap.put(key, key);
            objectSet.add(key);
            intSet.add(key);
        }
        for(long key : LONG_KEYS) {
            longMap.put(key, 0);
        }
        out.append("ObjectMap:");
        for(Integer key : objectMap.keys()) out.append(key).append(',');
        out.append("\nIntMap:");
        for(IntMap.Entry<Integer> entry : intMap) out.append(entry.key).append(',');
        out.append("\nObjectSet:");
        for(Integer key : objectSet) out.append(key).append(',');
        out.append("\nIntSet:");
        IntSet.IntSetIterator intKeys = intSet.iterator();
        while(intKeys.hasNext) out.append(intKeys.next()).append(',');
        out.append("\nLongMap:");
        for(LongMap.Entry<Integer> entry : longMap) out.append(entry.key).append(',');
        out.append('\n');
        return out.toString();
    }

    /** {@code rounds} lookups of 8 string keys in an ObjectIntMap, like ShaderProgram.getAttributeLocation per flush. */
    static int bench(int rounds) {
        String[] names = {"a_position", "a_color", "a_texCoord0", "a_normal", "a_tangent", "a_binormal", "a_boneWeight0",
            "a_boneWeight1"};
        ObjectIntMap<String> locations = new ObjectIntMap<>();
        for(int i = 0; i < names.length; i++) {
            locations.put(names[i], i);
        }
        int sum = 0;
        for(int round = 0; round < rounds; round++) {
            for(String name : names) {
                sum += locations.get(name, -1);
            }
        }
        return sum;
    }

    interface Placer {
        int shift();

        int place(int key);

        default int place(long key) {
            throw new UnsupportedOperationException();
        }

        /** What the slot was computed from: the int key, or the identity hash for IdentityMap. */
        default int key(int key) {
            return key;
        }
    }

    /** A collection of the named class with the initial capacity, exposing its protected place() and shift. */
    static Placer create(String name, int capacity) {
        switch(name) {
            case "ObjectMap": {
                class P extends ObjectMap<Integer, Object> implements Placer {
                    P() { super(capacity); }
                    public int place(int key) { return place(Integer.valueOf(key)); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            case "ObjectSet": {
                class P extends ObjectSet<Integer> implements Placer {
                    P() { super(capacity); }
                    public int place(int key) { return place(Integer.valueOf(key)); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            case "ObjectIntMap": {
                class P extends ObjectIntMap<Integer> implements Placer {
                    P() { super(capacity); }
                    public int place(int key) { return place(Integer.valueOf(key)); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            case "ObjectFloatMap": {
                class P extends ObjectFloatMap<Integer> implements Placer {
                    P() { super(capacity); }
                    public int place(int key) { return place(Integer.valueOf(key)); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            case "ObjectLongMap": {
                class P extends ObjectLongMap<Integer> implements Placer {
                    P() { super(capacity); }
                    public int place(int key) { return place(Integer.valueOf(key)); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            case "IntMap": {
                class P extends IntMap<Object> implements Placer {
                    P() { super(capacity); }
                    public int place(int key) { return super.place(key); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            case "IntSet": {
                class P extends IntSet implements Placer {
                    P() { super(capacity); }
                    public int place(int key) { return super.place(key); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            case "IntIntMap": {
                class P extends IntIntMap implements Placer {
                    P() { super(capacity); }
                    public int place(int key) { return super.place(key); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            case "IntFloatMap": {
                class P extends IntFloatMap implements Placer {
                    P() { super(capacity); }
                    public int place(int key) { return super.place(key); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            case "IdentityMap": {
                // The int key only selects one of 32 objects; the slot comes from that object's identity hash.
                class P extends IdentityMap<Object, Object> implements Placer {
                    final Object[] objects = new Object[32];
                    P() {
                        super(capacity);
                        for(int i = 0; i < objects.length; i++) objects[i] = new Object();
                    }
                    public int place(int key) { return place(objects[Math.abs(key % objects.length)]); }
                    public int key(int key) { return System.identityHashCode(objects[Math.abs(key % objects.length)]); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            case "LongMap": {
                class P extends LongMap<Object> implements Placer {
                    P() { super(capacity); }
                    public int place(int key) { return super.place((long)key); }
                    public int place(long key) { return super.place(key); }
                    public int shift() { return shift; }
                }
                return new P();
            }
            default: throw new IllegalArgumentException(name);
        }
    }

    @JSFunctor
    interface Command extends JSObject {
        String run(String command, int arg);
    }

    @JSBody(params = "command", script = "globalThis.__hashPlaceProbe = command;")
    private static native void register(Command command);
}
