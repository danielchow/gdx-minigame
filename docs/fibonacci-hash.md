# Allocation-free hash placement

## Problem

Every libGDX hash collection places a key with Fibonacci hashing:

```java
protected int place (K item) { return (int)(item.hashCode() * 0x9E3779B97F4A7C15L >>> shift); }
```

TeaVM 0.13.1 compiles Java `long` to JavaScript `BigInt`. Each BigInt result is a heap object,
and the 64-bit constant is rebuilt from two ints on every call, so one lookup allocates about
15 BigInts (measured: 224 bytes per `ObjectIntMap<String>.get`). The hot caller is
`SpriteBatch.flush → Mesh.render → VertexBufferObject.bind/unbind → ShaderProgram.getAttributeLocation → ObjectIntMap.get`,
twice per vertex attribute per flush. In Solitaire's browser build this one method was about 60%
of all idle JavaScript allocation (3.7 to 4.1 MB/s at 60 fps); the mini game compiles the same
pattern 8 times and pays it on every drawn frame. Upstream has no fix: gdx-teavm 1.6.2 and
libGDX 1.14.0 are unchanged, and TeaVM master (after 0.16.0) makes long operations cheaper but
values stay heap BigInts.

In libGDX 1.14.0 (the version this backend compiles and tests against) the multiplier appears in
exactly eleven classes, all in `com.badlogic.gdx.utils` (the whole gdx jar was scanned by
bytecode): `ObjectMap`, `ObjectSet`, `ObjectIntMap`, `ObjectFloatMap`, `ObjectLongMap`,
`IdentityMap` (`System.identityHashCode`), `IntMap`, `IntSet`, `IntIntMap`, `IntFloatMap` (the
int key is the hash) and `LongMap` (`(key ^ key >>> 32) * C`). `OrderedMap`, `OrderedSet`,
`ArrayMap` and the others inherit or do not hash this way. libGDX 1.14.2 (what Solitaire's mini
game compiles against) adds `LongSet` and changes `IdentityMap.place` to the int-only
`System.identityHashCode(item) >>> shift`. The game's libGDX wins at compile time, so the
transformer must cope with both; it does, and the guards below are what noticed the difference.

## Design

`FibonacciHashTransformer` (a TeaVM `ClassHolderTransformer`, installed by `MiniGamePlugin`)
rewrites each `place()` in place after TeaVM parses it. The hash itself (`hashCode()`,
`identityHashCode()`, the int key) and the `this.shift` read stay; the five instructions
`(long)hash`, the constant, the multiply, the unsigned shift and the `(int)` cast become one static
call to `FibonacciHash.place(hash, shift)`, or `FibonacciHash.placeLong(key, shift)` for `LongMap`.
`FibonacciHash` is ordinary Java in the backend jar, compiled by TeaVM into the game, and computes
the identical slot with int arithmetic (`Math.imul`, shifts, masks), so a lookup allocates nothing.
TeaVM inlines it where it sees fit.

### Why not the other mechanisms

- **Whole-class emulation** (`emu/` sources mapped by `mapPackageHierarchy` in
  `META-INF/teavm.properties`): would copy eleven libGDX classes (ObjectMap alone is 700 lines)
  to change one line each, and would silently diverge from the next libGDX version.
- **`@Emulate(updateCode = true)` method replacement** (`MiniGameClassTransformer`, as
  `ClassEmu`/`ThrowableEmu` use it): eleven stub classes, and it has no guard: if libGDX renames
  or reshapes `place()`, the stub is added as dead code and the original keeps allocating.
- **Caching attribute locations in the emulated `VertexBufferObject`**: fixes only the one
  caller; every other map lookup per frame (fonts, skins, pools, scene2d, game code) keeps
  allocating.
- **A different hash** (no 64-bit multiply): changes slots, so iteration order and anything that
  depends on it would differ from the JVM.

The transformer is one file, covers every collection and every subclass that overrides
`place()`, and is the only option that can verify the method shape.

### Loud failure

Within `com.badlogic.gdx.*`, every method whose program contains the multiplier must be an
`int place(key)` of exactly the expected shape: one body block (TeaVM precedes it with an empty
entry block that jumps to it, and may add `nop`s), the chain connected variable by variable, the
multiplicand either the int key, one call on the object key, or `key ^ key >>> 32` for a long key,
and nothing else in the block. Any deviation is a `Diagnostics` error carrying the method listing,
which `TeaBackend.logBuild` turns into "Build Failed". In addition, every class in
`PLACE_CLASSES` must, when it is compiled, have an `int place(key)` that contains no long
arithmetic (no long constant, long operation or conversion from or to long) after the rewrite.
That is the property we need, so a libGDX that hashes with another long expression fails, while
one that hashes with ints alone (IdentityMap in 1.14.2) passes without a rewrite. Classes outside
libGDX are never rewritten: game code that happens to use the constant is left alone.

### Equivalence

Let `C = HI:LO` with `HI = 0x9E3779B9`, `LO = 0x7F4A7C15` (`LO` is positive as an int). For an
int `hash`, sign-extended to 64 bits:

```
hash * C  mod 2^64  =  hash * LO  +  (hash * HI mod 2^32) << 32        mod 2^64
```

so the product's low word is `hash * LO` (int multiply) and its high word is
`mulHighSigned(hash, LO) + hash * HI` (int add), where `mulHighSigned` is the signed high word of
a 32x32 product (Hacker's Delight 8-2, four 16-bit partial products, no overflow in any step).
`(int)(product >>> shift)` is then `hi >>> (shift - 32)` for shift 32..63, `lo` for 0, and
`(lo >>> shift) | (hi << (32 - shift))` for 1..31. The collections' `shift` is
`Long.numberOfLeadingZeros(mask)` with a table of at most 2^30 entries, so it is 34..63, but the
helper is exact for every shift count (Java masks it to 0..63), so no assumption is needed.

For `LongMap`, the folded key `k = key ^ key >>> 32` has words `keyHi : keyLo ^ keyHi`, and a
64x64 product mod 2^64 has low word `kLo * LO` and high word
`mulHighUnsigned(kLo, LO) + kLo * HI + kHi * LO`, with `mulHighUnsigned(a, LO) = mulHighSigned(a, LO) + (a < 0 ? LO : 0)`.

Tests (`FibonacciHashTransformerTest`, JVM): the transformer runs on the real libGDX classes
parsed by TeaVM from the test classpath; every collection ends up long-free and calling the
helper with its hash producer intact, a `place()` with an extra instruction or with a different
long constant is rejected with the listing, and a class outside libGDX that uses the constant is
untouched.

Tests (`FibonacciHashTest`, JVM): `place(hash, 32)`, the product's high word, is checked for
**every** int (2^32 cases, about 4 s); all shift counts from -70 to 130 with edge and random
hashes; the shift step with random words; `placeLong` with edge and random keys per shift.
`hash-place.test.mjs` (Node, against the real compiled probe) checks that all eleven compiled
collections place 32 int keys (21 long keys) at shifts 63, 58, 53, 47 and 44 exactly as the
BigInt reference, that their iteration order equals the JVM order (`HashPlaceProbe.EXPECTED_ORDER`,
asserted on the JVM by `FibonacciHashTest`), and that no compiled probe contains the BigInt
multiply any more while the int replacement is present.

## Measurement

`src/test/js/hash-place-bench.mjs`, Node 24 on an M4 Max, 1.6 M `ObjectIntMap<String>.get`
over 8 keys in the compiled `hash-place` probe, median of 5, sampled heap allocation including
collected objects. "Before" is the same probe built with the transformer unregistered.

| build | ns per lookup | bytes per lookup |
|---|---|---|
| before (BigInt place) | 1100 | 224 |
| after (int place) | 3.6 | 0 |

The 224 bytes match the browser measurement (about 65 KB per frame for about 290 lookups).
`LongMap.place` still converts its long key into two ints (two BigInt operations); it is not on
a per-frame path.

Solitaire's mini game built against this backend has no occurrence of the BigInt multiply left in
`app.js` (8 before). Device verification (WeChat on Android/iOS) is still to do.

## Browser build

Solitaire's browser build uses gdx-teavm 1.4.0 (`backend-teavm`), whose plugin
(`TeaClassTransformer`) is the one `MiniGameClassTransformer` was copied from. The same
transformer and helper would work there unchanged: it only depends on TeaVM's model API
(`ClassHolderTransformer`, instructions), not on the backend. Solitaire's `teavm/patches/`
mechanism patches the backend's Java sources before compiling; adding the transformer there means
adding the two classes and registering the transformer in the patched plugin, with the same
probe-style test on the generated `app.js`. gdx-teavm 1.4.0 ships TeaVM 0.10/0.11; the parsed
`place()` shape should be the same, and the guard reports if it is not.
