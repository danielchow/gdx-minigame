import assert from 'node:assert/strict';
import test from 'node:test';
import {loadEngine} from './engine.mjs';

const engineUrl = new URL('../../main/resources/gdx.wasm.js', import.meta.url);

test('void memcpy import returns nothing and copies overlapping ranges', {timeout: 15000}, async () => {
    const {gdx, imports} = await loadEngine(engineUrl);
    // a.b is emscripten_memcpy_js, declared (i32, i32, i32) -> void. Returning the heap from
    // Uint8Array.copyWithin made WeChat's iOS bridge convert the entire 64 MiB heap to a number
    // on every call, freezing or killing WeChat while decoding large textures.
    // Compare types only: a failure message must not format the returned heap either.
    const memcpy = imports.a.b;
    const pointer = gdx._malloc(16);
    try {
        gdx.HEAPU8.set([1, 2, 3, 4, 5, 6, 7, 8], pointer);
        assert.equal(typeof memcpy(pointer + 2, pointer, 6), 'undefined');
        assert.deepEqual(Array.from(gdx.HEAPU8.subarray(pointer, pointer + 8)), [1, 2, 1, 2, 3, 4, 5, 6]);
        assert.equal(typeof memcpy(pointer, pointer + 2, 6), 'undefined');
        assert.deepEqual(Array.from(gdx.HEAPU8.subarray(pointer, pointer + 8)), [1, 2, 3, 4, 5, 6, 5, 6]);
    } finally {
        gdx._free(pointer);
    }
});
