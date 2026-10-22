package com.github.xpenatan.gdx.teavm.backends.minigame.utils;

/**
 * libGDX's Fibonacci hashing step, {@code (int)(hash * 0x9E3779B97F4A7C15L >>> shift)}, computed
 * with 32-bit integer arithmetic only. TeaVM compiles Java {@code long} to JavaScript BigInt, whose
 * every intermediate result is a heap object; the original expression allocates about 15 BigInts per
 * hash-map lookup. {@link com.github.xpenatan.gdx.teavm.backends.minigame.config.plugins.FibonacciHashTransformer}
 * rewrites the collection classes' {@code place()} methods to call these methods instead.
 * <p>
 * Every method here returns exactly what the {@code long} expression returns on the JVM, for every
 * input and every shift count (not only the 34..63 the collections use). See
 * {@code docs/fibonacci-hash.md} for the derivation; {@code FibonacciHashTest} checks the only
 * non-trivial step, {@link #mulHighSigned}, exhaustively for the constant it is used with.
 */
public final class FibonacciHash {
    /** The multiplier libGDX uses: 2^64 / golden ratio, rounded to odd. */
    public static final long MULTIPLIER = 0x9E3779B97F4A7C15L;
    /** The multiplier's high word, {@code (int)(MULTIPLIER >>> 32)}. */
    private static final int HI = 0x9E3779B9;
    /** The multiplier's low word, {@code (int)MULTIPLIER}; positive, which {@link #mulHighSigned} relies on. */
    private static final int LO = 0x7F4A7C15;

    private FibonacciHash() {
    }

    /** {@code (int)((long)hash * MULTIPLIER >>> shift)}: ObjectMap, IntMap, IdentityMap and the other int-hash collections. */
    public static int place(int hash, int shift) {
        // (long)hash * MULTIPLIER mod 2^64, as a high and a low word. The sign extension of hash is
        // carried by the signed high product; hash * HI only contributes its low word.
        int lo = hash * LO;
        int hi = mulHighSigned(hash, LO) + hash * HI;
        return shiftRightUnsigned(hi, lo, shift);
    }

    /** {@code (int)((key ^ key >>> 32) * MULTIPLIER >>> shift)}: LongMap. */
    public static int placeLong(long key, int shift) {
        int keyHi = (int)(key >>> 32);
        int keyLo = (int)key ^ keyHi;
        // (keyHi:keyLo) * (HI:LO) mod 2^64, with keyLo as an unsigned word.
        int lo = keyLo * LO;
        int hi = mulHighSigned(keyLo, LO) + (keyLo < 0 ? LO : 0) + keyLo * HI + keyHi * LO;
        return shiftRightUnsigned(hi, lo, shift);
    }

    /** {@code (int)(((long)hi << 32 | (lo & 0xFFFFFFFFL)) >>> shift)} for any shift count (Java masks it to 0..63). */
    static int shiftRightUnsigned(int hi, int lo, int shift) {
        shift &= 63;
        if(shift >= 32) {
            return hi >>> (shift - 32);
        }
        if(shift == 0) {
            return lo;
        }
        return (lo >>> shift) | (hi << (32 - shift));
    }

    /** The high word of the signed 64-bit product {@code (long)a * b}; Hacker's Delight 8-2, mulhs. */
    static int mulHighSigned(int a, int b) {
        int aLo = a & 0xFFFF;
        int aHi = a >> 16;
        int bLo = b & 0xFFFF;
        int bHi = b >> 16;
        int loLo = aLo * bLo;
        int t = aHi * bLo + (loLo >>> 16);
        int w1 = aLo * bHi + (t & 0xFFFF);
        return aHi * bHi + (t >> 16) + (w1 >> 16);
    }
}
