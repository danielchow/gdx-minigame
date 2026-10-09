import {readFileSync} from 'node:fs';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import test from 'node:test';
import {loadEngine, typedArrays} from './engine.mjs';

const packageUrl = new URL('../../../build/regression-probes/pixmap-buffer/minigame/', import.meta.url);
const engineUrl = new URL('subpackages/engine/gdx.wasm.js', packageUrl);

test('pixmaps copy pixels to Java only on demand, keep native ownership and fail cleanly', {timeout: 15000}, async () => {
    const {gdx} = await loadEngine(engineUrl, readFileSync(new URL('gdx.wasm', engineUrl)));
    const stats = {atlasCopies: 0, frees: 0, complete: false};
    const originalSet = Int8Array.prototype.set;
    const originalFree = gdx.Gdx.prototype.g2d_free;
    // Count heap-to-Java copies of the probe's 1024x1024 RGBA atlas, and native frees.
    Int8Array.prototype.set = function(source, offset) {
        if (source.byteLength === 1024 * 1024 * 4 && source.buffer === gdx.HEAP8.buffer
            && this.buffer !== source.buffer) stats.atlasCopies++;
        return originalSet.call(this, source, offset);
    };
    gdx.Gdx.prototype.g2d_free = function(...args) {
        stats.frees++;
        return originalFree.apply(this, args);
    };
    try {
        const app = {exports: {}, console, Gdx: gdx, window: {Gdx: gdx}, __pixmapProbe: stats,
            setTimeout, clearTimeout, ...typedArrays};
        vm.runInNewContext(readFileSync(new URL('subpackages/game/app.js', packageUrl), 'utf8'), app);
        await new Promise((resolve, reject) => app.exports.main([], error => error ? reject(error) : resolve()));
        assert.equal(stats.complete, true, 'all Java assertions must complete');
    } finally {
        Int8Array.prototype.set = originalSet;
        gdx.Gdx.prototype.g2d_free = originalFree;
    }
});
