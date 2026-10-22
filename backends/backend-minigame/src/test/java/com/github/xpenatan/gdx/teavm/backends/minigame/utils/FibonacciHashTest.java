package com.github.xpenatan.gdx.teavm.backends.minigame.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.github.xpenatan.gdx.teavm.backends.minigame.regression.HashPlaceProbe;
import java.util.Random;
import org.junit.Test;

/**
 * FibonacciHash must return, bit for bit, what libGDX's long expression returns on the JVM. The
 * product's high word is the only non-trivial step and is checked for every int; the shift step is
 * checked for every shift count with many words; the long-key variant with random and edge keys.
 */
public class FibonacciHashTest {
    private static final long C = FibonacciHash.MULTIPLIER;

    private static int referencePlace(int hash, int shift) {
        return (int)(hash * C >>> shift);
    }

    private static int referencePlaceLong(long key, int shift) {
        return (int)((key ^ key >>> 32) * C >>> shift);
    }

    @Test
    public void placeMatchesTheHighWordForEveryInt() {
        // shift 32 returns the product's high word unchanged, which every map shift (34..63) is derived from.
        for(long i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE; i++) {
            int hash = (int)i;
            int expected = referencePlace(hash, 32);
            int actual = FibonacciHash.place(hash, 32);
            if(expected != actual) {
                fail("place(" + hash + ", 32): expected " + expected + ", got " + actual);
            }
        }
    }

    @Test
    public void placeMatchesForEveryShift() {
        Random random = new Random(42);
        for(int shift = -70; shift <= 130; shift++) {
            for(int hash : HashPlaceProbe.KEYS) {
                assertEquals("place(" + hash + ", " + shift + ")", referencePlace(hash, shift), FibonacciHash.place(hash, shift));
            }
            for(int i = 0; i < 20_000; i++) {
                int hash = random.nextInt();
                assertEquals("place(" + hash + ", " + shift + ")", referencePlace(hash, shift), FibonacciHash.place(hash, shift));
            }
        }
    }

    @Test
    public void placeLongMatchesForEveryShift() {
        Random random = new Random(7);
        for(int shift = -70; shift <= 130; shift++) {
            for(long key : HashPlaceProbe.LONG_KEYS) {
                assertEquals("placeLong(" + key + ", " + shift + ")", referencePlaceLong(key, shift), FibonacciHash.placeLong(key, shift));
            }
            for(int i = 0; i < 20_000; i++) {
                long key = random.nextLong();
                assertEquals("placeLong(" + key + ", " + shift + ")", referencePlaceLong(key, shift), FibonacciHash.placeLong(key, shift));
                // Low words with the sign bit set or clear, high words of every kind.
                key = (long)random.nextInt() << 32 | (random.nextInt() & 0xFFFFFFFFL);
                assertEquals("placeLong(" + key + ", " + shift + ")", referencePlaceLong(key, shift), FibonacciHash.placeLong(key, shift));
            }
        }
    }

    @Test
    public void unsignedShiftMatchesForEveryShiftCount() {
        Random random = new Random(3);
        for(int shift = -70; shift <= 130; shift++) {
            for(int i = 0; i < 5_000; i++) {
                int hi = random.nextInt();
                int lo = random.nextInt();
                long word = (long)hi << 32 | (lo & 0xFFFFFFFFL);
                assertEquals("shift " + shift + " of " + word, (int)(word >>> shift), FibonacciHash.shiftRightUnsigned(hi, lo, shift));
            }
        }
    }

    @Test
    public void probeFixturesMatchTheJvm() {
        // The same fixtures hash-place.test.mjs checks in the compiled game; the expected orders below are
        // what the JVM produces, so both runtimes are tied to one literal.
        assertEquals(HashPlaceProbe.EXPECTED_ORDER, HashPlaceProbe.order());
        for(String line : HashPlaceProbe.slots().split("\n")) {
            String[] parts = line.split("\\|");
            int shift = Integer.parseInt(parts[1]);
            for(String pair : parts[2].split(",")) {
                long key = Long.parseLong(pair.substring(0, pair.indexOf('=')));
                int slot = Integer.parseInt(pair.substring(pair.indexOf('=') + 1));
                int expected = parts[0].equals("LongMap") ? referencePlaceLong(key, shift) : referencePlace((int)key, shift);
                assertEquals(line, expected, slot);
            }
        }
    }
}
